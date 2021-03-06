package com.cascadia.hidenseek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.cascadia.hidenseek.pending.Settings;
import com.cascadia.hidenseek.login.HostLogin;
import com.cascadia.hidenseek.login.JoinLogin;
import com.cascadia.hidenseek.utilities.ConnectionChecks;

public class Home extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);
		final ConnectionChecks connectionChecks = new ConnectionChecks(this);
		
		//User clicked Host Match button
        ImageButton btnHost = (ImageButton) findViewById(R.id.btnHostHome);
        btnHost.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				if (connectionChecks.isConnected()) {
					Intent intent = new Intent(Home.this, HostLogin.class);
					startActivity(intent);
				}
				else {
					connectionChecks.showAlert();
				}
            }
        });
        //User clicked Join Match button
        ImageButton btnJoin = (ImageButton) findViewById(R.id.btnJoinHome);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (connectionChecks.isConnected()) {
                    Intent intent = new Intent(Home.this, JoinLogin.class);
                    startActivity(intent);
                }
                else {
                    connectionChecks.showAlert();
                }
            }
        });
        //User clicked Settings button
        ImageButton btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
                if (connectionChecks.isConnected()) {
                    Intent intent = new Intent(Home.this, Settings.class);
                    startActivity(intent);
                }
                else {
                    connectionChecks.showAlert();
                }
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
