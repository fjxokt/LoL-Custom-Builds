import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.simple.parser.ParseException;


public class LoLCustomBuildsManager {

	private static LoLCustomBuildsManager instance;
	private static LoLResources lolSrc;
	private Map<GameMode, SortedMap<String, List<LoLCustomBuild>>> champBuildsMap;
	private GameMode mode;
	private ItemChooser chooser;
	private Filter filter = new Filter();
	private static Map<String, LoLCustomBuild> globalBuilds;
	private static Map<String, LoLCustomBuild> defaultBuilds;
	private static Map<String, Item> items = new HashMap<String, Item>();
	
	public class Filter {
		private String text;
		private int count;
		public Filter() {}
		public Filter(String str) {
			text = str;
		}
		public String getFilterValue() {
			return text;
		}
		public void setFilterValue(String filter) {
			if (filter != null && filter.isEmpty()) text = null;
			else {
				text = filter;
			}
		}
		public int getCount() {
			return count;
		}
		public void setCount(int c) {
			count = c;
		}
		public void resetCount() {
			count = 0;
		}
	}
	
	public static LoLCustomBuildsManager getInst() {
		if (instance == null) {
			instance = new LoLCustomBuildsManager();
		}
		else {
			if (instance.getResources() == null) {
				Log.getInst().severe("setResources() has to be called before anything else !");
				return null;
			}
		}
		return instance;
	}
	
	// has to be called before anything else !
	public void setResources(LoLResources res) {
		lolSrc = res;
		// load items
		for (String itemId : lolSrc.items.keySet()) {
			String[] data = lolSrc.items.get(itemId).split("\\$");
			Integer price = Integer.parseInt(data[1]);
			Item item = new Item(itemId, data[0], price, data[3], data[2]);
			items.put(itemId, item);
		}
		// and init manager resources
		_init();
	}
	
	public LoLResources getResources() {
		return lolSrc;
	}
	
	public GameMode getMode() {
		return mode;
	}

	public void setMode(GameMode mode) {
		this.mode = mode;
	}
	
	public ItemChooser getItemChooser(String champ) {
		chooser.filter(champ, getMode());
		chooser.filterList();
		return chooser;
	}

	// private constructor
	private LoLCustomBuildsManager() {}
	
	private void _init() {
		champBuildsMap = new HashMap<GameMode, SortedMap<String,List<LoLCustomBuild>>>();
		for (GameMode mode : GameMode.values()) {
			SortedMap<String, List<LoLCustomBuild>> smap = new TreeMap<String, List<LoLCustomBuild>>();
			champBuildsMap.put(mode, smap);
		}
		mode = GameMode.Classic;
		chooser = new ItemChooser(lolSrc);
	}
	
	/////////////////////////////////////////////
	// items
	/////////////////////////////////////////////
	
	public static Item getItem(String id) {
		Item res = items.get(id);
		if (res == null) {
			res = new Item(id, id, 0, "Item " + id + " is unknown", "");
		}
		return res;
	}
	
	public static Map<String, Item> getItems() {
		return items;
	}
	
	/////////////////////////////////////////////
	// global builds
	/////////////////////////////////////////////
	
	public static void importGlobalBuilds(String file) {
		loadGlobalBuilds(file, true);
	}
	
	public static void exportGlobalBuilds(String file) {
		saveGlobalBuilds(file);
	}
	
	public static Map<String,LoLCustomBuild> getGlobalBuilds() {
		if (globalBuilds == null) {
			loadGlobalBuilds();
		}
		return globalBuilds;
	}
	
	public static void loadGlobalBuilds() {
		globalBuilds = loadGlobalBuilds(LoLWin.globalBuildsFile, false);
	}
	
	public static Map<String,LoLCustomBuild> loadGlobalBuilds(String file, boolean addToList) {
		Map<String,LoLCustomBuild> res = new TreeMap<String,LoLCustomBuild>();
		IniFile f = new IniFile(new File(file));
		IniFile.IniSection s = f.getSection("builds");
		// no data, nothing to load
		if (s == null) return res;
		for (String key : s.keySet()) {
			String json = s.get(key);
			if (json != null) {
				try {
					LoLCustomBuild build = new LoLCustomBuild(json);
					if (addToList) {
						globalBuilds.put(build.getBuildName(), build);
					}
					res.put(key, build);
				} catch (ParseException e) {}
			}
		}
		// save build file if new glob builds added
		if (addToList) {
			saveGlobalBuilds();
		}
		return res;
	}
	
