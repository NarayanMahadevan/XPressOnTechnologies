// RGPT PACKAGES
package com.rgpt.util;

import java.util.Vector;
import java.util.Map;
import java.awt.Container;
import java.awt.image.BufferedImage;

public interface ThumbViewImageHandler
{
   // This function is used to activate and de-activate the different Image Source 
   // based on Theme Selection
   public void manageImageSrcActivation(int themeId, boolean allowImgUpld);
   
   // This sets the ImageBBox of the VDP Image so as to maintain appropriate Aspect
   // Ratio and for Down Sampling
   public void setImageBBox(RGPTRectangle imgBBox);
   
   // This methos is used to handle Theme Images
   public void handleThemeImages(Map <Integer, Vector> themeImages) 
               throws Exception;
   
   // This is to show Direction Icon to move Images Horizontally or Vertically
   public BufferedImage getDirectionIcon();
   
   // This is for Clean-up Activity. 
   public void cleanUpMemory();
   
   // This is to make the UI Visible
   public void setVisibility(boolean isVisible);
   
   // This is to add Image Files
   public void addImageFile();
   
   // This is get the UI Visible
   public boolean getVisibility();
   
   // This is to retrieve the ThumbViewImage Frame or the Panel
   public Container getThumbViewUI();
}