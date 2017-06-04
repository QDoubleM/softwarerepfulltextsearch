package fulltextsearch;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

public class MultiThreadSearch implements Runnable{
	private String searchContent;
	List<String> fileResult = null;
	List<String> dbResult = null;
	FileSearcher filesearcher = new FileSearcher();
	DBSearcher dbsearcher = new DBSearcher();
	public void setName(String searchContent) 
	{ 
	this.searchContent = searchContent; 
	} 
	@Override
	public void run() {
		String range = Thread.currentThread().getName();
		//System.out.println(Thread.currentThread().getName() + searchContent);
		try {
			if (range.equals("localFile")) {
				fileResult = filesearcher.searcherimpl(searchContent);
				System.out.println("一共有"+fileResult.size()+"条记录来自本地文件");
			}
			if (range.equals("DB")) {
				dbResult = dbsearcher.searcher(searchContent);
				System.out.println("一共有"+dbResult.size()+"条记录来自本地文件");
			}
		} catch (IOException | ParseException | InvalidTokenOffsetsException e) {
			e.printStackTrace();
		}
	}
	public void totalResult(){
		System.out.println();
	}
	public static void main(String[] args) {		
		String searchContent = "邱敏明";
		MultiThreadSearch st = new MultiThreadSearch();
		st.setName(searchContent);
		new Thread(st, "localFile").start();
		new Thread(st, "DB").start();
		//new Thread(st, "GitRep").start();
	}
}
