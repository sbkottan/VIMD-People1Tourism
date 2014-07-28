/* Login module - Created By Nitin Rao
 * This module performs user authentication and provides option to sign up on the website
 */
package com.vimd.p1t;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.androidtablayout.R;

public class Login extends Activity {
	ProgressDialog pDialog;
	List<NameValuePair> params;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Luna.ttf"); 
		TextView tv = (TextView) findViewById(R.id.TextView01);
		
		tv.setTypeface(type);
		tv.setText(Html.fromHtml(
	            "<a href=\"http://www.peoplefirsttourism.com\">New User? Sign up here</a> "));
	tv.setMovementMethod(LinkMovementMethod.getInstance());
	String filePath = this.getApplicationContext().getFilesDir().getPath().toString() + "/user.txt";
	 File file  = new File(filePath);
		 // Check if file exists that contains user name, else create new file
		try {
			if(!file.exists())
				file.createNewFile();
			  InputStream in = new FileInputStream(file);
			//FileInputStream in = openFileInput(name)
			InputStreamReader inputStreamReader = new InputStreamReader(in);
			
			BufferedReader br  = new BufferedReader(inputStreamReader);
			StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = br.readLine()) != null) {
		        sb.append(line);
		    }
		    //If file found then start the app
		if(null!=sb && !sb.toString().equals("")) {
			Intent intent  = new Intent(this,Home.class);
			startActivityForResult(intent, RESULT_OK);
			
			finish();
		}
			
		br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
//	new NewThread().execute((String[]) null);
	}

	 public boolean fetchJson(String user) {
			try {
			//	EditText txtBox = (EditText) findViewById(R.id.editText1);
				//gps = new GPS(this);
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		        StrictMode.setThreadPolicy(policy);
		   
				params = new ArrayList<NameValuePair>();
			//	if(sb.toString()=="")
					params.add(new BasicNameValuePair("user_name",user));
				//else
				//	params.add(new BasicNameValuePair("user_name",sb.toString()));
				//params.add(new BasicNameValuePair("longitude",String.valueOf(gps.getLongitude())));
		        DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://dev.oscar.ncsu.edu:9991/mobileapi/login/");
				httpPost.setEntity(new UrlEncodedFormEntity(params));
				HttpResponse httpResponse = httpClient.execute(httpPost);
			//	HttpEntity httpEntity = httpResponse.getEntity();
				//is = httpEntity.getContent();
				// HttpResponse response = httpclient.execute(httppost);
				   String data = EntityUtils.toString(httpResponse.getEntity());
		
			  
				   if(data.equals("1")) {
					   String filePath = this.getApplicationContext().getFilesDir().getPath().toString() + "/user.txt";
					   File file = new File(filePath);
						//FileInputStream in = openFileInput("user.txt");	
						 OutputStream out = new FileOutputStream(file);
						//FileOutputStream fp = openFileOutput("user.txt", 0);
							EditText tv1 = (EditText) findViewById(R.id.editText1);
						OutputStreamWriter op = new OutputStreamWriter(out);
					//	FileWriter fileWriter =   new FileWriter("user.txt");
						BufferedWriter bufferedWriter =    new BufferedWriter(op);
						//bufferedWriter.write("");
						
						//bufferedWriter.write(" ");
						bufferedWriter.write(tv1.getText().toString());
						bufferedWriter.close();
						Intent intent  = new Intent(this,Home.class);
						startActivityForResult(intent, RESULT_OK);
						finish();
					}
				   else return false;
			   
			    
			}
				catch(Exception ex) {
					System.out.println(ex.getMessage());
				}
			return true;
			
		}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	// Authenticate user
	public void authenticate(View v) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setMessage("Invalid username/password!");
		EditText tv1 = (EditText) findViewById(R.id.editText1);
		
		
		try {
			String filePath = this.getApplicationContext().getFilesDir().getPath().toString() + "/user.txt";
			File file = new File(filePath);
			//FileInputStream in = openFileInput("user.txt");	
			 OutputStream out = new FileOutputStream(file);
			//FileOutputStream fp = openFileOutput("user.txt", 0);
			
			OutputStreamWriter op = new OutputStreamWriter(out);
		//	FileWriter fileWriter =   new FileWriter("user.txt");
			BufferedWriter bufferedWriter =    new BufferedWriter(op);
			//bufferedWriter.write("");
			if(fetchJson(tv1.getText().toString())==false) {
				alert.show();
				return;
			}
			
			//bufferedWriter.write(" ");
			bufferedWriter.write(tv1.getText().toString());
			bufferedWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		

                     
		Intent intent  = new Intent(this,Home.class);
		startActivity(intent);
		
		
		
	}
	public class NewThread extends AsyncTask<String, String, String> {

		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Login.this);
			pDialog.setMessage("Page loading. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
			
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			fetchJson(null);
			return null;
		}
		
		protected void onPostExecute(String str) {
			//setViews();
			pDialog.dismiss();	
		}
		
	}
}
