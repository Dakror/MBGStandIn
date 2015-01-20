package de.dakror.standinparser;

import java.util.ArrayList;

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
		String s = "";
		
		int x = 0;
		int y = 0;
		for (TextRenderInfo i : blobs) {
			if (s.length() == 0) {
				s += i.getText();
				x = Util.x(i) + Util.width(i);
				y = Util.y(i);
			} else {
				int delta = Util.x(i) - x;
				if (delta <= 1 && y == Util.y(i)) {
					s += i.getText();
					x = Util.x(i) + Util.width(i);
				} else {
					s += " " + i.getText();
					x = Util.x(i) + Util.width(i);
				}
			}
		}
		
		return s.trim();
	}
}
