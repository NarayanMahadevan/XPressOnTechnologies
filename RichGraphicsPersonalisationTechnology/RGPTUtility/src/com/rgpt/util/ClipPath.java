 // RGPT PACKAGES
package com.rgpt.util;

// This classes are used to draw General Path
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;

// This files are added to support serialization
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.RuntimeException;

import com.rgpt.templateutil.PDFPageInfo;

// ZSelection - ("ClipSegmentCount", ++pathno); ("ClipPath"+pathno, zpath); Line 550
// ZPath.namevalue - ("StrokeColor", Color) => zpath.stroked = true; ("FillColor", Color); ("Pathmtx", gs.getTransform()); 
// ("LineWidth", (double) gs.getLineWidth() );("LineCap", (int) gs.getLineCap() );("LineJoin", (int) gs.getLineJoin() );
// ("MitreLimit", (double) gs.getMiterLimit() ); ("Dashes", double[] gs.getDashes() );("DashPhase", (double) gs.getPhase());
// ("PathPoints",(double[]) path.getPathPoints()); ("PathTypes",(byte[]) path.getPathTypes());
// ("ElementCTM", path.getCTM());

public class ClipPath implements Serializable
{

   // This constants match the constants defined in PDFNet Element class. The PathType 
   // array defines the path and the Path Points defines the corresponding points 
   // need to draw general path.
   public static final int CUBIC_TO = 3;
   public static final int LINE_TO = 2;
   public static final int MOVE_TO = 1; 
   public static final int RECT = 5; 
   public static final int NULL = 0; 
   public static final int CLOSE_PATH = 6; 

   // public transient GeneralPath m_ClipShape;
   
   // Transformation Matrix for the Path and the Element.
   public AffineTransform m_PathCTM;
   public AffineTransform m_ElementCTM;

   public int m_StrokeColorPt;
   public int m_FillColorPt;
   
   public transient Color m_FillColor;
   public transient Color m_StrokeColor;
   
   public boolean m_IsStroked;
   public boolean m_IsFilled;
	
   public double[] m_PathPoints;
   public byte[] m_PathTypes;

   // This defines the BasicStroke for rendering the Path.
   public transient BasicStroke m_PathStroke;
   public float m_LineWidth;
   public int m_LineCap;
   public int m_LineJoin;
   public float m_MitreLimit;
   public float[] m_Dashes;
   public float m_DashPhase;

   public ClipPath() {}
   
   public void buildProperFields()
   {
      m_FillColor = new Color(m_FillColorPt);
      m_StrokeColor = new Color(m_StrokeColorPt);
      try
      {
         m_PathStroke = new BasicStroke(m_LineWidth, m_LineCap, m_LineJoin, 
                                   m_MitreLimit, m_Dashes, m_DashPhase);      
      }
      catch (Exception ex)
      {
         RGPTLogger.logToFile("EXCEPTION CREATING BASIC STROKE: " + ex.getMessage());
         m_PathStroke = new BasicStroke(2.0f);
      }
   }
   
   public GeneralPath getClipShape(PDFPageInfo pdfPageInfo, Point2D.Double startPt)
   {
      return this.getClipShape(pdfPageInfo.m_PageCTM, 
                           pdfPageInfo.m_CalcDeviceCTM, null, startPt);
   }
   
