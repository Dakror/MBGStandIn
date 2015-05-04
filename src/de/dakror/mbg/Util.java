package de.dakror.mbg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Maximilian Stark | Dakror
 */
public class Util {
	public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new HashSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}
	
	public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new HashSet<T>();
		for (T x : setA)
			if (setB.contains(x)) tmp.add(x);
		return tmp;
	}
	
	public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new HashSet<T>(setA);
		tmp.removeAll(setB);
		return tmp;
	}
	
	public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
		Set<T> tmpA;
		Set<T> tmpB;
		
		tmpA = union(setA, setB);
		tmpB = intersection(setA, setB);
		return difference(tmpA, tmpB);
	}
	
	public static <T> boolean isSubset(Set<T> setA, Set<T> setB) {
		return setB.containsAll(setA);
	}
	
	public static <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
		return setA.containsAll(setB);
	}
	
	public static String getMessage(Set<String> courses, StandIn r, boolean added, boolean withText) {
		String firstRelevant = null;
		for (String c : r.courses) {
			if (courses.contains(c)) {
				firstRelevant = c;
				break;
			}
		}
		
		if (added) {
			String lessons = Arrays.toString(r.lessons).replace(", ", ". - ");
			String subject = (r.subject.equals("---") ? (firstRelevant != null ? ": " + firstRelevant : "") : ": " + r.subject);
			String replace = " bei " + r.replacer + " in " + r.room + (r.text != null && withText && r.text.length() > 0 ? ": " + r.text : "");
			return lessons.substring(1, lessons.length() - 1) + ". St." + subject + (r.free ? " entfällt" : replace) + ".";
		} else {
			return "HAHA";
			// TODO: do something
		}
	}
}
