import java.util.ArrayList;
import java.util.List;


public class LoLClientManager {

	private static LoLClientManager instance;
	private List<LoLClient> clients;
	private LoLClient activeClient;
	
	private LoLClientManager() {
		clients = new ArrayList<LoLClient>();
	}
	
	public static LoLClientManager getInst() {
		if (instance == null) {
			instance = new LoLClientManager();
		}
		return instance;
	}
	
	public void loadClients(String file) {
		IniFile f = new IniFile(file);
		String activeC = f.getSection("general").get("activeClient");
		int nbClients = Integer.parseInt(f.getSection("general").get("clients"));
		for (int i=0; i<nbClients; i++) {
			String cId = "client" + i;
			IniFile.IniSection sec = f.getSection(cId);
			LoLClient c = new LoLClient(cId, sec.get("clientInputPath"), sec.get("clientPath"), sec.get("clientDataPath"));
			clients.add(c);
			if (cId.equals(activeC)) {
				activeClient = c;
			}
		}
	}
	
	public LoLClient createClient(String clientInputPath, String clientPath, String clientDataPath) {
		LoLClient res = new LoLClient("", clientInputPath, clientPath, clientDataPath);
		addClient(res);
		return res;
	}
	
	public void addClient(LoLClient c) {
		String cId = "client" + clients.size();
		c.setClientId(cId);
		clients.add(c);
	}
	
	public void removeClient(LoLClient c) {
		int index = clients.indexOf(c);
		if (index == -1) {
			return;
		}
		clients.remove(index);
		// change id of all following clients
		for (int i=index; i<clients.size(); i++) {
			clients.get(index).setClientId("client" + index);
		}
	}
	
	public void addClientsToIni(IniFile file) {
		for (LoLClient c : clients) {
			addClientToIni(c, file);
		}
	}
	
	public void addClientToIni(LoLClient c, IniFile file) {
		IniFile.IniSection sec = file.getSection(c.getClientId());
		if (sec == null) {
			sec = file.createSection(c.getClientId());
		}
		sec.put("clientInputPath", c.getClientInputPath());
		sec.put("clientPath", c.getClientPath());
		sec.put("clientDataPath", c.getClientDataPath());
	}

	
	public LoLClient getActiveClient() {
		return activeClient;
	}
	
	public void setActiveClient(LoLClient c) {
		activeClient = c;
	}
	
	public LoLClient getClientAt(int index) {
		if (index < 0 || index >= clients.size()) return null;
		return clients.get(index);
	}
	
	public List<LoLClient> getClients() {
		return clients;
	}
	
	public int getClientCount() {
		return clients.size();
	}
	
	
}
