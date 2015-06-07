/*******************************************************************************
 * Copyright 2015 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package de.dakror.mbg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Maximilian Stark | Dakror
 */
public class MBGStandIns extends Activity implements OnSharedPreferenceChangeListener, OnRefreshListener {
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
		
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public boolean onNavigateUp() {
			doWarnings(this, false);
			return super.onNavigateUp();
		}
	}
	
	SwipeRefreshLayout refreshLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_mbgstandins);
		Intent service = new Intent(this, NotificationService.class);
		startService(service);
		
		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
		refreshLayout.setColorSchemeColors(Color.parseColor("#FF6F00"), Color.parseColor("#ffb300"), Color.parseColor("#FFD54F"), Color.parseColor("#FFF8E1"));
		// using http://www.google.com/design/spec/style/color.html#color-color-palette
		refreshLayout.setOnRefreshListener(this);
		
		try {
			makeTable();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		String pwd = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.password_id), "");
		String courses = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.courses_id), "");
		
		if (pwd.length() == 0 || courses.length() == 0) {
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
			case R.id.menu_refresh:
				onRefresh();
				break;
			case R.id.settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	JSONObject data;
	
	public void makeTable() throws JSONException {
		Set<StandIn> set = new HashSet<StandIn>();
		final ListView listView = (ListView) findViewById(R.id.standins_list);
		
		String standins = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.standins_is), null);
		if (standins != null) {
			data = new JSONObject(standins);
			if (doWarnings(this, false)) set.addAll(Util.loadStandIns(data.getJSONArray("courses")));
		}
		
		final ArrayList<StandIn> list = new ArrayList<StandIn>(set);
		Collections.sort(list);
		
		final String[] str = new String[list.size() + 1];
		int i = 0;
		
		if (standins == null) {
			str[0] = "Keine Einträge verfügbar.";
		} else {
			str[i++] = "Vertretungsplan des " + data.getString("date") + ".";
			Set<String> courses = new HashSet<String>(Arrays.asList(Util.getCourses(this).split(",")));
			for (StandIn s : list) {
				s.added = true;
				str[i] = Util.getMessage(courses, s, false);
				i++;
			}
		}
		
		listView.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = null;
				
				if (position > 0) view = setView(str[position], list.get(position - 1).text.length() == 0 ? null : list.get(position - 1).text);
				else {
					String info = data != null ? data.optString("info") : "";
					view = setView(str[position], info.length() > 0 ? info : "für die Klasse(n) / Kurs(e): " + Util.getCourses(MBGStandIns.this).replace(",", ", "));
				}
				
				view.setOnClickListener(null);
				
				return view;
			}
			
			public View setView(String headerText, String subText) {
				View view = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(subText == null ? R.layout.standins_textview_alt : R.layout.standins_textview, null);
				TextView header = (TextView) view.findViewById(R.id.standins_textview_header);
				header.setText(headerText);
				if (subText != null) {
					TextView text = (TextView) view.findViewById(R.id.standins_textview_text);
					text.setText(subText);
				}
				
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return str[position];
			}
			
			@Override
			public int getCount() {
				return str.length;
			}
		});
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.refresh_id))) {
			refreshLayout.setRefreshing(sharedPreferences.getBoolean(key, false));
		}
		
		if (key.equals(getString(R.string.standins_is))) {
			try {
				makeTable();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			doWarnings(this, false);
		}
	}
	
	public static boolean doWarnings(Context ctx, boolean inetToo) {
		int err = PreferenceManager.getDefaultSharedPreferences(ctx).getInt(ctx.getString(R.string.error_id), 200);
		boolean pwd = PreferenceManager.getDefaultSharedPreferences(ctx).getString(ctx.getString(R.string.password_id), "").length() > 0;
		boolean crs = Util.getCourses(ctx).length() > 0;
		boolean inet = inetToo ? Util.hasConnection(ctx) : true;
		String msg = "";
		
		if (err == 401) msg = "Das von Ihnen eingegebene Passwort ist falsch.";
		else if (!pwd) msg = "Bitte geben Sie das Passwort ein.";
		
		if (!crs) msg += "Bitte geben Sie Ihre Klassen / Kurse an.";
		
		if ((!inet || err == 199) && inetToo) msg = "Nicht mit dem Internet verbunden. Vertretungsplan konnte nicht aktualisiert werden.";
		if (err != 200 && msg.length() == 0) msg = "Es ist ein Fehler aufgetreten. Bitte versuchen Sie es später erneut.";
		
		if (msg.length() > 0) {
			Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
		}
		
		PreferenceManager.getDefaultSharedPreferences(ctx).edit().putInt(ctx.getString(R.string.error_id), 200).apply(); // reset error code
		
		return pwd && crs && inet;
	}
	
	@Override
	public void onRefresh() {
		if (doWarnings(this, true)) PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getString(R.string.refresh_id), true).apply();
		else refreshLayout.setRefreshing(false);
	}
}
