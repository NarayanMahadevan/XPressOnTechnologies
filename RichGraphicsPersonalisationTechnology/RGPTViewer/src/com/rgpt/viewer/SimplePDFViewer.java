// RGPT PACKAGES
package com.rgpt.viewer;

import javax.swing.*;

import java.util.Map;
import java.util.HashMap;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class SimplePDFViewer extends PDFViewer 
{
   //public static String CENTER_PANE_BACKGROUND_IMAGE = "classic.jpg";
   public static String CENTER_PANE_BACKGROUND_IMAGE = "";
   
   protected BufferedImage m_ShowPDFProofImage;
   protected BufferedImage m_ApprovePDFProofImage;
   
   protected SimplePDFViewer(PDFPageHandler pdfPgHdlr)
	{
      super(pdfPgHdlr, PDFPageHandler.THUMBVIEW_IMAGE_PANE, 
            CENTER_PANE_BACKGROUND_IMAGE);
      this.createPDFViewUI();
      m_ShowPDFProofImage = getPDFViewerImage("proof.gif");
      m_ApprovePDFProofImage = getPDFViewerImage("approve.gif");
      this.revalidate();
      this.setVisible(true);
      repaint();
      final BufferedImage progBarImg = getPDFViewerImage(PROGRESS_BAR_IMAGE);
      m_PDFPageHandler.setProgBarImage(progBarImg);
      SwingUtilities.invokeLater(new Runnable() 
      {
         public void run() {
            initPageHandler(PDFPageHandler.THUMBVIEW_IMAGE_PANE, 
                            CENTER_PANE_BACKGROUND_IMAGE, progBarImg);
         }
      });
   }
   
   private void createPDFViewUI()
   {
      // Settting the Layout of PDFView to BorderLayout
      this.setLayout(new BorderLayout());
      //BufferedImage backGrdImg = getPDFViewerImage(CENTER_PANE_BACKGROUND_IMAGE);
      JPanel contentPane = m_PDFPageHandler.createContentPane();
		this.add(contentPane,BorderLayout.CENTER);
   }

   protected boolean showAlwaysPDFThumbImages()
   {
      return true;
   }
   
   protected boolean hasPDFActionComps()
   {
      return true;
   }
   
   protected Map<Integer, BufferedImage> getPDFActionImages()
   {
      Map<Integer, BufferedImage> actionComps = new HashMap<Integer, BufferedImage>();
      actionComps.put(PDFPageHandler.PDF_PROOF, m_ShowPDFProofImage);
      actionComps.put(PDFPageHandler.APPROVE_PDF, m_ApprovePDFProofImage);
      return actionComps;
   }
   
   protected Map<Integer, Rectangle> getPDFActions(Dimension panelSize)
   {
      Rectangle proofPdfRect, approvePdfRect;
      int startx = panelSize.width - m_ShowPDFProofImage.getWidth() - 
                   m_ApprovePDFProofImage.getWidth() - 20;
      proofPdfRect = new Rectangle(startx, 10, m_ShowPDFProofImage.getWidth(),
                                   m_ShowPDFProofImage.getHeight());
      startx = panelSize.width - m_ApprovePDFProofImage.getWidth() - 10;
      approvePdfRect = new Rectangle(startx, 10, m_ApprovePDFProofImage.getWidth(),
                                    m_ApprovePDFProofImage.getHeight());
      Map<Integer, Rectangle> actionComps = new HashMap<Integer, Rectangle>();
      actionComps.put(PDFPageHandler.PDF_PROOF, proofPdfRect);
      actionComps.put(PDFPageHandler.APPROVE_PDF, approvePdfRect);
      return actionComps;
   }
   
   protected boolean showPDFThumbPages()
   {
      return false;
   }
   
   protected Map<Integer, Rectangle> getPDFThumbPageViewBBox(Dimension panelSize)
   {
      int pgNum = 1;
      int hgap = 10;
      int imgWt = m_PDFPageBookmarkImages.get(pgNum).getWidth();
      int imgHt = m_PDFPageBookmarkImages.get(pgNum).getHeight();
      int numPages = m_PDFPageBookmarkImages.size();
      int margin = panelSize.width - ((numPages*imgWt) + ((numPages-1)*hgap));
      int startX = margin/2, startY = panelSize.height - (imgHt + 5);
      int x = 0, y = startY;
      Rectangle pageThumbRect = null;
      Map<Integer, Rectangle> pdfPageThumbRect = new HashMap<Integer, Rectangle>();
      for (int i = 0; i < numPages; i++) 
      {
         x = startX + i*imgWt + i*hgap;
         pgNum = i+1;
         pageThumbRect = new Rectangle(x, y, imgWt, imgHt);
         pdfPageThumbRect.put(pgNum, pageThumbRect);
      }
      return pdfPageThumbRect;
   }
   
}
