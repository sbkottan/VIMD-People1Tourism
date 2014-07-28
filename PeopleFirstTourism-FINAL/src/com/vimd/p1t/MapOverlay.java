/* MapOverlay - Created By Nitin Rao
 *  This class allows user to create markers on the map and display information about their location.
 */
package com.vimd.p1t;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

	private Context context;

	public MapOverlay(Context context, Drawable marker) {
		super(boundCenterBottom(marker));
		this.context = context;
		populate();
	}
	

	public void addMarker(String markerName,double dist, GeoPoint geoPoint) {
		if(markerName.equalsIgnoreCase("Source"))
			items.add(new OverlayItem(geoPoint, markerName, "I'm Here!"));
		else
			items.add(new OverlayItem(geoPoint, markerName, dist+ " miles"));
		super.populate();
	}
	
	
	@Override
	protected OverlayItem createItem(int i) {
		return (items.get(i));
	}


	@Override
	protected boolean onTap(int index) {
		  OverlayItem item = items.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.setNeutralButton("OK", null);
		  dialog.show();
		  return true;
		}
	@Override
	public int size() {
		return items.size();
	}
	@SuppressWarnings("unchecked")
	public void removeOverlay(List<Overlay> mapOverlays, OverlayItem overlay) {
		ItemizedOverlay<OverlayItem> o;
		for(int i = 0; i<mapOverlays.size();i++) {
			o = (ItemizedOverlay<OverlayItem>) mapOverlays.get(i);
			try {
			if(o.getItem(0).getTitle().toString().equalsIgnoreCase(overlay.getTitle().toString())){
				mapOverlays.remove(i);
				break;
			}
			}
			catch(Exception e) {
				
			}
		}
	
	}
	
	/*	@Override
	public void draw(Canvas canvas,MapView map,boolean shadow) {
		super.draw(canvas, map, shadow);
		OverlayItem item = this.getItem(0);
		if(!item.getTitle().equalsIgnoreCase("Source"))
			return;
		Point p = new Point();
		
		drawCircle(map,canvas,map.getProjection().toPixels(item.getPoint(), p));
		System.out.println("in draw!");
	}
	
	protected void drawCircle(MapView map,Canvas canvas, Point curScreenCoords) {
	  //  curScreenCoords = toScreenPoint(curScreenCoords);
	    int CIRCLE_RADIUS = 100;
	    Projection proj = map.getProjection();
	    int mToR= metersToRadius(1000, map,this.getItem(0).getPoint().getLatitudeE6());
	    Point pt = map.getProjection().toPixels(this.getItem(0).getPoint(), null);
	    float radius = (float) Math.pow(2, map.getZoomLevel() - 5);
	    float projectedRadius = map.getProjection().metersToEquatorPixels(100);
	    if(radius < canvas.getHeight()/25){
	        radius = canvas.getHeight()/25;
	    }
	   //float radius = proj.metersToEquatorPixels(CIRCLE_RADIUS);
	    // Draw inner info window
	    canvas.drawCircle((float) curScreenCoords.x, (float) curScreenCoords.y, radius, getInnerPaint());
	    // if needed, draw a border for info window
	    canvas.drawCircle(curScreenCoords.x, curScreenCoords.y,radius, getBorderPaint());
	}*/

	private Paint innerPaint, borderPaint;

	public Paint getInnerPaint() {
	    if (innerPaint == null) {
	        innerPaint = new Paint();
	        innerPaint.setARGB(25, 68, 89, 82); // gray
	        innerPaint.setAntiAlias(true);
	    }
	    return innerPaint;
	}

	public Paint getBorderPaint() {
	    if (borderPaint == null) {
	        borderPaint = new Paint();
	        borderPaint.setARGB(255, 68, 89, 82);
	        borderPaint.setAntiAlias(true);
	        borderPaint.setStyle(Style.STROKE);
	        borderPaint.setStrokeWidth(2);
	    }
	    return borderPaint;
	}
	public static int metersToRadius(float meters, MapView map, double latitude) {
	    return (int) (map.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
	}

}