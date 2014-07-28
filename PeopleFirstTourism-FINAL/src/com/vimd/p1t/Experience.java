package com.vimd.p1t;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonparsers.ShareExperienceJSONParser;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtablayout.R;

/* 
 * This is the class which handles the Feedback tab.
 * Users can see previously acquired services and share meaningful experiences and tips for travelers.
 * Users can also share photos corresponding to the services.
 * Created by Alok Sarang
 */
public class Experience extends Activity {

	private Spinner spinnerVisits;
	private EditText meaningfulExperience, tipsForTravellers;
	private CheckBox chkAgreeToShare;
	private Button btnUpload;
	private Button btnUploadImg;
	private String strUserName = "";
	// This is the URL which returns all previous services purchased by a user
	private static final String strServerURLVisits = "http://dev.oscar.ncsu.edu:9991/mobileapi/share/";
	// This is the JSON tag expected when at least one previous service has been
	// found
	private static final String strSuccessRetrieveVisits = "success_visits";
	// This is the URL which saves the comments and tips of a particular service
	// purchased by a user
	private static final String strServerURLUploadExperience = "http://dev.oscar.ncsu.edu:9991/mobileapi/comment/";
	// This is the JSON tag expected when the comments and tips have been saved
	private static final String strSuccessfulUpload = "success_upload";
	// This is the URL which saves the photo a user wants to save for a service
	private static final String urlServerUploadImage = "http://dev.oscar.ncsu.edu:9991/mobileapi/picture/";
	private static final String strTagVisits = "visits";
	private static final String strTagVisit = "visit";
	private ProgressDialog pDialog;
	private ShareExperienceJSONParser jParser = new ShareExperienceJSONParser();
	private JSONArray visits = null;
	private List<String> listVisits = new ArrayList<String>();
	private boolean blnGotVisits = false;
	private String pathToImageFile = "";
	private JSONObject jsonVisits;
	private ArrayAdapter<String> dataAdapter;
	private Map<String, String> mapTruncatedServiceNames = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.experience_layout);

		// Retrieve username of logged in user
		getUserName();

		// Display URL of P1T website if no previous services found
		TextView urlServ = (TextView) findViewById(R.id.p1t_url);
		urlServ.setText(Html
				.fromHtml("<a href=\"https://www.peoplefirsttourism.com/services/\">No experiences yet. Get one here!</a>"));
		urlServ.setMovementMethod(LinkMovementMethod.getInstance());
		urlServ.setVisibility(View.GONE);

		// Display the list pf previously acquired services
		spinnerVisits = (Spinner) findViewById(R.id.spinner_visits);
		addItemsToSpinnerVisits();

		// Button to browse to gallery and select one photo
		btnUploadImg = (Button) findViewById(R.id.btnUploadPhoto);
		btnUploadImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openGallery(3);
			}
		});

		// Button to uplold the comments and photo
		btnUpload = (Button) findViewById(R.id.btnUploadExperience);
		btnUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				chkAgreeToShare = (CheckBox) findViewById(R.id.chkAgree);
				// Prompt user to agree to allow P1T to use shared content
				if (!chkAgreeToShare.isChecked()) {
					Toast.makeText(
							Experience.this,
							"Please authorize People-First Tourism to use this content in web and marketing material.",
							Toast.LENGTH_SHORT).show();
				} else {
					new UploadExperience().execute();
				}
			}
		});
	}

	// Code to open the gallery so that the user can select a photo
	public void openGallery(int req_code) {

		Intent intent = new Intent();

		intent.setType("image/*");

		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(
				Intent.createChooser(intent, "Select file to upload "),
				req_code);

	}

	// This method returns a handle to the file (photo) that the user selects from the gallery.
	// This file is then written as a stream of bytes to the server.
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			Uri selectedImageUri = data.getData();
			String selectedPath1 = getPath(selectedImageUri);

			System.out.println("selectedPath1 : " + selectedPath1);
			pathToImageFile = selectedPath1;

