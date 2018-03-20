package openreplicator;

import indexcreater.IndexDB;
import indexcreater.IndexFileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import Utils.XmlAnalyzer;

import com.google.code.or.binlog.BinlogEventListener;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.QueryEvent;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.google.code.or.binlog.impl.event.XidEvent;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.util.MySQLConstants;

public class InstanceListener implements BinlogEventListener{
    private static final Logger logger = LoggerFactory.getLogger(InstanceListener.class);

    private IndexWriter writer;
	private IndexWriterConfig indexWriterConfig;
	private Analyzer smartAnalyzer;
	private TrackingIndexWriter trackingIndexWriter;
	private String path = "E:\\lucene\\fileindex";
	IndexFileHandler indexFile = new IndexFileHandler();
    private IndexDB indexDB = new IndexDB();
    @Override
	public void onEvents(BinlogEventV4 binogEvent) {
		if (binogEvent == null) {
			logger.error("binlog event is null");
			return;
		}

		int eventType = binogEvent.getHeader().getEventType();
		switch (eventType) {
		case MySQLConstants.FORMAT_DESCRIPTION_EVENT: {
			logger.trace("FORMAT_DESCRIPTION_EVENT");
			break;
		}
		case MySQLConstants.TABLE_MAP_EVENT:// 每次ROW_EVENT前都伴随一个TABLE_MAP_EVENT事件，保存一些表信息，如tableId,
											// tableName, databaseName,
											// 而ROW_EVENT只有tableId
		{
			TableMapEvent tableMapEvent = (TableMapEvent) binogEvent;
			TableInfoKeeper.saveTableIdMap(tableMapEvent);
			logger.trace("TABLE_MAP_EVENT:tableId:{}",
					tableMapEvent.getTableId());
			break;
		}
		case MySQLConstants.DELETE_ROWS_EVENT: {
			DeleteRowsEvent deleteEvent = (DeleteRowsEvent) binogEvent;
			long tableId = deleteEvent.getTableId();
			logger.trace("DELETE_ROW_EVENT:tableId:{}", tableId);

			TableInfo tableInfo = TableInfoKeeper.getTableInfo(tableId);
			String databaseName = tableInfo.getDatabaseName();
			String tableName = tableInfo.getTableName();
			List<Row> rows = deleteEvent.getRows();
			for (Row row : rows) {
				List<Column> before = row.getColumns();
				Map<String, String> beforeMap = getMap(before, databaseName,
						tableName);
				if (beforeMap != null && beforeMap.size() > 0) {
					CDCEvent cdcEvent = new CDCEvent(deleteEvent, databaseName,
							tableName);
					cdcEvent.setBefore(beforeMap);
					CDCEventManager.queue.addLast(cdcEvent);
					logger.info("cdcEvent:{}", cdcEvent);
				}
			}
			break;
		}
		case MySQLConstants.UPDATE_ROWS_EVENT: {
			UpdateRowsEvent updateEvent = (UpdateRowsEvent) binogEvent;
			long tableId = updateEvent.getTableId();
			logger.info("UPDATE_ROWS_EVENT:tableId:{}", tableId);

			TableInfo tableInfo = TableInfoKeeper.getTableInfo(tableId);
			String databaseName = tableInfo.getDatabaseName();
			String tableName = tableInfo.getTableName();
			String primaryKey = getPrimaryKey(tableName);// 表主键
			try {// 获取配置文件中的column
				Element tableElement = XmlAnalyzer.getElementByName(tableName);
				System.out.println("+++++"+ XmlAnalyzer.getIndexColumns(tableElement));
				List<String> indexColumns = Arrays.asList(XmlAnalyzer.getIndexColumns(tableElement).split(","));
				List<String> storeColumns = XmlAnalyzer.getStoreColumns(tableElement);
				
				List<Pair<Row>> rows = updateEvent.getRows();
				for (Pair<Row> p : rows) {
					List<Column> colsBefore = p.getBefore().getColumns();
					List<Column> colsAfter = p.getAfter().getColumns();

					Map<String, String> beforeMap = getMap(colsBefore,
							databaseName, tableName);
					Map<String, String> afterMap = getMap(colsAfter,
							databaseName, tableName);
					if (beforeMap != null && afterMap != null
							&& beforeMap.size() > 0 && afterMap.size() > 0) {
						CDCEvent cdcEvent = new CDCEvent(updateEvent,
								databaseName, tableName);
						cdcEvent.setBefore(beforeMap);
						cdcEvent.setAfter(afterMap);
						CDCEventManager.queue.addLast(cdcEvent);
						logger.info("cdcEvent:{}", cdcEvent);
					}
					//删除旧document
					
					List<ColumnInfo> indexColumn = getIndexedColumns(colsAfter, databaseName, tableName,storeColumns);
					indexDB.incrementalUpdate(indexColumn, tableElement);
				}
			} catch (DocumentException | IOException e) {
				e.printStackTrace();
			}
			break;
		}
		case MySQLConstants.WRITE_ROWS_EVENT: {
			WriteRowsEvent writeEvent = (WriteRowsEvent) binogEvent;
			long tableId = writeEvent.getTableId();
			logger.trace("WRITE_ROWS_EVENT:tableId:{}", tableId);

			TableInfo tableInfo = TableInfoKeeper.getTableInfo(tableId);
			String databaseName = tableInfo.getDatabaseName();
			String tableName = tableInfo.getTableName();

			List<Row> rows = writeEvent.getRows();

			for (Row row : rows) {
				List<Column> after = row.getColumns();
				Map<String, String> afterMap = getMap(after, databaseName,
						tableName);
				if (afterMap != null && afterMap.size() > 0) {
					CDCEvent cdcEvent = new CDCEvent(writeEvent, databaseName,
							tableName);
					cdcEvent.setAfter(afterMap);
					CDCEventManager.queue.addLast(cdcEvent);
					logger.info("cdcEvent:{}", cdcEvent);
				}
			}
			break;
		}
		case MySQLConstants.QUERY_EVENT: {
			QueryEvent queryEvent = (QueryEvent) binogEvent;
			TableInfo tableInfo = createTableInfo(queryEvent);
			if (tableInfo == null)
				break;
			String databaseName = tableInfo.getDatabaseName();
			String tableName = tableInfo.getTableName();
			logger.trace("QUERY_EVENT:databaseName:{},tableName:{}",
					databaseName, tableName);

			CDCEvent cdcEvent = new CDCEvent(queryEvent, databaseName,
					tableName);
			cdcEvent.setIsDdl(true);
			cdcEvent.setSql(queryEvent.getSql().toString());

			CDCEventManager.queue.addLast(cdcEvent);
			logger.info("cdcEvent:{}", cdcEvent);

			break;
		}
		case MySQLConstants.XID_EVENT: {
			XidEvent xe = (XidEvent) binogEvent;
			logger.trace("XID_EVENT: xid:{}", xe.getXid());
			break;
		}
		default: {
			logger.trace("DEFAULT:{}", eventType);
			break;
		}
		}
	}

