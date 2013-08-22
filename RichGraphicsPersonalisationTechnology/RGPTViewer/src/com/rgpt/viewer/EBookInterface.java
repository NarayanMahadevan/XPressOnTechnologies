package com.rgpt.viewer;

import javax.swing.*;

import com.rgpt.serverhandler.EBookPageHandler;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface EBookInterface 
{
   // This method is to set EBook UI Control and register the EBookInterface for 
   // Page retrivals
   public void setEBookUIControls(Container parentPane, EBookPageHandler eBookHdlr, 
                                  int assetId, int numPages, boolean enablePDFAppr, 
                                  boolean enablePgAnim, int animSpeed, int animStep);
   public void resetEBookCache();
   public boolean isEBookSet();
   public void setVisibility(boolean isVisible);
   public void displayEBook();
   public void populateContentPane(JPanel contPane);
}
