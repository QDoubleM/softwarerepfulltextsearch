package fulltextsearch;
import java.nio.file.Paths;
import java.sql.Connection;  
import java.sql.ResultSet;  
import java.sql.Statement;  
import java.util.ArrayList;  
import java.util.List;  

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class SearchDBTable {
	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	//private static ResultSet rs1 = null;
	// 索引保存目录
	private String indexDir = "E:\\lucence\\dataindex";
	private static IndexSearcher searcher = null;
	// 创建分词器
	private static Analyzer analyzer = new IKAnalyzer(true);

	public List<TeacherInfo> getResult(String queryStr) throws Exception {
		List<TeacherInfo> result = null;
		conn = ConnectDBs.getConnection();
		if (conn == null) {
			throw new Exception("数据库连接失败！");
		}
		String sql = "select * from t_teacher";
		//String sql1= "select table_name from information_schema.tables where table_schema='test';";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			//rs1 = stmt.executeQuery(sql1);
			// 给数据库创建索引,此处执行一次，不要每次运行都创建索引
			// 以后数据有更新可以后台调用更新索引
			this.createIndex(rs);
			TopDocs topDocs = this.search(queryStr);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			result = this.addHits2List(scoreDocs);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("数据库查询sql出错！ sql : " + sql);
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
		return result;
	}
	
	private void createIndex(ResultSet rs) throws Exception {
		// 创建或打开索引
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		// 创建IndexWriter
		Analyzer analyzer = new IKAnalyzer(true);//标准分词器
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		//IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_5_5_0,analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, conf);
		// 遍历ResultSet创建索引
		int i =0;
		while (rs.next()) {
			// 创建document并添加field
			Document doc = new Document();
			doc.add(new TextField("id", rs.getString("id"), Field.Store.YES));
			doc.add(new TextField("realName", rs.getString("realName"),Field.Store.YES));
			System.out.println(rs.getString("realName"));
			doc.add(new TextField("email",rs.getString("email"),Field.Store.YES));
			doc.add(new TextField("address",rs.getString("address"),Field.Store.YES));
			// 将doc添加到索引中
			indexWriter.addDocument(doc);
			i++;
			System.out.println(i);
		}
		indexWriter.commit();
		indexWriter.close();
		directory.close();
	}

	private TopDocs search(String queryStr) throws Exception {
		// 创建或打开索引目录
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(directory);
		if (searcher == null) {
			searcher = new IndexSearcher(reader);
		}
		// 使用查询解析器创建Query
		//String[] fields = { "realName", "email", "address" };  
		//Query query = new MultiFieldQueryParser(fields, analyzer).parse(queryStr); 
		QueryParser parser = new QueryParser("realName",analyzer);
		Query query = parser.parse(queryStr);
		// 从索引中搜索得到排名前10的文档
		TopDocs topDocs = searcher.search(query, 10);
		return topDocs;
	}	
	
	private List<TeacherInfo> addHits2List(ScoreDoc[] scoreDocs) throws Exception {
		List<TeacherInfo> listBean = new ArrayList<TeacherInfo>();
		TeacherInfo bean = null;
		for (int i = 0; i < scoreDocs.length; i++) {
			int docId = scoreDocs[i].doc;
			Document doc = searcher.doc(docId);
			bean = new TeacherInfo();
			bean.setId(doc.get("id"));
			bean.setRealName(doc.get("realName"));
			bean.setEmail(doc.get("email"));
			bean.setAddress(doc.get("address"));
			listBean.add(bean);
		}
		return listBean;
	}

	
	public  List<String> resultList(String q) {
		SearchDBTable logic = new SearchDBTable();
		List<String> resultlist=new ArrayList<String>();
		try {
			Long startTime = System.currentTimeMillis();
			List<TeacherInfo> result = logic.getResult(q);
			int i = 0;
			for (TeacherInfo bean : result) {
				if (i == 10)
					break;
				System.out.println("bean.name: " + bean.getClass().getName()
						+ " , bean.id: " + bean.getId() + " , bean.username: "
						+ bean.getRealName());
				i++;
				resultlist.add("email"+bean.getEmail());
				resultlist.add("address"+bean.getAddress());
			}
			System.out.println("searchBean.result.size : " + result.size());
			Long endTime = System.currentTimeMillis();
			System.out.println("查询所花费的时间为：" + (endTime - startTime) / 1000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return resultlist;
	}
}
