package com.rgpt.pdfnetlib;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import pdftron.Common.PDFNetException;
import pdftron.PDF.Rect;
import pdftron.SDF.Obj;

import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.PDFRect;

public class PDFNetRect extends PDFRect {

	Rect m_Rect;

	public PDFNetRect(Rect rect) {
		m_Rect = rect;
	}

	@Override
	public void set(Rectangle2D.Double rect) throws PDFLibException {
		try {
			m_Rect = new Rect(rect);
			set(m_Rect);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public void set(Object rect) throws PDFLibException {
		try {
			m_Rect = new Rect((Obj) rect);
			set(m_Rect);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	public void set(Rect rect) throws PDFNetException {
		x1 = m_Rect.getX1();
		x2 = m_Rect.getX2();
		y1 = m_Rect.getY1();
		y2 = m_Rect.getY2();
	}

	@Override
	public Double getRectangle() throws PDFLibException {
		try {
			return m_Rect.getRectangle();
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public Object getPDFRect() {
		return m_Rect;
	}
}
