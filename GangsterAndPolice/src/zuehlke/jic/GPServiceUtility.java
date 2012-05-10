package zuehlke.jic;

import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public final class GPServiceUtility {

	private GPServiceUtility() {
		super();
	}

	public static void registerLocationManager(LocationManager locationManager,
			LocationListener locationListener) {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 3, locationListener);

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation != null) {
			locationListener.onLocationChanged(lastKnownLocation);
		} else {
			lastKnownLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastKnownLocation != null) {
				locationListener.onLocationChanged(lastKnownLocation);
			}
		}
	}

	public static GeoPoint toGeoPoint(Location location) {
		return new GeoPoint(((int) (location.getLatitude() * 1e6)),
				(int) (location.getLongitude() * 1e6));
	}

	public static Location toLocation(double lat, double lng) {
		Location loc = new Location(LocationManager.GPS_PROVIDER);
		loc.setLatitude(lat);
		loc.setLongitude(lng);
		return loc ;
	}
}
