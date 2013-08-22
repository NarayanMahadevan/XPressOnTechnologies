// RGPT PACKAGES
package com.rgpt.pdfnetlib;

import pdftron.Common.PDFNetException;
//import pdftron.Filters.FilterWriter;
//import pdftron.Filters.StdFile;
import pdftron.PDF.Font;
import pdftron.PDF.GState;

public class PDFGStateHolder {

	public Font m_Font;
	public String m_FontName;
	public long m_SDFFontObjNum;

	public PDFGStateHolder(GState gs) throws PDFNetException {
		m_Font = gs.getFont();
		m_FontName = m_Font.getName();
		m_SDFFontObjNum = m_Font.GetSDFObj().getObjNum();

	}
}
