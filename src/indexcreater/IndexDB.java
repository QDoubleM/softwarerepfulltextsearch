package indexcreater;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import fulltextsearch.ConnectDBs;
import fulltextsearch.XmlAnalyzer;

public class IndexDB {
	private static Connection conn = null;
	private static Statement stmt = null;

	private static String indexDir = "E:\\lucene\\";	
	//private static Analyzer analyzer = new IKAnalyzer(true);
	public static void main(String args[]) throws Exception{
		dataIndexInit();
	}			
	//获取表名列表	
	/*public static List<String> getColumns() throws DocumentException, IOException{
		List<List<String>> tablelist = XmlAnalyzer.getcolumns(XmlAnalyzer.getTables(XmlAnalyzer.getXmlPath()));
		List<String> tablenamelist = new ArrayList<String>();
		for(int i=0;i<tablelist.size();i++){
    		String tablename="";
    		List<String> columnlist = tablelist.get(i);
    		tablename=columnlist.get(0);
    		tablenamelist.add(tablename);
    	}
		return tablenamelist;
	}*/
	//获取具体表中数据集
	public static void dataIndexInit() throws Exception{
		conn = ConnectDBs.getConnection();
		IndexWriter indexwriter;
		List<List<String>> tablelist = XmlAnalyzer.getcolumns(XmlAnalyzer.getTables(XmlAnalyzer.getXmlPath()));
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
	}
	
    /*public static List<Path> indexPathInit() throws DocumentException, IOException{
    	List<List<String>> tablelist = XmlAnalyzer.getcolumns(XmlAnalyzer.getTables(XmlAnalyzer.getXmlPath()));
    	List<Path> indexFilePath = new ArrayList<Path>();
    	for(int i=0;i<tablelist.size();i++){
    		List<String> columnlist = tablelist.get(i);
    		String tablename=columnlist.get(0);
    		String indexdirectory = indexDir+tablename; 
    		indexFilePath.add(Paths.get(indexdirectory));
    	}
    	return indexFilePath;
    }*/  
    //索引创建
   /* public static void indexInit(ResultSet resultSet) throws IOException, SQLException, DocumentException{    	    		
    	Directory directory=null;
    	IndexWriter indexwriter=null;
    	List<Path> indexfilepath = indexPathInit();
    	Analyzer analyzer = new IKAnalyzer(true);
		IndexWriterConfig indexwriterconf = new IndexWriterConfig(analyzer);
		for(Path indexfiliepath:indexfilepath){
			directory = FSDirectory.open((Path) indexfilepath);
			indexwriter = new IndexWriter(directory, indexwriterconf);
			while(resultSet.next()){
				Document document = new Document();
				document.add(new StringField("columnname", resultSet.getString("columnname"), Field.Store.YES)); 
			}
		}		
		indexwriter.commit();
		indexwriter.close();
		directory.close();
		conn.close();
    }  
    public static void getData() throws SQLException{
    	conn = ConnectDb.Connect();
    	String sql = "select t_student.realName from t_student";
		stmt = conn.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		while(resultSet.next()){
			System.out.println(resultSet.getString("t_student.realName"));
		}
    }*/
}
