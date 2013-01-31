import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LoLCustomBuild {
	private String buildName;
	private String champName;
	private GameMode mode;
	private boolean enabled;
	
	// BuildMap is = <category, List of items>
	private BuildMap catMap;
	public class BuildMap extends LinkedHashMap<String, List<BuildItem>> {
		
		private static final long serialVersionUID = 1L;
		
		public String getJSON() {
			StringBuilder sb = new StringBuilder();
			for (String key : keySet()) {
				sb.append("{").append(quotes("type")).append(":").append(quotes(key)).append(",");
				sb.append(quotes("items")).append(":[");
				for (BuildItem item : get(key)) {
					sb.append("{").append(quotes("id")).append(":").append(quotes(item.getId())).append(",");
					sb.append(quotes("count")).append(":").append(Integer.toString(item.getCount())).append("}");
					sb.append(",");
				}
				// remove last "," if there was at least one item in this category
				if (get(key).size() > 0) {
					sb.deleteCharAt(sb.length()-1);
				}
				sb.append("]}");
				sb.append(",");
			}
			// remove last "," if there was at least one category in this build
			if (keySet().size() > 0) {
				sb.deleteCharAt(sb.length()-1);
			}
			return sb.toString();
		}
	}
	
	public LoLCustomBuild() {
		catMap = new BuildMap();
		enabled = false;
	}
	
	public LoLCustomBuild copyBuild() {
		LoLCustomBuild build = new LoLCustomBuild(champName, mode, buildName);
		for (String cat : catMap.keySet()) {
			List<BuildItem> lst = new ArrayList<BuildItem>();
			for (BuildItem item : catMap.get(cat)) {
				lst.add(new BuildItem(item.getId(), item.getCount()));
			}
			build.getBuildMap().put(cat, lst);
		}
		build.setEnabled(enabled);
		return build;
	}
	
	public void updateBuild(LoLCustomBuild other, boolean keepBuildName) {
		// this will update everything from the build 'this' except champName and gameMode
		if (!keepBuildName) setBuildName(other.getBuildName());
		setEnabled(other.isEnabled());
		catMap.clear();
		for (String cat : other.getBuildMap().keySet()) {
			List<BuildItem> lst = new ArrayList<BuildItem>();
			for (BuildItem item : other.getBuildMap().get(cat)) {
				lst.add(new BuildItem(item.getId(), item.getCount()));
			}
			catMap.put(cat, lst);
		}
	}

	public LoLCustomBuild(String json) throws ParseException {
		this();
		
		JSONParser parser = new JSONParser();
		JSONObject obj;
		try {
			obj = (JSONObject)parser.parse(json);
		} catch (ParseException e) {
			Log.getInst().warning("Could not parse json string '" + json + "' (" + e + ")");
			throw e;
		}
		
		buildName = (String)obj.get("title");
		champName = (String)obj.get("champion");
		mode = GameMode.getModeFromModeAndMap((String)obj.get("mode"), (String)obj.get("map"));
		enabled = (Boolean)obj.get("priority");
		
		JSONArray array = (JSONArray)obj.get("blocks");
		for (Object section : array) {
			JSONObject sec = (JSONObject)section;
			String sectionName = (String)sec.get("type");
			for (Object oi : (JSONArray)sec.get("items")) {
				JSONObject item = (JSONObject)oi;
				addItem(sectionName, (String)item.get("id"), ((Long)item.get("count")).intValue());
			}
		}
	}
	
	public LoLCustomBuild(String champName, GameMode mode, String buildName) {
		this();
		this.champName = champName;
		this.mode = mode;
		this.buildName = buildName;
	}
	
	// can be used for global builds
	public LoLCustomBuild(GameMode mode) {
		this();
		this.mode = mode;
	}
	
	public void setBuildName(String name) {
		buildName = name;
	}
	
	public String getBuildName() {
		return buildName;
	}
	
	public String getChampName() {
		return champName;
	}

	public void setChampName(String champName) {
		this.champName = champName;
	}

	public GameMode getMode() {
		return mode;
	}

	public void setMode(GameMode mode) {
		this.mode = mode;
	}

	public BuildMap getBuildMap() {
		return catMap;
	}
	
	public void setBuildMap(BuildMap map) {
		catMap = map;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean state) {
		enabled = state;
	}
	
	public int getCost() {
		int total = 0;
		for (List<BuildItem> lst : catMap.values()) {
			for (BuildItem item : lst) {				
				total += item.getItemData().getPrice() * item.getCount();
			}
		}
		return total;
	}
	
	public int getItemsCount() {
		int total = 0;
		for (List<BuildItem> lst : catMap.values()) {
			total += lst.size();
		}
		return total;
	}
	
	public int getCategoryCount() {
		return catMap.size();
	}
	
	public void addCategory(String category, List<BuildItem> items) {
		catMap.put(category, items);
	}
	
	public List<BuildItem> addCategory(String category) {
		List<BuildItem> lst = catMap.get(category);
		if (lst == null) {
			lst = new ArrayList<BuildItem>();
			catMap.put(category, lst);
		}
		return lst;
	}
	
	public void renameCategory(String catName, String newName) {
		List<BuildItem> items = getCategory(catName);
		catMap.remove(catName);
		addCategory(newName, items);
	}
	
	public List<BuildItem> getCategory(String catName) {
		return catMap.get(catName);
	}
	
	public void removeCategory(String category) {
		catMap.remove(category);
	}
	
	public void addItem(String category, String itemId, int count) {
		List<BuildItem> lst = catMap.get(category);
		// if cat doesnt exist, create it
		if (lst == null) {
			lst = addCategory(category);
		}
				
		for (BuildItem item : lst) {
			// if item already present, just inc its count
			if (item.getId().equals(itemId)) {
				item.setCount(item.getCount() + count);
				return;
			}
		}
		// otherwise create new pair
		BuildItem newItem = new BuildItem(itemId, count);
		lst.add(newItem);
	}
	
	public void removeItem(String category, String itemId) {
		List<BuildItem> lst = catMap.get(category);
		if (lst != null) {
			Iterator<BuildItem> iterator = lst.iterator();
			while (iterator.hasNext()) {
				// if we find the desired item, remove it and return
				if (iterator.next().getId().equals(itemId)) {
					iterator.remove();
					return;
				}
			}
		}
	}
	
	public String getJSON() {
		return getJSON(champName, mode, buildName);
	}
	
	public String getJSON(String champion, GameMode mode, String buildName) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(quotes("champion")).append(":").append(quotes(champion)).append(",");
		sb.append(quotes("title")).append(":").append(quotes(buildName)).append(",");
		sb.append(quotes("priority")).append(":").append(enabled ? "true" : "false").append(",");
		sb.append(quotes("type")).append(":").append(quotes("lcb")).append(",");
		sb.append(quotes("map")).append(":").append(quotes(mode.getMapId())).append(",");
		sb.append(quotes("mode")).append(":").append(quotes(mode.getMode())).append(",");
		sb.append(quotes("blocks")).append(":[").append(catMap.getJSON()).append("]");
		sb.append("}");
		return sb.toString();
	}
	
	public void save(String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(getJSON());
			out.close();
			Log.getInst().info("Correctly saved build '" + buildName + "' in file '" + filename + "'");
		 } catch (Exception e) {
			Log.getInst().warning("Could not save build '" + buildName + "' in file '" + filename + "'");
		 	e.printStackTrace();
		 }
	}
	
	private String quotes(String str) {
		return "\"" + str + "\"";
	}
	
	public String toString() {
		return getJSON();
	}
}
