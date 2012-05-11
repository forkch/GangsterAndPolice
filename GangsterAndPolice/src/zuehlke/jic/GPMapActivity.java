package zuehlke.jic;

import static zuehlke.jic.GPServiceUtility.registerLocationManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class GPMapActivity extends MapActivity implements GPServiceListener,
		LocationListener {

	private static final String TAG = "GPMapActivity";

	private static final int ARREST_RADIUS = 100;

	private MapView map;
	private GPApplication application;
	private MyLocationOverlay myLocationOverlay;
	private Handler handler;

	private MediaPlayer ambianceMediaPlayer;

	private MediaPlayer sirenMediaPlayer;

	private MediaPlayer currentMediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		map = (MapView) findViewById(R.id.map);
		map.setBuiltInZoomControls(true);
		application = (GPApplication) getApplication();
		application.getService().registerGPServiceListener(this);
		
		

		GPOverlay policeOverlay = new GPOverlay(this, this.getResources()
				.getDrawable(R.drawable.cool), application.getClientId(),
				false, ARREST_RADIUS);

		GPOverlay gangsterOverlay = new GPOverlay(this, this.getResources()
				.getDrawable(R.drawable.devil), application.getClientId(),
				true, ARREST_RADIUS);

		application.getService().registerGPServiceListener(policeOverlay);
		application.getService().registerGPServiceListener(gangsterOverlay);

		map.getController().setZoom(16);

		List<Overlay> overlays = map.getOverlays();
		myLocationOverlay = new MyLocationOverlay(this, map);

		overlays.add(gangsterOverlay);
		overlays.add(policeOverlay);
		overlays.add(myLocationOverlay);

		map.invalidate();
		handler = new Handler(Looper.getMainLooper());
		ambianceMediaPlayer = MediaPlayer.create(this, R.raw.ambience_louder);
		ambianceMediaPlayer.setLooping(true);
		ambianceMediaPlayer.setVolume(1.f, 1.f);
		currentMediaPlayer = ambianceMediaPlayer;

		sirenMediaPlayer = MediaPlayer.create(this, R.raw.siren);
		sirenMediaPlayer.setLooping(true);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		myLocationOverlay.enableCompass();
		myLocationOverlay.enableMyLocation();

		// location service
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		registerLocationManager(locationManager, this);

		currentMediaPlayer.start();


	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		myLocationOverlay.disableCompass();
		myLocationOverlay.disableMyLocation();
		currentMediaPlayer.pause();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(this, GPMessagesActivity.class));
		return true;
	}

	public void sendArrest(View view) {
		application.getService().arrest();
	}

	public void sendMessage(View view) {
		EditText messageField = (EditText) findViewById(R.id.sendMessageField);

		application.getService().sendMessage(messageField.getText().toString());
		messageField.setText("");
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(messageField.getWindowToken(), 0);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onConnectionEstablished() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegistration(String clientId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPositionUpdate(final Player p) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				drawPathForPlayer(p.getLastLocations(),
						Color.parseColor("red"), map);

				if (p.isGangster()
						&& !p.getClientId().equals(application.getClientId())) {

					MediaPlayer mediaPlayer = MediaPlayer.create(
							GPMapActivity.this, R.raw.mariojump);
					mediaPlayer.start();

				}

			}
		});
	}

	@Override
	public void onMessageReceived(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewPlayer(final Player p) {
		String role = p.isGangster() ? "R" : "P";
		final String msg = "Player " + p.getName() + "[" + role
				+ "] joined the game";
		toastMsg(msg);
	}

	private void toastMsg(final String msg) {
		handler.post(new Runnable() {

			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	@Override
	public void onMessage(GPMessage msg) {
		Player player = application.getPlayers().get(msg.getClientId());
		if (player != null) {
			String role = player.isGangster() ? "R" : "P";
			toastMsg(player.getName() + "[" + role + "]: " + msg.getMessage());
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		if (map != null) {
			Log.d(TAG, "Center to my position");
			map.getController()
					.animateTo(GPServiceUtility.toGeoPoint(location));
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	/**
	 * Does the actual drawing of the route, based on the geo points provided in
	 * the nav set
	 * 
	 * @param navSet
	 *            Navigation set bean that holds the route information, incl.
	 *            geo pos
	 * @param color
	 *            Color in which to draw the lines
	 * @param mMapView01
	 *            Map view to draw onto
	 */
	public void drawPathForPlayer(List<Location> positions, int color,
			MapView mMapView01) {

		if (positions.size() < 2) {
			return;
		}
		// color correction for dining, make it darker
		if (color == Color.parseColor("#add331"))
			color = Color.parseColor("#6C8715");

		List<Overlay> overlaysToAddAgain = new ArrayList<Overlay>();
		for (Iterator<Overlay> iter = mMapView01.getOverlays().iterator(); iter
				.hasNext();) {
			Overlay o = iter.next();
			if (!RouteOverlay.class.getName().equals(o.getClass().getName())) {
				// mMapView01.getOverlays().remove(o);
				overlaysToAddAgain.add(o);
			}
		}
		mMapView01.getOverlays().clear();
		mMapView01.getOverlays().addAll(overlaysToAddAgain);

		try {
			GeoPoint startGP = GPServiceUtility.toGeoPoint(positions.get(0));
			// mMapView01.getOverlays().add(new RouteOverlay(startGP, startGP,
			// 1));
			GeoPoint gp1;
			GeoPoint gp2 = startGP;

			for (int i = 1; i < positions.size(); i++) // the last one would be
														// crash
			{

				gp1 = gp2;

				// for GeoPoint, first:latitude, second:longitude
				gp2 = GPServiceUtility.toGeoPoint(positions.get(i));

				if (i < positions.size()) {
					mMapView01.getOverlays().add(
							new RouteOverlay(gp1, gp2, 2, color));
				}

			}
			// routeOverlays.add(new RouteOverlay(gp2,gp2, 3));
			// mMapView01.getOverlays().add(new RouteOverlay(gp2, gp2, 3));
		} catch (NumberFormatException e) {
		}

		// mMapView01.getOverlays().addAll(routeOverlays); // use the default
		// color
		mMapView01.setEnabled(true);
	}

	@Override
	public void onArrestablePlayer(Player arrestablePlayer) {
		ambianceMediaPlayer.pause();
		if (!sirenMediaPlayer.isPlaying()) {
			sirenMediaPlayer.start();
		}
		currentMediaPlayer = sirenMediaPlayer;
	}

	@Override
	public void onNoArrestablePlayerLeft() {
		sirenMediaPlayer.pause();
		if (!ambianceMediaPlayer.isPlaying()) {
			ambianceMediaPlayer.start();
		}
		currentMediaPlayer = ambianceMediaPlayer;
	}

	@Override
	public void onHit(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAllRobbersArrested(final String string) {
		handler.post(new Runnable() {

			@Override
			public void run() {

				currentMediaPlayer.stop();
				MediaPlayer mediaPlayer = MediaPlayer.create(
						GPMapActivity.this, R.raw.defeat);
				toastMsg(string);
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						Intent backToLogin = new Intent(GPMapActivity.this,
								GPLoginActivity.class);
						startActivity(backToLogin);

					}
				});

			}
		});
	}

	@Override
	public void onGameOver() {
		handler.post(new Runnable() {

			@Override
			public void run() {

				currentMediaPlayer.stop();
				MediaPlayer mediaPlayer = MediaPlayer.create(
						GPMapActivity.this, R.raw.gameover);
				toastMsg("GAME OVER");
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						Intent backToLogin = new Intent(GPMapActivity.this,
								GPLoginActivity.class);
						startActivity(backToLogin);

					}
				});

			}
		});
	}

	@Override
	public void onWeAreBeingArrested() {
		ambianceMediaPlayer.pause();
		if (!sirenMediaPlayer.isPlaying()) {
			sirenMediaPlayer.start();
		}
		currentMediaPlayer = sirenMediaPlayer;
		toastMsg("ATTENTION POLICE NEARBY");
	}
}
