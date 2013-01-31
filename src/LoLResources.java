import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.json.simple.parser.ParseException;

public class LoLResources {
	// did we find the LolClient.exe
	File found = null;
	// input file (for key binding)
	static File inputFile = null;
	// folder where to save the Champ/file.ini files
	File baseCharsFolder;
	// folder where proprety files are stored
	//File propertiesFolder;
	// used to see if the game has been patched
	String lolVersion;
	// data file
	File data;
	// list of champions
	ArrayList<String> champList;
	// each line = list of rec items id, separated by ,
	ArrayList<String> recommandedItems;
	// each line = skill names, separated by ,
	ArrayList<String> champSkills;
	// items <Name, ID>
	Map<String,String> items;
	// filters list
	Map<String,String> filters;
	// used to give informations to the gui
	static LoLWin.Loading win;
	// loaded client
	LoLClient client;
	
	static final int IMAGE_NORMAL_SIZE = 60;
	static final int IMAGE_THUMB_SIZE = 45;
	static final int IMAGE_SMALL_SIZE = 30;

	private class ImgDownload {
		String url;
		String name;
		String extension;
		public ImgDownload(String u, String n, String ext) {
			url = u;
			name = n;
			extension = ext;
		}
	}
	
	List<ImgDownload> queue = new ArrayList<ImgDownload>();
	
	enum Type { CHAMPION, ITEM, SKILL };
	
	public static class Finder {
		static File found = null;
		static File inputFile = null;
		static File findFolder(String dir, String file, boolean findOnlyOne, boolean findChampFolder) {
			found = null;
			_findFolder(new File(dir), file, findOnlyOne, findChampFolder, 0);
			return found;
		}
		// return null if not, return the input.ini path if yes
		static File isLoLFolder(File f) {
			// windows
			if (f.getAbsolutePath().contains(File.separator + "RADS" + File.separator + "solutions" + File.separator)) {
				File res = new File(getParentFile(f, 7).getAbsolutePath() + "/Config/");
				if (res.exists()) return res;
				// PBE path
				return new File(getParentFile(f, 8).getAbsolutePath() + "/Config/");
			}
			// Wine 
			else if (f.getAbsolutePath().contains(File.separator + "rads" + File.separator + "solutions" + File.separator)) {
				return new File(getParentFile(f, 7).getAbsolutePath() + "/Config/");
			}
			// iLoL
			else if (f.getAbsolutePath().contains(File.separator + "files" + File.separator + "DATA")) {
				return new File(getParentFile(f, 3).getAbsolutePath() + "/League of Legends.app/Contents/Resources/drive_c/Config/");
			}
			// Ace client
			else if (f.getAbsolutePath().contains(File.separator + "game" + File.separator + "DATA")) {
				return new File(f.getParentFile().getAbsolutePath() + "/Config/");
			}
			// lol PH - garena
			else if (f.getAbsolutePath().contains(File.separator + "Game" + File.separator + "DATA")) {
				return new File(f.getParentFile().getAbsolutePath() + "/Config/");
			}
			return null;
		}
		// recursive function
		static void _findFolder(File dir, String file, boolean findOnlyOne, boolean findChampFolder, int maxRec) {
	    	if (found != null) return;
	    	if (maxRec > 15) return;
			File[] files = dir.listFiles();
			if (files == null) return;
			for (File f : files) {
				//System.out.println(f);
				if (f.getName().equals(file)) {
					System.out.println("found: " + f.getAbsolutePath());
					if (findChampFolder) {
						inputFile = isLoLFolder(f);
						if (inputFile != null) {
							found = f;
						}
					}
					else {
						found = f;
					}
					// if we found one we stop
					if (findOnlyOne) {
						LoLResources.inputFile = inputFile;
						return;
					}

				}
				// avoid links to directories
				if (f.isDirectory() && !f.getName().contains(":"))
					_findFolder(f, file, findOnlyOne, findChampFolder, maxRec+1);
			}
			return;
		}
		public static File getParentFile(File f, int n) {
			File res = new File(f.getAbsolutePath());
			// for n = 1, return the parent folder
			while (n > 0) {
				res = res.getParentFile();
				n--;
			}
			return res;
		}
	}
	
	public LoLResources(LoLWin.Loading win) {
		champList = new ArrayList<String>();
		items = new HashMap<String,String>();
		filters = new HashMap<String,String>();
		recommandedItems = new ArrayList<String>();
		champSkills = new ArrayList<String>();
		LoLResources.win = win;
	}
	
