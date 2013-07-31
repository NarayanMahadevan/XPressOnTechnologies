// RGPT PACKAGES
package com.rgpt.util;

import java.awt.*;
import java.util.*;
import java.awt.geom.*;

public class RGPTShapeUtil
{
   public static GeneralPath drawPath(String pathSelId, AffineTransform affine, 
                             Vector<Point2D.Double> gPathPoints, Graphics g)
   {
      // Drawing the GPath to get the 
      // The functions in Shape Util draw the shape using the Graphics and Path Points
      if (pathSelId == RGPTActionListener.ImageActions.SHAPE_LINE_PATH_MENU.toString()) 
         return drawLine(affine, gPathPoints, g);
      else if (pathSelId == RGPTActionListener.ImageActions.SHAPE_QUAD_PATH_MENU.toString()) 
         return drawQuadCurve(affine, gPathPoints, g);
      else if (pathSelId == RGPTActionListener.ImageActions.SHAPE_CUBIC_PATH_MENU.toString()) 
         return drawBezierCurve(affine, gPathPoints, null);
      return null;
   }
   
   public static Shape drawCircle(AffineTransform affine, double radius, 
                                        Point2D.Double centerPt, Graphics g)
   { 
      return drawCircle(affine, radius, Color.black, centerPt, g);
   }
   
   public static Shape drawCircle(AffineTransform affine, double radius, 
                                  Color color, Point2D.Double centerPt, Graphics g)
   { 
      Graphics2D g2d = null;
      if (affine == null) { affine = new AffineTransform(); affine.setToIdentity(); }
      boolean drawPath = true; Color origColor = null;
      if (g == null) drawPath = false;
      else { origColor = g.getColor(); g2d = (Graphics2D) g; }
      Shape theCircle = new Ellipse2D.Double(centerPt.x-radius, centerPt.y-radius, 
                                             2.0 * radius, 2.0 * radius);
      if (drawPath) { g.setColor(color); g2d.draw(theCircle); g.setColor(origColor); }
      return theCircle;
   }
   
   public static GeneralPath drawLine(AffineTransform affine, 
                             Vector<Point2D.Double> gPathPoints, Graphics g)
   { return drawLine(g, affine, Color.blue, Color.black, gPathPoints); }
   
   public static GeneralPath drawLine(Graphics g, AffineTransform affine, Color ptColor, 
                                      Color lineColor, Vector<Point2D.Double> gPathPoints)
   {
      if (affine == null) { affine = new AffineTransform(); affine.setToIdentity(); }
      boolean drawPath = true; Color origColor = null;
      if (g == null) drawPath = false;
      else origColor = g.getColor();
      int ptSize = 4;
      Point2D.Double pt;
      if (gPathPoints.size() == 0) return null;
      // RGPTLogger.logToFile("Path Points: "+gPathPoints);
      Graphics2D g2d = (Graphics2D) g;
      GeneralPath gPath = new GeneralPath();
      if (gPathPoints.size() == 1) {
         if (drawPath) drawPoint(affine, g2d, gPathPoints.elementAt(0), 1, Color.BLACK);
         return null;
      }
      for (int i = 0; i < gPathPoints.size(); i++) {
         pt = gPathPoints.elementAt(i);
         Point2D.Double affinePt = (Point2D.Double) affine.transform(pt, null);   
         
         if (drawPath) {
            if (ptColor != null) {
               g.setColor(ptColor);
               g.fillOval((int)affinePt.x - ptSize, (int)affinePt.y - ptSize, 2*ptSize, 2*ptSize);         
            }
            g2d.setColor(lineColor);
         }
         if (i == 0) {
            gPath.moveTo(pt.x, pt.y);
            continue;
         }
         gPath.lineTo(pt.x, pt.y);     
      }
      gPath = (GeneralPath) gPath.createTransformedShape(affine);
      if (drawPath) { g2d.draw(gPath); g.setColor(origColor); }
      return gPath;
   }
   
