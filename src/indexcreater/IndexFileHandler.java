package indexcreater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import Utils.FieldUtil;
import Utils.FieldUtilImpl;
import Utils.IndexUtil;

public class IndexFileHandler {

	IndexUtil indexUtil = new IndexUtil();
	private FieldUtil fieldUtil;
	/**
	 * 对单个文件进行索引
	 */
	public void indexSingleFile(Document doc,TrackingIndexWriter trackingIndexWriter,File file) throws FileNotFoundException, IOException{
		String filecontents = null;
		FieldUtil fieldUtil;
		fieldUtil = new FieldUtilImpl();
		if (file.getName().endsWith("doc")){
			filecontents = new WordExtractor(new FileInputStream(file)).getText().toString().replaceAll("\\s", "");								
		}				
		else if (file.getName().endsWith("docx")) 	{
			XWPFDocument document = new XWPFDocument(new FileInputStream(file));
		    filecontents = new XWPFWordExtractor(document).getText().replaceAll("\\s", "");
		}		
		if(filecontents!=null){
			doc.add(fieldUtil.stringField("filepath", file.getAbsolutePath(), Field.Store.YES));
			doc.add(fieldUtil.stringField("filename", file.getName(), Field.Store.YES));
			doc.add(fieldUtil.textField("filecontents", filecontents, Field.Store.YES));
		}
		trackingIndexWriter.addDocument(doc);//添加到索引里面
	}
	
	/**
	 * 对包含多个文件或文件夹的文件夹进行索引
	 */
	public void indexFiles(TrackingIndexWriter trackingIndexWriter,String root) throws IOException{
		File[] subFile = new File(root).listFiles();
		for (int i = 0; i < subFile.length; i++) {
			Document doc = new Document();
			if (subFile[i].isDirectory()) {
				indexFiles(trackingIndexWriter,subFile[i].getAbsolutePath());
			} 
			else {
				indexSingleFile(doc,trackingIndexWriter,subFile[i]);
			}
		}
	}
}
