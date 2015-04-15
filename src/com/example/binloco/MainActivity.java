package com.example.binloco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends Activity {
	public static int searchType = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main );
	}

	public void garSearch(View view){
		searchType = Bin.LitterMask;
		Intent i = new Intent(this, LocationActivity.class);
		this.startActivity(i);
	}
	public void recSearch(View view){
		searchType = Bin.RecyclingMask;
		Intent i = new Intent(this, LocationActivity.class);
		this.startActivity(i);
	}
	
}
 