// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.util.RGPTLogger;
import com.rgpt.viewer.PDFViewer;

public abstract class PDFViewer extends JPanel {
	public static final String SIMPLE_PDF_VIEWER = "Simple_PDF_Viewer";
	public static final String BASIC_PDF_VIEWER = "Basic_PDF_Viewer";
	public static final String ADVANCED_PDF_VIEWER = "Advance_PDF_Viewer";

	public static int THUMBVIEW_HEIGHT = 50;
	public static int THUMBVIEW_WIDTH = 50;

	public static String PROGRESS_BAR_IMAGE = "progress_bar.gif";

	// Maintains references to the child Objects
	private static Map<String, PDFViewer> m_PDFViewers;

	// The PDFPageViewer component takes care of actual display of PDF Page
	protected static PDFPageHandler m_PDFPageHandler;

	// Bookmark Images of PDF Pages
	Map<Integer, BufferedImage> m_PDFPageBookmarkImages;

	static {
		m_PDFViewers = new HashMap<String, PDFViewer>();
	}

	protected PDFViewer(PDFPageHandler pdfPgHdlr, final int thumbViewImgMode,
			final String backgroundImg) {
		m_PDFPageHandler = pdfPgHdlr;
	}

	protected void initPageHandler(final int thumbViewImgMode,
			final String backgroundImg, final BufferedImage progBarImg) {
		BufferedImage pdfPgBckgroundImg = getPDFViewerImage(backgroundImg);
		if (m_PDFPageHandler == null)
			return;
		m_PDFPageHandler.initPDFPageHandler(this, thumbViewImgMode, progBarImg,
				pdfPgBckgroundImg);
		m_PDFPageBookmarkImages = m_PDFPageHandler.getPDFPageBookmarks();
		this.activatePDFViewer();
		this.handlePDFPageBookmarks();
	}

	protected void activatePDFViewer() {
	}

	protected void handlePDFPageBookmarks() {
	}

	protected BufferedImage getPDFViewerImage(String imageName) {
		BufferedImage backgroundImg = null;
		try {
			if (imageName == null || imageName.length() == 0)
				return null;
			String imgLoc = PDFPageHandler.IMAGE_PATH + imageName;
			backgroundImg = ImageUtils
					.getBufferedImage(imgLoc, this.getClass());
			System.out.println("PDFView BackgroundImage: "
					+ backgroundImg.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return backgroundImg;
	}

	protected boolean hasPDFActionComps() {
		return false;
	}

	protected Map<Integer, BufferedImage> getPDFActionImages() {
		return null;
	}

	protected Map<Integer, Rectangle> getPDFActions(Dimension panelSize) {
		return null;
	}

	protected boolean showAlwaysPDFThumbImages() {
		return false;
	}

	protected boolean showPDFThumbPages() {
		return false;
	}

	protected void updatePDFThumbPage(int pgNum, BufferedImage pdfThumbPg) {
		m_PDFPageBookmarkImages.put(pgNum, pdfThumbPg);
	}

	protected Map<Integer, BufferedImage> getPDFThumbPages() {
		return m_PDFPageBookmarkImages;
	}

	protected Map<Integer, Rectangle> getPDFThumbPageViewBBox(
			Dimension panelSize) {
		return null;
	}

	public static PDFViewer getInstance(PDFPageHandler pdfPgHdlr,
			String pdfViewerName) {
		RGPTLogger.logToFile("Getting Instance from PDFViewer ");
		PDFViewer pdfView = m_PDFViewers.get(pdfViewerName);
		RGPTLogger.logToFile("PDFView Cached: " + pdfView);
		if (pdfView != null)
			return pdfView;
		if (pdfViewerName.equals(ADVANCED_PDF_VIEWER))
			pdfView = new AdvancedPDFViewer(pdfPgHdlr);
		else if (pdfViewerName.equals(BASIC_PDF_VIEWER))
			pdfView = new BasicPDFViewer(pdfPgHdlr);
		else if (pdfViewerName.equals(SIMPLE_PDF_VIEWER))
			pdfView = new SimplePDFViewer(pdfPgHdlr);
		else
			RGPTLogger.logToFile("No Match Found for: " + pdfViewerName);
		RGPTLogger.logToFile("Created PDFView Cached: " + pdfView);
		m_PDFViewers.put(pdfViewerName, pdfView);
		return pdfView;
	}

	public static void cleanUpMemory() {
		m_PDFViewers.clear();
		m_PDFPageHandler = null;
	}
}
