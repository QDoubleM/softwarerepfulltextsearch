package Utils;

import indexcreater.IndexFileHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class MyFileListener extends FileAlterationListenerAdaptor {
	private IndexWriter writer;
	private IndexWriterConfig indexWriterConfig;
	private Analyzer smartAnalyzer;
	private TrackingIndexWriter trackingIndexWriter;
	private String path = "E:\\lucene\\fileindex";
	IndexFileHandler indexFile = new IndexFileHandler();
	@Override
	public void onFileCreate(File file) {
		System.out.println("[新建]:" + file.getAbsolutePath());
		addDocument(file);
		commit();
		close();
	}
	// 文件内容改变
	@Override
	public void onFileChange(File file) {
		System.out.println("[修改]:" + file.getAbsolutePath());
		updateDocument(file);
		commit();
		close();
	}

	// 删除索引
	@Override
	public void onFileDelete(File file) {
		deleteDocument(file);
		close();
	}
	
	private TrackingIndexWriter getindexWriter(String path) throws IOException, InterruptedException{
		Map<String,IndexWriter> indexWriterMap = new HashMap<String,IndexWriter>();
		smartAnalyzer = new SmartChineseAnalyzer();
		indexWriterConfig = new IndexWriterConfig(smartAnalyzer);
		Directory directory = FSDirectory.open(Paths.get(path));
		if(! indexWriterMap.containsKey(path)){
			while (IndexWriter.isLocked(directory)){
				Thread.sleep(1000);
			}
			writer = new IndexWriter(directory, indexWriterConfig);
			indexWriterMap.put(path, writer);
		}else{
			writer = indexWriterMap.get(path);
		}
		trackingIndexWriter = new TrackingIndexWriter(writer);
		return trackingIndexWriter;
	}
	/**
	 * 增加索引
	 */
	private void addDocument(File file){
		try {
			trackingIndexWriter = getindexWriter(path);
			indexFile.indexSingleFile(new Document(), trackingIndexWriter, file);			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 删除索引
	 */
	public void deleteDocument(File file){
		Term term= new Term("filepath", file.getPath());
		Query query = new TermQuery(term);
		try {
			trackingIndexWriter = getindexWriter(path);
			trackingIndexWriter.deleteDocuments(query);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 更新索引
	 */
	public void updateDocument(File file){
		Document document = new Document();
		try {
			trackingIndexWriter = getindexWriter(path);
			document = indexFile.getDocument(file);
			trackingIndexWriter.updateDocument(new Term("filepath", file.getPath()), document);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void commit(){
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void close(){
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
