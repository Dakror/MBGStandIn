package de.dakror.replacementparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * @author Maximilian Stark | Dakror
 */
public class Replacement {
	Course[] courses;
	int[] lessons;
	
	boolean free;
	String replacer;
	String subject;
	String room;
	
	long timestamp;
	
	String text;
	
	boolean courseCaching;
	
	/**
	 * COURSES = 0
	 * LESSONS = 1
	 * FREE = 2
	 * REPLACER = 3
	 * SUBJECT = 4
	 * ROOM = 5
	 * TEXT = 6
	 */
	public static final int[] xCoords = { 45, 107, 160, 208, 271, 311, 357, 1000 /* some random big value */};
	
	String courseCache;
	String lessonCache;
	
	public Replacement(ArrayList<TextRenderInfo>[] row) {
		timestamp = System.currentTimeMillis();
		
		compile(row);
	}
	
	public void compile(ArrayList<TextRenderInfo>[] row) {
		courseCache = Util.makeString(row[0]).replace(" ", "");
		
		String[] c = courseCache.split(",");
		courses = new Course[c.length];
		for (int i = 0; i < c.length; i++)
			courses[i] = new Course(c[i]);
		
		lessonCache = Util.makeString(row[1]).replace(" ", "");
		String[] l = lessonCache.split("-");
		
		lessons = new int[l.length];
		for (int i = 0; i < l.length; i++)
			lessons[i] = Integer.parseInt(l[i]);
		
		free = Util.makeString(row[2]).length() > 0;
		
		replacer = Util.makeString(row[3]);
		subject = Util.makeString(row[4]);
		room = Util.makeString(row[5]);
		
		text = Util.makeString(row[6]);
	}
	
	public boolean isRelevantForCourse(Course course) {
		for (Course c : courses) {
			if (c.equals(course)) return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return courseCache + "	|	" + lessonCache + "	|	" + Boolean.toString(free) + "	|	" + replacer + "	|	" + subject + "	|	" + room + "	|	" + text;
	}
	
	public Course[] getCourses() {
		return courses;
	}
	
	public int[] getLessons() {
		return lessons;
	}
	
	public boolean isFree() {
		return free;
	}
	
	public String getReplacer() {
		return replacer;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getRoom() {
		return room;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return toString().equals(o.toString());
	}
	
	/**
	 * Call only for fully loaded Replacements!
	 * Otherwise unexpected outcome!
	 */
	public Set<String> serialize() {
		HashSet<String> set = new HashSet<String>();
		set.add(courseCache);
		set.add(lessonCache);
		set.add(Boolean.toString(free));
		set.add(replacer);
		set.add(subject);
		set.add(room);
		set.add(text);
		set.add(timestamp + "");
		
		return set;
	}
}