	public File findDataFolder(String dir) {
		File res = Finder.findFolder(dir, "DATA", true, true);
		return res;
	}
	
	public static void outputOnLoadWin(String str, boolean debug) {
		if (win != null) win.setLabel(str);
		if (debug) System.out.println(str);
	}
	
	public boolean init() {
		return _init(false);
	}
	
	public boolean reset() {
		return _init(true);
	}
	
	private boolean _init(boolean force) {
		// create the images folder
		File dir = new File("images");
		boolean dirExists = dir.exists();
		dir.mkdir();
		
		data = new File(LoLWin.dataFile);
		clear();
		
		// if data is here
		if (data.exists() && dirExists && !force)
			loadData(data);

		// get item/champ data if not existing, or checking for new updates
		getItems();
		getChampions();
		
		// download pictures
		if (queue.size() > 0) {
			int s = queue.size();
			Log.getInst().info(s + " images files in download queue...");
			Iterator<ImgDownload> it = queue.iterator();
			while (it.hasNext()) {
				ImgDownload cur = it.next();
				boolean res;
				// items
				if (cur.extension.equals("png")) {
	        		res = getImage(cur.url, cur.name, "png", "images", 0, 0, true, true);
				}
				else if (cur.extension.equals("spell")) {
	        		res = getImage(cur.url, cur.name, "png", "images", IMAGE_NORMAL_SIZE, false);
				}
				// champions
				else {
					res = getImage(cur.url, cur.name, "jpg", "images", IMAGE_NORMAL_SIZE);
				}
				if (res) {
					it.remove();
				}
			}
			if (queue.size() > 0) {
				Log.getInst().warning((s - queue.size()) + " images downloaded, " + queue.size() + " images already existing !");
			}
			else {
				Log.getInst().info("All images have been downloaded !");
			}
		}
		
		// check for manual champions/items updates (when riot's website isn't up-to-date)
		checkManualUpdates();

		// save data
		saveData(data);
		
		return true;
	}
	
	public void checkManualUpdates() {
    	 URL site;
    	 // reading the update file
    	 try {
 			site = new URL(LoLWin.website + "/fix/assets.txt");
 	        URLConnection yc = site.openConnection();
 	        yc.setConnectTimeout(5000);
 	        yc.setReadTimeout(5000);
 	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
 	        IniFile file = new IniFile(in);
 	        
 	        // checking champions
 	        IniFile.IniSection champs = file.getSection("champions");
 	        if (champs != null) {
 	        	for (String champName : champs.keySet()) {
 	        		
 	        		if (champList.contains(champName)) {
 	        			continue;
 	        		}

 	        		String champUrl = champs.get(champName);
 	        		getImage(champUrl, champName, "jpg", "images", IMAGE_NORMAL_SIZE);
 	        		 	        		
 	        		IniFile.IniSection spellsSec = file.getSection(champName);
 	        		String recItems = spellsSec.get("rec_items");
 	        		
 	 	        	String spells = "";
 	        		for (int i=1; i<5; i++) {
 	        			String spellPic = spellsSec.get("spell" + i + "_pic");
 	        			String spellName = spellsSec.get("spell" + i + "_name");
 	        			String spellDesc = spellsSec.get("spell" + i + "_desc");
 	        			
 	        			getImage(spellPic, spellName, "png", "images", IMAGE_NORMAL_SIZE, false);
 	        			
 	        			spells +=  spellName + "#" + spellDesc + "§";
 	        		}
 	        		
 	        		spells = spells.substring(0, spells.length()-1);
 	        		
 	        		champSkills.add(spells);
 	        		recommandedItems.add(recItems);
 	        		champList.add(champName);
 	        	}
 	        }
 	        
 	        // checking items
 	        IniFile.IniSection itemsSec = file.getSection("items");
 	        if (itemsSec != null) {
 	        	for (String itemId : itemsSec.keySet()) {
 	        		// new items
 	        		if (items.get(itemId) == null) {
 	        			String[] data = itemsSec.get(itemId).split("#");
 	        			getImage(data[0], itemId, "png", "images", 0, 0, true, true);
 	        			items.put(itemId, data[1]);
 	        		}	
 	        	}
 	        }
 	        
 	        // checking recommended builds
 	        IniFile.IniSection recBuildsSec = file.getSection("recommended");
 	        if (recBuildsSec != null) {
 	        	boolean newBuilds = false;
 	        	IniFile f = new IniFile(LoLWin.recItemsFile);
 	        	IniFile.IniSection sec = f.getSection("recommended", true);
 	        	for (String champkey : recBuildsSec.keySet()) {
 	        		if (LoLCustomBuildsManager.getDefaultBuilds().get(champkey) == null) {
 	        			// create and add the build to the map
 	        			LoLCustomBuild build = new LoLCustomBuild(recBuildsSec.get(champkey));
 	        			LoLCustomBuildsManager.getDefaultBuilds().put(champkey, build);
 	        			// and add it to the recItems file
 	        			sec.put(champkey, recBuildsSec.get(champkey));
 	        			newBuilds = true;
 	        		}
 	        	}
 	        	// new rec builds added, save the file
 	        	if (newBuilds) {
 	        		f.save();
 	        	}
 	        }
 	        in.close();
    	 } catch (Exception e) {
    		 System.out.println("Error while manual update checking: " + e.getMessage());
    		 e.printStackTrace();
    	 }
	}
	
