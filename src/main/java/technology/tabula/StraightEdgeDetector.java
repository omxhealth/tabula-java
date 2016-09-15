package technology.tabula;

import technology.tabula.Page;
import technology.tabula.TextChunk;
import technology.tabula.TextElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Reads text chunks to find non-ragged edges from a spreadhsheet. These can then
 * be used as vertical rulings.
 */
public class StraightEdgeDetector {
    // uses very simple edge comparison, on exact floating point values. For
    // numerical stability, this could be done using an epsilon comparison,
    // but
    private HashMap<Float, Integer> edgeCounts = new HashMap<Float, Integer>();
    private int maxEdgeCount = 0;

    public int distinctEdges() {
      return edgeCounts.size();
    }

    public void countEdge(float edge) {
      Float boxed = new Float(edge);
      int newValue = getCurrentEdgeCount(edge) + 1;
      edgeCounts.put(edge, new Integer(newValue));
      maxEdgeCount = Math.max(maxEdgeCount, newValue);
    }

    public List<Float> significantEdgePositions() {
      ArrayList<Float> significantEdges = new ArrayList<Float>();
      for (Float edge: edgeCounts.keySet()) {
        if (edgeCounts.get(edge).intValue() * 2 < maxEdgeCount) continue;
        // simple heuristic to discard unimportant edges. The idea is that for significant
        // edges, basically every row should have something starting at that column
        significantEdges.add(edge);
      }
      Collections.sort(significantEdges);
      return significantEdges;
    }

    public static List<Float> getBestPageEdges(Page page) {
      List<TextChunk> chunks = TextElement.mergeWords(page.getText());
      StraightEdgeDetector leftEdges = detectLeftTextEdges(chunks);
      StraightEdgeDetector rightEdges = detectRightTextEdges(chunks);

      // return the detector with the least edges. ragged edges should produce
      // many more edges, so this should select the side with straight edges
      if (leftEdges.distinctEdges() < rightEdges.distinctEdges()) return leftEdges.significantEdgePositions();
      return rightEdges.significantEdgePositions();
    }

    public static StraightEdgeDetector detectLeftTextEdges(List<TextChunk> chunks) {
      StraightEdgeDetector detector = new StraightEdgeDetector();
      for (TextChunk chunk: chunks) {
          Float left = new Float(chunk.getLeft());
          detector.countEdge(left);
      }

      return detector;
    }

    public static StraightEdgeDetector detectRightTextEdges(List<TextChunk> chunks) {
      StraightEdgeDetector detector = new StraightEdgeDetector();
      for (TextChunk chunk: chunks) {
          Float left = new Float(chunk.getRight());
          detector.countEdge(left);
      }

      return detector;
    }

    private int getCurrentEdgeCount(Float key) {
      if (!edgeCounts.containsKey(key)) edgeCounts.put(key, new Integer(0));
      return edgeCounts.get(key).intValue();
    }
}
