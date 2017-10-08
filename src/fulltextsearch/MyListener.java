package fulltextsearch;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import Utils.FileMonitor;

public class MyListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		FileMonitor fileMonitor = new FileMonitor();
		try {
			fileMonitor.FileListenter();
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}

}
