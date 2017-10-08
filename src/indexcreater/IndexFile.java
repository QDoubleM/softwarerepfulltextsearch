package indexcreater;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IndexFile {
	
	private IndexWriter indexwriter;
	int mergeFactor = 10;
    int minMergeDocs = 10;
    int maxMergeDocs = Integer.MAX_VALUE;
	public void recursion(String root) throws Exception {
		File[] subFile = new File(root).listFiles();
		for (int i = 0; i < subFile.length; i++) {
			Document doc = new Document();
			if (subFile[i].isDirectory()) {
				recursion(subFile[i].getAbsolutePath());
			} 
			else {
				String filecontents = null;
				System.out.println(subFile[i].getName());
				if (subFile[i].getName().endsWith("doc")){
					filecontents = new WordExtractor(new FileInputStream(subFile[i])).getText().toString().replaceAll("\\s", "");								
				}				
				if (subFile[i].getName().endsWith("docx")) 	{
					XWPFDocument document = new XWPFDocument(new FileInputStream(subFile[i]));
				    filecontents = new XWPFWordExtractor(document).getText().replaceAll("\\s", "");
				}		
				if(filecontents!=null){
					doc.add(new StringField("filename",subFile[i].getName(),Field.Store.YES));
					doc.add(new TextField("filecontents",filecontents,Field.Store.YES));
					doc.add(new TextField("filepath",subFile[i].getCanonicalPath(),Field.Store.YES));
				}
				indexwriter.addDocument(doc);//添加到索引里面		
			}
		}
	}
	
	public IndexFile(String indexDir)throws Exception{
		Directory dir = FSDirectory.open(Paths.get(indexDir));	
		//IKAnalyzer analyzer = new IKAnalyzer(true);
		Analyzer smartAnalyzer = new SmartChineseAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(smartAnalyzer);
		indexwriter = new IndexWriter(dir,iwc);
	}
	
	public void close()throws Exception{
		indexwriter.close();
	}
	
	/*索引目录的所有文件*/
	public int index(String dataDir)throws Exception{
		recursion(dataDir);
		return indexwriter.numDocs(); 
	}
		
	public static void main(String args[]){
		String indexDir = "E:\\lucene\\fileindex";
		String dataDir = "E:/findfile";
		IndexFile indexer = null;
		int numIndexer = 0;
		long start = System.currentTimeMillis();
		try {
			indexer = new IndexFile(indexDir);
			numIndexer = indexer.index(dataDir);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				indexer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("索引："+numIndexer+"个文件共花费了："+(end-start)+"毫秒");
	}
}