   public GeneralPath getClipShape(AffineTransform pageMatrix, 
                                   AffineTransform devMatrix, 
                                   AffineTransform rotMatrix, 
                                   Point2D.Double startPt)
   {
		boolean isClosePath = false;
      //RGPTLogger.logToFile("\n----GET CLIP SHAPE------\n");
      StringBuffer mesg = new StringBuffer();
      // Getting the Clone of Device CTMThis is because the Device CTM will be 
      // manipulated after Multiplication with PageCTM
      // Getting the Clone of PageCTM. This is to avoid any manipulation of Page CTM 
      AffineTransform pageCTM = (AffineTransform) pageMatrix.clone();
      AffineTransform devCTM = (AffineTransform) devMatrix.clone();
      AffineTransform pathCTM = (AffineTransform) m_PathCTM.clone();
      AffineTransform elemCTM = (AffineTransform) m_ElementCTM.clone();
      
      AffineTransform rotCTM = null;
      if (rotMatrix != null) rotCTM = (AffineTransform) rotMatrix.clone();
      
      // Creating Final Transformation Matrix by Multiplying PathCTM with Element 
      // CTM. The resultant with the Page CTM and finally with the Device CTM to 
      // get the final transformation matrix.
      elemCTM.concatenate(pathCTM);
      pageCTM.concatenate(elemCTM);
      if (rotCTM != null)
      {
         rotCTM.concatenate(pageCTM);
         devCTM.concatenate(rotCTM);
      }
      else devCTM.concatenate(pageCTM);
      AffineTransform finalCTM = devCTM;
		//RGPTLogger.logToFile("FINAL TRANSFORM MATRIX " + finalCTM.toString());
      
      double x1 = 0.0, y1 = 0.0;
      Point2D.Double ptSrc = null, ptDst = null;
      Point2D.Double ptDst1 = null, ptDst2 = null;
      Point2D.Double ptDst3 = null, ptDst4 = null;
      
      float x, desX1, desX2, desX3, desX4; 
      float y, desY1, desY2, desY3, desY4; 
      GeneralPath clipShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
      mesg.append("PATH TYPES: " + m_PathTypes.length);
      mesg.append(" PATH POINTS: " + m_PathPoints.length);
		int data_index=0;
		for (int opr_index = 0; opr_index < m_PathTypes.length; opr_index++)
		{
			switch(m_PathTypes[opr_index])
			{
   			case MOVE_TO:
               x1 = m_PathPoints[data_index]; ++data_index;
               y1 = m_PathPoints[data_index]; ++data_index;
               ptSrc = new Point2D.Double(x1, y1);
               ptDst = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst);
               x = (float)ptDst.getX() - (float)startPt.getX();
               y = (float)ptDst.getY() - (float)startPt.getY();
               clipShape.moveTo(x, y);
               //clipShape.moveTo((float)ptDst.getX(), (float)ptDst.getY());
               mesg.append("\nM" + ptDst.getX() + " " + ptDst.getY());
               break;
            case LINE_TO:
   				x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               ptSrc = new Point2D.Double(x1, y1);
               ptDst = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst);
               x = (float)ptDst.getX() - (float)startPt.getX();
               y = (float)ptDst.getY() - (float)startPt.getY();
               clipShape.lineTo(x, y);
               //clipShape.lineTo((float)ptDst.getX(), (float)ptDst.getY());
               mesg.append("\nL " + ptDst.getX() + " " + ptDst.getY());
               break;
   			case CUBIC_TO:
   				x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               ptSrc = new Point2D.Double(x1, y1);
               ptDst1 = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst1);
   				
