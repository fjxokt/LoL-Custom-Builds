import java.util.Collection;
import java.util.TreeMap;


public class Languages {

	static class Lang {
		String locale;
		String url;
		String name;
		public Lang(String loc, String ur, String nam) {
			locale = loc;
			url = ur;
			name = nam;
		}
	}
	
	@SuppressWarnings("serial")
	// for languages that don't have their lol website, use the english website
	static TreeMap<String,Lang> languages = new TreeMap<String,Lang>() {{
		put("en_US", new Lang("en_US", "http://na.leagueoflegends.com", "English"));
		put("fr_FR", new Lang("fr_FR", "http://euw.leagueoflegends.com/fr", "Français"));
		put("de_DE", new Lang("de_DE", "http://euw.leagueoflegends.com/de", "Deutsch"));
		put("es_ES", new Lang("es_ES", "http://euw.leagueoflegends.com/es", "Español"));
		put("it_IT", new Lang("it_IT", "http://na.leagueoflegends.com", "Italiano"));
		put("se_SE", new Lang("se_SE", "http://na.leagueoflegends.com", "Svenska"));
		put("pl_PL", new Lang("pl_PL", "http://eune.leagueoflegends.com/pl", "Polski"));
		put("el_GR", new Lang("el_GR", "http://eune.leagueoflegends.com/el", "ελληνικά"));
		put("ro_RO", new Lang("ro_RO", "http://eune.leagueoflegends.com/ro", "Română"));
		put("pt_BR", new Lang("pt_BR", "http://br.leagueoflegends.com/pt-br", "Português"));
		put("tr_TR", new Lang("tr_TR", "http://tr.leagueoflegends.com/tr", "Türkçe"));
		put("fa_IR", new Lang("fa_IR", "http://na.leagueoflegends.com", "فارسی"));
		put("zh_TW", new Lang("zh_TW", "http://na.leagueoflegends.com", "繁體中文"));
	}};
	
	public static Collection<Lang> getList() {
		return languages.values();
	}
	
	public static String getUrl(String locale) {
		return languages.get(locale).url;
	}
	
	public static String getName(String locale) {
		return languages.get(locale).name;
	}
	
}
