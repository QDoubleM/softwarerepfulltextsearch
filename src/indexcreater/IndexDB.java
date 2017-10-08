package indexcreater;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import Utils.ConnectDBs;
import Utils.FieldUtil;
import Utils.FieldUtilImpl;
import Utils.XmlAnalyzer;


public class IndexDB {
	private static Connection conn = null;
	private static Statement stmt = null;
	private static String indexFilePath = "E:\\lucene\\";
	
	public static void main(String args[]) throws Exception{
		indexPathInit();
	}				
	//获取具体表中数据集
	/*public static void dataIndexInit() throws Exception{
		conn = ConnectDBs.getConnection();
		IndexWriter indexwriter;
		List<List<String>> tablelist = XmlAnalyzer.getcolumns(XmlAnalyzer.getTableList(XmlAnalyzer.getXmlPath()));
    	for(int i=0;i<tablelist.size();i++){
    		String columns="";
    		List<String> columnlist = tablelist.get(i);
    		String tablename=columnlist.get(0);
    		for(int j=1;j<columnlist.size();j++){
    			columns = columns + "," +columnlist.get(j);
    		}
    		columns = columns.substring(1);
    		String sql = "select "+columns+" from "+tablename;
    		stmt = conn.createStatement();
    		ResultSet resultSet = stmt.executeQuery(sql);//获取表的结果集    		
    		//开始建立索引
    		String indexdirectory = indexDir + tablename;						
			Analyzer analyzer = new IKAnalyzer(true);   

			Directory directory = FSDirectory.open(Paths.get(indexdirectory));
			IndexWriterConfig conf = new IndexWriterConfig(analyzer);
			indexwriter = new IndexWriter(directory, conf);
			while (resultSet.next()) {
				Document tabledoc = new Document();
				for (int j = 1; j < columnlist.size(); j++) {
					tabledoc.add(new StringField(columnlist.get(j), resultSet.getString(columnlist.get(j)), Field.Store.YES));
					System.out.println(columnlist.get(j)+":"+resultSet.getString(columnlist.get(j)));
					
				}
				indexwriter.addDocument(tabledoc);
			}
			indexwriter.commit();
			indexwriter.close();
			directory.close();
			indexdirectory = indexDir;
		}
    	conn.close();
	}*/
	
    @SuppressWarnings("static-access")
	public static void indexPathInit() throws DocumentException, IOException, ClassNotFoundException, SQLException{
    	conn = ConnectDBs.getConnection();
    	Analyzer smartAnalyzer = new SmartChineseAnalyzer();
    	XmlAnalyzer xmlanalyzer = new XmlAnalyzer();
    	FieldUtil fieldUtil;
    	FieldUtilImpl fieldUtilImpl = new FieldUtilImpl();
    	fieldUtil = fieldUtilImpl;
    	Store storeType = Field.Store.YES;
		List<Element> tableList = xmlanalyzer.getTableElement(xmlanalyzer.getXmlPath());		
		//对每个表进行索引
		
		for(Element tableElement : tableList){       	
        	Directory directory = FSDirectory.open(Paths.get(indexFilePath+xmlanalyzer.tableName(tableElement))); 
        	IndexWriterConfig conf = new IndexWriterConfig(smartAnalyzer);
			IndexWriter indexwriter = new IndexWriter(directory, conf);
    		String indexColumns = xmlanalyzer.getIndexColumns(tableElement);
    		ResultSet resultSet = getResultSet(xmlanalyzer.tableName(tableElement),indexColumns);
    		  		
    		Map<String,String> columnType = getColumnType(resultSet);
    		List<String> storeColumns = xmlanalyzer.getStoreColumns(tableElement);
    		
    		while(resultSet.next()){
    			Document tabledoc = new Document();
    			tabledoc.add(fieldUtil.textField("tableName", xmlanalyzer.tableName(tableElement), storeType));
    			for(String columnName : indexColumns.split(",")){
    				
    				if(!storeColumns.contains(columnName)){
        				//分词,使用textfield
        	    		tabledoc.add(fieldUtil.textField(columnName, resultSet.getString(columnName), storeType));
        			}else{
        				//不分词，如果是char类型那就用stringfield,如果是integer那就使用storedfield
        				if(columnType.get(columnName).toLowerCase().equals("char")||columnType.get(columnName).toLowerCase().equals("varchar")){
        					tabledoc.add(fieldUtil.stringField(columnName, resultSet.getString(columnName), storeType));
        				}else{
        					if(columnType.get(columnName).toLowerCase().equals("integer")){
        						tabledoc.add(fieldUtil.stringField(columnName, resultSet.getString(columnName),storeType));
        					}
        				}
        			}
        		}
        		indexwriter.addDocument(tabledoc);
    		}
    		indexwriter.commit();
    		indexwriter.close();
    		directory.close();
        }
    } 
     
    public static ResultSet getResultSet(String tableName,String columns) throws SQLException, ClassNotFoundException{    	
		String sql = "select "+columns+" from "+tableName;
		stmt = conn.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		System.out.println(resultSet);
		return resultSet;
    }
    
    //判断字段类型
    public static Map<String, String> getColumnType(ResultSet resultSet) throws SQLException{
    	Map<String,String> columnType = new HashMap<String,String>();
    	ResultSetMetaData resultSetMetaData= resultSet.getMetaData();
    	if(resultSet.first()){
    		for(int i=1;i<=resultSetMetaData.getColumnCount();i++){    			
    			columnType.put(resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnTypeName(i));
    			System.out.println(resultSetMetaData.getColumnName(i)+ resultSetMetaData.getColumnTypeName(i));
    		}
    	}
    	resultSet.beforeFirst();
    	
    	return columnType;
    }
    
    public static void indexType(String dataType){
    }
    
}