               x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               ptSrc = new Point2D.Double(x1, y1);
               ptDst2 = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst2);
   				
               x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               ptSrc = new Point2D.Double(x1, y1);
               ptDst3 = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst3);
               
               desX1 = (float)ptDst1.getX() - (float)startPt.getX();
               desY1 = (float)ptDst1.getY() - (float)startPt.getY();
               desX2 = (float)ptDst2.getX() - (float)startPt.getX();
               desY2 = (float)ptDst2.getY() - (float)startPt.getY();
               desX3 = (float)ptDst3.getX() - (float)startPt.getX();
               desY3 = (float)ptDst3.getY() - (float)startPt.getY();
               clipShape.curveTo(desX1, desY1, desX2, desY2, desX3, desY3);
               //clipShape.curveTo((float)ptDst1.getX(), (float)ptDst1.getY(),
               //                  (float)ptDst2.getX(), (float)ptDst2.getY(),
               //                  (float)ptDst3.getX(), (float)ptDst3.getY());
               mesg.append("\nC " + ptDst1.getX() + " " + ptDst1.getY() +
                           " " + ptDst2.getX() + " " + ptDst2.getY() +
                           " " + ptDst3.getX() + " " + ptDst3.getY());
               break;
   			case RECT:
					x1 = m_PathPoints[data_index]; ++data_index;
					y1 = m_PathPoints[data_index]; ++data_index;
               ptSrc = new Point2D.Double(x1, y1);
               ptDst1 = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst1);
					
               // Getting the Width and Ht for the Rect
               double w = m_PathPoints[data_index]; ++data_index;
					double h = m_PathPoints[data_index]; ++data_index;
               
               double x2 = x1 + w;
					double y2 = y1;
               ptSrc = new Point2D.Double(x2, y2);
               ptDst2 = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst2);
					
               double x3 = x2;
					double y3 = y1 + h;
               ptSrc = new Point2D.Double(x3, y3);
               ptDst3 = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst3);
					
               double x4 = x1; 
					double y4 = y3;
               ptSrc = new Point2D.Double(x4, y4);
               ptDst4 = new Point2D.Double();
               finalCTM.transform(ptSrc, ptDst4);

               desX1 = (float)ptDst1.getX() - (float)startPt.getX();
               desY1 = (float)ptDst1.getY() - (float)startPt.getY();
               desX2 = (float)ptDst2.getX() - (float)startPt.getX();
               desY2 = (float)ptDst2.getY() - (float)startPt.getY();
               desX3 = (float)ptDst3.getX() - (float)startPt.getX();
               desY3 = (float)ptDst3.getY() - (float)startPt.getY();
               desX4 = (float)ptDst4.getX() - (float)startPt.getX();
               desY4 = (float)ptDst4.getY() - (float)startPt.getY();
               clipShape.moveTo(desX1, desY1);
               clipShape.lineTo(desX2, desY2);
               clipShape.lineTo(desX3, desY3);
               clipShape.lineTo(desX4, desY4);
               
               //clipShape.moveTo((float)ptDst1.getX(), (float)ptDst1.getY());
               //clipShape.lineTo((float)ptDst2.getX(), (float)ptDst2.getY());
               //clipShape.lineTo((float)ptDst3.getX(), (float)ptDst3.getY());
               //clipShape.lineTo((float)ptDst4.getX(), (float)ptDst4.getY());
               
               mesg.append("\nRECT M" + ptDst1.getX() + " " + ptDst1.getY());
               mesg.append(" L " + ptDst2.getX() + " " + ptDst2.getY());
               mesg.append(" L " + ptDst3.getX() + " " + ptDst3.getY());
               mesg.append(" L " + ptDst4.getX() + " " + ptDst4.getY());
   				break;
   			case CLOSE_PATH:
   				mesg.append("\nClose Path");
               isClosePath = true;
   				clipShape.closePath();
   				break;
               //return clipShape;
   			default: 
               mesg.append("\nInvalid Element Type " + m_PathTypes[opr_index]);
   				break;
   				//throw new RuntimeException("Invalid Element Type");
         } // switch(m_PathPoints[opr_index])	
      } // for (int opr_index = 0; opr_index < m_PathPoints.length; opr_index++)
      //RGPTLogger.logToFile(mesg.toString()); 
      Rectangle2D clipBounds = clipShape.getBounds2D();
      //RGPTLogger.logToFile("The Clip Shape Bounds " + clipBounds.toString());
      if (clipBounds.getWidth() != 0 && clipBounds.getHeight() != 0 && !isClosePath)
         clipShape.closePath();
      //RGPTLogger.logToFile("FINISHED DRAWING SHAPE.");
      return clipShape;
   }

   public String toString()
   {
      return this.toString(new StringBuffer());
   }
   
   public String toString(StringBuffer mesg)
   {
      mesg.append("\n-----PRINTING CLIP SEGMENT.-----\n");
      mesg.append("PATH CTM: " + m_PathCTM.toString());
      mesg.append(" ELEMENT CTM: " + m_ElementCTM.toString());
      mesg.append("\nIs Stroked: " + m_IsStroked);
      mesg.append(" Stroke Color Pt: " + m_StrokeColorPt);
      mesg.append(" Is Filled: " + m_IsFilled);
      mesg.append(" Fill Color Pt: " + m_FillColorPt);
      if (m_PathStroke != null)
         mesg.append(" Basic Stroke: " + m_PathStroke.toString());
      this.printClipPath(mesg);
      return mesg.toString();
   }  
   
   public void printClipPath(StringBuffer mesg)
   {
      double x1 = 0.0, y1 = 0.0;
		mesg.append("\n----PRINTING PATH DATA POINTS----\n");
      mesg.append("PATH POINTS LENGTH: " + m_PathPoints.length + 
                  " PATH TYPES LENGTH: " + m_PathTypes.length + "\n");
		int data_index=0;
		for (int opr_index = 0; opr_index < m_PathTypes.length; opr_index++)
		{
			switch(m_PathTypes[opr_index])
			{
   			case MOVE_TO:
               x1 = m_PathPoints[data_index]; ++data_index;
               y1 = m_PathPoints[data_index]; ++data_index;
               mesg.append("\nM: " + x1 + " " + y1);
               break;
            case LINE_TO:
   				x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               mesg.append("\nL: " + x1 + " " + y1);
               break;
   			case CUBIC_TO:
   				x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               mesg.append("\nC: " + x1 + " " + y1);   				
               
               x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               mesg.append(" " + x1 + " " + y1);   				
   				
               x1 = m_PathPoints[data_index]; ++data_index;
   				y1 = m_PathPoints[data_index]; ++data_index;
               mesg.append(" " + x1 + " " + y1);   				               
               break;
   			case RECT:
					x1 = m_PathPoints[data_index]; ++data_index;
					y1 = m_PathPoints[data_index]; ++data_index;
               mesg.append("\nR: " + x1 + " " + y1);   				
					
               // Getting the Width and Ht for the Rect
               double w = m_PathPoints[data_index]; ++data_index;
					double h = m_PathPoints[data_index]; ++data_index;
               mesg.append(" W " + x1 + " Ht " + y1);   				
   				break;
   			case CLOSE_PATH:
   				mesg.append("\nClose Path\n");
   				break;
   			default: 
               mesg.append("\nInvalid Element Type " + m_PathTypes[opr_index]);
   				break;
   				//throw new RuntimeException("Invalid Element Type");
         } // switch(m_PathPoints[opr_index])	
      } // for (int opr_index = 0; opr_index < m_PathPoints.length; opr_index++)
   } // End of Function printClipPath(StringBuffer mesg)
}
