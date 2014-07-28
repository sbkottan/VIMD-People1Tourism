package com.example.peoplefirsttourism;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jsonparsers.ShareExperienceJSONParser;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private Spinner spinnerVisits;
	private EditText meaningfulExperience, tipsForTravellers;
	private CheckBox chkAgreeToShare;
	private Button btnUpload;
	private Button btnUploadImg;
	private String strUserName = "";
	private static final String strServerURLVisits = "http://dev.oscar.ncsu.edu:9991/mobileapi/share/";
	private static final String strSuccessRetrieveVisits = "success_visits";
	private static final String strServerURLUploadExperience = "http://dev.oscar.ncsu.edu:9991/mobileapi/comment/";
	private static final String strSuccessfulUpload = "success_upload";
	private static final String urlServerUploadImage = "http://dev.oscar.ncsu.edu:9991/mobileapi/picture/";
	private static final String strTagVisits = "visits";
	private static final String strTagVisit = "visit";
	// private static final String strChooseVisit = "Choose a visit";
	private ProgressDialog pDialog;
	private ShareExperienceJSONParser jParser = new ShareExperienceJSONParser();
	private JSONArray visits = null;
	private List<String> listVisits = new ArrayList<String>();
	private boolean blnGotVisits = false;
	private String pathToImageFile = "";
	private JSONObject jsonVisits;
	private ArrayAdapter<String> dataAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getUserName();
		
		TextView urlServ = (TextView) findViewById(R.id.p1t_url);
		urlServ.setText(
//				"OLA");
				Html.fromHtml("<a href=\"https://www.peoplefirsttourism.com/services/\">No experiences yet. Get one here!</a>"));
		urlServ.setMovementMethod(LinkMovementMethod.getInstance());
		urlServ.setVisibility(View.GONE);
		
