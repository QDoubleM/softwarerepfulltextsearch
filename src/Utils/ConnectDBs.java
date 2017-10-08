package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectDBs {
	XmlAnalyzer xmlanalyzer = new XmlAnalyzer();
	public static Connection getConnection() throws ClassNotFoundException  
    {  
        Connection conn = null; 
        try {   
            JdbcConfig jdbcconfig = XmlAnalyzer.getConenectInfo();  
            Class.forName(jdbcconfig.getDriverName()); 
            conn = DriverManager.getConnection(jdbcconfig.getUrl(), jdbcconfig.getUserName(), jdbcconfig.getPassword());  
            System.out.println("连接成功!");  
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();
            System.out.println("连接失败!");
        }catch(SQLException e)  
        {  
            e.printStackTrace();  
        }            
        return conn;            
    }     
     
}
