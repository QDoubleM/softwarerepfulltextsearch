package openreplicator;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.google.code.or.OpenReplicator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OpenReplicatorTest {
	    private static final Logger logger = LoggerFactory.getLogger(OpenReplicatorTest.class);
	    private static final String host = "localhost";
	    private static final int port = 3306;
	    private static final String user = "root";
	    private static final String password = "root";

	    public static void main(String[] args){
	    	OpenReplicator openReplicator = new OpenReplicatorPlus();
	    	openReplicator.setUser(user);
	    	openReplicator.setPassword(password);
	    	openReplicator.setHost(host);
	    	openReplicator.setPort(port);
	        MysqlConnection.setConnection(host, port, user, password);

	        openReplicator.setServerId(MysqlConnection.getServerId());

	        BinlogMasterStatus bms = MysqlConnection.getBinlogMasterStatus();
	        openReplicator.setBinlogFileName(bms.getBinlogName());
	        openReplicator.setBinlogFileName("mysql-bin.000001");
	        openReplicator.setBinlogPosition(318);
	        openReplicator.setBinlogEventListener(new InstanceListener());
	        try {
	        	openReplicator.start();
	        } catch (Exception e) {
	            logger.error(e.getMessage(),e);
	        }

	        Thread thread = new Thread(new PrintCDCEvent());
	        thread.start();
	    }

	    public static class PrintCDCEvent implements Runnable{
	        @Override
	        public void run() {
	            while(true){
	                if(CDCEventManager.queue.isEmpty() == false)
	                {
	                    //CDCEvent ce = CDCEventManager.queue.pollFirst();
	                    //CDCEventManager.queue.pollLast();
	                    Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	                    String pretty = gson.toJson(CDCEventManager.queue.pollLast()).toString();
	                    //System.out.println("prettyStr1:"+pretty); 
	                    JsonObject returnData = new JsonParser().parse(pretty).getAsJsonObject();
	                    //System.out.println("++++++"+returnData.get("before"));
	                }
	                else{
	                    try {
	                        TimeUnit.SECONDS.sleep(1);
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        }       
	    }
}
