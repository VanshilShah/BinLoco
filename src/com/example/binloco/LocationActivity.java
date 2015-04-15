package com.example.binloco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {	
	
	 public static final int DOCUMENT_REQUEST_CODE = 0;

	    public static final boolean debugToasts = false;
	    
	    Bin [] closeBins = new Bin [5];
		static Bin myLocation = new Bin(new LatLng(0, 0), 0B1000);

	    GoogleMap map = null;

	    volatile boolean locFound = false;

	    Document [] docs = new Document[closeBins.length];

		private void loadBins() throws IOException {
	        Pattern p = Pattern.compile("([0-9]+\\.[0-9]+),\\s(-[0-9]+\\.[0-9]+)\\stypes\\s=\\s([0-9]+)");

	        String str = "";
	        InputStream is = getAssets().open("bins.txt");
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        if (is != null) {
	        	int nFound = 0;
	            for (int i = 0; (str = reader.readLine()) != null; ++i) {
	                str += "\n";
	                Matcher m = p.matcher(str);
	                m.find();

	                double lat = Double.parseDouble(m.group(1)), lon = Double.parseDouble(m.group(2));
	                int types = Integer.parseInt(m.group(3));

	                Bin curBin = new Bin(new LatLng(lat, lon), types);
	                if ((nFound < closeBins.length || closeBins[closeBins.length - 1].compareTo(curBin) > 0) && (curBin.getTypes()&MainActivity.searchType) != 0) {
	                    int farthest = Math.min(closeBins.length - 1, nFound);
	                    closeBins[farthest] = curBin;
	                    for (int j = farthest; j > 0 && closeBins[j - 1].compareTo(curBin) > 0; --j) {
	                        closeBins[j] = closeBins[j - 1];
	                        closeBins[j - 1] = curBin;
	                    }
	                    ++nFound;
	                }
	            }
	        }
	        is.close();
	    }

		@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_location);
	        if (debugToasts) {
	        	Toast.makeText(getBaseContext(), "Starting Load", Toast.LENGTH_LONG).show();
	        }
	        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
	      	mapFragment.getMapAsync(this);
		} 
		@Override
		protected void onStart() {
	        super.onStart();       
	        updateUserLocation();
		}


	    public void updateUserLocation(){
	         LocationManager Lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	         LocationListener Ll = new myLocationListener();
	         Lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, Ll);
	    }

	    @Override
	    public void onMapReady(GoogleMap googleMap) {
	        if (debugToasts) {
	        	Toast.makeText(getBaseContext(), "Map Ready", Toast.LENGTH_LONG).show();
	        }
	        map = googleMap;
	        map.setOnMarkerClickListener(this);
	        if (closeBins[4] != null) {
	            addMarkers();
	        }
	    }

	    private void addMarkers() {
	        if (debugToasts) {
	        	Toast.makeText(getBaseContext(), "Adding my location Marker", Toast.LENGTH_LONG).show(); }
	        map.addMarker(myLocation.makeMarker("You", Bin.PersonMask));
	        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation.getLatLng(), 15));
	     
	        if (debugToasts) {
	        	Toast.makeText(getBaseContext(), "Requesting documents", Toast.LENGTH_LONG).show(); }

	        for (int i = 0; i < closeBins.length; ++i) {
	        	map.addMarker(myLocation.makeMarker("Bin" + (i+1), MainActivity.searchType));
	        }

	        if (debugToasts) {
	            Toast.makeText(getBaseContext(), "Markers Added", Toast.LENGTH_LONG).show();
	        }
	    }

	    public void startMaps(LatLng end, char mode){
	        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + end.latitude +"," + end.longitude +"&mode=" + mode);
	        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
	        mapIntent.setPackage("com.google.android.apps.maps");
	        startActivity(mapIntent);
	    }

	    @Override
	    public boolean onMarkerClick(Marker marker) {
	        Pattern p = Pattern.compile("Bin([0-9]+)");
	        Matcher m = p.matcher(marker.getTitle());
	        if (m.find())
	        {
	            startMaps(closeBins[Integer.parseInt(m.group(1))-1].getLatLng(), 'w');
	        }
	        return false;
	    }

	    class myLocationListener implements LocationListener{

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				if(location != null){
					double pLong = location.getLongitude();
					double pLat = location.getLatitude();

					myLocation = new Bin(new LatLng(pLat, pLong), 0B1000);
					
	                if (!locFound)
	                {
	                    locFound = true;

	                    if (debugToasts)
	                    {
	                        Toast.makeText(getBaseContext(), "Found Location", Toast.LENGTH_LONG).show();
	                    }

	                    try {
	                        loadBins();
	                    } catch (IOException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
	                    if (map != null) {
	                        addMarkers();
	                    }
	                }
				}
	        }

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}

			@Override
			public void onProviderEnabled(String provider) {}

			@Override
			public void onProviderDisabled(String provider) {}
	    	
	    }
}
