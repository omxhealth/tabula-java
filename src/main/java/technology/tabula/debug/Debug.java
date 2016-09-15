package technology.tabula.debug;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import technology.tabula.Cell;
import technology.tabula.CommandLineApp;
import technology.tabula.Line;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.ProjectionProfile;
import technology.tabula.Rectangle;
import technology.tabula.Ruling;
import technology.tabula.StraightEdgeDetector;
import technology.tabula.Table;
import technology.tabula.TextChunk;
import technology.tabula.TextElement;
import technology.tabula.Utils;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;

public class Debug {

    private static final float CIRCLE_RADIUS = 5f;

    private static final Color[] COLORS = { new Color(27, 158, 119),
            new Color(217, 95, 2), new Color(117, 112, 179),
            new Color(231, 41, 138), new Color(102, 166, 30) };

    public static void debugIntersections(Graphics2D g, Page page) {
        int i = 0;
        for (Point2D p: Ruling.findIntersections(page.getHorizontalRulings(), page.getVerticalRulings()).keySet()) {
            g.setColor(COLORS[(i++) % 5]);
            g.fill(new Ellipse2D.Float((float) p.getX() - CIRCLE_RADIUS/2f, (float) p.getY() - CIRCLE_RADIUS/2f, 5f, 5f));
        }
    }

    private static void debugNonCleanRulings(Graphics2D g, Page page) {
        drawShapes(g, page.getUnprocessedRulings());
    }

    private static void debugRulings(Graphics2D g, Page page) {
        // draw detected lines
        List<Ruling> rulings = new ArrayList<Ruling>(page.getHorizontalRulings());
        rulings.addAll(page.getVerticalRulings());
        drawShapes(g, rulings);
    }

    private static void debugStraightEdges (Graphics2D g, Page page) {
        List<Float> bestEdges = StraightEdgeDetector.getBestPageEdges(page);

        int i = 0;
        for (Float p: bestEdges) {
            Ruling r = new Ruling(new Point2D.Float(p.floatValue(), (float) page.getTop()), new Point2D.Float(p.floatValue(), (float) page.getBottom()));
            g.setColor(COLORS[(i++) % 5]);
            drawShape(g, r);
        }
    }

    private static void debugColumnRegions(Graphics2D g, Page page) {
        List<TextChunk> textChunks = TextElement.mergeWords(page.getText());
        List<Line> lines = TextChunk.groupByLines(textChunks);
        List<Rectangle> columns = BasicExtractionAlgorithm.columnRegions(lines);
        drawShapes(g, columns);
    }

    private static void debugColumns(Graphics2D g, Page page) {
        List<TextChunk> textChunks = TextElement.mergeWords(page.getText());
        List<Line> lines = TextChunk.groupByLines(textChunks);
        List<Float> columns = BasicExtractionAlgorithm.columnPositions(lines);
        int i = 0;
        for(float p: columns) {
            Ruling r = new Ruling(new Point2D.Float(p, (float) page.getTop()), new Point2D.Float(p, (float) page.getBottom()));
            g.setColor(COLORS[(i++) % 5]);
            drawShape(g, r);
        }
    }

    private static void debugCharacters(Graphics2D g, Page page) {
        drawShapes(g, page.getText());
    }

    private static void debugTextChunks(Graphics2D g, Page page) {
        List<TextChunk> chunks = TextElement.mergeWords(page.getText(), page.getVerticalRulings());
        drawShapes(g, chunks);
    }

