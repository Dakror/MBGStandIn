package de.dakror.mbg;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * @author Maximilian Stark | Dakror
 */
public class Util {
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
	
	public static <T> Set<T> union(Set<T> SetA, Set<T> SetB) {
		Set<T> tmp = new HashSet<T>(SetA);
		tmp.addAll(SetB);
		return tmp;
	}
	
	public static <T> Set<T> intersection(Set<T> SetA, Set<T> SetB) {
		Set<T> tmp = new HashSet<T>();
		for (T x : SetA)
			if (SetB.contains(x)) tmp.add(x);
		return tmp;
	}
	
	public static <T> Set<T> difference(Set<T> SetA, Set<T> SetB) {
		Set<T> tmp = new HashSet<T>(SetA);
		tmp.removeAll(SetB);
		return tmp;
	}
	
	public static <T> Set<T> symDifference(Set<T> SetA, Set<T> SetB) {
		Set<T> tmpA;
		Set<T> tmpB;
		
		tmpA = union(SetA, SetB);
		tmpB = intersection(SetA, SetB);
		return difference(tmpA, tmpB);
	}
	
	public static <T> boolean isSubSet(Set<T> SetA, Set<T> SetB) {
		return SetB.containsAll(SetA);
	}
	
	public static <T> boolean isSuperSet(Set<T> SetA, Set<T> SetB) {
		return SetA.containsAll(SetB);
	}
	
	public static String getMessage(Set<String> courses, StandIn r, boolean withText) {
		String releventCourses = "";
		for (String c : r.courses)
			if (courses.contains(c)) releventCourses += c + ", ";
		
		releventCourses = releventCourses.substring(0, Math.max(0, releventCourses.length() - 2));
		
		if (r.added) {
			String lessons = Arrays.toString(r.lessons).replace(", ", ". - ");
			String subject = (r.subject.length() == 0 ? "" : ": " + r.subject);
			String replace = (r.replacer.contains("EVA") ? ": " : " bei ") + r.replacer + " in " + r.room + (r.text != null && withText && r.text.length() > 0 ? ": " + r.text : "");
			return lessons.substring(1, lessons.length() - 1) + ". St." + subject + (r.free ? " entfällt" : replace) + ". (" + releventCourses + ")";
		} else {
			return "drop of standins not implemented yet :P";
			// TODO: do something
		}
	}
	
	public static Set<StandIn> loadStandIns(JSONArray arr) throws JSONException {
		Set<StandIn> set = new HashSet<StandIn>();
		for (int i = 0; i < arr.length(); i++)
			set.add(StandIn.create(arr.getJSONObject(i)));
		
		return set;
	}
	
	public static boolean hasConnection(Context ctx) {
		ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	public static String getCourses(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString(ctx.getString(R.string.courses_id), "").replace(" ", "").trim();
	}
}
