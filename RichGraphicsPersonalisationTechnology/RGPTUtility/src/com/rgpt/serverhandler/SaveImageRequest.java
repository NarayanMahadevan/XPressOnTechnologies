// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.RuntimeException;

// This is to request PDF Page Image from the Server
public class SaveImageRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "SaveImageRequest";
   
   // Template Data 
   public int m_PageNum;
   public int m_TemplateId;
   
   // Customer Data
   public int m_CustomerId;
   
   // Save Image Data
   public String m_FieldName;
   public String m_FileFormat;
   public String m_ImageFileName;
   public transient BufferedImage m_VDPImage;
   
   public SaveImageRequest() {}
   
   //Serialization
   public void save(ObjectOutputStream objstream) throws IOException 
   {
      objstream.writeObject(this);
      if (m_VDPImage == null)
         throw new RuntimeException("The Image Object is Null");
      
      m_FileFormat = m_ImageFileName.substring(m_ImageFileName.indexOf(".")+1, 
                                               m_ImageFileName.length());
      ImageIO.write(m_VDPImage, m_FileFormat, objstream);
   }
   
   //De-Serialization
   public static Object load(ObjectInputStream objstream) throws Exception 
   {
      SaveImageRequest saveImgReq = (SaveImageRequest) objstream.readObject();
      saveImgReq.m_VDPImage = ImageIO.read(objstream); 
      return saveImgReq;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" TEMPLATE ID: " + m_TemplateId);
      mesg.append(" PAGE NUM: " + m_PageNum);
      mesg.append("\nVDP Field Name: " + m_FieldName);
      mesg.append(" File Name: " + m_ImageFileName);
      mesg.append(" Image Width: " + m_VDPImage.getWidth());
      mesg.append(" Image Height: " + m_VDPImage.getHeight());
      mesg.append(" Image Data: " + m_VDPImage.toString());
      mesg.append(" Customer Id: " + m_CustomerId);
      return mesg.toString();
   }
}
