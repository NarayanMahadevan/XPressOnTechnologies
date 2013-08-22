package com.rgpt.pdfnetlib;

import pdftron.Common.PDFNetException;
import pdftron.PDF.PDFDoc;
import pdftron.SDF.SDFDoc;

import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.PDFPage;
import com.rgpt.pdflib.ProgressInfoListener;

public class PDFNetDoc extends com.rgpt.pdflib.PDFDoc {

	private PDFDoc m_PDFNetDoc;

	public PDFNetDoc(String filePath) throws PDFLibException {
		try {
			m_PDFNetDoc = new PDFDoc(filePath);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	public PDFNetDoc(PDFDoc doc) {
		m_PDFNetDoc = doc;
	}

	/**
	 * The Constructor creates PDFDoc from Bytes and the whole of PDFDoc is in
	 * the memory
	 * 
	 * @param buf
	 *            which is the byte array to create PDF Doc
	 * @throws PDFLibException
	 */
	public PDFNetDoc(byte[] buf) throws PDFLibException {
		try {
			m_PDFNetDoc = new PDFDoc(buf);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public Object getPDFDoc() {
		return m_PDFNetDoc;
	}

	public boolean initSecurityHandler() throws PDFLibException {
		try {
			if (m_PDFNetDoc.initSecurityHandler())
				return true;
			else
				return false;
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	public boolean isAuthorizedToView(String password) throws PDFLibException {
		try {
			if (m_PDFNetDoc.initStdSecurityHandler(password))
				return true;
			else
				return false;
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public PDFPage getPDFPage(int pageNum) throws PDFLibException {
		try {
			return new PDFNetPage(m_PDFNetDoc.getPage(pageNum));
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	@Override
	public void save(String path, SaveOptions options,
			ProgressInfoListener progress) throws PDFLibException {
		try {
			m_PDFNetDoc.save(path, getFlag(options), null);
		} catch (PDFNetException ex) {
			throw PDFNetUtil.newPDFLibException(ex);
		}
	}

	private long getFlag(SaveOptions options) {
		switch (options) {
		case IncrementalSave:
			return SDFDoc.e_incremental;
		case FullSave:
			return 0;
		case SaveToRemoveUnusedElem:
			return SDFDoc.e_remove_unused;
		case QuickSave:
			return SDFDoc.e_linearized;
		default:
			return 0;
		}
	}

}
