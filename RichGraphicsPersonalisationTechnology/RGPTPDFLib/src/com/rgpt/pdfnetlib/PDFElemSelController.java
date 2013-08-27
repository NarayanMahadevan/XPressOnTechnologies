package com.rgpt.pdfnetlib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JDialog;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.PDF.Element;
import pdftron.PDF.ElementReader;
import pdftron.PDF.PDFViewCtrl;
import pdftron.PDF.Page;
import pdftron.PDF.Rect;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.SelectedImageHandler;
import com.rgpt.pdflib.ImageSelectionController;
import com.rgpt.pdflib.PDFElemSelHandler;
import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.PDFMatrix2D;
import com.rgpt.pdflib.PDFPage.PageData;
import com.rgpt.pdflib.PDFViewHandler;
import com.rgpt.pdflib.VDPElement;
import com.rgpt.pdflib.VDPElement.SelType;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTUtil;
import com.rgpt.viewer.ThumbviewToolBar;

public class PDFElemSelController implements PDFElemSelHandler {

	Page m_PDFPage;
	Matrix2D m_PageCTM;
	Matrix2D m_DeviceCTM;
	Matrix2D m_PageRotCTM;
	PDFViewCtrl m_PDFViewCtrl;
	PDFNetViewController m_PDFViewer;

	// Stores Font File embedded into PDF Document and the corresponding PDFNet
	// Object id
	public Map<String, Long> m_EmbedFontFile;

	// This is used to select the exact PDF Image Element
	SelectedImageHandler m_ImageSelHdlr = null;

	public PDFElemSelController(PDFViewHandler pdfVw, PDFMatrix2D devCTM,
			Map<PageData, Object> pageData) {
		m_DeviceCTM = (Matrix2D) devCTM.getPDFMatrix2D();
		m_PDFPage = ((PDFNetPage) pageData.get(PageData.PDFPage)).m_Page;
		m_PageCTM = (Matrix2D) ((PDFMatrix2D) pageData.get(PageData.PageMatrix))
				.getPDFMatrix2D();
		m_PageRotCTM = (Matrix2D) ((PDFMatrix2D) pageData
				.get(PageData.PageRotMatrix)).getPDFMatrix2D();
		m_PDFViewer = (PDFNetViewController) pdfVw;
		m_PDFViewCtrl = m_PDFViewer.m_PDFViewCtrl;
	}

	@Override
	public void selectImageElements(int x, int y) {
		try {
			Point2D.Double pgPt = m_PDFViewCtrl.convScreenPtToPagePt(x, y);
			RGPTLogger.logDebugMesg("Page Pts: " + pgPt);
			Element elem = getImageElements(pgPt.x, pgPt.y);
			RGPTLogger.logDebugMesg("Element Selected: " + elem);
			addSelectedImage(elem);
		} catch (PDFNetException ex) {

		} catch (PDFLibException ex) {

		}
	}

