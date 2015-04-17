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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Activity class that handles the front end and back end of the loading of map data and display of it.
 * BinLoco
 * ICS4UP
 * @author Adit, Daniel, Vanshil
 * @version April 14, 2015
 */
public class LocationActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {	

	public static final boolean debugToasts = false;//Used to enable and disable debug information
	volatile boolean locationFound = false;

	static Bin myLocation = new Bin(new LatLng(0, 0), 0B1000);
	Bin [] closeBins = new Bin [5];

	Document [] docs = new Document[closeBins.length];
	
	GoogleMap map = null;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart(android.os.Bundle)
	 */
	@Override
	protected void onStart() {
		super.onStart();       
		updateUserLocation();
	}    
		
	/**
	 * Uses google APIs to update the known Location of the user.
	 */
	public void updateUserLocation(){
			
			LocationManager Lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			LocationListener Ll = new myLocationListener();
			Lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, Ll);
			
		}

	/* (non-Javadoc)
	 * @see com.google.android.gms.maps.OnMapReadyCallback#onMapReady(com.google.android.gms.maps.GoogleMap)
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		
		map = googleMap;
		map.setOnMarkerClickListener(this);
		
		if (closeBins[4] != null) {
			addMarkers();
		}		
	}
		
	/**
	 * Adds the markers of the found bins and user's location to the map.
	 */
	private void addMarkers() {
			
			map.addMarker(myLocation.makeMarker("You", Bin.PersonMask));
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation.getLatLng(), 15));
			
			for (int i = 0; i < closeBins.length; ++i) {
				map.addMarker(closeBins[i].makeMarker("Bin" + (i+1), MainActivity.searchType));
			}
			
		}
	
	/* (non-Javadoc)
	 * @see com.google.android.gms.maps.GoogleMap.OnMarkerClickListener#onMarkerClick(com.google.android.gms.maps.model.Marker)
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {
		
		Pattern pattern = Pattern.compile("Bin([0-9]+)");
		Matcher matcher = pattern.matcher(marker.getTitle());
		
		if (matcher.find()){
			startMaps(closeBins[Integer.parseInt(matcher.group(1))-1].getLatLng(), 'w');
		}
		
		return false;
	}
		
	/**
	 * Starts google maps through an intent loaded with the destination to navigate to.
	 * @param end The location of the bin that the user selected.
	 * @param mode The mode of travel towards the bin.
	 */
	public void startMaps(LatLng end, char mode){
			
			Uri gmmIntentUri = Uri.parse("google.navigation:q=" + end.latitude +"," + end.longitude +"&mode=" + mode);
			
			Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
			mapIntent.setPackage("com.google.android.apps.maps");
			
			startActivity(mapIntent);
		}

	
	/**
	 * A location listener that responds to updates in location.
	 * @author Adit, Daniel, Vanshil
	 */
	class myLocationListener implements LocationListener{

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
		 */
		@Override
		public void onLocationChanged(Location location) {
			
			if(location != null){
				double pLong = location.getLongitude();
				double pLat = location.getLatitude();

				myLocation = new Bin(new LatLng(pLat, pLong), 0B1000);

				if (!locationFound){
					
					locationFound = true;
					
					try {
						loadBins();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if (map != null) {
						addMarkers();
					}
				
				}
			}
		}
			
		/**
		 * Loads the bin data and figures out the 5 closest bins surrounding the user
		 * @throws IOException
		 */
		private void loadBins() throws IOException {
				
				Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]+),\\s(-[0-9]+\\.[0-9]+)\\stypes\\s=\\s([0-9]+)");
	
				String str = "";
				InputStream stream = getAssets().open("bins.txt");
	
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				if (stream != null) {
	
					int numberFound = 0;
	
					while((str = reader.readLine()) != null){
	
						str += "\n";
						Matcher matcher = pattern.matcher(str);
						matcher.find();
	
						double lat = Double.parseDouble(matcher.group(1)), lon = Double.parseDouble(matcher.group(2));
						int types = Integer.parseInt(matcher.group(3));
						
						numberFound += compareToCloseBins(numberFound, lat, lon, types);
						
					}
				}
				stream.close();
			}
				
		/**
		 * Compares The current bin found with the 5 closest bins known so far.
		 * @param numberFound
		 * @param lat
		 * @param lon
		 * @param types
		 * @return Whether or not a value must be added to numberFound
		 */
		private int compareToCloseBins(int numberFound, double lat, double lon, int types){
	
					Bin curBin = new Bin(new LatLng(lat, lon), types);
					
					if ((numberFound < closeBins.length || closeBins[closeBins.length - 1].compareTo(curBin) > 0) && (curBin.getTypes()&MainActivity.searchType) != 0) {
						
						int farthest = Math.min(closeBins.length - 1, numberFound);
						closeBins[farthest] = curBin;
	
						for (int j = farthest; j > 0 && closeBins[j - 1].compareTo(curBin) > 0; --j) {
							closeBins[j] = closeBins[j - 1];
							closeBins[j - 1] = curBin;
						}
						
						return 1;	
					}
					
					return 0;
				}
		
		/* (non-Javadoc)
		 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
		 */
		@Override
		public void onProviderEnabled(String provider) {}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
		 */
		@Override
		public void onProviderDisabled(String provider) {}
		
	}
	
}
