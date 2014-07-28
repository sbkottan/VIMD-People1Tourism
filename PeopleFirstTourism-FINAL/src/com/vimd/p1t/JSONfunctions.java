/*Created by Spurthi Kottan and Sushma Sivakumar
 * 
 */
package com.vimd.p1t;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONfunctions {

	private static String getUserName(String path) {
		//String filePath = getApplicationContext().getFilesDir().getPath().toString() + "/user.txt";
		String strUserName = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					path)));
			strUserName = br.readLine();
			br.close();
		} catch (Exception e) {
			System.out.println("exception while reading username");
		//	e.printStackTrace();
			//strUserName = "Spencer";
		}
		return strUserName;
	}

	public static JSONObject getJSONfromURL(String url,String path) {
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			//
			ArrayList<NameValuePair> postParameters;
			postParameters = new ArrayList<NameValuePair>();
			postParameters
					.add(new BasicNameValuePair("user_name", getUserName(path)));

			httppost.setEntity(new UrlEncodedFormEntity(postParameters));
			//
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		} catch (Exception e) {
			Log.e("log_tag", "Error in http connection " + e.toString());
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			Log.e("log_tag", "Error converting result " + e.toString());
		}

		try {

			jArray = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		return jArray;
	}

}