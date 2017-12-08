package indexcreater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class IndexUpdateUtil {
	private static IndexWriter indexWriter;
	private static TrackingIndexWriter trackWriter;
	private Document document;
	private static SmartChineseAnalyzer analyzer;
	IndexFileHandler indexFileHandler;
	/**
	 * 删除索引document
	 */
	public static void deleteDocument(File file) throws IOException{
		IndexWriter indexWriter;
		Directory directory = FSDirectory.open(Paths.get("E:/lucene/fileindex"));
		IndexWriterConfig icw = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(directory, icw);
        trackWriter = new TrackingIndexWriter(indexWriter);
        System.out.println(file.getPath());
		Term term= new Term("filepath", file.getPath());
		trackWriter.deleteDocuments(term);
		indexWriter.commit();  
	    indexWriter.close();
	    System.out.println("删除结束");
	}
	/**
	 * 添加索引document
	 */
	public void addDocument(File file) throws FileNotFoundException, IOException{
		document = new Document();
		IndexWriterConfig icw = new IndexWriterConfig(analyzer);
		Directory directory = FSDirectory.open(Paths.get("E:/lucene/fileindex"));
        indexWriter = new IndexWriter(directory, icw);
        trackWriter = new TrackingIndexWriter(indexWriter);
		indexFileHandler.indexSingleFile(document, trackWriter, file);
	}
	
	public void incrementalIndexing() throws IOException{
		Directory ramDirectory = new RAMDirectory();
		analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		indexWriter = new IndexWriter(ramDirectory,iwc);
	}

	public void ramIndex2FSDIndex() throws IOException {
		Directory ramDirectory = new RAMDirectory();
		FSDirectory fsdDirectory = FSDirectory.open(Paths.get(""));
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		IndexWriter fsdIndexWriter = new IndexWriter(fsdDirectory, iwc);
		fsdIndexWriter.addIndexes(ramDirectory);
		fsdIndexWriter.commit();
		fsdIndexWriter.close();
	}
}

