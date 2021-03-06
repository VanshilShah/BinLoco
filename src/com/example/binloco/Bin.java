package com.example.binloco;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
/**
 * Bin Class used to store the bin data in an object.
 * BinLoco
 * ICS4UP
 * @author Adit, Daniel, Vanshil
 * @version April 14, 2015
 */
public class Bin implements Comparable<Object>{
	private LatLng loc;
	private int types;
	public static final int LitterMask = 0B001, RecyclingMask = 0B010, GreenMask = 0B100, PersonMask = 0B1000;
	public static final float LitterColour = BitmapDescriptorFactory.HUE_GREEN, RecyclingColour = BitmapDescriptorFactory.HUE_BLUE, GreenColour = BitmapDescriptorFactory.HUE_YELLOW, PersonColour = BitmapDescriptorFactory.HUE_RED;
	public static final float [] Colours = {0, LitterColour, RecyclingColour, 0, GreenColour, 0, 0, 0, PersonColour};
    private static int distAlgrthm = 1;
	
	public Bin(LatLng loc, int types)
	{
		this.loc = loc;
		this.types = types;		
	}
	
	@Override
	public int compareTo(Object other) {
		Bin b2 = (Bin)other;
		return (int) Math.copySign(1, LocationActivity.myLocation.getDist(this, 1)-LocationActivity.myLocation.getDist(b2, 1));
	}
	
	
	
	/**
	 * Calculates the approximate distance between this point, and another point. <br>
	 * Points should be in the form x = latitude, y = longitude
	 * @param p2
	 * @param technique
	 * 		controls which technique is used to calculate distance.
	 * 		0 will use pythagorean, 1 haversine, 2 Spherical law of cosines 
	 * @return distance
	 */
    public double getDist(Bin p2, int technique) {return getDist(p2.getLatLng(), technique);}

    /**
	 * Calculates the approximate distance between this point, and another point. <br>
	 * Points should be in the form x = latitude, y = longitude
	 * @param p2
	 * @param technique
	 * 		controls which technique is used to calculate distance.
	 * 		0 will use pythagorean, 1 haversine, 2 Spherical law of cosines 
	 * @return distance
	 */
	public double getDist(LatLng p2, int technique)
	{
		int R = 6371000; // metres
		double lat1 = Math.toRadians(this.getLat()), lat2 = Math.toRadians(p2.latitude);
		double deltaLat = Math.toRadians(lat2-lat1);
		double lon1 = Math.toRadians(this.getLon()), lon2 = Math.toRadians(p2.longitude);
		double deltaLon = Math.toRadians(lon2-lon1);
		
		double d = 0;
		switch(technique){
			case 0: //pythagorean
			{
				double x = (lon2-lon1) * Math.cos((lat1+lat2)/2);
				double y = (lat2-lat1);
				d = Math.sqrt(x*x + y*y) * R;
				break;
			}
			case 1: //haversine
			{
				double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
				        Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
				double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
				d = R * c;
				break;
			}
			case 2: //Spherical Law of Cosines
			{
				d = Math.acos( Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2) * Math.cos(deltaLon) ) * R;
			}
		}
		return d;
	}
	
	/**
	 * generates a MarkerOptions object at this location, with a specified title and bin type to use for colouring
	 * @param title the pin title
	 * @param type the bin type to associate the marker colour with 
	 * @return a MarkerOptions object
	 */
	public MarkerOptions makeMarker(String title, int type)
	{
		return new MarkerOptions()
    	.position(this.getLatLng())
    	.title(title)
    	.icon(BitmapDescriptorFactory.defaultMarker(Colours[type]));
	}
	
	/**
	 * getter for the user's current latitude coordinate
	 * @return coordinate
	 */
	public double getLat() { return loc.latitude; }
	
	/**
	 * getter for the user's current longitude coordinate
	 * @return coordinate
	 */
	public double getLon() { return loc.longitude; }
	
	/**
	 * getter for the user's current location coordinates
	 * @return coordinate(s)
	 */
	public LatLng getLatLng() { return loc; }

	/**
	 * getter for the type of bin
	 * @return types
	 */
	public int getTypes() {return types; }
	
	/**
	 * Method used to identiy if bin is a garbage bin or not
	 * @return types, LitterMask
	 */
	public boolean isLitter() { return (types&LitterMask) != 0; }
	/**
	 * Method used to identiy if bin is a recycling bin or not
	 * @return types, RecyclingMask
	 */
	public boolean isRecycling() { return (types&RecyclingMask) != 0; }
	/**
	 * Method used to identiy if bin is a green bin or not
	 * @return types, GreenMask
	 */
	public boolean isGreenBin() { return (types&GreenMask) != 0; }
	/**
	 * Method used to identiy if tag is the user or not
	 * @return types, RecyclingMask
	 */
	public boolean isPerson() { return (types&PersonMask) != 0; }

	/**
	 * Setter to set the Distance Algorithm 
	 * @return distAlgrthm
	 */
    public static void setDistAlgrthm(int distAlgrthm) { Bin.distAlgrthm = distAlgrthm; }
    /**
	 * Getter to get the Distance Algorithm 
	 * @return distAlgrthm
	 */
    public static int getDistAlgrthm() { return distAlgrthm; }
}
