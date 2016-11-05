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
	
	private static List<String> stopWords= Arrays.asList("mucho", "tuviese", "míos", "seremos", "del", "serán", "vuestros", "hube", "tus"
			, "habríamos", "les", "está", "estuvierais", "hubieras", "otras", "mí", "habido", "esté", "estarán"
			, "fuera", "algunas", "tendrías", "estarás", "habréis", "al", "tengan", "quien", "son", "habrían", "tengas", 
			"habrías", "estar", "estas", "sobre", "estaréis", "unos", "nosotros", "tuvieron", "mía", "estuviste", "estarían", 
			"seáis", "habíamos", "todo", "tiene", "estarías", "tuyas", "tenemos", "hubiéramos", "mío", "sus", "se", "algo", 
			"tendrán", "mis", "por", "serías", "sea", "estuviéramos", "ella", "vuestras", "necesito", "tendrás", "a", "su", "teniendo",
			"esas", "fuiste", "tuvieseis", "o", "te", "ti", "habrán", "tendremos", "tuve", "y", "habrás", "hubimos", "fueran", "tu",
			"fuese", "tendré", "estáis", "sois", "suyos", "seré", "fueras", "nosotras", "tuvieran", "tendrían", "tuvieras",
			"estaban", "han", "habidas", "tuviésemos", "estabas", "has", "fueron", "tenían", "estuve", "hay", "tendrá", "tenías", "él", 
			"el", "estuvo", "en", "habéis", "poco", "es", "estés", "habían", "estén", "tengáis", "habías", "serás", "hubisteis", "otros", 
			"hubiera", "que", "vosotros", "seréis", "era", "suyas", "e", "tuviesen", "también", "hayan", "nuestra", "tuvieses", "hayas", 
			"otra", "vuestra", "sin", "sí", "fueseis", "nuestro", "esta", "más", "eras", "contra", "otro", "hubiese", "vuestro",
			"tenidos", "una", "estada", "cuando", "estuvisteis", "todos", "estuvieron", "muchos", "pero", "hemos", "uno", "habidos", 
			"habrá", "ha", "tuvo", "tuvimos", "he", "estuviesen", "habré", "donde", "tú", "tienen", "ya", "fueses", "estemos", 
			"tienes", "tenéis", "como", "esos", "estad", "fuisteis", "habremos", "yo", "nuestros", "seamos", "estaba", "siendo", 
			"seríamos", "fuimos", "estos", "será", "fuesen", "estadas", "tendríais", "hayáis", "tuviéramos", "estuvimos", "somos", 
			"estábamos", "teníais", "hubieses", "estuvieran", "hubo", "un", "estuvieras", "estaremos", "qué", "habíais", "tuya", 
			"hubiésemos", "fuerais", "hubieseis", "nos", "los", "estéis", "seríais", "tuvierais", "tuyo", "estuviésemos", 
			"quienes", "había", "ante", "no", "hubieron", "sido", "nuestras", "estado", "tenida", "tenido", "nada", "estuviera", 
			"esa", "sería", "la", "fue", "ese", "le", "serían", "tened", "habida", "estuvieses", "estuvieseis", "teníamos", 
			"eso", "con", "lo", "vosotras", "tuviera", "fui", "tuviste", "estará", "estaría", "erais", "porque", "hubiste", "estaré",
			"estuviese", "estamos", "ellas", "me", "eran", "de", "mi", "soy", "las", "este", "cual", "estaríamos", "tenga", "esto",
			"hubieran", "hayamos", "están", "mías", "estás", "ni", "tendríamos", "tendría", "haya", "tengo", "estabais", "fuésemos",
			"habríais", "estados", "tenidas", "muy", "éramos", "seas", "eres", "algunos", "tuvisteis", "tanto", "habría", "ellos", 
			"para", "suya", "os", "fuéramos", "tendréis", "hubierais", "hubiesen", "estoy", "tenía", "habiendo", "estaríais",
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
   * @param consulta query con la que se realiza la búsqueda
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
   * como parámetro.
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
