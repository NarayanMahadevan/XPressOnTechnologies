// RGPT PACKAGES
package com.rgpt.serverhandler;

// This files are added to support serialization
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.RuntimeException;

public class FileUploadServerInterface implements Serializable
{
   public int m_PSPIdentifier;
   public int m_ServerRequestIndicater;
   public ServerRequest m_ServerRequest;
   
   public static final int UPLOAD_ASSET_REQUEST = 1;
   
   public FileUploadServerInterface(int pspId, int serverRequestIndicator) 
   {
      m_PSPIdentifier = pspId;
      m_ServerRequestIndicater = serverRequestIndicator;
      switch (m_ServerRequestIndicater) 
      {
         case UPLOAD_ASSET_REQUEST: 
            {
               m_ServerRequest = new UploadAssetRequest();
            }
            break;
         default:
            throw new RuntimeException("WRONG SERVER REQUEST");
      }
   }
   
   //Serialization
   public void save(ObjectOutputStream objstream) throws IOException 
   {
      objstream.writeObject(this);
   }
   
   //De-Serialization
   public static FileUploadServerInterface load(ObjectInputStream objstream ) throws Exception 
   {
      FileUploadServerInterface reqObj = (FileUploadServerInterface) objstream.readObject();
      return reqObj;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("PSP ID: " + m_PSPIdentifier + " ");
      mesg.append(m_ServerRequest.toString());
      return mesg.toString();
   }
}
