package com.vimd.p1t;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vimd.peoplefirsttourism.R;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
public class Dashboard extends ExpandableListActivity {
	 private static final String NAME = "NAME";
	    private static final String IS_EVEN = "IS_EVEN";
	     
	    private ExpandableListAdapter mAdapter;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);
       // ExpandableListView view = (ExpandableListView) findViewById(R.id.l)
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
       /* for (int i = 0; i < 3; i++) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(NAME, "Item " + i);
            curGroupMap.put(IS_EVEN, (i % 2 == 0) ? "This group is even" : "This group is odd");
             
            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int j = 0; j < 5; j++) {
                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);
                curChildMap.put(NAME, "Child " + j);
                curChildMap.put(IS_EVEN, (j % 2 == 0) ? "Hello " + j: "Good Morning "+ j);
            }
            childData.add(children);
        }*/
       
        //for (int i = 0; i < 3; i++) {
            Map<String, String> curGroupMap1 = new HashMap<String, String>();
            groupData.add(curGroupMap1);
            curGroupMap1.put(NAME, "Offers");
           // curGroupMap.put(IS_EVEN, (i % 2 == 0) ? "This group is even" : "This group is odd");
             
            List<Map<String, String>> children1 = new ArrayList<Map<String, String>>();
            //for (int j = 0; j < 5; j++) {
                Map<String, String> curChildMap1 = new HashMap<String, String>();
                children1.add(curChildMap1);
                curChildMap1.put(NAME, "Homily Stick and Ponder Panel Workshop");
                List<Map<String, String>> children11 = new ArrayList<Map<String, String>>();
                //for (int j = 0; j < 5; j++) {
                    Map<String, String> curChildMap11 = new HashMap<String, String>();
                    children11.add(curChildMap11);
                    curChildMap11.put(NAME, "Homily Stick and Ponder Panel Workshop");
                //curChildMap1.put("pending","pending");
                Map<String, String> curChildMap2 = new HashMap<String, String>();
                children1.add(curChildMap2);
                curChildMap2.put(NAME, "Overnight farm experience for couples or families");
                //curChildMap2.put("completed","completed");
            //}
            childData.add(children1);
       // }
            
            Map<String, String> curGroupMap2 = new HashMap<String, String>();
            groupData.add(curGroupMap2);
            curGroupMap2.put(NAME, "My Schedule");
           // curGroupMap.put(IS_EVEN, (i % 2 == 0) ? "This group is even" : "This group is odd");
             
            List<Map<String, String>> children2 = new ArrayList<Map<String, String>>();
            //for (int j = 0; j < 5; j++) {
                Map<String, String> curChildMap3 = new HashMap<String, String>();
                children2.add(curChildMap3);
                curChildMap3.put(NAME, "Homily Stick and Ponder Panel Workshop");
               // curChildMap3.put("pending","pending");
                Map<String, String> curChildMap4 = new HashMap<String, String>();
                children2.add(curChildMap4);
                curChildMap4.put(NAME, "Overnight farm experience for couples or families");
                //curChildMap4.put(NAME, "pending");
               // curChildMap4.put("completed","completed");
            //}
            childData.add(children2);
            
            Map<String, String> curGroupMap3 = new HashMap<String, String>();
            groupData.add(curGroupMap3);
            curGroupMap3.put(NAME, "My History");
           // curGroupMap.put(IS_EVEN, (i % 2 == 0) ? "This group is even" : "This group is odd");
             
            List<Map<String, String>> children3 = new ArrayList<Map<String, String>>();
            //for (int j = 0; j < 5; j++) {
                Map<String, String> curChildMap5 = new HashMap<String, String>();
                children3.add(curChildMap5);
                curChildMap5.put(NAME, "Homily Stick and Ponder Panel Workshop");
                curChildMap5.put("pending","pending");
                Map<String, String> curChildMap6 = new HashMap<String, String>();
                children3.add(curChildMap6);
                curChildMap6.put(NAME, "Overnight farm experience for couples or families");
                curChildMap6.put("completed","completed");
            //}
            childData.add(children2);
           // ExpandableListView e = (ExpandableListView) findViewById(R.id.expandableListView1);
          
        mAdapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { NAME, IS_EVEN },
                new int[] { android.R.id.text1, android.R.id.text2 },
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] { NAME, IS_EVEN },
                new int[] { android.R.id.text1, android.R.id.text2 }
                );
      
        setListAdapter(mAdapter);
       
       // btn.setGravity()
    }
    public void logout(View v){
    	try {
    	FileOutputStream fp = openFileOutput("user.txt", 0);
    	
		OutputStreamWriter op = new OutputStreamWriter(fp);
	//	FileWriter fileWriter =   new FileWriter("user.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(op);
		bufferedWriter.write("");
		bufferedWriter.close();
		//super.onBackPressed();
    	finish();
    	}
    	catch(Exception e) {
    		
    	}
    }
    }
