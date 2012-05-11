package zuehlke.jic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.app.Application;

public class GPApplication extends Application {

	private String clientId;
	private double lat;
	private double lng;

	private Map<String, Player> players = new HashMap<String, Player>();

	private LinkedList<GPMessage> messages = new LinkedList<GPMessage>();

	private GPService service;

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public Map<String, Player> getPlayers() {
		return players;

	}

	public LinkedList<GPMessage> getMessages() {
		return messages;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public GPService getService() {
		return service;
	}

	public void setService(GPService service) {
		this.service = service;
	}

	public void reset() {
		players.clear();
		clientId = "";
		lat = Double.NEGATIVE_INFINITY;
		lng = Double.NEGATIVE_INFINITY;
		messages.clear();
	}

}
