package org.nerdpower.tabula;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.nerdpower.tabula.extractors.ExtractionAlgorithm;

@SuppressWarnings("serial")
public class Table extends Rectangle {
    
    class CellPosition implements Comparable<CellPosition> {
        int row, col;
        CellPosition(int row, int col) {
            this.row = row; this.col = col;
        }
        
        @Override
        public boolean equals(Object other) {
            return other != null && this.row == ((CellPosition) other).row && this.col == ((CellPosition) other).col;
        }

        @Override
        public int compareTo(CellPosition other) {
           int rv = 0;
           if(this.row < other.row) {
               rv = -1;
           }
           else if (this.row > other.row) {
               rv = 1;
           }
           else if (this.col > other.col) {
               rv = 1;
           }
           else if (this.col < other.col) {
               rv = -1;
           }
           return rv;
        }
    }
    
    class CellContainer extends TreeMap<CellPosition, RectangularTextContainer> {
        
        public int maxRow = 0, maxCol = 0;
        
        public RectangularTextContainer get(int row, int col) {
            return this.get(new CellPosition(row, col));
        }
        
        public List<RectangularTextContainer> getRow(int row) {
            return new ArrayList<RectangularTextContainer>(this.subMap(new CellPosition(row, 0), new CellPosition(row, maxRow+1)).values());
        }
        
        @Override
        public RectangularTextContainer put(CellPosition cp, RectangularTextContainer value) {
            this.maxRow = Math.max(maxRow, cp.row);
            this.maxCol = Math.max(maxCol, cp.col);
            super.put(cp, value);
            return value;
        }
        
        @Override
        public RectangularTextContainer get(Object key) {
            return this.containsKey(key) ? super.get(key) : TextChunk.EMPTY;
        }
        
        public boolean containsKey(int row, int col) {
            return this.containsKey(new CellPosition(row, col));
        }
        
    }
    
    public static final Table EMPTY = new Table();
    
    CellContainer cellContainer = new CellContainer();
    Page page;
    ExtractionAlgorithm extractionAlgorithm;
    List<List<RectangularTextContainer>> rows = null;
    
    public ExtractionAlgorithm getExtractionAlgorithm() {
        return extractionAlgorithm;
    }
    
    public Table() {
        super();
    }

    public Table(Page page, ExtractionAlgorithm extractionAlgorithm) {
        this();
        this.page = page;
        this.extractionAlgorithm = extractionAlgorithm;
    }

    public void add(RectangularTextContainer tc, int i, int j) {
        this.merge(tc);
        this.cellContainer.put(new CellPosition(i, j), tc);
        this.rows = null; // clear the memoized rows
    }
    
    public List<List<RectangularTextContainer>> getRows() {
        if (this.rows != null) {
            return this.rows;
        }
        
        this.rows = new ArrayList<List<RectangularTextContainer>>();
        for (int i = 0; i <= this.cellContainer.maxRow; i++) {
            List<RectangularTextContainer> lastRow = new ArrayList<RectangularTextContainer>(); 
            this.rows.add(lastRow);
            for (int j = 0; j <= this.cellContainer.maxCol; j++) {
                lastRow.add(this.cellContainer.containsKey(i, j) ? this.cellContainer.get(i, j) : TextChunk.EMPTY);
            }
        }
        return this.rows;
    }
    
    public List<List<RectangularTextContainer>> getCols() {
        return Utils.transpose(this.getRows());
    }
}