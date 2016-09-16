package technology.tabula.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import technology.tabula.Line;
import technology.tabula.TextChunk;


public class PageNumberFilter implements LineFilter {
    // regex to match lines that look like "   1/2  "
    private static final Pattern PAGE_NUMBER_PATTERN = Pattern.compile("\\A\\s*\\d+\\s*/\\s*\\d+\\s*\\z");


    public List<Line> filterLines(List<Line> lines) {
      Collections.sort(lines);
      // just check first and last lines, to avoid messing up anything in the middle of the page
      if (isLineNumber(lines.get(0))) lines.remove(0);
      if (isLineNumber(lines.get(lines.size() - 1))) lines.remove(lines.size() - 1);
      return lines;
    }

    private static boolean isLineNumber(Line line) {
      return PAGE_NUMBER_PATTERN.matcher(lineTextContent(line)).matches();
    }

    private static String lineTextContent(Line line) {
        StringBuilder sb = new StringBuilder();
        for (TextChunk text: line.getTextElements()) {
            sb.append(text.getText());
        }
        return sb.toString();
    }
}
