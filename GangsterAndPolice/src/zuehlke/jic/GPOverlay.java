package zuehlke.jic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class GPOverlay extends ItemizedOverlay<OverlayItem> implements
		GPServiceListener {

	private static final int FONT_SIZE = 12;
	private static final int TITLE_MARGIN = 3;

	List<Player> players = new ArrayList<Player>();
	private boolean isGangster;
	private String myClientId;
	private Context context;
	private int markerHeight;
	private int arrestRadiusInMeter;

	public GPOverlay(Context context, Drawable marker, String myClientId,
			boolean isGangster, int arrestRadiusInMeter) {
		super(boundCenterBottom(marker));

		this.context = context;
		this.myClientId = myClientId;
		this.isGangster = isGangster;

		this.markerHeight = ((BitmapDrawable) marker).getBitmap().getHeight();

		this.arrestRadiusInMeter = arrestRadiusInMeter;

		populate();
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		Player p = players.get(arg0);

		GeoPoint geoPoint = createGeoPoint(p);

		return new OverlayItem(geoPoint, p.getName(), null);
	}

	private GeoPoint createGeoPoint(Player p) {
		GeoPoint geoPoint = new GeoPoint((int) (p.getLat() * 1e6),
				(int) (p.getLng() * 1e6));
		return geoPoint;
	}

	@Override
	public int size() {
		return players.size();
	}

	@Override
	public void onPositionUpdate(final Player p) {

		if (p.isGangster() != isGangster)
			return;

		if (p.getClientId().equals(myClientId))
			return;

		new Handler(context.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {

				int index = players.indexOf(p);
				if (index < 0)
					players.add(p);
				else
					players.set(index, p);

				setLastFocusedIndex(-1);
				populate();
			}
		});

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
	public void onNewPlayer(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(android.graphics.Canvas canvas, MapView mapView,
			boolean shadow) {
		super.draw(canvas, mapView, shadow);

		// go through all OverlayItems and draw title for each of them
		for (Player p : players) {
			createPlayerMarker(canvas, mapView, p);
			if (p.isGangster()) {
				createGangsterArrestCircle(canvas, mapView, p,
						arrestRadiusInMeter);
			}
		}
	}

	private void createGangsterArrestCircle(Canvas canvas, MapView mapView,
			Player p, int arrestRadiusInMeter) {
		Projection projection = mapView.getProjection();

		Point pt = new Point();

		GeoPoint geo = new GeoPoint((int) (p.getLat() * 1e6),
				(int) (p.getLng() * 1e6));

		projection.toPixels(geo, pt);
		float circleRadius = projection
				.metersToEquatorPixels(arrestRadiusInMeter);

		Paint innerCirclePaint;

		innerCirclePaint = new Paint();
		if (p.isArrestable()) {
			innerCirclePaint.setColor(Color.RED);
		} else {
			innerCirclePaint.setColor(Color.BLUE);
		}
		innerCirclePaint.setAlpha(25);
		innerCirclePaint.setAntiAlias(true);

		innerCirclePaint.setStyle(Paint.Style.FILL);

		canvas.drawCircle((float) pt.x, (float) pt.y, circleRadius,
				innerCirclePaint);

	}

	private void createPlayerMarker(android.graphics.Canvas canvas,
			MapView mapView, Player p) {
		/*
		 * Converts latitude & longitude of this overlay item to coordinates on
		 * screen. As we have called boundCenterBottom() in constructor, so
		 * these coordinates will be of the bottom center position of the
		 * displayed marker.
		 */
		GeoPoint point = createGeoPoint(p);
		Point markerBottomCenterCoords = new Point();
		mapView.getProjection().toPixels(point, markerBottomCenterCoords);

		/* Find the width and height of the title */
		TextPaint paintText = new TextPaint();
		Paint paintRect = new Paint();

		Rect rect = new Rect();
		paintText.setTextSize(FONT_SIZE);
		paintText.getTextBounds(p.getName(), 0, p.getName().length(), rect);

		rect.inset(-TITLE_MARGIN, -TITLE_MARGIN);
		rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2,
				markerBottomCenterCoords.y - markerHeight - rect.height());

		paintText.setTextAlign(Paint.Align.CENTER);
		paintText.setTextSize(FONT_SIZE);
		paintText.setARGB(255, 255, 255, 255);
		paintRect.setARGB(130, 0, 0, 0);

		canvas.drawRoundRect(new RectF(rect), 2, 2, paintRect);
		canvas.drawText(p.getName(), rect.left + rect.width() / 2, rect.bottom
				- TITLE_MARGIN, paintText);
	}

	@Override
	public void onMessage(GPMessage msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onArrestablePlayer(Player arrestablePlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNoArrestablePlayerLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHit(final Player player) {

		new Handler(context.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {

				int index = players.indexOf(player);
				if (index >= 0)
					players.remove(index);

				setLastFocusedIndex(-1);
				populate();
			}
		});
	}

	@Override
	public void onAllRobbersArrested(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameOver() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWeAreBeingArrested() {
		// TODO Auto-generated method stub
		
	}

}
