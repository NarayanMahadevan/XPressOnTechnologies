// XONImagingEngine is the utility class which does Image Processing and Creation
// It processes the data from XODImageInfo Object to create the Imagery.

// RGPT PACKAGES
package com.rgpt.imageutil;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.border.*;

import com.rgpt.templateutil.VDPFieldInfo;
import com.rgpt.templateutil.VDPImageFieldInfo;
import com.rgpt.templateutil.VDPTextFieldInfo;
import com.rgpt.templateutil.XODImageInfo;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTShapeUtil;
import com.rgpt.util.RGPTUtil;

public class XONImagingEngine 
{  
   public static void createXONImage(Graphics2D g2d, XODImageInfo imgInfo)
   {
      // Drawing the Main Background Image. Windows Device Matrix will be an Identity Matrix is the Image Size 
      // and Panel Size are the same. This will be true on the Server Side when XOD is getting Processed
      // g2d.drawImage(imgInfo.m_CanvasImageHolder.m_ClippedImage, imgInfo.m_WindowDeviceCTM, null);      
      Rectangle bounds = imgInfo.m_CanvasImageField.m_ScreenBounds; 
      int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
      setAlphaValue(g2d, imgInfo.m_CanvasImageField);
      g2d.drawImage(imgInfo.m_CanvasImageHolder.m_ClippedImage, x, y, w, h, null);
      drawImageFilterParam(g2d, imgInfo.m_CanvasImageField);
   
      // Drawing the VDP Fields
      VDPFieldInfo vdpFld = null;  boolean isFldSel = false, showElem = false;
      Vector<VDPFieldInfo> vdpFldList = imgInfo.m_VDPFieldInfoList;
      for (int i = 0; i < vdpFldList.size(); i++)
      {
         isFldSel = false; showElem = false;
         vdpFld =  vdpFldList.elementAt(i);
         if (vdpFld instanceof VDPImageFieldInfo) {
            VDPImageFieldInfo vdpImgFld = (VDPImageFieldInfo) vdpFld;
            if (vdpImgFld.m_IsBackgroundImage) continue;
         }
         if (vdpFld.m_IsFieldSelected) isFldSel = true;
         if (vdpFld.m_ShowElement) showElem = true;
         // RGPTLogger.logToFile("VDPFieldInfo: "+vdpFld.m_FieldName+
                              // " isFldVisible: "+vdpFld.m_IsFieldSelected+
                              // " showFld: "+vdpFld.m_ShowElement);
         if (!showElem && !isFldSel) continue;
         if (vdpFld.m_FieldType.equals("Image")) 
            drawField(g2d, (VDPImageFieldInfo) vdpFld, imgInfo.m_WindowDeviceCTM);
         if (vdpFld.m_FieldType.equals("Text")) drawField((VDPTextFieldInfo) vdpFld);
      }
         
   }
   
   public static void drawField(Graphics2D g2d, VDPImageFieldInfo vdpFld, 
                                AffineTransform affine)
   {
      vdpFld.m_ScreenPathPoints = RGPTUtil.getTransformedPt(affine, vdpFld.m_GPathPoints);      
      Graphics2D drawG = null; if (vdpFld.m_ShowPathPoints) drawG = g2d;
      GeneralPath gPath = RGPTShapeUtil.drawPath(vdpFld.m_PathSelection, null, 
                                                 vdpFld.m_ScreenPathPoints, drawG);
      vdpFld.m_ScreenBounds = gPath.getBounds();
      if (vdpFld.m_ShowScaleBounds) {
         Point2D.Double[] boundPts = RGPTUtil.getRectPoints(vdpFld.m_ScreenBounds, 8);
         RGPTShapeUtil.drawBoundary(g2d, vdpFld.m_ScreenBounds, boundPts, 
                                    Color.BLUE, true); 
      } else if (vdpFld.m_ShowRotBounds) {
         Point2D.Double[] boundPts = RGPTUtil.getRectPoints(vdpFld.m_ScreenBounds, 4);
         RGPTShapeUtil.drawBoundary(g2d, vdpFld.m_ScreenBounds, boundPts, 
                                    Color.BLUE, false); 
      }
      // RGPTLogger.logToFile("ScreenBounds: "+vdpFld.m_ScreenBounds);
      Color origClr = g2d.getColor(); Stroke origStroke = g2d.getStroke(); 
      Shape origClip = g2d.getClip(); Composite origComp = g2d.getComposite();
      if (vdpFld.m_IsFieldSelected) {
         g2d.setColor(Color.DARK_GRAY); g2d.setStroke(new BasicStroke(0.5f));
         g2d.draw(vdpFld.m_ScreenBounds);  
         g2d.setColor(origClr);  g2d.setStroke(origStroke);
      }
      g2d.setClip(gPath);
      BufferedImage adjImage = vdpFld.m_ImageHolder.m_ClippedImage;
      if (vdpFld.m_RotAngle > 0.0D)
         adjImage = ImageUtils.rotateImage(vdpFld.m_ImageHolder.m_ClippedImage, 
                                           vdpFld.m_RotAngle);
      setAlphaValue(g2d, vdpFld);
      Rectangle bounds = vdpFld.m_ScreenBounds; 
      int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
      g2d.drawImage(adjImage, x, y, w, h, null);
      g2d.setClip(origClip); g2d.setComposite(origComp);
      drawImageFilterParam(g2d, vdpFld);
   }
   
