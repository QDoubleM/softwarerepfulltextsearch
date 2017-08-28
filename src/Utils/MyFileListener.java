package Utils;

import indexcreater.IndexFile;
import indexcreater.IndexFileHandler;
import indexcreater.IndexUpdateUtil;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

public class MyFileListener extends FileAlterationListenerAdaptor {
	IndexUpdateUtil indexUpdateUtil;
	//NearRealTimeSearch nrtSearch;
	 @Override  
	    public void onFileCreate(File file) {  
	        System.out.println("[新建]:" + file.getAbsolutePath()); 
	        //NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
	        /*try {
	        	IndexFileHandler indexFile = new IndexFileHandler();
	        	IndexUpdateUtil indexUpdate = new IndexUpdateUtil();
	        	indexUpdate.addDocument(file);
	    		
				//nrtSearch.addDocument(file);
			} catch (IOException e) {
				e.printStackTrace();
			}*/
	    }  
	 
	    //文件内容改变
	    @Override  
	    public void onFileChange(File file) {  
	        System.out.println("[修改]:" + file.getAbsolutePath());
	        NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
	        nrtSearch.updateDocument();
	    }  
	    
	    
	    @Override  
	    public void onFileDelete(File file) {  
	        //删除索引
	        try {
	        	/*NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
	        	System.out.println("run?");
	        	nrtSearch.deleteDocument(file);*/
	        	indexUpdateUtil = new IndexUpdateUtil();
	        	indexUpdateUtil.deleteDocument(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    } 
}
