package de.dakror.standinparser;

import java.util.ArrayList;

import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * @author Maximilian Stark | Dakror
 */
public class StandIn implements Comparable<StandIn> {
	Course[] courses;
	int[] lessons;
	
	boolean free;
	String replacer;
	String subject;
	String room;
	
	String text;
	
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
	
	public StandIn(ArrayList<TextRenderInfo>[] row) {
		compile(row);
	}
	
	public StandIn(String serialized) {
		if (serialized.startsWith("[")) {
			serialized = serialized.substring(1, serialized.length() - 1);
		}
		String[] parts = serialized.split(", ");
		courseCache = parts[0];
		lessonCache = parts[1];
		loadCoursesAndLessons();
		free = Boolean.parseBoolean(parts[2]);
		replacer = parts[3];
		subject = parts[4];
		room = parts[5];
		text = parts.length > 6 ? parts[6] : "";
	}
	
	public void compile(ArrayList<TextRenderInfo>[] row) {
		courseCache = Util.makeString(row[0]).replace(" ", "");
		
		lessonCache = Util.makeString(row[1]).replace(" ", "");
		
		loadCoursesAndLessons();
		
		free = Util.makeString(row[2]).length() > 0;
		
		replacer = Util.makeString(row[3]);
		subject = Util.makeString(row[4]);
		room = Util.makeString(row[5]);
		
		text = Util.makeString(row[6]);
	}
	
	void loadCoursesAndLessons() {
		String[] l = lessonCache.split("-");
		
		lessons = new int[l.length];
		for (int i = 0; i < l.length; i++)
			lessons[i] = Integer.parseInt(l[i]);
		
		String[] c = courseCache.split(",");
		courses = new Course[c.length];
		for (int i = 0; i < c.length; i++)
			courses[i] = new Course(c[i]);
		
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
	public String serialize() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(courseCache);
		list.add(lessonCache);
		list.add(Boolean.toString(free));
		list.add(replacer);
		list.add(subject);
		list.add(room);
		list.add(text);
		
		String s = list.toString();
		return s.substring(1, s.length() - 1);
	}
	
	@Override
	public int compareTo(StandIn another) {
		if (lessons[0] < another.lessons[0]) return -1;
		else if (lessons[0] > another.lessons[0]) return 1;
		else return another.courseCache.compareTo(courseCache);
	}
}
