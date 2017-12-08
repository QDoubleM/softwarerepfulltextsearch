package fulltextsearch;

import java.io.IOException;
import java.nio.file.Paths;
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

public class SearchThread implements Callable<Integer> {
	private ScoreDoc scoredoc;
	private String indexDir = "E:/lucene/";
    private SearchContent searchContent;
    private String content;
    private String scope;
    private String field;
    static SearchUtil searchUtil = new SearchUtil();
	@Override
	public Integer call() throws Exception {
		Integer res = new Random().nextInt(100);
		//System.out.println("任务执行:获取到结果 :" + res);
		//search("fileindex","filecontents","Lucene");
		multiTableSearch();
		return res;
	}
	
	public SearchContent getSearchContent() {
		return searchContent;
	}
	public void setSearchContent(SearchContent searchContent) {
		this.searchContent = searchContent;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@SuppressWarnings("static-access")
	public void multiTableSearch() throws IOException, ParseException, DocumentException{
		String tableName = searchContent.getScope();
		String queryContent = searchContent.getContent();
		searchUtil.searchTable(tableName, queryContent);
	}
	
}
