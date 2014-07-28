/*Dashboard - Created by Sushma Sivakumar and Spurthi Kottan
 * This class handles the creation of the dashboard and populating the list with information pertaining
 * to the user
 */
package com.vimd.p1t;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.example.androidtablayout.R;

public class Dashboard extends ExpandableListActivity {
	 ProgressDialog pDialog;
	ArrayList<String> services;
	ArrayList<String> lat;
	ArrayList<String> longi;
	public void setAdapter() {
		SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(
				this, createGroupList(), // Creating group List.
				R.layout.group_row, // Group item layout XML.
				new String[] { "Group Item" }, // the key of group item.
				new int[] { R.id.row_name }, // ID of each group item.-Data
												// under the key goes into this
												// TextView.
				createChildList(), // childData describes second-level entries.
				R.layout.child_row, // Layout for sub-level entries(second
									// level).
				new String[] { "Sub Item" }, // Keys in childData maps to
												// display.
				new int[] { R.id.grp_child } // Data under the keys above go
												// into these TextViews.
		);

		setListAdapter(expListAdapter);
		ExpandableListView list = getExpandableListView();
		
		//list.expandGroup(0);
		list.expandGroup(1);
		//list.expandGroup(2);
	}
	public void Logout(View v) {
		 String filePath = this.getApplicationContext().getFilesDir().getPath().toString() + "/user.txt";
		   File file = new File(filePath);
			//FileInputStream in = openFileInput("user.txt");	
			 OutputStream out;
			try {
				out = new FileOutputStream(file,false);
				EditText tv1 = (EditText) findViewById(R.id.editText1);
				OutputStreamWriter op = new OutputStreamWriter(out);
			//	FileWriter fileWriter =   new FileWriter("user.txt");
				BufferedWriter bufferedWriter =    new BufferedWriter(op);
				//bufferedWriter.write("");
				
				//bufferedWriter.write(" ");
				bufferedWriter.write("");
				bufferedWriter.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//FileOutputStream fp = openFileOutput("user.txt", 0);
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Intent intent  = new Intent(this,Login.class);
			startActivityForResult(intent, RESULT_OK);
			finish();
		
	}
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.dashboardmain);
			Intent intent = getIntent();
		//	intent.getI
		//	Intent extras = intent.get
			// ExpandableListView list = (ExpandableListView)
			// findViewById(R.id.list)
			new NewThread().execute((String[]) null);

			// setting the adapter in the list.

		} catch (Exception e) {
			System.out.println("Errrr +++ " + e.getMessage());
		}
	}

	JSONObject json;
	JSONObject dashBoardObj;
	JSONArray historyArr;
	JSONArray offerArr;
	JSONArray scheduleArr;

	public void mapview() {

		try {
			String filePath = this.getApplicationContext().getFilesDir().getPath().toString() + "/user.txt";
			json = JSONfunctions.getJSONfromURL("http://dev.oscar.ncsu.edu:9991/mobileapi/dashboard/",filePath);
			dashBoardObj = (JSONObject) json.get("dashboard");
			historyArr = dashBoardObj.getJSONArray("history");
			offerArr = dashBoardObj.getJSONArray("offer");
			scheduleArr = dashBoardObj.getJSONArray("schedule");
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}

	}

	public class NewThread extends AsyncTask<String, String, String> {
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Dashboard.this);
			pDialog.setMessage("Page loading. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {

			mapview();
			return null;
		}

		protected void onPostExecute(String str) {
			pDialog.dismiss();
			setAdapter();
			
		}
	}

	/* Creating the Hashmap for the row */
	@SuppressWarnings("unchecked")
	private List createGroupList() {
		ArrayList result = new ArrayList();

		HashMap m1 = new HashMap();
		m1.put("Group Item", "My Offers"); // the key and it's value.
		result.add(m1);
		HashMap m2 = new HashMap();
		m2.put("Group Item", "My Schedule "); // the key and it's value.
		result.add(m2);
		HashMap m3 = new HashMap();
		m3.put("Group Item", "My History"); // the key and it's value.
		result.add(m3);

		return (List) result;
	}

	/* creatin the HashMap for the children */
	@SuppressWarnings("unchecked")
	private List createChildList() {
		ArrayList result = new ArrayList();
		try {

			ArrayList secList1 = new ArrayList();
			for (int j = 0; j < offerArr.length(); j++) {
				String offer = offerArr.getJSONObject(j).getString("service");
				String status = offerArr.getJSONObject(j).getString("status");
				if(status.equals("A"))
					status = "Accepted";
				else if(status.equals("P"))
					status = "Pending";
				else if(status.equals("D"))
					status  = "Declined";
				else if(status.equals("F"))
					status = "Paid";
				else if(status.equals("C"))
					status = "Cancelled";
				HashMap child1 = new HashMap();
				child1.put("Sub Item", offer + "\nStatus:" + status);
				secList1.add(child1);
			}
			result.add(secList1);

			ArrayList secList2 = new ArrayList();
			services  = new ArrayList<String>();
			lat = new ArrayList<String>();
			longi = new ArrayList<String>();
			for (int k = 0; k < scheduleArr.length(); k++) {
				services.add(scheduleArr.getJSONObject(k).getString("service"));
				String schedule = scheduleArr.getJSONObject(k).getString(
						"service");
				String time = scheduleArr.getJSONObject(k).getString("time");
				
				String longitude = scheduleArr.getJSONObject(k).getString(
						"longitude");
				longi.add(longitude);
				String latitude = scheduleArr.getJSONObject(k).getString(
						"latitude");
				lat.add(latitude);
				String date = scheduleArr.getJSONObject(k).getString("date");

				HashMap child22 = new HashMap();
				child22.put("Sub Item", schedule + "\nDate: " + date
						+ "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tTime:" + time);
				secList2.add(child22);
			}
			result.add(secList2);

			ArrayList secList3 = new ArrayList();
			for (int i = 0; i < historyArr.length(); i++) {
				String service = historyArr.getJSONObject(i).getString(
						"service");
				String date = historyArr.getJSONObject(i).getString("date");

				HashMap child111 = new HashMap();
				child111.put("Sub Item", service + " \nDate:" + date);
				secList3.add(child111);
			}
			result.add(secList3);

		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		return (List) result;
	}

	public void onContentChanged() {
		System.out.println("onContentChanged");
		super.onContentChanged();
	}

	/* This function is called on each child click */
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if(groupPosition==1) {
			System.out.println("selected "+ services.get(childPosition));
			Intent intent = new Intent(this, Home.class);
			intent.putExtra("services",services.get(childPosition));
			intent.putExtra("longitude",longi.get(childPosition));
			intent.putExtra("latitude",lat.get(childPosition));
			startActivityForResult(intent, RESULT_OK);
			finish();
			
		}
		return true;
	}

	/* This function is called on expansion of the group */
	public void onGroupExpand(int groupPosition) {
		try {
			System.out.println("Group exapanding Listener => groupPosition = "
					+ groupPosition);
		} catch (Exception e) {
			System.out.println(" groupPosition Errrr +++ " + e.getMessage());
		}
	}

}