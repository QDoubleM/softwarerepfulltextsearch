package Utils;

import indexcreater.IndexFileHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.dom4j.DocumentException;


import fulltextsearch.FileInfo;

public class NearRealTimeSearch {
	
	private TrackingIndexWriter trackingIndexWriter = null;
	private ReferenceManager<IndexSearcher> reManager = null;// 类似于Lucene3.x中的NrtManager
	
	private IndexWriter writer = null;
	private String path = "E:\\lucene\\fileindex";
	private NRTManager nrtManager ;
	public NearRealTimeSearch() {
		nrtManager = NRTManager.getNRTManager();
		try {
			writer = nrtManager.getindexWriter(path);
			reManager = nrtManager.getSearcherManager(path, writer);
			
			nrtManager.startThread();
			trackingIndexWriter = new TrackingIndexWriter(writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	/** 
	 * 获取IndexSearch
    **/
	public IndexSearcher getSearcher(){
		IndexSearcher indexSearcher = null;
		indexSearcher= nrtManager.getIndexSearcher(path, writer);
		return indexSearcher;
	}

	/**
	 * 增加索引
	 */
	public void addDocument(File file) throws FileNotFoundException, IOException{
		IndexFileHandler indexFile = new IndexFileHandler();
		indexFile.indexSingleFile(new Document(), trackingIndexWriter, file);
	}
	/**
	 * 删除索引
	 */
	public void deleteDocument(File file){
		Term term= new Term("filepath", file.getPath());
		Query query = new TermQuery(term);
		try {
			trackingIndexWriter.deleteDocuments(query);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 更新索引
	 */
	public void updateDocument(File file) throws FileNotFoundException, IOException{
		Document document = new Document();
		IndexFileHandler indexFile = new IndexFileHandler();
		document = indexFile.getDocument(file);
		trackingIndexWriter.updateDocument(new Term("filepath", file.getPath()), document);
	}
	
	/**
	 * 查询时search如果使用完成，需要将search释放会searchFactory中，使用reManager.release(indexSearcher)
	 * @return 
	 * @throws InvalidTokenOffsetsException 
	 * **/
	public List<FileInfo> search(String q) throws IOException, ParseException, InvalidTokenOffsetsException {
		SearchUtil searchUtil = new SearchUtil();
		IndexSearcher indexSearcher = getSearcher();
		
		List<FileInfo> resultlist=new ArrayList<>();
		try {
			//IKAnalyzer smartAnalyzer = new IKAnalyzer(true);
			Analyzer smartAnalyzer = new SmartChineseAnalyzer();
			Query query = searchUtil.queryPaserSearch("filecontents", q);
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
	
	/**
	 *检索数据库索引 
	 */
	public void dbSearch() throws IOException, ParseException, DocumentException{
		SearchUtil searchUtil = new SearchUtil();
		searchUtil.searchTable("tableName", "stearchContens");
	}
	
	/**
	 * 关闭初始化线程的处理
	 */
	public void close() {
		nrtManager.threadClose();
		try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}


