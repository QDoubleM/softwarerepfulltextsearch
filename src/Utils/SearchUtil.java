package Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentException;

public class SearchUtil{
	private static String indexFilePath = "E:/lucene/";
	private static List<SearchContent> searchContentList = null;
	private String tableName;
	private String queryContent;
	
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getQueryContent() {
		return queryContent;
	}

	public void setQueryContent(String queryContent) {
		this.queryContent = queryContent;
	}

	/**
	 * @param tableName
	 * @param stearchContens  形式biaoxian:B;name:创新杯;
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public List<TableColumns> searchTable(String tableName,String searchContents) throws IOException, ParseException, DocumentException{				
		StringHandlerUtil stringHandler = new StringHandlerUtil();
		String[] contentArray = stringHandler.String2Array(searchContents, ";");
		XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		List<TableColumns> columnList = xmlAnalyzer.columnTableMap(tableName).get(tableName);
		return boleanQuery(tableName,contentArray,columnList);
	}
	
	//利用lucene自带的MultiReader进行测试
	@SuppressWarnings("unchecked")
	public Builder multiTableSearch(String queryContent) throws DocumentException, IOException, ParseException{
		SearchContentAnalyzer searchContentAnalyzer = new SearchContentAnalyzer();
		searchContentList = searchContentAnalyzer.tableContentHandler(queryContent);
		StringHandlerUtil stringHandler = new StringHandlerUtil();
		XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		List<TableColumns> columnList = null;
		
		BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		
		for(SearchContent searchContent:searchContentList){
			Query query=null;
			String tableName = searchContent.getScope();
			String[] contentArray = stringHandler.String2Array(searchContent.getContent(), ";");
			columnList = xmlAnalyzer.columnTableMap(tableName).get(tableName);
			for(int i=0;i<contentArray.length;i++){
				String columnname = contentArray[i].split(":")[0];//对应lucene document中field的name
				String content = contentArray[i].split(":")[1];
				for(TableColumns column:columnList){
					if(column.getText().equals(columnname)){
						if(column.getIsAnalyzed()){
							query = termQuerySearch(column.getName(), content);
						}else{
							query = queryPaserSearch(column.getName(), content);
						}						   
					}	
				}
				booleanQuery.add(query,BooleanClause.Occur.MUST);
			}
		}
		return booleanQuery;
	}
		
	public List<TableColumns> boleanQuery(String tableName,String[] searchContents,List<TableColumns> columnList) throws IOException, ParseException{
		Query query = null;
		BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		for(int i=0;i<searchContents.length;i++){
			String columnname = searchContents[i].split(":")[0];//对应lucene document中field的name
			String content = searchContents[i].split(":")[1];
			for(TableColumns column:columnList){
				if(column.getText().equals(columnname)){
					if(column.getIsAnalyzed()){
						query = termQuerySearch(column.getName(), content);
					}else{
						query = queryPaserSearch(column.getName(), content);
					}						   
				}	
			}
		    booleanQuery.add(query,BooleanClause.Occur.MUST);
		}
		return booleanSearch(indexFilePath+tableName, booleanQuery,columnList);
		
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
	
	public List<TableColumns> booleanSearch(String dir,Builder booleanQuery,List<TableColumns> columnList) throws IOException{		
		
		List<TableColumns> tableColumnsList = new ArrayList<TableColumns>();
		List<List<TableColumns>> recordList = new ArrayList<List<TableColumns>>();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(dir)));
		IndexSearcher indexsearcher = new IndexSearcher(reader);
		TopDocs hits = indexsearcher.search(booleanQuery.build(), 100);
		System.out.println("一共有"+hits.totalHits+"条记录");
		
		for(ScoreDoc scoredoc:hits.scoreDocs){
			
			for(TableColumns tableColumn:columnList){
				TableColumns column = new TableColumns();
				column.setText(tableColumn.getText());
				column.setName(tableColumn.getName());
				column.setContent(indexsearcher.doc(scoredoc.doc).get(tableColumn.getName()));
				tableColumnsList.add(column);
			}
			//recordList.add(tableColumnsList);
		}
		return tableColumnsList;
	}	
}
