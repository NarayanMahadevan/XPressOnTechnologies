// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;
import java.util.HashMap;
import java.awt.image.BufferedImage;

public interface PDFViewInterface
{
   // This variables indicate if the PDF is saved in Memory or File System  
   public static int SAVE_PDF_IN_FILE = 0;
   public static int SAVE_PDF_IN_MEMORY = 1;
   
   // PDF Page Thumbview Image Width and Height settings
   public static int THUMBVIEW_HEIGHT = 100;
   public static int THUMBVIEW_WIDTH = 100;
   
   // This indicates the Quality Mode for Drawing PDF Page to Image
   public int PAGE_QUALITY_PDF = 0;
   public int MEDIUM_QUALITY_PDF = 1;
   public int REGULAR_QUALITY_PDF = 2;
   public int MAX_QUALITY_PDF = 3;
   //public int HIGH_QUALITY_PDF = 2;
   //public int BEST_QUALITY_PDF = 3;
   
   // This parameter is used to specify the output image size and quality. 
   // A t ypical screen resolution for monitors these days is 92 DPI
   // Here initially setting to 72 DPI which is actually PPI and since the 
   // Page Points is in User Space we will set it up and it is 1/72 of Inch
   // so we will try to set the DPI at 72
   public int PAGE_QUALITY_DPI = 72;
   public int MEDIUM_QUALITY_DPI = 100;
   public int REGULAR_QUALITY_DPI = 150;
   public int MAX_QUALITY_DPI = 300;
   
   public String SAVED_PDF_IMAGE_FORMAT="PNG";
   //public int HIGH_QUALITY_DPI = 92;
   //public int BEST_QUALITY_DPI = 200;
   
   // This function is called to retrieve Pages corresponding to the Page Numbers
   public HashMap getPDFPages(Vector pageNums) throws Exception;
   
   // This function is called to retrieve Thumbview Images of Pages corresponding 
   // to the Page Numbers
   public HashMap getPDFPageThumbviews(Vector pageNums) throws Exception;
   
   // This function returns the Page as Image based on the Quality Mode
   public BufferedImage getPDFPage(int qualityMode, int pageNum) throws Exception;
   
   // This function saves the PDF Page as Image based on the Quality Mode to the File
   public void savePDFPage(int qualityMode, int pageNum, 
                           String fileName) throws Exception;
   
   // This function returns the Page as Image based on the supplied Image Width and Height
   public BufferedImage getPDFPage(int pageNum, int width, 
                                   int height) throws Exception;
   
   // This function saves the PDF Page as Image based on the Size to the File
   public void savePDFPage(int pageNum, int width, int height, 
                           String fileName) throws Exception;
   
   // This function saves the Image uploaded 
   // public void savePDFPage(int pageNum, int width, int height, 
   //                        String fileName) throws Exception;

   // This function is call to either Save the User Data to Server or to create 
   // new PDF Document with user data in memory
   public boolean createPersonalizedDocument(HashMap userPageData, int pdfSaveMode, 
                                             String filePath) throws Exception;  
}