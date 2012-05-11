package zuehlke.jic;

import static zuehlke.jic.GPServiceUtility.registerLocationManager;
import static zuehlke.jic.GPServiceUtility.toLocation;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPService extends Service implements IOCallback, LocationListener {

	private static final String GP_WEBSERVICE_URL = "http://ec2-67-202-49-208.compute-1.amazonaws.com";
	// private static final String GP_WEBSERVICE_URL = "http://192.168.56.101";
	private static final String TAG = "GPService";
	private final IBinder mBinder = new GPBinder();
	private List<GPServiceListener> listeners = new ArrayList<GPServiceListener>();
	private SocketIO socket;

	@Override
	public IBinder onBind(Intent arg0) {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		registerLocationManager(locationManager, this);

		return mBinder;
	}

	public GPApplication getGPApplication() {
		return ((GPApplication) getApplication());
	}

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class GPBinder extends Binder {
		GPService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return GPService.this;
		}
	}

	public String ping(String message) {
		return message;
	}

	public void connectSocket() throws GPServiceException {
		try {
			if (socket == null) {
				socket = new SocketIO(GP_WEBSERVICE_URL);
				socket.connect(this);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error in connectSocket()", e);
			throw new GPServiceException(e);
		}
	}

	public void registerClient(String name, String role)
			throws GPServiceException {
		JSONObject registerObj = new JSONObject();
		try {
			registerObj.put("role", role);
			registerObj.put("name", name);

			socket.emit("register", registerObj);

		} catch (JSONException e) {
			Log.wtf(TAG, e);
		}
	}

	public void arrest() {
		JSONObject arrestObj = new JSONObject();
		try {
			arrestObj.put("clientId", getGPApplication().getClientId());
			arrestObj.put("lat", getGPApplication().getLat());
			arrestObj.put("lng", getGPApplication().getLng());

			socket.emit("arrest", arrestObj);

		} catch (JSONException e) {
			Log.wtf(TAG, e);
		}
	}

	public void sendMessage(String string) {
		JSONObject arrestObj = new JSONObject();
		try {
			arrestObj.put("clientId", getGPApplication().getClientId());
			arrestObj.put("message", string);
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

			arrestObj.put("time",
					format.format(new Date(System.currentTimeMillis())));

			socket.emit("message", arrestObj);

		} catch (JSONException e) {
			Log.wtf(TAG, e);
		}
	}

	public void registerGPServiceListener(GPServiceListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void onDisconnect() {
		Log.d(TAG, "onDisconnect()");

	}

	@Override
	public void onConnect() {
		Log.d(TAG, "onConnect()");
		for (GPServiceListener l : listeners) {
			l.onConnectionEstablished();
		}
	}

	@Override
	public void onMessage(String data, IOAcknowledge ack) {
		Log.d(TAG, data);

	}

	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {
		Log.d(TAG, json.toString());

	}

	@Override
	public void on(String event, IOAcknowledge ack, Object... args) {
		Log.d(TAG, "on()");
		try {
			JSONObject json = (JSONObject) args[0];
			String clientId = "";
			if (json.has("clientId")) {
				clientId = json.getString("clientId");
			}
			if (event.toLowerCase().equals("register")) {

				getGPApplication().setClientId(clientId);
				for (GPServiceListener l : listeners) {
					l.onRegistration(clientId);
				}
			} else if (event.toLowerCase().equals("position")) {
				Log.d(TAG, "position msg recieved: " + json.toString());
				double lat = json.getDouble("lat");
				double lng = json.getDouble("lng");
				String time = json.getString("time");

				if (getGPApplication().getClientId().equals(clientId)) {
					return;
				}

				Player p = getGPApplication().getPlayers().get(clientId);
				if (p == null) {
					p = initPlayer(json, clientId);

				}
				p.setLat(lat);
				p.setLng(lng);
				p.setTimestamp(time);
				p.addLocation(toLocation(p.getLat(), p.getLng()));

				if (!getGPApplication().getPlayers().values().contains(p)
						&& !p.getClientId().equals(
								getGPApplication().getClientId())) {
					getGPApplication().getPlayers().put(p.getClientId(), p);
					for (GPServiceListener l : listeners) {
						l.onNewPlayer(p);
					}
				}

				for (GPServiceListener l : listeners) {
					l.onPositionUpdate(p);
				}
			} else if (event.toLowerCase().equals("arrest")) {
				Log.d(TAG, "arrest msg recieved: " + json.toString());

				JSONArray arrestables = json.getJSONArray("arrestables");
				for (int i = 0; i < arrestables.length(); i++) {
					JSONObject arrestable = arrestables.getJSONObject(i);

					String arrestableId = arrestable.getString("clientId");
					if (arrestableId.equals(getGPApplication().getClientId())) {

						for (GPServiceListener l : listeners) {
							l.onWeAreBeingArrested();
						}
					} else {

						Player arrestablePlayer = getGPApplication()
								.getPlayers().get(arrestableId);
						if(arrestablePlayer == null) {
							continue;
						}
						arrestablePlayer.setArrestable(true);
						for (GPServiceListener l : listeners) {
							l.onArrestablePlayer(arrestablePlayer);
						}
					}

				}
			} else if (event.toLowerCase().equals("arrest-timeout")) {
				Log.d(TAG, "arrest-timeout msg recieved: " + json.toString());

				if (!json.has("arrestables"))
					return;
				JSONArray arrestables = json.getJSONArray("arrestables");
				for (int i = 0; i < arrestables.length(); i++) {
					JSONObject arrestable = arrestables.getJSONObject(i);

					String arrestableId = arrestable.getString("clientId");

					Player arrestablePlayer = getGPApplication().getPlayers()
							.get(arrestableId);
					if(arrestablePlayer == null)
						continue;
					arrestablePlayer.setArrestable(false);

				}

				boolean noArrestablePlayerLeft = true;
				for (Player p : getGPApplication().getPlayers().values()) {
					if (p.isArrestable()) {
						noArrestablePlayerLeft = false;
						break;
					}
				}
				if (noArrestablePlayerLeft) {
					for (GPServiceListener l : listeners) {
						l.onNoArrestablePlayerLeft();
					}
				}
			} else if (event.toLowerCase().equals("hit")) {
				Log.d(TAG, "hit msg recieved: " + json.toString());

				String hitRooberId = json.getString("clientId");
				if (hitRooberId.equals(getGPApplication().getClientId())) {

					for (GPServiceListener l : listeners) {
						l.onGameOver();
					}
					getGPApplication().reset();
					return;
				}
				Player player = getGPApplication().getPlayers()
						.get(hitRooberId);
				player.setArrestable(false);
				getGPApplication().getPlayers().remove(hitRooberId);
				for (GPServiceListener l : listeners) {
					l.onHit(player);
				}

			} else if (event.toLowerCase().equals("all-robbers-arrested")) {
				Log.d(TAG,
						"all-robbers-arrested msg recieved: " + json.toString());
				for (GPServiceListener l : listeners) {
					l.onAllRobbersArrested(json.getString("msg"));
				}

			} else if (event.toLowerCase().equals("message")) {
				Log.d(TAG, "message received: " + json.toString());

				if (!json.has("clientId")) {
					return;
				}

				GPMessage msg = new GPMessage(json.getString("clientId"),
						json.getString("message"), json.getString("time"));
				getGPApplication().getMessages().addFirst(msg);

				for (GPServiceListener l : listeners) {
					l.onMessage(msg);
				}
			}
		} catch (JSONException e) {
			Log.wtf(TAG, e);
		}
	}

	private Player initPlayer(JSONObject json, String aClientId)
			throws JSONException {
		Player p;
		p = new Player();
		p.setClientId(aClientId);
		if (json.has("name"))
			p.setName(json.getString("name"));
		else
			p.setName(p.getClientId());

		if (json.getString("role").toLowerCase().equals("robber"))
			p.setGangster(true);
		else
			p.setGangster(false);
		return p;
	}

	@Override
	public void onError(SocketIOException socketIOException) {
		Log.e(TAG, socketIOException.getMessage());
		for (GPServiceListener l : listeners) {
			l.onConnectionFailed(socketIOException);
		}
	}

	@Override
	public void onLocationChanged(Location location) {

		Log.d(TAG, "location changed: lat=" + location.getLatitude() + ", lng="
				+ location.getLongitude());

		String clientId = getGPApplication().getClientId();
		if (socket == null || clientId == null) {
			return;
		}

		JSONObject positionMessage = new JSONObject();

		try {
			getGPApplication().setLat(location.getLatitude());
			getGPApplication().setLng(location.getLongitude());
			positionMessage.put("clientId", clientId);
			positionMessage.put("lat", location.getLatitude());
			positionMessage.put("lng", location.getLongitude());
		} catch (JSONException e) {
			Log.wtf(TAG, e);
		}

		socket.emit("position", positionMessage);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
