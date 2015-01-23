package de.dakror.mbg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.dakror.standinparser.Course;
import de.dakror.standinparser.StandIn;

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
	
	public static Set<StandIn> loadStandIns(Set<String> serializedSet) {
		if (serializedSet == null) return null;
		TreeSet<StandIn> set = new TreeSet<StandIn>();
		
		for (String s : serializedSet)
			set.add(new StandIn(s));
		
		return set;
	}
	
	public static String getMessage(Set<Course> courses, StandIn r, boolean added, boolean withText) {
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
			String replace = " bei " + r.getReplacer() + " in " + r.getRoom() + (r.getText() != null && withText && r.getText().length() > 0 ? ": " + r.getText() : "");
			return lessons.substring(1, lessons.length() - 1) + ". St." + subject + (r.isFree() ? " entfällt" : replace) + ".";
		} else {
			return "HAHA";
			// TODO: do something
		}
	}
}
