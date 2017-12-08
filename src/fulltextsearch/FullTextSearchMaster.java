package fulltextsearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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

import Utils.JSONHelper;
import Utils.NearRealTimeSearch;
import Utils.SearchContentAnalyzer;
import Utils.SearchUtil;
import Utils.StringHandlerUtil;
import Utils.TableColumns;
import Utils.XmlAnalyzer;

public class FullTextSearchMaster extends ServletMaster {

	private static final long serialVersionUID = 1L;
    FileSearcher filesearcher = new FileSearcher();
    
    DBSearcher dbsearcher = new DBSearcher();
    JSONArray jsonArray;

	
	public FullTextSearchMaster() {
		super();
	}
	
	//在全部范围内做检索
	public void fullTextTretrieval(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException,IllegalArgumentException, InvocationTargetException,NoSuchMethodException, ParseException, InvalidTokenOffsetsException, DocumentException {
		NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
		String searchcontent = request.getParameter("searchcon");
		String searchRange = request.getParameter("range");
		List<FileInfo> fileResult = null;
		
		fileResult = nrtSearch.search(searchcontent);
		nrtSearch.close();
		JSONHelper.ResponseList(fileResult, response);
	} 
	
	//针对文件系统检索
	public void fileSearcher(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException,IllegalArgumentException, InvocationTargetException,NoSuchMethodException, ParseException, InvalidTokenOffsetsException, DocumentException {
		String searchContent = request.getParameter("searchcon");
		List<FileInfo> fileResult = null;
		NearRealTimeSearch nrtSearch = new NearRealTimeSearch();
		fileResult = nrtSearch.search(searchContent);
		fileResult = filesearcher.searcherimpl(searchContent);
		JSONHelper.ResponseList(fileResult, response);
	}
	
	//针对某张表或者几张做检索
	public void dbSearcher(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String searchContents = request.getParameter("searchcon");//content形式：name:邱敏明		
		StringHandlerUtil stringHandlerUltil = new StringHandlerUtil(); 
		/*searchContents = stringHandlerUltil.removeEndCharacter(searchContents);
		
		String[] resultArray = stringHandlerUltil.String2Array(searchContents, "\\|"); 
		for(int i=0;i<resultArray.length;i++){
			String content = resultArray[i];
			boolean result = stringHandlerUltil.isEndCharacter(content, ";");
			//String lastchar = content.substring(content.length()-1);
			System.out.println("最后一个是否是进行分割的字符"+result);
		}*/
		//SearchUtil searchUtil = new SearchUtil();
		SearchThreadPool multiTreadSearch = new SearchThreadPool();
		SearchContentAnalyzer searchContentAnalyzer = new SearchContentAnalyzer();
		multiTreadSearch.setSearchContentList(searchContentAnalyzer.tableContentHandler(searchContents));
		
		multiTreadSearch.exec();
		//searchUtil.searchTable(tableName, searchContents);
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
	@SuppressWarnings("unchecked")
	public void tableQueryFields(HttpServletRequest request,HttpServletResponse response) throws DocumentException, IOException{		
		String targetTable = request.getParameter("targetTable");
		XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
		List<TableColumns> columnList = xmlAnalyzer.columnTableMap(targetTable).get(targetTable);
		JSONHelper.ResponseList(columnList, response);
	}
	
}