	// create the recItems.ini file from the folder where all the rec builds .json files are located
	public static void readRecommendedBuilds() {
		String path = "/Users/adrienforest/Desktop/recItems";
   	 	File folder = new File(path);
   	 	IniFile save = new IniFile();
   	 	IniFile.IniSection sec = save.createSection("recommended");
   	 	for (File f : folder.listFiles()) {
   	 		if (f.getName().endsWith(".json")) {
   	 			BufferedReader br = null;
   	 			try {
   	 				String sCurrentLine;
   	 				br = new BufferedReader(new FileReader(f));
   	 				while ((sCurrentLine = br.readLine()) != null) {
   	 					LoLCustomBuild b = null;
						try {
							b = new LoLCustomBuild(sCurrentLine);
							if (b.getMode() != null) {
	   	 						sec.put(b.getChampName().toLowerCase() + b.getMode().getTag(), sCurrentLine);
							}
						} catch (ParseException e) {
							Log.getInst().warning("Error when parsing build from file '" + f + "'");
						}
   	 					break;
   	 				}
   	 				br.close();
   	 			} catch (IOException e) {
   	 				e.printStackTrace();
   	 			} 
   	 		}
   	 	}
   	 	save.save(LoLWin.recItemsFile);
	}
	
	public boolean isClient(File dir) {
		found = findDataFolder(dir.toString());
		if (found != null) {
			System.out.println("File found : " + found);
		}
		return (found != null);
	}
	
	public int updateClient(LoLClient c) {
		File f = new File(c.getClientPath());
		int res = 0;
		if (!f.exists()) {
			Log.getInst().warning(c.getClientPath() + " doesn't exists anymore !");
			return 1;
		}
		f = new File(c.getClientDataPath());
		// data dir disapeared, new version of the game
		if (!f.exists()) {
			found = findDataFolder(c.getClientPath());
			Log.getInst().info(c.getClientDataPath() + " updated to " + found.getAbsolutePath());
			res = 2;
		}
		else {
			found = new File(c.getClientDataPath());
		}
		return res;
	}
	
	public boolean load(LoLClient c) {	
		client = c;
		found = new File(c.getClientDataPath());
		String data = new File(c.getClientInputPath()).getAbsolutePath();
				
		// creating /Champions folder
		File charFolder = new File(data + File.separator + "Champions");
		if (charFolder.mkdir()) {
			System.out.println("Folder /Champions created");
		}
		baseCharsFolder = charFolder;
		
		// create all champ folders
		for (String champ : champList) {
			champ = fixChampNames(champ);
			File champFolder = new File(charFolder + File.separator + champ + File.separator + "Recommended");
			if (champFolder.mkdirs()) {
				System.out.println("Folder /Champions/" + champ + "/Recommended created");
			}
		}
		
		return true;
	}
	
	// remove all "strange" chars from champs names
	// TODO: for champs with not maching folder name, modify stuff here (cf. Wukong)
	public static String fixChampNames(String name) {
		return name.replaceAll(" |\\.|'", "").replaceAll("Wukong", "MonkeyKing");
	}
	
	public static String getImagePath(Type type, String data) {
		return getImagePath(type, data, 0);
	}
	
