// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;

public interface ImageHandlerInterface
{
   // This identifies the different modes supported for uploading the Image. They
   // incude Image downloaded from Server, Client Machiene, or from Flickr
   public static int SERVER_IMAGES = 0;
   // This is to show Desktop Images
   public static int DESKTOP_IMAGES = 1;
   public static int FLICKR_IMAGES = 2;
   // This constant is used for Uploading Desktop Images by Opening the File System
   public static int UPLOAD_DESKTOP_IMAGES = 3;
   
   // There could be many Themes that could be loaded from the server. This will 
   // all have number starting from the Hundred series
   public static int THEME_IMAGES = 100;
   
   // This DPI configuration is used for Down Sampling the Actual Image.
   public static int IMAGE_DIAPLAY_DPI = 150;
   public static int IMAGE_PRINT_DPI = 300;
   
   // This function is called by the ImageUploadImterface to handle the new 
   // images downloaded from one of the modes defined above.
   public void handleImagesUploaded(Vector imageHolders, 
                                    int imageDownloadMode) throws Exception;

   // This API is to show Busy Cursor when the Image is getting uploaded
   public void showBusyCursor();
}