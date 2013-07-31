// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;
import java.util.HashMap;

public interface ImageUploadInterface
{
   // This function is called by the Image Handler to upload new images from 
   // the user machine.
   public void addImageFile(ImageHandlerInterface imgHandler, 
                            boolean useFileUploadUI) throws Exception;
}