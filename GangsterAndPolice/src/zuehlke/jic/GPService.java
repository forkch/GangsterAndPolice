package zuehlke.jic;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPService extends Service implements IOCallback, LocationListener {

	private static final String GP_WEBSERVICE_URL = "http://ec2-67-202-49-208.compute-1.amazonaws.com";
	private static final String TAG = "GPService";
	private final IBinder mBinder = new GPBinder();
	private List<GPServiceListener> listeners = new ArrayList<GPServiceListener>();
	private SocketIO socket;

	private String clientId;

	@Override
	public IBinder onBind(Intent arg0) {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 0, this);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 0, this);
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation != null) {
			onLocationChanged(lastKnownLocation);
		} else {
			lastKnownLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastKnownLocation != null) {
				onLocationChanged(lastKnownLocation);
			}
		}

		return mBinder;
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

			if (event.toLowerCase().equals("register")) {
				clientId = json.getString("clientId");
				for (GPServiceListener l : listeners) {
					l.onRegistration(clientId);
				}
			} else if (event.toLowerCase().equals("position")) {
				Log.d(TAG, "position msg recieved: " + json.toString());

				Player p = new Player();

				p.setClientId(json.getString("clientId"));
				p.setName(json.getString("clientId")); // FIXME
				if (json.getString("role").toLowerCase().equals("robber"))
					p.setGangster(true);
				else
					p.setGangster(false);

				p.setLat(json.getDouble("lat"));
				p.setLng(json.getDouble("lng"));
				p.setTimestamp(json.getString("time"));

				for (GPServiceListener l : listeners) {
					l.onPositionUpdate(p);
				}
			}
		} catch (JSONException e) {
			Log.wtf(TAG, e);
		}
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

		if (socket == null || clientId == null) {
			return;
		}

		JSONObject positionMessage = new JSONObject();

		try {
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
