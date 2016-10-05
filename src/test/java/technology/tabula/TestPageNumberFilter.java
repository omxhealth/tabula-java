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
		List<Line> lines = lastLinePageNumber("1/2", 4);
		Line expectedLastLine = lines.get(2);

		lines = new PageNumberFilter().filterLines(lines);
		assertEquals(3, lines.size());
		assertEquals(expectedLastLine, lines.get(2));
	}

	@Test
	public void testDifferentPatterns() {
		String[] numbers = {"1/2", "1", "32", "page 1", "page 1 of 3", "PAGE 1 of 3"};
		for (String number: numbers) {
			List<Line> lines = lastLinePageNumber(number, 5);
			Line expectedLastLine = lines.get(3);

			lines = new PageNumberFilter().filterLines(lines);
			assertEquals("failed to remove last line: (" + number + ")", 4, lines.size());
			assertEquals(expectedLastLine, lines.get(3));
		}
	}

	private List<Line> lastLinePageNumber(String pageNumber, int count) {
		List<Line> lines = new ArrayList<Line>();

		for (int i = 0; i < count - 1; i++) {
			if (i % 3 == 0)
				lines.add(lineWithText("wow", count * 10 + 10));
			else if (i % 3 == 1)
				lines.add(lineWithText("some text", count * 10 + 10));
			else
				lines.add(lineWithText("not a page number", count * 10 + 10));
		}
		lines.add(lineWithText(pageNumber, count * 10 + 10));

		return lines;
	}
}
