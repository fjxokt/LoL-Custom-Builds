import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class LocaleString {

    private static LocaleString instance;
    private static ResourceBundle messages = null;
    public static String locale;
    
    public static LocaleString getInst() {
        if (instance == null) {
            instance = new LocaleString();
        }
        return instance;
    }
    
    public static String string(String str) {
    	if (messages == null) return "ERROR";
    	try {
    		return toUTFString(messages.getString(str.replaceAll(" ", "_")));
    	} catch (MissingResourceException e) {
    		Log.getInst().warning("Cannot find value for key '" + str + "'");
    		return "ERROR";
    	}
    }
    
    public static String toUTFString(String str) {
    	try {
			return new String(str.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return "ERROR";
    }
    
    public static String string(String str, String var) {
    	return string(str, new String[]{var});
    }

    public static String string(String str, String[] var) {
    	if (messages == null) return "ERROR";
    	String res = messages.getString(str.replaceAll(" ", "_"));
    	for (int i=0; i<var.length; i++)
    		res = res.replaceFirst("\\$" + i + "\\$", var[i]);
    	return toUTFString(res);
    }
    
    public void initLocale(String lang) {
    	try {
    		InputStream u = this.getClass().getResourceAsStream("language_"+lang+".properties");
    		if (u == null) {
    			u = this.getClass().getResourceAsStream("language_en_US.properties");
    		}
    		// messages = new PropertyResourceBundle(new BufferedReader(new InputStreamReader(u, "UTF-8")));
    		// doesn't work with java 1.5 - cf. http://sourceforge.net/projects/gpsmid/forums/forum/677687/topic/4063854
    		// have to use this method toUTFString(String str) to do the job
    		messages = new PropertyResourceBundle(u);
    		locale = lang;
    	} catch (Exception e) { e.printStackTrace(); }
    }
    
    public String getLocale() {
    	return locale;
    }

    private LocaleString() {}
}