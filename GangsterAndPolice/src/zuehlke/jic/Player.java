package zuehlke.jic;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

public class Player {

	private static final int LOCATION_HISTORY_SIZE = 100;
	private boolean isGangster;
	private String clientId;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	String timestamp;
	double lat;
	double lng;
	private AutoDiscardingDeque<Location> locations = new AutoDiscardingDeque<Location>(
			LOCATION_HISTORY_SIZE);
	private boolean isArrestable = false;

	public boolean isGangster() {
		return isGangster;
	}

	public void setGangster(boolean isGangster) {
		this.isGangster = isGangster;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientId == null) ? 0 : clientId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		return true;
	}

	public List<Location> getLastLocations() {
		return new ArrayList<Location>(this.locations);
	}

	public void addLocation(Location location) {
		this.locations.offerFirst(location);
	}

	public void setArrestable(boolean isArrestable) {
		this.isArrestable = isArrestable;
	}

	public boolean isArrestable() {
		return isArrestable;
	}
}
