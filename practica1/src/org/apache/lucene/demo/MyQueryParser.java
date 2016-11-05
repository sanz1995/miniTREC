package org.apache.lucene.demo;

import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.index.Term;
public class MyQueryParser extends QueryParser{
	
	int trabajoPos=-1;
	int pos=0;
	
	public MyQueryParser(Version v, String f, Analyzer a){
		super(v,f,a);
	}
	
	public Query parse(String necesidad) throws ParseException{
		BooleanQuery base=new BooleanQuery();
		
		
		Query q =super.parse(necesidad);
		System.out.println(q.getClass());
		String termSec=q.toString(super.getField());
		System.out.println("------------");
		System.out.println(termSec);
		String[] stems=termSec.split(" ");
		
		checkIdentifier(stems,base);
		checkDate(stems,base);
		return base;
		
	}
	public void checkIdentifier(String[] stems,BooleanQuery base){
		for (String stem: stems){
			pos++;
			if(stem.equals("tesis")){
				base.add(new TermQuery(new Term("identifier","TESIS")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
			}else if(stem.contains("tfg")){
				base.add(new TermQuery(new Term("identifier","TFG")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
			}else if(stem.contains("tfm")){
				base.add(new TermQuery(new Term("identifier","TFM")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
			}else if(stem.contains("pfc")){
				base.add(new TermQuery(new Term("identifier","PFC")),BooleanClause.Occur.MUST);
				trabajoPos=pos;
			}else if(stem.equals("trabaj")){
				trabajoPos=pos;
			}else if(stem.equals("publicad")){
				trabajoPos=pos;
			}else if(stem.equals("proyect")){
				trabajoPos=pos;
			}
		}
	}
	public void checkDate(String[] stems,BooleanQuery base){
		String text="";
		//0 nada encontrado, 1 previos, 2 posteriores, 3 entre, 4 ultimos, 5 año, 6 desde
		int mode =0;
		int pos =0;
		String año1="";
		String año2="";
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
				}else if(stem.equals("año")){
					if(mode == 0 | mode==4)
						mode=5;
				}else if(stem.compareTo(min)>0 && stem.compareTo(max)<0 && stem.length()==4){
					if(mode == 1 | mode == 2 | mode==5){
						año1=stem;
					}else if(mode == 3 | mode == 6){
						if(!año1.equals("")){
							año2=stem;
						}else{
							año1=stem;
						}
					}
				}else if((stem.compareTo("0")>0 && stem.compareTo("9")<=0  && stem.length()==1) 
						| (stem.compareTo("10")>=0 && stem.compareTo("90")<0 && stem.length()==2)){
					if(mode == 4){
						año1=stem;
					}
				}
			}
			
		}
		if(año2.equals("")){
			if(!año1.equals("")){
				if(mode==1){
					base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(min), 
							Integer.parseInt(año1), true, true),BooleanClause.Occur.MUST);
				}else if(mode==2 | mode == 6){
					base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(año1), 
							Integer.parseInt(max), true, true),BooleanClause.Occur.MUST);
				}else if(mode==4){
					Date d = new Date();
					int añoActual=Integer.parseInt(d.toString().substring(d.toString().lastIndexOf(" ") +1));
					base.add(NumericRangeQuery.newIntRange("date", añoActual - Integer.parseInt(año1), 
							añoActual, true, true),BooleanClause.Occur.MUST);
				}else if(mode == 5){
					base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(año1), 
							Integer.parseInt(año1), true, true),BooleanClause.Occur.MUST);
				}
			}else{
				if(mode == 5){
					Date d = new Date();
					int añoActual=Integer.parseInt(d.toString().substring(d.toString().lastIndexOf(" ") +1));
					base.add(NumericRangeQuery.newIntRange("date", añoActual, 
							añoActual, true, true),BooleanClause.Occur.MUST);
				}
			}
		}else{
			if(mode==3 | mode == 6){
				base.add(NumericRangeQuery.newIntRange("date", Integer.parseInt(año1), 
						Integer.parseInt(año2), true, true),BooleanClause.Occur.MUST);
			}
		}
		
	}
}