    /**
     * ROW_EVENT中是没有Column信息的，需要通过MysqlConnection（下面会讲到）的方式读取列名信息，
     * 然后跟取回的List<Column>进行映射。
     * 
     * @param cols
     * @param databaseName
     * @param tableName
     * @return
     */
    private Map<String,String> getMap(List<Column> cols, String databaseName, String tableName){
        Map<String,String> map = new HashMap<>();
        if(cols == null || cols.size()==0){
            return null;
        }

        String fullName = databaseName+"."+tableName;
        List<ColumnInfo> columnInfoList = TableInfoKeeper.getColumns(fullName);
        if(columnInfoList == null)
            return null;
        if(columnInfoList.size() != cols.size()){
            TableInfoKeeper.refreshColumnsMap();
            if(columnInfoList.size() != cols.size())
            {
                logger.warn("columnInfoList.size is not equal to cols.");
                return null;
            }
        }
        for(int i=0;i<columnInfoList.size(); i++){
        	System.out.println("columnName:"+columnInfoList.get(i).getName()+" columnType:"+columnInfoList.get(i).getType()+"  "+cols.get(i).toString());
            if(cols.get(i).getValue()==null){
            	map.put(columnInfoList.get(i).getName(),"");
            }
            else{
            	map.put(columnInfoList.get(i).getName(), cols.get(i).toString());
            }
        }
        return map;
    }

