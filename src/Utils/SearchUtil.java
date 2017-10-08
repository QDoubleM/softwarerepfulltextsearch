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
	/*public static void main(String[] args) throws IOException, DocumentException, ParseException{
		//multiSearcher();
		String tableName = "t_activity";
		String searchcontents = "biaoxian:B;name:创新杯";//content形式：name:邱敏明		
		searchTable(tableName,searchcontents);
	}*/
	public void searchTable(String tableName,String stearchContens) throws IOException, ParseException, DocumentException{
		String[] contents = stearchContens.split(";");
		XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		List<TableColumns> columnList = xmlAnalyzer.columnTableMap(tableName).get(tableName);
		boleanQuery(tableName,contents,columnList);
	}
	
	public void boleanQuery(String tableName,String[] searchContents,List<TableColumns> columnList) throws IOException, ParseException{
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
	
	public Query queryPaserSearch(String fieldName,String queryContent) throws ParseException{
		Analyzer smartAnalyzer = new SmartChineseAnalyzer();
		QueryParser parser = new QueryParser(fieldName,smartAnalyzer);
		Query query = parser.parse(queryContent);
		return query;
	}
	
	public Query termQuerySearch(String fieldName,String queryContent){
		Term term = new Term(fieldName, queryContent);
		Query query = new TermQuery(term);
		return query;
	}
	
	public void booleanSearch(String dir,Builder booleanQuery) throws IOException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(dir)));
		IndexSearcher indexsearcher = new IndexSearcher(reader);
		TopDocs hits = indexsearcher.search(booleanQuery.build(), 100);
		System.out.println("一共有"+hits.totalHits+"条记录");
		for(ScoreDoc scoredoc:hits.scoreDocs){
			System.out.println(indexsearcher.doc(scoredoc.doc).get("id")+":"+indexsearcher.doc(scoredoc.doc).get("name"));			
		}
	}
}
