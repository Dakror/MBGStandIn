package de.dakror.standinparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
		StandInExtractionStrategy sies = StandInParser.obtain(new InputStreamProvider() {
			@Override
			public InputStream provide(final URL url) {
				try {
					String pwd = "37a08ed30093a133b1bb4ae0b8f3601f";
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					conn.setDoInput(true);
					
					conn.getOutputStream().write("pwd=".getBytes());
					conn.getOutputStream().write(pwd.getBytes());
					
					return conn.getInputStream();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		});
		
		System.out.println(sies.getStandIns().toString().replace("),", "),\n"));
		System.out.println(sies.getAdditionalInfo());
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
		
		// the normal school day is being considered over at say 6PM
		if (today != null && today.getCalendar().get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH) && c.get(Calendar.HOUR_OF_DAY) < 18) {
			return today;
		} else {
			StandInExtractionStrategy tomorrow = obtainDay(provider, false);
			if (tomorrow != null) {
				return tomorrow;
			} else {
				System.err.println("Could neither obtain today nor tomorrow!");
				return null;
			}
		}
	}
	
	public static StandInExtractionStrategy obtainDay(InputStreamProvider provider, boolean today) {
		try {
			InputStream is = provider.provide(new URL(url + (today ? StandInParser.today : tomorrow)));
			if (is == null) return null;
			PdfReader reader = new PdfReader(is);
			PdfReaderContentParser contentParser = new PdfReaderContentParser(reader);
			
			StandInExtractionStrategy extractionStrategy = new StandInExtractionStrategy();
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				contentParser.processContent(i, extractionStrategy);
			}
			return extractionStrategy;
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
	}
}
