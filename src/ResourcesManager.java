import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


public class ResourcesManager {

    private static ResourcesManager instance;
    private HashMap<String,ImageIcon> map;
    private HashMap<String,Object> cellMap;
    private HashMap<String,CustomIcon> ciconMap;
    
    public static ResourcesManager getInst() {
        if (instance == null) {
            instance = new ResourcesManager();
        }
        return instance;
    }
    
    public ImageIcon getIcon(String res) {
    	return getIcon(res, false);
    }
    
    public ImageIcon getIcon(String res, boolean isResource) {
    	ImageIcon icon = map.get(res);
    	if (icon == null) {
    		icon = (isResource) ? new ImageIcon(this.getClass().getResource(res)) : new ImageIcon(res);
			map.put(res, icon);
    	}
    	return icon;
    }
    
    public CustomIcon getCustomIcon(String name) {
    	return ciconMap.get(name);
    }
    
    public CustomIcon setCustomIcon(String name, CustomIcon icon) {
    	ciconMap.put(name, icon);
    	return icon;
    }
    
    public BufferedImage getImage(String res) {
    	BufferedImage img = (BufferedImage) cellMap.get(res);
    	if (img == null) {
    		try {
				img = ImageIO.read(new File(res));
			} catch (IOException e) {
				Log.getInst().warning("Exception reading file '" + res + "' : " + e.getMessage());
			}
			cellMap.put(res, img);
    	}
    	return img;
    }
    
    public Object getCell(int col) {
    	Object res = cellMap.get("" + col);
    	return res;
    }
    
    public Object putCell(int col, Object o) {
    	cellMap.put("" + col, o);
    	return o;
    }

    private ResourcesManager() {
    	map = new HashMap<String,ImageIcon>();
    	cellMap = new HashMap<String,Object>();
    	ciconMap = new HashMap<String,CustomIcon>();
    }
}