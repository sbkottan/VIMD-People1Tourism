package com.vimd.p1t;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.vimd.peoplefirsttourism.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		TextView tv = (TextView) findViewById(R.id.TextView01);
		tv.setText(Html.fromHtml(
	            "<a href=\"http://www.peoplefirsttourism.com\">New User? Sign up here</a> "));
	tv.setMovementMethod(LinkMovementMethod.getInstance());
	// File file  = new File("/data/data/com.example.androidtablayout/files/user.txt");
		 
		try {
		//	file.createNewFile();
			FileInputStream in = openFileInput("user.txt");	
			InputStreamReader inputStreamReader = new InputStreamReader(in);
			
			BufferedReader br  = new BufferedReader(inputStreamReader);
			StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = br.readLine()) != null) {
		        sb.append(line);
		    }
		if(sb.toString().equalsIgnoreCase("user")) {
			Intent intent  = new Intent(this,Home.class);
			startActivityForResult(intent, RESULT_OK);
			finish();
		}
			
		br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	public void authenticate(View v) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setMessage("Invalid username/password!");
		EditText tv1 = (EditText) findViewById(R.id.editText1);
		
		if(!tv1.getText().toString().equalsIgnoreCase("user")) {
			alert.show();
			return;
		}
		try {
			FileOutputStream fp = openFileOutput("user.txt", 0);
			
			OutputStreamWriter op = new OutputStreamWriter(fp);
		//	FileWriter fileWriter =   new FileWriter("user.txt");
			BufferedWriter bufferedWriter =    new BufferedWriter(op);
			//bufferedWriter.write(" ");
			bufferedWriter.write(tv1.getText().toString());
			bufferedWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		

            // Always wrap FileWriter in BufferedWriter.
            
		Intent intent  = new Intent(this,Home.class);
		startActivity(intent);
		//finish();
		
		
	}
}