   public static GeneralPath drawBezierCurve(AffineTransform affine, 
                             Vector<Point2D.Double> gPathPoints, Graphics g)
   {
      if (affine == null) { affine = new AffineTransform(); affine.setToIdentity(); }
      boolean drawPath = true;
      if (g == null) drawPath = false;
      int pixelSize = 1;
      Point2D.Double pt;
      if (gPathPoints.size() == 0) return null;
      // if (gPathPoints.size() % 2 == 0) return  null;
      // System.out.println("Path Points Size: "+gPathPoints.size() + 
                         // " Mod2: "+gPathPoints.size() % 2 );
      Point2D.Double[] points = gPathPoints.toArray(new Point2D.Double[0]);
      Graphics2D g2 = null;
      AffineTransform origAffine = null;
      if (drawPath) {
         g2 = (Graphics2D) g;
         origAffine = g2.getTransform();
      }
      GeneralPath gPath = new GeneralPath();
      gPath.moveTo(points[0].x, points[0].y);
      if (points.length == 1) {
         if (drawPath) drawPoint(affine, g2, points[0], pixelSize, Color.BLACK);
         return null;
      }
      for (int i = 1; i < points.length; i += 3) {
         if (i+1 == points.length) gPath.lineTo(points[i].x, points[i].y);
         else if (i+2 == points.length) 
            gPath.quadTo(points[i].x, points[i].y, points[i+1].x, points[i+1].y);
         else gPath.curveTo(points[i].x, points[i].y, points[i+1].x, points[i+1].y, 
                           points[i+2].x, points[i+2].y);
      }
      Color ptColor;
      if (drawPath) 
      {
         drawPoint(affine, g2, points[0], pixelSize, Color.BLUE);      
         for (int i = 1; i < points.length;  i += 3) 
         {
            if (i+1 == points.length) {
                drawPoint(affine, g2, points[i], pixelSize, Color.CYAN);
            } else if (i+2 == points.length) {
               drawPoint(affine, g2, points[i], pixelSize, Color.CYAN);
               drawPoint(affine, g2, points[i+1], pixelSize, Color.CYAN);
            } else {
               for (int cntr = 0; cntr < 3; cntr++) {
                  int ptCntr = i + cntr;
                  if (cntr == 2) g2.setColor(Color.blue);
                  else g2.setColor(Color.cyan);
                  Point2D.Double affinePt = (Point2D.Double) affine.transform(points[ptCntr], null);   
                  g2.fill( new Ellipse2D.Double(affinePt.x - 4*pixelSize, 
                               affinePt.y - 4*pixelSize, 8*pixelSize, 8*pixelSize) );
                  // System.out.println("Drawing Point at PtCntr: "+ptCntr);
               }
            }
         } // for (int i = 1; i < points.length;  i += 3)
      }
      gPath = (GeneralPath) gPath.createTransformedShape(affine);
      if (drawPath) {
         g2.setColor(Color.DARK_GRAY);
         Stroke origStroke = g2.getStroke();      
         g2.setStroke(new BasicStroke(0.5f));
         g2.draw(gPath);  
         g2.setStroke(origStroke);
         g2.transform(origAffine);
      }
      return gPath;
   }
   
   public static GeneralPath drawQuadCurve(AffineTransform affine, 
                             Vector<Point2D.Double> gPathPoints, Graphics g)
   {
      if (affine == null) { affine = new AffineTransform(); affine.setToIdentity(); }
      boolean drawPath = true;
      if (g == null) drawPath = false;
      int pixelSize = 1;
      Point2D.Double pt;
      if (gPathPoints.size() == 0) return null;
      // if (gPathPoints.size() % 2 == 0) return null;
      // System.out.println("Path Points Size: "+gPathPoints.size() + 
                         // " Mod2: "+gPathPoints.size() % 2 );
      Point2D.Double[] points = gPathPoints.toArray(new Point2D.Double[0]);
      Graphics2D g2 = (Graphics2D) g;
      GeneralPath gPath = new GeneralPath();
      gPath.moveTo(points[0].x, points[0].y);
      if (points.length == 1) {
         if (drawPath) drawPoint(affine, g2, points[0], pixelSize, Color.CYAN);
         return null;
      }
      for (int i = 1; i < points.length; i += 2) {
         if (i+1 == points.length) gPath.lineTo(points[i].x, points[i].y);
         else gPath.quadTo(points[i].x, points[i].y, points[i+1].x, points[i+1].y);
      }
      for (int i = 0; i < points.length;  i++) {
         if (drawPath) {
            if ( (i & 1) == 1)
               g2.setColor(Color.CYAN);
            else
               g2.setColor(Color.BLUE);
            Point2D.Double affinePt = (Point2D.Double) affine.transform(points[i], null);   
            g2.fill( new Ellipse2D.Double(affinePt.x - 4*pixelSize, 
                         affinePt.y - 4*pixelSize, 8*pixelSize, 8*pixelSize) );
         }
      }
      gPath = (GeneralPath) gPath.createTransformedShape(affine);
      if (drawPath) {
         g2.setColor(Color.DARK_GRAY);
         // g2.setStroke( new BasicStroke(1) );
         g2.draw(gPath); 
      }
      return gPath;
   }
   
