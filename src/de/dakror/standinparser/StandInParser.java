package de.dakror.standinparser;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

/**
 * @author Maximilian Stark | Dakror
 */
public class StandInParser {
	public static final String url = "http://mbg-germering.de/mbg/";
	public static final String today = "heute.pdf";
	public static final String tomorrow = "morgen.pdf";
	
	/**
	 * Test main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PdfReader pdf = new PdfReader("in/heute3.pdf");
		
		PdfReaderContentParser contentParser = new PdfReaderContentParser(pdf);
		StandInExtractionStrategy extractionStrategy = new StandInExtractionStrategy();
		contentParser.processContent(1, extractionStrategy);
		
		extractionStrategy.compile();
		
		System.out.println(extractionStrategy.getStandIns());
	}
	
	/**
	 * Returns null if no standins for today could be found.
	 * Blame the school's site in that case. ;)
	 * 
	 * @return
	 */
	public static StandInExtractionStrategy obtain(InputStreamProvider provider) {
		StandInExtractionStrategy today = obtainDay(provider, true);
		
		GregorianCalendar c = new GregorianCalendar();
		
		if (today != null && today.getCalendar().get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)) {
			return today;
		} else {
			StandInExtractionStrategy tomorrow = obtainDay(provider, false);
			if (tomorrow != null && tomorrow.getCalendar().get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)) {
				return tomorrow;
			} else {
				System.err.println("Could neither obtain today nor tomorrow!");
				return null;
			}
		}
	}
	
	public static StandInExtractionStrategy obtainDay(InputStreamProvider provider, boolean today) {
		try {
			PdfReader reader = new PdfReader(provider.provide(new URL(url + (today ? StandInParser.today : tomorrow))));
			PdfReaderContentParser contentParser = new PdfReaderContentParser(reader);
			
			StandInExtractionStrategy extractionStrategy = new StandInExtractionStrategy();
			contentParser.processContent(1, extractionStrategy);
			return extractionStrategy;
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
	}
}