//		TextView photoName = (TextView) findViewById(R.id.txtPhotoName);
//		photoName.setVisibility(View.GONE);
		
		spinnerVisits = (Spinner) findViewById(R.id.spinner_visits);
		addItemsToSpinnerVisits();
		spinnerVisits.setOnItemSelectedListener(this);
		
		// addListenerOnButton();
				btnUploadImg = (Button) findViewById(R.id.btnUploadPhoto);
				btnUploadImg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
							openGallery(3);
					}
				});

		// addListenerOnButton();
		btnUpload = (Button) findViewById(R.id.btnUploadExperience);
		btnUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				chkAgreeToShare = (CheckBox) findViewById(R.id.chkAgree);
				if (!chkAgreeToShare.isChecked()) {
					Toast.makeText(
							MainActivity.this,
							"Please authorize People-First Tourism to use this content in web and marketing material.",
							Toast.LENGTH_SHORT).show();
				} else {
//					openGallery(3);
					
//					spinnerVisits = (Spinner) findViewById(R.id.spinner_visits);
					meaningfulExperience = (EditText) findViewById(R.id.p1t_meaningful_exp);
					tipsForTravellers = (EditText) findViewById(R.id.p1t_tips_for_travellers);
					Toast.makeText(
							MainActivity.this,
							"username: " + strUserName +
							"\nvisit: " + String.valueOf(spinnerVisits
									.getSelectedItem()) +
							"\nmean exp: " + String.valueOf(meaningfulExperience
									.getText()) +
							"\ntips: " + String.valueOf(tipsForTravellers.getText()),
							Toast.LENGTH_SHORT).show();
					new UploadExperience().execute();
//					//#######
					// Toast.makeText(
					// MainActivity.this,
					// "Spinner : "
					// + String.valueOf(spinnerVisits
					// .getSelectedItem()) + "\nComments : "
					// + String.valueOf(comments.getText()),
					// Toast.LENGTH_SHORT).show();

				}
			}
		});
	}

	public void openGallery(int req_code){

        Intent intent = new Intent();

        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent,"Select file to upload "), req_code);

   }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {



        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
//            if (requestCode == SELECT_FILE1)

//            {

                String selectedPath1 = getPath(selectedImageUri);

                System.out.println("selectedPath1 : " + selectedPath1);
                pathToImageFile = selectedPath1;
                
                TextView photoName = (TextView) findViewById(R.id.txtPhotoName);
                
                String fileName = selectedPath1.substring(selectedPath1.lastIndexOf("/") + 1, selectedPath1.length());
                System.out.println("fileNaMe : " + fileName);
                
//                photoName.setText(fileName);
//        		photoName.setVisibility(View.VISIBLE);
                EditText et = (EditText) findViewById(R.id.p1t_meaningful_exp);
                et.setText(fileName);

//            }

//            if (requestCode == SELECT_FILE2)
//
//            {
//
//                selectedPath2 = getPath(selectedImageUri);
//
//                System.out.println("selectedPath2 : " + selectedPath2);
//
//            }

//            tv.setText("Selected File paths : " + selectedPath1 + "," + selectedPath2);

        }

    }

 public String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = managedQuery(uri, projection, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(column_index);

    }
	
	private void getUserName() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("user.txt")));
			strUserName = br.readLine();
			br.close();
		} catch (Exception e) {
			System.out.println("exception while reading username");
			e.printStackTrace();
			strUserName = "Spencer";
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void addItemsToSpinnerVisits() {
//		spinnerVisits = (Spinner) findViewById(R.id.spinner_visits);

		new RetrieveVisits().execute();

//		listVisits.add("test 1");
//		listVisits.add("test2");
//		listVisits.add("test3");
//		listVisits.add("tes 4");
//		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
		dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, listVisits);
//		dataAdapter
//				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spinnerVisits.setAdapter(dataAdapter);
	}

	// public void addListenerOnButton() {
	// spinnerVisits = (Spinner) findViewById(R.id.spinner_visits);
	// comments = (EditText) findViewById(R.id.p1t_comment);
	// btnUpload = (Button) findViewById(R.id.btnUploadExperience);
	//
	// btnUpload.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// Toast.makeText(
	// MainActivity.this,
	// "Spinner : "
	// + String.valueOf(spinnerVisits
	// .getSelectedItem()) + "\nComments : "
	// + String.valueOf(comments.getText()),
	// Toast.LENGTH_SHORT).show();
	//
	// }
	// });
	// }

	/**
	 * Background Async Task to Load all visits by making HTTP Request
	 * */
	class RetrieveVisits extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Page loading. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All products from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_name", strUserName));
			// getting JSON string from URL
			jsonVisits = jParser.makeHttpRequest(strServerURLVisits,
					"POST", params);

			// Check your log cat for JSON reponse
			Log.d("All visits: ", jsonVisits.toString());
			System.out.println(">>>>>>>>>>>>>>> josn string: "
					+ jsonVisits.toString());

			try {
				// Checking for SUCCESS TAG
				int success = jsonVisits.getInt(strSuccessRetrieveVisits);
				System.out.println(">>>>>>>>>>>>>>> josn success: " + success);

				if (success == 1) { // indicates found at least one visit
					blnGotVisits = true;

					// Getting Array of visits
//					visits = jsonVisits.getJSONArray(strTagVisits);
//					// listVisits.add(strChooseVisit);
//
//					// looping through All visits
//					for (int i = 0; i < visits.length(); i++) {
//						JSONObject c = visits.getJSONObject(i);
//
//						// Storing each json item in variable
//						String strVisit = c.getString(strTagVisit);
//
//						listVisits.add(strVisit);
//					}
				} else {
//					TextView urlServ = (TextView) findViewById(R.id.p1t_url);
//					urlServ.setVisibility(View.VISIBLE);
//					
					blnGotVisits = false;
//					blnGotVisits = true;
//					for (int i = 0; i < 8; i++) {
//						listVisits.add("hey there " + i);
//					}
				
//					System.out.println("No experiences yet.  Get one here!");
					// } else {
					// // no products found
					// // Launch Add New product Activity
					// Intent i = new Intent(getApplicationContext(),
					// NewProductActivity.class);
					// // Closing all previous activities
					// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// startActivity(i);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			if(blnGotVisits) {
				TextView urlServ = (TextView) findViewById(R.id.p1t_url);
				urlServ.setVisibility(View.GONE);
				try {
					visits = jsonVisits.getJSONArray(strTagVisits);
					// listVisits.add(strChooseVisit);

					// looping through All visits
					for (int i = 0; i < visits.length(); i++) {
						JSONObject c = visits.getJSONObject(i);

						// Storing each json item in variable
						String strVisit = c.getString(strTagVisit);

						listVisits.add(strVisit);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinnerVisits.setAdapter(dataAdapter);
				spinnerVisits.setVisibility(View.VISIBLE);
			} else {
				System.out.println("ye in here");
				TextView urlServ = (TextView) findViewById(R.id.p1t_url);
				urlServ.setVisibility(View.VISIBLE);
				((Spinner) findViewById(R.id.spinner_visits)).setVisibility(View.GONE);
				((Button) findViewById(R.id.btnUploadPhoto)).setVisibility(View.GONE);
				((TextView) findViewById(R.id.txtMeaningfulExp)).setVisibility(View.GONE);
				((EditText) findViewById(R.id.p1t_meaningful_exp)).setVisibility(View.GONE);
				((TextView) findViewById(R.id.txtTipsTravellers)).setVisibility(View.GONE);
				((EditText) findViewById(R.id.p1t_tips_for_travellers)).setVisibility(View.GONE);
				((CheckBox) findViewById(R.id.chkAgree)).setVisibility(View.GONE);
				((Button) findViewById(R.id.btnUploadExperience)).setVisibility(View.GONE);
			}
			// updating UI from Background Thread
			// runOnUiThread(new Runnable() {
			// public void run() {
			// /**
			// * Updating parsed JSON data into ListView
			// * */
			// ListAdapter adapter = new SimpleAdapter(
			// AllProductsActivity.this, productsList,
			// R.layout.list_item, new String[] { TAG_PID,
			// TAG_NAME},
			// new int[] { R.id.pid, R.id.name });
			// // updating listview
			// setListAdapter(adapter);
			// }
			// });

		}

	}

	/**
	 * Background Async Task to submit experience by making HTTP Request
	 * */
	class UploadExperience extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Sharing experience. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * uploading experience to url
		 * */
		protected String doInBackground(String... args) {
			if(blnGotVisits) {
//				spinnerVisits = (Spinner) findViewById(R.id.spinner_visits);
				meaningfulExperience = (EditText) findViewById(R.id.p1t_meaningful_exp);
				tipsForTravellers = (EditText) findViewById(R.id.p1t_tips_for_travellers);

				String strVisitName = String.valueOf(spinnerVisits
						.getSelectedItem());
				String strMeaningfulExperiences = String.valueOf(meaningfulExperience.getText());
				String strTipsForTravellers = String.valueOf(tipsForTravellers.getText());
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user_name", strUserName));
				params.add(new BasicNameValuePair("visit_name", strVisitName));
				params.add(new BasicNameValuePair("meaningful_exp", strMeaningfulExperiences));
				params.add(new BasicNameValuePair("tips_for_travellers", strTipsForTravellers));
				// getting JSON string from URL
				JSONObject json = jParser.makeHttpRequest(
						strServerURLUploadExperience, "POST", params);
//				strServerURLVisits, "POST", params);

				// Check your log cat for JSON reponse
				Log.d("Upload comments result: ", json.toString());
				
				// ##### UPLOAD IMAGE
				
//				if(false) {

				HttpURLConnection connection = null;
				DataOutputStream outputStream = null;
				DataInputStream inputStream = null;

				String lineEnd = "\r\n";
				String twoHyphens = "--";
				String boundary =  "*****";

				int bytesRead, bytesAvailable, bufferSize;
				byte[] buffer;
				int maxBufferSize = 1*1024*1024;

				try
				{
				FileInputStream fileInputStream = new FileInputStream(new File(pathToImageFile) );

				URL url = new URL(urlServerUploadImage);
				connection = (HttpURLConnection) url.openConnection();

				// Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				// Enable POST method
				connection.setRequestMethod("POST");

				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

				outputStream = new DataOutputStream( connection.getOutputStream() );
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				System.out.println("pathToImageFile: " + pathToImageFile);
				outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";photo=\"" + pathToImageFile +"\"" + lineEnd);
				outputStream.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0)
				{
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				int serverResponseCode = connection.getResponseCode();
				String serverResponseMessage = connection.getResponseMessage();

				fileInputStream.close();
				outputStream.flush();
				outputStream.close();
				}
				catch (Exception ex)
				{
				//Exception handling
					System.out.println("exception while uploading photo");
					ex.printStackTrace();
				}
				
//				}
				
				// ##### UPLOAD IMAGE
				
				try {
					// Checking for SUCCESS TAG
//					int success = json.getInt(strSuccessfulUpload);
					int success = json.getInt(strSuccessfulUpload);
					System.out.println("wel it was a :" + success);

					if (success == 1) { // experience uploaded successfully
						Toast.makeText(
								MainActivity.this,
								"Content shared succefully. Thank you!",
								Toast.LENGTH_SHORT).show();
						// // username found
						// // Getting Array of visits
						// products = json.getJSONArray(strTagVisits);
						// listVisits.add(strChooseVisit);
						//
						// // looping through All visits
						// for (int i = 0; i < products.length(); i++) {
						// JSONObject c = products.getJSONObject(i);
						//
						// // Storing each json item in variable
						// String strVisit = c.getString(strTagVisit);
						//
						// listVisits.add(strVisit);
						// }
					} else {
//						Toast.makeText(
//								MainActivity.this,
//								"Error while sharing content. Please contact People-First Tourism.",
//								Toast.LENGTH_SHORT).show();
						// // no products found
						// // Launch Add New product Activity
						// Intent i = new Intent(getApplicationContext(),
						// NewProductActivity.class);
						// // Closing all previous activities
						// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						// startActivity(i);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			// runOnUiThread(new Runnable() {
			// public void run() {
			// /**
			// * Updating parsed JSON data into ListView
			// * */
			// ListAdapter adapter = new SimpleAdapter(
			// AllProductsActivity.this, productsList,
			// R.layout.list_item, new String[] { TAG_PID,
			// TAG_NAME},
			// new int[] { R.id.pid, R.id.name });
			// // updating listview
			// setListAdapter(adapter);
			// }
			// });

		}

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
//		spinnerVisits.setOnItemSelectedListener(this);
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
