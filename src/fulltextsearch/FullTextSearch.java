package fulltextsearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import Utils.FileMonitor;
import Utils.JSONHelper;
import Utils.NearRealTimeSearch;
import Utils.SearchUtil;
import Utils.TableColumns;
import Utils.XmlAnalyzer;

public class FullTextSearch extends ServletMaster {

	private static final long serialVersionUID = 1L;
    FileSearcher filesearcher = new FileSearcher();
    
    DBSearcher dbsearcher = new DBSearcher();
    JSONArray jsonArray;
    NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
	public FullTextSearch() {
		super();
	}
	
	
	public void init(){
		FileMonitor fileMonitor = new FileMonitor();
		try {
			fileMonitor.FileListenter();
		} catch (Exception e) {
			 System.err.print(e);
		}
	}
	
	//在全部范围内做检索
	public void fullTextTretrieval(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException,IllegalArgumentException, InvocationTargetException,NoSuchMethodException, ParseException, InvalidTokenOffsetsException, DocumentException {
		String searchcontent = request.getParameter("searchcon");
		String searchRange = request.getParameter("range");
		System.out.println(searchRange);
		List<FileInfo> fileResult = null;
		//tableQueryFields(searchRange);
		
		//fileResult = nrtSearch.search(searchcontent);
		fileResult = filesearcher.searcherimpl(searchcontent);
		JSONHelper.ResponseList(fileResult, response);
	} 
	
	//针对文件系统检索
	public void fileSearcher(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException,IllegalArgumentException, InvocationTargetException,NoSuchMethodException, ParseException, InvalidTokenOffsetsException, DocumentException {
		String searchContent = request.getParameter("searchcon");
		String searchRange = request.getParameter("range");
		List<FileInfo> fileResult = null;
		fileResult = nrtSearch.search(searchContent);
		//fileResult = filesearcher.searcherimpl(searchContent);
		JSONHelper.ResponseList(fileResult, response);
	}
	
	//针对某张表或者几张做检索
	public void dbSearcher(HttpServletRequest request,HttpServletResponse response) throws DocumentException, IOException, ParseException{
		String tableName = request.getParameter("tablename");
		String searchContents = request.getParameter("content");//content形式：name:邱敏明
		searchContents.split(";");
		Query query=null;
		Builder booleanQuery = null;
		XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		SearchUtil searchUtil = new SearchUtil();
		searchUtil.searchTable(tableName, searchContents);
	}
	
	//获取数据表名
	@SuppressWarnings("static-access")
	public void getTableNames(HttpServletRequest request,HttpServletResponse response) throws DocumentException, IOException{
		List<String> tableList = new ArrayList<String>(); 
		XmlAnalyzer xmlanalyzer = new XmlAnalyzer();
		List<Element> tablesIterator = xmlanalyzer.getTableElement(xmlanalyzer.getXmlPath());
		for(Element tableElement : tablesIterator){       	
        	Attribute attribute = tableElement.attribute("name");
        	String tablename = attribute.getText();
        	tableList.add(tablename);
        }
		jsonArray = JSONArray.fromObject(tableList);
		PrintWriter writer = response.getWriter();
		writer.print(jsonArray);
	}
	
	//获取数据表的列的集合并返回值js
	public void tableQueryFields(HttpServletRequest request,HttpServletResponse response) throws DocumentException, IOException{
		TableColumns tc = new TableColumns();
		String targetTable = request.getParameter("targetTable");
		System.out.println(targetTable);
		XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		List<TableColumns> columnList = xmlAnalyzer.columnTableMap(targetTable).get(targetTable);
		JSONHelper.ResponseList(columnList, response);
	}
	
}