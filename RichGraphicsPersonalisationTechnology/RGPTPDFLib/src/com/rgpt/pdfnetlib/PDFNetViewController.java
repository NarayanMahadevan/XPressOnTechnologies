package com.rgpt.pdfnetlib;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.PDF.PDFViewCtrl;
import pdftron.PDF.PDFViewScrollPane;

import com.rgpt.pdflib.PDFDoc;
import com.rgpt.pdflib.PDFErrorReportListener;
import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.PDFMatrix2D;
import com.rgpt.pdflib.PDFPageListener;
import com.rgpt.pdflib.PDFViewHandler;
import com.rgpt.pdflib.VDPElement;

public class PDFNetViewController implements PDFViewHandler {

	PDFNetViewer m_PDFViewCtrl;

	// This maintains PDF Image Elements for every Page
	Map<Integer, Vector<Map<String, Object>>> m_PageImageElements;

	// This maintains the Elements that are selected for Variable Data Element
	Vector<VDPElement> m_VDPElements;

	public PDFNetViewController() {
		// m_PDFViewCtrl = new PDFViewCtrl();
		m_PageImageElements = new HashMap<Integer, Vector<Map<String, Object>>>();
		m_VDPElements = new Vector<VDPElement>();
	}

	void setPDFViewCtrl(PDFNetViewer viewer) {
		m_PDFViewCtrl = viewer;
	}

	@Override
	public void setDoc(PDFDoc doc) {
		m_PDFViewCtrl.setDoc((pdftron.PDF.PDFDoc) doc.getPDFDoc());

		// Setting the default view mode for the pdf to be viewed
		m_PDFViewCtrl.setToolMode(0);
		m_PDFViewCtrl.setPagePresentationMode(PDFViewCtrl.e_single_page);
		m_PDFViewCtrl.setPageViewMode(PDFViewCtrl.e_fit_page);
	}

	@Override
	public PDFDoc getDoc() {
		return new PDFNetDoc(m_PDFViewCtrl.getDoc());
	}

	@Override
	public void setCurrentPage(int pageNum) {
		m_PDFViewCtrl.setCurrentPage(pageNum);
	}

	@Override
	public JPanel getViewPanel() {
		return m_PDFViewCtrl.getViewPane();
	}

	@Override
	public JPanel setContentPane(JSplitPane splitPane) {
		PDFViewScrollPane contentPane = m_PDFViewCtrl.getViewPane();
		splitPane.setRightComponent(m_PDFViewCtrl.getViewPane());
		return contentPane;
	}

	@Override
	public void setErrorReportListener(PDFErrorReportListener listener,
			Object data) {
		m_PDFViewCtrl.setErrorReportProc(new PDFErrorReporter(listener), data);
	}

	@Override
	public void setPDFPageListener(PDFPageListener listener, Object data) {
		m_PDFViewCtrl.setCurrentPageProc(
				new com.rgpt.pdfnetlib.PDFPageNotifier(listener), data);
	}

	@Override
	public int getPageCount() {
		return m_PDFViewCtrl.getPageCount();
	}

	@Override
	public int getCurrentPage() {
		return m_PDFViewCtrl.getCurrentPage();
	}

	@Override
	public boolean gotoNextPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean gotoLastPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean gotoFirstPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean gotoPreviousPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRasterizer(boolean isEnabled) {
		m_PDFViewCtrl.setRasterizer(isEnabled);
	}

	@Override
	public void setOverprint(int operator) throws PDFLibException {
		if (operator < 0 || operator > 2)
			return;
		try {
			m_PDFViewCtrl.setOverprint(operator);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public void setAntiAliasing(boolean isEnabled) {
		m_PDFViewCtrl.setAntiAliasing(isEnabled);
	}

	@Override
	public void setImageSmoothing(boolean isEnabled) {
		m_PDFViewCtrl.setImageSmoothing(isEnabled);
	}

	@Override
	public void setPageTransparencyGrid(boolean isEnabled) {
		m_PDFViewCtrl.setPageTransparencyGrid(isEnabled);
	}

	@Override
	public double getZoom() {
		return m_PDFViewCtrl.getZoom();
	}

	@Override
	public boolean setZoom(double val) {
		return m_PDFViewCtrl.setZoom(val);
	}

	@Override
	public void rotateClockwise() {
		m_PDFViewCtrl.rotateClockwise();
	}

	@Override
	public void rotateCounterClockwise() {
		m_PDFViewCtrl.rotateCounterClockwise();
	}

	@Override
	public void setPageViewMode(PageViewMode mode) {
		switch (mode) {
		case FIT_PAGE:
			m_PDFViewCtrl.setPageViewMode(PDFViewCtrl.e_fit_page);
			break;
		case FIT_WIDTH:
			m_PDFViewCtrl.setPageViewMode(PDFViewCtrl.e_fit_width);
			break;
		}
	}

	@Override
	public void setPagePresentationMode(PagePresentationMode mode) {
		switch (mode) {
		case SINGLE_PAGE:
			m_PDFViewCtrl.setPagePresentationMode(PDFViewCtrl.e_single_page);
			break;
		case SINGLE_CONTINUOUS:
			m_PDFViewCtrl
					.setPagePresentationMode(PDFViewCtrl.e_single_continuous);
			break;
		case FACING:
			m_PDFViewCtrl.setPagePresentationMode(PDFViewCtrl.e_facing);
			break;
		case FACING_CONTINUOUS:
			m_PDFViewCtrl
					.setPagePresentationMode(PDFViewCtrl.e_facing_continuous);
			break;

		}
	}

	@Override
	public void setSelectionMode(SelectionMode mode) {
		if (mode == null)
			return;
		switch (mode) {
		case PAN_MODE:
			m_PDFViewCtrl.setToolMode(PDFViewCtrl.e_pan);
			break;
		case TEXT_RECT_SELECT:
			m_PDFViewCtrl.setToolMode(PDFViewCtrl.e_text_rect_select);
			break;
		case TEXT_STRUCT_SELECT:
			m_PDFViewCtrl.setToolMode(PDFViewCtrl.e_text_struct_select);
			break;
		}
	}

	@Override
	public PDFMatrix2D getDeviceTransform() throws PDFLibException {
		try {
			Matrix2D devctm = handleDeviceCTM(m_PDFViewCtrl
					.getDeviceTransform());
			return new PDFNetMatrix2D(devctm);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public PDFMatrix2D getDeviceTransform(int pageNum) throws PDFLibException {
		try {
			Matrix2D devctm = handleDeviceCTM(m_PDFViewCtrl
					.getDeviceTransform(pageNum));
			return new PDFNetMatrix2D(devctm);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	private Matrix2D handleDeviceCTM(Matrix2D devctm) throws PDFNetException {
		if ((devctm.getA() == 0 && devctm.getD() == 0)
				&& (devctm.getB() != 0 && devctm.getC() != 0)) {
			// System.out.println("Scale values coming in Shx, Shy instead of Sx and Sy...");
			devctm = new Matrix2D(devctm.getB(), 0, 0, devctm.getC(),
					devctm.getH(), devctm.getV());
		}
		return devctm;
	}

	@Override
	public void close() {
		try {
			m_PDFViewCtrl.getDoc().close();
		} catch (Exception ex) {
		}
		m_PDFViewCtrl.destroy();
		m_PDFViewCtrl = null;
	}

	@Override
	public void destroy() {
		m_PDFViewCtrl.destroy();
		m_PDFViewCtrl = null;
	}

}
