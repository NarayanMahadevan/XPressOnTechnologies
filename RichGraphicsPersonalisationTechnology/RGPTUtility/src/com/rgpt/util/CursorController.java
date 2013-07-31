// RGPT PACKAGES
package com.rgpt.util;

import java.awt.*;
import java.util.*;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.rgpt.imageutil.ImageUtils;

public final class CursorController 
{
   public final static int DEFAULT_CURSOR = 0;
   public final static int BUSY_CURSOR = 1;

   public final static Cursor m_BusyCursor = Cursor.
                                             getPredefinedCursor(Cursor.WAIT_CURSOR);
   public final static Cursor m_DefaultCursor = Cursor.getDefaultCursor();

   // START Variables for Custom Cursors
   
   // This static variable is used to handle the Rotation and to specify the corr Rotate Cursor
   public final static int NW_ROTATE_CURSOR = 100;
   public final static int NE_ROTATE_CURSOR = 101;
   public final static int SE_ROTATE_CURSOR = 102;
   public final static int SW_ROTATE_CURSOR = 103;
   
   public static Map<Integer, String> CustomCursorIcon;
   
   static {
      CustomCursorIcon = new HashMap<Integer, String>(); 
      CustomCursorIcon.put(NW_ROTATE_CURSOR, "rgpticons/nw_rotate_cursor.gif");
      CustomCursorIcon.put(NE_ROTATE_CURSOR, "rgpticons/ne_rotate_cursor.gif");
      CustomCursorIcon.put(SE_ROTATE_CURSOR, "rgpticons/se_rotate_cursor.gif");
      CustomCursorIcon.put(SW_ROTATE_CURSOR, "rgpticons/sw_rotate_cursor.gif");
   }
   
   // END Variables for Custom Cursors
   
   private CursorController() {}

   public static ActionListener createListener(final Component comp, 
                                               final ActionListener mainListener, 
                                               final int cursorType) 
   {
      ActionListener actionListener = new ActionListener() 
      {
         public void actionPerformed(ActionEvent ae) {
            try {
               if (cursorType == BUSY_CURSOR) comp.setCursor(m_BusyCursor);
               mainListener.actionPerformed(ae);
            } 
            finally 
            {
               comp.setCursor(m_DefaultCursor);
            }
         }
      };
      return actionListener;
   }

   public static Cursor getCursor(int cursorType)
   {
      if (cursorType < 100) return Cursor.getPredefinedCursor(cursorType);
      else {
         String cursorImg = CustomCursorIcon.get(cursorType);
         if (cursorImg == null) return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR); 
         Point cursorPt = null;
         switch(cursorType)
         {
            case NW_ROTATE_CURSOR : cursorPt = new Point(20,20); break;
            case NE_ROTATE_CURSOR : cursorPt = new Point(0,20); break;
            case SW_ROTATE_CURSOR : cursorPt = new Point(20,0); break;              
            default: cursorPt = new Point(0,0);
         }
         return createCustomCursor(cursorImg, "Cursor_"+cursorType, false, cursorPt);
      }
   }
   
   public static Cursor createCustomCursor(String cursorImg, String cursorName, 
                                           boolean resize, Point cursorPt)
   {
      //Load an image for the cursor
      //Load an image for the cursor
      Image image = (RGPTUIUtil.getImageIcon(cursorImg)).getImage();  
      if (image == null) return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR); 
      //Create the custom cursor
      return createCustomCursor(image, cursorName, resize, cursorPt);
   }
   
   public static Cursor createRoundCursor(int size)
   {
      int wt = 32, ht = 32;
      Object localObject = null;
      int k = Color.red.getRGB();
      BufferedImage cursorImg = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
      Graphics g = cursorImg.getGraphics();
      g.setColor(Color.WHITE);
      g.drawOval(0, 0, size, size);
      g.dispose();
      //Create the custom cursor
      return createCustomCursor(cursorImg, "RoundCursor", true);
   }
   
   public static Cursor createCustomCursor(Image image, String cursorName, 
                                           boolean resize)
   {
      //Create the hotspot for the cursor
      Point cursorPt = new Point(0,0);
      return createCustomCursor(image, cursorName, resize, cursorPt);
   }
   
   public static Cursor createCustomCursor(Image image, String cursorName, 
                                           boolean resize, Point cursorPt)
   {
      //Get the default toolkit
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      if (resize) {         
         Dimension sz = toolkit.getBestCursorSize(image.getWidth(null), image.getHeight(null));
         image = ImageUtils.scaleImage(ImageUtils.createImageCopy(image), sz.width, 
                                       sz.height, true, false);
      }
      //Create the custom cursor
      return toolkit.createCustomCursor(image, cursorPt, cursorName);
   }
   
   
}

