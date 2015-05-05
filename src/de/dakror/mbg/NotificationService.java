package de.dakror.mbg;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;

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
		
		int cooldown = 0;
		
		private Notifier() {
			standIns = new HashSet<StandIn>();
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void run() {
			// Set<StandIn> old = Util.loadStandIns(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getStringSet(getString(R.string.standins_is), null));
			// if (old != null) standIns = old;
			
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
			
			System.out.println(body);
			
			conn.setRequestProperty("Content-Length", String.valueOf(body.length()));
			
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(body);
			writer.flush();
			
			System.out.println(conn.getResponseCode());
			
			if (conn.getResponseCode() != 200) return null;
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copyInputStream(conn.getInputStream(), baos);
			
			writer.close();
			
			return new JSONObject(new String(baos.toByteArray()));
		}
		
		public void execute() {
			Builder builder = new Builder(NotificationService.this);
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			int id = 1;
			
			try {
				JSONObject data = fetchData();
				
				if (data == null) return;
				
				JSONArray standins = data.getJSONArray("standins");
				
				System.out.println(data);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			//
			//
			// boolean removedSome = false;
			// for (Iterator<StandIn> iter = changed.iterator(); iter.hasNext();) {
			// StandIn s = iter.next();
			// boolean relevant = false;
			// for (Course c : courses) {
			// if (s.isRelevantForCourse(c)) {
			// relevant = true;
			// break;
			// }
			// }
			//
			// if (!relevant) {
			// iter.remove();
			// standIns.remove(s);
			// removedSome = true;
			// }
			// }
			//
			// if (changed.size() != 0) {
			// InboxStyle inboxStyle = new InboxStyle();
			// String bigMessage = changed.size() + " neue Änderungen.";
			// inboxStyle.setBigContentTitle(bigMessage);
			//
			// for (StandIn r : changed)
			// inboxStyle.addLine(Util.getMessage(courses, r, newStandIns.contains(r) /* if the new ones contains it but the old ones don't, it's an addition */, true));
			//
			// String message = Util.getMessage(courses, changed.iterator().next(), true, true);
			//
			// builder.setContentTitle(message);
			// builder.setContentText(getString(R.string.app_name));
			// builder.setSmallIcon(R.drawable.ic_mbg_logo);
			// // builder.setDefaults(Notification.DEFAULT_ALL);
			// builder.setAutoCancel(true);
			// builder.setTicker(message);
			//
			// Intent intent = new Intent(NotificationService.this, MBGStandIns.class);
			//
			// TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotificationService.this);
			// stackBuilder.addParentStack(MBGStandIns.class);
			// stackBuilder.addNextIntent(intent);
			// PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			// builder.setContentIntent(pendingIntent);
			//
			// if (changed.size() > 1) {
			// builder.setContentTitle(bigMessage);
			// builder.setNumber(changed.size());
			// builder.setStyle(inboxStyle);
			// builder.setTicker(bigMessage);
			// }
			//
			// standIns.removeAll(Util.intersection(changed, standIns));
			// standIns.addAll(Util.intersection(changed, newStandIns));
			//
			// nManager.notify(id, builder.build());
			// } else {
			// Log.d(TAG, "No new standins");
			// }
			//
			// // saving preferences
			// if (changed.size() > 0 || removedSome)
			// PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putStringSet(getString(R.string.standins_is), set).apply();
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(getString(R.string.courses_id))) requestUpdate();
		}
		
		public String getCourses() { // TODO: add validity check
			return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.courses_id), "").replace(" ", "");
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
