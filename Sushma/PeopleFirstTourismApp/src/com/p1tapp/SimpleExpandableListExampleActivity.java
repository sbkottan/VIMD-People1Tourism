package com.p1tapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.simpleexpandablelistexample.R;
 
import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import android.app.ListActivity;
import android.os.AsyncTask;

import android.util.Log;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/*-------------------------------------
 * This code was developed by Sushma Sivakumar
 */
 
public class SimpleExpandableListExampleActivity extends ExpandableListActivity {
	@SuppressWarnings("unchecked")
	public void setAdapter() {
		 SimpleExpandableListAdapter expListAdapter =
					new SimpleExpandableListAdapter(
							this,
							createGroupList(), 				// Creating group List.
							R.layout.group_row,				// Group item layout XML.			
							new String[] { "Group Item" },	// the key of group item.
							new int[] { R.id.row_name },	// ID of each group item.-Data under the key goes into this TextView.					
							createChildList(),				// childData describes second-level entries.
							R.layout.child_row,				// Layout for sub-level entries(second level).
							new String[] {"Sub Item"},		// Keys in childData maps to display.
							new int[] { R.id.grp_child}		// Data under the keys above go into these TextViews.
						);
		        
		       
		        
					setListAdapter( expListAdapter );
	}
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
    	try{
    		 super.onCreate(savedInstanceState);
    		 setContentView(R.layout.main);
    		 
    		 new NewThread().execute((String[]) null);
        
       		// setting the adapter in the list.
			
			
			
    	}catch(Exception e){
    		System.out.println("Errrr +++ " + e.getMessage());
    	}
    }
    
	JSONObject json;
	JSONObject  dashBoardObj;
	JSONArray historyArr;
	JSONArray offerArr;
	JSONArray scheduleArr;
	
	/*-----------------------------
	 * This function was written by Spurthi Kottan
	 * -------
	 */
	 public void mapview() 
		{
		
			try{
	        	json = JSONfunctions.getJSONfromURL("http://dev.oscar.ncsu.edu:9991/mobileapi/dashboard/");
	        	dashBoardObj = (JSONObject) json.get("dashboard");
	        	historyArr =  dashBoardObj.getJSONArray("history");
	        	offerArr = dashBoardObj.getJSONArray("offer");
	        	scheduleArr =  dashBoardObj.getJSONArray("schedule");
				}catch(JSONException e)        {
	        	 Log.e("log_tag", "Error parsing data "+e.toString());
				}  
			
		}		
		
	/*----------------------
	 * This function was written by Spurthi Kottan	
	 */
		
		public class NewThread extends AsyncTask<String, String, String>
		{
			protected void onPreExecute() {
				super.onPreExecute();
			}
			@Override
			protected String doInBackground(String... params){
				
			 mapview();
			 return null;
			}
			
			protected void onPostExecute(String str) {
				setAdapter();
			}
		}	
		
	/* Creating the Hashmap for the row */
	
	
	/*
	 * The remaining code in this file was developed by Sushma Sivakumar
	 */
	
	@SuppressWarnings("unchecked")
	private List createGroupList() {
	  	  ArrayList result = new ArrayList();
	  	  
	  	HashMap m1 = new HashMap();
  	    m1.put( "Group Item","My Offers" ); // the key and it's value.
  		result.add( m1 );
  		HashMap m2 = new HashMap();
  	    m2.put( "Group Item","My Schedule " ); // the key and it's value.
  		result.add( m2 );
  		HashMap m3 = new HashMap();
  	    m3.put( "Group Item","My History" ); // the key and it's value.
  		result.add( m3 );
  		
  		
	  	return (List)result;
    }
    
	/* creatin the HashMap for the children */
    @SuppressWarnings("unchecked")
	private List createChildList() {
    	ArrayList result = new ArrayList();
    	try
    	{
    	
    	
    	ArrayList secList1 = new ArrayList(); 
    	if(offerArr==null||offerArr.length()==0)
    	{
    		HashMap child1 = new HashMap();
      		child1.put( "Sub Item", "You have no offers to display at this time");    	    
      		secList1.add( child1 );
    	}
    	else
    	{
    	for(int j=0;j<offerArr.length();j++)
      	{
    		String status1 = null;
      		String offer = offerArr.getJSONObject(j).getString("service");
      		String status = offerArr.getJSONObject(j).getString("status");
      		if(status.equals("A"))
      		{
      			status1="Accepted";
      		}
      		if(status.equals("P"))
      		{
      			status1="Pending";
      		}
      		if(status.equals("D"))
      		{
      			status1="Declined";
      		}
      		if(status.equals("C"))
      		{
      			status1="Cancelled";
      		}
      		HashMap child1 = new HashMap();
      		child1.put( "Sub Item", offer+"\nStatus:"+status1);    	    
      		secList1.add( child1 );
      	} 
    	}
    	result.add( secList1 );
    	
    	
    	ArrayList secList2 = new ArrayList();
    	if(scheduleArr==null||scheduleArr.length()==0)
    	{
    		HashMap child22 = new HashMap();
      		child22.put( "Sub Item", "You have no services scheduled at this time" );    	    
      		secList2.add( child22 );
    	}
    	else
    	{
    	for(int k=0;k<scheduleArr.length();k++)
    	{
    		String schedule = scheduleArr.getJSONObject(k).getString("service");
    		String time = scheduleArr.getJSONObject(k).getString("time");
    		String longitude = scheduleArr.getJSONObject(k).getString("longitude");
    		String latitude = scheduleArr.getJSONObject(k).getString("latitude");
    		String date = scheduleArr.getJSONObject(k).getString("date");
    		
    		HashMap child22 = new HashMap();
      		child22.put( "Sub Item", schedule+"\nDate: "+date+"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tTime:"+time );    	    
      		secList2.add( child22 );
    	}
    	}
    	result.add( secList2 );
    	
    	
    	ArrayList secList3 = new ArrayList(); 
    	if(historyArr==null||historyArr.length()==0)
    	{
    		HashMap child111 = new HashMap();
      		child111.put( "Sub Item", "You have no history to display");    	    
      		secList3.add( child111 );
    	}
    	else
    	{
    	  	for(int i=0;i< historyArr.length();i++) {
    		String service = historyArr.getJSONObject(i).getString("service");
    		String date =  historyArr.getJSONObject(i).getString("date");
    		
    		HashMap child111 = new HashMap();
      		child111.put( "Sub Item", service+" \nDate:"+date);    	    
      		secList3.add( child111 );
    	}
    	}
    	result.add( secList3 );
    	
    	  	 
    	
    	}
    	catch(JSONException e){
    		Log.e("log_tag", "Error parsing data "+e.toString());
    	}
    	return (List)result;
    }
    public void  onContentChanged  () {
    	System.out.println("onContentChanged");
	    super.onContentChanged();	      
    }
    /* This function is called on each child click */
    public boolean onChildClick( ExpandableListView parent, View v, int groupPosition,int childPosition,long id) {
    	System.out.println("Inside onChildClick at groupPosition = " + groupPosition +" Child clicked at position " + childPosition);
    	return true;
    }

    /* This function is called on expansion of the group */
    public void  onGroupExpand  (int groupPosition) {
    	try{
    		 System.out.println("Group exapanding Listener => groupPosition = " + groupPosition);
    	}catch(Exception e){
    		System.out.println(" groupPosition Error +++ " + e.getMessage());
    	}
    } 
    
   
}