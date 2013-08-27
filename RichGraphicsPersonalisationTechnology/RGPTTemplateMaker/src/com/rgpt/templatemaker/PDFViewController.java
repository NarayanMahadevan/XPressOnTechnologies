package com.rgpt.templatemaker;

import java.awt.Cursor;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.rgpt.pdflib.PDFDoc;
import com.rgpt.pdflib.PDFErrorReportListener;
import com.rgpt.pdflib.PDFLibConnector;
import com.rgpt.pdflib.PDFLibConnector.PDFLib;
import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.PDFMatrix2D;
import com.rgpt.pdflib.PDFPage.PageData;
import com.rgpt.pdflib.PDFThumbViewHandler;
import com.rgpt.pdflib.PDFView;
import com.rgpt.pdflib.PDFView.VDPMode;
import com.rgpt.pdflib.PDFViewHandler;
import com.rgpt.util.LocalizationUtil;
import com.rgpt.util.PDFFilter;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTUIUtil;

/**
 * This is a singleton class and the main purpose is to be a bridge to interact
 * with PDF Library
 * 
 * @author Narayan
 * 
 */

class PDFErrorProc implements PDFErrorReportListener {

	public void reportError(String message, Object data) {
		JOptionPane.showMessageDialog((JFrame) data, message,
				"PDFViewCtrl Error", JOptionPane.ERROR_MESSAGE);
	}
}

public class PDFViewController {

	public enum SelMode {
		NONE, EDIT_MODE, DEL_MODE
	}

	public SelMode m_SelMode = SelMode.NONE;

	// VDP Mode points to the constant field defined in StaticFieldInfo Class
	// 0 - Default Mode, No Selection
	// 1 - Text Mode
	// 2 - Image Mode
	// 3 - Draw Text Curve Mode
	// 4 - Draw Text Rect Mode
	public VDPMode m_VDPMode = VDPMode.NULL;

	public PDFView m_PDFView;
	public PDFLibConnector m_PDFLibConnector;
	public PDFViewer m_PDFViewer;
	public PDFViewHandler m_PDFViewHandler;
	public PDFThumbViewHandler m_PDFThumbView;
	public RGPTTemplateMaker m_RGPTTemplateMaker;
	public static PDFViewController m_PDFViewController;

	// PDF Doc and corresponding page data
	public PDFMatrix2D m_DeviceCTM;
	public Map<PageData, Object> m_PDFPageData;

	private PDFViewController() {
		m_PDFLibConnector = PDFLibConnector.getInstance(PDFLib.PDFNET);
	}

	public static PDFViewController getInstance() {
		if (m_PDFViewController == null)
			m_PDFViewController = new PDFViewController();
		return m_PDFViewController;
	}

	public void initPDFViewController(RGPTTemplateMaker templMaker) {
		m_RGPTTemplateMaker = templMaker;
		m_PDFLibConnector.initPDFLib();
		m_PDFViewer = new PDFViewer(this);
	}

	public void terminatePDFLib() {
		m_PDFLibConnector.terminatePDFLib();
	}

	public void open() {
		// Closing the Opened PDF before opening the new one.
		if (isPDFDocActive())
			this.close();

		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new PDFFilter());
		int returnVal = fc.showOpenDialog(m_RGPTTemplateMaker);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// Creating PDF Doc for the file path
		String origFilePath = fc.getSelectedFile().getAbsolutePath();
		String origlFileName = fc.getSelectedFile().getName();
		boolean readInMemory = true;
		PDFDoc doc = createPDFDoc(origFilePath, readInMemory);

		// Creating PDF View Handler to display PDF Pages
		m_PDFViewHandler = m_PDFLibConnector.getPDFViewHandler();
		m_PDFView = m_PDFLibConnector.getPDFView();
		m_PDFView.setPPFHandler(m_PDFViewHandler);
		m_PDFViewHandler.setDoc(doc);
		m_PDFViewHandler.setPDFPageListener(m_RGPTTemplateMaker.m_StatusBar,
				null);
		m_PDFViewHandler.setErrorReportListener(new PDFErrorProc(),
				m_RGPTTemplateMaker);
		m_PDFViewer.setPDFViewHandler(m_PDFViewHandler);
		// m_RGPTTemplateMaker.setPDFViewer(m_PDFViewer);

