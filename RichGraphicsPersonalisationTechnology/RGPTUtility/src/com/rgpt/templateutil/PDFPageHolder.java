// RGPT PACKAGES
package com.rgpt.templateutil;

import java.io.Serializable;

public class PDFPageHolder implements Serializable
{
   public int m_AssetId;
   public int m_PageNum;
   // PDF Page Info Object Stream
   public byte[] m_PageStr;
   public byte[] m_ImgStr;
   public String m_SrcDir;
   public String m_SerializedFileName;
   
   public PDFPageHolder(int assetId, String srcDir, String fileName, 
                        int pgNum, byte[] pgStr, byte[] imgStr)
   {
     m_AssetId = assetId; 
     m_PageNum = pgNum; 
     m_SrcDir = srcDir;  
     m_ImgStr = imgStr;  
     m_PageStr = pgStr;  
     m_SerializedFileName = fileName;  
   }
   
   public PDFPageHolder(int pgNum, byte[] imgStr)
   {
     m_AssetId = -1; 
     m_PageNum = pgNum; 
     m_SrcDir = "";  
     m_ImgStr = imgStr;  
     m_PageStr = null;  
     m_SerializedFileName = "";  
   }
}
