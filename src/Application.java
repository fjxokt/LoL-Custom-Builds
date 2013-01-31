import java.net.URL;
import java.net.URLConnection;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;



public class Application {
	public static void main(String[] args) {
		
		try {
            // for windows users
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {}
    
		// for mac users
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", LoLWin.appName);
		
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		
		Log.getInst().info("Starting " + LoLWin.appFullName);
 	 
		// if there was an update, the app will be restarted
		LoLUpdater.applyUpdate();
		
		// log
		logStartup();
		
		// run the main win
		//javax.swing.SwingUtilities.invokeLater(new Runnable() {
			//public void run() {
				new LoLWin();
			//}
		//});
	}
	
	public static void logStartup() {
		try {
 			URL site = new URL(LoLWin.website + "/startup.php");
 	        URLConnection yc = site.openConnection();
 	        yc.setConnectTimeout(1000);
 	        yc.setReadTimeout(1000);
 	        yc.getInputStream().close();
		} catch (Exception e) {}
	}
}