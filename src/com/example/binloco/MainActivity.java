package com.example.binloco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * MainActivity Class that allows the user to decide which bin to search for.
 * BinLoco
 * ICS4UP
 * @author Adit, Daniel, Vanshil
 * @version April 14, 2015
 */
public class MainActivity extends Activity {
	
	public static int searchType = 1;
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main );
	}

	/**
	 * Searches the 5 closest Garbage bins from your location if Garbage search is selected
	 */
	public void garSearch(View view){
		searchType = Bin.LitterMask;
		Intent i = new Intent(this, LocationActivity.class);
		this.startActivity(i);
	}
	/**
	 * Searches the 5 closest Recycle bins from your location if Recycle search is selected
	 */
	public void recSearch(View view){
		searchType = Bin.RecyclingMask;
		Intent i = new Intent(this, LocationActivity.class);
		this.startActivity(i);
	}
	
}
 