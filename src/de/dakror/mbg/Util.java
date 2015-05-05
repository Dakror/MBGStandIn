package de.dakror.mbg;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Maximilian Stark | Dakror
 */
public class Util {
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
