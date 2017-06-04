package fulltextsearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XmlAnalyzer {
	private static String indexDir = "E:\\lucene\\";
	static JdbcConfig jdbcconfig = new JdbcConfig();

	public static String getXmlPath() {
		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		path = path.replace("file:", ""); // 去掉file:
		path = path.replace("classes/", ""); // 去掉class\
		path = path.substring(1); // 去掉第一个\,如 \D:\JavaWeb...
		path += "testdbconfig.xml";
		return path;
	}
	public static JdbcConfig getConenectInfo() {
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File(getXmlPath()));
			Element driverNameElt = (Element) doc.selectObject("//test/db-info/driver-name");
			Element urlElt = (Element) doc.selectObject("//test/db-info/url");
			Element userNameElt = (Element) doc.selectObject("//test/db-info/user-name");
			Element passwordElt = (Element) doc.selectObject("//test/db-info/password");
			jdbcconfig.setDriverName(driverNameElt.getStringValue());
			jdbcconfig.setUrl(urlElt.getStringValue());
			jdbcconfig.setUserName(userNameElt.getStringValue());
			jdbcconfig.setPassword(passwordElt.getStringValue());
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return jdbcconfig;
	}
	
	//获取表名
	public static Iterator<Element> getTables(String filepath)throws DocumentException {		
		//List<String> tablelist = new ArrayList<String>();
		SAXReader reader = new SAXReader();
		org.dom4j.Document document = reader.read(new File(filepath));
		List<Element> tableList = document.selectNodes("//test/table");
		Iterator<Element> tablesiterator = tableList.iterator();
		return tablesiterator;
	}
	
	//获取列名
	public static List<List<String>> getcolumns(Iterator<Element> tablesiterator) throws DocumentException, IOException{
		String columns = "";
		List<List<String>> tablelist=new ArrayList<List<String>>();
		tablesiterator = getTables(getXmlPath());
		while (tablesiterator.hasNext()) {
			Element e = tablesiterator.next();
			Attribute attribute = e.attribute("name");// name是指标签属性
			if (!attribute.getText().equals("")) {
				List<Element> childElements = e.elements();
				Iterator<Element> childIterator = childElements.iterator();
				String indexdirectory = indexDir + attribute.getText();
				Directory directory = FSDirectory.open(Paths.get(indexdirectory));
				indexdirectory = indexDir;	
				List<String> columnlist = new ArrayList<String>();
				columnlist.add(attribute.getText());
				while (childIterator.hasNext()) {
					Element childElement = childIterator.next();
					columnlist.add(childElement.getText());
					columns = columns + "," + childElement.getText(); 
				}
				tablelist.add(columnlist);
				columns = columns.substring(1);
				columns ="";
			}
		}
		return tablelist;
	}
}