	public static void saveGlobalBuilds() {
		saveGlobalBuilds(LoLWin.globalBuildsFile);
	}
	
	public static void saveGlobalBuilds(String file) {
		// create and populate our inifile class
		IniFile f = new IniFile();
		f.createSection("builds");
		Set<String> keys = globalBuilds.keySet();
		for (String key : keys) {
			f.addValue("builds", key, globalBuilds.get(key).getJSON());
		}
		// and save it
		f.save(new File(file));
	}
	
	public static void addGlobalBuild(LoLCustomBuild b) {
		LoLCustomBuild clone = b.copyBuild();
		getGlobalBuilds().put(clone.getBuildName(), clone);
		saveGlobalBuilds();
	}
	
	public static void removeGlobalBuild(LoLCustomBuild b) {
		getGlobalBuilds().remove(b.getBuildName());
		saveGlobalBuilds();
	}
	
	/////////////////////////////////////////////
	// manager methods
	/////////////////////////////////////////////
	
	public void importBuilds(File file) {
		Log.getInst().info("Importing builds from file \"" + file.getAbsolutePath() + "\"...");
		IniFile load = new IniFile(file);
		for (GameMode mode : GameMode.values()) {
			IniFile.IniSection sec = load.getSection(mode.getName());
			if (sec == null) {
				continue;
			}
			for (String key : sec.keySet()) {
				try {
					LoLCustomBuild build = new LoLCustomBuild(sec.get(key));
					addBuild(build);
				} catch (ParseException e) {}
			}
		}
	}
	
	public void exportBuilds(String filename) {
		Log.getInst().info("Exporting builds to file \"" + filename + "\"...");
		IniFile save = new IniFile();
		// for each game mode
		for (GameMode mode : GameMode.values()) {
			SortedMap<String, List<LoLCustomBuild>> map = champBuildsMap.get(mode);
			IniFile.IniSection sec = save.createSection(mode.getName());
			int i = 0;
			// for each champ
			for (String champ : map.keySet()) {
				List<LoLCustomBuild> list = map.get(champ);
				// for each of his builds
				for (LoLCustomBuild build : list) {
					sec.put("" + i, build.getJSON());
					i++;
				}
			}
		}
		save.save(filename);
	}
	
	private Map<String,List<LoLCustomBuild>> getBuildsList(GameMode mode) {
		return champBuildsMap.get(mode);
	}
	
	public void clear() {
		// i do that because it is important to see if some champs get 0 builds
		for (GameMode mode : GameMode.values()) {
			SortedMap<String, List<LoLCustomBuild>> map = champBuildsMap.get(mode);
			for (String champ : map.keySet()) {
				map.get(champ).clear();
			}
		}
	}
	
	// create files in the lol file structure
	public void synchronize() {
		for (GameMode mode : GameMode.values()) {
			SortedMap<String, List<LoLCustomBuild>> map = champBuildsMap.get(mode);
			for (String champ : map.keySet()) {
				// get enabled build for this champ
				LoLCustomBuild build = getEnabledBuild(champ, mode);
				// get the correct filename for this champ and mode
				String filename = lolSrc.getBuildFileName(champ, mode);
				// if build is null, lets delete the file
				if (build == null) {
					File f = new File(filename);
					if (f.exists()) f.delete();
				}
				// otherwise lets save it
				else {
					build.save(filename);
				}
			}
		}
	}
	
