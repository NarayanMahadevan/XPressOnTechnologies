// RGPT PACKAGES
package com.rgpt.util;

public class ScalingFactor
{
   public static final int FIT_PAGE_SCALE_FACTOR = 1;
   public static final int FIT_WIDTH_SCALE_FACTOR = 2;
   public static final int FIT_HEIGHT_SCALE_FACTOR = 3;
   public static final int ZOOM_IN_OUT_SCALE_FACTOR = 4;
   private int m_ScaleFactor;
   private String m_ScaleFactorName;
   
   // This is set if the ScalingFactor is set to ZOOM_IN_OUT
   public int m_ZoomValue;
   
   // This defines list of Zoom Values
   private int m_ZoonIndex;
   public static final int[] m_ZoomValueList = {25, 50, 100, 125, 150, 200, 250,
                                                300, 400, 500, 600, 800, 1000};
   
   // This fits the PDF Page Image to the Height of the Viewable Panel
   public static final ScalingFactor FIT_HEIGHT;
   
   // This fits the PDF Page Image to the Width of the Viewable Panel
   public static final ScalingFactor FIT_WIDTH;

   // This fits the whole of the PDF Page Image to the  Viewable Panel
   public static final ScalingFactor FIT_PAGE;
   
   // This allows to Zoom in or Zoom out of the PDF Page
   public static final ScalingFactor ZOOM_IN_OUT;
      
   static
   {
      FIT_PAGE = new ScalingFactor(FIT_PAGE_SCALE_FACTOR, "FIT PAGE");
      FIT_WIDTH = new ScalingFactor(FIT_WIDTH_SCALE_FACTOR, "FIT WIDTH");
      FIT_HEIGHT = new ScalingFactor(FIT_HEIGHT_SCALE_FACTOR, "FIT HEIGHT");
      ZOOM_IN_OUT = new ScalingFactor(ZOOM_IN_OUT_SCALE_FACTOR, "ZOOM IN OUT");
   }
   
   public ScalingFactor(int scaleFactor, String scaleFactorName)
   {
      m_ScaleFactor = scaleFactor;
      m_ScaleFactorName = scaleFactorName;
      m_ZoonIndex = 0;
   }
   
   public static String[] getScalingFactor()
   {
      String[] scalingFactor = new String[3];
      scalingFactor[0] = FIT_PAGE.m_ScaleFactorName; 
      scalingFactor[1] = FIT_WIDTH.m_ScaleFactorName; 
      scalingFactor[2] = FIT_HEIGHT.m_ScaleFactorName; 
      return scalingFactor;
   }
   
   public static ScalingFactor getScalingFactor(String scaleFactor)
   {
      if (FIT_PAGE.m_ScaleFactorName.equals(scaleFactor))
         return FIT_PAGE;
      else if (FIT_WIDTH.m_ScaleFactorName.equals(scaleFactor))
         return FIT_WIDTH;
      else if (FIT_HEIGHT.m_ScaleFactorName.equals(scaleFactor))
         return FIT_HEIGHT;
      // Default if non matches
      else return FIT_PAGE;
   }
   
   public static ScalingFactor getScalingFactor(java.awt.Dimension dispSize, double imgWt, 
                                                double imgHt)
   {
      ScalingFactor scaleFactor = null;
      double dispWt = 0, dispHt = 0;
      double aspectRatio =  ((double)imgHt / (double) imgWt);
      double dispAspectRatio = ((double)dispSize.height / (double) dispSize.width);
      // RGPTLogger.logToFile("Img aspectRatio: "+aspectRatio+" Disp aspectRatio: "+
                           // dispAspectRatio);
      if (dispAspectRatio == aspectRatio || 
          Math.abs(dispAspectRatio-aspectRatio)<0.01) return FIT_PAGE;
      if (imgWt < imgHt) {   
         dispWt = (double)dispSize.width;
         dispHt = dispWt * aspectRatio;
         scaleFactor = FIT_WIDTH;
         if (dispHt < (double)dispSize.height) {
            dispHt = (double) dispSize.height;
            dispWt = dispHt / aspectRatio;
            scaleFactor = FIT_HEIGHT;
         }
      }
      else {
         dispHt = (double) dispSize.height;
         dispWt = dispHt / aspectRatio;
         scaleFactor = FIT_HEIGHT;
         if (dispWt < (double) dispSize.width) {
            dispWt = (double) dispSize.width;
            dispHt = dispWt * aspectRatio;;
            scaleFactor = FIT_WIDTH;
         }
      }
      return scaleFactor;
   }
   
   
   
   public void setZoom(int zoomValue)
   {
      m_ZoomValue = zoomValue;
   }
   
   public String[] getZoomValueList()
   {
      String [] zoomList = new String[m_ZoomValueList.length];
      for(int i = 0; i < m_ZoomValueList.length; i++)
         zoomList[i] = new Integer(m_ZoomValueList[i]).toString();
      return zoomList;
   }
   
   public int zoomOut(int zoomValue)
   {
      int prevIndex = 0;
      int result = -1;
      if (zoomValue <= m_ZoomValueList[0])
         result = m_ZoomValueList[0];
      else if ((zoomValue > m_ZoomValueList[m_ZoomValueList.length-1]))
         result = m_ZoomValueList[m_ZoomValueList.length-1];
      else
      {
         for(int i = 0; i < m_ZoomValueList.length; i++)
         {
            if (result != -1) continue;
            if(m_ZoomValueList[i] >= zoomValue)
               result = m_ZoomValueList[prevIndex];
            else prevIndex = i;
         }
      }
      m_ZoomValue = result;
      return m_ZoomValue;
   }
   
   public int zoomIn(int zoomValue)
   {
      int result = -1;
      if (zoomValue < m_ZoomValueList[0])
         result = m_ZoomValueList[0];
      else if ((zoomValue >= m_ZoomValueList[m_ZoomValueList.length-1]))
         result = m_ZoomValueList[m_ZoomValueList.length-1];
      else
      {
         for(int i = 0; i < m_ZoomValueList.length; i++)
         {
            if (result != -1) continue;
            if(m_ZoomValueList[i] > zoomValue)
               result = m_ZoomValueList[i];
         }
      }
      m_ZoomValue = result;
      return m_ZoomValue;
   }
   
   public boolean isFitHeight() { return this.equals(FIT_HEIGHT); }
   public boolean isFitWidth() { return this.equals(FIT_WIDTH); }
   public boolean isFitPage() { return this.equals(FIT_PAGE); }
   public boolean isZoomInOut() { return this.equals(ZOOM_IN_OUT); }
   
   public boolean equals(ScalingFactor obj)
   {
      if(this.m_ScaleFactor == obj.m_ScaleFactor) return true;
      return false;
   }
   
   public String toString()
   {
      if(this.m_ScaleFactor == 1) return "SCALING FACTOR: FIT_PAGE";
      else if(this.m_ScaleFactor == 2) return "SCALING FACTOR: FIT_WIDTH";
      else if(this.m_ScaleFactor == 3) return "SCALING FACTOR: FIT_HEIGHT";
      else if(this.m_ScaleFactor == 4) return "SCALING FACTOR: ZOOM_IN_OUT";
      return null;
   }
}
