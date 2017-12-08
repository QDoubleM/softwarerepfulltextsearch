package openreplicator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MysqlConnection {
    private static final Logger logger = LoggerFactory.getLogger(MysqlConnection.class);

    private static Connection conn;

    private static String host;
    private static int port;
    private static String user;
    private static String password;

    public static void setConnection(String hostArg, int portArg, String userArg, String passwordArg){
        try {
            if(conn == null || conn.isClosed()){
                Class.forName("com.mysql.jdbc.Driver");

                host = hostArg;
                port = portArg;
                user = userArg;
                password = passwordArg;

                conn = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/",user,password);
                logger.info("connected to mysql:{} : {}",user,password);
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(),e);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
    }

    public static Connection getConnection(){
        try {
            if(conn == null || conn.isClosed()){
                setConnection(host,port,user,password);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return conn;
    }

    /**
     * 获取Column信息
     * 
     * @return
     */
    public static Map<String,List<ColumnInfo>> getColumns(){
        Map<String,List<ColumnInfo>> cols = new HashMap<>();
        Connection conn = getConnection();

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet r = metaData.getCatalogs();
            String tableType[] = {"TABLE"};
            while(r.next()){
                String databaseName = r.getString("TABLE_CAT");
                ResultSet result = metaData.getTables(databaseName, null, null, tableType);
                while(result.next()){
                    String tableName = result.getString("TABLE_NAME");
                    String key = databaseName +"."+tableName;
                    ResultSet colSet = metaData.getColumns(databaseName, null, tableName, null);
                    cols.put(key, new ArrayList<ColumnInfo>());
                    while(colSet.next()){
                        ColumnInfo columnInfo = new ColumnInfo();
                        columnInfo.setName(colSet.getString("COLUMN_NAME"));
                        columnInfo.setType(colSet.getString("TYPE_NAME"));
                        cols.get(key).add(columnInfo);
                    }
                }
            }
        } catch (SQLException e) { 
            logger.error(e.getMessage(),e);
        }   
        return cols;
    }

    public static List<BinlogInfo> getBinlogInfo(){
        List<BinlogInfo> binlogList = new ArrayList<>();

        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            conn = getConnection();
            statement = conn.createStatement();
            resultSet = statement.executeQuery("show binary logs");
            while(resultSet.next()){
                BinlogInfo binlogInfo = new BinlogInfo();
                binlogInfo.setBinlogName(resultSet.getString("Log_name"));
                binlogInfo.setFileSize(resultSet.getLong("File_size"));
                binlogList.add(binlogInfo);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        } finally{
            try {
                if(resultSet != null)
                    resultSet.close();
                if(statement != null)
                    statement.close();
                if(conn != null)
                    conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
            }
        }

        return binlogList;
    }

    public static BinlogMasterStatus getBinlogMasterStatus(){
        BinlogMasterStatus binlogMasterStatus = new BinlogMasterStatus();

        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            conn = getConnection();
            statement = conn.createStatement();
            resultSet = statement.executeQuery("show master status");
            while(resultSet.next()){
                binlogMasterStatus.setBinlogName(resultSet.getString("File"));
                binlogMasterStatus.setPosition(resultSet.getLong("Position"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        } finally{
            try {
                if(resultSet != null)
                    resultSet.close();
                if(statement != null)
                    statement.close();
                if(conn != null)
                    conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
            }
        }

        return binlogMasterStatus;
    }

    /**
     * 获取open-replicator所连接的mysql服务器的serverid信息
     * @return
     */
    public static int getServerId(){
        int serverId=100001;
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            conn = getConnection();
            statement = conn.createStatement();
            resultSet = statement.executeQuery("show variables like 'server_id'");
            while(resultSet.next()){
                serverId = resultSet.getInt("Value");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        } finally{
            try {
                if(resultSet != null)
                    resultSet.close();
                if(statement != null)
                    statement.close();
                if(conn != null)
                    conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
            }
        }
        return serverId;
    }
}
