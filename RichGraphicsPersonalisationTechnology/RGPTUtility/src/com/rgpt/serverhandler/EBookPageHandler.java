// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;
import java.util.HashMap;

public interface EBookPageHandler
{
   // This function is called to retrieve Pages corresponding to the Page Numbers
   public HashMap getPages(Vector pageNums) throws Exception;
   
   // This is an optional method used to approve or disapprove Pages in the PDF
   // This method is invoked if only if the PDFPageApproval is set on EBookViewer
   public void setPDFBatchApproval(HashMap pdfPgApproveStatus);
   
   // This methos is used as described above during  PDF Page Approvals
   public HashMap getPDFDocApprovals(int totalPgCnt, int pdfPgCnt, 
                                  HashMap pgApprStatus);

}