		// Showing PDF Pages in thumb view
		int thumbNailWt = RGPTParams.getIntVal("ThumbNailWidth");
		m_PDFThumbView = m_PDFLibConnector.getPDFThumbViewHandler(thumbNailWt);
		m_PDFThumbView.setPDFView(m_PDFViewHandler);
		// m_RGPTTemplateMaker.setPDFThumbView(m_PDFThumbView.getViewPanel());

		// Sets the PDF Page being viewed
		setPDFPageParams();

		// Setting the Template Maker View with the PDF Document
		int pgno = m_PDFViewHandler.getCurrentPage();
		int totalpages = m_PDFViewHandler.getPageCount();
		m_RGPTTemplateMaker.setView(origlFileName, pgno, totalpages,
				m_PDFThumbView.getViewPanel(), m_PDFViewer);
	}

	private void setPDFPageParams() {
		try {
			this.m_DeviceCTM = m_PDFViewHandler.getDeviceTransform();
			this.m_PDFPageData = m_PDFViewHandler.getDoc()
					.getPDFPage(m_PDFViewHandler.getCurrentPage())
					.getPageData();
			RGPTLogger.logDebugMesg("DeviceCTM: " + m_DeviceCTM + " PageData: "
					+ m_PDFPageData);
			m_PDFView.setPDFElemSelHandler(m_PDFLibConnector
					.createPDFElemSelHandler(m_DeviceCTM, m_PDFPageData));
		} catch (PDFLibException ex) {
			ex.printStackTrace();
			String[] errorInfo = LocalizationUtil
					.getText("ERROR_PDF_PAGE_VIEW").split("::");
			RGPTUIUtil.showError(m_RGPTTemplateMaker, errorInfo[1],
					errorInfo[0]);
		}
	}

	public PDFDoc createPDFDoc(String filepath, boolean readInMemory) {
		try {
			PDFDoc doc = m_PDFLibConnector.createPDFDoc(filepath, readInMemory);
			if (!doc.initSecurityHandler()) {
				boolean success = true;
				success = PasswordDialog.isSuccessful(m_RGPTTemplateMaker, doc);
				if (!success) {
					String errorInfo = LocalizationUtil
							.getText("ERROR_DOC_AUTH");
					RGPTUIUtil.showError(m_RGPTTemplateMaker, errorInfo, "");
					return null;
				}
			}
			return doc;
		} catch (PDFLibException ex) {
			ex.printStackTrace();
			String[] errorInfo = LocalizationUtil.getText("ERROR_PDF_VIEW")
					.split("::");
			RGPTUIUtil.showError(m_RGPTTemplateMaker, errorInfo[1],
					errorInfo[0]);
			return null;
		}
	}

	public void setvdpImgmode() {
		// No Action taken if there is no active PDF Document
		if (!isPDFDocActive())
			return;
		if (m_VDPMode == VDPMode.MARK_IMAGE) {
			resetVDPMode();
			return;
		}
		m_VDPMode = VDPMode.MARK_IMAGE;
		String dispSelMode = LocalizationUtil.getText("VDPModeSelImage");
		String dispStatus = LocalizationUtil.getText("DispStatusSelImage");
		setVDPMode(Cursor.DEFAULT_CURSOR, dispSelMode, dispStatus);
	}

	@SuppressWarnings("deprecation")
	private void setVDPMode(int cursor, String dispSelMode, String dispStatus) {
		m_RGPTTemplateMaker.setCursor(cursor);
		m_RGPTTemplateMaker.setButtonActivation();
		m_RGPTTemplateMaker.m_StatusBar.displaymode(dispSelMode);
		m_RGPTTemplateMaker.m_StatusBar.displaystatus(dispStatus);
		RGPTLogger.logDebugMesg("VDP Sel: " + dispSelMode + " Status: "
				+ dispStatus);
	}

	@SuppressWarnings("deprecation")
	private void resetVDPMode() {
		m_RGPTTemplateMaker.setCursor(Cursor.DEFAULT_CURSOR);
		m_RGPTTemplateMaker.m_StatusBar.displaymode(" ");
		m_SelMode = SelMode.NONE;
		m_VDPMode = VDPMode.NULL;
	}

	public boolean isPDFDocActive() {
		if (m_PDFViewHandler == null)
			return false;
		return true;
	}

	public void close() {
		resetVDPMode();
		m_PDFView.close();
		m_PDFViewer.close();
		m_PDFView = null;
		m_PDFViewHandler = null;
		m_PDFThumbView = null;
		m_RGPTTemplateMaker.resetView();
		m_PDFLibConnector.closePDFLibHandlers();
	}
}
