package psimulator.userInterface.SimulatorEditor.DrawPanel.Support;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class GraphicUtils {
    
    /**
     * Gets middle point of line with two end points.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    public static Point getMiddlePoint(double x1, double y1, double x2, double y2){
        int x,y;
        x = (int)((Math.abs(x2 + x1)) / 2.0);
        y = (int)((Math.abs(y2 + y1)) / 2.0);
        
        return new Point(x,y);
    }
    
    /**
     * Gets intersection point of rectangle and line with one point inside and one outside
     * @param r
     * @param insidePoint
     * @param outsidePoint
     * @return 
     */
    public static Point getIntersectingPoint(Rectangle r, Point insidePoint, Point outsidePoint) {
        //Rectangle r = new Rectangle(getX(), getY(), bi.getWidth(), bi.getHeight());

        double s = (outsidePoint.getY() - insidePoint.getY()) / (outsidePoint.getX() - insidePoint.getX());

        Point intersectonPointX;
        Point intersectonPointY;


        if ((-r.height / 2.0 <= s * r.width / 2.0) && (s * r.width / 2.0 <= r.height / 2.0)) {
            if (outsidePoint.getX() > insidePoint.getX()) {
                // right edge
                //System.out.println("Right edge");
                intersectonPointX = new Point(r.x + r.width, r.y);
                intersectonPointY = new Point(r.x + r.width, r.y + r.height);
            } else {
                // left edge
                //System.out.println("Left edge");
                intersectonPointX = new Point(r.x, r.y);
                intersectonPointY = new Point(r.x, r.y + r.height);
            }
        } else {
            if (outsidePoint.getY() < insidePoint.getY()) {
                // top edge
                //System.out.println("Top edge");
                intersectonPointX = new Point(r.x, r.y);
                intersectonPointY = new Point(r.x + r.width, r.y);
            } else {
                // bottom edge
                //System.out.println("Bottom edge");
                intersectonPointX = new Point(r.x, r.y + r.height);
                intersectonPointY = new Point(r.x + r.width, r.y + r.height);
            }
        }
        return findLineIntersection(insidePoint, outsidePoint, intersectonPointX, intersectonPointY);
    }
    
    /**
     * Finds intersection of two lines
     * @param start1
     * @param end1
     * @param start2
     * @param end2
     * @return 
     */
    public static Point findLineIntersection(Point start1, Point end1, Point start2, Point end2) {
        float denom = (float) (((end1.getX() - start1.getX()) * (end2.getY() - start2.getY())) - ((end1.getY() - start1.getY()) * (end2.getX() - start2.getX())));

        float numer = (float) (((start1.getY() - start2.getY()) * (end2.getX() - start2.getX())) - ((start1.getX() - start2.getX()) * (end2.getY() - start2.getY())));

        float r = numer / denom;

        float numer2 = (float) (((start1.getY() - start2.getY()) * (end1.getX() - start1.getX())) - ((start1.getX() - start2.getX()) * (end1.getY() - start1.getY())));

        float s = numer2 / denom;

        int x = (int) (start1.getX() + (r * (end1.getX() - start1.getX())));
        int y = (int) (start1.getY() + (r * (end1.getY() - start1.getY())));
        // Find intersection point
        Point result = new Point(x, y);

        return result;
    }
}
