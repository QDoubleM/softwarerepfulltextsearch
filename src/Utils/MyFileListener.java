package Utils;

import indexcreater.IndexFile;
import indexcreater.IndexFileHandler;
import indexcreater.IndexUpdateUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

public class MyFileListener extends FileAlterationListenerAdaptor {
	 @Override  
	    public void onFileCreate(File file) {  
	        System.out.println("[新建]:" + file.getAbsolutePath()); 
	        NearRealTimeSearch nrtSearch = new NearRealTimeSearch();	       
	        try {
				nrtSearch.addDocument(file);
				nrtSearch.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }  
	 
	    //文件内容改变
	    @Override  
	    public void onFileChange(File file) {  
	        System.out.println("[修改]:" + file.getAbsolutePath());
	        NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
	        try {
				nrtSearch.updateDocument(file);
				nrtSearch.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }  
	    
	    //删除索引
	    @Override  
	    public void onFileDelete(File file) {  
	    	NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
	        nrtSearch.deleteDocument(file);	 
			nrtSearch.close();
	    } 
}
