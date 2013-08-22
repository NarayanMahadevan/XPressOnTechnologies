package com.rgpt.pdflib;

/**
 * 
 * This is exception class that encapsulates the exception thrown by PDF
 * Libraries
 * 
 * @author Narayan
 * 
 */

public class PDFLibException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * The conditional expression that caused the exception
	 */
	public String m_CondExpr;

	/**
	 * The filename indicating where the exception occurred.
	 */
	public String m_FileName;

	/**
	 * The line number indicating where the exception occurred.
	 */
	public long m_LineNumber;

	/**
	 * The function name where the exception occurred.
	 */
	public String m_Function;

	/**
	 * The Error Message
	 */
	public String message;

	public PDFLibException(String message) {
		this.m_CondExpr = "";
		this.m_FileName = "";
		this.m_Function = "";
		this.m_LineNumber = -1;
		this.message = message;
	}

	public PDFLibException(String condExpr, long lineNumber, String fileName,
			String function, String message) {
		this.m_CondExpr = condExpr;
		this.m_FileName = fileName;
		this.m_Function = function;
		this.m_LineNumber = lineNumber;
		this.message = message;
	}
}
