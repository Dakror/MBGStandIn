package de.dakror.replacementparser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class ReplacementExtractionStrategy implements TextExtractionStrategy {
	public static final String DATE_PATTERN = "([0-9]+)\\.([0-9]+)\\.";
	
	Calendar c = new GregorianCalendar();
	
	HashSet<Replacement> replacements;
	
	ArrayList<ArrayList<TextRenderInfo>[]> rows = new ArrayList<ArrayList<TextRenderInfo>[]>();
	
	int y = 0;
	int num = -1;
	
	@Override
	public void beginTextBlock() {}
	
	@Override
	public void renderText(TextRenderInfo renderInfo) {
		int y = Util.y(renderInfo);
		
		if (y > 705) return; // filter out header
		
		String text = renderInfo.getText().trim();
		
		if (text.length() == 0) return; // filter out empty text
		
		if (y == 705 && text.matches(DATE_PATTERN)) {
			Matcher m = Pattern.compile(DATE_PATTERN).matcher(text);
			m.find();
			
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(1)));
			c.set(Calendar.MONTH, Integer.parseInt(m.group(2)));
		} else if (y <= 637) { // start of actual table
			if (y == this.y) {
				insert(renderInfo);
			} else {
				num++;
				this.y = y;
				
				@SuppressWarnings("unchecked")
				ArrayList<TextRenderInfo>[] columns = new ArrayList[7];
				for (int i = 0; i < 7; i++)
					columns[i] = new ArrayList<TextRenderInfo>();
				rows.add(columns);
				
				insert(renderInfo);
			}
		}
	}
	
	void insert(TextRenderInfo info) {
		int x = Util.x(info);
		
		for (int i = 0; i < Replacement.xCoords.length; i++) {
			if (x >= Replacement.xCoords[i] && x < Replacement.xCoords[i + 1]) {
				rows.get(num)[i].add(info);
			}
		}
	}
	
	/**
	 * Compiles the collected fragments to the full list of replacements
	 */
	public void compile() {
		replacements = new HashSet<Replacement>();
		
		// -- remove all wrapped rows -- //
		for (int i = 0; i < rows.size(); i++) {
			ArrayList<TextRenderInfo>[] row = rows.get(i);
			if (row[3].size() == 0) { // if there's nothing in the 'replacer' gap, then it's a new line due to wrapping
				for (int j = 0; j < row.length; j++)
					rows.get(i - 1)[j].addAll(row[j]);
				rows.remove(i);
			}
		}
		
		for (ArrayList<TextRenderInfo>[] row : rows) {
			replacements.add(new Replacement(row));
		}
	}
	
	@Override
	public void endTextBlock() {}
	
	@Override
	public void renderImage(ImageRenderInfo renderInfo) {}
	
	public Date getDate() {
		return c.getTime();
	}
	
	public Calendar getCalendar() {
		return c;
	}
	
	public HashSet<Replacement> getReplacements() {
		if (replacements == null) compile();
		
		return replacements;
	}
	
	public HashSet<Replacement> getRelevantReplacements(Course... courses) {
		return getRelevantReplacements(courses);
	}
	
	public HashSet<Replacement> getRelevantReplacements(Iterable<Course> courses) {
		if (replacements == null) compile();
		
		HashSet<Replacement> repl = new HashSet<Replacement>();
		for (Replacement replacement : replacements) {
			for (Course c : courses) {
				if (replacement.isRelevantForCourse(c)) {
					repl.add(replacement);
					break;
				}
			}
		}
		
		return repl;
	}
	
	@Override
	public String getResultantText() {
		return null;
	}
}
