package com.gdg.csub1;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
//import android.widget.Toast;
//import android.widget.Toast;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;

public class ListBuildingsActivity extends ListActivity {
    private int position;
    AlertDialog dialog;
    EditText rename_et;
    ListView listView;
    private List<String> list_places;
    private CompassActivity ca;
    public double glat=0.0, glon=0.0, galtitude=0.0;
    int locationReady=0;
    private LocationManager mlocManager;
    private LocationListener mlocListener;
    public static Location my_loc = new Location("dummyprovider");
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Called when the activity is first created.
        super.onCreate(savedInstanceState);
        Csub1Activity.activity = "csub_places";
        ca = new CompassActivity();
		String[] places = getResources().getStringArray(R.array.campus_buildings);
        String[] dlat = getResources().getStringArray(R.array.campus_lat);
		String[] dlon = getResources().getStringArray(R.array.campus_lon);
    	int len = dlat.length;
        list_places = new ArrayList<String>();
		Csub1Activity.places.clear();
		for (int i=0; i<len; i++) {
			Csub1Activity.places.add(new Csub1Activity.myPlace(places[i],Double.valueOf(dlat[i]), Double.valueOf(dlon[i])));
            list_places.add(places[i]);
		}
        Csub1Activity.nList = len;
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main2, list_places));
		listView = getListView();
		listView.setTextFilterEnabled(true);
        registerForContextMenu(listView);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			//@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				position=arg2;
				return false;
			}
		});
        mlocManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Context Menu");
		menu.add(0, v.getId(), 0, "Cancel");
		menu.add(0, v.getId(), 0, "Sort");
	}

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle()=="Cancel") { function2(item.getItemId()); return true; }
        if (item.getTitle()=="Sort"){
            dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle("Please choose how:");
            final MenuItem f_item = item;
            dialog.setButton(DialogInterface.BUTTON1, "SORT BY NAME", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    function4(f_item.getItemId());
                    dialog.dismiss();
                }
            });

            dialog.setButton(DialogInterface.BUTTON2, "SORT BY DISTANCE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    function5(f_item.getItemId());
                    dialog.dismiss();
                }
            });
            //dialog.getButton(DialogInterface.BUTTON2).setEnabled(false);
            dialog.show();
        }
    return false;
    }

    public void function2(int id) {
        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
    }

     //SORT BY NAME:
    public void function4(int id) {
        Collections.sort(Csub1Activity.places,new ListYourPlacesActivity.PlaceComparator());
        Toast.makeText(this, "Sorted by name", Toast.LENGTH_SHORT).show();
        list_places.clear();
        for (int i=0;i<Csub1Activity.nList;i++){
            list_places.add(Csub1Activity.places.get(i).place);
        }
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main2, list_places));
    }
    //SORT BY DISTANCE:
    public void function5(int id) {
        Collections.sort(Csub1Activity.places,new ListYourPlacesActivity.DistanceComparator());
        Float distance = 0.0f;
        if (locationReady == 1){
            my_loc.setLatitude(glat);
            my_loc.setLongitude(glon);
            Toast.makeText(this, "Sorted by distance FROM YOUR LOCATION", Toast.LENGTH_SHORT).show();
        }
        else{
            //CSUB LOCATION: 9001 STOCKDALE HWY, BAKERSFIELD, CA:
            //EG W.S. Library = 35.351405 lat & -119.103109 lon
            my_loc.setLatitude(35.351400);
            my_loc.setLongitude(-119.103100);
            Toast.makeText(this, "Sorted by distance FROM YOUR CSUB", Toast.LENGTH_SHORT).show();
        }
        Location loc = new Location("dummyprovider");
        list_places.clear();
        for (int i=0;i<Csub1Activity.nList;i++){
           list_places.add(Csub1Activity.places.get(i).place);
           System.out.println(Csub1Activity.places.get(i).place);
            System.out.println("dlat="+Csub1Activity.places.get(i).dlat + ", dlon="+Csub1Activity.places.get(i).dlon);
            loc.setLatitude(Csub1Activity.places.get(i).dlat);
            loc.setLongitude(Csub1Activity.places.get(i).dlon);
            distance = my_loc.distanceTo(loc);
           Toast.makeText(this,Csub1Activity.places.get(i).place + " distance="+distance,Toast.LENGTH_LONG).show();
        }
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main2, list_places));
    }

	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
	    //Jump to CompassActivity!
	    Intent intent = new Intent(); 
	    intent.setClass(getApplicationContext(), CompassActivity.class);
	    Csub1Activity.currentSelection = position;
		startActivity(intent); 
	}

    class MyLocationListener implements LocationListener {
        //@Override
        public void onLocationChanged(Location loc) {
            glat = loc.getLatitude();
            glon = loc.getLongitude();
            locationReady = 1;
            //dialog.getButton(DialogInterface.BUTTON2).setEnabled(true);
        }
        //@Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        //@Override
        public void onProviderDisabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }
        //@Override
        public void onProviderEnabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }
    }
}
