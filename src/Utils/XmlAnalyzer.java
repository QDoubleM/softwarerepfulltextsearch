package Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;



public class XmlAnalyzer {
	static JdbcConfig jdbcconfig = new JdbcConfig();

	public static String getXmlPath() {
		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		path = path.replace("file:", ""); // 去掉file:
		path = path.replace("classes/", ""); // 去掉class\
		path = path.substring(1); // 去掉第一个\,如 \D:\JavaWeb...
		path += "dbconfig.xml";
		return path;
	}
	public static JdbcConfig getConenectInfo() {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File(getXmlPath()));
			
			jdbcconfig.setDriverName(document.selectSingleNode("//DataBase/db-info/driver-name").getText());
			jdbcconfig.setUrl(document.selectSingleNode("//DataBase/db-info/url").getText());
			jdbcconfig.setUserName(document.selectSingleNode("//DataBase/db-info/user-name").getText());
			jdbcconfig.setPassword(document.selectSingleNode("//DataBase/db-info/password").getText());
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return jdbcconfig;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Element> getTableElement(String filepath)throws DocumentException {				
		SAXReader reader = new SAXReader();
		org.dom4j.Document document = reader.read(new File(filepath));
		
		List<Element> tableElementList = document.selectNodes("//DataBase/table");		
		return tableElementList;
	}
	
	public static String tableName(Element tableElement) {
		String tableName = tableElement.attribute("name").getText();
		return tableName;
	}
	
	@SuppressWarnings("unchecked")
	public static String getIndexColumns(Element tableNode) throws IOException {
		String columns = "";
		Attribute primaryKeyAttr = tableNode.attribute("primaryKey");
		columns = primaryKeyAttr.getText();
		List<Element> fieldList = tableNode.elements("indexfield");
		for (Element fieldElement : fieldList) {
			List<Element> list = fieldElement.elements();
			for (Element element : list) {
				if (element.getName().equals("name")) {
					columns = columns + "," + element.getText();
				}
			}
		}
		return columns;
	}

	public static List<String> getStoreColumns(Element tableNode) throws IOException{		
		String[] storeColumnArray = tableNode.element("storecolumn").getText().split(",");
		List<String> storeColumnList=Arrays.asList(storeColumnArray);
		return storeColumnList;
	} 
	
	@SuppressWarnings("null")
	public static List<TableColumns> columnsObjectList(Element tableNode) throws IOException{
		List<String> storeColumnList = getStoreColumns(tableNode);	
		List<Element> fieldList = tableNode.elements("indexfield");		
		String primaryKey = tableNode.attribute("primaryKey").getText();
		
		TableColumns primaryColumn = new TableColumns();
		primaryColumn.setName(primaryKey);
		primaryColumn.setText(primaryKey);
		primaryColumn.setIsAnalyzed(storeColumnList.contains(primaryKey));
		
		List<TableColumns> columnsObjList = new ArrayList<TableColumns>();
		for (Element fieldElement : fieldList) {			
			List<Element> list = fieldElement.elements();			
			TableColumns tableColumn = new TableColumns();
			for (Element element : list) {	
				if(element.getName().equals("name")){
					tableColumn.setName(element.getText());
					tableColumn.setIsAnalyzed(storeColumnList.contains(element.getText()));
				}else{
					if(element.getName().equals("text")){
						tableColumn.setText(element.getText());
					}
				}					
			}
			columnsObjList.add(tableColumn);
		}
		columnsObjList.add(primaryColumn);
		return columnsObjList;
	}
	
	public static Map<String, List> columnTableMap(String tableName) throws DocumentException, IOException{
		Document document = null;
		List<Element> tableList = getTableElement(getXmlPath());
		Map<String, List> tablemap = new HashMap<String,List>();
		for(Element tableElement : tableList){
			if(tableName(tableElement).equals(tableName)){
				tablemap.put(tableName(tableElement), columnsObjectList(tableElement));
			}		
		}
		return tablemap;
	}
}
