package com.rgpt.pdfnetlib;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;

import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.PDFMatrix2D;

public class PDFNetMatrix2D extends PDFMatrix2D {

	public Matrix2D m_Matrix2D;

	public PDFNetMatrix2D(Matrix2D mtx2D) {
		m_Matrix2D = mtx2D;
	}

	@Override
	public Object getPDFMatrix2D() {
		return m_Matrix2D;
	}

	@Override
	public PDFMatrix2D multiply(PDFMatrix2D mtx) throws PDFLibException {
		try {
			return new PDFNetMatrix2D(m_Matrix2D.multiply((Matrix2D) mtx
					.getPDFMatrix2D()));
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public String toString() {
		try {
			return PDFNetUtil.getAffineMatrix(m_Matrix2D).toString();
		} catch (PDFNetException ex) {
			return "";
		}
	}

}