    private static void debugSpreadsheets(Graphics2D g, Page page) {
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page);
        drawShapes(g, tables);
    }

    private static void debugCells(Graphics2D g, Rectangle area, Page page) {
        List<Ruling> h = page.getHorizontalRulings();
        List<Ruling> v = page.getVerticalRulings();
        if (area != null) {
            h = Ruling.cropRulingsToArea(h, area);
            v = Ruling.cropRulingsToArea(v, area);
        }
        SpreadsheetExtractionAlgorithm ea = new SpreadsheetExtractionAlgorithm();
        List<Cell> cells = SpreadsheetExtractionAlgorithm.findCells(h, v);
        drawShapes(g, cells);
    }

    private static void debugDetectedTables(Graphics2D g, Page page) {
        NurminenDetectionAlgorithm detectionAlgorithm = new NurminenDetectionAlgorithm();
        List<Rectangle> tables = detectionAlgorithm.detect(page);
        drawShapes(g, tables);
    }

    private static void drawShapes(Graphics2D g, Collection<? extends Shape> shapes, Stroke stroke) {
        int i = 0;
        g.setStroke(stroke);
        for (Shape s: shapes) {
            g.setColor(COLORS[(i++) % 5]);
            drawShape(g, s);
        }
    }

    private static void drawShapes(Graphics2D g, Collection<? extends Shape> shapes) {
        drawShapes(g, shapes, new BasicStroke(2f));
    }

    private static void debugProjectionProfile(Graphics2D g, Page page) {
        float horizSmoothKernel = 0, vertSmoothKernel = 0;
        //for (Rectangle r: page.getText()) {
        for (Rectangle r: page.getText()) {
            horizSmoothKernel += r.getWidth();
            vertSmoothKernel += r.getHeight();
        }
        horizSmoothKernel /= page.getText().size();
        vertSmoothKernel /= page.getText().size();
        System.out.println("hsk: " + horizSmoothKernel + " vsk: " + vertSmoothKernel);
        //ProjectionProfile profile = new ProjectionProfile(page, page.getText(), horizSmoothKernel, vertSmoothKernel);
        ProjectionProfile profile = new ProjectionProfile(page, TextElement.mergeWords(page.getText(), page.getVerticalRulings()), horizSmoothKernel * 1.5f, vertSmoothKernel);
        float prec = (float) Math.pow(10, ProjectionProfile.DECIMAL_PLACES);


        float[] hproj = profile.getHorizontalProjection();
        float[] vproj = profile.getVerticalProjection();

        g.setStroke(new BasicStroke(1f));
        g.setColor(Color.RED);

        // hproj
        //Point2D last = new Point2D.Double(page.getLeft(), page.getBottom() - hproj[0] / prec), cur;
        Point2D last = new Point2D.Double(page.getLeft(), page.getBottom()), cur;
        for (int i = 0; i < hproj.length; i++) {
            cur = new Point2D.Double(page.getLeft() + i / prec, page.getBottom() - hproj[i]);
            g.draw(new Line2D.Double(last, cur));
            last = cur;
        }

        // hproj first derivative
        g.setColor(Color.BLUE);
        float[] deriv = ProjectionProfile.filter(ProjectionProfile
                .getFirstDeriv(profile.getHorizontalProjection()),
                0.01f);
        last = new Point2D.Double(page.getLeft(), page.getBottom());
        for (int i = 0; i < deriv.length; i++) {
            cur = new Point2D.Double(page.getLeft() + i / prec, page.getBottom() - deriv[i]);
            g.draw(new Line2D.Double(last, cur));
            last = cur;
        }

        // columns
        g.setColor(Color.MAGENTA);
        g.setStroke(new BasicStroke(1f));
        float[] seps = profile.findVerticalSeparators(horizSmoothKernel * 2.5f);
        for (int i = 0; i < seps.length; i++) {
            float x = (float) (page.getLeft() + seps[i]);
            g.draw(new Line2D.Double(x, page.getTop(), x, page.getBottom()));
        }

        // vproj
        g.setStroke(new BasicStroke(1f));
        g.setColor(Color.GREEN);
        last = new Point2D.Double(page.getLeft(), page.getTop());
        for (int i = 0; i < vproj.length; i++) {
            cur = new Point2D.Double(page.getLeft() + vproj[i] / prec, page.getTop() + i / prec);
            g.draw(new Line2D.Double(last, cur));
            last = cur;
        }

        // vproj first derivative
        g.setColor(new Color(0, 0, 1, 0.5f));
        deriv = ProjectionProfile.filter(ProjectionProfile.getFirstDeriv(vproj), 0.1f);
        last = new Point2D.Double(page.getRight(), page.getTop());
        for (int i = 0; i < deriv.length; i++) {
            cur = new Point2D.Double(page.getRight() - deriv[i] * 10, page.getTop() + i / prec);
            g.draw(new Line2D.Double(last, cur));
            last = cur;
        }

        // rows
        g.setStroke(new BasicStroke(1.5f));
        seps = profile.findHorizontalSeparators(vertSmoothKernel);
        for (int i = 0; i < seps.length; i++) {
            float y = (float) (page.getTop() + seps[i]);
            g.draw(new Line2D.Double(page.getLeft(), y, page.getRight(), y));
        }

    }

    private static void drawShape(Graphics2D g, Shape shape) {
        //g.setStroke(new BasicStroke(1));
        g.draw(shape);
    }

    public static void renderPage(String pdfPath, String outPath, int pageNumber, Rectangle area,
            boolean drawTextChunks, boolean drawSpreadsheets, boolean drawRulings, boolean drawIntersections,
            boolean drawStraightEdges, boolean drawColumnRegions, boolean drawColumns, boolean drawCharacters,
            boolean drawArea, boolean drawCells, boolean drawUnprocessedRulings, boolean drawProjectionProfile,
            boolean drawClippingPaths, boolean drawDetectedTables) throws IOException {
        PDDocument document = PDDocument.load(pdfPath);

        ObjectExtractor oe = new ObjectExtractor(document, true);

        Page page = oe.extract(pageNumber + 1);

        if (area != null) {
            page = page.getArea(area);
        }

        PDPage p = (PDPage) document.getDocumentCatalog().getAllPages().get(pageNumber);

//        PDFRenderer renderer = new PDFRenderer(document);
//        BufferedImage image = renderer.renderImage(pageNumber);

        BufferedImage image = p.convertToImage(BufferedImage.TYPE_INT_RGB, 72);

        Graphics2D g = (Graphics2D) image.getGraphics();

        if (drawTextChunks) {
            debugTextChunks(g, page);
        }
        if (drawSpreadsheets) {
            debugSpreadsheets(g, page);
        }
        if (drawRulings) {
            debugRulings(g, page);
        }
        if (drawIntersections) {
            debugIntersections(g, page);
        }
        if (drawStraightEdges) {
            debugStraightEdges(g, page);
        }
        if (drawColumnRegions) {
            debugColumnRegions(g, page);
        }
        if (drawColumns) {
            debugColumns(g, page);
        }
        if (drawCharacters) {
            debugCharacters(g, page);
        }
        if (drawArea) {
            g.setColor(Color.ORANGE);
            drawShape(g, area);
        }
        if (drawCells) {
            debugCells(g, area, page);
        }
        if (drawUnprocessedRulings) {
            debugNonCleanRulings(g, page);
        }
        if (drawProjectionProfile) {
            debugProjectionProfile(g, page);
        }
        if (drawClippingPaths) {
            drawShapes(g, oe.clippingPaths, new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 3f }, 0f));
        }
        if (drawDetectedTables) {
            debugDetectedTables(g, page);
        }

        document.close();

        ImageIOUtil.writeImage(image, outPath, 72);
    }

    @SuppressWarnings("static-access")
    private static Options buildOptions() {
        Options o = new Options();

        o.addOption("h", "help", false, "Print this help text.");
        o.addOption("r", "rulings", false, "Show detected rulings.");
        o.addOption("i", "intersections", false, "Show intersections between rulings.");
        o.addOption("s", "spreadsheets", false, "Show detected spreadsheets.");
        o.addOption("t", "textchunks", false, "Show detected text chunks (merged characters)");
        o.addOption("v", "straightedges", false, "Show vertical edges as detected by StraightEdgeDetector");
        o.addOption("c", "columns", false, "Show columns as detected by BasicExtractionAlgorithm");
        o.addOption("C", "columnregions", false, "Show column regions as detected by BasicExtractionAlgorithm");
        o.addOption("e", "characters", false, "Show detected characters");
        o.addOption("g", "region", false, "Show provided region (-a parameter)");
        o.addOption("l", "cells", false, "Show detected cells");
        o.addOption("u", "unprocessed-rulings", false, "Show non-cleaned rulings");
        o.addOption("f", "profile", false, "Show projection profile");
        o.addOption("n", "clipping-paths", false, "Show clipping paths");
        o.addOption("d", "detected-tables", false, "Show detected tables");

        o.addOption(OptionBuilder.withLongOpt("area")
                .withDescription("Portion of the page to analyze (top,left,bottom,right). Example: --area 269.875,12.75,790.5,561. Default is entire page")
                .hasArg()
                .withArgName("AREA")
                .create("a"));
        o.addOption(OptionBuilder.withLongOpt("pages")
                .withDescription("Comma separated list of ranges, or all. Examples: --pages 1-3,5-7, --pages 3 or --pages all. Default is --pages 1")
                .hasArg()
                .withArgName("PAGES")
                .create("p"));
        return o;
    }


    public static void main(String[] args) throws IOException {
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(buildOptions(), args );
            List<Integer> pages = new ArrayList<Integer>();
            if (line.hasOption('p')) {
                pages = Utils.parsePagesOption(line.getOptionValue('p'));
            }
            else {
                pages.add(1);
            }

            if (line.hasOption('h')) {
                printHelp();
                System.exit(0);
            }

            if (line.getArgs().length != 1) {
                throw new ParseException("Need one filename\nTry --help for help");
            }

            File pdfFile = new File(line.getArgs()[0]);
            if (!pdfFile.exists()) {
                throw new ParseException("File does not exist");
            }

            if (line.hasOption('g') && !line.hasOption('a')) {
                throw new ParseException("-g argument needs an area (-a)");
            }

            Rectangle area = null;
            if (line.hasOption('a')) {
                List<Float> f = CommandLineApp.parseFloatList(line.getOptionValue('a'));
                if (f.size() != 4) {
                    throw new ParseException("area parameters must be top,left,bottom,right");
                }
                area = new Rectangle(f.get(0), f.get(1), f.get(3) - f.get(1), f.get(2) - f.get(0));
            }

            if (pages == null) {
                // user specified all pages
                PDDocument document = PDDocument.load(pdfFile.getAbsolutePath());

                int numPages = document.getNumberOfPages();
                pages = new ArrayList<Integer>(numPages);

                for (int i = 1; i <= numPages; i++) {
                    pages.add(i);
                }

                document.close();
            }

            for (int i: pages) {
                renderPage(pdfFile.getAbsolutePath(),
                           new File(pdfFile.getParent(), removeExtension(pdfFile.getName()) + "-" + (i) + ".jpg").getAbsolutePath(),
                           i-1,
                           area,
                           line.hasOption('t'),
                           line.hasOption('s'),
                           line.hasOption('r'),
                           line.hasOption('i'),
                           line.hasOption('v'),
                           line.hasOption('C'),
                           line.hasOption('c'),
                           line.hasOption('e'),
                           line.hasOption('g'),
                           line.hasOption('l'),
                           line.hasOption('u'),
                           line.hasOption('f'),
                           line.hasOption('n'),
                           line.hasOption('d'));
            }
        }
        catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }



    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("tabula-debug", "Generate debugging images", buildOptions(), "", true);
    }

    private static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }




}
