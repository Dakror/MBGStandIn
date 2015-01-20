package de.dakror.mbg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
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
		
		HashSet<StandIn> standIns;
		HashSet<Course> courses;
		
		private Notifier() {
			standIns = new HashSet<StandIn>();
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void run() {
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			int id = 1;
			
			Builder builder = new Builder(NotificationService.this);
			
			// TODO: load old replacements
			
			while (true) {
				if (courses == null) courses = new HashSet<Course>();
				
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				
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
				
				Set<StandIn> changed = Util.symDifference(standIns, newStandIns);
				
				if (changed.size() != 0) {
					InboxStyle inboxStyle = new InboxStyle();
					String bigMessage = changed.size() + " neue Änderungen.";
					inboxStyle.setBigContentTitle(bigMessage);
					
					for (StandIn r : changed)
						inboxStyle.addLine(getMessage(r, newStandIns.contains(r) /* if the new ones contains it but the old ones dont, it's an addition */));
					
					String message = getMessage(changed.iterator().next(), true);
					
					builder.setContentTitle(message);
					builder.setContentText(getString(R.string.app_name));
					builder.setSmallIcon(R.drawable.ic_launcher);
					// builder.setDefaults(Notification.DEFAULT_ALL);
					builder.setAutoCancel(true);
					builder.setTicker(message);
					
					if (changed.size() > 1) {
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
				
				try {
					Thread.sleep(1000 * 60 * 5); // 5 minute interval
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public String getMessage(StandIn r, boolean added) {
			Course firstRelevant = null;
			for (Course c : r.getCourses()) {
				if (courses.contains(c)) {
					firstRelevant = c;
					break;
				}
			}
			
			if (added) {
				String lessons = Arrays.toString(r.getLessons()).replace(", ", ". - ");
				String subject = (r.getSubject().equals("---") ? (firstRelevant != null ? ": " + firstRelevant : "") : ": " + r.getSubject());
				String replace = " bei " + r.getReplacer() + " in " + r.getRoom() + (r.getText() != null && r.getText().length() > 0 ? ": " + r.getText() : "");
				return lessons.substring(1, lessons.length() - 1) + ". St." + subject + (r.isFree() ? " entfällt" : replace) + ".";
			} else {
				return "HAHA";
			}
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.d(TAG, "onSharedPreferenceChanged: " + key);
			if (key.equals(getString(R.string.courses_id))) {
				String coursePref = sharedPreferences.getString(getString(R.string.courses_id), null);
				if (coursePref == null) {
					Log.d(TAG, "courses = null");
				} else {
					coursePref = coursePref.trim().replace(" ", "");
					
					HashSet<Course> courses = new HashSet<Course>();
					for (String part : coursePref.split(","))
						courses.add(new Course(part));
					if (!this.courses.equals(courses)) {
						Set<Course> tmp = Util.intersection(this.courses, courses);
						this.courses.clear();
						this.courses.addAll(tmp);
						Log.d(TAG, "courses changed");
					} else {
						Log.d(TAG, "courses stay the same");
					}
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
