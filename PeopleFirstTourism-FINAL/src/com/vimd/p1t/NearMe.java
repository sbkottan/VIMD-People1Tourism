/*Near me module - Created by Nitin Rao
 * This class implements the Near me feature which includes displaying nearby services in a list view 
 * as well as in map view
 */
package com.vimd.p1t;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.androidtablayout.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class NearMe extends MapActivity {
	public GPS gps;
	//final static String EXTRA_MESSAGE = "edit.list.message";
	ArrayList<Double> distance;
	List<NameValuePair> params;
	 ArrayList<String> names;
	 ArrayList<String> imgs;
	 ArrayList<Double> lat;
	 ArrayList<Double> lng;
	 ProgressDialog pDialog;
	 ImageAdapter adapter;
	private final static String NAME_ID ="list_itm_service_name";
   private final static String DISTANCE_ID ="list_item_distance"; 
   private final static String IMAGE_ID = "list_item_img";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearme_layout);
        
      //  fl.setBackgroundResource(Color.GREEN);
      Intent intent = getIntent();
      String lat = intent.getStringExtra("latitude");
      String lon = intent.getStringExtra("longitude");
      String serv = intent.getStringExtra("services");
      if(lat!= null) {
    	  new NewThread().execute((String[])null);
    	  MapView mapView = (MapView) findViewById(R.id.MapView);
  		mapView.setVisibility(View.VISIBLE);
  		ListView listView = (ListView) findViewById(R.id.FriendList);
  		
  		listView.setVisibility(View.GONE);
  		ToggleButton tog = (ToggleButton) findViewById(R.id.toggleButton1);
  		//tog.setVisibility(View.GONE);
  		tog.setChecked(true);
  		//gps = new GPS(this);
  		plotRoute(serv, Double.parseDouble(lon), Double.parseDouble(lat), 0);
  	
      }
      else {
    	  
      
        new NewThread().execute((String[])null);
        //gps = new GPS(this);
        TextView service = (TextView) findViewById(R.id.list_itm_service_name);
        TextView distances = (TextView) findViewById(R.id.list_item_distance);
		//fetchJson();
		//setViews();
        MapView mapView = (MapView) findViewById(R.id.MapView);
		mapView.setVisibility(View.GONE);
	//	ImageButton imgButton = (ImageButton) findViewById(R.id.button2);
	//	imgButton.setVisibility(View.GONE);
		
		
	//	mapView.getController().setCenter(new GeoPoint(35772052, -78673718));
	//	mapView.getController().setZoom(15);
      } 
    }
  
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	// Fetch services based on current location. Performs geospatial query on the location.
	 public void fetchJson() {
			try {
			//	EditText txtBox = (EditText) findViewById(R.id.editText1);
				//gps = new GPS(this);
				//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		        //StrictMode.setThreadPolicy(policy);
				params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("latitude",String.valueOf(gps.getLatitude())));
				params.add(new BasicNameValuePair("longitude",String.valueOf(gps.getLongitude())));
		        DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://dev.oscar.ncsu.edu:9991/mobileapi/geospatial/");
				httpPost.setEntity(new UrlEncodedFormEntity(params));
				HttpResponse httpResponse = httpClient.execute(httpPost);
		        String data = EntityUtils.toString(httpResponse.getEntity());
		        
			    // Parse json file received from the server
			    JSONObject jObj = new JSONObject(data);
			    JSONArray jArray = jObj.getJSONArray("jsondata");
			    final ListView list = (ListView) findViewById(R.id.FriendList);
			     names = new ArrayList<String>();
			    imgs = new ArrayList<String>();
			    lat = new ArrayList<Double>();
			     lng = new ArrayList<Double>();
			    JSONObject location = null;
			    JSONObject geo = new JSONObject();
			  
			    for (int i = 0;i<jArray.length();i++) {
			    	JSONObject obj = jArray.getJSONObject(i);
			   
			    	imgs.add("http://dev.oscar.ncsu.edu:9991"+obj.getString("picture"));
			    	lat.add(Double.parseDouble(obj.getJSONObject("point").getString("y")));
			    	lng.add(Double.parseDouble(obj.getJSONObject("point").getString("x")));
			    	names.add(obj.getString("name"));
			 
			    }
			    ArrayAdapter<String> ListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
			    double num = 0;
			    DecimalFormat df = new DecimalFormat("#.00");
			    
			    float [] results = new float[2];
			    distance = new ArrayList<Double>();
			    for(int i = 0;i<lat.size();i++) {
			    	// API to calculate distance between 2 coordinates
			    	 Location.distanceBetween( gps.location.getLatitude(),  gps.location.getLongitude(), lat.get(i), lng.get(i), results);
			    	 num = results[0]/(1000*1.6);
			    	 String newNum = df.format(num);
			    	 distance.add(Double.parseDouble(newNum));
			    }
			   // list.setAdapter(ListAdapter);
			    ArrayList<Double> distBkup = (ArrayList<Double>) distance.clone();
			    ArrayList<Double> latBkup = (ArrayList<Double>) lat.clone();
			    ArrayList<Double> lngBkup = (ArrayList<Double>) lng.clone();
			    ArrayList<String> namesBkup = (ArrayList<String>) names.clone();
			    ArrayList<String> imgsBkup = (ArrayList<String>) imgs.clone();
			    Collections.sort(distance);
			    lat.clear();
			    lng.clear();
			    names.clear();
			    imgs.clear();
			    for(int k = 0;k<distance.size();k++) {
			    	int index = distBkup.indexOf(distance.get(k));
			    	lat.add(latBkup.get(index));
			    	lng.add(lngBkup.get(index));
			    	names.add(namesBkup.get(index));
			    	imgs.add(imgsBkup.get(index));
			    	
			    }
			   
			    
			}
				catch(Exception ex) {
					System.out.println(ex.getMessage());
				}
			
		}
	 //Function to set views to all elements on the activity
	  public void setViews() {
		 final  ListView list = (ListView)findViewById(R.id.FriendList);
	        List<Map<String, String>> values = new ArrayList<Map<String, String>>();
	        Map<String, String> map = null;
	        for (int i=0;i<names.size();i++) {
	            map = new HashMap<String,String>();
	            map.put(IMAGE_ID, imgs.get(i));
	            map.put(NAME_ID, names.get(i));
	            map.put(DISTANCE_ID, String.valueOf(distance.get(i)) + " miles");
	            
	            values.add(map);
	        }
	        TextView service = (TextView) findViewById(R.id.list_itm_service_name);
	        TextView distances = (TextView) findViewById(R.id.list_item_distance);
	        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Luna.ttf"); 
 //	       service.setTypeface(type);
	//       distances.setTypeface(type);
	        String[] from = new String[]{NAME_ID,DISTANCE_ID};
	        int[] to = new int[]{R.id.list_itm_service_name,R.id.list_item_distance};
	        //Initiliazing Adapter
	        SimpleAdapter adapters = new SimpleAdapter(NearMe.this,
	                values,//values to be displayed
	                R.layout.listview_item_row,//list item layout id
	                from,//Keys for input values
	                to//keys for output items
	                );
	        String[] imgList = new String[imgs.size()];
	        for(int j = 0;j<imgs.size();j++) {
	        	imgList[j] = imgs.get(j);
	        }
	        adapter=new ImageAdapter(this,imgList,names,distance);
	        //setting Adapter to ListView
	       list.setAdapter(adapter);
	       
	        list.setOnItemClickListener(new OnItemClickListener() {
	        	   public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
	        	      Object listItem = list.getItemAtPosition(position);
	        	      Object item = list.getSelectedItem();
	        	      ToggleButton tog = (ToggleButton) findViewById(R.id.toggleButton1);
	        	      tog.toggle();
	        	      testListener(position);
	        	      //addMarkerAtCurrentLocation((String) listItem);
	        	   } 
	        	});
	    }
	  public void testListener(int pos) {
	
		  plotRoute(names.get(pos),lng.get(pos),lat.get(pos),distance.get(pos));;
		 
	  }
	  //Function to plot route on map between 2 coordinates
	 public void plotRoute(String dest,double lng,double lat,double dist) {
		// switchView(null);
			MapView mapView = (MapView) findViewById(R.id.MapView);
			  ListView list = (ListView)findViewById(R.id.FriendList);
		 if(mapView.getVisibility()==View.GONE) {
			  mapView.setVisibility(View.VISIBLE);

				list.setVisibility(View.GONE);
		  }
		// MapView mapView = (MapView) findViewById(R.id.MapView);
	    	mapView.invalidate();
	    	mapView.getOverlays().clear();
	    	
	    	String url = "http://maps.googleapis.com/maps/api/directions/json?origin="+gps.getLatitude()+","+gps.getLongitude()+"&destination="+lat+","+lng+"&sensor=true";
	    	try {
	    	HttpPost httppost = new HttpPost(url);
	    	  HttpClient httpclient = new DefaultHttpClient();
	    	HttpResponse response = httpclient.execute(httppost);
	    	HttpEntity entity = response.getEntity();
	    	InputStream is = null;
	    	is = entity.getContent();
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
	    	StringBuilder sb = new StringBuilder();
	    	sb.append(reader.readLine() + "\n");
	    	String line = "0";
	    	while ((line = reader.readLine()) != null) {
	    	    sb.append(line + "\n");
	    	}
	    	is.close();
	    	reader.close();
	    	String result = sb.toString();
	    	JSONObject jsonObject = new JSONObject(result);
	    	JSONArray routeArray = jsonObject.getJSONArray("routes");
	    	JSONObject routes = routeArray.getJSONObject(0);
	    	JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
	    	String encodedString = overviewPolylines.getString("points");
	    	List<GeoPoint> pointToDraw = decodePoly(encodedString);

	    	//Added line:
	    	mapView.getOverlays().clear();
	    	addMarkerAtCurrentLocation("Source",gps.getLongitude(),gps.getLatitude(),0.0 );
	    	  float [] results = new float[2];
	    	 Location.distanceBetween( gps.location.getLatitude(),  gps.location.getLongitude(), lat, lng, results);
	    	 dist = results[0]/(1000*1.6);
	    	  double num = 0;
			    DecimalFormat df = new DecimalFormat("#.00");
	    	 String newNum = df.format(dist);
	    	 dist = Double.parseDouble(newNum);
	    	addMarkerAtCurrentLocation(dest,lng,lat,dist);
	    	mapView.getOverlays().add(new RoutePathOverlay(pointToDraw));
	    	
	    	/* MyLocationOverlay mMyLocationOverlay = new MyLocationOverlay(this, mapView);
	    	  mMyLocationOverlay.enableMyLocation();
	    	    mMyLocationOverlay.enableCompass();
	    	    mapView.getOverlays().add(mMyLocationOverlay);*/
	    	  
	    	
	    	}
	    	catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	  }
	 private List<GeoPoint> decodePoly(String encoded) {

		    List<GeoPoint> poly = new ArrayList<GeoPoint>();
		    int index = 0, len = encoded.length();
		    int lat = 0, lng = 0;

		    while (index < len) {
		        int b, shift = 0, result = 0;
		        do {
		            b = encoded.charAt(index++) - 63;
		            result |= (b & 0x1f) << shift;
		            shift += 5;
		        } while (b >= 0x20);
		        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		        lat += dlat;

		        shift = 0;
		        result = 0;
		        do {
		            b = encoded.charAt(index++) - 63;
		            result |= (b & 0x1f) << shift;
		            shift += 5;
		        } while (b >= 0x20);
		        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		        lng += dlng;

		        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
		        poly.add(p);
		    }

		    return poly;
		}
	 
	 public void addMarkerAtCurrentLocation(String name,double lng,double lati,double dist) {

			//gps = new GPS(this);
	    	MapView mapView = (MapView) findViewById(R.id.MapView);
	    	mapView.invalidate();
	    	
	        mapView.setBuiltInZoomControls(true);
	        final MapController mControl = mapView.getController();
	        
	        List<Overlay> mapOverlays = mapView.getOverlays();
	        Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
	        if(name.equalsIgnoreCase("Source")) {
	        	 drawable = this.getResources().getDrawable(R.drawable.markerblue);
	        }
	       
	        MapOverlay itemizedoverlay = new MapOverlay(this, drawable);
	        int lat = (int) (lati * 1E6);
	        int lon = (int) (lng * 1E6);
	       
	        GeoPoint point = new GeoPoint(lat,lon);
	       // OverlayItem overlayitem = new OverlayItem(point, name, "I'm heresss!");
	        
	      //  itemizedoverlay.removeOverlay(mapOverlays,overlayitem);
	        itemizedoverlay.addMarker(name,dist, point);
	        mapOverlays.add(itemizedoverlay);
	        mControl.setCenter(point);
	        mControl.setZoom(10);
	     


		}
		public void switchView(View v) {
			//ImageButton imgButton = (ImageButton) findViewById(R.id.button2);
			
			MapView mapView = (MapView) findViewById(R.id.MapView);
			  ListView list = (ListView)findViewById(R.id.FriendList);
			  if(mapView.getVisibility()==View.GONE) {
				  mapView.setVisibility(View.VISIBLE);
				//  imgButton.setImageResource(R.drawable.back_button);
					//imgButton.refreshDrawableState();
					placeAllMarkers();
			  }
			  else if(mapView.getVisibility()==View.VISIBLE)
				  mapView.setVisibility(View.GONE);
			  if(list.getVisibility()==View.GONE) {
				  list.setVisibility(View.VISIBLE);
				//  imgButton.setImageResource(R.drawable.maps_icon);
					//imgButton.refreshDrawableState();
			  }
			  else  if(list.getVisibility()==View.VISIBLE)
				  list.setVisibility(View.GONE);
			  
		}
		public void placeAllMarkers() {
			MapView map = (MapView) findViewById(R.id.MapView);
			map.getOverlays().clear();
			addMarkerAtCurrentLocation("Source",gps.getLongitude(),gps.getLatitude(),0.0);
			 for(int i = 0;i<lng.size();i++)
		         	addMarkerAtCurrentLocation(names.get(i),lng.get(i),lat.get(i),distance.get(i));
			 
		}
	//Separate thread for background processing	
		public class NewThread extends AsyncTask<String, String, String> {

			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(NearMe.this);
				pDialog.setMessage("Page loading. Please wait...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(false);
				pDialog.show();
				gps = new GPS(NearMe.this);
			}

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				fetchJson();
				return null;
			}
			
			protected void onPostExecute(String str) {
				setViews();
				pDialog.dismiss();	
			}
			
		}
	
}
