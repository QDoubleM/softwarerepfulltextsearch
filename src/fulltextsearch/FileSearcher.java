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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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

public class FileSearcher {
	public List<String> searcherimpl(String q) throws IOException, ParseException, InvalidTokenOffsetsException{
		String indexDir = "E:\\lucene\\fileindex";
		List<String> resultlist=new ArrayList<String>();
		Directory dir= FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(dir);
		long start = System.currentTimeMillis();
		IndexSearcher indexsearcher = new IndexSearcher(reader);
		IKAnalyzer analyzer = new IKAnalyzer(true);
		QueryParser parser = new QueryParser("filecontents",analyzer);
		Query query = parser.parse(q);
		TopDocs hits = indexsearcher.search(query, 10);//10是查询前10条数据
		long end = System.currentTimeMillis();
		System.out.println("匹配"+q+",总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"条记录");
		
		//QueryScorer queryscore = new QueryScorer(query);//显示得分较高的片段
		//Fragmenter fragmenter = new SimpleFragmenter(100);
		//SimpleHTMLFormatter simplehtmlfor = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		//Highlighter highlight = new Highlighter(simplehtmlfor, queryscore);
	   // highlight.setTextFragmenter(fragmenter);
	    
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc = indexsearcher.doc(scoreDoc.doc);//文档的id
			System.out.println("文件路径"+doc.get("filepath"));
			String contents = doc.get("filecontents");
			String filename = doc.get("filename");
			if(filename!=null){
				TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(contents)); 
				resultlist.add(filename);
			}
		}
		reader.close();
		return resultlist;
	}
	
}