   private static void setAlphaValue(Graphics2D g2d, VDPImageFieldInfo vdpFld)
   {
      if (vdpFld.m_AlphaValue < 1.0D) {
         int rule = AlphaComposite.SRC_OVER;  
         AlphaComposite ac = AlphaComposite.getInstance(rule, (float)vdpFld.m_AlphaValue);
         g2d.setComposite(ac);
      }
   }
   
   private static void drawImageFilterParam(Graphics2D g2d, VDPImageFieldInfo vdpFld)
   {
      StringBuffer ctrlPtsBuf = new StringBuffer();
      if (vdpFld.isCircleFilter()) {         
         Vector<Point2D.Double> filterCtrlPoints = null;
         filterCtrlPoints = vdpFld.getFilterControlPoints("RotAngPt");
         RGPTShapeUtil.drawLine(null, filterCtrlPoints, g2d);
         filterCtrlPoints = vdpFld.getFilterControlPoints("SpreadPt");
         RGPTShapeUtil.drawLine(null, filterCtrlPoints, g2d);
         filterCtrlPoints = vdpFld.getFilterControlPoints("RadialPt");
         RGPTShapeUtil.drawLine(null, filterCtrlPoints, g2d);
      } else if (vdpFld.isWarpFilter()) {
         Map<String, Object> ctrlValues = vdpFld.getImageFilterControlValues(); 
         // drawing horizontal line
         int numGrid = RGPTParams.getIntVal("NumOfGrid"); 
         Vector<Point2D.Double> srcGridPtList=null, desGridPtList=null, gridPtLists=null; 
         srcGridPtList = (Vector<Point2D.Double>) ctrlValues.get("SrcGirdPts"); 
         desGridPtList = (Vector<Point2D.Double>) ctrlValues.get("DesGirdPts"); 
         Point2D.Double[] srcGridPts = srcGridPtList.toArray(new Point2D.Double[0]);
         Point2D.Double[] desGridPts = desGridPtList.toArray(new Point2D.Double[0]);
         Point2D.Double[] gridPts = new Point2D.Double[numGrid];
         for (int row = 0; row < numGrid; row++) {
            int startInd = row*numGrid, endInd = startInd+numGrid-1;
            // System.arraycopy(srcGridPts, startInd, gridPts, 0, numGrid);
            // gridPtLists = new Vector<Point2D.Double>(Arrays.asList(gridPts));
            // RGPTShapeUtil.drawLine(null, gridPtLists, g2d);
            System.arraycopy(desGridPts, startInd, gridPts, 0, numGrid);
            gridPtLists = new Vector<Point2D.Double>(Arrays.asList(gridPts));
            RGPTShapeUtil.drawLine(null, gridPtLists, g2d);
         }
      } else if (vdpFld.isRadialFilter(ctrlPtsBuf)) { 
         String[] ctrlKeys = ctrlPtsBuf.toString().split("::");
         Vector<Point2D.Double> filterCtrlPoints = null;
         if (RGPTUtil.contains(ctrlKeys, "SliderPt")) {
            filterCtrlPoints = vdpFld.getFilterControlPoints("SliderPt");
            RGPTShapeUtil.drawLine(null, filterCtrlPoints, g2d);
         }
         if (RGPTUtil.contains(ctrlKeys, "AnglePt")) {
            filterCtrlPoints = vdpFld.getFilterControlPoints("AnglePt");
            RGPTShapeUtil.drawLine(null, filterCtrlPoints, g2d);
         }
         if (RGPTUtil.contains(ctrlKeys, "RadialPt")) {
            filterCtrlPoints = vdpFld.getFilterControlPoints("RadialPt");
            Map<String, Object> ctrlValues = vdpFld.getImageFilterControlValues(); 
            Point2D.Double centerPt = (Point2D.Double) ctrlValues.get("CenterPt");
            float radius = ((Float) ctrlValues.get("Radius")).floatValue();
            if (vdpFld.isRadialAdjFilter()) 
               RGPTShapeUtil.drawCircle(null, radius, centerPt, g2d);               
            RGPTShapeUtil.drawLine(null, filterCtrlPoints, g2d);
         }
      } 
   }
   
   private static void drawField(VDPTextFieldInfo vdpFld){}
}