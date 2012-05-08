package zuehlke.jic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class GPOverlay extends ItemizedOverlay<OverlayItem> implements GPServiceListener {

	List<Player> players = new ArrayList<Player>();
	
	public GPOverlay(Context context, Drawable marker) {
		super(boundCenterBottom(marker));

		
		populate();
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		Player p = players.get(arg0);
		
		GeoPoint geoPoint = new GeoPoint((int) (p.getLat() * 1e6),(int) (p.getLng() * 1e6));
		
		return new OverlayItem(geoPoint, p.getName(), null);
	}

	@Override
	public int size() {
		return players.size();
	}

	@Override
	public void onPositionUpdate(Player p) {

		int index = players.indexOf(p);
		if(index < 0)
			players.add(p);
		else
			players.set(index, p);
		populate();
		
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
