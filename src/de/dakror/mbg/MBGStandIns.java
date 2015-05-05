package de.dakror.mbg;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Maximilian Stark | Dakror
 */
public class MBGStandIns extends Activity implements OnSharedPreferenceChangeListener {
	/**
	 * @author Maximilian Stark | Dakror
	 */
	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
		}
	}
	
	/**
	 * @author Maximilian Stark | Dakror
	 */
	public static class SettingsActivity extends Activity {
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.acitivity_settings);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_mbgstandins);
		Intent service = new Intent(this, NotificationService.class);
		startService(service);
		makeTable();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		String pwd = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.password_id), null);
		String courses = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.courses_id), null);
		
		if (pwd == null || courses == null) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
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
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void makeTable() {
		// Set<StandIn> set = Util.loadStandIns(PreferenceManager.getDefaultSharedPreferences(this).getStringSet(getString(R.string.standins_is), null));
		//
		// String coursePref = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.courses_id), null);
		// if (coursePref == null) {
		// Log.d("MBGStandIns", "courses = null");
		// } else if (set == null) {
		// Log.d("MBGStandIns", "set = null");
		// } else {
		// coursePref = coursePref.trim().replace(" ", "");
		//
		// HashSet<Course> courses = new HashSet<Course>();
		// for (String part : coursePref.split(","))
		// courses.add(new Course(part));
		//
		// ListView layout = (ListView) findViewById(R.id.standins_list);
		//
		// final String[] str = new String[set.size()];
		// int i = 0;
		// for (StandIn s : set) {
		// str[i] = Util.getMessage(courses, s, true, false);
		// i++;
		// }
		//
		// final ArrayList<StandIn> list = new ArrayList<StandIn>(set);
		//
		// layout.setAdapter(new BaseAdapter() {
		// @Override
		// public View getView(int position, View convertView, ViewGroup parent) {
		// LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		// View view = null;
		// if (list.get(position).getText().length() == 0) {
		// view = inflater.inflate(R.layout.standins_textview_alt, null);
		// TextView header = (TextView) view.findViewById(R.id.standins_textview_header_alt);
		// header.setText(str[position]);
		// } else {
		// view = inflater.inflate(R.layout.standins_textview, null);
		// TextView header = (TextView) view.findViewById(R.id.standins_textview_header);
		// header.setText(str[position]);
		// TextView text = (TextView) view.findViewById(R.id.standins_textview_text);
		// text.setText(list.get(position).getText());
		// }
		// return view;
		// }
		//
		// @Override
		// public long getItemId(int position) {
		// return position;
		// }
		//
		// @Override
		// public Object getItem(int position) {
		// return str[position];
		// }
		//
		// @Override
		// public int getCount() {
		// return str.length;
		// }
		// });
		//
		// }
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.standins_is))) {
			makeTable();
		}
	}
}