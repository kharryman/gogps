package com.gdg.csub1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Csub1Activity extends Activity implements OnClickListener {
	public static final String filename = "myplaces.txt";
	public static final int MAX_LIST = 64;
	public static List<myPlace> places, my_places;

	public static int currentSelection=0;
	public static Context ctx;
	public static int nList = 0;
	public static int nMyList = 0;
	public static String activity;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        //Called when the activity is first created.
        super.onCreate(savedInstanceState);
		places = new ArrayList<myPlace>();
		my_places = new ArrayList<myPlace>();
        setContentView(R.layout.main);
        ctx = getApplicationContext();
	}

	public void onClick(View v) { }

	public void onClick1(View v) {
		//Toast.makeText(getApplicationContext(), "California State University, Bakersfield", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, ListBuildingsActivity.class);
		startActivity(intent);
	}

	public void onClick2(View v) {
		Intent intent = new Intent(this, ListBuildingsActivity.class);
		startActivity(intent);
	}

	public void onClick4(View v) {
		Intent intent = new Intent(this, ListYourMenuActivity.class);
		startActivity(intent);
	}

	public void onClick5(View v) {
		Toast.makeText(getApplicationContext(), "your car", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, ListYourCarActivity.class);
		startActivity(intent);
	}

	static public class myPlace {
		String place;
		double dlat;
		double dlon;

		public myPlace(String in_place, double in_lat, double in_lon) {
			this.place = in_place;
			this.dlat = in_lat;
			this.dlon = in_lon;
		}
	}
}
