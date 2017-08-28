package Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import fulltextsearch.FileSearcher;


public class getDBConfigInfo {
	static FileSearcher filesearcher = new FileSearcher();
	static XmlAnalyzer xmlAnalizer = new XmlAnalyzer();
	static String indexFilePath = "E:/lucene/";
	static File dbIndexFile;
	public static void main(String[] args) throws Exception{
		test();
	}
	
	@SuppressWarnings("unchecked")
	public static void test() throws Exception{  
        SAXReader reader = new SAXReader();  
        Document document = reader.read(new File(xmlAnalizer.getXmlPath()));  
        List<Element> tableList = document.selectNodes("//DataBase/table");
        Element root=document.getRootElement();     
        /*for(Element tableElement : tableList){
        	System.out.println(tableElement.getName());       	
        	List<Attribute> attributeList = tableElement.attributes();
        	List<Element> fieldList = tableElement.elements("indexfield");
        	for(Element fieldElement:fieldList){
        		List<Element> list = fieldElement.elements();
        		for(Element element:list){
        			System.out.println(element.getName()+":"+element.getText());
        		}
        	}
        	
        }*/
       listNodes(root);  
    }  
      
    //遍历当前节点下的所有节点  
    public static void listNodes(Element node){  
        System.out.println("当前节点的名称：" + node.getName());
        if(node.getName().equals("table")){
        	dbIndexFile = new File(indexFilePath + node.attribute("name").getText());
            dbIndexFile.mkdir();
            List<String> columnList = new ArrayList<>();
        }
        //首先获取当前节点的所有属性节点  
        List<Attribute> list = node.attributes();  
        //遍历属性节点  
        for(Attribute attribute : list){  
        	
            System.out.println("属性"+attribute.getName() +":" + attribute.getValue());  
        }  
        //如果当前节点内容不为空，则输出  
        if(!(node.getTextTrim().equals(""))){  
             System.out.println( node.getName() + "：" + node.getText());    
        }  
        //同时迭代当前节点下面的所有子节点  
        Iterator<Element> iterator = node.elementIterator();  
        while(iterator.hasNext()){  
            Element e = iterator.next();  
            listNodes(e); 
        }  
    } 
}
