package org.apache.lucene.demo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.NodeList;

/** Simple command-line based search demo. */
public class SearchFiles {
	
	private static List<String> stopWords= Arrays.asList("mucho", "tuviese", "m�os", "seremos", "del", "ser�n", "vuestros", "hube", "tus"
			, "habr�amos", "les", "est�", "estuvierais", "hubieras", "otras", "m�", "habido", "est�", "estar�n"
			, "fuera", "algunas", "tendr�as", "estar�s", "habr�is", "al", "tengan", "quien", "son", "habr�an", "tengas", 
			"habr�as", "estar", "estas", "sobre", "estar�is", "unos", "nosotros", "tuvieron", "m�a", "estuviste", "estar�an", 
			"se�is", "hab�amos", "todo", "tiene", "estar�as", "tuyas", "tenemos", "hubi�ramos", "m�o", "sus", "se", "algo", 
			"tendr�n", "mis", "por", "ser�as", "sea", "estuvi�ramos", "ella", "vuestras", "necesito", "tendr�s", "a", "su", "teniendo",
			"esas", "fuiste", "tuvieseis", "o", "te", "ti", "habr�n", "tendremos", "tuve", "y", "habr�s", "hubimos", "fueran", "tu",
			"fuese", "tendr�", "est�is", "sois", "suyos", "ser�", "fueras", "nosotras", "tuvieran", "tendr�an", "tuvieras",
			"estaban", "han", "habidas", "tuvi�semos", "estabas", "has", "fueron", "ten�an", "estuve", "hay", "tendr�", "ten�as", "�l", 
			"el", "estuvo", "en", "hab�is", "poco", "es", "est�s", "hab�an", "est�n", "teng�is", "hab�as", "ser�s", "hubisteis", "otros", 
			"hubiera", "que", "vosotros", "ser�is", "era", "suyas", "e", "tuviesen", "tambi�n", "hayan", "nuestra", "tuvieses", "hayas", 
			"otra", "vuestra", "sin", "s�", "fueseis", "nuestro", "esta", "m�s", "eras", "contra", "otro", "hubiese", "vuestro",
			"tenidos", "una", "estada", "cuando", "estuvisteis", "todos", "estuvieron", "muchos", "pero", "hemos", "uno", "habidos", 
			"habr�", "ha", "tuvo", "tuvimos", "he", "estuviesen", "habr�", "donde", "t�", "tienen", "ya", "fueses", "estemos", 
			"tienes", "ten�is", "como", "esos", "estad", "fuisteis", "habremos", "yo", "nuestros", "seamos", "estaba", "siendo", 
			"ser�amos", "fuimos", "estos", "ser�", "fuesen", "estadas", "tendr�ais", "hay�is", "tuvi�ramos", "estuvimos", "somos", 
			"est�bamos", "ten�ais", "hubieses", "estuvieran", "hubo", "un", "estuvieras", "estaremos", "qu�", "hab�ais", "tuya", 
			"hubi�semos", "fuerais", "hubieseis", "nos", "los", "est�is", "ser�ais", "tuvierais", "tuyo", "estuvi�semos", 
			"quienes", "hab�a", "ante", "no", "hubieron", "sido", "nuestras", "estado", "tenida", "tenido", "nada", "estuviera", 
			"esa", "ser�a", "la", "fue", "ese", "le", "ser�an", "tened", "habida", "estuvieses", "estuvieseis", "ten�amos", 
			"eso", "con", "lo", "vosotras", "tuviera", "fui", "tuviste", "estar�", "estar�a", "erais", "porque", "hubiste", "estar�",
			"estuviese", "estamos", "ellas", "me", "eran", "de", "mi", "soy", "las", "este", "cual", "estar�amos", "tenga", "esto",
			"hubieran", "hayamos", "est�n", "m�as", "est�s", "ni", "tendr�amos", "tendr�a", "haya", "tengo", "estabais", "fu�semos",
			"habr�ais", "estados", "tenidas", "muy", "�ramos", "seas", "eres", "algunos", "tuvisteis", "tanto", "habr�a", "ellos", 
			"para", "suya", "os", "fu�ramos", "tendr�is", "hubierais", "hubiesen", "estoy", "ten�a", "habiendo", "estar�ais",
			"tengamos", "tuyos", "suyo", "estando", "sean","interesa","encontrar");
	

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    String usage =
      "Usage:\tjava SearchFiles -index <indexPath> -infoNeeds <infoNeedsFile> -output <resultsFile>";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0])) || args.length != 6) {
      System.out.println(usage);
      System.exit(0);
    }

    String index = "";
    String entrada = "";
    String salida = "";
    
    for(int i = 0;i < args.length;i++) {
      if ("-index".equals(args[i])) {
        index = args[i+1];
        i++;
      } else if ("-infoNeeds".equals(args[i])) {
        entrada = args[i+1];
        i++;
      } else if ("-output".equals(args[i])) {
        salida = args[i+1];
      }
    }
    
    if (index.equals("") || entrada.equals("") || salida.equals("")) {
    	System.out.println(usage);
    	System.exit(0);
    }
    
    final File fEntrada = new File(entrada);
    if (!fEntrada.exists() || !fEntrada.canRead()) {
      System.out.println("Document directory '" +fEntrada.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    FileWriter fSalida = new FileWriter(salida);
    PrintWriter pw = new PrintWriter(fSalida);
  
    String[][] consultas = extraerConsultas(fEntrada);
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    CharArraySet cas = new CharArraySet(Version.LUCENE_44, stopWords, true);
    SpanishAnalyzer analyzer = new SpanishAnalyzer(Version.LUCENE_44, cas);
    MyQueryParser parser = new MyQueryParser(analyzer);
    
    for (int i = 0; i < consultas.length; i++) {
    	Query query = parser.parse(consultas[i][1]);
    	hacerConsultas(searcher, consultas[i][0], query, pw);
    }
    
    pw.close();
    reader.close();
    fSalida.close();
  }
  
  /**
   * Realiza la busqueda de ficheros que coinciden con la query pasada y los escribe en un
   * fichero.
   * @param searcher indice sobre el que se buscan los documentos
   * @param idConsulta identificador de la consulta
   * @param consulta query con la que se realiza la b�squeda
   * @param pw objeto Printwritter sobre el que se escriben los fichero obtenidos.
   */
  public static void hacerConsultas (IndexSearcher searcher, String idConsulta, Query consulta, PrintWriter pw) throws IOException{
	  TopDocs results = searcher.search(consulta, 1);
	  results = searcher.search(consulta, results.totalHits);
	  ScoreDoc[] hits = results.scoreDocs;
	  
	  for (int i = 0; i < hits.length; i++) {
		  Document doc = searcher.doc(hits[i].doc);
	      String path = doc.get("path");
	      String fichero = path.substring(path.lastIndexOf('\\') + 1, path.length());
	      pw.println(idConsulta + "\t" + fichero);
	  }
  }

  
  /**
   * Extrae las 5 consultas que se encuentran en el fichero XML proporcionado
   * como par�metro.
   * @param docDir fichero XML del que extraer las consultas
   * @return array de cadena de caracteres con las consultas extraidas.
   */
  private static String[][] extraerConsultas (File docDir) {
	  String [][] array = new String [5][2];
	  try {
          // parse the document
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();	
          DocumentBuilder db = dbf.newDocumentBuilder();
          org.w3c.dom.Document pDoc = db.parse(docDir);
          pDoc.getDocumentElement().normalize();
          
          NodeList nList = pDoc.getElementsByTagName("informationNeed");
          for (int i = 0; i < nList.getLength(); i++) {
        	  String texto = nList.item(i).getTextContent().replaceAll("\\s", " ").trim();
              String id = texto.substring(0, texto.indexOf("  ")).trim();
              String consulta = texto.substring(texto.indexOf("  "), texto.length()).trim();
              array[i][0] = id;
              array[i][1] = consulta;
          }
	  } catch (Exception e) {
		  System.out.println(e.getStackTrace());
	  }
	  return array;
  }
}
