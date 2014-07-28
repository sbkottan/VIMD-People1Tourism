/* Home page - Created by Nitin Rao
 * This is the home page where tabs are created for each module - Dashboard, Near me and Feedback
 */
package com.vimd.p1t;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.example.androidtablayout.R;

public class Home extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TabHost tabHost = getTabHost();
        Intent intent = getIntent();
        String lat = intent.getStringExtra("latitude");
        String lon = intent.getStringExtra("longitude");
        String serv = intent.getStringExtra("services");
        if(lat!=null){
        	
            TabSpec photospec = tabHost.newTabSpec("Nearme");
            photospec.setIndicator("Near Me", getResources().getDrawable(R.drawable.icon_nearme_tab));
            Intent photosIntent = new Intent(this, NearMe.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            photosIntent.putExtra("latitude",lat);
            photosIntent.putExtra("longitude",lon);
            photosIntent.putExtra("services",serv);
            photospec.setContent(photosIntent);
       
            TabSpec songspec = tabHost.newTabSpec("Experience");
            // setting Title and Icon for the Tab
            songspec.setIndicator("Feedback", getResources().getDrawable(R.drawable.icon_experience_tab));
            Intent songsIntent = new Intent(this, Experience.class);
            songspec.setContent(songsIntent);
            
           
            TabSpec videospec = tabHost.newTabSpec("Dashboard");
            videospec.setIndicator("Dashboard", getResources().getDrawable(R.drawable.icon_dashboard_tab));
            Intent videosIntent = new Intent(this, Dashboard.class);
            videosIntent.putExtras(this.getIntent());
            videospec.setContent(videosIntent);
         
            // Adding all TabSpec to TabHost
            tabHost.addTab(videospec);
            tabHost.addTab(photospec); 
            tabHost.addTab(songspec);
        	tabHost.setCurrentTab(1);
        	return;
        }
        
       
        
        
        // Tab for Near me
        TabSpec photospec = tabHost.newTabSpec("Nearme");
        photospec.setIndicator("Near Me", getResources().getDrawable(R.drawable.icon_nearme_tab));
        Intent photosIntent = new Intent(this, NearMe.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        photospec.setContent(photosIntent);
      //  photosIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       
        // Tab for Feedback
        TabSpec songspec = tabHost.newTabSpec("Experience");
        // setting Title and Icon for the Tab
        songspec.setIndicator("Feedback", getResources().getDrawable(R.drawable.icon_experience_tab));
        Intent songsIntent = new Intent(this, Experience.class);
        songspec.setContent(songsIntent);
        
        // Tab for Dashboard
        TabSpec videospec = tabHost.newTabSpec("Dashboard");
        videospec.setIndicator("Dashboard", getResources().getDrawable(R.drawable.icon_dashboard_tab));
        Intent videosIntent = new Intent(this, Dashboard.class);
        videosIntent.putExtras(this.getIntent());
        videospec.setContent(videosIntent);
     
        // Adding all TabSpec to TabHost
        tabHost.addTab(videospec);
        tabHost.addTab(photospec); 
        tabHost.addTab(songspec); 
   
    }
}