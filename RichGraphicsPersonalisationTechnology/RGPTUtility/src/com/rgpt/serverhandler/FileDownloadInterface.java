// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.ByteArrayOutputStream;

public interface FileDownloadInterface
{
   // This function is called to download Zip File. This can be for in-memory usage,
   // or can download from Server using HTPP, FTP or any other protocol.
   public void downloadZipFile(String fileURLPath, String fileName, 
                               String outDirPath) throws Exception;
   
   // This function is called to download file. This can be for in-memory usage,
   // or can download from Server using HTPP, FTP or any other protocol.
   public ByteArrayOutputStream 
          downloadFile(String fileURLPath, String fileName) throws Exception;
}