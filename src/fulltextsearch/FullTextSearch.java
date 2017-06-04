package fulltextsearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;

public class FullTextSearch extends ServletMaster {

	private static final long serialVersionUID = 1L;
    FileSearcher filesearcher = new FileSearcher();
    DBSearcher dbsearcher = new DBSearcher();
    JSONArray jsonArray;
	public FullTextSearch() {
		super();
	}
	//模糊查询，或者限定查询范围
	public void fullTextTretrieval(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException,IllegalArgumentException, InvocationTargetException,NoSuchMethodException, ParseException, InvalidTokenOffsetsException {
		String searchcontent = request.getParameter("searchcon");
		String searchRange = request.getParameter("range");
		List<String> fileResult = null;
		List<String> dbResult = null;
		List<String> gitRepResult = null;
		// 这里创建三个线程来进行索引
		fileResult = filesearcher.searcherimpl(searchcontent);
		dbResult = dbsearcher.searcher(searchcontent);

		jsonArray = JSONArray.fromObject(fileResult);
		PrintWriter writer = response.getWriter();
		writer.print(jsonArray);
	} 
	//选择范围后查询
	public void rangeSearcher(HttpServletRequest request,HttpServletResponse response){
		String searchRange = request.getParameter("range");
		List<String> result = null;
		if(searchRange.equals("localFile")){
			//filesearcher.searcherimpl(searchcontent);
		}
	}
	//获取数据表名
	public void getTableNames(HttpServletRequest request,HttpServletResponse response) throws DocumentException, IOException{
		List<String> tableList = new ArrayList<String>(); 
		XmlAnalyzer xmlanalyzer = new XmlAnalyzer();
		Iterator<Element> tablesIterator = xmlanalyzer.getTables(xmlanalyzer.getXmlPath());
		while(tablesIterator.hasNext()){
			Element e = tablesIterator.next();
			String tablename = e.attribute("name").getText();
			tableList.add(tablename);
		}
		jsonArray = JSONArray.fromObject(tableList);
		PrintWriter writer = response.getWriter();
		writer.print(jsonArray);
	}
	
	public void multiThreadSearch(){
		
	}
	//创建两三个线程同时对文档和数据库的索引文件进行搜索
    
	
	/*public List<String> searcherimpl(String q) throws IOException, ParseException, InvalidTokenOffsetsException{
		String indexDir = "E:\\lucene\\fileindex";
		List<String> resultlist=new ArrayList<String>();
		Directory dir= FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(dir);
		long start = System.currentTimeMillis();
		IndexSearcher indexsearcher = new IndexSearcher(reader);
		IKAnalyzer analyzer = new IKAnalyzer(true);
		QueryParser parser = new QueryParser("filecontents",analyzer);
		Query query = parser.parse(q);
		TopDocs hits = indexsearcher.search(query, 10);//10是查询前10条数据
		long end = System.currentTimeMillis();
		System.out.println("匹配"+q+",总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"条记录");
		
		QueryScorer queryscore = new QueryScorer(query);//显示得分较高的片段
		Fragmenter fragmenter = new SimpleFragmenter(100);
		SimpleHTMLFormatter simplehtmlfor = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		Highlighter highlight = new Highlighter(simplehtmlfor, queryscore);
	    highlight.setTextFragmenter(fragmenter);
	    
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc = indexsearcher.doc(scoreDoc.doc);//文档的id
			System.out.println("文件路径"+doc.get("fullPath"));
			String contents = doc.get("filecontents");
			String filename = doc.get("filename");
			if(filename!=null){
				TokenStream tokenStream = analyzer.tokenStream("filecontents", new StringReader(contents)); 
				resultlist.add(filename);
			}
		}
		reader.close();
		return resultlist;
	}*/
	//创建一个新的线程用于处理内存索引
}