	// init and load custom builds for active client
	public void init() {
		for (GameMode mode : GameMode.values()) {
			champBuildsMap.get(mode).clear();
		}
		LoLClient c = lolSrc.client;
		IniFile f = new IniFile(LoLWin.clientsBuildsFile);
		IniFile.IniSection sec = f.getSection(c.getClientPath());
		int nbBuilds = 0;
		if (sec != null) {
			for (; nbBuilds<sec.size(); nbBuilds++) {
				String json = sec.get("" + nbBuilds);
				try {
					addBuild(new LoLCustomBuild(json));
				} catch (ParseException e) {}
			}
		}
		Log.getInst().info("" + nbBuilds + " builds loaded for client '" + c.getClientPath() + "'");
	}
	
	
	public void saveBuilds() {
		LoLClient c = lolSrc.client;
		IniFile f = new IniFile(LoLWin.clientsBuildsFile);
		// get the current client section
		IniFile.IniSection sec = f.getSection(c.getClientPath(), true);
		// clear it
		sec.clear();
		int total = 0;
		for (GameMode mode : GameMode.values()) {
			Map<String, List<LoLCustomBuild>> map = getBuilds(mode);
			for (String champ : map.keySet()) {
				// get the path to the json file
				String path = lolSrc.getBuildFilePath(champ, mode);
				// remove any file that could exists
				new File(path).delete();
				// get list of champ build
				List<LoLCustomBuild> builds = map.get(champ);
				for (LoLCustomBuild b : builds) {
					// save the build in the ini file
					sec.put("" + total, b.getJSON());
					total++;
					// if the build is active, save it in the correct LoL client folder
					if (b.isEnabled()) {
						b.save(path);
					}
				}
			}
		}
		f.save();
		Log.getInst().info(total + " builds saved for client '" + c.getClientPath() + "'");
	}
	
