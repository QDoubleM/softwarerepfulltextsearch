package fulltextsearch;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class SearchHelperImpl implements SearchHelper {
	String indexDir;
	IndexReader reader;
	IndexSearcher indexsearcher;
	Directory dir;
	public void queryPaserSearch(String queryContent) throws IOException, ParseException, InvalidTokenOffsetsException{
		indexDir = "E:\\lucene\\fileindex";
		List<FileInfo> resultlist=new ArrayList<>();
		dir= FSDirectory.open(Paths.get(indexDir));
		reader = DirectoryReader.open(dir);
		long start = System.currentTimeMillis();
		indexsearcher = new IndexSearcher(reader);
		IKAnalyzer analyzer = new IKAnalyzer(true);
		QueryParser parser = new QueryParser("filecontents",analyzer);
		Query query = parser.parse(queryContent);
		TopDocs hits = indexsearcher.search(query, 10);//10是查询前10条数据
		long end = System.currentTimeMillis();
		System.out.println("匹配"+queryContent+",总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"条记录");
		
		QueryScorer queryscore = new QueryScorer(query);//显示得分较高的片段
		Fragmenter fragmenter = new SimpleFragmenter(100);
		SimpleHTMLFormatter simplehtmlfor = new SimpleHTMLFormatter("","");
		Highlighter highlight = new Highlighter(simplehtmlfor, queryscore);
	    highlight.setTextFragmenter(fragmenter);
	    
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			FileInfo fileInfo = new FileInfo();
			Document doc = indexsearcher.doc(scoreDoc.doc);//文档的id
			String contents = doc.get("filecontents");
			fileInfo.setFileName(doc.get("filename"));
			fileInfo.setFilePath(doc.get("filepath"));
			System.out.println(doc.get("filename"));
			System.out.println(doc.get("filepath"));
			if(contents!=null){
				TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(contents)); 
				fileInfo.setFileContent(highlight.getBestFragment( tokenStream, contents));
				resultlist.add(fileInfo);
			}
		}
		reader.close();
	}
	@Override
	public TopDocs queryPaser(String queryContent) throws IOException, ParseException {
		indexDir = "E:\\lucene\\fileindex";
		dir= FSDirectory.open(Paths.get(indexDir));
		reader = DirectoryReader.open(dir);
		long start = System.currentTimeMillis();
		indexsearcher = new IndexSearcher(reader);
		IKAnalyzer analyzer = new IKAnalyzer(true);
		QueryParser parser = new QueryParser("filecontents",analyzer);
		Query query = parser.parse(queryContent);
		TopDocs hits = indexsearcher.search(query, 10);//10是查询前10条数据
		long end = System.currentTimeMillis();
		System.out.println("匹配"+queryContent+",总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"条记录");
		return hits;
	}
	@Override
	public TopDocs termQuery(String queryContent) throws IOException, ParseException {
		indexDir = "E:\\lucene\\fileindex";
		dir= FSDirectory.open(Paths.get(indexDir));
		reader = DirectoryReader.open(dir);
		long start = System.currentTimeMillis();
		indexsearcher = new IndexSearcher(reader);
				
		Term term = new Term("realName", queryContent);
		Query termQuery = new TermQuery(term);
		TopDocs hits = indexsearcher.search(termQuery, 100);
		long end = System.currentTimeMillis();
		System.out.println("匹配"+queryContent+",总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"条记录");
		return hits;
	}
	@Override
	public void booleanQuery() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void multiTermQuery() {
		// TODO Auto-generated method stub
		
	}
}