	public static String getImagePath(Type type, String data, int thumb) {
		String thumbi = (thumb == 0) ? "" : (thumb == 1) ? "_thumb" : "_small";
		String res = null;
		switch (type) {
		case CHAMPION:
			res = "images" + File.separator + data + thumbi + ".jpg";
			break;
		case ITEM:
			res = "images" + File.separator + data + thumbi + ".png";
			break;
		case SKILL:
			res = "images" + File.separator + data + ".png";
			break;
		}
		return res;
	}
	
	public void clear() {
		champList.clear();
		recommandedItems.clear();
		champSkills.clear();
		items.clear();
		filters.clear();
	}
	
	public void getChampions() {
		outputOnLoadWin(LocaleString.string("Retrieving champions list and pictures") + "...", true);
		URL site;
		String url = Languages.getUrl("en_US") + "/champions";
		String link = null;
		try {
			site = new URL(url);
			HttpURLConnection yc = (HttpURLConnection) site.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/4.76");
	        yc.setConnectTimeout(5000);
	        yc.setReadTimeout(5000);
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        String champPicUrl = null;
	        while ((inputLine = in.readLine()) != null) {
	        	if (inputLine.contains("class=\"lol_champion\"")) {
	        		champPicUrl = inputLine.split("\"")[5];
	        		String[] s = inputLine.split("/");
	        		link = "/champions/" + s[2] + "/" + s[3].split("\"")[0];
	        	}
	        	else if (inputLine.contains("<div class=\"champion_name")) {
	        		String champ = inputLine.split(">")[1].split("<")[0];
	        		queue.add(new ImgDownload(champPicUrl, champ, "jpg"));
	        		// if we don't have this champ
	        		if (!champList.contains(champ))
		        		getChampion(link);
	        	}
	        	// we parsed everything we needed, we can stop reading
	        	else if (inputLine.contains("mode_view_list")) {
	        		break;
	        	}
	        }
	        in.close();
		} catch (Exception e) {
			Log.getInst().warning("Error while connecting to '" + url + "'");
			e.printStackTrace();
		}
	}
	
	public void getChampion(String champurl) {
		outputOnLoadWin(LocaleString.string("Reading from $0$", new String[]{champurl}) + "...", true);
		URL site;
		String url = Languages.getUrl("en_US") + champurl;
		try {
			site = new URL(url);
			HttpURLConnection yc = (HttpURLConnection) site.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/4.76");
	        yc.setConnectTimeout(5000);
	        yc.setReadTimeout(5000);
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        String champName = null, skillUrl = null;
	        String recItems = "";
	        String skills = "";
	        int cptSkills = 0;
	        while ((inputLine = in.readLine()) != null) {
	        	// champion name
	        	if (inputLine.contains("span class=\"champion_name\"")) {
	        		champName = inputLine.split(">")[1].split("<")[0];
	        		champList.add(champName);
	        	}
	        	// recommanded items
	        	else if (inputLine.contains("item_icon")) {
	        		recItems += "1234" + ",";
	        	}
	        	else if (inputLine.contains("ability_icon")) {
	        		skillUrl = inputLine.split("\"")[3];
	        	}
	        	else if (inputLine.contains("ability_name")) {
	        		String skillName = inputLine.split(">")[1].split("<")[0];
	        		String skillDesc = inputLine.split(">")[3].split("<")[0];
					// replace this char because not allowed in file name
					skillName = skillName.replace("/", "-").replace(":", "");
					// put skill picture in queue
	        		queue.add(new ImgDownload(Languages.getUrl("en_US") + skillUrl, skillName, "spell"));
	        		// save skill name
	        		skills += skillName + "#" + skillDesc;
	        		// if not last skill, add , separator
	        		if (cptSkills != 3) {
	        			skills += "§";
	        		}	        		
	        		cptSkills++;
	        		// if more than 4 skills, we finished parsing the page (skip passive skill)
	        		if (cptSkills == 4) {
	        			break;
	        		}
	        	}
	        }
	        if (recItems.isEmpty()) {
	        	recItems = "1234,1324,1324,1324,1324,1324,1234,1324,1324,1324,1324,13245";
	        }
    		recommandedItems.add(recItems.substring(0, recItems.length()-1));
    		champSkills.add(skills);
	        in.close();
		} catch (Exception e) {
			Log.getInst().warning("Error while connecting to '" + url + "'");
			e.printStackTrace();
		}
	}
	