   public static void drawBoundary(Graphics2D g2d, Rectangle bounds, Point2D.Double[] pts)
   {
      Color smokey = new Color(128, 128, 128, 128);
      drawBoundary(g2d, bounds, pts, smokey, true);
   }
   
   public static void drawBoundary(Graphics2D g2d, Rectangle bounds, Point2D.Double[] pts, 
                                   Color color, boolean drawSqPt)
   {
      g2d.setPaint(Color.BLACK);
      BasicStroke basicStroke = new BasicStroke(0.25f);            
      Stroke origStroke = g2d.getStroke();
      g2d.setStroke(basicStroke);
      g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                           java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.draw(bounds);
      basicStroke = new BasicStroke(1.0f);            
      g2d.setStroke(basicStroke);      
      for(int i = 0; i < pts.length; i++) {
         if (drawSqPt) drawSquarePoint(g2d, pts[i], 1, color);
         else drawPoint(g2d, pts[i], 1, color);
      }
      g2d.setStroke(origStroke);
   }
   
   public static void drawSquarePoint(Graphics2D g2, Point2D.Double pt, int ptSize, Color color)
   {
      Color origColor = g2.getColor();
      g2.setColor(color);
      Rectangle2D.Double ptRect = new Rectangle2D.Double(pt.x - 4*ptSize, pt.y - 4*ptSize, 
                                                         8*ptSize, 8*ptSize);
      g2.fill(ptRect); g2.draw(ptRect);
      g2.setColor(origColor);
   }
   
   public static void drawPoint(Graphics2D g2, Point2D.Double pt, int ptSize, Color color)
   {
      AffineTransform affine = new AffineTransform();
      affine.setToIdentity();
      drawPoint(affine, g2, pt, ptSize, color);
   }
   
   public static void drawPoint(AffineTransform affine, Graphics2D g2, Point2D.Double point, 
                                int ptSize, Color color)
   {
      Point2D.Double pt = (Point2D.Double) affine.transform(point, null);   
      Color origColor = g2.getColor();
      g2.setColor(color);
      g2.fill( new Ellipse2D.Double(pt.x - 4*ptSize, 
                      pt.y - 4*ptSize, 8*ptSize, 8*ptSize) );
      g2.setColor(origColor);
   }
   
   public static boolean saveShape(String gPathSel, AffineTransform affine, 
                         Rectangle pathBounds, Vector<Point2D.Double> gPathPoints, 
                         String serFilePath)
   {
      GeneralPath gPath = null;
      Point2D.Double[] boundPts = null;
      Rectangle newBounds = new Rectangle(100, 100);
      double sx = (double)newBounds.width/(double)pathBounds.width; 
      double sy = 1.0;
      if (pathBounds.height > 0) sy = (double)newBounds.height/(double)pathBounds.height;
      affine = RGPTUtil.getAffineTransform(affine, pathBounds, sx, sy, 0);
      sx = affine.getScaleX(); sy = affine.getScaleY();
      System.out.println("New Affine: "+affine);
      System.out.println("sx: "+sx+" sy: "+sy);
      if (gPathSel.equals("line")) 
         gPath = drawLine(affine, gPathPoints, null);
      else if (gPathSel.equals("quad_curve")) 
         gPath = drawQuadCurve(affine, gPathPoints, null);
      else if (gPathSel.equals("bezier_curve")) 
         gPath = drawBezierCurve(affine, gPathPoints, null);
      else return false;
      pathBounds = gPath.getBounds();
      boundPts = RGPTUtil.getRectPoints(pathBounds, 8);
      Vector<Point2D.Double> transGPathPts = RGPTUtil.getTransformedPt(affine, gPathPoints);
      Point2D.Double[] points = transGPathPts.toArray(new Point2D.Double[0]);      
      System.out.println("sx: "+sx+" sy: "+sy+" pathBounds: "+pathBounds);
      boolean tranNotFound = true;
      int tx = 0, ty = 0;
      Point transPt = new Point(50, 50);
      tx = transPt.x - pathBounds.x;
      ty = transPt.y - pathBounds.y;
      System.out.println("tx: "+tx+" ty: "+ty);
      RGPTUtil.adjustPt(affine, points, tx, ty);
      // this.resetPathObjects(false); // This is essentially done for repaint to reset all the path objects
      // repaint();
      // String serFilePath = "shapes/"+this.gPathSel+"1.ser";
      // System.out.println("serFilePath: "+serFilePath);
      try {
         RGPTUtil.serializeObject(serFilePath, transGPathPts);
      } catch(Exception ex) { ex.printStackTrace(); }
      return true;
   }
   
}
