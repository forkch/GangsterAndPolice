package zuehlke.jic;

import java.util.List;
import java.util.Map;

import android.os.Bundle;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class GPMapActivity extends MapActivity implements GPServiceListener {

	private MapView map;
	private GPApplication application;
	private MyLocationOverlay myLocationOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		map = (MapView) findViewById(R.id.map);
		map.setBuiltInZoomControls(true);
		application = (GPApplication) getApplication();
		application.getService().registerGPServiceListener(this);

		GPOverlay overlay = new GPOverlay(this, this.getResources().getDrawable(
				R.drawable.ic_launcher));
		application.getService().registerGPServiceListener(overlay);

//		map.getController().zoomToSpan(overlay.getLatSpanE6(),
//				overlay.getLonSpanE6());

		List<Overlay> overlays = map.getOverlays();
		myLocationOverlay = new MyLocationOverlay(this, map);


		overlays.add(overlay);
		overlays.add(myLocationOverlay);
		map.invalidate();
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

}
