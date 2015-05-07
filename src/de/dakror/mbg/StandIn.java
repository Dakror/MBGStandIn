package de.dakror.mbg;

import java.math.BigInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Maximilian Stark | Dakror
 */
public class StandIn implements Comparable<StandIn> {
	String[] courses;
	int[] lessons;
	boolean free;
	String replacer;
	String subject;
	String room;
	String text;
	
	String checksum;
	
	JSONObject cache;
	
	/**
	 * For notification purposes
	 */
	boolean added;
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof StandIn) return ((StandIn) o).checksum.equals(checksum);
		return false;
	}
	
	@Override
	public int hashCode() {
		return new BigInteger(checksum, 16).intValue();
	}
	
	@Override
	public int compareTo(StandIn another) {
		int dLessons = lessons[0] - another.lessons[0];
		if (dLessons == 0) {
			int dCourses = courses[0].compareTo(another.courses[0]);
			if (dCourses == 0) return subject.compareTo(another.subject);
			return dCourses;
		}
		return dLessons;
	}
	
	@Override
	public String toString() {
		return cache.toString() + "\r\n";
	}
	
	public static StandIn create(JSONObject o) throws JSONException {
		StandIn s = new StandIn();
		
		s.cache = o;
		
		JSONArray c = o.getJSONArray("courses");
		s.courses = new String[c.length()];
		for (int i = 0; i < c.length(); i++)
			s.courses[i] = c.getString(i);
		
		JSONArray l = o.getJSONArray("lessons");
		s.lessons = new int[l.length()];
		for (int i = 0; i < l.length(); i++)
			s.lessons[i] = l.getInt(i);
		
		s.free = o.optBoolean("free");
		s.replacer = o.optString("replacer");
		s.subject = o.optString("subject");
		s.room = o.optString("room");
		s.text = o.optString("text");
		s.checksum = o.optString("checksum");
		
		return s;
	}
}