	@Override
	public void mousePressed(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {
		this.m_PDFViewCtrl.update();
	}

	@Override
	public void displaySelection(Rectangle2D.Double rect2D)
			throws PDFLibException {
		try {
			Rect rect = new Rect(rect2D);
			Rectangle dispRect = getDisplayRectangle(rect);
			System.out
					.println("Display Rectangle BBox: " + dispRect.toString());
			Graphics2D g2d = (Graphics2D) this.m_PDFViewCtrl.getGraphics();
			g2d.setPaint(Color.BLACK);
			BasicStroke basicStroke = new BasicStroke(2.0f);
			g2d.setStroke(basicStroke);
			g2d.draw(dispRect);
			Color col = getSelColor();
			g2d.setColor(col);
			g2d.fill(dispRect);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public void addSelectedImage(Map<String, Object> selImgMap)
			throws PDFLibException {
		try {
			Element element = PDFNetUtil.getMappedImageElement(m_PDFPage,
					selImgMap);
			addSelectedImage(element);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	public void addSelectedImage(Element elem) throws PDFNetException,
			PDFLibException {
		// Refer line 2914
		Rect rect = elem.getBBox();
		int currentpage = m_PDFPage.getIndex();
		// Search image is in selection prior to loading
		Vector<VDPElement> vdpElements = m_PDFViewer.m_VDPElements;

		// This is the last selection added to the Vector
		int selIndex = vdpElements.size() - 1;

		if (PDFNetUtil.searchVDPElement(vdpElements, currentpage, rect,
				SelType.IMAGE) < 0) {
			// Refer line 1124
			vdpElements.addElement(VDPElementHandler.createVDPElement(
					currentpage, SelType.IMAGE, elem, selIndex));
		}
	}

	public Rectangle getDisplayRectangle(Rect rect) throws PDFNetException {
		Matrix2D mtx = this.m_DeviceCTM.multiply(this.m_PageCTM);
		java.awt.geom.Point2D.Double t1, t2;
		t1 = mtx.multPoint(rect.getX1(), rect.getY1());
		t2 = mtx.multPoint(rect.getX2(), rect.getY2());
		return RGPTUtil.getRectangle(t1, t2);
	}

	private Color getSelColor() {
		float rcol, gcol, bcol, alpha;
		rcol = new Float(0.5);
		gcol = new Float(0.1);
		bcol = new Float(0.7);
		alpha = new Float(0.2);
		Color col = new Color(rcol, gcol, bcol, alpha);
		return col;
	}

	//
	// Functions to access Elements in PDF Page
	//

	private Element getImageElements(double x, double y) throws PDFNetException {
		// Get all the Image Elements on the page
		int pageNum = m_PDFPage.getIndex();
		Vector<Map<String, Object>> pageImageElements = m_PDFViewer.m_PageImageElements
				.get(pageNum);
		if (pageImageElements == null) {
			ElementReader reader = new ElementReader();
			reader.begin(m_PDFPage);
			pageImageElements = PDFNetUtil.createPageImageElements(m_PDFPage,
					reader);
			RGPTLogger.logToFile("Total Number of Images Found in Page: "
					+ pageNum + " is: " + pageImageElements.size());
			RGPTLogger.logToFile("\n*****Image Details are:*****\n\n"
					+ pageImageElements.toString());
			m_PDFViewer.m_PageImageElements.put(new Integer(pageNum),
					pageImageElements);
			reader.end();
		}

		// Get all the Image Elements selected by the user at point x, y
		Vector<Map<String, Object>> selectedImages = PDFNetUtil
				.getImageElements(pageNum, pageImageElements,
						m_PDFViewer.m_VDPElements, x, y);

		if (selectedImages == null || selectedImages.size() == 0)
			return null;

		RGPTLogger
				.logDebugMesg("Total Number of Images Found for selected point is: "
						+ selectedImages.size());

		// Show all the Images Selected for the Point to the End User
		// The End User Selects Appropriate Image to make as Variable
		ImageHolder imgHldr = null;
		Map<String, Object> selImageElement = null;

		// The Images userSelImages is passed to ThumviewImageBox for user
		// selection
		Map<Integer, ImageHolder> userSelImages = new HashMap<Integer, ImageHolder>();

		// This iteration is to extract images from the PDF Element and show the
		// images to the user to get the exact image selection as Variable Data
		// Element
		for (int i = 0; i < selectedImages.size(); i++) {
			selImageElement = selectedImages.elementAt(i);
			if (selectedImages.size() == 1) {
				return PDFNetUtil.getMappedImageElement(m_PDFPage,
						selImageElement);
			}
			imgHldr = PDFNetUtil.extractImage(m_PDFViewCtrl.getDoc(),
					selImageElement);
			if (imgHldr == null)
				continue;
			int assetId = i + 1;
			imgHldr.m_AssetId = assetId;
			userSelImages.put(assetId, imgHldr);
			selImageElement.put("DigitalAssetId", assetId);
		}

		// This ensures the Image Handler Window is closed and Memory is
		// released.
		this.close();

		ThumbviewToolBar imgSelUI = null;
		imgSelUI = new ThumbviewToolBar(userSelImages);
		imgSelUI.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		imgSelUI.setLocation(100, 600);
		imgSelUI.setVisible(true);
		imgSelUI.repaintContent();

		// Populating the Handler and the Image Selection UI for Selecting the
		// exact PDF Image Element to be added to the selection
		m_ImageSelHdlr = new ImageSelectionController(imgSelUI, this,
				selectedImages);
		imgSelUI.setImageSelectionHandler(m_ImageSelHdlr);

		// Multiple Images are selected. The user will select any one using
		// ThumbViewImageFrame. And correspondingly the call will be made to
		// PDF Viewer to add selection. Hence null is returned in this scenario.
		return null;

	}

	@Override
	public void close() {
		if (m_ImageSelHdlr != null) {
			m_ImageSelHdlr.close();
			m_ImageSelHdlr = null;
		}
	}

}
