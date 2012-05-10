package zuehlke.jic;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class GPMapActivity extends MapActivity implements GPServiceListener {

	private MapView map;
	private GPApplication application;
	private MyLocationOverlay myLocationOverlay;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		map = (MapView) findViewById(R.id.map);
		map.setBuiltInZoomControls(true);
		application = (GPApplication) getApplication();
		application.getService().registerGPServiceListener(this);

		GPOverlay policeOverlay = new GPOverlay(this, this.getResources()
				.getDrawable(R.drawable.cool), application.getClientId(), false);

		GPOverlay gangsterOverlay = new GPOverlay(this, this.getResources()
				.getDrawable(R.drawable.devil), application.getClientId(), true);

		application.getService().registerGPServiceListener(policeOverlay);
		application.getService().registerGPServiceListener(gangsterOverlay);

		// map.getController().zoomToSpan(overlay.getLatSpanE6(),
		// overlay.getLonSpanE6());

		List<Overlay> overlays = map.getOverlays();
		myLocationOverlay = new MyLocationOverlay(this, map);

		overlays.add(gangsterOverlay);
		overlays.add(policeOverlay);
		overlays.add(myLocationOverlay);
		map.invalidate();
		handler = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		myLocationOverlay.disableCompass();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		myLocationOverlay.enableCompass();
		myLocationOverlay.enableMyLocation();
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
	public void onPositionUpdate(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageReceived(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onArrestSuccessful() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onArrestUnsuccessful() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewPlayer(final Player p) {
		final String msg = "Player " + p.getName() + " joined the game";
		toastMsg(msg);

	}

	private void toastMsg(final String msg) {
		handler.post(new Runnable() {

			public void run() {
				Toast.makeText(getApplicationContext(),
						msg,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onMessage(GPMessage msg) {
		Player player = application.getPlayers().get(msg.getClientId());
		if (player != null) {
			toastMsg(player.getName() + ": " + msg.getMessage());
		}
		
	}

}
