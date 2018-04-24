package fulltextsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentException;

import Utils.SearchUtil;

public class MultiSearcher {//多索引目录搜索
	private String indexDirectory = "E:/lucene/";
	private Map<String,IndexReader> previousReaders =null;
	
	public static void main(String args[]) throws IOException, ParseException, DocumentException{
		MultiSearchHelp("t_activity 获奖情况:创新杯;表现:B;id:15;|t_student 学生姓名:李苏兴;id:1|");
		//MultiSearchHelp("docker");
	}
	public void createDirectoryReader(String[] indexFilesName){
		Directory fsDirectory;
		IndexReader previousReaders[] = null;
		try {
			if(indexFilesName.length == 1){
				fsDirectory= FSDirectory.open(Paths.get(indexDirectory+indexFilesName[0]));
			    previousReaders[0] = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory+indexFilesName[0])));
			}
			else{
				for(int i=0;i<=indexFilesName.length;i++){
					previousReaders[i] = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory+indexFilesName[0])));
				}
			}
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	public static void MultiSearchHelp(String queryContent) throws IOException, ParseException, DocumentException{
		SearchUtil searchUtil = new SearchUtil();
		
		String indexDir = "E:/lucene/fileindex";
		Directory dir= FSDirectory.open(Paths.get(indexDir));
		String indexDir1 = "E:/lucene/t_activity";
		Directory dir1= FSDirectory.open(Paths.get(indexDir1));
		String indexDir2 = "E:/lucene/t_student";
		Directory dir2= FSDirectory.open(Paths.get(indexDir2));
		DirectoryReader reader1 = DirectoryReader.open(dir);
		DirectoryReader reader2 = DirectoryReader.open(dir1);
		DirectoryReader reader3 = DirectoryReader.open(dir2);
		System.out.println(reader1.openIfChanged(reader1));
		IndexReader i1 = reader1; 
		IndexReader i2 = reader2; 
		IndexReader i3 = reader3; 
		IKAnalyzer analyzer = new IKAnalyzer(true);
		MultiReader multiReader = new MultiReader(i1,i2,i3);
		long start = System.currentTimeMillis();
		IndexSearcher indexsearcher = new IndexSearcher(multiReader);
	
		BooleanQuery.Builder booleanQuery = searchUtil.multiTableSearch(queryContent);//检索方式
		TopDocs hits = indexsearcher.search(booleanQuery.build(), 100);//匹配结果
		
		
		
		long end = System.currentTimeMillis();
		System.out.println("匹配"+queryContent+",总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"条记录");
		
	}
}
