package com.transport.mbtalocpro;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Settings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	/*
	public void about(View view) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
		dialog.setTitle("About");
		dialog.setMessage("Author: Gururaj Sridhar \nBuild: 1.0.1");
		dialog.show();
	}
	
	public void sendFeedback(View view) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"mbtaloc@gmail.com"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "Bug/Feedback Report");
		intent.putExtra(Intent.EXTRA_TEXT   , "body of email");
		try {
		    startActivity(Intent.createChooser(intent, "Send mail..."));
		} catch (ActivityNotFoundException ex) {
		    Toast.makeText(Settings.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}*/
	
	
	
	
}
