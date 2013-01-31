
public class LoLClient {

	private String clientId;
	private String clientInputPath;
	private String clientPath;
	private String clientDataPath;
	
	public LoLClient(String clientId, String clientInputPath, String clientPath, String clientDataPath) {
		this.clientId = clientId;
		this.clientInputPath = clientInputPath;
		this.clientPath = clientPath;
		this.clientDataPath = clientDataPath;
	}
	
	public LoLClient() {
	}
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientInputPath() {
		return clientInputPath;
	}
	public void setClientInputPath(String clientInputPath) {
		this.clientInputPath = clientInputPath;
	}
	public String getClientPath() {
		return clientPath;
	}
	public void setClientPath(String clientPath) {
		this.clientPath = clientPath;
	}
	public String getClientDataPath() {
		return clientDataPath;
	}
	public void setClientDataPath(String clientDataPath) {
		this.clientDataPath = clientDataPath;
	}
	public String toString() {
		return clientPath;
	}

}
