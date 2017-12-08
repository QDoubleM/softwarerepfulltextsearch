package openreplicator;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.google.code.or.OpenReplicator;

public class OpenReplicatorPlus extends OpenReplicator{
    private static final Logger logger = LoggerFactory.getLogger(OpenReplicatorPlus.class);
    private volatile boolean autoRestart = true;
    @Override
    public void stopQuietly(long timeout, TimeUnit unit){
        super.stopQuietly(timeout, unit);
        if(autoRestart){
            try {
                TimeUnit.SECONDS.sleep(10);
                logger.error("Restart OpenReplicator");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
