// RGPT PACKAGES
package com.rgpt.pdfnetlib;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Vector;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.Filters.Filter;
import pdftron.PDF.Font;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.PDFNet;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;
import pdftron.PDF.Rect;
import pdftron.SDF.DictIterator;
import pdftron.SDF.Obj;
import pdftron.SDF.SDFDoc;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.templateutil.PDFPageInfo;
import com.rgpt.templateutil.VDPFieldInfo;
import com.rgpt.templateutil.VDPImageFieldInfo;
import com.rgpt.templateutil.VDPTextFieldInfo;
import com.rgpt.util.FontStreamHolder;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.StaticFieldInfo;

public class VDPFieldsExtractor {
	private static VDPFieldsExtractor m_VDPFieldsExtractor;

	private VDPFieldsExtractor() {
	}

	public static VDPFieldsExtractor getInstance() {
		if (m_VDPFieldsExtractor == null)
			m_VDPFieldsExtractor = new VDPFieldsExtractor();
		return m_VDPFieldsExtractor;
	}

	//
	// This Method Traverses From the Page SDF Object to PieceInfo Dictionary to
	// RGPTPageVDP Object to Private Object to finally get to RGPTPageVDPFields
	// Dictinary. This
	// then internally calls the extractVDPFIelds method on the rgptVDPFldsObj
	// Dictinary
	// to populate the PDFPageInfo Object
	//
	public PDFPageInfo extractVDPFIelds(Page currPage, boolean usePrivateObj)
			throws PDFNetException, Exception {
		long objNum;
		StringBuffer mesg = new StringBuffer();
		mesg.append("\n---------EXTRACTING VDP FIELDS--------------\n\n");
		StringBuffer errMsg = new StringBuffer("\n\n------ERROR------\n\n");

		// Getting SDF Object. This is required to retrieve the Private Object
		// where RGPT VDP Elements are defined.
		Obj page_sdfobj = (Obj) currPage.getSDFObj();

		// Retrieving PieceInfo Dictionary from the Page
		DictIterator pieceinfo_itr = page_sdfobj.find("PieceInfo");
		if (!pieceinfo_itr.hasNext()) {
			errMsg.append("WRONG PDF FILE. THIS HAS NO PRIVATE OBJECTS DEFINED");
			errMsg.append("\nPLEASE MAKE SURE THE FILE IS GENERATED USING RGPT TEMPLATE VIEWER\n");
			throw new RuntimeException(errMsg.toString());
		}

		// Found PieceInfo Dictionary. Further drilling down to find RGPTPageVDP
		// Object
		mesg.append(" Found PieceInfo Dictionary.");
		// mesg.append(" Key: " + pieceinfo_itr.key().getName());
		Obj pieceinfo_dict_obj = (Obj) pieceinfo_itr.value();

		mesg.append("\n RETRIEVING RGPTPageVDP SDF Obj. ");
		// Retrieving ZViewer Private Object which holds the VDP Fields
		// defined using RGPT TEMPLATE VIEWER
		Obj RGPTPageVDP_obj = pieceinfo_dict_obj.findObj("RGPTPageVDP");
		if (RGPTPageVDP_obj == null) {
			errMsg.append("NO ZViewer OBJECTS DEFINED.");
			errMsg.append("\nPLEASE MAKE SURE TO SELECT VDP ELEMENTS ");
			errMsg.append("AND THE FILE IS GENERATED USING RGPT Template Maker\n");
			throw new RuntimeException(errMsg.toString());
		}

		// Found ZViewer Object. Find the next level Private Object
		objNum = RGPTPageVDP_obj.getObjNum();
		mesg.append("RGPTPageVDP Object Num: " + objNum);

		mesg.append("\n RETRIEVING Private SDF Obj. ");
		// Retriving the Private Object from ZViewer Object
		Obj prvt_obj = RGPTPageVDP_obj.findObj("Private");
		if (prvt_obj == null) {
			errMsg.append("NO PRIVATE OBJECTS DEFINED.");
			errMsg.append("\nPLEASE MAKE SURE TO SELECT VDP ELEMENTS ");
			errMsg.append("AND GENERATED THE FILE USING RGPT Template Maker\n");
			throw new RuntimeException(errMsg.toString());
		}

		// Found Private Object. Find the next level rgptVDPFldsObj Dictionary
		objNum = prvt_obj.getObjNum();
		mesg.append("Private Object Num: " + objNum);

		mesg.append("\n RETRIEVING RGPTPageVDPFields Dictionary. ");
		Obj rgptPageVDPFields_dict = prvt_obj.findObj("RGPTPageVDPFields");
		if (rgptPageVDPFields_dict == null) {
			errMsg.append("NO rgptVDPFldsObj DICTIONARY DEFINED.");
			errMsg.append("\nPLEASE MAKE SURE TO SELECT VDP ELEMENTS ");
			errMsg.append("AND GENERATED THE FILE USING RGPT TEMPLATE VIEWER\n");
			throw new RuntimeException(errMsg.toString());
		}

		// Found rgptVDPFldsObj Dictionary Object. Calling extractVDPFields
		// method is
		// called on rgptVDPFldsObj Dictionary to extract the metadata of VDP
		// Fields
		mesg.append(" FOUND RGPTPageVDPFields Dictionary");
		RGPTLogger.logToFile(mesg.toString());
		mesg.setLength(0);

		PDFPageInfo pdfPageInfo = null;
		if (usePrivateObj) {
			pdfPageInfo = new PDFPageInfo();
			pdfPageInfo.m_PageNum = currPage.getIndex();
			pdfPageInfo.m_PageWidth = new Double(currPage.getPageWidth())
					.intValue();
			pdfPageInfo.m_PageHeight = new Double(currPage.getPageHeight())
					.intValue();
			pdfPageInfo.m_PageRotation = currPage.getRotation();
			Matrix2D pgCTM = currPage.getDefaultMatrix(false, 0, 0);
			pdfPageInfo.m_PageCTM = (new PDFUtil()).getAffineMatrix(pgCTM);
			pdfPageInfo.m_OrigPageCTM = (new PDFUtil()).getAffineMatrix(pgCTM);
			mesg.append("PDF Page OrigCTM: "
					+ pdfPageInfo.m_OrigPageCTM.toString() + " Page CTM: "
					+ pdfPageInfo.m_PageCTM.toString());
			extractVDPFields(rgptPageVDPFields_dict, pdfPageInfo);
			RGPTLogger.logToFile("Printing FontStream: "
					+ VDPTextFieldInfo.m_FontStreamHolder);
		} else
			pdfPageInfo = this.extractVDPPage(rgptPageVDPFields_dict);

		mesg.append("\n\n FINISHED EXTRACTING VDP FIELDS FOR THE PAGE. \n");
		RGPTLogger.logToFile(mesg.toString());
		mesg.setLength(0);
		return pdfPageInfo;
	}

