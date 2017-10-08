package Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class NRTManager {
	private IndexWriter writer;
	private IndexWriterConfig indexWriterConfig;
	private Analyzer smartAnalyzer;
	private TrackingIndexWriter trackingIndexWriter;
	private ReferenceManager<IndexSearcher> reManager = null;// 类似于Lucene3.x中的NrtManager
	private ControlledRealTimeReopenThread<IndexSearcher> conRealTimeOpenThread = null;
	/**
	 * 每个索引目录都只有一个IndexWriter
	 * @return 
	 * */
	public IndexWriter getindexWriter(String path) throws IOException{
		Map<String,IndexWriter> indexWriterMap = new HashMap<String,IndexWriter>();
		smartAnalyzer = new SmartChineseAnalyzer();
		indexWriterConfig = new IndexWriterConfig(smartAnalyzer);
		Directory directory = FSDirectory.open(Paths.get("E:\\lucene\\fileindex"));
		if(! indexWriterMap.containsKey(path)){
			writer = new IndexWriter(directory, indexWriterConfig);
			indexWriterMap.put(path, writer);
		}else{
			writer = indexWriterMap.get(path);
		}
		return writer;
	} 
	
	public ReferenceManager<IndexSearcher> getSearcherManager(String path,IndexWriter writer) throws IOException{
		trackingIndexWriter = new TrackingIndexWriter(writer);
		try {
			reManager = new SearcherManager(writer, true, new SearcherFactory());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reManager;
	}
	
	public void startThread(){
		conRealTimeOpenThread = new ControlledRealTimeReopenThread<>(trackingIndexWriter,reManager, 5.0, 0.025);
		conRealTimeOpenThread.setDaemon(true);// 设置为后台服务
		conRealTimeOpenThread.setName("near real time search");// 线程名称
		conRealTimeOpenThread.start();// 线程启动
	}
	
	public IndexSearcher getIndexSearcher(String path,IndexWriter writer){
		IndexSearcher indexSearcher = null;
		try {
			reManager = getSearcherManager(path,writer);		
			reManager.maybeRefresh();// 刷新reManager,获取最新的IndexSearcher
			indexSearcher = reManager.acquire();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return indexSearcher;
	}
	
	public void relsease(IndexSearcher indexSearcher){
		try {
			reManager.release(indexSearcher);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		conRealTimeOpenThread.interrupt();
		conRealTimeOpenThread.close();
	}
	
}