	// can't work, no item id on their website
//	public void getMobafireItems() {
//		outputOnLoadWin(LocaleString.string("Retrieving items list and pictures") + "...", true);
//		URL site;
//		String url = "http://www.mobafire.com/league-of-legends/items";
//		ArrayList<String> itemsFilters = new ArrayList<String>();
//		try {
//			site = new URL(url);
//	        URLConnection yc = site.openConnection();
//	        yc.setConnectTimeout(5000);
//	        yc.setReadTimeout(5000);
//	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
//	        String inputLine;
//	        String itemUrl;
//	        while ((inputLine = in.readLine()) != null) {
//	        	// filters data
//	        	if (inputLine.contains("value=\"filter-category-")) {
//	        		String key = inputLine.split("\"")[1];
//	        		String val = inputLine.split(">")[1].split("<")[0];
//	        		filters.put(key, val);
//	        	}
//	        	// item page
//	        	else if (inputLine.contains("/league-of-legends/item/")) {
//	        		// item page
//	        		itemUrl = inputLine.split("\"")[1];
//	        		// get the filter for the item
//	        		String[] words = inputLine.split(" ");
//	        		String fwords = "";
//	        		for (String word : words) {
//	        			if (word.startsWith("filter-category")) {
//	        				fwords += word + " ";
//	        			}
//	        		}
//	        		itemsFilters.add(fwords);
//	        	}
//	        	else if (inputLine.contains("champ-name")) {
//	        		String itemName = inputLine.split(">")[1].split("<")[0];
//	        	}
//	        }
//		} catch (Exception e) {
//			Log.getInst().warning("Error while connecting to '" + url + "'");
//			e.printStackTrace();
//		}
//	}
	
