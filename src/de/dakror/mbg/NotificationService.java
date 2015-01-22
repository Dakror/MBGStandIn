package de.dakror.mbg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import android.app.Notification;
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
import de.dakror.standinparser.Course;
import de.dakror.standinparser.InputStreamProvider;
import de.dakror.standinparser.StandIn;
import de.dakror.standinparser.StandInExtractionStrategy;
import de.dakror.standinparser.StandInParser;

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
		public static final int INTERVAL = 5000; // 5 minute interval
		
		Set<StandIn> standIns;
		Set<Course> courses;
		
		private Notifier() {
			standIns = new HashSet<StandIn>();
			courses = new HashSet<Course>();
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void run() {
			updateCourses();
			Set<StandIn> old = Util.loadStandIns(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getStringSet(getString(R.string.standins_is), null));
			if (old != null) standIns = old;
			
			while (true) {
				execute();
				try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void execute() {
			Builder builder = new Builder(NotificationService.this);
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			int id = 1;
			
			StandInExtractionStrategy res = StandInParser.obtainDay(new InputStreamProvider() {
				@Override
				public InputStream provide(URL url) {
					try {
						return getAssets().open("morgen2.pdf");
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			}, true);
			
			HashSet<StandIn> newStandIns = res.getRelevantStandIns(courses);
			
			Set<StandIn> changed = new TreeSet<StandIn>(Util.symDifference(standIns, newStandIns));
			
			if (changed.size() != 0) {
				InboxStyle inboxStyle = new InboxStyle();
				String bigMessage = changed.size() + " neue Änderungen.";
				inboxStyle.setBigContentTitle(bigMessage);
				
				HashSet<String> set = new HashSet<String>();
				
				for (StandIn r : changed) {
					inboxStyle.addLine(Util.getMessage(courses, r, newStandIns.contains(r) /* if the new ones contains it but the old ones don't, it's an addition */));
					set.add(r.serialize());
				}
				
				// saving preferences
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putStringSet(getString(R.string.standins_is), set).apply();
				
				String message = Util.getMessage(courses, changed.iterator().next(), true);
				
				builder.setContentTitle(message);
				builder.setContentText(getString(R.string.app_name));
				builder.setSmallIcon(R.drawable.ic_mbg_logo);
				builder.setDefaults(Notification.DEFAULT_ALL);
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
				
				standIns.clear();
				standIns.addAll(Util.intersection(changed, newStandIns));
				
				nManager.notify(id, builder.build());
			} else {
				Log.d(TAG, "No new standins");
			}
			
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(getString(R.string.courses_id))) {
				updateCourses();
				
				execute();
			}
		}
		
		public void updateCourses() {
			String coursePref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.courses_id), null);
			if (coursePref == null) {
				Log.d(TAG, "courses = null");
			} else {
				coursePref = coursePref.trim().replace(" ", "");
				
				HashSet<Course> courses = new HashSet<Course>();
				for (String part : coursePref.split(","))
					courses.add(new Course(part));
				if (!this.courses.equals(courses)) {
					this.courses.clear();
					this.courses.addAll(courses);
					Log.d(TAG, "courses changed");
				} else {
					Log.d(TAG, "courses stay the same");
				}
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
}
