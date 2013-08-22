package com.rgpt.pdflib;

//---------------------------------------------------------------------------------------
//Copyright (c) 2013 by XPressOn Technologies Inc. All Rights Reserved.
//Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

import java.util.Map;

import com.rgpt.pdflib.PDFPage.PageData;
import com.rgpt.pdfnetlib.PDFElemSelController;
import com.rgpt.pdfnetlib.PDFNetDoc;
import com.rgpt.pdfnetlib.PDFNetUtil;
import com.rgpt.pdfnetlib.PDFNetViewController;
import com.rgpt.pdfnetlib.PDFThumbview;

/**
 * RGPT Library
 * 
 * This is the Factory Object to create Adapter Objected for the corresponding
 * Original PDF Library like PDFNet Library from PDFTron
 * 
 * @author Narayan
 * 
 */

public class PDFLibConnector {

	/**
	 * 
	 * PDFLib enum specifies the supported PDF Libraries in RGPT
	 * 
	 */
	public enum PDFLib {
		PDFNET
	}

	// Selected PDF Library
	private static PDFLib m_SelectedPDFLib;

	private PDFViewHandler m_PDFViewHandler;
	private PDFThumbViewHandler m_PDFThumbViewHandler;
	private static PDFLibConnector m_PDFLibFactory;

	private PDFLibConnector(PDFLib selPDFLib) {
		m_SelectedPDFLib = selPDFLib;
	}

	public static PDFLibConnector getInstance() {
		return getInstance(PDFLib.PDFNET);
	}

	public static PDFLibConnector getInstance(PDFLib selPDFLib) {
		if (m_PDFLibFactory == null)
			m_PDFLibFactory = new PDFLibConnector(selPDFLib);
		return m_PDFLibFactory;
	}

	public void initPDFLib() {
		switch (m_SelectedPDFLib) {
		case PDFNET:
			PDFNetUtil.initPDFLib();
			break;
		}
	}

	public void terminatePDFLib() {
		switch (m_SelectedPDFLib) {
		case PDFNET:
			PDFNetUtil.terminatePDFLib();
			break;
		}
	}

	public PDFViewHandler getPDFViewHandler() {
		if (m_PDFViewHandler != null)
			return m_PDFViewHandler;
		switch (m_SelectedPDFLib) {
		case PDFNET:
			m_PDFViewHandler = new PDFNetViewController();
			break;
		}
		return m_PDFViewHandler;
	}

	public PDFThumbViewHandler getPDFThumbViewHandler(int thumbNailWt) {
		if (m_PDFThumbViewHandler != null)
			return m_PDFThumbViewHandler;
		switch (m_SelectedPDFLib) {
		case PDFNET:
			m_PDFThumbViewHandler = new PDFThumbview(thumbNailWt);
			break;
		}
		return m_PDFThumbViewHandler;
	}

	/**
	 * Creates PDF Doc from the file path. If readInMemory is set to true, the
	 * complete PDF is read into memory. This is set when the pdf needs to be
	 * manipulated
	 * 
	 * @param filePath
	 * @param readInMemory
	 *            if true then the complete document is read into memory
	 * @return PDFDoc object
	 * @throws PDFLibException
	 */
	public PDFDoc createPDFDoc(String filePath, boolean readInMemory)
			throws PDFLibException {
		switch (m_SelectedPDFLib) {
		case PDFNET:
			if (readInMemory)
				return new PDFNetDoc(PDFNetUtil.getPDFBuffer(filePath));
			else
				return new PDFNetDoc(filePath);
		}
		return null;
	}

	/**
	 * Creates Element Selection Handler for every PDF Page in the Document
	 * 
	 * @param devCTM
	 *            Device transformation matrix for a page
	 * @param pageData
	 *            Page Data to scan elements in the Page
	 * @return PDFElemSelHandler to select the element clicked on the PDF Page
	 */
	public PDFElemSelHandler createPDFElemSelHandler(PDFMatrix2D devCTM,
			Map<PageData, Object> pageData) {
		switch (m_SelectedPDFLib) {
		case PDFNET:
			return new PDFElemSelController(m_PDFViewHandler, devCTM, pageData);
		}
		return null;
	}

	public void closePDFLibHandlers() {
		m_PDFViewHandler = null;
		m_PDFThumbViewHandler = null;
	}

	public void destroyPDFLibHandlers() {
		if (m_PDFViewHandler != null)
			m_PDFViewHandler.destroy();
		if (m_PDFThumbViewHandler != null)
			m_PDFThumbViewHandler.close();
		m_PDFViewHandler = null;
		m_PDFThumbViewHandler = null;
	}
}
