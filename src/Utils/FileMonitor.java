package Utils;

import java.util.concurrent.TimeUnit;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;


public class FileMonitor {
	 public static void FileListenter() throws Exception{
	        String rootDir = "E:\\findfile";
	        long interval = TimeUnit.SECONDS.toMillis(1);
	        FileAlterationObserver observer = new FileAlterationObserver(rootDir);
	        observer.addListener(new MyFileListener());
	        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
	        monitor.start();
           System.out.println("-----开始监控-----");
	    }
	    /*public static void main (String[] args) throws Exception{
	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try{
	                    FileListenter();
	                } catch (Exception e){
	                    System.err.print(e);
	                }
	            }
	        }).start();
	        //如果文件发生变化那就对该文件创建索引，放在内存中。
	    }*/
	 
}
