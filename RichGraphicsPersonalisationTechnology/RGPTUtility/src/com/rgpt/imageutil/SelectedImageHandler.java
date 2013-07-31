// RGPT PACKAGES
package com.rgpt.imageutil;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;

public interface SelectedImageHandler
{
   
   // This function is predominantly used when  a particular image selected from 
   // ThumbviewImageFrame needs to be handled. This is used when a Image is 
   // selected by the Buyer in Applet Mode or an exact image needs to be selected
   // during VDP IMage Selection. In this scenarion always the serverpath is null,
   // isclipped is set to false.
   public void updateImageData(BufferedImage image, ImageHolder imgHldr);

   // This method call is implemented to show the selected Image on the screen 
   // by drawing lines around the Image. This method is invoked when there are 
   // multiple images for a particular selection to be handled.
   public void showImage(int assetId);

   // This method call is implemented to further process the Selected Image.
   // This method is invoked when there are multiple images for a particular 
   // selection to be handled.
   public void selectImage(int assetId);
   
   // This method is called when the Display Box is closed. In this scenario,
   // the this object does the necessary clean-up and refresh operations.
   public void windowClosing();   
   
   // This method is called on Mouse Released to check the VDP Image Field on 
   // which the mouse is dropped.
   public boolean containsVDPImage(MouseEvent me);
   
   // This method is called to check if the User has Selected any Image for the VDP Field.
   public boolean containsUserSelImage();

   // This API is to show Busy Cursor when the Image is getting uploaded
   public void showBusyCursor();   
   
   // This API is to reset the Cursor to default Cursor
   public void setDefaultCursor();   
}