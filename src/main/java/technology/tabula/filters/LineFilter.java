package technology.tabula.filters;

import technology.tabula.Line;
import java.util.List;


public interface LineFilter {
    List<Line> filterLines(List<Line> lines);
}
