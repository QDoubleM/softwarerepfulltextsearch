package fulltextsearch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentException;

import Utils.SearchContent;
import Utils.SearchUtil;
import Utils.TableColumns;

public class SearchThread implements Callable<List<TableColumns>> {
    private SearchContent searchContent;
    SearchUtil searchUtil = new SearchUtil();
	@Override
	public List<TableColumns> call() throws Exception {
		List<TableColumns> recordList  = tableSearch();
		return recordList;
	}
	
	public SearchContent getSearchContent() {
		return searchContent;
	}
	public void setSearchContent(SearchContent searchContent) {
		this.searchContent = searchContent;
	}

	public List<TableColumns> tableSearch() throws IOException, ParseException, DocumentException{
		
		String tableName = searchContent.getScope();
		String queryContent = searchContent.getContent();
		List<TableColumns> recordList = searchUtil.searchTable(tableName, queryContent);
		return recordList;
	}
	
}
