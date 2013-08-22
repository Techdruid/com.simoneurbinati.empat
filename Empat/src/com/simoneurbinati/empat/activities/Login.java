package com.simoneurbinati.empat.activities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

import com.simoneurbinati.empat.R;
import com.simoneurbinati.empat.utils.Config;
import com.simoneurbinati.empat.utils.Utility;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		  //setto il menu a tendina
		  	Spinner spinner = (Spinner)findViewById(R.id.spinner1);
		  	
		  	ArrayList<String> ar = new ArrayList<String>();
		  	Set<String> temp =  Utility.getCountryMap().keySet();
		  	for(String k : temp){
		  		ar.add(k);
		  	}
		  	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_spinner_item,
        		ar
        		);
		  	
	        spinner.setAdapter(adapter);
	    	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    		@Override
	    		public void onItemSelected(AdapterView<?> adapter, View view,int pos, long id) {
	        		String selected = (String)adapter.getItemAtPosition(pos);
	        		Toast.makeText(
	        				getApplicationContext(), 
	        				"hai selezionato "+selected, 
	        				Toast.LENGTH_LONG
	        			).show();
	        	}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
	    	});
	    
	    	//fine menu a tendina
	    	
	    	//bottone continua
	    	Button b = (Button) findViewById(R.id.button1);
	    	b.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					registerRoutine();
					
				}
			});
	    	
	    	//fine bottone continua
	}

	private void registerRoutine() {
		String serverAddress = Config.SERVER_DEFAULT_BASE_URL;
		EditText phoneNumberView = (EditText) findViewById(R.id.editText1);
		String phoneNumber = phoneNumberView.getText().toString(); 
		if (serverAddress.charAt(serverAddress.length() - 1) != '/') {
			serverAddress += "/";
		}
		try {
			URL serverUrl = new URL(serverAddress);
		} catch (MalformedURLException e) {
			Toast.makeText(this, getString(R.string.toast_invalid_server_address), Toast.LENGTH_SHORT).show();
			return;
		}
		if (phoneNumber.length() == 0) {
			Toast.makeText(this, getString(R.string.toast_empty_phone_string), Toast.LENGTH_SHORT).show();
			return;
		}
		if (!Pattern.matches("^\\+\\d{5,19}$", phoneNumber)) {
			Toast.makeText(this, getString(R.string.toast_invalid_phone), Toast.LENGTH_SHORT).show();
			return;
		}
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){

			@Override
			protected void onPreExecute() {
				
			}
			
			@Override
			protected Boolean doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				
			}
		};
		
	}
	


}
