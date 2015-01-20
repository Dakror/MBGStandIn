package de.dakror.mbg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Maximilian Stark | Dakror
 */
public class MBGStandIn extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mbgstandin);
		startService(new Intent(this, NotificationService.class));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings:
				getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
