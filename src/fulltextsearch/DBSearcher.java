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
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class DBSearcher {
	//显示表名字段名和值
	private static String indexDir = "E:\\lucene\\t_student";
	public static List<String> searcher(String queryStr) throws IOException,ParseException, InvalidTokenOffsetsException {

		List<String> resultlist=new ArrayList<String>();
		IKAnalyzer analyzer = new IKAnalyzer(true);
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(directory);
		long start = System.currentTimeMillis();
		IndexSearcher searcher = new IndexSearcher(reader);
		//QueryParser parser = new QueryParser("realName",analyzer);
		//Query query = parser.parse(queryStr);
		Term term = new Term("realName", queryStr);
		Query termQuery = new TermQuery(term);
		TopDocs hits = searcher.search(termQuery, 100);// 10是查询前10条数据
		long end = System.currentTimeMillis();
		System.out.println("匹配" + queryStr + ",总共花费" + (end - start) + "毫秒"+ "查询到" + hits.totalHits + "条记录");

		QueryScorer queryscore = new QueryScorer(termQuery);// 显示得分较高的片段
		SimpleHTMLFormatter simplehtmlfor = new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
		Highlighter highlight = new Highlighter(simplehtmlfor, queryscore);
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);// 文档的id
			String contents = doc.get("id")+","+doc.get("realName");
			System.out.println("realName:"+contents);
			//String id = doc.get("id");
			//System.out.println(id);
			if (contents != null) {
				TokenStream tokenStream = analyzer.tokenStream("realName",new StringReader(contents));
				resultlist.add(contents);
			}
		}
		reader.close();
		return resultlist;
	}
	public static void main(String args[]) throws IOException, ParseException, InvalidTokenOffsetsException{
		searcher("邱敏明");
	}
}
