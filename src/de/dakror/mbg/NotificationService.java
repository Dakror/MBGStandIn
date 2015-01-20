package de.dakror.mbg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import de.dakror.replacementparser.Course;
import de.dakror.replacementparser.InputStreamProvider;
import de.dakror.replacementparser.Replacement;
import de.dakror.replacementparser.ReplacementExtractionStrategy;
import de.dakror.replacementparser.ReplacementParser;

/**
 * @author Maximilian Stark | Dakror
 */
public class NotificationService extends Service {
	/**
	 * Multithreading for better preformance on UI-Thread
	 * 
	 * @author Maximilian Stark | Dakror
	 */
	class Notifier extends Thread {
		HashSet<Replacement> replacements;
		HashSet<Course> courses;
		
		public Notifier() {
			replacements = new HashSet<Replacement>();
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
				
				String courses = pref.getString(getString(R.string.courses_id), null);
				if (courses == null) {
					// TODO: inform user
				} else {
					courses = courses.trim().replace(" ", "");
					String[] parts = courses.split(",");
					if (parts.length != this.courses.size()) {
						for (String part : parts) {
							Course course = new Course(part);
							this.courses.add(course);
						}
					}
				}
				
				ReplacementExtractionStrategy res = ReplacementParser.obtainDay(new InputStreamProvider() {
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
				
				HashSet<Replacement> replacements = res.getRelevantReplacements(this.courses);
				HashSet<Replacement> added = new HashSet<Replacement>();
				HashSet<Replacement> removed = new HashSet<Replacement>();
				
				for (Replacement r : replacements) {
					if (!this.replacements.contains(r)) {
						for (Course c : this.courses) {
							if (r.isRelevantForCourse(c)) {
								added.add(r);
								break;
							}
						}
					}
				}
				
				for (Replacement r : this.replacements) {
					if (!replacements.contains(r)) {
						for (Course c : this.courses) {
							if (r.isRelevantForCourse(c)) {
								removed.add(r);
								break;
							}
						}
					}
				}
				
				if (added.size() != 0 || removed.size() != 0) {
					InboxStyle inboxStyle = new InboxStyle();
					String bigMessage = (added.size() + removed.size()) + " neue Änderungen.";
					inboxStyle.setBigContentTitle(bigMessage);
					
					for (Replacement r : added)
						inboxStyle.addLine(getMessage(r, true));
					for (Replacement r : removed)
						inboxStyle.addLine(getMessage(r, false));
					
					String message = getMessage(added.iterator().next(), true);
					
					builder.setContentTitle(message);
					builder.setContentText(getString(R.string.app_name));
					builder.setSmallIcon(R.drawable.ic_launcher);
					// builder.setDefaults(Notification.DEFAULT_ALL);
					builder.setAutoCancel(true);
					builder.setTicker(message);
					
					if (added.size() + removed.size() > 1) {
						builder.setNumber(added.size() + removed.size());
						builder.setStyle(inboxStyle);
						builder.setTicker(bigMessage);
					}
					
					this.replacements.clear();
					this.replacements.addAll(added);
					
					nManager.notify(id, builder.build());
				}
				
				try {
					Thread.sleep(1000 * 60 * 5); // 5 minute interval
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public String getMessage(Replacement r, boolean added) {
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
		
		public <T> String implode(Iterable<T> array, String glue) {
			String s = "";
			for (T elem : array)
				s += elem + glue;
			
			return s.substring(0, s.length() - glue.length());
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
