import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum GameMode {
	
	Classic(0, "CLASSIC", "SR", "Classic", "1"),
	Dominion(1, "ODIN", "DM", "Dominion", "8"),
	Proving_Grounds(2, "ARAM", "PG", "Proving Grounds", "3"),
	Twisted_Treeline(3, "CLASSIC", "TT", "Twisted Treeline", "10");
	
	private int modeId;
	private String mode;
	private String gameName;
	private String tag;
	private String mapId;
	
	public static final Map<GameMode,List<String>> mapRestrictedItems = new HashMap<GameMode,List<String>>() {
		private static final long serialVersionUID = 1L;
		{
			put(GameMode.Classic, Arrays.asList(new String[]{"1062","1063","3181","3182","3184","3185","3186","3187","2047","1061","2048","3122","3104","3188","3084","3159","3132","3180","2040","3090"}));
			put(GameMode.Dominion, Arrays.asList(new String[]{"2037","2038","2039","2043","2044","3041","3072","3102","3138","3141","3154","1055","1056","3106","3126","2042","3083","3084","3132","2048","3153","3122","3157","3104","3089","3056","3206","3207","3209","1039","2045","2049","3151","3255","3265","3275","1080","2040","3250","3255","3260","3265","3270","3275","3280"}));
			put(GameMode.Twisted_Treeline, Arrays.asList(new String[]{"1062","1063","2043","2044","2042","2038","2039","2037","3186","3126","3106","3185","3184","1039","3206","3207","3209","3026","3089","3157","3138","3141","3041","3154","3151","3075","3255","3265","3275","3072","3083","3056","2049","2045","3176","3132","3107","3122","3104","1080","3180"}));
			put(GameMode.Proving_Grounds, Arrays.asList(new String[]{"3026","3041","3141","3138","1062","1063","3072","3185","2042","3106","3126","3154","2044","2043","3104","3188","3084","3159","2048","3153","3122","2045","2049","3056","3206","3207","3209","1039","3255","3265","3275","2041","3132","3180","2040","1080","3090","3250","3255","3260","3265","3270","3275","3280"}));
		}
	};
	
	public static final Map<String,List<String>> champSpecificItems = new HashMap<String,List<String>>() {
		private static final long serialVersionUID = 1L;
		{
			put("Viktor", Arrays.asList(new String[]{"3196","3197","3198","3200"}));
			put("Rengar", Arrays.asList(new String[]{"3166"}));
		}
	};
	
	GameMode(int type, String mode, String gt, String gameN, String mapId) {
		this.modeId = type;
		this.mode = mode;
		this.tag = gt;
		this.gameName = gameN;
		this.mapId = mapId;
	}
	
	public static GameMode getModeFromModeAndMap(String mode, String map) {
		if (mode.equals("CLASSIC")) {
			if (map.equals("1")) {
				return GameMode.Classic;
			}
			if (map.equals("10")) {
				return GameMode.Twisted_Treeline;
			}
			return null;
		}
		if (mode.equals("ODIN")) {
			return GameMode.Dominion;
		}
		if (mode.equals("ARAM")) {
			return GameMode.Proving_Grounds;
		}
		return null;
	}
	
	// not correct if mode = "TUTORIAL" for instance
	/*
	public static GameMode getModeFromMapId(String str) {
		if (str.equals("1")) {
			return GameMode.Classic;
		}
		if (str.equals("8")) {
			return GameMode.Dominion;
		}
		if (str.equals("3")) {
			return GameMode.Proving_Grounds;
		}
		if (str.equals("10")) {
			return GameMode.Twisted_Treeline;
		}
		return null;
	}
	*/
	
	public int getModeId() {
		return modeId;
	}
	public String getMode() {
		return mode;
	}
	public String getTag() {
		return tag;
	}
	public String getMapId() {
		return mapId;
	}
	public String getName() {
		return gameName;
	}
	public String toString() {
		return getName();
	}
}