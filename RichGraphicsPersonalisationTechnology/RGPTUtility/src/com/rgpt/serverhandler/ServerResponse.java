// RGPT PACKAGES
package com.rgpt.serverhandler;
import java.util.HashMap;

// This files are added to support serialization
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.RuntimeException;

public class ServerResponse implements Serializable
{
   //public static final long serialVersionUID = 6873131385651359146L;
   public static final long serialVersionUID = 3770930649657372124L;
   
   public boolean m_IsSuccess = false;
   public HashMap m_ResultValues;
   
   public ServerResponse()
   {
      m_ResultValues = new HashMap();
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer("SERVER RESPONSE: ");
      if (m_IsSuccess) mesg.append("SUCCESSFULLY EXECUTED THE REQUEST");
      else 
      {
         mesg.append("FAILURE IN EXECUTING THE REQUEST");
         return mesg.toString();
      }
      mesg.append("\n" + m_ResultValues.toString());
      return mesg.toString();
   }

   //Serialization
   public void save(ObjectOutputStream objstream) throws IOException 
   {
      objstream.writeObject(this);
   }
   
   //De-Serialization
   public static ServerResponse load(ObjectInputStream objstream ) throws Exception 
   {
      ServerResponse respObj = (ServerResponse) objstream.readObject();
      return respObj;
   }
   
}
