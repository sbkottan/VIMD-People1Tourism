package com.vimd.p1t;

import com.vimd.peoplefirsttourism.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Home extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost tabHost = getTabHost();
        
        
        // Tab for Photos
        TabSpec photospec = tabHost.newTabSpec("Photos");
        photospec.setIndicator("Near Me", getResources().getDrawable(R.drawable.icon_nearme_tab));
        Intent photosIntent = new Intent(this, NearMe.class);
        photospec.setContent(photosIntent);
       
        // Tab for Songs
        TabSpec songspec = tabHost.newTabSpec("Songs");
        // setting Title and Icon for the Tab
        songspec.setIndicator("Share Experience", getResources().getDrawable(R.drawable.icon_experience_tab));
        Intent songsIntent = new Intent(this, Experience.class);
        songspec.setContent(songsIntent);
        
        // Tab for Videos
        TabSpec videospec = tabHost.newTabSpec("Videos");
        videospec.setIndicator("Dashboard", getResources().getDrawable(R.drawable.icon_dashboard_tab));
        Intent videosIntent = new Intent(this, Dashboard.class);
        videospec.setContent(videosIntent);
     
        // Adding all TabSpec to TabHost
        tabHost.addTab(videospec);
        tabHost.addTab(photospec); // Adding photos tab
        tabHost.addTab(songspec); // Adding songs tab
        // Adding videos tab
    }
}