//			TextView photoName = (TextView) findViewById(R.id.txtPhotoName);

			String fileName = selectedPath1.substring(
					selectedPath1.lastIndexOf("/") + 1, selectedPath1.length());
			System.out.println("fileNaMe : " + fileName);
		}

	}

	// This method uses a cursor to get the exact path of the photo
	public String getPath(Uri uri) {

		String[] projection = { MediaStore.Images.Media.DATA };

		Cursor cursor = managedQuery(uri, projection, null, null, null);

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		return cursor.getString(column_index);

	}

	// This method reads the local file that has the logged in user's username.
	// It sends the username to the server so that the server can save the comments etc corresponding to a particular user
	private void getUserName() {
		try {
			String filePath = this.getApplicationContext().getFilesDir()
					.getPath().toString()
					+ "/user.txt";
			BufferedReader br = new BufferedReader(new FileReader(new File(
					filePath)));
			strUserName = br.readLine();
			br.close();
		} catch (Exception e) {
			System.out.println("exception while reading username");
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	// This method retrieves the list of previously acquired services that a user has purchased and displays them in the dropdown box
	public void addItemsToSpinnerVisits() {
		new RetrieveVisits().execute();
		dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, listVisits);
	}

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
			pDialog = new ProgressDialog(Experience.this);
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
			jsonVisits = jParser.makeHttpRequest(strServerURLVisits, "POST",
					params);

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
				} else {
					blnGotVisits = false;
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
			if (blnGotVisits) {
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
						String strTruncatedVisit = strVisit;
						// If the name of the visit exceeds 30 characters, display only the first 30 characters
						if (strVisit.length() > 30) {
							strTruncatedVisit = strVisit.substring(0, 30)
									+ "...";
						}
						mapTruncatedServiceNames.put(strTruncatedVisit,
								strVisit);

						listVisits.add(strTruncatedVisit);
					}
					System.out.println(mapTruncatedServiceNames);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dataAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinnerVisits.setAdapter(dataAdapter);
				spinnerVisits.setVisibility(View.VISIBLE);
			} else {
				// Hide all other components if no previous visits found
				TextView urlServ = (TextView) findViewById(R.id.p1t_url);
				urlServ.setVisibility(View.VISIBLE);
				((Spinner) findViewById(R.id.spinner_visits))
						.setVisibility(View.GONE);
				((Button) findViewById(R.id.btnUploadPhoto))
						.setVisibility(View.GONE);
				((TextView) findViewById(R.id.txtMeaningfulExp))
						.setVisibility(View.GONE);
				((EditText) findViewById(R.id.p1t_meaningful_exp))
						.setVisibility(View.GONE);
				((TextView) findViewById(R.id.txtTipsTravellers))
						.setVisibility(View.GONE);
				((EditText) findViewById(R.id.p1t_tips_for_travellers))
						.setVisibility(View.GONE);
				((CheckBox) findViewById(R.id.chkAgree))
						.setVisibility(View.GONE);
				((Button) findViewById(R.id.btnUploadExperience))
						.setVisibility(View.GONE);
			}

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
			pDialog = new ProgressDialog(Experience.this);
			pDialog.setMessage("Sharing experience. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * uploading experience to url
		 * */
		protected String doInBackground(String... args) {
			if (blnGotVisits) {
				meaningfulExperience = (EditText) findViewById(R.id.p1t_meaningful_exp);
				tipsForTravellers = (EditText) findViewById(R.id.p1t_tips_for_travellers);

				String strVisitName = String.valueOf(spinnerVisits
						.getSelectedItem());
				strVisitName = mapTruncatedServiceNames.get(strVisitName);
				String strMeaningfulExperiences = String
						.valueOf(meaningfulExperience.getText());
				String strTipsForTravellers = String.valueOf(tipsForTravellers
						.getText());
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user_name", strUserName));
				params.add(new BasicNameValuePair("visit_name", strVisitName));
				params.add(new BasicNameValuePair("meaningful_exp",
						strMeaningfulExperiences));
				params.add(new BasicNameValuePair("tips_for_travellers",
						strTipsForTravellers));
				// getting JSON string from URL
				JSONObject json = jParser.makeHttpRequest(
						strServerURLUploadExperience, "POST", params);

				// Check your log cat for JSON reponse
				Log.d("Upload comments result: ", json.toString());

				// Code to upload the photo to the server
				// Right now this code doesn't execute since the service to save the photo is not functional
				// When it is fixed, this code can be made executable again
				// ##### UPLOAD IMAGE

				if (false) {

					HttpURLConnection connection = null;
					DataOutputStream outputStream = null;
					DataInputStream inputStream = null;

					String lineEnd = "\r\n";
					String twoHyphens = "--";
					String boundary = "*****";

					int bytesRead, bytesAvailable, bufferSize;
					byte[] buffer;
					int maxBufferSize = 1 * 1024 * 1024;

					try {
						FileInputStream fileInputStream = new FileInputStream(
								new File(pathToImageFile));

						URL url = new URL(urlServerUploadImage);
						connection = (HttpURLConnection) url.openConnection();

						// Allow Inputs & Outputs
						connection.setDoInput(true);
						connection.setDoOutput(true);
						connection.setUseCaches(false);

						// Enable POST method
						connection.setRequestMethod("POST");

						connection.setRequestProperty("Connection",
								"Keep-Alive");
						connection.setRequestProperty("Content-Type",
								"multipart/form-data;boundary=" + boundary);

						outputStream = new DataOutputStream(
								connection.getOutputStream());
						outputStream
								.writeBytes(twoHyphens + boundary + lineEnd);
						System.out.println("pathToImageFile: "
								+ pathToImageFile);
						outputStream
								.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";photoname=\""
										+ pathToImageFile + "\"" + lineEnd);
						outputStream.writeBytes(lineEnd);

						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						buffer = new byte[bufferSize];

						// Read file
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);

						while (bytesRead > 0) {
							outputStream.write(buffer, 0, bufferSize);
							bytesAvailable = fileInputStream.available();
							bufferSize = Math
									.min(bytesAvailable, maxBufferSize);
							bytesRead = fileInputStream.read(buffer, 0,
									bufferSize);
						}

						outputStream.writeBytes(lineEnd);
						outputStream.writeBytes(twoHyphens + boundary
								+ twoHyphens + lineEnd);

						// Responses from the server (code and message)
						int serverResponseCode = connection.getResponseCode();
						String serverResponseMessage = connection
								.getResponseMessage();

						fileInputStream.close();
						outputStream.flush();
						outputStream.close();
					} catch (Exception ex) {
						// Exception handling
						System.out.println("exception while uploading photo");
						ex.printStackTrace();
					}

				}

				// ##### UPLOAD IMAGE

				try {
					// Checking for SUCCESS TAG
					int success = json.getInt(strSuccessfulUpload);
					System.out.println("wel it was a :" + success);
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
			// Display message to user that the content has been successfully uploaded
			Toast.makeText(Experience.this,
					"Content shared succefully. Thank you!", Toast.LENGTH_SHORT)
					.show();
		}

	}

}
