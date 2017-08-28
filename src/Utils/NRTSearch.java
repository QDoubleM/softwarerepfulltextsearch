package Utils;

import fulltextsearch.FileInfo;
import indexcreater.IndexUpdateUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class NRTSearch {
	private Directory directory;
	private SmartChineseAnalyzer analyzer;
	// //追踪writer，这样才能在更新之后通知搜索
	private TrackingIndexWriter writer;
	// 是线程安全的.第二个参数是是否在所有缓存清空后让search看到
	private SearcherManager searcherManager;
	private IndexSearcher searcher;

	public static void main(String[] args) throws IOException, ParseException, InvalidTokenOffsetsException {

		//NRTSearch nrt =  new NRTSearch();
		//nrt.delete();
		String q= "邱敏明";
		IndexUpdateUtil iu = new IndexUpdateUtil();
		NearRealTimeSearch n = new NearRealTimeSearch();
		for(int i = 0;i < 5;i++){
			//n.search(q);
			if(i==2){
				//n.deleteDocument(new File("E:/findfile/gaigemingzikankan/毕业生户档转回生源地的申请表--邱敏明.doc"));
				n.addDocument(new File("E:/findfile/gaigemingzikankan/新建文件夹/aa3.doc"));
			}
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			System.out.println("--------------");
		}
		n.close();
	}

	public NRTSearch() throws IOException {
		directory = FSDirectory.open(Paths.get("E:/lucene/findfile"));
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		writer = new TrackingIndexWriter(indexWriter);
		searcherManager = new SearcherManager(indexWriter, true,
				new SearcherFactory());
		ControlledRealTimeReopenThread CRTReopenThead = new ControlledRealTimeReopenThread(
				writer, searcherManager, 5.0, 0.025);
		// 守护线程，又叫后台线程，级别比较低，如果没有主线程这个也会消失，这个线程作用就是定期更新让searchManager管理的search能获得更新
		CRTReopenThead.setDaemon(true);
		CRTReopenThead.setName("更新线程");
		CRTReopenThead.start();
//		this.addDoc();
		this.searchDoc();
		
	}
	
	/*public void delete() throws IOException {
		Directory directory = new RAMDirectory();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, iwc);
		writer = new TrackingIndexWriter(indexWriter);
		Term term = new Term("filepath","E:/findfile/gaigemingzikankan/毕业设计说明书_张娟.doc");
		writer.deleteDocuments(term);
		searcherManager = new SearcherManager(indexWriter, true,
				new SearcherFactory());

		ControlledRealTimeReopenThread<IndexSearcher> CRTReopenThread = new ControlledRealTimeReopenThread<IndexSearcher>(
				writer, searcherManager, 5.0, 0.025);
		CRTReopenThread.setDaemon(true);
		CRTReopenThread.start();
	}*/
	 
	/*public synchronized void addDoc() {
		final int i = 0;
		new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
						System.out.println("----加入文档中");
						Document doc = new Document();
						doc.add(new Field("title", "标题" + i,
								TextField.TYPE_STORED));
						doc.add(new Field("content", "我爱ww你中国" + i,
								TextField.TYPE_STORED));
						writer.addDocument(doc);

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}*/
	
	
	
	public synchronized void searchDoc() {
		new Thread() {
			public void run() {
				while (true) {
					try {
						//Thread.sleep(5000);
						System.out.println("----检索中");
						searcher = searcherManager.acquire();
						QueryParser parser = new QueryParser("filecontents",new SmartChineseAnalyzer());
						Query query = parser.parse("宠物商店");
						TopDocs hits = searcher.search(query, 100);
						System.out.println(hits.totalHits);
						for(ScoreDoc scoreDoc:hits.scoreDocs){
							FileInfo fileInfo = new FileInfo();
							Document doc = searcher.doc(scoreDoc.doc);
							System.out.println(doc.get("filepath"));
						}
					} catch (IOException|ParseException e) {
						e.printStackTrace();
					}finally {
						try {
							 
							searcherManager.release(searcher);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					searcher = null;
				}
			};
		}.start();
	}
}

