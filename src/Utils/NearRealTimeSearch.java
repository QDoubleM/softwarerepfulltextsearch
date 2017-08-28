package Utils;

import indexcreater.IndexFileHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentException;



import fulltextsearch.FileInfo;

public class NearRealTimeSearch {
	
	private TrackingIndexWriter trackingIndexWriter = null;
	private ReferenceManager<IndexSearcher> reManager = null;// 类似于Lucene3.x中的NrtManager
	private ControlledRealTimeReopenThread<IndexSearcher> conRealTimeOpenThread = null;
	private IndexWriter writer = null;
	private SearcherManager searchManger;
	private IndexWriterConfig indexWriterConfig;
	private String indexFilePath;
	private ReentrantReadWriteLock writerLock = new ReentrantReadWriteLock();
	public NearRealTimeSearch() {
		try {
			Analyzer smartAnalyzer = new SmartChineseAnalyzer();
			Directory directory = FSDirectory.open(Paths.get("E:\\lucene\\fileindex"));
			indexWriterConfig = new IndexWriterConfig(smartAnalyzer);
			if(IndexWriter.isLocked(directory)){
				System.out.println("被锁了");
				writerLock.writeLock().unlock();
			}
				writer = new IndexWriter(directory, indexWriterConfig);
				trackingIndexWriter = new TrackingIndexWriter(writer);
				reManager = new SearcherManager(writer, true, new SearcherFactory());
				
				//在0.025s~5.0s之间重启一次线程的时间 
				conRealTimeOpenThread = new ControlledRealTimeReopenThread<>(trackingIndexWriter,reManager, 5.0, 0.025);
				conRealTimeOpenThread.setDaemon(true);// 设置为后台服务
				conRealTimeOpenThread.setName("near real time search");// 线程名称
				conRealTimeOpenThread.start();// 线程启动
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** 
	 * 使用单例获取IndexSearch
    **/
	public IndexSearcher getSearcher() {
		IndexSearcher indexSearcher = null;
		try {
				String indexDir = "E:\\lucene\\fileindex";
				List<FileInfo> resultlist=new ArrayList<>();
				//reManager = new SearcherManager(writer, true, new SearcherFactory());
				reManager.maybeRefresh();// 刷新reManager,获取最新的IndexSearcher
				indexSearcher = reManager.acquire();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return indexSearcher;
	}

	//删除索引
	public void deleteDocument(File file) throws IOException{
        trackingIndexWriter = new TrackingIndexWriter(writer);
		Term term= new Term("filepath", file.getPath());
		trackingIndexWriter.deleteDocuments(term);
	    System.out.println("删除结束");
	}
	
	//更新索引
	public static void updateDocument(){
		//trackingIndexWriter.updateDocuments(t, docs);
	}
	
	public void addDocument(File file) throws FileNotFoundException, IOException{
		IndexFileHandler indexFile = new IndexFileHandler();
		indexFile.indexSingleFile(new Document(), trackingIndexWriter, file);
		//close();
	}
	/**
	 * 查询时search如果使用完成，需要将search释放会searchFactory中，使用reManager。release(indexSearcher)
	 * @return 
	 * @throws InvalidTokenOffsetsException 
	 * **/
	public List<FileInfo> search(String q) throws IOException, ParseException, InvalidTokenOffsetsException {
		IndexSearcher indexSearcher = getSearcher();
		//reManager = new SearcherManager(writer, true, new SearcherFactory());  		
		
		List<FileInfo> resultlist=new ArrayList<>();
		try {
			Analyzer smartAnalyzer = new SmartChineseAnalyzer();
			QueryParser parser = new QueryParser("filecontents",smartAnalyzer);
			Query query = parser.parse(q);
			TopDocs hits = indexSearcher.search(query, 100);//10是查询前10条数据
			
			QueryScorer queryscore = new QueryScorer(query);//显示得分较高的片段
			Fragmenter fragmenter = new SimpleFragmenter(100);
			SimpleHTMLFormatter simplehtmlfor = new SimpleHTMLFormatter("","");
			Highlighter highlight = new Highlighter(simplehtmlfor, queryscore);
		    highlight.setTextFragmenter(fragmenter);
		    
			for(ScoreDoc scoreDoc:hits.scoreDocs){
				FileInfo fileInfo = new FileInfo();
				Document doc = indexSearcher.doc(scoreDoc.doc);
				String contents = doc.get("filecontents");
				fileInfo.setFileName(doc.get("filename"));
				fileInfo.setFilePath(doc.get("filepath"));
				System.out.println(doc.get("filepath"));
				if(contents!=null){
					TokenStream tokenStream = smartAnalyzer.tokenStream("contents", new StringReader(contents)); 				
					fileInfo.setFileContent(highlight.getBestFragment( tokenStream, contents));
					resultlist.add(fileInfo);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reManager.release(indexSearcher);				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultlist;
	}
	
	//检索数据库索引
	public void dbSearch() throws IOException, ParseException, DocumentException{
		SearchUtil searchUtil = new SearchUtil();
		searchUtil.searchTable("tableName", "stearchContens");
	}
	
	/**
	 * 关闭初始化线程的处理
	 */
	public void close() {
		conRealTimeOpenThread.interrupt();
		conRealTimeOpenThread.close();
		try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}