	public void getItems() {
		outputOnLoadWin(LocaleString.string("Retrieving items list and pictures") + "...", true);
		URL site;
		String url = Languages.getUrl(LocaleString.getInst().getLocale()) + "/items";
		ArrayList<String> itemsFilters = new ArrayList<String>();
		try {
			site = new URL(url);
			HttpURLConnection yc = (HttpURLConnection) site.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/4.76");
	        yc.setConnectTimeout(5000);
	        yc.setReadTimeout(5000);
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        String id = null, name = null, desc = null;
	        int cpt = -1;
	        boolean newItem = false;
	        boolean itemsData = false;
	        List<String> prices = new ArrayList<String>();
	        while ((inputLine = in.readLine()) != null) {
	        	// getting filters data (first part of the parsing)
	        	if (!itemsData) {
		        	// correspondance filter_tag / text
		        	if (inputLine.contains("filter checkbx")) {
		        		String tag = inputLine.substring(inputLine.indexOf("value=\"")).split("\"")[1];
		        		String val = inputLine.split(">")[2].split("<")[0];
		        		filters.put(tag, val);
		        	}
		        	// filter tags for the item
		        	else if (inputLine.contains("<li class=\" filter_tag")) {
		        		itemsFilters.add(inputLine.split("\"")[1].trim());
		        	}
		        	// handle the case of no filter
		        	else if (inputLine.contains("<li class=\"\"")) {
		        		itemsFilters.add(" ");
		        	}
		        	// add item url to dl queue
		        	// TODO: obsolete
		        	/*else if (inputLine.contains("champion_icon")) {
		        		String picUrl = inputLine.split("\"")[1];
		        		String itemId = picUrl.substring(picUrl.length()-8, picUrl.length()-4);
	        			queue.add(new ImgDownload(picUrl, itemId, "png"));
		        	}*/
		        	else if (inputLine.contains("class=\"search_text")) {
		        		String itemId = inputLine.split("\"")[5];
		        		String picUrl = "http://ddragon.leagueoflegends.com/cdn/0.152.115/img/item/" + itemId + ".png";
	        			queue.add(new ImgDownload(picUrl, itemId, "png"));
		        	}
		        	// now we know the number of items, we can stop reading if no new item found
		        	else if (inputLine.contains("mode_view_list")) {
		        		if (items.size() == itemsFilters.size()) {
		        			break;
		        		}
		        		itemsData = true;
		        	}
	        	}
	        	// getting items data (second part of the parsing)
	        	else {
	        		if (inputLine.contains("data-rg-aux=\"query=name\"></p></span>")) {
	        			cpt++;
	        			id = inputLine.split("\"")[5];
	        			// new item found
		        		if (!items.containsKey(id)) {
		        			newItem = true;
		        		}
	        		}
//	        		else if (inputLine.contains("<stats>") || inputLine.contains("<consumable>") || inputLine.contains("<unique>")) {
//	        		}
//	        		if (inputLine.contains("style=\" position")) {
//	        			cpt++;
//		        		id = inputLine.split("/")[10].substring(0, 4);
//		        		// new item found
//		        		if (!items.containsKey(id)) {
//		        			newItem = true;
//		        		}
//		        	}
	        		else if (newItem && inputLine.contains("search_text\"> ")) {
	        			name = inputLine.split(">")[1].split("<")[0].trim();
	        			name = uppercaseFirstLetters(name);
	        			outputOnLoadWin(LocaleString.string("Reading item $0$", new String[]{name}) + "...", true);
	        		}
//		        	else if (newItem && inputLine.contains("item_detail_name")) {
//		        		name = inputLine.split(">")[1].split("<")[0];
//	        			outputOnLoadWin(LocaleString.string("Reading item $0$", new String[]{name}) + "...", true);
//		        	}
	        		else if (newItem && inputLine.contains("<stats>") || inputLine.contains("<consumable>") || inputLine.contains("<unique>")) {
//		        	else if (newItem && inputLine.contains("item_description")) {
		        		// remove tags from desc
		        		desc = removeTags(inputLine);
		        		
		        		// TODO: fix this ? or is it okay like that
		        		// format items specs: this code works except for a few items...
		        		String str[] = desc.split(" {3}");
		        		String regex = " {3}";
		        		if (str.length == 1) {
		        			str = desc.split(" {2}");
		        			regex = " {2}";
		        		}
		        		if (str.length > 1 || (str.length == 1 && str[0].charAt(0) == '+')) {
		        			int index = str[0].length();
		        			String stats[] = str[0].split("\\+");
		        			String statsDiv = "<div style=\"color: green;\">";
		        			for (String stat : stats) {
		        				if (!stat.trim().isEmpty()) {
		        					statsDiv += "+" + stat + "<br/>";
		        				}
		        			}
		        			statsDiv += "</div>";
		        			
		        			desc = statsDiv + desc.substring(index).replaceAll(regex, "<br/>");
		        		}
		        		else {
		        			desc = desc.replaceAll(" {3}", "<br/>");
		        		}
		        		
		        		//desc = desc.replaceAll(" {3}| {2}", "<br/>");
		        		// TODO: small fix because on the lol page the itemn with id 3172 is no longer available
		        		// until the page is updated i need this here so the old item is not added
		        		//if (!id.equals("3172") || (id.equals("3172") && name.equals("Zephyr"))) {
//		        		System.out.println(prices.size());
//		        		items.put(id, name + "$" + prices.get(cpt) + "$" + itemsFilters.get(cpt) + "$" + desc);
//		        		//}
//		        		newItem = false;
		        	}
	        		else if (newItem && inputLine.contains("span class=\"big")) {
	        			String price = inputLine.split(">")[4].split("<")[0];
	        			prices.add(price);
	        			
	        			// price is the last info we need before adding the item
		        		items.put(id, name + "$" + price + "$" + itemsFilters.get(cpt) + "$" + desc);
		        		newItem = false;
	        		}
	        	}
	        }
	        in.close();
		} catch (Exception e) {
			Log.getInst().warning("Error while connecting to '" + url + "'");
			e.printStackTrace();
		}
	}
	
	public static String uppercaseFirstLetters(String str) 
	{
	    boolean prevWasWhiteSp = true;
	    char[] chars = str.toCharArray();
	    for (int i = 0; i < chars.length; i++) {
	        if (Character.isLetter(chars[i])) {
	            if (prevWasWhiteSp) {
	                chars[i] = Character.toUpperCase(chars[i]);    
	            }
	            prevWasWhiteSp = false;
	        } else {
	            prevWasWhiteSp = Character.isWhitespace(chars[i]);
	        }
	    }
	    return new String(chars);
	}
	
	private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

	public static String removeTags(String string) {
	    if (string == null || string.length() == 0) {
	        return string;
	    }

	    Matcher m = REMOVE_TAGS.matcher(string);
	    return m.replaceAll("");
	}
	
	public static boolean getImage(String url, String name, String ext, String folder, int newSize) {
		return getImage(url, name, ext, folder, newSize, true);
	}
	
