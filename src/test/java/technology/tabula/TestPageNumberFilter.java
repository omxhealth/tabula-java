package technology.tabula;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import technology.tabula.filters.PageNumberFilter;


public class TestPageNumberFilter {

	private Line lineWithText(String text, float y) {
		Line line = new Line();
		TextElement tElement = new TextElement(y, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, text, 5);
		List<TextChunk> tList = new ArrayList<TextChunk>();
		tList.add(new TextChunk(tElement));
		line.setTextElements(tList);
		return line;
	}

	@Test
	public void testRemoveFirstLine() {
		List<Line> lines = new ArrayList<Line>();
		lines.add(lineWithText("1/2", 0));
		Line expectedFirstLine = lineWithText("not a page number", 10);
		lines.add(expectedFirstLine);
		lines.add(lineWithText("some text", 20));

		lines = new PageNumberFilter().filterLines(lines);
		assertEquals(2, lines.size());
		assertEquals(expectedFirstLine, lines.get(0));
	}

	@Test
	public void testRemoveLastLine() {
		List<Line> lines = new ArrayList<Line>();
		lines.add(lineWithText("wow", 0));
		lines.add(lineWithText("not a page number", 10));
		Line expectedLastLine = lineWithText("some text", 20);
		lines.add(expectedLastLine);
		lines.add(lineWithText("1/2", 30));

		lines = new PageNumberFilter().filterLines(lines);
		assertEquals(3, lines.size());
		assertEquals(expectedLastLine, lines.get(2));
	}
}
