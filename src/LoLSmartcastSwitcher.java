import java.awt.im.InputContext;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoLSmartcastSwitcher {

	private static final String[] iniKeys = { "evtCastSpell1", "evtCastSpell2",
			"evtCastSpell3", "evtCastSpell4", "evtSmartCastSpell1",
			"evtSmartCastSpell2", "evtSmartCastSpell3", "evtSmartCastSpell4",
			"evtSmartPlusSelfCastSpell1", "evtSmartPlusSelfCastSpell2",
			"evtSmartPlusSelfCastSpell3", "evtSmartPlusSelfCastSpell4" };

	private Map<String, String> iniMap = new HashMap<String, String>();
	private Map<String, LoLSkills> champMap = new HashMap<String, LoLSkills>();

	private static final String keysAz = "azer";
	private static final String keysQw = "qwer";
	public static String keys = null;

	private boolean status;
	private static final String smartcastFile = "smartcast.ini";
	private String inputFile;
	private String gameFile;

	private IniFile iniFile;
	private Boolean showRangeIndicator;

	private String curChampSmartcast = null;

	public class LoLSkills {
		StringBuffer skills;
		static final int CAST = 0;
		static final int SMARTCAST = 1;
		static final int SELFSMARTCAST = 2;

		public LoLSkills(String vals) {
			skills = new StringBuffer(vals);
		}

		public LoLSkills() {
			skills = new StringBuffer("0000");
		}

		public void setSkill(int skill, int state) {
			skills.setCharAt(skill, (char) ('0' + state));
		}

		public int getSkill(int skill) {
			return skills.charAt(skill) - '0';
		}

		public boolean isSmartcast(int skill) {
			return skills.charAt(skill) == '1';
		}

		public boolean isSelfSmartcast(int skill) {
			return skills.charAt(skill) == '2';
		}

		public boolean hasSmartcasts() {
			return !skills.toString().equals("0000");
		}

		public String toString() {
			return skills.toString();
		}
	}

	public LoLSmartcastSwitcher() {
		loadSmartCasts();
	}

	public LoLSmartcastSwitcher(String inputFilePath) {
		loadSmartCasts();
		load(inputFilePath);
	}

	public void debug() {
		String res = "";
		for (String champ : champMap.keySet()) {
			res += champ + ", ";
		}
		System.out.println(res);
	}

	public void load(String inputPath) {
		iniMap.clear();
				
		gameFile = inputPath + File.separator + "game.cfg";
		if (!new File(gameFile).exists()) {
			Log.getInst().warning("'" + gameFile + "' file hasn't been found. You won't be able to save the range indicator option");
			gameFile = null;
			showRangeIndicator = null;
		}
		else {
			loadRangeIndicator();
		}
		
		if (inputPath == null || !new File(inputPath).exists()) {
			Log.getInst().warning("'" + inputPath + "' path hasn't been found. You won't be able to enable SmartCast Switcher");
			status = false;
			return;
		}

		inputFile = inputPath + File.separator + "input.ini";
		loadInput();
		
		// everything's ok
		status = true;

		// TODO: check if the keys have been changed in game after sswitcher has
		// been enabled
		// TODO: add self smart cast ?
	}

	public boolean getStatus() {
		return status;
	}
	
	// get smartcast indicator state
	public void loadRangeIndicator() {
		IniFile game = new IniFile(gameFile);
		String range = game.getValue("HUD", "SmartCastOnKeyRelease");
		showRangeIndicator = !(range == null || range.equals("0"));
	}
	
	// save smartcast indicator state to game cfg file
	public void saveRangeIndicator() {
		if (gameFile == null) {
			return;
		}
		IniFile game = new IniFile(gameFile);
		game.addValue("HUD", "SmartCastOnKeyRelease", (showRangeIndicator) ? "1" : "0");
		game.save(gameFile);
	}
	
	public Boolean getShowRangeIndicator() {
		return showRangeIndicator;
	}
	
	public void setShowRangeIndicator(boolean state) {
		showRangeIndicator = state;
	}

	public boolean isCurChampSmartCast(String champ) {
		return getCurChampSmartcast().equals(champ);
	}

	public String getCurChampSmartcast() {
		if (curChampSmartcast == null)
			return "";
		return curChampSmartcast;
	}

	public void setCurChampSmartcast(String champ) {
		curChampSmartcast = (champ == null) ? "" : champ;
	}

	public LoLSkills getChampSkills(String champ) {
		return champMap.get(champ);
	}

	public void setChampSkills(String champ, LoLSkills skills) {
		champMap.put(champ, skills);
	}

	public int getChampState(String champ) {
		if (getCurChampSmartcast().equals(champ)) {
			return 2;
		}
		if (hasSmartCasts(champ)) {
			return 1;
		}
		return 0;
	}

	public void removeChamp(String champ) {
		champMap.remove(champ);
	}

	public boolean hasSmartCasts(String champ) {
		LoLSkills s = champMap.get(champ);
		if (s == null)
			return false;
		return s.hasSmartcasts();
	}

	public LoLSkills getSkills(String champ) {
		LoLSkills res = champMap.get(champ);
		if (res == null) {
			res = new LoLSkills();
		}
		return res;
	}

	public void loadSmartCasts() {
		IniFile ini = new IniFile(smartcastFile);
		// create default data if not existing
		IniFile.IniSection sec = ini.getSection("champions");
		if (sec == null) {
			sec = ini.createSection("champions");
		}
		for (String champ : sec.keySet()) {
			String bools = sec.get(champ);
			champMap.put(champ, new LoLSkills(bools));
		}
		// load default keys if custom ones set
		sec = ini.getSection("config");
		if (sec != null) {
			keys = sec.get("layout");
			if (keys != null && keys.length() != 4) {
				Log.getInst().warning("Data 'layout' corrupted, setting default values");
				keys = null;
			}
		}
		// otherwise get default ones
		if (keys == null) {
			keys = (InputContext.getInstance().getLocale() != null && InputContext.getInstance().getLocale().equals(Locale.FRENCH)) ? keysAz : keysQw;
		}
	}

	public void saveSmartCasts() {
		IniFile ini = new IniFile();
		// save champions skills
		IniFile.IniSection sec = ini.createSection("champions");
		for (String champ : champMap.keySet()) {
			LoLSkills sk = champMap.get(champ);
			// if at least one smartcast
			if (sk.hasSmartcasts()) {
				sec.put(champ, sk.toString());
			}
		}
		// save custom keys
		sec = ini.getSection("config");
		if (sec == null) {
			sec = ini.createSection("config");
		}
		sec.put("layout", keys);
		ini.save(smartcastFile);
	}
	
	public void saveKeys() {
		IniFile ini = new IniFile(smartcastFile);
		IniFile.IniSection sec = ini.getSection("config");
		if (sec == null) {
			sec = ini.createSection("config");
		}
		sec.put("layout", keys);
		ini.save(smartcastFile);
	}
	
	public void replaceKey(int pos, char newc) {
		StringBuffer buf = new StringBuffer(keys);
		buf.setCharAt(pos, newc);
		keys = buf.toString();
		saveKeys();
	}
	
	public char getDefaultKey(int pos) {
		String k = InputContext.getInstance().getLocale().equals(Locale.FRENCH) ? keysAz : keysQw;
		return k.charAt(pos);
	}

	public void loadInput() {
		Log.getInst().info("Loading input file '" + inputFile + "'");
		iniFile = new IniFile(inputFile);
		IniFile.IniSection sec = iniFile.getSection("GameEvents");
		// if ini file without the needed section
		if (sec == null) {
			sec = iniFile.createSection("GameEvents");
		}

		// if smartcast switcher is enabled
		curChampSmartcast = sec.get("smartcastChamp");
		if (curChampSmartcast != null) {
			// it means we have the old keys stored in the file, plus the custom
			// ones
			for (String key : iniKeys) {
				iniMap.put(key, sec.get("sav_" + key));
			}

			// if ss is enabled but the champ has no smartcasts saved (may
			// happens if smartcast.ini is erased)
			if (!hasSmartCasts(curChampSmartcast)) {
				Log.getInst().warning(
						"No smartcasts defined for enabled champion '"
								+ curChampSmartcast
								+ "', disabling Smartcast Switcher");
				// we just disable ss
				setCurChampSmartcast(null);
				setSmartcast();
			}
		} else {
			// otherwise we only have the non custom keys
			for (String key : iniKeys) {
				iniMap.put(key, sec.get(key));
			}
		}
	}

	private void restaureSavedKeys(IniFile.IniSection sec) {
		for (String key : iniKeys) {
			String val = iniMap.get(key);
			// restaure original key if any
			if (val != null) {
				sec.put(key, val);
			} else {
				sec.remove(key);
			}
			// remove saved keys
			sec.remove("sav_" + key);
		}
	}

	private void saveKeys(IniFile.IniSection sec) {
		for (String key : iniKeys) {
			String val = iniMap.get(key);
			if (val != null) {
				sec.put("sav_" + key, val);
			}
		}
	}

	public Map<String, String> simulatedKeys(LoLSkills skills) {
		Map<String, String> newKeys = new HashMap<String, String>();
		// we have our old keys in the iniMap
		// set new keys
		for (int i = 1; i <= keys.length(); i++) {
			// get the saved keys from the map
			String cast = iniMap.get("evtCastSpell" + i);
			String smartcast = iniMap.get("evtSmartCastSpell" + i);
			String selfsmartcast = iniMap.get("evtSmartPlusSelfCastSpell" + i);
			// if smartcast for this key
			if (skills.isSmartcast(i - 1)) {
				// if no key defined, take the default one
				// change: we will always use the "default" keys (that can be changed by the user)
				//if (cast == null) {
					cast = "[" + keys.charAt(i-1) + "]";
				//}
				if (smartcast == null || smartcast.equals(cast)) {
					// smartcast = "[<Unbound>]";
					smartcast = "[Shift]" + cast;
				}

				// switch keys
				newKeys.put("evtSmartCastSpell" + i, cast);
				newKeys.put("evtCastSpell" + i, smartcast);
				newKeys.put("evtSmartPlusSelfCastSpell" + i, "[<Unbound>]");
			} 
			else if (skills.isSelfSmartcast(i - 1)) {
				// if no key defined, take the default one
				//if (cast == null) {
					cast = "[" + keys.charAt(i-1) + "]";
				//}
				if (selfsmartcast == null || selfsmartcast.equals(cast)) {
					// smartcast = "[<Unbound>]";
					selfsmartcast = "[Shift]" + cast;
				}

				// switch keys
				newKeys.put("evtSmartPlusSelfCastSpell" + i, cast);
				newKeys.put("evtCastSpell" + i, selfsmartcast);
				newKeys.put("evtSmartCastSpell" + i, "[<Unbound>]");
			}
			// TODO: usefull ?
			// no smartcast, do not switch anything
			else {
				if (smartcast != null) {
					newKeys.put("evtSmartCastSpell" + i, smartcast);
				} else {
					newKeys.remove("evtSmartCastSpell" + i);
				}
				if (selfsmartcast != null) {
					newKeys.put("evtSmartPlusSelfCastSpell" + i, smartcast);
				} else {
					newKeys.remove("evtSmartPlusSelfCastSpell" + i);
				}
				if (cast != null) {
					newKeys.put("evtCastSpell" + i, cast);
				} else {
					newKeys.remove("evtCastSpell" + i);
				}
			}
		}
		return newKeys;
	}

	public void setSmartcast() {
		if (iniFile == null) {
			return;
		}
		// read input file
		IniFile.IniSection sec = iniFile.getSection("GameEvents");
		// if smartcast switcher needs to be disabled
		if (curChampSmartcast == null || curChampSmartcast.isEmpty()) {
			// remove the champ key if exists
			sec.remove("smartcastChamp");
			// restaure saved keys
			restaureSavedKeys(sec);
		}
		// we will add a champion config
		else {
			// get champ skills values
			LoLSkills skills = getSkills(curChampSmartcast);
			// if no champ config already, save keys
			if (sec.get("smartcastChamp") == null) {
				saveKeys(sec);
			}
			// champ champ name
			sec.put("smartcastChamp", curChampSmartcast);
			
			// get new keys from the saved ones
			Map<String, String> newKeys = simulatedKeys(skills);
			
			// replace old with new keys in the ini file
			for (String key : newKeys.keySet()) {
				sec.put(key, newKeys.get(key));
			}
		}
		iniFile.save(inputFile);
	}
}