	public static boolean getImage(String url, String name, String ext, String folder, int newSize, boolean fthumb) {
		return getImage(url, name, ext, folder, newSize, newSize, fthumb, false);
	}
	
	public static boolean getImage(String url, String name, String ext, String folder, int sizeX, int sizeW, boolean fthumb, boolean fsmall) {
		File res = new File(folder + File.separator + name + "." + ext);
		// if file already exists, return
		if (res.exists()) {
			return false;
		}
		BufferedImage image = null;
		try {
			URL u = new URL(url);
			HttpURLConnection yc = (HttpURLConnection) u.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/4.76");
	        yc.setConnectTimeout(5000);
	        yc.setReadTimeout(5000);
			InputStream in = new BufferedInputStream(yc.getInputStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int n = 0;
			while (-1!=(n=in.read(buf))) {
			   out.write(buf, 0, n);
			}
			out.close();
			in.close();
			image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
			if (sizeX > 0) image = resize(image, sizeX, sizeW);
			ImageIO.write(image, ext, res);
	        outputOnLoadWin(LocaleString.string("File $0$ downloaded", new String[]{name+"."+ext}), true);
		} catch (Exception e) {
			Log.getInst().warning("Error downloading image from '" + url + "' : " + e.getMessage());
		}
		// create thumb
		if (fthumb) {
	 	   	BufferedImage newimg = resize(image, IMAGE_THUMB_SIZE, IMAGE_THUMB_SIZE);
	 	   	File thumb = new File(folder + File.separator + name + "_thumb." + ext);
	        try {
	            ImageIO.write(newimg, ext, thumb);
	        } catch(IOException e) {
				Log.getInst().warning("Error writing thumb image '" + thumb.getName() + "' : " + e.getMessage());
	        }
		}
		// create small
		if (fsmall) {
	 	   	BufferedImage newimg = resize(image, IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE);
	 	   	File thumb = new File(folder + File.separator + name + "_small." + ext);
	        try {
	            ImageIO.write(newimg, ext, thumb);
	        } catch(IOException e) {
				Log.getInst().warning("Error writing small image '" + thumb.getName() + "' : " + e.getMessage());
	        }
		}
		return true;
	}
	
	// resize a BufferedImage to the desired size
	private static BufferedImage resize(BufferedImage img, int newW, int newH) {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);  
        Graphics2D g = dimg.createGraphics();  
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
        g.dispose();  
        return dimg;  
    }  
	
	public void saveData(File file) {
		IniFile save = new IniFile();
		// saving champions / recommanded items
		save.createSection("champions");
		int i = 0;
		for (String cur : champList) {
			save.addValue("champions", cur, recommandedItems.get(i) + "$" + champSkills.get(i++));
		}
		// saving filters
		save.createSection("filters");
		Iterator<Map.Entry<String,String>> iter = filters.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
			save.addValue("filters", mEntry.getKey(), mEntry.getValue());
		}
		// saving items list
		save.createSection("items");
		iter = items.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
			save.addValue("items", mEntry.getKey(), mEntry.getValue());
		}
		save.save(file);
	}
	
	public void loadData(File file) {
		IniFile res = new IniFile(file);
		Iterator<Map.Entry<String,String>> iter;
		// loading champions / recommanded items
		champList.clear();
		IniFile.IniSection sec = res.getSection("champions");
		if (sec != null) {
			iter = sec.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
				champList.add(mEntry.getKey());
				String[] vals = mEntry.getValue().split("\\$");
				recommandedItems.add(vals[0]);
				champSkills.add(vals[1]);
			}
		}
		// loading filters
		filters.clear();
		sec = res.getSection("filters");
		if (sec != null) {
			iter = sec.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
				filters.put(mEntry.getKey(), mEntry.getValue());
			}
		}
		// loading items	
		items.clear();
		sec = res.getSection("items");
		if (sec != null) {
			iter = sec.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
				items.put(mEntry.getKey(), mEntry.getValue());
			}
		}
	}
	
	public String getBuildFileName(String champ, GameMode mode) {
		return "LCB_" + fixChampNames(champ).toLowerCase() + mode.getTag() + ".json";
	}
	
	public String getBuildFilePath(String champ, GameMode mode) {
		String file = getBuildFileName(champ, mode);
		return baseCharsFolder + File.separator + fixChampNames(champ) + File.separator + "Recommended" + File.separator + file;
	}

}
