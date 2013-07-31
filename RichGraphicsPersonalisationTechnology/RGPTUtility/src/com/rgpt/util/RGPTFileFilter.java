// RGPT PACKAGES
package com.rgpt.util;

import java.io.File;

import java.util.Hashtable;
import javax.swing.filechooser.FileFilter;

public class RGPTFileFilter extends FileFilter implements java.io.FileFilter, java.io.FilenameFilter
{
   // This Hashtable contains the Extension Files Supported by this Object and
   // this Object. Hashtable is used here for convinience for efficent search on 
   // the key
   public Hashtable m_Filters;
   public String m_FileType;
   public String m_FilterDesc;

   public RGPTFileFilter(String fileType, String[] filters, String desc)
   {
      m_FileType = fileType;
      m_Filters = new Hashtable(5);
      for (int i = 0; i < filters.length; i++)
         m_Filters.put(filters[i].toLowerCase(), this);
      m_FilterDesc = desc;
   }
   
   /**
         * Accept all directories and all files extensions contained in m_Filters 
         * Vector. If the File Type is ALL, this means this filter accepts all File 
         * Type. Hence this does not check into the m_Filters Object.
         */
   public boolean accept(File f) 
   {
      if(f.isDirectory() || m_FileType.equals("ANY")) return true;
      String extension = getExtension(f.getName());
      if(extension != null && m_Filters.get(extension) != null)
         return true;
      return false;
   }
   
   public boolean accept(File dir, String name)
   {
      String ext = getExtension(name);
      // RGPTLogger.logToFile("Dir: "+dir+" name: "+name+" ext: "+ext+" Filters: "+m_Filters);
      return accept(ext);
   }

   /**
         * Accept all directories and all files extensions contained in m_Filters 
         * Vector. If the File Type is ALL, this means this filter accepts all File 
         * Type. Hence this does not check into the m_Filters Object.
         */
   public boolean accept(String extension) 
   {
      if(m_FileType.equals("ANY")) return true;
      if(extension != null && m_Filters.get(extension) != null)
         return true;
      return false;
   }
    
   public boolean acceptFileType(String fileType) 
   {
      if(m_FileType.equals(fileType)) return true;
      return false;
   }
   
   //The description of this filter
   public String getDescription() 
   {
     return m_FilterDesc;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer("FileFilter of Type: " + m_FileType);
      mesg.append(" Desc: " + m_FilterDesc);
      Object[] extObj = m_Filters.keySet().toArray();
      mesg.append(" And Filters supprted are: " );
      for (int i = 0; i < extObj.length; i++)
         mesg.append((String) extObj[i] + ":");
      return mesg.toString();
   }
   
   public static String getExtension(File file) 
   {
      return getExtension(file.getName());
   }
   
   public static String getExtension(String file) 
   {
      String ext = null;
      int i = file.lastIndexOf('.');
      if (i > 0 &&  i < file.length() - 1) {
         ext = file.substring(i+1).toLowerCase();
      }
      return ext;
   }
   
   // This function takes the file Name with extension and returns file name 
   // without extension
   public static String getFileName(String file) 
   {
      String fileName = null;
      int i = file.lastIndexOf('.');
      if (i > 0 &&  i < file.length() - 1) {
         fileName = file.substring(0, i);
      }
      return fileName;
   }
}
