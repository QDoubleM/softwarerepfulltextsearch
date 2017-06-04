package fulltextsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class MultiSearcher {//多索引目录搜索
	
	
	public static void main(String args[]) throws IOException, ParseException{

		//File indexfile = new File("E:/lucene"); 
		MultiSearchHelp();
		//indexfilegroup(indexfile);
	}
	public static int indexfilegroup(File indexfile){
		int foldercount = 0;
		//int countFile = 0;
		if(indexfile.isDirectory()){
			File []files = indexfile.listFiles();
			for(File fileIndex:files){
				if(fileIndex.isDirectory()){
					foldercount++;
		            indexfilegroup(fileIndex);
			}else {
				continue;}
			}
		}
		System.out.println("foldercount:"+foldercount);
		return foldercount;
	}
	public static void MultiSearchHelp() throws IOException, ParseException{
		String q="1023";
		String indexDir = "E:/lucene/t_notice";
		Directory dir= FSDirectory.open(Paths.get(indexDir));
		String indexDir1 = "E:/lucene/t_activity";
		Directory dir1= FSDirectory.open(Paths.get(indexDir1));
		String indexDir2 = "E:/lucene/t_student";
		Directory dir2= FSDirectory.open(Paths.get(indexDir2));
		IndexReader reader1 = DirectoryReader.open(dir);
		IndexReader reader2 = DirectoryReader.open(dir1);
		IndexReader reader3 = DirectoryReader.open(dir2);
		IKAnalyzer analyzer = new IKAnalyzer(true);
		MultiReader multiReader = new MultiReader(reader1,reader2,reader3);
		long start = System.currentTimeMillis();
		IndexSearcher indexsearcher = new IndexSearcher(multiReader);
		//QueryParser parser = new QueryParser("filecontents",analyzer);
		//Query query = parser.parse(q);
		Term term = new Term("student_id", q);
		Query termQuery = new TermQuery(term);
		TopDocs hits = indexsearcher.search(termQuery, 10);//10是查询前10条数据
		long end = System.currentTimeMillis();
		System.out.println("匹配"+q+",总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"条记录");
		for(ScoreDoc scoredoc:hits.scoreDocs){
			System.out.println(indexsearcher.doc(scoredoc.doc).get("student_id"));
		}
	}	
}
