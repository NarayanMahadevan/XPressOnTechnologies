// RGPT PACKAGES
package com.rgpt.util;

import java.io.File;

public class FileFilterFactory 
{
   public static final RGPTFileFilter PDF_FILE_FILTER;
   public static final RGPTFileFilter FONT_FILE_FILTER;
   public static final RGPTFileFilter IMAGE_FILE_FILTER;
   public static final RGPTFileFilter XON_DESIGN_FILE_FILTER;
   public static final RGPTFileFilter ALL_FILE_FILTER;
   public static final RGPTFileFilter CSV_FILE_FILTER;
   public static final RGPTFileFilter ZIP_FILE_FILTER;
   public static final RGPTFileFilter SER_FILE_FILTER;
   public static final RGPTFileFilter XON_SHAPE_FILE_FILTER;

   static
   {
      // PDF Filter 
      String fileType = "PDF";
      String[] pdfFilters = {"pdf"};
      String filterDesc = "PDF Files(*.pdf)";
      PDF_FILE_FILTER = new RGPTFileFilter(fileType, pdfFilters, filterDesc);
      
      // Font Filter 
      fileType = "FONT";
      //String[] fontFilters = {"ttf", "otf"};
      String[] fontFilters = {"ttf"};
      //filterDesc = "Font Files(*.ttf, *.otf)";
      filterDesc = "True Type Fonts(*.ttf)";
      FONT_FILE_FILTER = new RGPTFileFilter(fileType, fontFilters, filterDesc);
      
      // Image Filter
      fileType = "IMAGE";
      String[] imageFilters = {"gif", "jpg", "png", "tiff"};
      filterDesc = "Image Files(*.gif, *.jpeg, *.png, *.tiff)";
      IMAGE_FILE_FILTER = new RGPTFileFilter(fileType, imageFilters, filterDesc);

      // Image Filter
      fileType = "XON_PDF_IMAGE";
      String[] xonDesignFilters = {"xod", "pdf", "gif", "jpg", "png", "tiff"};
      filterDesc = "XON Design Files(*.xod, *.pdf, *.gif, *.jpeg, *.png, *.tiff)";
      XON_DESIGN_FILE_FILTER = new RGPTFileFilter(fileType, xonDesignFilters, filterDesc);

      // All File Filter 
      fileType = "ANY";
      String[] allFilters = {"pdf", "zip", "ppt", "doc", "pps", "ai", "cdr", "psd", "indd", "pub", "xls"};
      filterDesc = "All File Types(*.*)";
      ALL_FILE_FILTER = new RGPTFileFilter(fileType, allFilters, filterDesc);

      // All File Filter
      fileType = "CSV";
      String[] csvFilters = {"csv"};
      filterDesc = "CSV Files(*.csv)";
      CSV_FILE_FILTER = new RGPTFileFilter(fileType, csvFilters, filterDesc);
      
      // All File Filter
      fileType = "ZIP";
      String[] zipFilters = {"zip"};
      filterDesc = "ZIP Files(*.zip)";
      ZIP_FILE_FILTER = new RGPTFileFilter(fileType, zipFilters, filterDesc);
      
      // SER File Filter
      fileType = "SER";
      String[] serFilters = {"ser"};
      filterDesc = "Serialization File(*.ser)";
      SER_FILE_FILTER = new RGPTFileFilter(fileType, serFilters, filterDesc);
      
      // XONShape File Filter
      fileType = "XONShape";
      String[] xshFilters = {"xsh"};
      filterDesc = "XPressOn Shape File(*.xsh)";
      XON_SHAPE_FILE_FILTER = new RGPTFileFilter(fileType, xshFilters, filterDesc);
   }
   // All File Filter ALL_FILE_FILTER CSV_FILE_FILTER 
   public static RGPTFileFilter getFileFilter4FileType(String fileType)
   {
      if (PDF_FILE_FILTER.acceptFileType(fileType)) return PDF_FILE_FILTER;
      if (FONT_FILE_FILTER.acceptFileType(fileType)) return FONT_FILE_FILTER;
      if (IMAGE_FILE_FILTER.acceptFileType(fileType)) return IMAGE_FILE_FILTER;
      if (XON_DESIGN_FILE_FILTER.acceptFileType(fileType)) return XON_DESIGN_FILE_FILTER;
      if (ALL_FILE_FILTER.acceptFileType(fileType)) return ALL_FILE_FILTER;
      if (CSV_FILE_FILTER.acceptFileType(fileType)) return CSV_FILE_FILTER;
      if (ZIP_FILE_FILTER.acceptFileType(fileType)) return ZIP_FILE_FILTER;
      if (SER_FILE_FILTER.acceptFileType(fileType)) return SER_FILE_FILTER;
      if (XON_SHAPE_FILE_FILTER.acceptFileType(fileType)) return XON_SHAPE_FILE_FILTER;
      return null;
   }
   
   public static RGPTFileFilter getFileFilter(File file)
   {
      return getFileFilter(RGPTFileFilter.getExtension(file));
   }
   
   public static RGPTFileFilter getFileFilter(String ext)
   {
      if (PDF_FILE_FILTER.accept(ext)) return PDF_FILE_FILTER;
      if (FONT_FILE_FILTER.accept(ext)) return FONT_FILE_FILTER;
      if (IMAGE_FILE_FILTER.accept(ext)) return IMAGE_FILE_FILTER;
      if (CSV_FILE_FILTER.accept(ext)) return CSV_FILE_FILTER;
      if (ZIP_FILE_FILTER.accept(ext)) return ZIP_FILE_FILTER;
      if (XON_DESIGN_FILE_FILTER.accept(ext)) return XON_DESIGN_FILE_FILTER;
      if (ALL_FILE_FILTER.accept(ext)) return ALL_FILE_FILTER;
      return null;
   }
}