	private PDFPageInfo extractVDPPage(Obj rgptVDPFldsObj)
			throws PDFNetException, Exception {
		String errMsg;
		String key = "PDFPageInfo";
		Obj pdfPageObj = rgptVDPFldsObj.findObj(key);

		if (pdfPageObj == null) {
			errMsg = "PDFPageInfo IS UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}

		// Filter filter = pdfPageObj.getDecodedStream();
		Filter filter = pdfPageObj.getRawStream(true);
		RGPTLogger.logToFile("GET DECODE NAME " + filter.getDecodeName());
		RGPTLogger.logToFile("GET NAME " + filter.getName());
		RGPTLogger.logToFile("IS INOUT FILTER " + filter.isInputFilter());

		RGPTLogger.logToFile("RAW STREAM LENGTH "
				+ pdfPageObj.getRawStreamLength());
		RGPTLogger.logToFile("SIZE " + pdfPageObj.size());
		pdftron.Filters.FilterReader reader = new pdftron.Filters.FilterReader(
				filter);
		RGPTLogger.logToFile("FILTER READER. COUNT " + reader.count());

		byte[] pdfPageStream = null;
		if (filter.getName().equals("MemoryFilter")) {
			Filter decFilter = pdfPageObj.getDecodedStream();
			RGPTLogger.logToFile("GET MEM DECODE NAME "
					+ decFilter.getDecodeName());
			RGPTLogger.logToFile("GET MEM NAME " + decFilter.getName());
			// MemoryFilter memFilter = (MemoryFilter) decFilter;
			pdftron.Filters.FilterReader decReader = new pdftron.Filters.FilterReader(
					decFilter);
			pdfPageStream = read(decReader, 367112);// getRawStreamLength());
			// pdfPageStream = memFilter.getBuffer();
		} else
			pdfPageStream = read(reader, pdfPageObj.getRawStreamLength());// getRawStreamLength());
		RGPTLogger.logToFile("FILTER READER. PDFPage STREAM SIZE "
				+ pdfPageStream.length);

		PDFPageInfo pgInfo = null;
		try {
			ByteArrayInputStream byteIPStream = new ByteArrayInputStream(
					pdfPageStream);
			pgInfo = (PDFPageInfo) PDFPageInfo.load(new ObjectInputStream(
					byteIPStream));
		} catch (Exception ex) {
			RGPTLogger
					.logToFile("EXCEPTION WHILE LOADING PDF PAGE FROM SERIALIZED STREAM: "
							+ ex.getMessage());
			ex.printStackTrace();
		}
		RGPTLogger.logToFile("\n\nTESTING SERIALIZED MESG\n\n");
		RGPTLogger.logToFile(pgInfo.toString());
		return pgInfo;
	}

	//
	// This extracts the SDF Object from the rgptVDPFldsObj Dictionary. The Type
	// of Obj
	// inserted is either Image or Text. Accordingly pdfPageInfo is populated
	// with
	// Text and Image Objects. The Text and Image objects are both marked as VDP
	// Elements.
	//
	private void extractVDPFields(Obj rgptVDPFldsObj, PDFPageInfo pdfPageInfo)
			throws PDFNetException, Exception {
		String errMsg;
		// Selection Type which can be either Text (value 1) or Image (value 2)
		int seltype = 0;
		String type = "";
		StringBuffer mesg = new StringBuffer();
		mesg.append("\n  EXTRACTING VDP FIELDS FROM RGPTPageVDPFields\n");
		RGPTLogger.logToFile(mesg.toString());

		// VDP Field Information Object which can be either Image or Text.
		VDPFieldInfo vdpFieldInfo;

		// Retriving Objects in the rgptVDPFldsObj Dictionary
		DictIterator itr = rgptVDPFldsObj.getDictIterator();
		if (!itr.hasNext()) {
			errMsg = "NO OBJECTS FOUND IN rgptVDPFldsObj DICTIONARY.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		while (itr.hasNext()) {
			Obj field_key = (Obj) itr.key();
			mesg.append("\nENTERING rgptVDPFldsObj DICTIONARY ");
			RGPTLogger.logToFile(mesg.toString());

			Obj vdpFieldObj = (Obj) itr.value();
			mesg.append(": Value:" + vdpFieldObj.getObjNum());
			RGPTLogger.logToFile(mesg.toString());

			// Checking for the VDP Type either Text or Image
			Obj fldvalue = vdpFieldObj.findObj("Type");
			if (fldvalue == null) {
				errMsg = "VDP FIELD TYPE IS UNDEFINED.";
				throw new RuntimeException(createErrorMesg(errMsg));
			}

			type = fldvalue.getAsPDFText();
			mesg.append("\n   RETRIEVED VDP FIELD TYPE:" + type);
			RGPTLogger.logToFile(mesg.toString());
			if (type.equals("Text")) {
				seltype = 1;
				vdpFieldInfo = new VDPTextFieldInfo();
			} else if (type.equals("Image")) {
				seltype = 2;
				vdpFieldInfo = new VDPImageFieldInfo();
			} else if (type.equals("TextOnGraphics")) {
				seltype = 3;
				vdpFieldInfo = new VDPTextFieldInfo();
			} else {
				errMsg = "VDP FIELD TYPE IS NOT PROPERLY DEFINED";
				throw new RuntimeException(createErrorMesg(errMsg));
			}

			vdpFieldInfo.m_FieldType = type;

			RGPTLogger.logToFile(mesg.toString());
			mesg.setLength(0);

			this.extractCommonVDPFields(vdpFieldObj, vdpFieldInfo);
			RGPTLogger
					.logToFile("\n---FINISHED extractCommonVDPFields Method ----\n");

			// Getting the Values for VDP Text Field
			if (seltype == 1 || seltype == 3) {
				RGPTLogger.logToFile("CALLING extractVDPTextFields METHOD");
				VDPTextFieldInfo vdpTextFieldInfo = (VDPTextFieldInfo) vdpFieldInfo;
				this.extractVDPTextFields(vdpFieldObj, vdpTextFieldInfo);
				// pdfPageInfo.m_VDPTextFieldInfo.addElement(vdpTextFieldInfo);

				// Populating the necessary VDP Fieds
				if (vdpTextFieldInfo.m_IsFontEmbedded) {
					if (vdpTextFieldInfo.m_FontStreamHolder == null)
						vdpTextFieldInfo.m_FontStreamHolder = new HashMap();
					long sdfObjNum = vdpTextFieldInfo.m_SDFObjNum;
					Object fontStrObj = vdpTextFieldInfo.m_FontStreamHolder
							.get(sdfObjNum);
					if (fontStrObj == null) {
						byte[] fontStream;
						try {
							fontStream = getFontStream(rgptVDPFldsObj.getDoc(),
									sdfObjNum);
						} catch (PDFNetException ex) {
							RGPTLogger
									.logToFile("Exception while retriving Font Stream: "
											+ ex.getMessage());
							ex.printStackTrace();
							throw ex;
						}
						RGPTLogger.logToFile("Embedded Font Stram Length: "
								+ fontStream.length);
						FontStreamHolder fontStrHldr = null;
						fontStrHldr = new FontStreamHolder(sdfObjNum,
								fontStream, vdpTextFieldInfo.m_FontName);
						vdpTextFieldInfo.m_FontStreamHolder.put(sdfObjNum,
								fontStrHldr);
					}
				}

				// Checking the PDF Font Type to create appropriate JAVA Font
				RGPTLogger.logToFile("Testing Font Type: "
						+ vdpTextFieldInfo.m_FontType);
				if (vdpTextFieldInfo.m_FontType == Font.e_TrueType)
					vdpTextFieldInfo.m_IsTrueType = true;
				else if (vdpTextFieldInfo.m_FontType == Font.e_Type0)
					vdpTextFieldInfo.m_IsType0 = true;
				else if (vdpTextFieldInfo.m_FontType == Font.e_Type1) {
					vdpTextFieldInfo.m_IsType1 = true;
					RGPTLogger.logToFile("Testing Type1 Font: "
							+ vdpTextFieldInfo.m_IsType1);
				} else if (vdpTextFieldInfo.m_FontType == Font.e_Type3)
					vdpTextFieldInfo.m_IsType3 = true;
				pdfPageInfo.m_VDPTextFieldInfo.addElement(vdpTextFieldInfo);
			}

			// Getting the Values for VDP Image Field
			if (seltype == 2) {
				this.extractVDPImageFields(vdpFieldObj,
						(VDPImageFieldInfo) vdpFieldInfo);
				pdfPageInfo.m_VDPImageFieldInfo.addElement(vdpFieldInfo);
			}

			RGPTLogger
					.logToFile("  FINISHED EXTRACTING VDP FIELD INFORMATION FOR: ");
			itr.next();
		}
	}

	// Getting the Font Stream from the embedded stream FontSDFObjNum FontSDFObj
	private byte[] getFontStream(SDFDoc doc, long sdfObjNum)
			throws PDFNetException {
		RGPTLogger.logToFile("SDF Object Number: " + sdfObjNum);
		if (sdfObjNum == -1) {
			RGPTLogger.logToFile("No Font is Embedded");
			throw new RuntimeException("No Font is Embedded");
		}

		Obj obj = doc.getObj(sdfObjNum);
		Font pdfFont = new Font(obj);
		Obj fontObj = pdfFont.getEmbeddedFont();
		if (fontObj == null) {
			RGPTLogger.logToFile("Font Object is not Embedded");
			throw new RuntimeException("Font Object is not Embedded");
		}

		int count = getFontCount(fontObj);
		RGPTLogger.logToFile("TOTAL FONT FILE COUNT " + count);

		Filter filter = fontObj.getDecodedStream();
		RGPTLogger.logToFile("GET DECODE NAME " + filter.getDecodeName());
		RGPTLogger.logToFile("GET NAME " + filter.getName());
		RGPTLogger.logToFile("IS INOUT FILTER " + filter.isInputFilter());

		RGPTLogger.logToFile("RAW STREAM LENGTH "
				+ fontObj.getRawStreamLength());
		RGPTLogger.logToFile("SIZE " + fontObj.size());
		pdftron.Filters.FilterReader reader = new pdftron.Filters.FilterReader(
				filter);
		RGPTLogger.logToFile("FILTER READER. COUNT " + reader.count());

		reader = new pdftron.Filters.FilterReader(filter);

		byte[] fontStream = read(reader, count);// getRawStreamLength());
		// byte[] fontStream = reader.read(367112);// getRawStreamLength());
		RGPTLogger.logToFile("FILTER READER. FONT STREAM SIZE "
				+ fontStream.length);
		return fontStream;
		/*
		 * TESTING THE PDF Embedded Font String fontFile =
		 * createPDFFont(vdpTxtFieldInfo); if (fontFile != null) {
		 * pdftron.PDF.Font pdfFont = null; pdfFont =
		 * pdftron.PDF.Font.createTrueTypeFont(doc.getSDFDoc(), fontFile, true);
		 * RGPTLogger.logToFile("\n---PDF FONT OBJECT IS CREATED.------" +
		 * pdfFont.getName() + "\n"); }
		 */
	}

	private int getFontCount(Obj fontObj) throws PDFNetException {
		Filter filter = fontObj.getDecodedStream();
		pdftron.Filters.FilterReader reader = new pdftron.Filters.FilterReader(
				filter);
		int count = 0;
		while (reader.get() != -1)
			count++;
		reader.destroy();
		return count;
	}

	//
	// This method is to extract the common VDP Fields for all the Field Types
	//
	private void extractCommonVDPFields(Obj vdpFieldObj,
			VDPFieldInfo vdpFieldInfo) throws PDFNetException {
		String errMsg;
		Obj fldvalue;
		StringBuffer mesg = new StringBuffer();
		mesg.append("\n---------EXTRACTING COMMON VDP FIELDS--------------\n\n");

		// Page Coordinate. This program stores Rectangular coordinates at Page,
		// Screen and Canvas level.
		double x1, x2, y1, y2; // Rectangle coordinate Points

		// Retrieving the Page Rectangular Coordinates for this VDP Element SDF
		// Obj
		fldvalue = vdpFieldObj.findObj("RectPAGE");
		if (fldvalue == null) {
			errMsg = "PAGE RECTANGLE COORDINATES USING NAME RectPAGE UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}

		x1 = fldvalue.getAt(0).getNumber();
		y1 = fldvalue.getAt(1).getNumber();
		x2 = fldvalue.getAt(2).getNumber();
		y2 = fldvalue.getAt(3).getNumber();

		if (vdpFieldInfo.m_FieldType.equals("TextOnGraphics"))
			vdpFieldInfo.m_PageRectangle = new Rectangle2D.Double(x1, y2,
					Math.abs(x2 - x1), Math.abs(y2 - y1));
		else
			vdpFieldInfo.m_PageRectangle = new Rectangle2D.Double(x1, y1,
					Math.abs(x2 - x1), Math.abs(y2 - y1));
		// Field Name
		fldvalue = vdpFieldObj.findObj("Name");
		if (fldvalue == null) {
			errMsg = "VDP FIELD - Name UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FieldName = fldvalue.getAsPDFText();

		// Field Value
		fldvalue = vdpFieldObj.findObj("Value");
		if (fldvalue == null) {
			errMsg = "VDP FIELD - Value UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FieldValue = fldvalue.getAsPDFText();

		// Field Length
		fldvalue = vdpFieldObj.findObj("Length");
		if (fldvalue == null) {
			errMsg = "VDP FIELD - Length UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FieldLength = fldvalue.getAsPDFText();

		// Is this Field Pre-populated
		fldvalue = vdpFieldObj.findObj("IsVDPPrepopulated");
		if (fldvalue == null) {
			errMsg = "VDP FIELD - IsVDPPrepopulated UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_IsVDPPrepopulated = fldvalue.getBool();

		RGPTLogger.logToFile(vdpFieldInfo.toString(mesg));
		mesg.setLength(0);
	}

	//
	// This method is to extract the Text VDP Fields
	//
	private void extractVDPTextFields(Obj vdpFieldObj,
			VDPTextFieldInfo vdpFieldInfo) throws PDFNetException, Exception {
		String errMsg;
		Obj fldvalue;
		StringBuffer mesg = new StringBuffer();
		mesg.append("\n---------EXTRACTING TEXT VDP FIELDS--------------\n\n");
		RGPTLogger.logToFile(mesg.toString());

		// VDPTextMode - Indicates Word, Line or Para
		fldvalue = vdpFieldObj.findObj("VDPTextMode");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - VDPTextMode UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_VDPTextMode = (int) fldvalue.getNumber();
		RGPTLogger.logToFile("  VDPTextMode: " + vdpFieldInfo.m_VDPTextMode);

		// IsFieldOptional
		fldvalue = vdpFieldObj.findObj("IsFieldOptional");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - IsFieldOptional UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_IsFieldOptional = fldvalue.getBool();
		RGPTLogger.logToFile("IsFieldOptional: "
				+ vdpFieldInfo.m_IsFieldOptional);

		// NEW FIELD - UseTitleCase. This lets the user eneterd text to be in
		// Title
		// Case Oct 13, 2009. This is replaced by Text Formats

		// NEW FIELD - FieldLengthFixed. This lets the user eneter only upto the
		// Length specified Oct 13, 2009
		fldvalue = vdpFieldObj.findObj("FieldLengthFixed");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FieldLengthFixed UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FieldLengthFixed = fldvalue.getBool();
		RGPTLogger.logToFile("FieldLengthFixed: "
				+ vdpFieldInfo.m_FieldLengthFixed);

		// IsFieldFixed
		Object objectVal = this.getVDPFldValue(vdpFieldObj, "IsFieldFixed",
				"Bool", false, new Boolean(false));
		vdpFieldInfo.m_IsFieldFixed = ((Boolean) objectVal).booleanValue();

		// TextWidthFixed
		objectVal = this.getVDPFldValue(vdpFieldObj, "TextWidthFixed", "Bool",
				false, new Boolean(false));
		vdpFieldInfo.m_TextWidthFixed = ((Boolean) objectVal).booleanValue();

		// AutoFitText
		objectVal = this.getVDPFldValue(vdpFieldObj, "AutoFitText", "Bool",
				false, new Boolean(false));
		vdpFieldInfo.m_AutoFitText = ((Boolean) objectVal).booleanValue();

		// NEW FIELD for specifying the Data Type of Text Entered. This can be
		// Text
		// or Number Oct 13, 2009
		fldvalue = vdpFieldObj.findObj("TextDataType");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - TextDataType UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_TextDataType = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("TextDataType: " + vdpFieldInfo.m_TextDataType);

		// NEW FIELD for storing the Original Field Value. Jun 25 2010
		fldvalue = vdpFieldObj.findObj("OriginalValue");
		if (fldvalue != null) {
			vdpFieldInfo.m_OriginalValue = fldvalue.getAsPDFText();
			RGPTLogger.logToFile("OrigValue: " + vdpFieldInfo.m_OriginalValue);
		}

		// NEW FIELD for specifying the Predefined Text after which the user can
		// type. Oct 13, 2009, Dec 21, 2009 SuffixValue TextFormatName
		// TextFormatType TextFormatValue
		fldvalue = vdpFieldObj.findObj("PrefixValue");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - PrefixValue UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_PrefixValue = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("PrefixValue: " + vdpFieldInfo.m_PrefixValue);

		fldvalue = vdpFieldObj.findObj("SuffixValue");
		if (fldvalue != null)
			vdpFieldInfo.m_SuffixValue = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("SuffixValue: " + vdpFieldInfo.m_SuffixValue);

		fldvalue = vdpFieldObj.findObj("TextFormatName");
		if (fldvalue != null)
			vdpFieldInfo.m_TextFormatName = fldvalue.getAsPDFText();
		RGPTLogger
				.logToFile("TextFormatName: " + vdpFieldInfo.m_TextFormatName);

		fldvalue = vdpFieldObj.findObj("TextFormatType");
		if (fldvalue != null)
			vdpFieldInfo.m_TextFormatType = (int) fldvalue.getNumber();
		RGPTLogger
				.logToFile("TextFormatType: " + vdpFieldInfo.m_TextFormatType);

		fldvalue = vdpFieldObj.findObj("TextFormatValue");
		if (fldvalue != null)
			vdpFieldInfo.m_TextFormatValue = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("TextFormatValue: "
				+ vdpFieldInfo.m_TextFormatValue);

		fldvalue = vdpFieldObj.findObj("AlternateVDPField");
		if (fldvalue != null)
			vdpFieldInfo.m_AlternateVDPField = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("AlternateVDPField: "
				+ vdpFieldInfo.m_AlternateVDPField);

		fldvalue = vdpFieldObj.findObj("SequenceId");
		if (fldvalue != null)
			vdpFieldInfo.m_SequenceId = (int) fldvalue.getNumber();
		RGPTLogger.logToFile("SequenceId: " + vdpFieldInfo.m_SequenceId);

		// New Field Jan 26 2010. This field is predominantly used when VDP Text
		// Mode
		// is Word. This is used to capture multiple words in a line.
		fldvalue = vdpFieldObj.findObj("LineBBox");
		if (fldvalue != null) {
			double x1 = fldvalue.getAt(0).getNumber();
			double y1 = fldvalue.getAt(1).getNumber();
			double x2 = fldvalue.getAt(2).getNumber();
			double y2 = fldvalue.getAt(3).getNumber();
			Rect rect = new Rect(x1, y1, x2, y2);
			vdpFieldInfo.m_LineBBox = RGPTRectangle.getReactangle(rect
					.getRectangle());
		}

		fldvalue = vdpFieldObj.findObj("NewBBox");
		if (fldvalue != null) {
			double x1 = fldvalue.getAt(0).getNumber();
			double y1 = fldvalue.getAt(1).getNumber();
			double x2 = fldvalue.getAt(2).getNumber();
			double y2 = fldvalue.getAt(3).getNumber();
			Rect rect = new Rect(x1, y1, x2, y2);
			vdpFieldInfo.m_NewBBox = RGPTRectangle.getReactangle(rect
					.getRectangle());
		}

		// FillColor
		System.out.println("Entering text color / font attributes");
		fldvalue = vdpFieldObj.findObj("FillColor");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FillColor UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		double fillColor = fldvalue.getNumber();
		vdpFieldInfo.m_FillColor = new Double(fillColor).intValue();
		RGPTLogger.logToFile("  FillColor: " + vdpFieldInfo.m_FillColor);

		// Horizontal Text Allignment
		fldvalue = vdpFieldObj.findObj("TextAllignment");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - TextAllignment UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_TextAllignment = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("  TextAllignment: "
				+ vdpFieldInfo.m_TextAllignment);

		// FontName
		fldvalue = vdpFieldObj.findObj("FontName");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FontName UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FontName = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("  FontName: " + vdpFieldInfo.m_FontName);

		// FontSize
		fldvalue = vdpFieldObj.findObj("FontSize");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FontSize UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		RGPTLogger.logToFile("  String FontSize: " + fldvalue.getAsPDFText());
		vdpFieldInfo.m_FontSize = new Double(fldvalue.getAsPDFText())
				.intValue();
		RGPTLogger.logToFile("  FontSize: " + vdpFieldInfo.m_FontSize);

		// FontWeight
		fldvalue = vdpFieldObj.findObj("FontWeight");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FontWeight UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FontWeight = (int) fldvalue.getNumber();
		RGPTLogger.logToFile("  FontWeight: " + vdpFieldInfo.m_FontWeight);

		// FontFamily
		fldvalue = vdpFieldObj.findObj("FontFamily");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FontFamily UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FontFamily = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("  FontFamily: " + vdpFieldInfo.m_FontFamily);

		// FontType
		fldvalue = vdpFieldObj.findObj("FontType");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FontType UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		double fontType = fldvalue.getNumber();
		vdpFieldInfo.m_FontType = new Double(fontType).intValue();
		RGPTLogger.logToFile("  FontType: " + vdpFieldInfo.m_FontType);

		// FontFlags
		fldvalue = vdpFieldObj.findObj("FontFlags");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FontFlags UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FontFlags = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("  FontFlags: " + vdpFieldInfo.m_FontFlags);

		// FontBBox
		fldvalue = vdpFieldObj.findObj("FontBBox");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - FontBBox UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_FontBBox = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("  FontBBox: " + vdpFieldInfo.m_FontBBox);

		// NEW FIELDS

		// FontSDFObjNum. This is set for Embedded Fonts
		fldvalue = vdpFieldObj.findObj("FontSDFObjNum");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - Font SDF Object Num UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_SDFObjNum = new Double(fldvalue.getNumber()).longValue();
		RGPTLogger.logToFile("  Font SDF Obj Num: " + vdpFieldInfo.m_SDFObjNum);

		// The attribute is used to check the PDF Fonts
		fldvalue = vdpFieldObj.findObj("StdType1FontType");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - Font SDF Object Num UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_StdType1Font = new Double(fldvalue.getNumber())
				.intValue();
		RGPTLogger.logToFile("Std Type Font : " + vdpFieldInfo.m_StdType1Font);

		// Embedded Font Name
		fldvalue = vdpFieldObj.findObj("EmbFontName");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - EmbFontName UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_EmbFontName = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("  EmbFontName: " + vdpFieldInfo.m_EmbFontName);

		// Base Font Name
		fldvalue = vdpFieldObj.findObj("BaseFontName");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - BaseFontName UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_BaseFontName = fldvalue.getAsPDFText();
		RGPTLogger.logToFile("  BaseFontName: " + vdpFieldInfo.m_BaseFontName);

		// If the Font used is Embedded Font then the SDF Object Number is used
		// to
		// retrieve the Font from the PDF
		// The Fonts are either embedded into PDF or the PDF Standard Fonts
		// cases
		// are currently supported. No Server Fonts in current implementation
		// So either the case will be useFontFile or usePDFStdFont

		// IsFontEmbedded
		fldvalue = vdpFieldObj.findObj("IsFontEmbedded");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - IsFontEmbedded UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_IsFontEmbedded = fldvalue.getBool();
		RGPTLogger.logToFile("  IsFontEmbedded: "
				+ vdpFieldInfo.m_IsFontEmbedded);

		// UsePDFStandardFont
		fldvalue = vdpFieldObj.findObj("UsePDFStandardFont");
		if (fldvalue == null) {
			errMsg = "VDP TEXT FIELD - UsePDFStandardFont UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_UsePDFStdFont = fldvalue.getBool();
		RGPTLogger.logToFile("  UsePDFStandardFont: "
				+ vdpFieldInfo.m_UsePDFStdFont);

		Object objVal = null;
		if (vdpFieldInfo.m_FieldType.equals("TextOnGraphics")) {
			// Vertical Text Allignment
			objVal = this.getVDPFldValue(vdpFieldObj, "VerticalTextAllignment",
					"Text", true);
			vdpFieldInfo.m_VertTextAllignment = (String) objVal;
			// Rotation Angle
			objVal = this.getVDPFldValue(vdpFieldObj, "Rotation", "Number",
					true);
			vdpFieldInfo.m_RotationAngle = ((Double) objVal).doubleValue();
			// ShearX
			objVal = this.getVDPFldValue(vdpFieldObj, "ShearX", "Number", true);
			vdpFieldInfo.m_ShearX = ((Double) objVal).doubleValue();
			// ShearY
			objVal = this.getVDPFldValue(vdpFieldObj, "ShearY", "Number", true);
			vdpFieldInfo.m_ShearY = ((Double) objVal).doubleValue();
			// ShapeType
			objVal = this.getVDPFldValue(vdpFieldObj, "ShapeType", "Number",
					true);
			vdpFieldInfo.m_ShapeType = ((Double) objVal).intValue();
			// DrawTextOutline
			objVal = this.getVDPFldValue(vdpFieldObj, "DrawTextOutline",
					"Bool", true);
			vdpFieldInfo.m_DrawTextOutline = ((Boolean) objVal).booleanValue();
			// GraphicPathPoints
			String key = "GraphicPathPoints";
			Vector<Point2D.Double> shPts = null;
			shPts = (Vector<Point2D.Double>) this.extractSerializedObject(key,
					vdpFieldObj);
			vdpFieldInfo.m_GraphicPathPoints = shPts;

			// AdjustTextX
			objVal = this.getVDPFldValue(vdpFieldObj, "AdjustTextX", "Number",
					true);
			vdpFieldInfo.m_AdjustTextX = ((Double) objVal).doubleValue();
			// AdjustTextY
			objVal = this.getVDPFldValue(vdpFieldObj, "AdjustTextY", "Number",
					true);
			vdpFieldInfo.m_AdjustTextY = ((Double) objVal).doubleValue();
			// FillShapeLogic
			objVal = this.getVDPFldValue(vdpFieldObj, "FillShapeLogic", "Text",
					true);
			vdpFieldInfo.m_FillShapeLogic = (String) objVal;
			// FillShapeColor
			objVal = this.getVDPFldValue(vdpFieldObj, "FillShapeColor",
					"Number", true);
			vdpFieldInfo.m_FillShapeColor = ((Double) objVal).intValue();
			// FillTransperancy
			objVal = this.getVDPFldValue(vdpFieldObj, "FillTransperancy",
					"Number", true);
			vdpFieldInfo.m_FillTransperancy = ((Double) objVal).intValue();

			// ClipImageHolder
			key = "ClipImageHolder";
			ImageHolder imgHldr = null;
			imgHldr = (ImageHolder) this.extractSerializedObject(key,
					vdpFieldObj, false);
			RGPTLogger.logToFile("Extracted Clip Img: " + imgHldr);
			vdpFieldInfo.m_ClipImageHolder = imgHldr;

			// FillShapeImage
			key = "FillShapeImage";
			imgHldr = (ImageHolder) this.extractSerializedObject(key,
					vdpFieldObj, false);
			vdpFieldInfo.m_FillShapeImage = imgHldr;
		}

		// Populating private VDP Text Field for the VDP Text Mode is WORD or
		// LINE
		if (vdpFieldInfo.m_VDPTextMode == StaticFieldInfo.WORD
				|| vdpFieldInfo.m_VDPTextMode == StaticFieldInfo.LINE) {
			// New Fields for Overflow Text
			objVal = this.getVDPFldValue(vdpFieldObj, "IsOverFlowField",
					"Bool", false, new Boolean(false));
			vdpFieldInfo.m_IsOverFlowField = ((Boolean) objVal).booleanValue();
			objVal = this.getVDPFldValue(vdpFieldObj, "OverFlowVDPField",
					"Text", false, new String(""));
			vdpFieldInfo.m_OverFlowVDPField = (String) objVal;

			// StartX
			fldvalue = vdpFieldObj.findObj("StartX");
			if (fldvalue == null) {
				errMsg = "VDP TEXT FIELD - StartX UNDEFINED.";
				throw new RuntimeException(createErrorMesg(errMsg));
			}
			vdpFieldInfo.m_StartX = fldvalue.getNumber();
			RGPTLogger.logToFile("  StartX: " + vdpFieldInfo.m_StartX);

			// StartY
			fldvalue = vdpFieldObj.findObj("StartY");
			if (fldvalue == null) {
				errMsg = "VDP TEXT FIELD - StartY UNDEFINED.";
				throw new RuntimeException(createErrorMesg(errMsg));
			}
			vdpFieldInfo.m_StartY = fldvalue.getNumber();
			RGPTLogger.logToFile("  StartY: " + vdpFieldInfo.m_StartY);

			// Printing the Populated Values
			RGPTLogger.logToFile(vdpFieldInfo.toString(mesg));
			mesg.setLength(0);
			return;
		}

		if (vdpFieldInfo.m_VDPTextMode != StaticFieldInfo.PARA)
			throw new RuntimeException("VDP Text Mode is not properly defined.");
		// Populating private VDP Text Field for the VDP Text Mode is PARA
		String key = "LineAttributes";
		Vector lineAttrList = (Vector) this.extractSerializedObject(key,
				vdpFieldObj);
		vdpFieldInfo.m_VDPLineAttrList = lineAttrList;

		// In Paragraph VDP Mode Resetting RewriteLineSel to False.
		HashMap lineAttrs = null;
		for (int i = 0; i < vdpFieldInfo.m_VDPLineAttrList.size(); i++) {
			lineAttrs = (HashMap) vdpFieldInfo.m_VDPLineAttrList.elementAt(i);
			lineAttrs.put("RewriteLineSel", false);
		}
	}

	private Object getVDPFldValue(Obj vdpFieldObj, String fldName,
			String fldType, boolean raiseExp) throws Exception {
		return this.getVDPFldValue(vdpFieldObj, fldName, fldType, raiseExp,
				null);
	}

	private Object getVDPFldValue(Obj vdpFieldObj, String fldName,
			String fldType, boolean raiseExp, Object defVal) throws Exception {
		Obj fldvalue = vdpFieldObj.findObj(fldName);
		RGPTLogger.logToFile(fldName + " : " + fldvalue);
		if (fldvalue == null) {
			if (raiseExp) {
				String errMsg = "VDP TEXT FIELD - " + fldName + " UNDEFINED.";
				throw new RuntimeException(createErrorMesg(errMsg));
			} else
				return defVal;
		}
		if (fldType.equals("Text"))
			return fldvalue.getAsPDFText();
		if (fldType.equals("Number"))
			return Double.valueOf(fldvalue.getNumber());
		if (fldType.equals("Bool"))
			return Boolean.valueOf(fldvalue.getBool());
		return null;
	}

	private Object extractSerializedObject(String key, Obj rgptVDPFldsObj)
			throws PDFNetException, Exception {
		return extractSerializedObject(key, rgptVDPFldsObj, true);
	}

	private Object extractSerializedObject(String key, Obj rgptVDPFldsObj,
			boolean raiseExp) throws PDFNetException, Exception {
		String errMsg;
		Obj serObj = rgptVDPFldsObj.findObj(key);

		if (serObj == null) {
			if (!raiseExp)
				return null;
			errMsg = key = " ARE UNDEFINED.";
			throw new RuntimeException(errMsg);
		}

		// Filter filter = pdfPageObj.getDecodedStream();
		Filter filter = serObj.getRawStream(true);

		RGPTLogger
				.logToFile("RAW STREAM LENGTH " + serObj.getRawStreamLength());
		pdftron.Filters.FilterReader reader = new pdftron.Filters.FilterReader(
				filter);

		byte[] serStream = null;
		serStream = read(reader, serObj.getRawStreamLength());
		RGPTLogger.logToFile("RAW STREAM SIZE " + serStream.length);

		ByteArrayInputStream byteIPStream = new ByteArrayInputStream(serStream);
		ObjectInputStream objstream = new ObjectInputStream(byteIPStream);
		return objstream.readObject();
	}

	//
	// This method is to extract the Image VDP Fields
	//
	private void extractVDPImageFields(Obj vdpFieldObj,
			VDPImageFieldInfo vdpFieldInfo) throws PDFNetException {
		String errMsg;
		Obj fldvalue;

		StringBuffer mesg = new StringBuffer();
		mesg.append("\n---------EXTRACTING IMAGE VDP FIELDS--------------\n\n");

		// ImageWidth
		fldvalue = vdpFieldObj.findObj("ImageWidth");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - ImageWidth UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_ImageWidth = fldvalue.getNumber();

		// ImageHeight
		fldvalue = vdpFieldObj.findObj("ImageHeight");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - ImageHeight UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_ImageHeight = fldvalue.getNumber();

		// ColorComponents
		fldvalue = vdpFieldObj.findObj("ColorComponents");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - ColorComponents UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_ColorComponents = new Double(fldvalue.getNumber())
				.intValue();

		// RenderingIntent
		fldvalue = vdpFieldObj.findObj("RenderingIntent");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - RenderingIntent UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_RenderingIntent = new Double(fldvalue.getNumber())
				.intValue();

		// BitsPerComponent
		fldvalue = vdpFieldObj.findObj("BitsPerComponent");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - BitsPerComponent UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_BitsPerComponent = new Double(fldvalue.getNumber())
				.intValue();

		// BitsPerComponent
		fldvalue = vdpFieldObj.findObj("BitsPerComponent");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - BitsPerComponent UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_BitsPerComponent = new Double(fldvalue.getNumber())
				.intValue();

		// To Check if this Image is set as Background
		fldvalue = vdpFieldObj.findObj("IsBackgoundImage");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - IsBackgoundImage UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_IsBackgroundImage = fldvalue.getBool();
		RGPTLogger.logToFile("Is Background Image: "
				+ vdpFieldInfo.m_IsBackgroundImage);

		// NEW FIELDS for setting the Theme and if Theme Images are used, can
		// the
		// user still upload Images. Added as of Sept 13, 2009
		// ThemeId
		fldvalue = vdpFieldObj.findObj("ThemeId");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - ThemeId UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_ThemeId = new Double(fldvalue.getNumber()).intValue();

		// AllowUploadWithTheme
		fldvalue = vdpFieldObj.findObj("AllowUploadWithTheme");
		if (fldvalue == null) {
			errMsg = "VDP IMAGE FIELD - AllowUploadWithTheme UNDEFINED.";
			throw new RuntimeException(createErrorMesg(errMsg));
		}
		vdpFieldInfo.m_AllowUploadWithTheme = fldvalue.getBool();

		// New Field for Picture Frame - 19th Feb 2010
		fldvalue = vdpFieldObj.findObj("UsePictureFrame");
		boolean usePictFrame = false;
		if (fldvalue != null && fldvalue.getBool()) {
			try {
				vdpFieldInfo.m_ShowPictureFrame = fldvalue.getBool();
				String key = "PictureFrame";
				ImageHolder imgHldr = null;
				imgHldr = (ImageHolder) this.extractSerializedObject(key,
						vdpFieldObj);
				RGPTLogger.logToFile("Picture Frame : " + imgHldr.toString());
				vdpFieldInfo.m_PictureFrameHolder = imgHldr;
				fldvalue = vdpFieldObj.findObj("OpaquePictureFrame");
				if (fldvalue == null)
					vdpFieldInfo.m_IsOpaquePictureFrame = false;
				vdpFieldInfo.m_IsOpaquePictureFrame = fldvalue.getBool();
				usePictFrame = true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!usePictFrame) {
			vdpFieldInfo.m_ShowPictureFrame = false;
			vdpFieldInfo.m_IsOpaquePictureFrame = false;
			vdpFieldInfo.m_PictureFrameHolder = null;
		}

		System.out.println("Exiting Image attributes");
		RGPTLogger.logToFile(vdpFieldInfo.toString(mesg));
		mesg.setLength(0);
	}

	private String createErrorMesg(String errorMsg) {
		StringBuffer errMsg = new StringBuffer("\n\n------ERROR------\n\n");
		errMsg.append(errorMsg);
		errMsg.append("\nPLEASE MAKE SURE TO SELECT VDP ELEMENTS ");
		errMsg.append("AND GENERATED THE FILE USING RGPT TEMPLATE VIEWER\n");
		return errMsg.toString();
	}

	//
	// Methods to support Backward Compatibility and current PDFNet Library
	//

	private byte[] read(pdftron.Filters.FilterReader reader, long length)
			throws PDFNetException {
		// return decReader.read(367112);
		byte[] pdfPageStream = new byte[(int) length];
		long numOfBytes = reader.read(pdfPageStream);
		RGPTLogger.logDebugMesg("Number of Bytes Read: " + numOfBytes);
		return pdfPageStream;
	}

	public static void main(String[] args) {
		// Initializes PDFNet library and called once, during process
		// initialization
		// PDFNet.initialize("ZESTA Technology Group (zestatech.com):CPU:1:E:W:AMC(20090613):9DF49411553D4A21E3C846109C92AA132F028A5A800B264C050B76F0FA");
		PDFNet.initialize(PDFUtil.PDFNET_LICENSE);
		// Sets the location of PDFNet resource file to process PDF documents
		// that
		// use CJKV encodings or standard fonts.
		PDFNet.setResourcesPath(args[0]);

		// The path to the folder containing test files and the output path to
		// dump the
		// modified PDF if any.
		String input_path = args[1];
		String output_path = args[2];
		// Input PDF File to be processed.
		String input_file_path = input_path + args[3];

		// PDFNet.initialize();
		// PDFNet.setResourcesPath("./resources");

		try {
			// Open an existing PDF document using the file path
			PDFDoc doc = new PDFDoc(input_file_path);
			doc.initSecurityHandler();

			VDPFieldsExtractor vdpFieldExt = new VDPFieldsExtractor();
			int pgnum = doc.getPageCount();
			PageIterator page_begin = doc.getPageIterator();
			PageIterator itr;
			PDFPageInfo pdfPageInfo;

			// Read every page
			for (itr = page_begin; itr.hasNext();) {
				Page currPage = (Page) itr.next();
				pdfPageInfo = new PDFPageInfo();
				pdfPageInfo.m_PageNum = currPage.getIndex();
				pdfPageInfo.m_PageWidth = new Double(currPage.getPageWidth())
						.intValue();
				pdfPageInfo.m_PageHeight = new Double(currPage.getPageHeight())
						.intValue();
				pdfPageInfo = vdpFieldExt.extractVDPFIelds(currPage, true);
				// System.out.println(pdfPageInfo.toString());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		PDFNet.terminate();
	}

}
