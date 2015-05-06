package de.dakror.mbg;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Maximilian Stark | Dakror
 */
public class NotificationService extends Service {
	/**
	 * Multithreading for better preformance on UI-Thread
	 * 
	 * @author Maximilian Stark | Dakror
	 */
	class Notifier extends Thread implements OnSharedPreferenceChangeListener {
		public static final String TAG = "Notifier";
		public static final int MAX_COOLDOWN = 5000; // 5 minute interval
		
		Set<StandIn> standIns;
		JSONObject data;
		int cooldown = 0;
		
		private Notifier() {
			standIns = new HashSet<StandIn>();
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void run() {
			String dataPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.standins_is), null);
			if (dataPref != null) {
				try {
					data = new JSONObject(dataPref);
					standIns = loadStandIns(data.getJSONArray("courses"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			while (true) {
				if (cooldown == 0) {
					execute();
					cooldown = MAX_COOLDOWN;
				}
				
				try {
					Thread.sleep(1);
					cooldown--;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void requestUpdate() {
			cooldown = 0;
		}
		
		public JSONObject fetchData() throws Exception {
			URL url = new URL("http://dakror.de/MBGStandIns/index.php");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			String pwd = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.password_id), null);
			if (pwd == null) return null;
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] md5 = md.digest(pwd.getBytes());
			BigInteger bi = new BigInteger(md5);
			
			String body = "courses=" + getCourses() + "&pwd=" + bi.toString(16);
			conn.setRequestProperty("Content-Length", String.valueOf(body.length()));
			
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(body);
			writer.flush();
			
			if (conn.getResponseCode() != 200) {
				System.out.println("Http-Response: " + conn.getResponseCode());
				return null;
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copyInputStream(conn.getInputStream(), baos);
			
			writer.close();
			
			return new JSONObject(new String(baos.toByteArray()));
		}
		
		public Set<StandIn> loadStandIns(JSONArray arr) throws JSONException {
			Set<StandIn> set = new HashSet<StandIn>();
			for (int i = 0; i < arr.length(); i++)
				set.add(StandIn.create(arr.getJSONObject(i)));
			
			return set;
		}
		
		public void execute() {
			Builder builder = new Builder(NotificationService.this);
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			int id = 1;
			
			try {
				data = fetchData();
				if (data == null) return;
				JSONArray standins = data.getJSONArray("standins");
				
				Set<StandIn> newStandIns = loadStandIns(standins);
				
				Collection<StandIn> changed = new ArrayList<StandIn>(Util.symDifference(standIns, newStandIns));
				Collections.sort((List<StandIn>) changed);
				
				Set<String> courses = new HashSet<String>(Arrays.asList(getCourses().split(",")));
				
				if (changed.size() != 0) {
					InboxStyle inboxStyle = new InboxStyle();
					String bigMessage = changed.size() + " neue Änderungen.";
					inboxStyle.setBigContentTitle(bigMessage);
					
					for (StandIn r : changed)
						inboxStyle.addLine(Util.getMessage(courses, r, newStandIns.contains(r) /* if the new ones contains it but the old ones don't, it's an addition */, true));
					
					String message = Util.getMessage(courses, changed.iterator().next(), true, true);
					
					builder.setContentTitle(message);
					builder.setContentText(getString(R.string.app_name));
					builder.setSmallIcon(R.drawable.ic_mbg_logo);
					// builder.setDefaults(Notification.DEFAULT_ALL);
					builder.setAutoCancel(true);
					builder.setTicker(message);
					
					Intent intent = new Intent(NotificationService.this, MBGStandIns.class);
					
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotificationService.this);
					stackBuilder.addParentStack(MBGStandIns.class);
					stackBuilder.addNextIntent(intent);
					PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
					builder.setContentIntent(pendingIntent);
					
					if (changed.size() > 1) {
						builder.setContentTitle(bigMessage);
						builder.setNumber(changed.size());
						builder.setStyle(inboxStyle);
						builder.setTicker(bigMessage);
					}
					
					standIns.removeAll(Util.intersection(changed, standIns));
					standIns.addAll(Util.intersection(changed, newStandIns));
					
					nManager.notify(id, builder.build());
				} else {
					Log.d(TAG, "No new standins");
				}
				
				// saving
				JSONArray arr = new JSONArray();
				for (StandIn s : standIns)
					arr.put(s.cache);
				
				data.put("courses", arr);
				
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(getString(R.string.standins_is), data.toString()).apply();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(getString(R.string.courses_id)) || key.equals(getString(R.string.password_id))) requestUpdate();
		}
		
		public String getCourses() {
			return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.courses_id), "").replace(" ", "");
		}
	}
	
	@Override
	public void onCreate() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new Notifier().start();
		} else {
			Toast.makeText(getApplicationContext(), "Nicht mit dem Internet verbunden. Vertretungsplan konnte nicht abgerufen werden.", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public static void copyInputStream(InputStream is, OutputStream out) throws Exception {
		byte[] buffer = new byte[2048];
		int len = is.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = is.read(buffer);
		}
		is.close();
		out.close();
	}
}