    /**
     * 从sql中提取Table信息，因为QUERY_EVENT是对应DATABASE这一级别的，不像ROW_EVENT是对应TABLE这一级别的，
     * 所以需要通过从sql中提取TABLE信息,封装到TableInfo对象中
     * 
     * @param queryEvent
     * @return
     */ 
    private TableInfo createTableInfo(QueryEvent queryEvent){
        String sql = queryEvent.getSql().toString().toLowerCase();

        TableInfo ti = new TableInfo();
        String databaseName = queryEvent.getDatabaseName().toString();
        String tableName = null;
        if(checkFlag(sql,"table")){
            tableName = getTableName(sql,"table");
        } else if(checkFlag(sql,"truncate")){
            tableName = getTableName(sql,"truncate");
        } else{
            return null;
        }
        ti.setDatabaseName(databaseName);
        ti.setTableName(tableName);
        ti.setFullName(databaseName+"."+tableName);
        return ti;
    }

    private boolean checkFlag(String sql, String flag){
        String[] ss = sql.split(" ");
        for(String s:ss){
            if(s.equals(flag)){
                return true;
            }
        }
        return false;
    }

    private String getTableName(String sql, String flag){
        String[] ss = sql.split("\\.");
        String tName = null;
        if (ss.length > 1) {
            String[] strs = ss[1].split(" ");
            tName = strs[0];
        } else {
            String[] strs = sql.split(" ");
            boolean start = false;
            for (String s : strs) {
                if (s.indexOf(flag) >= 0) {
                    start = true;
                    continue;
                }
                if (start && !s.isEmpty()) {
                    tName = s;
                    break;
                }
            }
        }
        tName.replaceAll("`", "").replaceAll(";", "");
        int index = tName.indexOf('(');
        if(index>0){
            tName = tName.substring(0, index);
        }
        return tName;
    }
    
    private String getPrimaryKey(String tableName){
    	XmlAnalyzer xmlAnalyzer = new XmlAnalyzer();
    	String primaryKey = null;
        try {
        	List<Element> tableList = xmlAnalyzer.getTableElement(xmlAnalyzer.getXmlPath());
        	for(Element tableElement : tableList){
        		if(tableName.equals(xmlAnalyzer.tableName(tableElement))){
        			primaryKey = xmlAnalyzer.getPrimaryKey(tableElement);
        		}
        	}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    	return primaryKey;
    }
    
    private List<ColumnInfo> getIndexedColumns(List<Column> cols, String databaseName, String tableName,List<String> indexColumns){
    	String fullName = databaseName+"."+tableName;
        List<ColumnInfo> columnInfoList = TableInfoKeeper.getColumns(fullName);
        List<ColumnInfo> indexColumnEntity = new ArrayList<>();
        if(columnInfoList == null)
            return null;
        else{
        	for(String columnName:indexColumns){
        		for(ColumnInfo columnInfo:columnInfoList){
        			if(columnName.equals(columnInfo.getName())){
        				indexColumnEntity.add(columnInfo);
        			}
        		}
        	}
        	return indexColumnEntity;
        }  	
    }
    
    private TrackingIndexWriter getindexWriter(String path) throws IOException, InterruptedException{
		Map<String,IndexWriter> indexWriterMap = new HashMap<String,IndexWriter>();
		smartAnalyzer = new SmartChineseAnalyzer();
		indexWriterConfig = new IndexWriterConfig(smartAnalyzer);
		Directory directory = FSDirectory.open(Paths.get(path));
		if(! indexWriterMap.containsKey(path)){
			while (IndexWriter.isLocked(directory)){
				Thread.sleep(1000);
			}
			writer = new IndexWriter(directory, indexWriterConfig);
			indexWriterMap.put(path, writer);
		}else{
			writer = indexWriterMap.get(path);
		}
		trackingIndexWriter = new TrackingIndexWriter(writer);
		return trackingIndexWriter;
	}
	/**
	 * 增加索引
	 */
	private void addDocument(File file){
		try {
			trackingIndexWriter = getindexWriter(path);
			indexFile.indexSingleFile(new Document(), trackingIndexWriter, file);			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 删除索引
	 */
	public void deleteDocument(File file){
		Term term= new Term("filepath", file.getPath());
		Query query = new TermQuery(term);
		try {
			trackingIndexWriter = getindexWriter(path);
			trackingIndexWriter.deleteDocuments(query);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 更新索引
	 */
	public void updateDocument(File file){
		Document document = new Document();
		try {
			trackingIndexWriter = getindexWriter(path);
			document = indexFile.getDocument(file);
			trackingIndexWriter.updateDocument(new Term("filepath", file.getPath()), document);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void commit(){
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void close(){
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
