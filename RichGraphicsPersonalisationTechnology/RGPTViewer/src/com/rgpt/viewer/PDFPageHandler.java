// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

public interface PDFPageHandler {
	public static int THUMBVIEW_IMAGE_FRAME = 0;
	public static int THUMBVIEW_IMAGE_PANE = 1;

	public static String ALLIGN_LEFT = "Left Allign";
	public static String ALLIGN_CENTER = "Center Allign";
	public static String ALLIGN_RIGHT = "Right Allign";
	public static String ALLIGN_PAGE_LEFT = "Page Left Allign";
	public static String ALLIGN_PAGE_CENTER = "Page Center Allign";
	public static String ALLIGN_PAGE_RIGHT = "Page Right Allign";

	public static String IMAGE_PATH = "res/pdfviewer/";

	// Action Component Indicator
	public static int EDIT_PDF = 1;
	public static int RESIZE_MOVE_PDF = 2;
	public static int PDF_PROOF = 3;
	public static int APPROVE_PDF = 4;
	public static int SELECT_PDF = 5;
	public static int DELETE_PDF = 6;
	public static int SAVE_PDF_WORK = 7;
	public static int ZOOM_PDF_BOX = 8;
	public static int PDF_PAGE_FIELD = 9;

	// This method is called as soon as the PDFPageHandler is created so as to
	// retrieve appropriate data from Server
	public void initPDFPageHandler(PDFViewer pdfViewer, int thumbImgViewMode,
			BufferedImage progBarImg, BufferedImage backGrdImg);

	// This is set the Background Image of the PDF Page Viewer
	public void setProgBarImage(BufferedImage progBarImg);

	// This is set the Background Image of the PDF Page Viewer
	public void setBackgroundImage(BufferedImage backGrdImg);

	// This method is to register Action Buttons with PDF Page Handler Object.
	// This is to mainly to activate and de-activate button based on conditions.
	public void regPDFActionComponent(int actionId, JComponent actionComp);

	// Checking conditions to enable or disable Button
	public void setButtonActivation();

	// This method is used to create and retrieve the Content Pane for viewing
	// and manipulating the PDF Page Content
	public JPanel createContentPane();

	// Page Navigation Functions
	public void displayFirstPDFPage();

	public void displayPrevPDFPage();

	public void displayNextPDFPage();

	public void displayLastPDFPage();

	public int getPageCount();

	public int getPDFPageNum();

	// This method is used to display PDF Page corresponding to the Page Number
	public void displayPDFPage(int pageNum);

	// This method retrieves the Bookmark BufferedImages of all PDF Pages
	public Map<Integer, BufferedImage> getPDFPageBookmarks();

	// This method is invoked to select VDP field for delete or allign
	public void selectPDF();

	// This method is invoked to edit and personalize PDF
	public void editPDF();

	// This method is invoked to close the PDF Viewer
	public void closePDFViwer();

	// This method is invoked to save the PDF Work
	public void savePDFWork();

	// This method is invoked to resize or move VDP field
	public void resizePDF();

	// This method is invoked to delete VDP field
	public void deleteVDPField();

	// This method is invoked to allign VDP field to Left, Center or Right WRT
	// BBox
	public void allignWRTBBox(String allign);

	// This method is invoked to allign VDP field to Left, Center or Right WRT
	// BBox
	public void allignWRTPage(String allign);

	// This method is invoked to scale PDF Page along Height, Width and coplete
	// Page
	public void scalePDFPage(String scaleFactor);

	// This method is invoked to zoom in and out of PDF Page
	public void zoomInPDFPage();

	public void zoomOutPDFPage();

	public void setPDFZoomValue(int zoomValue);

	// This method is invoked to show PDF Proof
	public void showPDFProof();

	// This method is invoked to approve PDF Proof
	public void approvePDF();

	// This is to check if the PDFProof is generated
	public boolean isPDFProofGenerated();

	// This is to called to repaint the Viewer
	public void reapintViewer();

	// This method is invoked to show PDF in EBook Preview
	public void showEBookView(JPanel ebookPanel, boolean showPreview);
}
