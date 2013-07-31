// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;

public interface ImageHandler {
	// This function is called by the ImageUploadImterface to handle the new
	// images uploaded from the user machine.
	public void handleImagesUploaded(Vector imageHolders) throws Exception;
}