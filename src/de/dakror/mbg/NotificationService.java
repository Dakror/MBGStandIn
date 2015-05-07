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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

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
		public static final int MAX_COOLDOWN = 1000 * 60 * 5; // 5 minute interval
		
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
					standIns = Util.loadStandIns(data.getJSONArray("courses"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			while (true) {
				if (cooldown <= 0) {
					execute();
					synchronized (this) {
						cooldown = MAX_COOLDOWN;
					}
				}
				
				try {
					Thread.sleep(10);
					synchronized (this) {
						cooldown -= 10;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void requestUpdate() {
			synchronized (this) {
				cooldown = 0;
				Log.d(TAG, "Requesting update");
			}
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
			
			String body = "courses=" + Util.getCourses(NotificationService.this) + "&pwd=" + bi.toString(16);
			conn.setRequestProperty("Content-Length", String.valueOf(body.length()));
			
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(body);
			writer.flush();
			
			if (conn.getResponseCode() != 200) {
				Log.d(TAG, "Http-Response: " + conn.getResponseCode());
				return null;
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copyInputStream(conn.getInputStream(), baos);
			
			writer.close();
			
			return new JSONObject(new String(baos.toByteArray()));
		}
		
		public void execute() {
			try {
				//@off
				if (!Util.hasConnection(NotificationService.this)) { Log.d(TAG, "No internet connection"); return; }
				if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.password_id), null) == null) { Log.d(TAG, "No password"); return; }
				if (Util.getCourses(NotificationService.this).length() == 0) { Log.d(TAG, "No courses"); return; }			
				//@on
				
				JSONObject data = fetchData();
				if (data == null) {
					Log.d(TAG, "No data fetched");
					return;
				}
				
				if (Util.getCourses(NotificationService.this).length() == 0) {
					Log.d(TAG, "No courses entered");
					return;
				}
				Set<String> courses = new HashSet<String>(Arrays.asList(Util.getCourses(NotificationService.this).split(",")));
				
				if (this.data != null) {
					
					if (!this.data.getString("date").equals(data.getString("date"))) {// Dates are not the same
						Log.d(TAG, "Dates are not the same");
						standIns.clear();
					}
				}
				
				JSONArray standins = data.getJSONArray("standins");
				
				Set<StandIn> newStandIns = Util.loadStandIns(standins);
				
				int i = 0;
				List<StandIn> changed = new ArrayList<StandIn>(Util.symDifference(newStandIns, standIns));
				
				Log.d(TAG, changed.toString());
				
				for (Iterator<StandIn> iter = changed.iterator(); iter.hasNext();) {
					StandIn si = iter.next();
					boolean hasNoRelevantCourse = true;
					for (String c : si.courses) {
						if (courses.contains(c)) {
							hasNoRelevantCourse = false;
							break;
						}
					}
					
					if (hasNoRelevantCourse) {
						Log.d(TAG, "Removed: " + Boolean.toString(standIns.remove(si)));
						iter.remove();
						i++;
					} else si.added = !standIns.contains(si);
				}
				
				if (i > 0) Log.d(TAG, "Removed irrelevant standins: " + i);
				
				if (changed.size() != 0) {
					Collections.sort(changed);
					buildNotifications(courses, changed);
				} else {
					Log.d(TAG, "No new standins");
				}
				
				// saving
				JSONArray arr = new JSONArray();
				for (StandIn s : standIns)
					arr.put(s.cache);
				
				data.put("courses", arr);
				
				this.data = data;
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(getString(R.string.standins_is), data.toString()).apply();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void buildNotifications(Set<String> courses, List<StandIn> changed) {
			Builder builder = new Builder(NotificationService.this);
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			int id = 1;
			
			InboxStyle inboxStyle = new InboxStyle();
			String bigMessage = changed.size() + " neue Änderungen.";
			inboxStyle.setBigContentTitle(bigMessage);
			
			String message = "";
			
			Log.d(TAG, "Old standIns size: " + standIns.size());
			
			for (StandIn r : changed) {
				String msg = Util.getMessage(courses, r, true);
				
				if (message.length() == 0) message = msg;
				inboxStyle.addLine(msg);
				
				if (r.added) standIns.add(r);
				else standIns.remove(r);
			}
			
			Log.d(TAG, "New standIns size: " + standIns.size());
			
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
			
			nManager.notify(id, builder.build());
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.d(TAG, "onSharedPreferenceChanged: " + key);
			if (key.equals(getString(R.string.courses_id)) || key.equals(getString(R.string.password_id))) {
				requestUpdate();
			}
		}
	}
	
	@Override
	public void onCreate() {
		new Notifier().start();
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
