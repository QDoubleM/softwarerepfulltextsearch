package fulltextsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import Utils.SearchContent;

public class SearchThreadPool {
	private SearchContent searchContent;
	private List<SearchContent> searchContentList = new ArrayList<SearchContent>();
	
	public SearchContent getSearchContent() {
		return searchContent;
	}

	public void setSearchContent(SearchContent searchContent) {
		this.searchContent = searchContent;
	}

	public List<SearchContent> getSearchContentList() {
		return searchContentList;
	}

	public void setSearchContentList(List<SearchContent> searchContentList) {
		this.searchContentList = searchContentList;
	}
	
	public void exec() throws Exception {
		// 进行异步任务列表
		List<FutureTask<Integer>> futureTasks = new ArrayList<FutureTask<Integer>>();
		ExecutorService executorService = Executors.newFixedThreadPool(30);
		long start = System.currentTimeMillis();        
		
		//把多线程多目录搜索 searchContent为搜索的表的对象；
		for(SearchContent searchContent:searchContentList){
			SearchThread searchThread = new SearchThread();
			searchThread.setSearchContent(searchContent);
			FutureTask<Integer> futureTask = new FutureTask<>(searchThread);
			futureTasks.add(futureTask);
			executorService.submit(futureTask);
		}
		
		/*for (int i = 0; i < 2; i++) {
			// 创建一个异步任务
			searchThread.setSearchContent(searchContent);
			FutureTask<Integer> futureTask = new FutureTask<>(searchThread);
			futureTasks.add(futureTask);
			// 提交异步任务到线程池
			executorService.submit(futureTask);
		}*/
		int count = 0;
		for (FutureTask<Integer> futureTask : futureTasks) {
			count += futureTask.get();
		}
		long end = System.currentTimeMillis();
		System.out.println("线程池的任务全部完成:结果为:" + count + "，main线程关闭，进行线程的清理");
		System.out.println("使用时间：" + (end - start) + "ms");
		// 清理线程池
		executorService.shutdown();
	}
}
