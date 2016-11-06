package org.apache.lucene.demo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.index.Term;
public class MyQueryParser{
	
	private int trabajoPos=-1;
	private Analyzer analyzer;
	private static List<String> worthless=Arrays.asList("text","informacion","trabaj","preferentement","entr","interesad"
								,"preferiri","gustari","conocer","academic","tesis","doctoral","relacionad",
								"publicad","partir","aÃ±o","incluid","participad","algun","miembr","famili",
								"usuari","necesit","obtener","implementarl","ofrecer","proyect","ultim","aÃ±os");
	private List<String> specific;
	public MyQueryParser(Analyzer a){
		analyzer=a;
	}
	/**
	 * procesa una necesidad de información para formar una query
	 * @param necesidad
	 * @return BooleanQuery que contiene el resto de las querys utilizadas
	 * @throws ParseException
	 */
	public Query parse(String necesidad) throws ParseException{
		BooleanQuery base=new BooleanQuery();
		specific=new ArrayList<String>();
		try {
			TokenStream ts=analyzer.tokenStream(null, new StringReader(necesidad));
			ts.reset();
			List<String> l = new ArrayList<String>();
			//System.out.println("----------------------------------------");
			while(ts.incrementToken()){
				l.add(ts.getAttribute(CharTermAttribute.class).toString());
				//System.out.println(ts.getAttribute(CharTermAttribute.class));
			}
			
			
			checkIdentifier(l,base);
			checkDate(l,base);
			
			
			Query q = getQuery(l,"title");
			q.setBoost(2f);
			base.add(q,BooleanClause.Occur.MUST);
			
			q = getQuery(l,"creator");
			q.setBoost(3f);
			base.add(q,BooleanClause.Occur.SHOULD);
			
			
			base.add(getQuery(l,"description"),BooleanClause.Occur.MUST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return base;
		
	}
	/**
	 * Si detecta que la necesidad de información hace referencia a un tipo particular de trabajo,
	 * añade una Query a base.
	 * @param stems
	 * @param base
	 */
	private void checkIdentifier(List<String> stems,BooleanQuery base){
		int pos=0;
		boolean tesis=false;
		boolean tfg=false;
		boolean tfm=false;
		boolean pfc=false;
		boolean trabajos=false;
		int type=0;
		int n=0;
		for (String stem: stems){
			pos++;
			if(stem.equals("tesis")){
				tesis=true;
				//base.add(new TermQuery(new Term("identifier","TESIS")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
				n++;
			}else if(stem.contains("tfg")){
				tfg=true;
				//base.add(new TermQuery(new Term("identifier","TFG")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
				n++;
			}else if(stem.contains("tfm")){
				tfm=true;
				//base.add(new TermQuery(new Term("identifier","TFM")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
				n++;
			}else if(stem.contains("pfc")){
				pfc=true;
				//base.add(new TermQuery(new Term("identifier","PFC")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
				n++;
			}else if(stem.equals("master")){
				tfm=true;
				trabajoPos=pos;
				n++;
			}else if(stem.equals("carrer")){
				pfc=true;
				trabajoPos=pos;
				n++;
			}else if(stem.equals("grad")){
				tfg=true;
				trabajoPos=pos;
				n++;
			}else if(stem.equals("trabaj")){
				
				trabajos=true;
				trabajoPos=pos;
			}else if(stem.equals("publicad")){
				trabajoPos=pos;
			}else if(stem.equals("proyect")){
				trabajos=true;
				trabajoPos=pos;
			}else if(stem.equals("text")){
				trabajoPos=pos;
			}else if(stem.equals("informacion")){
				trabajoPos=pos;
			}
		}
		if(n>1){
			if(tesis){
				base.add(new TermQuery(new Term("identifier","TESIS")),BooleanClause.Occur.SHOULD);
			}
			if(tfg){
				base.add(new TermQuery(new Term("identifier","TFG")),BooleanClause.Occur.SHOULD);
			}
			if(tfm){
				base.add(new TermQuery(new Term("identifier","TFM")),BooleanClause.Occur.SHOULD);
			}
			if(pfc){
				base.add(new TermQuery(new Term("identifier","PFC")),BooleanClause.Occur.SHOULD);
			}
		}else{
			if(tesis){
				if(!trabajos){
					base.add(new TermQuery(new Term("identifier","TESIS")),BooleanClause.Occur.MUST);
				}
			}else if(tfg){
				base.add(new TermQuery(new Term("identifier","TFG")),BooleanClause.Occur.MUST);
			}else if(tfm){
				base.add(new TermQuery(new Term("identifier","TFM")),BooleanClause.Occur.MUST);
			}else if(pfc){
				base.add(new TermQuery(new Term("identifier","PFC")),BooleanClause.Occur.MUST);
			}
		}
	}
	
	/**
	 * Si detecta que la publicación de la necesidad de información hace referencia a un 
	 * periodo de tiempo, añade una Query a base. 
	 * @param stems
	 * @param base
	 */
	private void checkDate(List<String> stems,BooleanQuery base){
		//0 nada encontrado, 1 previos, 2 posteriores, 3 entre, 4 ultimos, 5 aÃ±o, 6 desde
		int mode =0;
		int pos =0;
		String anio1="";
		String anio2="";
		String min="1980";
		String max="2070";
		
		for (String stem: stems){
			pos++;
			if(trabajoPos!=-1 && (pos-trabajoPos<5 | mode!=0)){
				if(stem.equals("previ")){
					mode=1;
				}else if(stem.equals("anterior")){
					mode=1;
				}else if(stem.equals("ant")){
					mode=1;
				}else if(stem.equals("despu")){
					mode=2;
				}else if(stem.equals("partir")){
					mode=2;
				}else if(stem.equals("posterior")){
					mode=2;
				}else if(stem.equals("entr")){
					mode=3;
				}else if(stem.equals("durant")){
					mode=3;
				}else if(stem.equals("desd")){
					mode=6;
				}else if(stem.equals("hast")){
					if(mode!=6){
						mode=1;
					}
				}else if(stem.equals("ultim")){
					mode=4;
				}else if(stem.equals("aÃ±o")){
					if(mode == 0 | mode==4)
						mode=5;
				}else if(stem.compareTo(min)>0 && stem.compareTo(max)<0 && stem.length()==4){
					if(mode == 1 | mode == 2 | mode==5){
						anio1=stem;
					}else if(mode == 3 | mode == 6){
						if(!anio1.equals("")){
							anio2=stem;
						}else{
							anio1=stem;
						}
					}
					
					if(mode == 1 | mode == 2 | mode==3 | mode==6){
						specific.add(stem);
					}
				}else if((stem.compareTo("0")>0 && stem.compareTo("9")<=0  && stem.length()==1) 
						| (stem.compareTo("10")>=0 && stem.compareTo("90")<0 && stem.length()==2)){
					if(mode == 4){
						anio1=stem;
						specific.add(stem);
					}
				}
			}
			
		}
		if(anio2.equals("")){
			if(!anio1.equals("")){
				if(mode==1){
					base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(min), 
							Integer.parseInt(anio1), true, true),BooleanClause.Occur.MUST);
				}else if(mode==2 | mode == 6){
					base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(anio1), 
							Integer.parseInt(max), true, true),BooleanClause.Occur.MUST);
				}else if(mode==4){
					Date d = new Date();
					int anioActual=Integer.parseInt(d.toString().substring(d.toString().lastIndexOf(" ") +1));
					base.add(NumericRangeQuery.newIntRange("date", anioActual - Integer.parseInt(anio1), 
							anioActual, true, true),BooleanClause.Occur.MUST);
				}else if(mode == 5){
					base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(anio1), 
							Integer.parseInt(anio1), true, true),BooleanClause.Occur.MUST);
				}
			}else{
				if(mode == 5){
					Date d = new Date();
					int anioActual=Integer.parseInt(d.toString().substring(d.toString().lastIndexOf(" ") +1));
					base.add(NumericRangeQuery.newIntRange("date", anioActual, 
							anioActual, true, true),BooleanClause.Occur.MUST);
				}
			}
		}else{
			if(mode==3 | mode == 6){
				base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(anio1), 
						Integer.parseInt(anio2), true, true),BooleanClause.Occur.MUST);
			}
		}
		
	}
	/**
	 * Forma una BooleanQuery compuesta por los terminos de la necesidad sobre un campo field,
	 * siempre que estos, no aparezcan en las lista specific, worthless o viewed.
	 * @param stems
	 * @param field
	 * @return
	 */
	private Query getQuery(List<String> stems, String field){
		BooleanQuery bq=new BooleanQuery();
		List<String> viewed= new ArrayList<String>();
		for (String stem: stems){
			if(!worthless.contains(stem) && !specific.contains(stem) && !viewed.contains(stem)){
				//if(field.equals("description"))
					//System.out.println(stem);
				viewed.add(stem);
				bq.add(new TermQuery(new Term(field,stem)),BooleanClause.Occur.SHOULD);
			}
		}
		return bq;
	}
}
