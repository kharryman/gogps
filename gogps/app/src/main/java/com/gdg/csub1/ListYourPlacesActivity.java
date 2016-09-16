package com.gdg.csub1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;

public class ListYourPlacesActivity extends ListActivity {
	int saveFlag=0;
	ListView listView;
	int position;
	private List<String> list_places;
	private ArrayAdapter<String> mAdapter;
	AlertDialog dialog, dialog2;
	EditText rename_et;
    private LocationManager mlocManager;
    private LocationListener mlocListener;
    private static CompassActivity ca;
	public static double glat=0.0, glon=0.0, galtitude=0.0;
	public static int locationReady=0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Csub1Activity.activity = "your_places";
		Csub1Activity.nMyList = readPlacesFile();
		list_places = new ArrayList<String>();
		for (int i=0;i<Csub1Activity.nMyList;i++){
			list_places.add(Csub1Activity.my_places.get(i).place);
		}
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_places);
		setListAdapter(mAdapter);
		listView = getListView();
		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView);
		listItemClicked();
		mlocManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		mlocListener = new MyLocationListener();
		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
	}

	private void listItemClicked() { 
		// TODO Auto-generated method stub 
		listView.setOnItemLongClickListener(new OnItemLongClickListener() { 
			//@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
				// TODO Auto-generated method stub
				System.out.println("ITEM LONG CLICKED");
				position=arg2;
				return false; 
			}
		}); 
	} 
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);  
		menu.setHeaderTitle("Context Menu");  
		menu.add(0, v.getId(), 0, "Delete");  
		menu.add(0, v.getId(), 0, "Cancel");
		menu.add(0, v.getId(), 0, "Rename");
		menu.add(0, v.getId(), 0, "Sort");
	}  

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle()=="Delete") { function1(item.getItemId()); return true; }
		if (item.getTitle()=="Cancel") { function2(item.getItemId()); return true; }
		if (item.getTitle()=="Rename") {
			dialog = new AlertDialog.Builder(this).create();
			dialog.setTitle("Please rename:");
			rename_et = new EditText(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			rename_et.setLayoutParams(lp);
			dialog.setView(rename_et);
			final MenuItem f_item = item;
			rename_et.setText(Csub1Activity.my_places.get(position).place);
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, "RENAME", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface, int i) {
							function3(f_item.getItemId());
							dialog.dismiss();
						}
			       });
			dialog.show();
		}
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
	
	public void function1(int id) {
		String s = mAdapter.getItem(position);
		//Toast.makeText(this, "function 1 called:"+position, Toast.LENGTH_SHORT).show();  
		Toast.makeText(this, "Deleting: "+s, Toast.LENGTH_SHORT).show();  
		//remove array element here...
		//rewrite the file, but skip the deleted element.
		if (rewriteFile(position)) finish();
		finish();
	}
	public void function2(int id) {
		Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
	}

	public void function3(int id) {
		Csub1Activity.my_places.get(position).place = rename_et.getText().toString();
		list_places.clear();
		for (int i=0;i<Csub1Activity.nMyList;i++){
			list_places.add(Csub1Activity.my_places.get(i).place);
			System.out.println(Csub1Activity.my_places.get(i).place);
		}
		//Dont change anything..just the name....
		rewriteFile(-1);
		Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
	}
    //SORT BY NAME:
    public void function4(int id) {
        Collections.sort(Csub1Activity.my_places,new PlaceComparator());
        Toast.makeText(this, "Sorted by name", Toast.LENGTH_SHORT).show();
		list_places.clear();
		for (int i=0;i<Csub1Activity.nMyList;i++){
			list_places.add(Csub1Activity.my_places.get(i).place);
			System.out.println(Csub1Activity.my_places.get(i).place);
		}
        rewriteFile(-1);

    }
    //SORT BY DISTANCE:
    public void function5(int id) {
        Collections.sort(Csub1Activity.places,new DistanceComparator());
		Toast.makeText(this, "Sorting by distance", Toast.LENGTH_SHORT).show();
		Float distance = 0.0f;
		Location my_loc = new Location("dummyprovider");
		if (locationReady == 1){
			my_loc.setLatitude(glat);
			my_loc.setLongitude(glon);
		}
		else{
			//CSUB LOCATION: 9001 STOCKDALE HWY, BAKERSFIELD, CA:
			my_loc.setLatitude(35.3514);
			my_loc.setLongitude(-119.1031);
		}
		Location loc = new Location("dummyprovider");
		list_places.clear();
		for (int i=0;i<Csub1Activity.nMyList;i++){
			list_places.add(Csub1Activity.my_places.get(i).place);
			System.out.println(Csub1Activity.my_places.get(i).place);
			System.out.println("dlat="+Csub1Activity.my_places.get(i).dlat + ", dlon="+Csub1Activity.my_places.get(i).dlon);
			loc.setLatitude(Csub1Activity.my_places.get(i).dlat);
			loc.setLongitude(Csub1Activity.my_places.get(i).dlon);
			distance = my_loc.distanceTo(loc);
			Toast.makeText(this,Csub1Activity.my_places.get(i).place + " distance="+distance,Toast.LENGTH_LONG).show();
		}
    }

	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		//Single press by user
		//Jump to CompassActivity!
	    Intent intent = new Intent();
	    intent.setClass(getApplicationContext(), CompassActivity.class);
	    Csub1Activity.currentSelection = position;
		startActivity(intent);
	}

	int readPlacesFile() {
		int n=0;
		//read file from sd card.
		try {
			File myFile = new File(getExternalFilesDir(null), Csub1Activity.filename);
			if (!myFile.exists()) {
				Toast.makeText(getBaseContext(), "file not found", Toast.LENGTH_SHORT).show();
				return 0;
			}
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader( new InputStreamReader(fIn) );
			String place="", line="";
			Double lat=0.0,lon=0.0;
			//

			Csub1Activity.my_places.clear();
			while(true) {
				line = myReader.readLine();
				if (line==null){
					break;
				}
				place = line;
				line = myReader.readLine();
				if (line==null){
					break;
				}
				lat = Double.parseDouble(line);
				line = myReader.readLine();
				if (line==null){
					break;
				}
				lon = Double.parseDouble(line);
				Csub1Activity.myPlace my_place = new Csub1Activity.myPlace(place,lat,lon);
				Csub1Activity.my_places.add(my_place);
				n++;
				if (n >= Csub1Activity.MAX_LIST) break;
			}
			myReader.close();
			//Toast.makeText(getBaseContext(), "read " + nlines + " lines", Toast.LENGTH_SHORT).show();
		}
		catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return n;
	}

	boolean rewriteFile(int pos) {
		//Reduce size of array by one
		if (pos!=-1) {
			Csub1Activity.nMyList--;
			Csub1Activity.my_places.remove(pos);
		}
		//rewrite the file.
		FileOutputStream fop = null;
		File file;
		try {
			file = new File(getExternalFilesDir(null), Csub1Activity.filename);
			fop = new FileOutputStream(file,false); //true=append
			//if (!file.exists()) { file.createNewFile(); }
	        //
			if (Csub1Activity.nMyList == 0) {
				file.delete();
				return true;
			}
			String str = "";
			for (int i=0; i<Csub1Activity.nMyList; i++) {
				str = Csub1Activity.my_places.get(i).place+"\n";
				fop.write(str.getBytes());
				str = Double.toString(Csub1Activity.my_places.get(i).dlat)+"\n";
				fop.write( str.getBytes() );
				str = Double.toString(Csub1Activity.my_places.get(i).dlon)+"\n";
				fop.write( str.getBytes() );
			}
			fop.flush();
			fop.close();
			Toast.makeText(getBaseContext(), "file saved", Toast.LENGTH_SHORT).show();
		}
		catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return false;
	}


    public static class PlaceComparator implements Comparator
    {
        public int compare(Object obj1, Object obj2)
        {
            String place1 = ((Csub1Activity.myPlace) obj1).place;
            String place2 = ((Csub1Activity.myPlace) obj2).place;
            return place1.compareTo(place2);
        }
    }//

    public static class DistanceComparator implements Comparator    {

        public int compare(Object obj1, Object obj2)
        {
			CompassActivity ca = new CompassActivity();
            Double lat1 = ((Csub1Activity.myPlace) obj1).dlat;
            Double lon1 = ((Csub1Activity.myPlace) obj1).dlon;

            Double lat2 = ((Csub1Activity.myPlace) obj2).dlat;
            Double lon2 = ((Csub1Activity.myPlace) obj2).dlon;

    		Location my_loc = new Location("dummyprovider");
			if (locationReady == 1){
				my_loc.setLatitude(glat);
				my_loc.setLongitude(glon);
			}
			else{
				//CSUB LOCATION: 9001 STOCKDALE HWY, BAKERSFIELD, CA:
				my_loc.setLatitude(35.3514);
				my_loc.setLongitude(-119.1031);
			}



			Location loc1 = new Location("dummyprovider");
			Location loc2 = new Location("dummyprovider");
            loc1.setLatitude(lat1);
            loc1.setLongitude(lon1);
            loc2.setLatitude(lat2);
            loc2.setLongitude(lon2);
            Float distance1 = my_loc.distanceTo(loc1);
            Float distance2 = my_loc.distanceTo(loc2);

            return distance1.compareTo(distance2);
        }
    }//

	class MyLocationListener implements LocationListener {
		public Location my_loc;
		//@Override
		public void onLocationChanged(Location loc) {
			my_loc = loc;
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
