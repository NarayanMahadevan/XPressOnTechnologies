// RGPT PACKAGES
package com.rgpt.serverhandler;

public interface ApprovalHandler
{
   // This indicates the different Approval Mode
   public int APPROVE_PDF = 1;
   public int DISAPPROVE_PDF = 2;
   public int DICIDE_LATER = 3;
   
   // This method is invoked to Process PDF Approval
   public void processApproval(int approvalMode);
}