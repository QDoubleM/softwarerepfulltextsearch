package Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentException;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class SearchUtil{
	public static void main(String[] args) throws IOException, DocumentException, ParseException{
		//multiSearcher();
		String tableName = "t_activity";
		String searchcontents = "biaoxian:B;name:创新杯";//content形式：name:邱敏明		
		//XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		//List<TableColumns> columnList = xmlAnalyzer.columnTableMap(tableName).get(tableName);
		//String[] contents = searchcontents.split(";");
		/*Query query=null;
		BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("E:/lucene/"+tableName)));
		IndexSearcher indexsearcher = new IndexSearcher(reader);
		
		
		for(int i=0;i<searchcontents.split(";").length;i++){			
			String columnname = searchcontents.split(";")[i].split(":")[0];//对应lucene document中field的name
			String content = searchcontents.split(";")[i].split(":")[1];//要查询的内容	
	
			for(TableColumns column:columnList){
				if(column.getName().equals(columnname)){
					if(column.getIsAnalyzed()){
						query = termQuerySearch(columnname, content);
					}else{
						query = queryPaserSearch(columnname, content);
					}						   
				}	
			}
		    booleanQuery.add(query,BooleanClause.Occur.MUST);
		}
		searchUtil.booleanSearch("E:/lucene/"+tableName, booleanQuery);*/
		//boleanQuery(tableName,contents,columnList);
		searchTable(tableName,searchcontents);
	}
	public static void searchTable(String tableName,String stearchContens) throws IOException, ParseException, DocumentException{
		String[] contents = stearchContens.split(";");
		XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		List<TableColumns> columnList = xmlAnalyzer.columnTableMap(tableName).get(tableName);
		boleanQuery(tableName,contents,columnList);
	}
	
	public static void boleanQuery(String tableName,String[] searchContents,List<TableColumns> columnList) throws IOException, ParseException{
		Query query=null;
		BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		for(int i=0;i<searchContents.length;i++){
			String columnname = searchContents[i].split(":")[0];//对应lucene document中field的name
			String content = searchContents[i].split(":")[1];
			
			for(TableColumns column:columnList){
				if(column.getName().equals(columnname)){
					if(column.getIsAnalyzed()){
						query = termQuerySearch(columnname, content);
					}else{
						query = queryPaserSearch(columnname, content);
					}						   
				}	
			}
		    booleanQuery.add(query,BooleanClause.Occur.MUST);
		}
		booleanSearch("E:/lucene/"+tableName, booleanQuery);
	}
	
	/*public static void multiSearcher() throws IOException{
		String indexDir = "E:/lucene/fileindex";
		Directory dir= FSDirectory.open(Paths.get(indexDir));
		String indexDir1 = "E:/lucene/t_activity";
		Directory dir1= FSDirectory.open(Paths.get(indexDir1));
		String indexDir2 = "E:/lucene/t_student";
		Directory dir2= FSDirectory.open(Paths.get(indexDir2));
		IndexReader reader1 = DirectoryReader.open(dir);
		IndexReader reader2 = DirectoryReader.open(dir1);
		IndexReader reader3 = DirectoryReader.open(dir2);
		MultiReader multiReader = new MultiReader(reader1,reader2,reader3);
		IndexSearcher indexsearcher = new IndexSearcher(multiReader);
		Term term = new Term("filecontents", "邱");
		Query termQuery = new TermQuery(term);
		TopDocs hits = indexsearcher.search(termQuery, 10);//10是查询前10条数据
		System.out.println("有"+hits.totalHits+"条记录");
		for(ScoreDoc scoredoc:hits.scoreDocs){
			System.out.println(indexsearcher.doc(scoredoc.doc).get("name"));			
		}
	}*/
	public static Query queryPaserSearch(String fieldName,String queryContent) throws ParseException{
		Analyzer smartAnalyzer = new SmartChineseAnalyzer();
		QueryParser parser = new QueryParser(fieldName,smartAnalyzer);
		Query query = parser.parse(queryContent);
		return query;
	}
	
	public static Query termQuerySearch(String fieldName,String queryContent){
		Term term = new Term(fieldName, queryContent);
		Query query = new TermQuery(term);
		return query;
	}
	
	public static void booleanSearch(String dir,Builder booleanQuery) throws IOException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(dir)));
		IndexSearcher indexsearcher = new IndexSearcher(reader);
		TopDocs hits = indexsearcher.search(booleanQuery.build(), 100);
		System.out.println("一共有"+hits.totalHits+"条记录");
		for(ScoreDoc scoredoc:hits.scoreDocs){
			System.out.println(indexsearcher.doc(scoredoc.doc).get("id")+":"+indexsearcher.doc(scoredoc.doc).get("name"));			
		}
	}
}
