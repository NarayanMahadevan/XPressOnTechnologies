// RGPT PACKAGES
package com.rgpt.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

class MnemonicKeyListener extends AbstractAction 
{
   private static final long serialVersionUID = 1L;
   String m_RGPTAction;
   String m_MnemonicActionName;
   RGPTActionListener m_RGPTActionListener;
   public MnemonicKeyListener(String action, String name, RGPTActionListener actionListener)
   {
      super(name);
      m_RGPTAction = action;
      m_MnemonicActionName = name;
      m_RGPTActionListener = actionListener;
   }
   
   public void actionPerformed(ActionEvent evt) 
   {
      String mnemonicKey = m_RGPTActionListener.getMnemonicKey(m_RGPTAction);      
      RGPTLogger.logToConsole("Action Id: " + m_RGPTAction +
                              " mnemonicKey: " + mnemonicKey);
      m_RGPTActionListener.performRGPTAction(m_RGPTAction);
   }
}

