package de.dakror.standinparser;

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

public class StandInExtractionStrategy implements TextExtractionStrategy {
	public static final String DATE_PATTERN = "([0-9]+)\\.([0-9]+)\\.";
	
	Calendar c = new GregorianCalendar();
	
	HashSet<StandIn> standIns;
	
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
		
		for (int i = 0; i < StandIn.xCoords.length; i++) {
			if (x >= StandIn.xCoords[i] && x < StandIn.xCoords[i + 1]) {
				rows.get(num)[i].add(info);
			}
		}
	}
	
	/**
	 * Compiles the collected fragments to the full list of standins
	 */
	public void compile() {
		standIns = new HashSet<StandIn>();
		
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
			standIns.add(new StandIn(row));
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
	
	public HashSet<StandIn> getStandIns() {
		if (standIns == null) compile();
		
		return standIns;
	}
	
	public HashSet<StandIn> getRelevantStandIns(Course... courses) {
		return getRelevantStandIns(courses);
	}
	
	public HashSet<StandIn> getRelevantStandIns(Iterable<Course> courses) {
		if (standIns == null) compile();
		
		HashSet<StandIn> repl = new HashSet<StandIn>();
		for (StandIn standIn : standIns) {
			for (Course c : courses) {
				if (standIn.isRelevantForCourse(c)) {
					repl.add(standIn);
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
