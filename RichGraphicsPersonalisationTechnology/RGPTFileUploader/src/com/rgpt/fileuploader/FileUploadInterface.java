// RGPT PACKAGES
package com.rgpt.fileuploader;

import java.util.HashMap;
import java.util.Vector;

import com.rgpt.util.AppletParameters;

public interface FileUploadInterface {
	// This function is called to upload file. This can be for in-memory usage,
	// or can up to upload to Server using HTPP, FTP or any other protocol.
	public HashMap uploadFile(AppletParameters params, Vector files)
			throws Exception;

	// This function is called to register the File Upload Viewer. The File
	// Upload
	// Viewer is used to notify when the Files have finished uploading.
	public void registerFileUploadViewer(FileUploadViewer fuViewer);
}