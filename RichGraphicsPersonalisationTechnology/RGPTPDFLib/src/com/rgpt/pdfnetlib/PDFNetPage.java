package com.rgpt.pdfnetlib;

import java.util.HashMap;
import java.util.Map;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.PDF.Page;

import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.PDFMatrix2D;
import com.rgpt.pdflib.PDFPage;

public class PDFNetPage extends PDFPage {

	Page m_Page;
	PDFNetMatrix2D m_PageRotMatrix2D;

	public PDFNetPage(Page page) throws PDFLibException {
		super();
		m_Page = page;
	}

	@Override
	public Map<PageData, Object> getPageData() throws PDFLibException {
		if (m_PageData != null)
			return m_PageData;
		m_PageData = new HashMap<PageData, Object>();
		m_PageData.put(PageData.PDFPage, this);
		m_PageData.put(PageData.PageNum, this.getPageNum());
		m_PageData.put(PageData.PageRot, this.getPageRotation());
		m_PageData.put(PageData.PageWt, this.getPageWidth());
		m_PageData.put(PageData.PageHt, this.getPageHeight());
		m_PageData.put(PageData.PageMatrix, this.getPageMatrix());
		m_PageData.put(PageData.PageRotMatrix, m_PageRotMatrix2D);
		return m_PageData;
	}

	@Override
	public PageRotation getPageRotation() throws PDFLibException {
		try {
			int rot = m_Page.getRotation();
			PageRotation pgRot = PageRotation.Rot_0;
			double ht = m_Page.getPageHeight();
			double wd = m_Page.getPageWidth();
			Matrix2D rotMtx = Matrix2D.identityMatrix();

			switch (rot) {
			case Page.e_0: // System.out.println("Page Rotation 0");
				rotMtx = new Matrix2D(1.0, 0.0, 0.0, 1.0, 1.0, 1.0);
				pgRot = PageRotation.Rot_0;
				break;
			case Page.e_90: // System.out.println("Page Rotation 90");
				rotMtx = new Matrix2D(-1.0, 0.0, 0.0, 1.0, wd, 1.0);
				pgRot = PageRotation.Rot_90;
				break;
			case Page.e_180: // System.out.println("Page Rotation 180");
				rotMtx = new Matrix2D(-1.0, 0.0, 0.0, -1.0, ht, wd);
				pgRot = PageRotation.Rot_180;
				break;
			case Page.e_270: // System.out.println("Page Rotation 270");
				rotMtx = new Matrix2D(1.0, 0.0, 0.0, -1.0, 1, ht);
				pgRot = PageRotation.Rot_270;
				break;
			}
			m_PageRotMatrix2D = new PDFNetMatrix2D(rotMtx);
			return pgRot;
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public double getPageWidth() throws PDFLibException {
		try {
			return m_Page.getPageWidth();
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public double getPageHeight() throws PDFLibException {
		try {
			return m_Page.getPageHeight();
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public PDFMatrix2D getPageMatrix() throws PDFLibException {
		try {
			return new PDFNetMatrix2D(m_Page.getDefaultMatrix(false, 0, 0));
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public int getPageNum() throws PDFLibException {
		try {
			return m_Page.getIndex();
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

}
