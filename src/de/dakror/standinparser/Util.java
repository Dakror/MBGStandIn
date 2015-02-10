package de.dakror.standinparser;

import java.util.ArrayList;

import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * @author Maximilian Stark | Dakror
 */
public class Util {
	public static final int y(TextRenderInfo tri) {
		return Math.round(tri.getBaseline().getStartPoint().get(Vector.I2));
	}
	
	public static final int x(TextRenderInfo tri) {
		return Math.round(tri.getBaseline().getStartPoint().get(Vector.I1));
	}
	
	public static int width(TextRenderInfo renderInfo) {
		return Math.round(renderInfo.getBaseline().getLength());
	}
	
	public static String makeString(ArrayList<TextRenderInfo> blobs) {
		return makeStringNL(blobs).replace("\n", " ");
	}
	
	public static String makeStringNL(ArrayList<TextRenderInfo> blobs) {
		SimpleTextExtractionStrategy es = new SimpleTextExtractionStrategy();
		for (TextRenderInfo i : blobs)
			es.renderText(i);
		
		return es.getResultantText();
	}
}