	public static Map<String,LoLCustomBuild> getDefaultBuilds() {
		if (defaultBuilds == null) {
			defaultBuilds = new HashMap<String, LoLCustomBuild>();
			// get recItems file if doesnt exist
			if (!new File(LoLWin.recItemsFile).exists()) {
				try {
					URL newUpdaterJar = new URL(LoLWin.website + "/recItems.ini");
					ReadableByteChannel rbc = Channels.newChannel(newUpdaterJar.openStream());
					File curF = new File(LoLWin.recItemsFile);
					FileOutputStream fos = new FileOutputStream(curF);
					fos.getChannel().transferFrom(rbc, 0, 1 << 24);
					fos.close();
					Log.getInst().info("recommended items file downloaded into : " + curF.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// load rec items
			IniFile f = new IniFile(LoLWin.recItemsFile);
			IniFile.IniSection sec = f.getSection("recommended", true);
			for (String key : sec.keySet()) {
				try {
					LoLCustomBuild build = new LoLCustomBuild(sec.get(key));
					defaultBuilds.put(key, build);
				} catch (ParseException e) {}
			}
		}
		return defaultBuilds;
	}
	
	public static LoLCustomBuild getDefaultBuild(String champName, GameMode mode) {
		String key = LoLResources.fixChampNames(champName).toLowerCase() + mode.getTag();
		LoLCustomBuild build = getDefaultBuilds().get(key);
		// if no rec build found for champ/mode, create random build
		if (build == null) {
			build = new LoLCustomBuild(champName, mode, "Custom Build");
			build.addCategory("starting");
			build.addCategory("essential");
			build.addCategory("offensive");
			build.addCategory("defensive");
			getDefaultBuilds().put(key, build);
		}
		else {
			build.setChampName(champName);
		}
		
		return build;
	}
	
	public void addBuild(String champ, GameMode mode, String name) {
		LoLCustomBuild b = new LoLCustomBuild(champ, mode, name);
		addBuild(b);
	}
	
	public void addBuild(LoLCustomBuild build) {
		String champName = build.getChampName();
		GameMode mode = build.getMode();
		
		List<LoLCustomBuild> buildsList = null;
		// if the champ doesnt have custom builds yet
		if (!getBuildsList(mode).containsKey(champName)) {
			buildsList = new ArrayList<LoLCustomBuild>();
			getBuildsList(mode).put(champName, buildsList);
		}
		else {
			buildsList = getBuildsList(mode).get(champName);
		}
		
		// remove previous build with same name
		int i = 0;
		for (; i<buildsList.size(); i++) {
			LoLCustomBuild b = buildsList.get(i);
			if (b.getBuildName().equals(build.getBuildName())) {
				buildsList.remove(b);
				break;
			}
		}
		
		// add this new build
		buildsList.add(i, build);
		
		// return it
		//return build;
	}
	
	public List<LoLCustomBuild> getBuilds(String champ, GameMode mode) {
		List<LoLCustomBuild> lst = getBuildsList(mode).get(champ);
		if (lst == null) {
			lst = new ArrayList<LoLCustomBuild>();
		}
		return lst;
	}
	
	public Map<String, List<LoLCustomBuild>> getBuilds(GameMode mode) {
		return getBuildsList(mode);
	}
	
	public LoLCustomBuild getBuild(String champ, GameMode mode, String name) {
		// for the Default build name we return the official default build
		if (name.equals("Default")) {
			return getDefaultBuild(champ, mode);
		}
		// otherwise just fetch the build
		List<LoLCustomBuild> builds = getBuilds(champ, mode);
		for (LoLCustomBuild b : builds) {
			if (b.getBuildName().equals(name))
				return b;
		}
		return null;
	}
	
	public LoLCustomBuild getEnabledBuild(String champ, GameMode mode) {
		List<LoLCustomBuild> list = getBuildsList(mode).get(champ);
		for (LoLCustomBuild build : list) {
			if (build.isEnabled()) {
				return build;
			}
		}
		return null;
	}
	
	public Map<String, List<LoLCustomBuild>> getFilteredBuilds(GameMode mode) {
		if (filter.getFilterValue() == null) {
			// we put one because we don't want to compute this value, its not used anyway
			filter.setCount(1);
			return getBuilds(mode);
		}
		
		filter.resetCount();
		Map<String, List<LoLCustomBuild>> map = new TreeMap<String, List<LoLCustomBuild>>();
		
		int count = 0;
		Set<String> keySet = getBuilds(mode).keySet();
		for (String champ : keySet) {
			List<LoLCustomBuild> lst = getFilteredBuilds(champ, mode);
			if (lst.size() > 0) {
				map.put(champ, lst);
				count += filter.getCount();
			}
		}
		filter.setCount(count);
		
		return map;
	}
	
	public List<LoLCustomBuild> getFilteredBuilds(String champ, GameMode mode) {
		if (filter.getFilterValue() == null) {
			List<LoLCustomBuild> builds = getBuilds(champ, mode);
			filter.setCount(builds.size());
			return builds;
		}
		
		filter.resetCount();
		
		List<LoLCustomBuild> builds = new ArrayList<LoLCustomBuild>(getBuilds(champ, mode));
		Iterator<LoLCustomBuild> iterator = builds.iterator();
		while (iterator.hasNext()) {
			LoLCustomBuild b = iterator.next();
			if (!buildMatchFilter(b, filter.getFilterValue())) {
				iterator.remove();
			}
		}
		filter.setCount(builds.size());
		return builds;
	}
	
	public int removeBuild(String champ, GameMode mode, String name) {
		List<LoLCustomBuild> builds = getBuilds(champ, mode);
		for (LoLCustomBuild b : builds) {
			if (b.getBuildName().equals(name)) {
				builds.remove(b);
				break;
			}
		}
		return builds.size();
	}
	
	public void renameBuild(String champ, GameMode mode, String name, String newName) {
		if (name.equals("Default")) return;
		if (newName.equals("Default")) return;
		LoLCustomBuild b = getBuild(champ, mode, name);
		if (b != null) b.setBuildName(newName);
	}
	
	public int getNumberOfBuilds(String champ, GameMode mode) {
		List<LoLCustomBuild> lst = getBuilds(champ, mode);
		return (lst == null) ? 0 : lst.size();
	}
	
	public void disableActiveBuild(LoLCustomBuild build) {
		disableActiveBuild(build.getChampName(), build.getMode());
	}
	
	public void disableActiveBuild(String champ, GameMode mode) {
		List<LoLCustomBuild> builds = getBuilds(champ, mode);
		for (LoLCustomBuild b : builds) {
			if (b.isEnabled()) {
				b.setEnabled(false);
				break;
			}
		}
	}
	
	public void enableActiveBuild(LoLCustomBuild build) {
		enableActiveBuild(build.getChampName(), build.getMode(), build.getBuildName());
	}
	
	public void enableActiveBuild(String champ, GameMode mode, String buildName) {
		if (buildName.equals("Default")) {
			disableActiveBuild(champ, mode);
			return;
		}
		List<LoLCustomBuild> builds = getBuilds(champ, mode);
		
		for (LoLCustomBuild build : builds) {
			if (build.getBuildName().equals(buildName)) {
				build.setEnabled(true);
			}
			else {
				build.setEnabled(false);
			}
		}
	}
	
	public Filter getFilter() {
		return filter;
	}
	
	public boolean buildMatchFilter(LoLCustomBuild build, String filter) {
		if (filter == null) return true;
		boolean isAnd;
		String[] keys;
		// or
		if (filter.contains("-")) {
			isAnd = false;
			keys = filter.split("-");
		}
		else {
			isAnd = true;
			keys = filter.split(",");
		}
		boolean matchAll = true;
		for (String key : keys) {
			if (isAnd && !matchAll) return false;
			// for each cat
			for (String category : build.getBuildMap().keySet()) {
				// for each item in cat
				for (BuildItem item : build.getBuildMap().get(category)) {
					// we get item name
					String itemName = item.getItemData().getName();
					if (itemName.toLowerCase().contains(key.trim().toLowerCase())) {
						if (!isAnd) {
							return true;
						}
						else {
							matchAll = true;
							break;
						}
					}
					else {
						matchAll = false;
					}
				}
				if (matchAll) break;
			}
		}
		if (isAnd && matchAll) return true;
		return false;
	}
	
	public static boolean isBuildAllowed(String champ, GameMode mode, LoLCustomBuild b) {
		for (String cat : b.getBuildMap().keySet()) {
			List<BuildItem> list = b.getBuildMap().get(cat);
			for (BuildItem item : list) {
				if (!isItemAllowed(champ, mode, item.getId())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean isItemAllowed(String champ, GameMode mode, Item item) {
		return isItemAllowed(champ, mode, item.getId());
	}
	
	public static boolean isItemAllowed(String champ, GameMode mode, String item) {
		// check champ specific items
		for (String champion : GameMode.champSpecificItems.keySet()) {
			if (!champ.equals(champion) && GameMode.champSpecificItems.get(champion).contains(item)) {
				return false;
			}
		}
		
		// check for map restricted items
		// see http://s3.microtony.com/maps.js
		return !GameMode.mapRestrictedItems.get(mode).contains(item);
		
		/*
		if (!champ.equals("Viktor") && viktorItems.contains(item)) {
			return false;
		}
		if (!champ.equals("Rengar") && rengarItems.contains(item)) {
			return false;
		}
		// dominion's items
		final List<String> dominionItems = Arrays.asList(new String[]{"1062","1063","3180","3181","3183","3184","3185","3186","3187","2047"});
		// classic's specific items
		final List<String> classicItems = Arrays.asList(new String[]{"1055","1056","3072","3102","3126","3106","3154","3041","3141","3138","2044","2043","2042","2037","2038","2039","3083"});
		// items removed from classic mode
		final List<String> provingItems = Arrays.asList(new String[]{"2044","2043","2042","1062","1063","3180","3187","3185","3126","3141","3072","3138","3041","3154","3026"});
		// items for new Twisted Treeline
		final List<String> twistedItems = Arrays.asList(new String[]{"3181","3188","3153","3159","2048","2040","3104","3084","3122", "3090"});
		if (!champ.equals("Viktor") && viktorItems.contains(item)) {
			return false;
		}
		if (item.equals("3166") && !champ.equals("Rengar")) {
			return false;
		}
		if (mode.equals(GameMode.Classic)) {
			if (dominionItems.contains(item)) return false;
			if (twistedItems.contains(item)) return false;
		}
		else if (mode.equals(GameMode.Dominion)) {
			// sanguin blade is for both dominion and twisted
			if (item.equals("3181")) return true;
			if (classicItems.contains(item)) return false;
			if (twistedItems.contains(item)) return false;
		}
		else if (mode.equals(GameMode.Proving_Grounds)) {
			if (provingItems.contains(item)) return false;
			if (twistedItems.contains(item)) return false;
		}
		else if (mode.equals(GameMode.Twisted_Treeline)) {
			// sanguin blade is for both dominion and twisted
			if (item.equals("3181")) return true;
			if (dominionItems.contains(item)) return false;
			if (classicItems.contains(item)) return false;
		}
		return true;
		*/
	}
	
	public static String createToolTip(Item item, String title) {
		if (item != null) {
			String t = (title != null) ? "<i>(" + title + ")</i><br/>" : "";
			String gold = (item.getPrice() > 0) ?  " <span style=\"color:#F1B82D\">(" + item.getPrice() + "g)</span>" : "";
	 	   	return "<html><div style=\"width: 200px\">"+t+"<b>"+item.getName()+gold+"</b><br/>"+item.getDesc()+"</div></html>";
		}
		return "error loading item " + item;
	}
	
	public static String createToolTip(BuildItem item, String title) {
		return createToolTip(item.getItemData(), title);
	}
	
}
