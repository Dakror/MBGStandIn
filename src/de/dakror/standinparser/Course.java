package de.dakror.standinparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maximilian Stark | Dakror
 */
public class Course {
	public static final String Q_LEVEL_PATTERN = "([12])([a-zA-Z]+)([1-9]+)";
	public static final String DEFAULT_PATTERN = "([0-9]+)([a-zA-Z])";
	
	int grade;
	String course;
	
	// -- qLevel -- //
	boolean qLevel;
	int qLevelCourse;
	String qLevelSubject;
	
	// -- custom -- //
	String name;
	
	String desc;
	
	public Course(String desc) {
		this.desc = desc;
		if (desc.matches(Q_LEVEL_PATTERN)) {
			qLevel = true;
			Matcher m = Pattern.compile(Q_LEVEL_PATTERN).matcher(desc);
			m.find();
			
			grade = 10 + Integer.parseInt(m.group(1));
			qLevelSubject = m.group(2);
			qLevelCourse = Integer.parseInt(m.group(3));
		} else if (desc.matches(DEFAULT_PATTERN)) {
			Matcher m = Pattern.compile(DEFAULT_PATTERN).matcher(desc);
			m.find();
			grade = Integer.parseInt(m.group(1));
			course = m.group(2);
		} else {
			name = desc;
		}
	}
	
	public boolean isQLevel() {
		return qLevel;
	}
	
	public int getGrade() {
		return grade;
	}
	
	public String getCourse() {
		return qLevel ? qLevelSubject : course;
	}
	
	public String getQLevelSubject() {
		return qLevelSubject;
	}
	
	/**
	 * For custom course names
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return desc;
	}
	
	@Override
	public int hashCode() {
		return desc.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Course)) return false;
		return ((Course) o).desc.equalsIgnoreCase(desc);
	}
}
