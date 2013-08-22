// RGPT PACKAGES
package com.rgpt.viewer;

//import javax.swing.JInternalFrame;
import java.awt.*;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

import com.rgpt.serverhandler.ApprovalHandler;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ApprovalDialog extends JDialog
{
   // To send Approval Request to Server
   ApprovalHandler m_ApprovalHandler;
   JRadioButton m_ApproveButton;
   JRadioButton m_DisApproveButton;
   JRadioButton m_NoDicisionButton;

   public ApprovalDialog(String[] approvalMesgs, ApprovalHandler approvalHandler)
   {
      setTitle("Approval Box");
      
      //Color col = RGPTLookAndFeel.getBackgroundColor();
      //if (col !=null) setBackground(col);
      //...Then set the window size or call pack...
      super.setSize(200,150);
      
      super.setLayout(new BorderLayout());

      m_ApprovalHandler = approvalHandler;
      
      this.add(this.addApprovalBox(approvalMesgs), BorderLayout.CENTER);
      this.add(this.addSubmitBox(), BorderLayout.SOUTH);
   }

   public JPanel addApprovalBox(String[] approvalMesgs)
   {
      int rows = 2, cols = 1, hgap = 0, vgap = 0;
      JPanel approvalPanel = new JPanel(new GridLayout(rows, cols, hgap, vgap));
      
      final ButtonGroup group = new ButtonGroup();
      m_ApproveButton = new JRadioButton(approvalMesgs[0], true);
      group.add(m_ApproveButton);
      approvalPanel.add(m_ApproveButton);
      m_DisApproveButton = new JRadioButton(approvalMesgs[1], false);
      group.add(m_DisApproveButton);
      approvalPanel.add(m_DisApproveButton);
      m_NoDicisionButton = new JRadioButton(approvalMesgs[2], false);
      //group.add(m_NoDicisionButton);
      //approvalPanel.add(m_NoDicisionButton);
      return approvalPanel;
   }
   
   public void processApproval()
   {
      if(m_NoDicisionButton.isSelected()) 
         m_ApprovalHandler.processApproval(ApprovalHandler.DICIDE_LATER);
      if (m_DisApproveButton.isSelected()) 
         m_ApprovalHandler.processApproval(ApprovalHandler.DISAPPROVE_PDF);
      else if (m_ApproveButton.isSelected()) 
         m_ApprovalHandler.processApproval(ApprovalHandler.APPROVE_PDF);
   }
   
   public JPanel addSubmitBox()
   {
      int allign = FlowLayout.CENTER, hgap = 5, vgap = 5;
      JPanel submitPanel = new JPanel(new FlowLayout(allign, hgap, vgap));
      final ButtonGroup group = new ButtonGroup();
      JButton okButton = new JButton("OK");
      group.add(okButton);
      submitPanel.add(okButton);
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            processApproval();
            setVisible(false);
         }
      });
      JButton cancelButton = new JButton("Cancel");
      group.add(cancelButton);
      submitPanel.add(cancelButton);
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
         }
      });
      return submitPanel;
   }
}
