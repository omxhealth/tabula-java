package technology.tabula;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import technology.tabula.Page;
import technology.tabula.StraightEdgeDetector;
import technology.tabula.UtilsForTesting;

public class TestStraightEdgeDetector {
    @Test
    public void testLeftAlignedText() throws IOException {
      Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/straight-edges.pdf", 1);
      List<Float> bestEdges = StraightEdgeDetector.getBestPageEdges(page);
      assertEquals(bestEdges.size(), 3);

      assertEquals(bestEdges.get(0).floatValue(), 57f, 1f);
      assertEquals(bestEdges.get(1).floatValue(), 269f, 1f);
      assertEquals(bestEdges.get(2).floatValue(), 392f, 1f);
    }
}
