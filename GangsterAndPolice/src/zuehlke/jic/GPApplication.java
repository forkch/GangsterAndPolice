package zuehlke.jic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;

public class GPApplication extends Application {

	private String clientId;

	private Map<String, Player> players = new HashMap<String,Player>();
	
	private List<GPMessage> messages = new ArrayList<GPMessage>();

	public Map<String,Player> getPlayers() {
		return players;

	}
	
	public List<GPMessage> getMessages() {
		return messages;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	private GPService service;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	public GPService getService() {
		return service;
	}

	public void setService(GPService service) {
		this.service = service;
	}

}
