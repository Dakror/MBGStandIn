package de.dakror.mbg;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Maximilian Stark | Dakror
 */
public class Util {
	public static <T> Set<T> union(Collection<T> setA, Collection<T> setB) {
		Set<T> tmp = new HashSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}
	
	public static <T> Set<T> intersection(Collection<T> setA, Collection<T> setB) {
		Set<T> tmp = new HashSet<T>();
		for (T x : setA)
			if (setB.contains(x)) tmp.add(x);
		return tmp;
	}
	
	public static <T> Set<T> difference(Collection<T> setA, Collection<T> setB) {
		Set<T> tmp = new HashSet<T>(setA);
		tmp.removeAll(setB);
		return tmp;
	}
	
	public static <T> Set<T> symDifference(Collection<T> setA, Collection<T> setB) {
		Set<T> tmpA;
		Set<T> tmpB;
		
		tmpA = union(setA, setB);
		tmpB = intersection(setA, setB);
		return difference(tmpA, tmpB);
	}
	
	public static <T> boolean isSubset(Collection<T> setA, Collection<T> setB) {
		return setB.containsAll(setA);
	}
	
	public static <T> boolean isSuperset(Collection<T> setA, Collection<T> setB) {
		return setA.containsAll(setB);
	}
	
	public static String getMessage(Set<String> courses, StandIn r, boolean added, boolean withText) {
		String releventCourses = "";
		for (String c : r.courses)
			if (courses.contains(c)) releventCourses += c + ", ";
		
		releventCourses = releventCourses.substring(0, Math.max(0, releventCourses.length() - 3));
		
		if (added) {
			String lessons = Arrays.toString(r.lessons).replace(", ", ". - ");
			String subject = (r.subject.equals("---") ? (releventCourses != null ? ": " + releventCourses : "") : ": " + r.subject);
			String replace = (r.replacer.contains("EVA") ? ": " : " bei ") + r.replacer + " in " + r.room + (r.text != null && withText && r.text.length() > 0 ? ": " + r.text : "");
			return lessons.substring(1, lessons.length() - 1) + ". St." + subject + (r.free ? " entfällt" : replace) + ".";
		} else {
			return "drop of standins not implemented yet :P";
			// TODO: do something
		}
	}
}
