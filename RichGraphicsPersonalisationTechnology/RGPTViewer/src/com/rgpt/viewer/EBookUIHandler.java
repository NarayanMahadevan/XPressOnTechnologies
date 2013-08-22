// RGPT PACKAGES
package com.rgpt.viewer;

import java.util.Map;
import java.util.Vector;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.rgpt.viewer.EBookInterface;

import java.awt.image.BufferedImage;

public interface EBookUIHandler 
{
   // This is valid for a Single Page Document
   public static int DISPLAY_SINGLE_PAGE = 1;
   // This is valid for a Two Page Document or for a Middle content in a 
   // multi page document
   public static int DISPLAY_DOUBLE_PAGE = 2;
   // This is valid for a Multi Page Document when First Page has to be Displayed
   public static int DISPLAY_RHS_PAGE_ONLY = 3;
   // This is valid for a Multi Page Document when Last Page has to be Displayed
   public static int DISPLAY_LHS_PAGE_ONLY = 4;
   
   // This method is to set EBook UI Control and register the EBookInterface for 
   // Page retrivals
   public void setEBookUIControls(boolean enablePgAnim, int animSpeed, 
                                  int animStep, EBookInterface eBookHdlr);
   
   // This method is to set Panel Size 
   public void setEBookPanelSize(int width, int height);
   
   // This method is invoked to show First page of PDF in EBook 
   public void showFirstPage();
   
   // This method is invoked to show Last page of PDF in EBook 
   public void showLastPage();
   
   // This method is invoked to show next page of PDF in EBook 
   public void showNextPage();
   
   // This method is invoked to show pervious page of PDF in EBook 
   public void showPrevPage();
   
   // This method is invoked to animate next page
   public void animateNextPage();
   
   // This method is invoked to animate previous page
   public void animatePrevPage();
}
