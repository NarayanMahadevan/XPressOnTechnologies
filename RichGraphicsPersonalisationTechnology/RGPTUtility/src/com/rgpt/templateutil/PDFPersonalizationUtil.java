// RGPT PACKAGES
package com.rgpt.templateutil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Hashtable;
// Java Holder Objects
import java.util.Map;
import java.util.Vector;

import javax.swing.JTextArea;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImagePersonalizationEngine;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.serverhandler.ImageHandlerInterface;
import com.rgpt.serverhandler.PDFPageHandler;
import com.rgpt.serverhandler.PDFViewInterface;
import com.rgpt.serverhandler.PersonalizedPDFFormRequest;
import com.rgpt.serverhandler.ServerResponse;
import com.rgpt.util.ClipPath;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.StaticFieldInfo;
import com.rgpt.util.TextFormatter;

// Font Classes

public class PDFPersonalizationUtil {
	public boolean m_EditImageArea = false;

	// This will be indicated
	public final static int EDIT_MODE = 0;

	// This will be indicated by Preview Button
	public final static int JAVA_PREVIEW_MODE = 1;

	// This mode will be indicated by View PDF in PC and Save PDF in Web
	public final static int PDF_PREVIEW_MODE = 2;

	// This mode will be indicated when user wants to resize or reposition text
	public final static int RESIZE_MODE = 3;

	// This mode will be indicated when user wants to resize or reposition text
	public final static int MOVE_MODE = 4;

	// This mode will be indicated when user wants to resize or reposition text
	public final static int RESIZE_MOVE_MODE = 5;

	// This mode will be indicated when user wants to allign or deleted VDP
	// Field
	public final static int SELECT_MODE = 6;

	// This mode will be indicated when user has selected Image to be dragged
	public final static int SELECT_IMAGE_MODE = 7;

	// This mode will be indicated when user has selected Image to be dragged
	public final static int SELECT_CROP_IMAGE = 8;

	// This mode will be indicated when user has selected Image to be dragged
	public final static int SHOW_FULL_IMAGE = 9;

	// This mode will be indicated when user has selected individual Field to be
	// resized
	public final static int RESIZE_MOVE_FIELD_MODE = 10;

	public PDFPersonalizationUtil() {
	}

	public PDFPersonalizationUtil(boolean editImageArea) {
		m_EditImageArea = editImageArea;
	}

	// This function generates filename for Bitmap Images for different Quality
	// Mode
	// and Enlarged or Thumbview size images. Enlarged or Thumbview is
	// identified
	// based on Quality Mode. This is because for Thumbview size of the image is
	// fixed.
	public String getPDFPageFileName(int qualityMode, int pageNum) {
		return this.getPDFPageFileName(qualityMode, pageNum, -1, ".png");
	}

	public String getPDFPageFileName(int qualityMode, int pageNum, int id,
			String imgExt) {
		String imgSize = "Enlarged";
		if (qualityMode == -1)
			imgSize = "ThumbView";
		StringBuffer fileName = new StringBuffer();
		fileName.append("Page_" + imgSize);
		if (qualityMode != -1)
			fileName.append("_" + qualityMode);
		fileName.append("_" + pageNum);
		if (id != -1)
			fileName.append("_" + id);
		fileName.append(imgExt);
		return fileName.toString();
	}

	public int getPDFPageQuality(double wd, double ht, boolean retQualMode) {
		if (wd < 8.0 && ht < 8.0) {
			if (retQualMode)
				return PDFViewInterface.REGULAR_QUALITY_PDF;
			return PDFViewInterface.REGULAR_QUALITY_DPI;
		}
		if (wd > 15.0 && ht > 8.0) {
			if (retQualMode)
				return PDFViewInterface.PAGE_QUALITY_PDF;
			return PDFViewInterface.PAGE_QUALITY_DPI;
		}
		if ((wd > 8.0 && wd < 15.0) && ht > 8.0) {
			if (retQualMode)
				return PDFViewInterface.MEDIUM_QUALITY_PDF;
			return PDFViewInterface.MEDIUM_QUALITY_DPI;
		}
		if (retQualMode)
			return PDFViewInterface.MEDIUM_QUALITY_PDF;
		return PDFViewInterface.MEDIUM_QUALITY_DPI;
	}

	/*
	 * This Method is used to Create Persdonalized PDF for a Page. This method
	 * only manipulates the Image and not the original PDF.
	 */
	public ServerResponse createPersonalizedPDFPage(
			PersonalizedPDFFormRequest vdpReq) {
		boolean isSuccess = false;
		BufferedImage persPageImg = null;
		Vector<Integer> pages = vdpReq.m_Pages;
		TemplateInfo templateInfo = vdpReq.m_TemplateInfo;
		Vector pdfPageInfoList = null;
		try {
			// Retrieving PDFPageInfo for all Pages
			pdfPageInfoList = this.getAllPDFPageInfo(pages, templateInfo);
			System.out.println("The Number of Pages to Personalize: "
					+ pdfPageInfoList.size());
			RGPTLogger.logToFile("PDF Page Info : "
					+ pdfPageInfoList.toString());

			// Creating a Personalized PDF Document for the data entered in
			// Form.
			String srcDir = vdpReq.m_PDFSourceDir;
			String custImagesDir = vdpReq.m_ImageSourceDir;
			System.out.println("Creating PDFDoc in: " + srcDir);
			// This creates User Page Data which do not handle Alternate and
			// Overflow
			// Fields. Once Personalized PDF Page is created, Process VDP Text
			// Fields will
			// appropriatly handle the alternate and overflow fields.
			boolean acceptEmptyFld = true;
			HashMap userPageDataSet = this.createPersonalizedPDFData(
					vdpReq.m_VDPFieldData, custImagesDir, pdfPageInfoList,
					acceptEmptyFld);
			RGPTLogger
					.logToFile("Derived User Page Data for PDF Personalization: "
							+ userPageDataSet);
			int pgNum = pages.elementAt(0);
			PDFPageInfo pdfPage = (PDFPageInfo) pdfPageInfoList.elementAt(0);
			UserPageData userPageData = (UserPageData) userPageDataSet
					.get(pgNum);
			persPageImg = this.getPersonalizedPDFPage(pdfPage, userPageData,
					true);
			Map<RGPTRectangle, Vector> multiWordSel = null;
			multiWordSel = this.createMultiWordSelLine(pdfPage, userPageData);
			Vector vdpTextFields = null, vdpImageFields = null;
			vdpTextFields = pdfPage.m_VDPTextFieldInfo;
			vdpImageFields = pdfPage.m_VDPImageFieldInfo;
			this.processVDPTextFields(pdfPage, vdpTextFields, userPageData,
					multiWordSel);
			this.processVDPImageFields(vdpImageFields, userPageData);

			// AGain creating the final Personalized Page by appropriatly
			// handling the
			// alternate and overflow fields.
			persPageImg = this.getPersonalizedPDFPage(pdfPage, userPageData,
					false);
			// RGPTLogger.logToFile("Generated PersonalizedPDFPage: " +
			// persPageImg.toString());
			// System.out.println("Generated PersonalizedPDFPage: " +
			// persPageImg.toString());

			// This HashMap maintains the Page Data for every Page. The Page
			// Data
			// consists of Serialized PDF Page Info Object and PDF Page Image.
			HashMap resultantPDF = new HashMap(), pagePDF = new HashMap();
			resultantPDF.put(new Integer(pgNum), pagePDF);

			// Generating Low Res and Thumbview File for the Page and storing in
			// the
			// file system and in Page Data for future retrival
			double ht = pdfPage.m_PageWidth / 72, wd = pdfPage.m_PageHeight / 72;
			int qualDPI = this.getPDFPageQuality(wd, ht, false);
			// System.out.println("Appropriate DPI for Viewing: " + qualDPI);
			RGPTLogger.logToFile("Appropriate DPI for Viewing: " + qualDPI);
			String imgExt = ".png";
			String pdfPgImgFile = this.getPDFPageFileName(qualDPI, pgNum,
					vdpReq.m_ProjectId, imgExt);
			BufferedImage lowResPgImg = ImageUtils.DownSampleImage(persPageImg,
					wd, ht, qualDPI);
			ImageUtils.SaveImageToFile(lowResPgImg, srcDir + pdfPgImgFile,
					"PNG");
			RGPTLogger.logToFile("Saved Low Res Image to: " + srcDir
					+ pdfPgImgFile);
			RGPTLogger.logToFile("Low Res Image File: "
					+ lowResPgImg.toString());
			// System.out.println("Saved Low Res Image to: " + srcDir +
			// pdfPgImgFile);
			pagePDF.put("RegQualityImageFile", pdfPgImgFile);
			String thumbImgFile = this.getPDFPageFileName(-1, pgNum,
					vdpReq.m_ProjectId, imgExt);

			BufferedImage thumbPgImg = ImageUtils.scaleImage(persPageImg, 100,
					100, true);
			ImageUtils
					.SaveImageToFile(thumbPgImg, srcDir + thumbImgFile, "PNG");
			pagePDF.put("ThumbViewFile", thumbImgFile);
			RGPTLogger.logToFile("Personalized Page Info: " + resultantPDF);

			// Storing Thumbview Image for the First Page
			ServerResponse servResp = new ServerResponse();
			servResp.m_IsSuccess = true;

			HashMap pdfAllPageImg = null;
			String pdfSerializedFile = vdpReq.m_PDFFileName + ".ser";
			String pdfPgPath = srcDir + pdfSerializedFile;
			try {
				pdfAllPageImg = (HashMap) RGPTUtil
						.getSerializeObject(pdfPgPath);
			} catch (Exception ex) {
			}

			if (pdfAllPageImg == null)
				pdfAllPageImg = resultantPDF;
			else
				pdfAllPageImg.put(new Integer(pgNum), pagePDF);
			RGPTUtil.serializeObject(pdfPgPath, pdfAllPageImg);

			String firstPgImg = pdfPgImgFile, firstPgSmallImg = thumbImgFile;
			if (pgNum > 1) {
				HashMap pdfPageImg = (HashMap) pdfAllPageImg.get(1);
				firstPgImg = (String) pdfPageImg.get("RegQualityImageFile");
				firstPgSmallImg = (String) pdfPageImg.get("ThumbViewFile");
			}

			servResp.m_ResultValues.put("EnlargedViewFile", firstPgImg);
			servResp.m_ResultValues.put("ThumbViewFile", firstPgSmallImg);
			servResp.m_ResultValues.put("PDFPageCount",
					templateInfo.m_PageCount);
			servResp.m_ResultValues.put("SerializedPDFFile", pdfSerializedFile);
			return servResp;
		} catch (Exception ex) {
			ServerResponse servResp = new ServerResponse();
			servResp.m_IsSuccess = false;
			StringBuffer msg = new StringBuffer("UNABLE TO CREATE FORM BASED ");
			msg.append("PERSONALIZE DOCUMENT- " + ex.getMessage());
			RGPTLogger.logToFile("\n" + msg.toString() + "\n");
			ex.printStackTrace();
			return servResp;
		}
	}

	// This function is called to create PDF Document corresponding to every row
	// in the Batch File. The assetPath identifies the Path for Image VDP Field
	public HashMap createPersonalizedPDFData(HashMap vdpDataMap,
			String assetPath, Vector pdfPageInfoList, boolean acceptEmptyFld) {
		// Setting the Scaling Factor to calculate the CTM and to find the
		// Bounds
		// for the VDP text fields
		ScalingFactor scaleFactor = ScalingFactor.ZOOM_IN_OUT;
		scaleFactor.setZoom(100);
		boolean isSuccess = false;
		HashMap vdpData = null;
		UserPageData userPageVDPData = null;
		Dimension panelSize = null;
		Vector vdpTextFields = null;
		VDPTextFieldInfo vdpTextFieldInfo;
		PDFPageInfo pgInfo = null;
		HashMap userPageData = new HashMap();
		Map<RGPTRectangle, Vector> multiWordSel = null;
		try {
			// Populating the User Page Data and setting the value from the
			// Batch File
			for (int i = 0; i < pdfPageInfoList.size(); i++) {
				pgInfo = (PDFPageInfo) pdfPageInfoList.elementAt(i);
				int pgNum = pgInfo.m_PageNum;
				try {
					userPageVDPData = pgInfo.populateUserPageData(assetPath,
							vdpDataMap, acceptEmptyFld);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				RGPTLogger.logToFile("User Page Data for Page: " + pgNum
						+ " is: " + userPageVDPData);
				userPageData.put(pgNum, userPageVDPData);
				panelSize = new Dimension((int) pgInfo.m_PageWidth,
						(int) pgInfo.m_PageHeight);
				pgInfo.deriveDeviceCTM(scaleFactor, panelSize, 0,
						pgInfo.m_PageWidth, pgInfo.m_PageHeight);
				vdpTextFields = pgInfo.m_VDPTextFieldInfo;
				// Assigning the Sequence based on the Position of the Text
				// Fields.
				// This is required for multi-word selection
				for (int iter = 0; iter < vdpTextFields.size(); iter++) {
					vdpTextFieldInfo = (VDPTextFieldInfo) vdpTextFields
							.elementAt(iter);
					vdpData = userPageVDPData.getVDPData("Text",
							vdpTextFieldInfo.m_PageRectangle);
					if (vdpData == null)
						continue;
					System.out.println("Fld Name: "
							+ vdpData.get("VDPFieldName") + " Value"
							+ vdpData.get("UserSetValue"));
					Rectangle2D.Double rect = ((RGPTRectangle) vdpData
							.get("NewBBox")).getRectangle2D();
					Rectangle2D.Double newRect = this.calcScreenBounds(pgInfo,
							rect);
					vdpData.put("DeviceBBox",
							RGPTRectangle.getReactangle(newRect));
				}
				boolean isTextSeqSet = (pgInfo.m_IsPageSequenceSet4Text
						.get(pgNum)).booleanValue();
				if (!isTextSeqSet) {
					userPageVDPData.assignSeqId();
					pgInfo.m_IsPageSequenceSet4Text.put(pgNum, true);
				}

				// Narayan - Do not know whats the use for Multi Word Spec.
				// Hence commenting out
				// multiWordSel = this.createMultiWordSelLine(vdpTextFields,
				// userPageVDPData);
				// adjustMultiWordSel(pgInfo, multiWordSel);
				/*
				 * for(int iter = 0; iter < vdpTextFields.size(); iter++) {
				 * vdpTextFieldInfo = (VDPTextFieldInfo)
				 * vdpTextFields.elementAt(iter); int vdpTextMode =
				 * vdpTextFieldInfo.m_VDPTextMode; if (vdpTextMode ==
				 * StaticFieldInfo.PARA) continue;
				 * RGPTLogger.logToFile("PageRectangle: " +
				 * vdpTextFieldInfo.m_PageRectangle.toString()); vdpData =
				 * userPageVDPData.getVDPData("Text",
				 * vdpTextFieldInfo.m_PageRectangle); if (vdpData == null)
				 * continue; String vdpText = (String)
				 * vdpData.get("UserSetValue"); String vdpfinalTxt = ""; //if
				 * (vdpText == null || vdpText.length() == 0) continue; if
				 * (vdpText == null || vdpText.length() == 0) { // vdpfinalTxt =
				 * getAltVDPText(pgInfo, userPageVDPData, vdpData); // if
				 * (vdpText == null) continue; continue; } else { vdpfinalTxt =
				 * this.applyRules(vdpData, vdpTextFieldInfo);
				 * vdpData.put("UserSetValue", vdpText);
				 * vdpData.put("UserSetFinalValue", vdpfinalTxt); } //if
				 * (vdpTextFieldInfo
				 * .m_TextAllignment.equals(PDFPageHandler.ALLIGN_LEFT)) //
				 * continue; // Narayan - Not needed after using Java to
				 * construct Final PDF // this.calcStartPoint(pgInfo,
				 * vdpTextFieldInfo, vdpData); // RGPTRectangle lineBBox =
				 * vdpTextFieldInfo.m_LineBBox; // if (vdpTextMode !=
				 * StaticFieldInfo.WORD || lineBBox == null) continue; //
				 * this.getLineTxt4MultiWdSel(multiWordSel, lineBBox, vdpData);
				 * }
				 */
			}

			return userPageData;
		} catch (Exception ex) {
			StringBuffer msg = new StringBuffer(
					"UNABLE TO CREATE PERSONALIZE DOCUMENT- ");
			msg.append(ex.getMessage());
			RGPTLogger.logToFile("\n" + msg.toString() + "\n");
			ex.printStackTrace();
			throw new RuntimeException(msg.toString());
		}
	}

	private Rectangle2D.Double calcScreenBounds(PDFPageInfo pgInfo,
			Rectangle2D.Double srcRect) {
		Point2D.Double ptSrc, ptDst1, ptDst2;
		double desX1, desY1, desWidth, desHeight;
		AffineTransform finalDevCTM = null;
		Rectangle2D.Double desRect = null;

		ptSrc = new Point2D.Double();
		ptDst1 = new Point2D.Double();
		ptDst2 = new Point2D.Double();

		// Assigning the Final Device CTM
		finalDevCTM = (AffineTransform) pgInfo.m_FinalDeviceCTM.clone();

		// Deriving x1, y1 of the Rectangle
		ptSrc.setLocation(srcRect.getX(), srcRect.getY());
		finalDevCTM.transform(ptSrc, ptDst1);
		// RGPTLogger.logToFile("Screen PT SRC " + ptSrc.toString() +
		// ": Screen PT DEST1 " + ptDst1.toString());

		// Deriving x2, y2 of the Rectangle
		ptSrc.setLocation(srcRect.getX() + srcRect.getWidth(), srcRect.getY()
				+ srcRect.getHeight());
		finalDevCTM.transform(ptSrc, ptDst2);
		// RGPTLogger.logToFile("PT DEST2 " + ptDst2.toString());

		desX1 = Math.min(ptDst1.getX(), ptDst2.getX());
		desY1 = Math.min(ptDst1.getY(), ptDst2.getY());
		desWidth = Math.abs(ptDst1.getX() - ptDst2.getX());
		desHeight = Math.abs(ptDst1.getY() - ptDst2.getY());
		;

		desRect = new Rectangle2D.Double(desX1, desY1, desWidth, desHeight);
		return desRect;
	}

	private void adjustMultiWordSel(PDFPageInfo pgInfo,
			Map<RGPTRectangle, Vector> multiWordSel) {
		Vector<HashMap> vdpTxtWords = null;
		HashMap vdpData = null;
		RGPTRectangle lineBBox = null, mappedBBox = null;
		Rectangle2D.Double bbox, pdfPgRect, lineRect;
		VDPTextFieldInfo vdpTextFieldInfo = null;
		RGPTRectangle[] lineBBoxArr = multiWordSel.keySet().toArray(
				new RGPTRectangle[0]);
		for (int j = 0; j < lineBBoxArr.length; j++) {
			vdpTxtWords = multiWordSel.get(lineBBoxArr[j]);
			for (int k = 0; k < vdpTxtWords.size(); k++) {
				vdpData = (HashMap) vdpTxtWords.elementAt(k);
				String vdpText = (String) vdpData.get("UserSetValue");
				bbox = ((RGPTRectangle) vdpData.get("BBox")).getRectangle2D();
				vdpTextFieldInfo = (VDPTextFieldInfo) pgInfo.getVDPFieldInfo(
						"Text", bbox);
				String vdpfinalTxt = "";
				if (vdpText == null)
					continue;
				else {
					vdpfinalTxt = this.applyRules(vdpData, vdpTextFieldInfo);
					vdpData.put("UserSetFinalValue", vdpfinalTxt);
				}
			}
			// Calculating the new Text BBox
			vdpData = (HashMap) vdpTxtWords.elementAt(0);
			String allign = (String) vdpData.get("TextAllignment");
			double totTxtWt = getLineTxtWidth(vdpTxtWords);
			lineRect = lineBBoxArr[j].getRectangle2D();
			if (allign.equals(PDFPageHandler.ALLIGN_LEFT))
				this.calcDevBBox4LeftAllign(pgInfo, vdpTxtWords, lineRect);
			else if (allign.equals(PDFPageHandler.ALLIGN_CENTER))
				this.calcDevBBox4CenterAllign(pgInfo, vdpTxtWords, lineRect,
						totTxtWt);
			else if (allign.equals(PDFPageHandler.ALLIGN_RIGHT))
				this.calcDevBBox4RightAllign(pgInfo, vdpTxtWords, lineRect);
		}
	}

	private int getVdpTxtCounter(int txtWdSeqId, Vector<HashMap> vdpTxtWords) {
		int cntr = 0;
		HashMap vdpData = null;
		for (int i = 0; i < vdpTxtWords.size(); i++) {
			vdpData = vdpTxtWords.elementAt(i);
			int seqId = ((Integer) vdpData.get("Counter")).intValue();
			if (txtWdSeqId < seqId)
				continue;
			cntr++;
		}
		return cntr;
	}

	private void calcDevBBox4LeftAllign(PDFPageInfo pgInfo,
			Vector<HashMap> vdpTxtWords, Rectangle2D.Double lineRect) {
		double x = 0.0, tx = 0.0, txtWt = 0.0, totTxtWt = 0.0;
		HashMap vdpData = null;
		RGPTRectangle devBBox = null, newDevBBox = null, lineBBox = null;
		Rectangle2D textRect = null;
		lineBBox = RGPTRectangle.getReactangle(lineRect);
		for (int i = 0; i < vdpTxtWords.size(); i++) {
			vdpData = vdpTxtWords.elementAt(i);
			txtWt = ((Double) vdpData.get("TextRectWt")).doubleValue();
			// txtWt = textRect.getWidth();
			devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
			newDevBBox = new RGPTRectangle(devBBox);
			if (i == 0)
				x = lineBBox.x;
			else {
				newDevBBox.x = x;
			}
			newDevBBox.width = txtWt;
			x += newDevBBox.width;
			RGPTLogger.logToFile("New Device BBox Left Allign: "
					+ newDevBBox.toString());
			vdpData.put("NewDeviceBBox", newDevBBox);
			calcNewBBox(pgInfo, vdpData);
		}
	}

	private void calcDevBBox4RightAllign(PDFPageInfo pgInfo,
			Vector<HashMap> vdpTxtWords, Rectangle2D.Double lineRect) {
		double x = 0.0, txtWt = 0.0, totTxtWt = 0.0;
		HashMap vdpData = null;
		RGPTRectangle devBBox = null, newDevBBox = null, lineBBox = null;
		Rectangle2D textRect = null;
		RGPTLogger.logToFile("In Right Allign, Size: " + vdpTxtWords.size());
		lineBBox = RGPTRectangle.getReactangle(lineRect);
		for (int i = vdpTxtWords.size() - 1; i > -1; i--) {
			vdpData = vdpTxtWords.elementAt(i);
			txtWt = ((Double) vdpData.get("TextRectWt")).doubleValue();
			// txtWt = textRect.getWidth();
			devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
			newDevBBox = new RGPTRectangle(devBBox);
			if (i == vdpTxtWords.size() - 1)
				x = lineBBox.x + lineBBox.width - txtWt;
			else
				x = x - txtWt;
			newDevBBox.x = x;
			newDevBBox.width = txtWt;
			RGPTLogger.logToFile("New Device BBox Right Allign: "
					+ newDevBBox.toString());
			vdpData.put("NewDeviceBBox", newDevBBox);
			calcNewBBox(pgInfo, vdpData);
		}
	}

	private void calcDevBBox4CenterAllign(PDFPageInfo pgInfo,
			Vector<HashMap> vdpTxtWords, Rectangle2D.Double lineRect,
			double totTxtWt) {
		double x = 0.0, deltax = 0.0, txtWt = 0.0;
		HashMap vdpData = null;
		RGPTRectangle devBBox = null, newDevBBox = null, lineBBox = null;
		Rectangle2D textRect = null;
		// panelWt = getTotalWordBBoxWidth(vdpTxtWords);
		double panelWt = lineRect.getWidth();
		lineBBox = RGPTRectangle.getReactangle(lineRect);
		for (int i = 0; i < vdpTxtWords.size(); i++) {
			vdpData = vdpTxtWords.elementAt(i);
			txtWt = ((Double) vdpData.get("TextRectWt")).doubleValue();
			// txtWt = textRect.getWidth();
			devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
			newDevBBox = new RGPTRectangle(devBBox);
			if (i == 0) {
				RGPTLogger.logToFile("Line Panel Wt: " + panelWt
						+ " Total Txt Wt: " + totTxtWt);
				if (panelWt < totTxtWt) {
					deltax = (totTxtWt - panelWt) / 2;
					x = lineBBox.x - deltax;
				} else {
					deltax = (panelWt - totTxtWt) / 2;
					x = lineBBox.x + deltax;
				}
			}
			newDevBBox.x = x;
			newDevBBox.width = txtWt;
			x += newDevBBox.width;
			RGPTLogger.logToFile("New Device BBox Right Allign: "
					+ newDevBBox.toString());
			vdpData.put("NewDeviceBBox", newDevBBox);
			calcNewBBox(pgInfo, vdpData);
		}
	}

	private double getLineTxtWidth(Vector<HashMap> vdpTxtWords) {
		double txtWt = 0.0, totTxtWt = 0.0;
		HashMap vdpData = null;
		Rectangle2D textRect = null;
		for (int i = 0; i < vdpTxtWords.size(); i++) {
			vdpData = vdpTxtWords.elementAt(i);
			// textRect = (Rectangle2D) vdpData.get("TextRect");
			txtWt = ((Double) vdpData.get("TextRectWt")).doubleValue();
			// txtWt = textRect.getWidth();
			totTxtWt += txtWt;
		}
		return totTxtWt;
	}

	private void calcNewBBox(PDFPageInfo pageInfo, HashMap vdpData) {
		RGPTRectangle rgptRect = (RGPTRectangle) vdpData
				.remove("NewDeviceBBox");
		if (rgptRect == null)
			return;
		Rectangle2D.Double bbox = rgptRect.getRectangle2D();
		RGPTLogger.logToFile("Screen BBox: " + bbox.toString());
		// Screen Points
		double x = bbox.getX(), y = bbox.getY(), w = bbox.getWidth(), h = bbox
				.getHeight();
		Point2D.Double scrPt1 = new Point2D.Double(x, y);
		Point2D.Double scrPt2 = new Point2D.Double(x, y + h);
		Point2D.Double scrPt3 = new Point2D.Double(x + w, y);
		Point2D.Double scrPt4 = new Point2D.Double(x + w, y + h);
		RGPTLogger.logToFile("Screen Pt 1: " + scrPt1.toString());
		RGPTLogger.logToFile("Screen Pt 2: " + scrPt2.toString());
		RGPTLogger.logToFile("Screen Pt 3: " + scrPt3.toString());
		RGPTLogger.logToFile("Screen Pt 4: " + scrPt4.toString());

		// Calc Page Points
		AffineTransform finalDevCTM = (AffineTransform) pageInfo.m_FinalDeviceCTM
				.clone();
		AffineTransform invCTM = null;
		try {
			invCTM = finalDevCTM.createInverse();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		RGPTLogger.logToFile("Inv Transform: " + invCTM.toString());

		Point2D.Double pgPt1 = new Point2D.Double();
		invCTM.transform(scrPt1, pgPt1);
		Point2D.Double pgPt2 = new Point2D.Double();
		invCTM.transform(scrPt2, pgPt2);
		Point2D.Double pgPt3 = new Point2D.Double();
		invCTM.transform(scrPt3, pgPt3);
		Point2D.Double pgPt4 = new Point2D.Double();
		invCTM.transform(scrPt4, pgPt4);
		RGPTLogger.logToFile("Page Pt 1: " + pgPt1.toString());
		RGPTLogger.logToFile("Page Pt 2: " + pgPt2.toString());
		RGPTLogger.logToFile("Page Pt 3: " + pgPt3.toString());
		RGPTLogger.logToFile("Page Pt 4: " + pgPt4.toString());

		double pgWth = Math.abs(pgPt3.getX() - pgPt1.getX());
		double pgHt = Math.abs(pgPt2.getY() - pgPt1.getY());

		double pgPtX = Math.min(pgPt1.getX(), pgPt3.getX());
		double pgPtY = Math.min(pgPt1.getY(), pgPt2.getY());
		Rectangle2D.Double pgBBox = new Rectangle2D.Double(pgPtX, pgPtY, pgWth,
				pgHt);
		Rectangle2D.Double origPgBBox = ((RGPTRectangle) vdpData.get("BBox"))
				.getRectangle2D();
		vdpData.put("NewBBox", RGPTRectangle.getReactangle(pgBBox));
		RGPTLogger.logToFile("Orig Page BBox: " + origPgBBox.toString());
		RGPTLogger.logToFile("New Page BBox: " + pgBBox.toString());

		// Calculating the Translation in Y direction
		double tx = pgBBox.getX() - origPgBBox.getX();
		double ty = pgBBox.getY() - origPgBBox.getY();
		vdpData.put("Tx", tx);
		vdpData.put("Ty", ty);
	}

	public String getAltVDPText(PDFPageInfo pgInfo, UserPageData userPgData,
			HashMap vdpData) {
		String altVDPFld = (String) vdpData.get("AlternateVDPField");
		if (altVDPFld.equals("None"))
			return null;
		RGPTLogger.logToFile("\n\n**********************\n\n");
		RGPTLogger.logToFile("Alt VDP Field to be Used: " + altVDPFld);
		HashMap altVDPData = userPgData.getVDPData(altVDPFld);
		if (altVDPData == null)
			return null;
		String vdpText = (String) altVDPData.get("UserSetValue");
		boolean applyRules = true;
		if (vdpText == null || vdpText.length() == 0) {
			vdpText = getAltVDPText(pgInfo, userPgData, altVDPData);
			if (vdpText == null)
				return null;
			applyRules = false;
		}
		String vdpFinalText = "";
		if (applyRules) {
			Rectangle2D.Double bbox = ((RGPTRectangle) altVDPData.get("BBox"))
					.getRectangle2D();
			VDPTextFieldInfo vdpTextFieldInfo = null;
			vdpTextFieldInfo = (VDPTextFieldInfo) pgInfo.getVDPFieldInfo(
					"Text", bbox);
			if (vdpTextFieldInfo == null)
				return null;
			vdpFinalText = this.applyRules(altVDPData, vdpTextFieldInfo);
		} else
			vdpFinalText = (String) altVDPData.get("UserSetFinalValue");
		RGPTLogger.logToFile("Alt VDP Text: " + vdpText
				+ " Fianl Formatted Text: " + vdpFinalText);
		vdpData.put("UserSetValue", vdpText);
		vdpData.put("UserSetFinalValue", vdpFinalText);
		altVDPData.put("UserSetValue", "");
		altVDPData.put("UserSetFinalValue", "");
		return vdpText;
	}

	private String applyRules(HashMap vdpData, VDPTextFieldInfo vdpTextFieldInfo) {
		String vdpText = (String) vdpData.get("UserSetValue");
		RGPTLogger.logToFile("VDP Text Before putting Rules: " + vdpText);
		boolean fldLthFixed = ((Boolean) vdpData.get("FieldLengthFixed"))
				.booleanValue();
		int fldLth = Integer.parseInt((String) vdpData.get("FieldLength"));
		String dataType = (String) vdpData.get("TextDataType");
		if (dataType.equals("NUMBER")) {
			try {
				long numEnt = Long.parseLong(vdpText);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException("Text Entered: " + vdpText
						+ " instead of Numbers");
			}
		}
		if (fldLthFixed) {
			String prefixVal = (String) vdpData.get("PrefixValue");
			fldLth = fldLth - prefixVal.length();
			if (vdpText.length() > fldLth) {
				vdpText = vdpText.substring(0, fldLth);
			}
		}
		if (vdpTextFieldInfo.m_UseTitleCase)
			vdpText = RGPTUtil.toTitleCase(vdpText);
		int formatType = vdpTextFieldInfo.m_TextFormatType;
		String formatSpec = vdpTextFieldInfo.m_TextFormatValue;
		vdpText = TextFormatter.getFormattedText(vdpText, formatType,
				formatSpec);
		vdpText = vdpTextFieldInfo.m_PrefixValue + vdpText;
		if (vdpTextFieldInfo.m_SuffixValue != null
				&& vdpTextFieldInfo.m_SuffixValue.trim().length() > 0)
			vdpText = vdpText + vdpTextFieldInfo.m_SuffixValue;
		RGPTLogger
				.logToFile("New Updated Text After putting Rules: " + vdpText);
		return vdpText;
	}

	// This retrives PDF Page Info Object for all the Pages in the Template
	public Vector getAllPDFPageInfo(TemplateInfo templateInfo) {
		return getAllPDFPageInfo(null, templateInfo);
	}

	public Vector getAllPDFPageInfo(Vector<Integer> pages,
			TemplateInfo templateInfo) {
		TemplatePageData pageData = null;
		ObjectInputStream objInputStr = null, fontObjStr = null;
		FileInputStream fileStream = null;
		HashMap pageDataMap = templateInfo.m_TemplatePageData;
		Vector pdfPageInfoList = new Vector(templateInfo.m_PageCount);
		boolean buildFontStream = false;
		if (pages != null)
			System.out.println("Retrieve PageInfo for Pages: "
					+ pages.toString());
		try {
			// Traversing through Each Page and retriving the PDFPageInfo from
			// Serialized File.
			for (int i = 0; i < pageDataMap.size(); i++) {
				int pgNum = i + 1;
				if (pages != null && !pages.contains(new Integer(pgNum)))
					continue;
				pageData = (TemplatePageData) pageDataMap
						.get(new Integer(pgNum));
				RGPTLogger.logToFile("TEMPLATE Page INFO: "
						+ pageData.toString());
				String srcDir = templateInfo.m_TemplateSourceDir;
				String serializedFile = templateInfo.m_SerializedDataFile;
				String fileName = srcDir + pageData.m_SerializedDataFile;
				objInputStr = new ObjectInputStream(new FileInputStream(
						fileName));
				PDFPageInfo pdfPageInfo = (PDFPageInfo) PDFPageInfo
						.load(objInputStr);
				objInputStr.close();
				String pdfPageImgPath = srcDir + pageData.m_MaxQualityImageFile;
				pdfPageInfo.m_BufferedImage = ImageUtils
						.getBufferedImage(pdfPageImgPath);
				// Resetting the Scale in the PDF Page Info to accomodate the
				// 300 DPI
				// Image for Final PDF Generation
				pdfPageInfo.m_PageScale = -1.0;
				pdfPageInfo.buildProperFields();
				pdfPageInfoList.addElement(pdfPageInfo);
				if (pdfPageInfo.m_VDPTextFieldInfo.size() == 0)
					continue;
				if (buildFontStream)
					continue;
				if (VDPTextFieldInfo.m_FontStreamHolder != null)
					continue;
				// Retriving the Font File
				String absFilePath = templateInfo.m_TemplateSourceDir
						+ templateInfo.m_SerializedFontFile;
				RGPTLogger.logToFile("Abs Font File Path: " + absFilePath);
				fileStream = new FileInputStream(absFilePath);
				fontObjStr = new ObjectInputStream(fileStream);
				HashMap fontStream = (HashMap) fontObjStr.readObject();
				VDPTextFieldInfo vdpText = (VDPTextFieldInfo) pdfPageInfo.m_VDPTextFieldInfo
						.elementAt(0);
				if (vdpText.m_FontStreamHolder != null)
					continue;
				vdpText.m_FontStreamHolder = fontStream;
				RGPTLogger.logToFile("\n*********************\n");
				RGPTLogger.logToFile("FONT STREAM SIZE: " + fontStream.size());
				RGPTLogger.logToFile("FONT STREAM SDFObjNum: "
						+ fontStream.toString());
				RGPTLogger.logToFile("\n*********************\n");
				fontObjStr.close();
				fileStream.close();
				pdfPageInfo.buildFontStream();
				buildFontStream = true;
			}
			return pdfPageInfoList;
		} catch (Exception ex) {
			RGPTLogger.logToFile("Error in getAllPDFPageInfo for Template: "
					+ templateInfo.m_TemplateName);
			ex.printStackTrace();
			throw new RuntimeException("Error in retriving Page Info: "
					+ ex.getMessage());
		} finally {
			try {
				if (objInputStr != null)
					objInputStr.close();
				if (fontObjStr != null)
					fontObjStr.close();
				if (fileStream != null)
					fileStream.close();
			} catch (Exception ex) {
			}
		}
	}

	public void calcDeviceBBox(PDFPageInfo pgInfo, UserPageData userPgData) {
		HashMap vdpData = null;
		Rectangle2D.Double origPDFPgBBox, pdfPgBBox, deviceBBox;
		Dimension panelSize = new Dimension((int) pgInfo.m_ImageWidth,
				(int) pgInfo.m_ImageHeight);
		BufferedImage persPDFPage = null;
		int pgImgWt = (int) pgInfo.m_ImageWidth, pgImgHt = (int) pgInfo.m_ImageHeight;
		persPDFPage = new BufferedImage(pgImgWt, pgImgHt,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = persPDFPage.createGraphics();
		ScalingFactor scaleFactor = ScalingFactor.ZOOM_IN_OUT;
		scaleFactor.setZoom(100);
		pgInfo.deriveDeviceCTM(scaleFactor, panelSize, 0, pgImgWt, pgImgHt);

		// Calc Graphic Points for Text Field
		Vector vdpTextFields = pgInfo.m_VDPTextFieldInfo;
		VDPTextFieldInfo vdpTextFieldInfo;
		for (int i = 0; i < vdpTextFields.size(); i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) vdpTextFields.elementAt(i);
			origPDFPgBBox = vdpTextFieldInfo.m_PageRectangle;
			vdpData = userPgData.getVDPData("Text", origPDFPgBBox);
			pdfPgBBox = ((RGPTRectangle) vdpData.get("NewBBox"))
					.getRectangle2D();
			deviceBBox = this.calcScreenBounds(pgInfo, pdfPgBBox);
			vdpData.put("DeviceBBox", RGPTRectangle.getReactangle(deviceBBox));
		}

		// Calc Graphic Points for Text Field
		Vector vdpImageFields = pgInfo.m_VDPImageFieldInfo;
		VDPImageFieldInfo vdpImageFieldInfo = null;
		for (int i = 0; i < vdpImageFields.size(); i++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) vdpImageFields.elementAt(i);
			origPDFPgBBox = vdpImageFieldInfo.m_PageRectangle;
			deviceBBox = this.calcScreenBounds(pgInfo, origPDFPgBBox);
			vdpData = userPgData.getVDPData("Image", origPDFPgBBox);
			vdpData.put("DeviceBBox", RGPTRectangle.getReactangle(deviceBBox));
		}
		g2d.dispose();
	}

	// This method is invoked from PDFServer to satisfy VDP Form
	// procVDPText signifies if VDP Text has to be written directly into
	// graphics
	// or pocessing of position, suffix, prefix, fonts, formats, etc are
	// required.
	// If set to true, processing is done, else font is directly written into
	// the graphics.
	public BufferedImage getPersonalizedPDFPage(PDFPageInfo pgInfo,
			UserPageData userPgData, boolean procVDPText) {
		return this.getPersonalizedPDFPage(pgInfo, userPgData, null,
				procVDPText);
	}

	// This method is called for PDFView Controller before creating the final
	// personalized PDF.
	// This takes the PDF Page size to create the Graphics which will be used to
	// create Personalized
	// Graphics with user input.
	public BufferedImage getPersonalizedPDFPage(PDFPageInfo pgInfo,
			UserPageData userPgData, Map<RGPTRectangle, Vector> multiWordSel,
			boolean procVDPText) {
		Dimension panelSize = new Dimension((int) pgInfo.m_ImageWidth,
				(int) pgInfo.m_ImageHeight);
		BufferedImage persPDFPage = null;
		persPDFPage = new BufferedImage((int) pgInfo.m_ImageWidth,
				(int) pgInfo.m_ImageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = persPDFPage.createGraphics();

		if (multiWordSel == null)
			multiWordSel = this.createMultiWordSelLine(pgInfo, userPgData);

		this.drawOnGraphics(g2d, pgInfo, userPgData, multiWordSel, procVDPText);
		g2d.dispose();
		return persPDFPage;
	}

	// Narayan - New API from PDFPageViewHandler

	// createMultiWordSelLine creats a Map of number of VDP Text Words in a Line
	// BBox
	public Map<RGPTRectangle, Vector> createMultiWordSelLine(
			PDFPageInfo pgInfo, UserPageData userPgData) {
		Vector<HashMap> vdpTxtWords = null;
		HashMap vdpData = null;
		RGPTRectangle lineBBox = null, mappedBBox = null;
		Rectangle2D.Double rect, pdfPgRect, newRect, desRect;
		Vector vdpTextFields = pgInfo.m_VDPTextFieldInfo;
		Map<RGPTRectangle, Vector> multiWordSelLine = new HashMap<RGPTRectangle, Vector>();
		VDPTextFieldInfo vdpTextFieldInfo;

		// Buiding the m_MultiWordSelLine Object
		for (int i = 0; i < vdpTextFields.size(); i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) vdpTextFields.elementAt(i);
			String text = vdpTextFieldInfo.m_FieldValue;
			pdfPgRect = vdpTextFieldInfo.m_PageRectangle;
			if (vdpTextFieldInfo.m_FieldType.equals("TextOnGraphics"))
				continue;
			vdpData = userPgData.getVDPData("Text", pdfPgRect);
			if (vdpData == null) {

				throw new RuntimeException("VDP Data not Created");
			}
			int vdpTextMode = vdpTextFieldInfo.m_VDPTextMode;
			lineBBox = vdpTextFieldInfo.m_LineBBox;
			if (vdpTextMode != StaticFieldInfo.WORD || lineBBox == null)
				continue;
			RGPTLogger.logToFile("Found for : " + text + " LineBBox: "
					+ lineBBox.toString());
			mappedBBox = getLineBBox(multiWordSelLine, lineBBox);
			if (mappedBBox == null)
				mappedBBox = lineBBox;
			else
				vdpTextFieldInfo.m_LineBBox = mappedBBox;
			lineBBox = mappedBBox;
			vdpTxtWords = multiWordSelLine.get(lineBBox);
			if (vdpTxtWords == null) {
				vdpTxtWords = new Vector<HashMap>();
				multiWordSelLine.put(lineBBox, vdpTxtWords);
				vdpTxtWords.addElement(vdpData);
				continue;
			}

			// Re-arranging the multi-word selection based on xy position
			int seqId = ((Integer) vdpData.get("Counter")).intValue();
			int cntr = getVdpTxtCounter(seqId, vdpTxtWords);
			if (cntr >= vdpTxtWords.size())
				vdpTxtWords.addElement(vdpData);
			else
				vdpTxtWords.insertElementAt(vdpData, cntr);
		}

		// RGPTLogger.logToFile("Initial MultiwordSelObject " +
		// multiWordSelLine.toString());
		// Discarding the ones that do not have multi word sel in a line
		RGPTRectangle[] lineBBoxArr = multiWordSelLine.keySet().toArray(
				new RGPTRectangle[0]);
		for (int j = 0; j < lineBBoxArr.length; j++) {
			vdpTxtWords = multiWordSelLine.get(lineBBoxArr[j]);
			if (vdpTxtWords.size() == 1) {
				multiWordSelLine.remove(lineBBoxArr[j]);
				continue;
			}
			for (int k = 0; k < vdpTxtWords.size() - 1; k++) {
				vdpData = vdpTxtWords.elementAt(k);
				String suffix = (String) vdpData.get("SuffixValue");
				if (suffix != null && suffix.length() > 0)
					continue;
				String fldName = (String) vdpData.get("VDPFieldName");
				vdpTextFieldInfo = (VDPTextFieldInfo) pgInfo.getVDPFieldInfo(
						"Text", fldName);
				vdpTextFieldInfo.m_SuffixValue = " ";
				vdpData.put("SuffixValue", vdpTextFieldInfo.m_SuffixValue);
			}
		}
		return multiWordSelLine;
	}

	private RGPTRectangle getLineBBox(Map<RGPTRectangle, Vector> multiWordSel,
			RGPTRectangle lineBBox) {
		RGPTRectangle mappedBBox = null;
		RGPTRectangle[] lineBBoxArr = multiWordSel.keySet().toArray(
				new RGPTRectangle[0]);
		for (int i = 0; i < lineBBoxArr.length; i++) {
			mappedBBox = lineBBoxArr[i];
			if (mappedBBox.equals(lineBBox))
				return mappedBBox;
		}
		return null;
	}

	// This Method is invoked from PDF Page View Handler. This takes the Java
	// Viewer
	// Panel Size for the graphics and fits PDF Page into it.
	public void drawOnGraphics(Graphics g, PDFPageInfo pgInfo,
			UserPageData userPgData, Dimension panelSize,
			ScalingFactor scaleFactor, int adjStartPty,
			Map<RGPTRectangle, Vector> multiWordSelLine, int viewMode,
			Vector selVDPFlds, HashMap selVDPData, JTextArea vdpTextArea) {
		Graphics2D g2d = (Graphics2D) g;
		drawOnGraphics(g2d, pgInfo, userPgData, panelSize, scaleFactor,
				adjStartPty, pgInfo.m_ImageWidth, pgInfo.m_ImageHeight,
				multiWordSelLine, viewMode, selVDPFlds, selVDPData,
				vdpTextArea, true);
	}

	// This is called by the Server and takes the PDF Page size itself as the
	// panel size for the graphics
	public void drawOnGraphics(Graphics2D g2d, PDFPageInfo pgInfo,
			UserPageData userPgData,
			Map<RGPTRectangle, Vector> multiWordSelLine, boolean procVDPText) {
		int viewMode = JAVA_PREVIEW_MODE;
		Dimension panelSize = new Dimension((int) pgInfo.m_ImageWidth,
				(int) pgInfo.m_ImageHeight);
		ScalingFactor scaleFactor = ScalingFactor.ZOOM_IN_OUT;
		scaleFactor.setZoom(100);
		drawOnGraphics(g2d, pgInfo, userPgData, panelSize, scaleFactor, 0,
				pgInfo.m_ImageWidth, pgInfo.m_ImageHeight, multiWordSelLine,
				viewMode, null, null, null, procVDPText);
	}

	private void drawOnGraphics(Graphics2D g2d, PDFPageInfo pgInfo,
			UserPageData userPgData, Dimension panelSize,
			ScalingFactor scaleFactor, int adjStartPty, double pgImgWt,
			double pgImgHt, Map<RGPTRectangle, Vector> multiWordSel,
			int viewMode, Vector selVDPFlds, HashMap selVDPData,
			JTextArea vdpTextArea, boolean procVDPText) {
		// System.out.println("In Paint");

		// This is a check to enable or disable buttons
		// this.checkToEnableButtons();

		// Calculating the CTM to find the Bounds for the VDP text fields
		// StringBuffer mesg = new
		// StringBuffer("***\n\ndrawOnGraphics function Paramsa are: ");
		// mesg.append("\nPDF Page Info: " + pgInfo);
		// mesg.append("\nUserPageData: " + userPgData);
		// mesg.append("\npanelSize: " + panelSize + " ImgWt: " + pgImgWt +
		// " ImgHt: " +
		// pgImgHt + " viewMode: " + viewMode + " scaleFactor: " + scaleFactor);
		// RGPTLogger.logToFile(mesg.toString() + "\n\n***");

		RenderingHints rh = g2d.getRenderingHints();
		rh.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHints(rh);

		pgInfo.deriveDeviceCTM(scaleFactor, panelSize, adjStartPty, pgImgWt,
				pgImgHt);

		g2d.drawImage(pgInfo.m_BufferedImage, pgInfo.m_WindowDeviceCTM, null);
		// System.out.println("\nPANEL SIZE: " + panelSize.toString());

		Shape gClip = g2d.getClip();
		// System.out.println("Graphic Clip Path: " + gClip.toString());

		// Drawing VDP Text Fields
		if (procVDPText)
			this.processVDPTextFields(g2d, pgInfo, userPgData, multiWordSel,
					viewMode, selVDPFlds, vdpTextArea);
		else
			this.drawVDPTextFields(g2d, pgInfo, userPgData);
		// System.out.println("Process VDP Texts " + vdpTextFields.size());
		// Drawing VDP Text Fields JAVA_PREVIEW_MODE PDF_PREVIEW_MODE

		this.processVDPImageFields(g2d, pgInfo, userPgData, selVDPData,
				viewMode);
		// System.out.println("Process VDP Images " + vdpImageFields.size());
	}

	// This method is invoked when no processing of VDP fields are needed.
	private void drawVDPTextFields(Graphics2D g2d, PDFPageInfo pgInfo,
			UserPageData userPgData) {
		Vector vdpTextFields = pgInfo.m_VDPTextFieldInfo;
		VDPTextFieldInfo vdpTextFieldInfo;
		m_FinalVDPTextEntered = new StringBuffer();
		try {
			for (int i = 0; i < vdpTextFields.size(); i++) {
				vdpTextFieldInfo = (VDPTextFieldInfo) vdpTextFields
						.elementAt(i);
				if (vdpTextFieldInfo.m_FieldType.equals("Text")) {
					HashMap vdpData = userPgData
							.getVDPData(vdpTextFieldInfo.m_FieldName);
					createFinalGraphicsForText(g2d, vdpData);
				} else if (vdpTextFieldInfo.m_FieldType
						.equals("TextOnGraphics")) {
				}
			}
		} finally {
			RGPTLogger.logToFile("Final VDP Text Entered: "
					+ m_FinalVDPTextEntered);
		}
	}

	StringBuffer m_FinalVDPTextEntered;

	private void createFinalGraphicsForText(Graphics2D g2d, HashMap vdpData) {
		String vdpText = (String) vdpData.get("UserSetFinalValue");
		if (vdpText == null || vdpText.trim().length() == 0)
			return;
		m_FinalVDPTextEntered.append(vdpText + "::");
		Font font = (Font) vdpData.get("DerivedFont");
		int fillClr = ((Integer) vdpData.get("FillColor")).intValue();
		if (vdpData.get("VDPStartPtX") == null) {
			String msg = "VDPStartPtX is null for Field: "
					+ vdpData.get("VDPFieldName");
			msg += "\n VDP Text Entered: " + vdpText;
			RGPTLogger.logToFile(msg);
			System.out.println(msg);
		}
		// else {
		// String msg = "VDPStartPtX is not null for Field: " +
		// vdpData.get("VDPFieldName");
		// msg += "\n VDP Text Entered: " + vdpText +
		// " ANd the VDPStartPtX: " + vdpData.get("VDPStartPtX");
		// RGPTLogger.logToFile(msg);
		// System.out.println(msg);
		// }
		float startPtx = ((Float) vdpData.get("VDPStartPtX")).floatValue();
		float startPty = ((Float) vdpData.get("VDPStartPtY")).floatValue();
		g2d.setFont(font);
		g2d.setPaint(new Color(fillClr));
		g2d.drawString(vdpText, startPtx, startPty);
	}

	// This method is invoked from the PDFPageViewHandlers Paint Method
	private void processVDPTextFields(Graphics2D g2d, PDFPageInfo pgInfo,
			UserPageData userPgData, Map<RGPTRectangle, Vector> multiWordSel,
			int viewMode, Vector selVDPFlds, JTextArea vdpTextArea) {
		this.processVDPTextFields(g2d, pgInfo, userPgData, multiWordSel, false,
				null, viewMode, selVDPFlds, vdpTextArea);
	}

	// This method is invoked internally from this file and also from the PDF
	// Page View
	// Handlers when the user is entering the text are. In that scenario
	// drawSelVDPTextOnly
	// is set to true and selVDPData is passed.
	public void processVDPTextFields(Graphics2D g2d, PDFPageInfo pgInfo,
			UserPageData userPgData, Map<RGPTRectangle, Vector> multiWordSel,
			boolean drawSelVDPTextOnly, HashMap selVDPData, int viewMode,
			Vector selVDPFlds, JTextArea vdpTextArea) {
		Vector vdpTextFields = pgInfo.m_VDPTextFieldInfo;
		VDPTextFieldInfo vdpTextFieldInfo;

		for (int i = 0; i < vdpTextFields.size(); i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) vdpTextFields.elementAt(i);
			if (vdpTextFieldInfo.m_FieldType.equals("Text")) {
				// No processing if this Field is an Overflow Field. The text
				// for this
				// field will come from the Text element, where in this vdp text
				// field is
				// specified as an overflow field.
				if (vdpTextFieldInfo.m_IsOverFlowField)
					continue;
				this.createGraphicsForText(g2d, pgInfo, vdpTextFieldInfo,
						userPgData, multiWordSel, drawSelVDPTextOnly,
						selVDPData, viewMode, selVDPFlds, vdpTextArea);
			} else if (vdpTextFieldInfo.m_FieldType.equals("TextOnGraphics")) {
				this.createGraphicsForTextOnGraphics(g2d, pgInfo,
						vdpTextFieldInfo, userPgData, drawSelVDPTextOnly,
						selVDPData, viewMode, selVDPFlds, vdpTextArea);
			}
		}
		if (multiWordSel != null)
			this.drawMultiWordSelTxt(g2d, pgInfo, userPgData,
					drawSelVDPTextOnly, selVDPData, null, multiWordSel,
					viewMode, selVDPFlds, vdpTextArea);
	}

	public boolean writeDefaultText(HashMap vdpData, HashMap selVDPData) {
		String vdpText = (String) vdpData.get("UserSetValue");
		boolean blankText = ((Boolean) vdpData.get("BlankDefaultText"))
				.booleanValue();
		System.out.println("VDP Text in UserSetValue: " + vdpText);

		if (vdpText == null)
			return true;
		if (selVDPData != null && selVDPData.equals(vdpData))
			return false;
		if (vdpText.trim().length() == 0)
			return true;
		return false;
	}

	private void drawMultiWordSelTxt(Graphics2D g2d, PDFPageInfo pgInfo,
			UserPageData userPgData, boolean drawSelVDPTextOnly,
			HashMap selVDPData, RGPTRectangle lineBBox,
			Map<RGPTRectangle, Vector> multiWordSel, int viewMode,
			Vector selVDPFlds, JTextArea vdpTextArea) {
		double txtWt = 0.0, totTxtWt = 0.0, panelWidth = 0.0;
		HashMap vdpData = null;
		Rectangle2D textRect = null;
		RGPTRectangle devBBox = null;
		Vector<HashMap> vdpTxtWords = null;
		VDPTextFieldInfo vdpTextFieldInfo = null;
		Rectangle2D.Double lineRect, bbox;
		RGPTRectangle[] lineBBoxArr = multiWordSel.keySet().toArray(
				new RGPTRectangle[0]);
		for (int i = 0; i < lineBBoxArr.length; i++) {
			if (lineBBox != null) {
				if (!lineBBox.equals(lineBBoxArr[i]))
					continue;
			}
			vdpTxtWords = multiWordSel.get(lineBBoxArr[i]);
			lineRect = this.calcScreenBounds(pgInfo,
					lineBBoxArr[i].getRectangle2D());
			RGPTLogger.logToFile("NEW Line BBox RECTANGLE "
					+ lineRect.toString());
			panelWidth = lineRect.getWidth();
			totTxtWt = getLineTxtWidth(vdpTxtWords);
			for (int j = 0; j < vdpTxtWords.size(); j++) {
				vdpData = vdpTxtWords.elementAt(j);
				String allign = (String) vdpData.get("TextAllignment");
				if (j == 0) {
					if (allign.equals(PDFPageHandler.ALLIGN_LEFT))
						this.calcDevBBox4LeftAllign(pgInfo, vdpTxtWords,
								lineRect);
					else if (allign.equals(PDFPageHandler.ALLIGN_CENTER))
						this.calcDevBBox4CenterAllign(pgInfo, vdpTxtWords,
								lineRect, totTxtWt);
					else if (allign.equals(PDFPageHandler.ALLIGN_RIGHT))
						this.calcDevBBox4RightAllign(pgInfo, vdpTxtWords,
								lineRect);
				}
				bbox = ((RGPTRectangle) vdpData.get("BBox")).getRectangle2D();
				vdpTextFieldInfo = (VDPTextFieldInfo) pgInfo.getVDPFieldInfo(
						"Text", bbox);

				// Getting the User Set Data and Drawing the String with the
				// specified Font
				this.createGraphicsForText(g2d, pgInfo, vdpTextFieldInfo,
						userPgData, multiWordSel, drawSelVDPTextOnly,
						selVDPData, true, viewMode, selVDPFlds, vdpTextArea);
			}
		}

	}

	private void showVDPTxt(Graphics2D g2d, Rectangle2D.Double desRect,
			HashMap vdpData, boolean drawSelVDPTextOnly, HashMap selVDPData,
			int viewMode, Vector selVDPFlds) {
		boolean isAllowEdit = ((Boolean) vdpData.get("AllowFieldEdit"))
				.booleanValue();
		String vdpFldName = (String) vdpData.get("VDPFieldName");
		int counter = ((Integer) vdpData.get("Counter")).intValue();
		RGPTLogger.logDebugMesg("VDP Field: " + vdpFldName + " Counter: "
				+ counter + " isAllowEdit: " + isAllowEdit);
		boolean showRectPts = false;
		if (viewMode == EDIT_MODE || viewMode == RESIZE_MOVE_FIELD_MODE) {
			if (selVDPData != null && selVDPData.equals(vdpData)) {
				int viewFldMode = ((Integer) vdpData.get("VDPViewMode"))
						.intValue();
				if (viewFldMode == RESIZE_MODE || viewFldMode == MOVE_MODE)
					showRectPts = true;
			}
			if (isAllowEdit && !drawSelVDPTextOnly)
				this.drawRect(g2d, desRect, vdpData, showRectPts, selVDPData,
						viewMode, selVDPFlds);
		} else if (viewMode == SELECT_MODE) {
			if (isAllowEdit)
				this.drawRect(g2d, desRect, vdpData, showRectPts, selVDPData,
						viewMode, selVDPFlds);
		}
		// else if (m_ViewMode == RESIZE_MOVE_MODE)

		else if (viewMode == RESIZE_MOVE_MODE || viewMode == RESIZE_MODE
				|| viewMode == MOVE_MODE) {
			showRectPts = true;
			if (isAllowEdit)
				this.drawRect(g2d, desRect, vdpData, showRectPts, selVDPData,
						viewMode, selVDPFlds);
		}
	}

	// This is the method decides the processing of Multiword texts and for
	// multiword
	// call necessary methods
	private void createGraphicsForText(Graphics2D origG2D, PDFPageInfo pgInfo,
			VDPTextFieldInfo vdpTextFieldInfo, UserPageData userPgData,
			Map<RGPTRectangle, Vector> multiWordSel,
			boolean drawSelVDPTextOnly, HashMap selVDPData, int viewMode,
			Vector selVDPFlds, JTextArea vdpTextArea) {
		this.createGraphicsForText(origG2D, pgInfo, vdpTextFieldInfo,
				userPgData, multiWordSel, drawSelVDPTextOnly, selVDPData,
				false, viewMode, selVDPFlds, vdpTextArea);
		if (!drawSelVDPTextOnly || multiWordSel == null)
			return;
		int vdpTextMode = vdpTextFieldInfo.m_VDPTextMode;
		RGPTRectangle lineBBox = vdpTextFieldInfo.m_LineBBox;
		if (vdpTextMode != StaticFieldInfo.WORD || lineBBox == null)
			return;
		Vector<HashMap> vdpTxtWords = multiWordSel.get(lineBBox);
		if (vdpTxtWords == null)
			return;
		this.drawMultiWordSelTxt(origG2D, pgInfo, userPgData,
				drawSelVDPTextOnly, selVDPData, lineBBox, multiWordSel,
				viewMode, selVDPFlds, vdpTextArea);
	}

	// This is the method that finally puts the text on the Graphics
	private void createGraphicsForText(Graphics2D origG2D, PDFPageInfo pgInfo,
			VDPTextFieldInfo vdpTextFieldInfo, UserPageData userPgData,
			Map<RGPTRectangle, Vector> multiWordSel,
			boolean drawSelVDPTextOnly, HashMap selVDPData,
			boolean drawMultiWdTxt, int viewMode, Vector selVDPFlds,
			JTextArea vdpTextArea) {
		Rectangle2D.Double rect, pdfPgRect, newRect, desRect;
		String text = vdpTextFieldInfo.m_FieldValue;

		pdfPgRect = vdpTextFieldInfo.m_PageRectangle;
		RGPTRectangle lineBBox = null;
		// RGPTLogger.logToFile("OLD RECTANGLE " + rect.toString());

		// Getting the User Set Data and Drawing the String with the specified
		// Font
		HashMap vdpData = null;
		vdpData = userPgData.getVDPData("Text", pdfPgRect);
		if (vdpData == null)
			throw new RuntimeException("VDP Data not Created");
		int vdpTextMode = vdpTextFieldInfo.m_VDPTextMode;
		lineBBox = vdpTextFieldInfo.m_LineBBox;
		Vector<HashMap> vdpTxtWords = null;

		// Checking if the vdpTextFieldInfo is part of Multi Word Selection
		boolean multiWdSel = false;
		if (vdpTextMode == StaticFieldInfo.WORD) {
			if (multiWordSel != null && lineBBox != null && !drawMultiWdTxt) {
				vdpTxtWords = multiWordSel.get(lineBBox);
				if (vdpTxtWords != null) {
					RGPTLogger.logToFile("Found for LineBBox: "
							+ lineBBox.toString() + " VDPTxtWords: "
							+ vdpTxtWords.toString());
					multiWdSel = true;
				}
			}
		}

		// Here only the selected VDP text field is written with a new entry
		if (!drawMultiWdTxt) {
			if (drawSelVDPTextOnly && selVDPData != null) {
				if (!selVDPData.equals(vdpData) && !multiWdSel)
					return;
			}
		}

		rect = ((RGPTRectangle) vdpData.get("NewBBox")).getRectangle2D();
		// RGPTLogger.logToFile("VDP Text Page RECTANGLE " +
		// pdfPgRect.toString());
		// RGPTLogger.logToFile("NEW Page RECTANGLE " + rect.toString());

		newRect = this.calcScreenBounds(pgInfo, rect);
		// RGPTLogger.logToFile("NEW DEST SCREEN RECTANGLE " +
		// newRect.toString());

		// This defines the margin to be added to the Rectangle while filling.
		// This fill rectangle are the ones identified for entering VDP text.
		int margin = 5;

		// Applying Margins
		desRect = new Rectangle2D.Double(newRect.getX() - margin,
				newRect.getY() - margin, newRect.getWidth() + 1.5 * margin,
				newRect.getHeight() + 1.5 * margin);
		double fillWidth = ((Double) vdpData.get("FillWidth")).doubleValue();
		if (fillWidth > desRect.width) {
			desRect.width = fillWidth;
		}

		// RGPTLogger.logToFile("NEW DEST RECTANGLE " + desRect.toString());

		Graphics2D g2d = origG2D;

		// The VDP Text is highlighted only in Edit Mode
		if (!multiWdSel && !vdpTextFieldInfo.m_IsOverFlowField)
			this.showVDPTxt(g2d, desRect, vdpData, drawSelVDPTextOnly,
					selVDPData, viewMode, selVDPFlds);

		// The Screen Rectangle is used to chack if the X, Y points retrieved
		// during
		// Mouse Click are contained within this VDPData
		vdpData.put("DeviceBBox", RGPTRectangle.getReactangle(newRect));

		boolean isDefaultVDPText = false;
		String vdpText = (String) vdpData.get("UserSetValue");
		boolean blankText = ((Boolean) vdpData.get("BlankDefaultText"))
				.booleanValue();
		// System.out.println("VDP Text in UserSetValue: " + vdpText);

		if (writeDefaultText(vdpData, selVDPData)) {
			if (!blankText) {
				vdpText = vdpTextFieldInfo.m_FieldValue;
				isDefaultVDPText = true;
				if (vdpText == null || vdpText.trim().length() == 0)
					vdpText = (String) vdpData.get("DefaultValue");
			}
		}

		System.out.println("VDP Text Displayed: " + vdpText);
		// Drawing the Set Text with the Font

		Point2D.Double srcPt, desPt = null;
		Font font = vdpTextFieldInfo.m_Font;

		double startX = ((Double) vdpData.get("StartPtX")).doubleValue();
		// double startY = vdpTextFieldInfo.m_StartY;
		double startY = ((Double) vdpData.get("StartPtY")).doubleValue();
		// srcPt = new Point2D.Double(vdpTextFieldInfo.m_StartX,
		// srcPt = new Point2D.Double(startX, startY);
		srcPt = new Point2D.Double(startX, vdpTextFieldInfo.m_StartY);
		desPt = new Point2D.Double();

		AffineTransform origTextMat = vdpTextFieldInfo.m_TextMatrix;
		AffineTransform textMat = (AffineTransform) origTextMat.clone();
		// RGPTLogger.logToFile("Orig Text Mat: " + origTextMat.toString());
		// RGPTLogger.logToFile("New Text Mat: " + textMat.toString());
		// if (startY != vdpTextFieldInfo.m_StartY)

		double transx = ((Double) vdpData.get("Tx")).doubleValue();
		double transy = ((Double) vdpData.get("Ty")).doubleValue();
		textMat.translate(transx, transy);

		// RGPTLogger.logToFile("Orig Text Mat 1: " + origTextMat.toString());
		// RGPTLogger.logToFile("New Text Mat 1: " + textMat.toString());

		AffineTransform finalCTM = null;
		finalCTM = this.getScreenPts(pgInfo, vdpTextFieldInfo.m_ElementCTM,
				textMat, srcPt, desPt);

		// RGPTLogger.logToFile("Orig Text Mat 2: " + origTextMat.toString());
		// RGPTLogger.logToFile("New Text Mat 2: " + textMat.toString());
		AffineTransform vdpTextCTM = (AffineTransform) vdpData
				.get("TextMatrix");
		// RGPTLogger.logToFile("Orig VDPData Text Matrix: " +
		// vdpTextCTM.toString());

		// Calculating the Inverse Transformation. This is used to calculate the
		// Text Page Points from Screen Points
		try {
			AffineTransform invTransform = finalCTM.createInverse();
			// if (!isDefaultVDPText)
			vdpData.put("FinalInvCTM", invTransform);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (desPt == null)
			finalCTM.transform(srcPt, desPt);

		// No further Processing is done if no text is entered
		if (vdpText == null || vdpText.length() == 0)
			return;

		System.out.println("VDP Text enetered by user: " + vdpText);

		// RGPTLogger.logToFile("Again Src Pt: " + srcPt.toString() +
		// " Des Pt: " + desPt.toString());
		// RGPTLogger.logToFile("Again Dev CTM Cocat Page CTM and ElementCTM and Text Met : "
		// + finalCTM.toString());

		// Applying Flip in the Y Dirn
		// AffineTransform(float m00, float m10, float m01, float m11, float
		// m02, float m12)
		// RGPTLogger.logToFile("Translating to Src Pt: " + srcPt.toString());
		finalCTM.translate(srcPt.getX(), srcPt.getY());
		Hashtable fontAttrib = vdpTextFieldInfo.m_DeriveFontAttrib;

		// g2d.drawString(setValue, (float) srcPt.getX(), (float) srcPt.getY());
		int startPos = 0, currPos = 0;
		if (vdpTextMode == StaticFieldInfo.WORD
				|| vdpTextMode == StaticFieldInfo.LINE) {
			boolean isTextScaled = false;
			font = font.deriveFont(finalCTM);
			fontAttrib.put(TextAttribute.FONT, font);
			// vdpText = (String) vdpData.get("UserSetValue");
			// if (!vdpTextFieldInfo.m_FieldLengthFixed)

			VDPTextFieldInfo overFlowVDPTextField = null;
			if (vdpTextFieldInfo.m_OverFlowVDPField != null
					&& vdpTextFieldInfo.m_OverFlowVDPField.length() > 0) {
				VDPFieldInfo vdpFld = pgInfo.getVDPFieldInfo("Text",
						vdpTextFieldInfo.m_OverFlowVDPField);
				if (vdpFld != null)
					overFlowVDPTextField = (VDPTextFieldInfo) vdpFld;
			}
			if (vdpTextFieldInfo.m_AutoFitText) {
				currPos = this.getVDPText4BBox(g2d, vdpText, vdpData,
						vdpTextMode, startPos, newRect, fontAttrib, false);
			} else if (overFlowVDPTextField == null
					&& !vdpTextFieldInfo.m_IsOverFlowField) {
				vdpText = this.getVDPText4FieldLength(vdpText, vdpData, font);
			}
			double percMargin = 0.01, fontScale = 1.0, sx = 1.0, sy = 1.0;
			sx = vdpTextFieldInfo.m_TextMatrix.getScaleX();
			sy = vdpTextFieldInfo.m_TextMatrix.getScaleY();
			double shx = vdpTextFieldInfo.m_TextMatrix.getShearX();
			double shy = vdpTextFieldInfo.m_TextMatrix.getShearY();
			double tx = vdpTextFieldInfo.m_TextMatrix.getTranslateX();
			double ty = vdpTextFieldInfo.m_TextMatrix.getTranslateY();
			// double tx = rect.getX();
			// double ty = rect.getY();
			AffineTransform textCTM = (AffineTransform) vdpData
					.get("TextMatrix");
			// textCTM.translate(transx, transy);
			// System.out.println("Initial Sx: " + sx + " sy: " + sy);
			// This loop is to reduce the Font Size to fit the Text
			// RGPTLogger.logToFile("Curr Pos: " + currPos + " VDPText Length: "
			// +
			// vdpText.length());
			// RGPTLogger.logToFile("Orig VDPDATA Text Matrix: " +
			// textCTM.toString());
			boolean fitTextInBBox = false;
			while (true) {
				if (!vdpTextFieldInfo.m_AutoFitText)
					break;
				if (overFlowVDPTextField != null)
					break;
				if (isDefaultVDPText)
					break;
				if (currPos >= vdpText.length())
					break;
				// Setting the Scale
				fitTextInBBox = true;
				fontScale = fontScale - percMargin;
				sx = sx * fontScale;
				sy = sy * fontScale;
				// RGPTLogger.logToFile("In While Loop, Scale Mult: " +
				// fontScale +
				// " Resetting Sx: " + sx + " sy: " + sy);
				textCTM.setTransform(sx, shy, shx, sy, tx, ty);
				// RGPTLogger.logToFile("Updated VDPData Text Matrix: " +
				// textCTM.toString());
				// textCTM.translate(transx, transy);
				// vdpTextFieldInfo.m_TextMatrix.scale(sx, sy);
				finalCTM = this.getScreenPts(pgInfo,
						vdpTextFieldInfo.m_ElementCTM, textCTM, srcPt, desPt);
				try {
					AffineTransform invTransform = finalCTM.createInverse();
					if (!isDefaultVDPText)
						vdpData.put("FinalInvCTM", invTransform);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// This is executed if the Text Matrix has changed because of
				// scaling.
				// Since the text is scaled new start point needs to be
				// calculated.
				this.calcTextStartPt(vdpData, newRect);
				startX = ((Double) vdpData.get("StartPtX")).doubleValue();
				// startY = ((Double) vdpData.get("StartPtY")).doubleValue();
				srcPt = new Point2D.Double(startX, vdpTextFieldInfo.m_StartY);
				// srcPt = new Point2D.Double(startX, startY);
				finalCTM.translate(srcPt.getX(), srcPt.getY());
				font = font.deriveFont(finalCTM);
				fontAttrib.put(TextAttribute.FONT, font);
				currPos = this.getVDPText4BBox(g2d, vdpText, vdpData,
						vdpTextMode, startPos, newRect, fontAttrib, false);
				isTextScaled = true;
			}
			/*
			 * if (isTextScaled) { this.calcTextStartPt(vdpData, newRect);
			 * startX = ((Double) vdpData.get("StartPtX")).doubleValue(); srcPt
			 * = new Point2D.Double(startX, vdpTextFieldInfo.m_StartY);
			 * finalCTM.translate(srcPt.getX(), srcPt.getY()); }
			 */
			if (!fitTextInBBox)
				vdpData.put("TextMatrix",
						(AffineTransform) vdpTextFieldInfo.m_TextMatrix.clone());
			if (vdpTextFieldInfo.m_AutoFitText)
				vdpText = vdpText.substring(startPos, currPos);
			// System.out.println("Final VDP Text to be drawn: " + vdpText);
			if (!isDefaultVDPText) {
				vdpData.put("UserSetValue", vdpText);
				vdpData.put("FinalCTM", finalCTM);
				// Steting the Text Area with the New String
				if (!vdpTextFieldInfo.m_IsOverFlowField && vdpTextArea != null
						&& selVDPData != null && selVDPData.equals(vdpData)
						&& !vdpText.equals(vdpTextArea.getText())) {
					vdpTextArea.setText(vdpText);
				}
			}

			String origText = "", overFlowTxt = "";
			g2d.setPaint(new Color(vdpTextFieldInfo.m_FillColor));
			String allign = (String) vdpData.get("TextAllignment");
			int formatType = vdpTextFieldInfo.m_TextFormatType;
			String formatSpec = vdpTextFieldInfo.m_TextFormatValue;
			if (!isDefaultVDPText) {
				if (!vdpTextFieldInfo.m_IsOverFlowField) {
					if (vdpTextFieldInfo.m_UseTitleCase)
						vdpText = RGPTUtil.toTitleCase(vdpText);
					vdpText = TextFormatter.getFormattedText(vdpText,
							formatType, formatSpec);
					vdpText = vdpTextFieldInfo.m_PrefixValue + vdpText;
					if (vdpTextFieldInfo.m_SuffixValue != null
							&& vdpTextFieldInfo.m_SuffixValue.length() > 0)
						vdpText = vdpText + vdpTextFieldInfo.m_SuffixValue;
				}
				origText = vdpText;
				startPos = 0;
				currPos = 0;
				if (overFlowVDPTextField != null) {
					vdpText = RGPTUtil.getTextForWidth(newRect.getWidth(),
							vdpText, font);
					if (!origText.equals(vdpText)) {
						Vector<String> splitTxts = RGPTUtil
								.getSplitedText(origText);
						// System.out.println("Split Txts: " + splitTxts);
						vdpText = RGPTUtil.getTextForWidth(splitTxts,
								newRect.getWidth(), font);
						overFlowTxt = origText.substring(vdpText.length(),
								origText.length());
					}
					RGPTLogger.logToFile("Checking Overflow, VDPText: "
							+ vdpText + "  And overFlowTxt: " + overFlowTxt);
				}

				vdpData.put("UserSetFinalValue", vdpText);
			}

			vdpData.put("DerivedFont", font);
			java.awt.FontMetrics fm = g2d.getFontMetrics(font);
			Rectangle2D textRect = fm.getStringBounds(vdpText, g2d);
			if (multiWdSel && isDefaultVDPText) {
				String vdpMultText = vdpTextFieldInfo.m_PrefixValue + vdpText;
				if (vdpTextFieldInfo.m_SuffixValue != null
						&& vdpTextFieldInfo.m_SuffixValue.length() > 0)
					vdpMultText = vdpMultText + vdpTextFieldInfo.m_SuffixValue;
				Rectangle2D multWordRect = fm.getStringBounds(vdpMultText, g2d);
				if (multWordRect.getWidth() > newRect.width)
					vdpData.put("TextRectWt", multWordRect.getWidth());
				else
					vdpData.put("TextRectWt", newRect.width);
			} else {
				if (textRect.getWidth() > newRect.width)
					vdpData.put("TextRectWt", textRect.getWidth());
				else
					vdpData.put("TextRectWt", newRect.width);
			}
			double textWidth = textRect.getWidth();
			double panelWidth = newRect.getWidth();
			if (textWidth > panelWidth)
				vdpData.put("FillWidth", textWidth);
			Color txtColor = new Color(vdpTextFieldInfo.m_FillColor);
			int scale = (int) (pgInfo.m_Scale * 100);
			if (allign.equals(PDFPageHandler.ALLIGN_LEFT)) {
				if (drawSelVDPTextOnly) {
					// This part of the code displays the text during edit with
					// larger Font.
					// This helps the user to see the text clearly.
					if (scale < 60 && fm.getHeight() < 12) {
						// System.out.println("Font Height: " + fm.getHeight());
						// System.out.println("Font Size: " + font.getSize());
						font = font.deriveFont((new Float(20)).floatValue());
						fm = g2d.getFontMetrics(font);
						textRect = fm.getStringBounds(vdpText, g2d);
					}
					Rectangle2D.Double fillRect = new Rectangle2D.Double();
					fillRect.x = desRect.x - margin;
					fillRect.y = desRect.y - margin;
					fillRect.height = desRect.height + 1.25 * margin;
					if (textRect.getWidth() > desRect.width) {
						fillRect.width = textRect.getWidth() + 1.75 * margin;
						vdpData.put("FillWidth", fillRect.width);
					} else
						fillRect.width = desRect.width + 1.75 * margin;
					this.drawRect(g2d, fillRect, txtColor, vdpData);
				}
				g2d.setFont(font);
				g2d.setPaint(new Color(vdpTextFieldInfo.m_FillColor));
				if (!multiWdSel) {
					g2d.drawString(vdpText, (float) srcPt.getX(),
							(float) srcPt.getY());
					vdpData.put("VDPStartPtX", new Float((float) srcPt.getX()));
					vdpData.put("VDPStartPtY", new Float((float) srcPt.getY()));
					// String msg = "Recording VDPStartPt: "+srcPt;
					// msg += "\nfor Field: " + vdpData.get("VDPFieldName");
					// msg += " VDP Text Entered: " + vdpText;
					// RGPTLogger.logToFile(msg);
					// System.out.println(msg);
				}
				if (overFlowVDPTextField != null && overFlowTxt.length() > 0) {
					pdfPgRect = overFlowVDPTextField.m_PageRectangle;
					HashMap overFlowVDPData = userPgData.getVDPData("Text",
							pdfPgRect);
					overFlowVDPData.put("UserSetValue", overFlowTxt);
					this.createGraphicsForText(origG2D, pgInfo,
							overFlowVDPTextField, userPgData, multiWordSel,
							drawSelVDPTextOnly, overFlowVDPData,
							drawMultiWdTxt, viewMode, selVDPFlds, vdpTextArea);
				}
				return;
			}

			// This is if the Text is Alligned Center or Right
			double startx = 0.0;

			// RGPTLogger.logToFile("Rect BBox: " + desRect.toString() +
			// "\nText Rectangle: " + textRect.toString());
			// RGPTLogger.logToFile("VDP Text FIRST: " + vdpText +
			// " Text Width: " + textWidth +
			// " Orig Panel Width: " +
			// vdpTextFieldInfo.m_PageRectangle.getWidth() +
			// " Trans Panel Width: " + panelWidth);

			// Center or Right Allign text horizontally
			// if (vdpTextFieldInfo.m_TextAllignment.equals("CENTER"))
			if (allign.equals(PDFPageHandler.ALLIGN_CENTER))
				startx = (panelWidth - textWidth) / 2;
			// else if (vdpTextFieldInfo.m_TextAllignment.equals("RIGHT"))
			else if (allign.equals(PDFPageHandler.ALLIGN_RIGHT))
				startx = (panelWidth - textWidth);
			RGPTLogger.logToFile("Text Field: " + vdpData.get("VDPFieldName")
					+ " vdpText: " + vdpText + " drawSelVDPTextOnly: "
					+ drawSelVDPTextOnly + " StartX for drawString: " + startx
					+ " and starty: " + srcPt.getY(), RGPTLogger.DEBUG_LOG);
			if (drawSelVDPTextOnly) {
				// This part of the code displays the text during edit with
				// larger Font.
				// This helps the user to see the text clearly.
				if (scale < 60 && fm.getHeight() < 12) {
					// System.out.println("Font Height: " + fm.getHeight());
					// System.out.println("Font Size: " + font.getSize());
					font = font.deriveFont((new Float(20)).floatValue());
					fm = g2d.getFontMetrics(font);
					textRect = fm.getStringBounds(vdpText, g2d);
					if (allign.equals(PDFPageHandler.ALLIGN_CENTER))
						startx = (panelWidth - textRect.getWidth()) / 2;
					// else if
					// (vdpTextFieldInfo.m_TextAllignment.equals("RIGHT"))
					else if (allign.equals(PDFPageHandler.ALLIGN_RIGHT))
						startx = (panelWidth - textRect.getWidth());
				}
				Rectangle2D.Double fillRect = new Rectangle2D.Double();
				if (startx < 0)
					fillRect.x = desRect.x - Math.abs(startx) - margin;
				else
					fillRect.x = desRect.x - margin;
				fillRect.y = desRect.y - margin;
				fillRect.height = desRect.height + 1.25 * margin;
				if (textRect.getWidth() > desRect.width)
					fillRect.width = textRect.getWidth() + 1.75 * margin;
				else
					fillRect.width = desRect.width + 1.75 * margin;
				this.drawRect(g2d, fillRect, txtColor, vdpData);
			}
			g2d.setFont(font);
			g2d.setPaint(new Color(vdpTextFieldInfo.m_FillColor));
			if (!multiWdSel) {
				g2d.drawString(vdpText, (float) startx, (float) srcPt.getY());
				vdpData.put("VDPStartPtX", new Float((float) startx));
				vdpData.put("VDPStartPtY", new Float((float) srcPt.getY()));
				// System.out.println("Startx for VDP Text: " + startx);
			}
			if (overFlowVDPTextField != null && overFlowTxt.length() > 0) {
				pdfPgRect = overFlowVDPTextField.m_PageRectangle;
				HashMap overFlowVDPData = userPgData.getVDPData("Text",
						pdfPgRect);
				overFlowVDPData.put("UserSetValue", overFlowTxt);
				// RGPTLogger.logToFile("Process Overflow Field: " +
				// overFlowVDPData.get("VDPFieldName") +
				// " And overFlowTxt: " + overFlowTxt);
				this.createGraphicsForText(origG2D, pgInfo,
						overFlowVDPTextField, userPgData, multiWordSel,
						drawSelVDPTextOnly, overFlowVDPData, drawMultiWdTxt,
						viewMode, selVDPFlds, vdpTextArea);
			}
			return;
		} else if (vdpTextMode == StaticFieldInfo.PARA) {
			HashMap lineAttrs = null;
			Rectangle2D.Double lineRect = null, calcLineRect = null;
			String origText = (String) vdpData.get("UserSetValue");
			if (origText == null)
				return;
			System.out.println("Length of VDP Text: " + origText.length());
			System.out.println("Number of Lines: "
					+ vdpTextFieldInfo.m_VDPLineAttrList.size());
			vdpData.put("LineAttributes", vdpTextFieldInfo.m_VDPLineAttrList);
			for (int i = 0; i < vdpTextFieldInfo.m_VDPLineAttrList.size(); i++) {
				lineAttrs = (HashMap) vdpTextFieldInfo.m_VDPLineAttrList
						.elementAt(i);
				// This is used to remove the vdp text set on Lines for which no
				// user
				// input text exists. This happens when all the lines of the
				// para
				// were orginally written and some were removed
				int currLine = i + 1;
				if (currPos >= origText.length()) {
					System.out.println("Setting empty text on line: "
							+ currLine);
					lineAttrs.put("UserSetValue", "");
					continue;
				}
				lineRect = ((RGPTRectangle) lineAttrs.get("LineBBox"))
						.getRectangle2D();
				calcLineRect = this.calcScreenBounds(pgInfo, lineRect);
				srcPt = new Point2D.Double(
						((Double) lineAttrs.get("StartX")).doubleValue(),
						((Double) lineAttrs.get("StartY")).doubleValue());

				finalCTM = this.getScreenPts(pgInfo,
						(AffineTransform) lineAttrs.get("ElementCTM"),
						(AffineTransform) lineAttrs.get("TextMatrix"), srcPt,
						desPt);
				font = vdpTextFieldInfo.m_Font;
				font = font.deriveFont(finalCTM);
				int txtColor = vdpTextFieldInfo.m_FillColor;
				if (lineAttrs.get("FillColor") != null)
					txtColor = ((Integer) lineAttrs.get("FillColor"))
							.intValue();
				g2d.setPaint(new Color(txtColor));
				g2d.setFont(font);
				font = g2d.getFont();
				fontAttrib.put(TextAttribute.FONT, font);
				fontAttrib.put(TextAttribute.FOREGROUND, new Color(txtColor));
				currPos = this.getVDPText4BBox(g2d,
						(String) vdpData.get("UserSetValue"), vdpData,
						vdpTextMode, startPos, calcLineRect, fontAttrib, true);
				System.out.println("Start Pos: " + startPos + " :Curr Pos: "
						+ currPos);
				if (origText.charAt(startPos) == '\n')
					startPos = startPos + 1;
				vdpText = origText.substring(startPos, currPos);
				lineAttrs.put("UserSetValue", vdpText);
				g2d.drawString(vdpText, (float) srcPt.getX(),
						(float) srcPt.getY());
				if (currLine == vdpTextFieldInfo.m_VDPLineAttrList.size()
						&& currPos < origText.length()) {
					startPos = 0;
					vdpText = origText.substring(startPos, currPos);
					System.out.println("Resetting VDP Text: "
							+ vdpText.length());
					vdpData.put("UserSetValue", vdpText);
					// vdpText = (String) \.get("UserSetValue");
					System.out.println("VDP Text to write on screen: "
							+ vdpText);
					if (selVDPData.equals(vdpData)
							&& !vdpText.equals(vdpTextArea.getText())) {
						vdpTextArea.setText(vdpText);
					}
				}
				startPos = currPos;
			}
		}
	}

	private void createGraphicsForTextOnGraphics(Graphics2D origG2D,
			PDFPageInfo pgInfo, VDPTextFieldInfo vdpTextFieldInfo,
			UserPageData userPgData, boolean drawSelVDPTextOnly,
			HashMap selVDPData, int viewMode, Vector selVDPFlds,
			JTextArea vdpTextArea) {
		// Initalizing the different Rectangle to be drawn on the Screen
		Rectangle2D.Double rect, pdfPgRect, newRect, desRect;
		String text = vdpTextFieldInfo.m_FieldValue;
		pdfPgRect = vdpTextFieldInfo.m_PageRectangle;
		RGPTRectangle lineBBox = null;
		// RGPTLogger.logToFile("OLD RECTANGLE " + rect.toString());

		// Getting the User Set Data and Drawing the String with the specified
		// Font
		HashMap vdpData = null;
		vdpData = userPgData.getVDPData("TextOnGraphics", pdfPgRect);
		if (vdpData == null)
			throw new RuntimeException("VDP Data not Created");

		int vdpTextMode = vdpTextFieldInfo.m_VDPTextMode;
		lineBBox = vdpTextFieldInfo.m_LineBBox;
		Vector<HashMap> vdpTxtWords = null;
		rect = ((RGPTRectangle) vdpData.get("NewBBox")).getRectangle2D();
		newRect = this.calcScreenBounds(pgInfo, rect);

		// RGPTLogger.logToFile("VDP Text Page RECTANGLE " +
		// pdfPgRect.toString());
		// RGPTLogger.logToFile("NEW Page RECTANGLE " + rect.toString());
		// RGPTLogger.logToFile("NEW DEST SCREEN RECTANGLE " +
		// newRect.toString());

		// This defines the margin to be added to the Rectangle while filling.
		// This fill rectangle are the ones identified for entering VDP text.
		int margin = 5;

		// Applying Margins
		desRect = new Rectangle2D.Double(newRect.getX() - margin,
				newRect.getY() - margin, newRect.getWidth() + 1.5 * margin,
				newRect.getHeight() + 1.5 * margin);
		double fillWidth = ((Double) vdpData.get("FillWidth")).doubleValue();
		if (fillWidth > desRect.width) {
			desRect.width = fillWidth;
		}

		// RGPTLogger.logToFile("NEW DEST RECTANGLE " + desRect.toString());

		Graphics2D g2d = origG2D;

		int shapeType = vdpTextFieldInfo.m_ShapeType;
		String fldType = vdpTextFieldInfo.m_FieldType;
		RGPTLogger.logToFile("Fld Type: " + fldType + " Shape Type: "
				+ shapeType);

		// The VDP Text is highlighted only in Edit Mode only when text are
		// drawn in Rect
		boolean isFieldFixed = vdpTextFieldInfo.m_IsFieldFixed;
		if (!isFieldFixed && shapeType == -1)
			this.showVDPTxt(g2d, desRect, vdpData, drawSelVDPTextOnly,
					selVDPData, viewMode, selVDPFlds);

		// The Screen Rectangle is used to chack if the X, Y points retrieved
		// during
		// Mouse Click are contained within this VDPData
		vdpData.put("DeviceBBox", RGPTRectangle.getReactangle(newRect));
		boolean isDefaultVDPText = false;
		String vdpText = (String) vdpData.get("UserSetValue");
		boolean blankText = ((Boolean) vdpData.get("BlankDefaultText"))
				.booleanValue();
		RGPTLogger.logToFile("VDP Text in UserSetValue: " + vdpText);

		// Retrieving all the Required Fields to draw Text
		int fontClr = vdpTextFieldInfo.m_FillColor;
		Vector<Point2D.Double> pgPts = vdpTextFieldInfo.m_GraphicPathPoints;
		Vector<Point2D.Double> shPts = null;
		shPts = this.calcScreenPoints(pgInfo, pgPts);

		if (!isFieldFixed) {
			if (writeDefaultText(vdpData, selVDPData)) {
				if (!blankText) {
					vdpText = vdpTextFieldInfo.m_FieldValue;
					isDefaultVDPText = true;
				}
			}
		}

		// RGPTLogger.logToFile("VDP Text Displayed: " + vdpText);
		// Drawing the Set Text with the Font

		Font font = vdpTextFieldInfo.m_Font;

		// No further Processing is done if no text is entered
		if (vdpText == null || vdpText.length() == 0)
			vdpText = "";
		if (!isFieldFixed) {
			if (vdpText == null || vdpText.trim().length() == 0)
				return;
		}
		// RGPTLogger.logToFile("VDP Text enetered by user: " + vdpText);
		Hashtable fontAttrib = vdpTextFieldInfo.m_DeriveFontAttrib;
		// font = font.deriveFont((float) vdpTextFieldInfo.m_FontSize);
		font = font.deriveFont(fontAttrib);
		// RGPTLogger.logToFile("Final VDP Text to be drawn: " + vdpText);
		// RGPTLogger.logToFile("Font Color: " + java.awt.Color.BLACK.getRGB());
		if (!isDefaultVDPText) {
			vdpData.put("UserSetValue", vdpText);
			// Steting the Text Area with the New String
			if (selVDPData != null && selVDPData.equals(vdpData)
					&& !vdpText.equals(vdpTextArea.getText())) {
				vdpTextArea.setText(vdpText);
			}
		}

		if (!isDefaultVDPText && vdpText.length() > 0) {
			if (vdpTextFieldInfo.m_UseTitleCase)
				vdpText = RGPTUtil.toTitleCase(vdpText);
			int formatType = vdpTextFieldInfo.m_TextFormatType;
			String formatSpec = vdpTextFieldInfo.m_TextFormatValue;
			vdpText = TextFormatter.getFormattedText(vdpText, formatType,
					formatSpec);
			vdpText = vdpTextFieldInfo.m_PrefixValue + vdpText;
			if (vdpTextFieldInfo.m_SuffixValue != null
					&& vdpTextFieldInfo.m_SuffixValue.length() > 0)
				vdpText = vdpText + vdpTextFieldInfo.m_SuffixValue;
			vdpData.put("UserSetFinalValue", vdpText);
		}

		float fontSz = (float) vdpTextFieldInfo.m_FontSize;
		boolean fitTxt = ((Boolean) vdpData.get("AutoFitText")).booleanValue();
		double panelWt = newRect.width, panelHt = newRect.height;

		if (fitTxt && shapeType == -1 && vdpText.length() > 0) {
			fontSz = RGPTUIUtil.getActualFontSize(font, fontSz, vdpText,
					panelWt, panelHt);
			font = font.deriveFont(fontSz);
			// System.out.println("Final Font Sz: " + fontSz);
		}
		vdpData.put("DerivedFont", font);

		java.awt.FontMetrics fm = g2d.getFontMetrics(font);
		Rectangle2D textRect = fm.getStringBounds(vdpText, g2d);
		// RGPTLogger.logToFile("Text Width: " + textRect.getWidth() +
		// " Pg Rect Width: " + pdfPgRect.width +
		// " Dev Rect Width: " + newRect.width);
		if (textRect.getWidth() > newRect.width) {
			vdpData.put("TextRectWt", textRect.getWidth());
			vdpData.put("FillWidth", textRect.getWidth());
		} else
			vdpData.put("TextRectWt", newRect.width);
		// RGPTLogger.logToFile("Shape Points Outside: " + shPts);

		HashMap result = null;
		try {
			boolean useLowResImage = false;
			result = ImagePersonalizationEngine.createPersonalizedImage(
					vdpText, font, RGPTUtil.getRectangle(newRect), vdpData,
					null, useLowResImage, shPts);
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		g2d.setPaint(new Color(vdpTextFieldInfo.m_FillColor));

		Shape txtOnShape = null, relTxtOnShape = null;
		if (shapeType != -1) {
			txtOnShape = (Shape) result.get("FinalShape");
			RGPTLogger.logToFile("Shape0 " + RGPTUtil.printShape(txtOnShape));
		}

		Polygon poly = (Polygon) result.get("FinalPolygon");
		AffineTransform affine = (AffineTransform) result.get("FinalTransform");
		Rectangle bounds = poly.getBounds();
		if (shapeType != -1) {
			desRect = (Rectangle2D.Double) RGPTUtil
					.getRectangle2D((Rectangle) result.get("FinalDisplayRect"));
			if (!isFieldFixed) {
				this.showVDPTxt(g2d, desRect, vdpData, drawSelVDPTextOnly,
						selVDPData, viewMode, selVDPFlds);
				g2d.draw(txtOnShape);
			}
			// g2d.setTransform(new AffineTransform());
		}

		java.awt.image.BufferedImage textImg = null, pdfTxtImg = null;
		java.awt.Image rotTextImg = (java.awt.Image) result.get("FinalImage");
		textImg = ImageUtils.ScaleToSize(rotTextImg, -1, -1);
		vdpData.put("UserSetImage", textImg);
		vdpData.put("Polygon", poly);
		AffineTransform finDevCTM = (AffineTransform) pgInfo.m_FinalDeviceCTM
				.clone();
		vdpData.put("FinalDevCTM", finDevCTM);
		/*
		 * if (vdpData.get("TextImgToFile") == null) { String tempFilePath =
		 * "../PDF_Files/"; tempFilePath = tempFilePath + "TextInRectImage.png";
		 * ImageUtils.SaveImage(textImg, tempFilePath, "PNG");
		 * vdpData.put("TextImgToFile", true); }
		 */
		// RGPTLogger.logToFile("Image Data. W: " + textImg.getWidth() +
		// " H: " + textImg.getHeight());
		// textImg = ImageUtils.scaleImage(textImg,new
		// Dimension((int)newRect.getWidth(),
		// (int)newRect.getHeight()));
		if (drawSelVDPTextOnly) {
			Rectangle2D.Double fillRect = new Rectangle2D.Double();
			fillRect.x = desRect.x - margin;
			fillRect.y = desRect.y - margin;
			fillRect.height = desRect.height + 1.25 * margin;
			if (textRect.getWidth() > desRect.width) {
				fillRect.width = textRect.getWidth() + 1.75 * margin;
				vdpData.put("FillWidth", fillRect.width);
			} else
				fillRect.width = desRect.width + 1.75 * margin;
			this.drawRect(g2d, fillRect, new Color(fontClr), vdpData);
		}
		// boolean isSuccess = g2d.drawImage(textImg, (int) newRect.getX(),
		// (int) newRect.getY(), null);
		boolean isSuccess = g2d.drawImage(textImg, bounds.x, bounds.y, null);
		RGPTLogger.logToFile("Image Success Result " + isSuccess);
	}

	// This method is called by TextOnGraphics
	private void drawRect(Graphics2D g2d, Rectangle2D.Double fillRect,
			Color txtColor, HashMap vdpData) {
		RGPTLogger.logToFile("Fill Rectangle: " + fillRect.toString());
		g2d.setPaint(Color.RED);
		BasicStroke basicStroke = new BasicStroke(2.0f);
		g2d.setStroke(basicStroke);

		Shape s = fillRect;
		double rotAng = ((Double) vdpData.get("Rotation")).doubleValue();
		double shx = ((Double) vdpData.get("ShearX")).doubleValue();
		double shy = ((Double) vdpData.get("ShearY")).doubleValue();
		if (rotAng != 0.0 || shx != 0 || shy != 0) {
			AffineTransform affine = new AffineTransform();
			java.awt.Polygon poly = RGPTUtil.getTransformPolygon(affine,
					fillRect, rotAng, shx, shy, true);
			s = poly;
		}

		g2d.draw(s);
		RGPTLogger.logToFile("Text Color is: " + txtColor.toString() + " RGB: "
				+ txtColor.getRGB());
		RGPTLogger.logToFile("Red: " + txtColor.getRed() + " Green: "
				+ txtColor.getGreen() + " Blue: " + txtColor.getBlue());
		if (Color.WHITE.equals(txtColor)
				|| (txtColor.getRed() >= 200 && txtColor.getGreen() >= 200 && txtColor
						.getBlue() >= 200)) {
			RGPTLogger.logToFile("Both Colors Same");
			g2d.setColor(Color.BLACK);
		} else
			g2d.setColor(Color.WHITE);
		g2d.fill(s);
	}

	private void drawRect(Graphics2D g2d, Rectangle2D.Double rect,
			HashMap vdpData, boolean showRectPts, HashMap selVDPData,
			int viewMode, Vector selVDPFields) {
		BasicStroke basicStroke = new BasicStroke(1.0f);
		;
		g2d.setPaint(Color.BLACK);
		if (viewMode == EDIT_MODE) {
			if (selVDPData != null && selVDPData.equals(vdpData)) {
				g2d.setPaint(Color.RED);
				basicStroke = new BasicStroke(2.0f);
			}
		} else if (viewMode == SELECT_MODE) {
			if (selVDPFields != null && selVDPFields.size() > 0
					&& selVDPFields.contains(vdpData)) {
				g2d.setPaint(Color.RED);
				basicStroke = new BasicStroke(2.0f);
			}
		} else
			basicStroke = new BasicStroke(1.0f);
		g2d.setStroke(basicStroke);
		Shape s = rect;
		double rotAng = ((Double) vdpData.get("Rotation")).doubleValue();
		double shx = ((Double) vdpData.get("ShearX")).doubleValue();
		double shy = ((Double) vdpData.get("ShearY")).doubleValue();
		if (rotAng != 0.0 || shx != 0 || shy != 0) {
			AffineTransform affine = new AffineTransform();
			java.awt.Polygon poly = RGPTUtil.getTransformPolygon(affine, rect,
					rotAng, shx, shy, true);
			RGPTLogger.logToFile("Polygonal Data: " + poly.toString());
			s = poly;
		}
		g2d.draw(s);
		Color col = definecolor();
		g2d.setColor(col);
		g2d.fill(s);
		if (!showRectPts)
			return;
		// Drawing the 4 points of the rectangle
		g2d.setPaint(Color.RED);
		basicStroke = new BasicStroke(2.0f);
		double size = 4.0;
		Rectangle2D.Double ptRect = new Rectangle2D.Double();
		ptRect.setRect(rect.getX() - (size / 2), rect.getY() - (size / 2),
				size, size);
		g2d.draw(ptRect);
		ptRect.setRect(rect.getX() - (size / 2), rect.getY() + rect.getHeight()
				- (size / 2), size, size);
		g2d.draw(ptRect);
		ptRect.setRect(rect.getX() + rect.getWidth() - (size / 2), rect.getY()
				- (size / 2), size, size);
		g2d.draw(ptRect);
		ptRect.setRect(rect.getX() + rect.getWidth() - (size / 2), rect.getY()
				+ rect.getHeight() - (size / 2), size, size);
		g2d.draw(ptRect);
	}

	private String getVDPText4FieldLength(String vdpTxt, HashMap vdpData,
			Font font) {
		boolean fldLthFixed = ((Boolean) vdpData.get("FieldLengthFixed"))
				.booleanValue();
		boolean txtWdthFixed = ((Boolean) vdpData.get("TextWidthFixed"))
				.booleanValue();
		int fldLth = Integer.parseInt((String) vdpData.get("FieldLength"));
		if (fldLthFixed) {
			String prefixVal = (String) vdpData.get("PrefixValue");
			String suffixVal = (String) vdpData.get("SuffixValue");
			fldLth = fldLth - prefixVal.length() - suffixVal.length();
			if (vdpTxt.length() > fldLth)
				vdpTxt = vdpTxt.substring(0, fldLth);
		} else if (txtWdthFixed) {
			RGPTRectangle devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
			java.awt.FontMetrics fm = Toolkit.getDefaultToolkit()
					.getFontMetrics(font);
			String prefixVal = (String) vdpData.get("PrefixValue");
			String suffixVal = (String) vdpData.get("SuffixValue");
			String text = prefixVal + vdpTxt + suffixVal;
			int width = fm.stringWidth(text);
			while (true) {
				if (width < devBBox.width)
					break;
				vdpTxt = vdpTxt.substring(0, vdpTxt.length() - 1);
				text = prefixVal + vdpTxt + suffixVal;
				width = fm.stringWidth(text);
			}
		}

		return vdpTxt;
	}

	private int getVDPText4BBox(Graphics2D g2d, String vdpText,
			HashMap vdpData, int vdpTextMode, int startPos,
			Rectangle2D.Double rectBBox, Hashtable fontAttrib,
			Boolean isParaSelection) {
		// String vdpText = (String) vdpData.get("UserSetValue");
		AttributedString vdpTextString = new AttributedString(vdpText,
				fontAttrib);
		AttributedCharacterIterator charIter = vdpTextString.getIterator();
		int paragraphStart = charIter.getBeginIndex();
		int paragraphEnd = charIter.getEndIndex();
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(charIter,
				g2d.getFontRenderContext());

		// This Margin is provided to avoid text overlap. If 1.0 No Margin set.
		double margin = 1.0;
		double formatWidth = rectBBox.getWidth() * margin;

		// Get line offset from lineMeasurer until the entire paragraph has been
		// displayed. This is added to UserSetValue to get the Exact Text during
		// PDF Generation
		int currPos = 0;
		String userSelText;
		if (startPos >= paragraphEnd)
			return -1;
		lineMeasurer.setPosition(startPos);
		while (lineMeasurer.getPosition() < paragraphEnd) {
			// Retrieve next layout.
			lineMeasurer.setPosition(startPos);
			currPos = lineMeasurer.nextOffset((float) formatWidth);
			// userSelText = vdpText.substring(startPos, currPos);
			System.out.println("Text Start: " + startPos + " :CurrPos: "
					+ currPos + " VDP Text Length: " + vdpText.length());
			break;
		}

		// The New Lines are supported only when the VDP Text Mode is Para.
		if (!isParaSelection) {
			// if (currPos < vdpText.length()) {
			// System.out.println("Current pos: " + currPos + " is less " +
			// "then VDP Text Length: " + vdpText.length());
			// //currPos = vdpText.length()-1;
			// }
			return currPos;
		}
		// Checking for New Line
		int charIndex = 0;
		// lineMeasurer.setPosition(paragraphStart);
		for (char c = charIter.first(); c != charIter.DONE; c = charIter.next()) {
			charIndex = charIter.getIndex();
			if (charIndex <= startPos)
				continue;
			if (charIndex >= currPos) {
				return currPos;
			}
			if (c == '\n') {
				System.out
						.println("Found New Line at Char Index: " + charIndex);
				return charIndex;
			}
		}

		return currPos;
	}

	private void processVDPImageFields(Graphics2D g2d, PDFPageInfo pgInfo,
			UserPageData userPgData, HashMap selVDPData, int viewMode) {
		VDPUIHolder vdpUIHolder = null;
		VDPImageFieldInfo vdpImageFieldInfo = null;
		Rectangle2D.Double rect = null, newRect = null;

		Vector vdpImageFields = pgInfo.m_VDPImageFieldInfo;

		// Getting the User Set Data and Drawing the String with the specified
		// Font
		HashMap vdpData = null;
		boolean setClip = true;

		// Retrieve the Clip set for this Graphics Area
		Shape gClip = g2d.getClip();
		// System.out.println("Graphic Clip Path: " + gClip.toString());

		// Processing first the Background Image Field
		if (pgInfo.m_HasBackgroundImage) {
			vdpImageFieldInfo = pgInfo.m_BackgroundImageField;
			rect = vdpImageFieldInfo.m_PageRectangle;
			newRect = this.calcScreenBounds(pgInfo, rect);
			vdpData = userPgData.getVDPData("Image", rect);
			if (vdpData == null)
				throw new RuntimeException("VDP Data not Created");
			System.out
					.println("Drawing Background for VDP Image DATA Field Name: "
							+ (String) vdpData.get("VDPFieldName"));
			System.out.println("VDP Image BBox: "
					+ (((RGPTRectangle) vdpData.get("BBox")).getRectangle2D())
							.toString());
			BufferedImage image = (BufferedImage) vdpData.get("UserSetImage");
			if (m_EditImageArea) {
				image = getScaledSubImage(vdpData);
			}
			if (image != null) {
				float alpha = (float) vdpImageFieldInfo.m_ImageAlphaValue;
				// Convert to transperent Image
				System.out.println("Prior Transperent Image apha value: "
						+ alpha);
				if (vdpImageFieldInfo.m_IsBackgroundImage && alpha == 1.0)
					alpha = (float) 0.5;
				System.out.println("Transperent Image apha value: " + alpha);
				if (alpha != 1.0)
					image = ImageUtils.ConvToTransparentImage(image, alpha);
				boolean isSuccess = g2d.drawImage(image, (int) newRect.getX(),
						(int) newRect.getY(), (int) newRect.getWidth(),
						(int) newRect.getHeight(), null);
				// System.out.println("Draw Graphics Result: " + isSuccess);
			}
		}

		// Processing the remaining VDP Image Fields
		for (int i = 0; i < vdpImageFields.size(); i++) {
			// g2d.setClip(null);
			g2d.setClip(gClip);
			vdpImageFieldInfo = (VDPImageFieldInfo) vdpImageFields.elementAt(i);
			// C:\WINDOWS\Fonts\Bell.ttf
			// Font font = new Font(Font.SERIF, Font.PLAIN, fontSize);
			// g2d.setFont(font);
			rect = vdpImageFieldInfo.m_PageRectangle;
			newRect = this.calcScreenBounds(pgInfo, rect);

			vdpData = userPgData.getVDPData("Image", rect);
			if (vdpData == null)
				throw new RuntimeException("VDP Data not Created");
			// The VDP Image is highlighted only in Edit Mode
			// The VDP Text is highlighted only in Edit Mode
			boolean isAllowEdit = ((Boolean) vdpData.get("AllowFieldEdit"))
					.booleanValue();
			if (viewMode == EDIT_MODE) {
				if (vdpImageFieldInfo.m_ClipSegmentCount > 0
						&& !vdpImageFieldInfo.m_IsBackgroundImage) {
					setClip = false;
					this.drawClipPath(g2d, pgInfo, vdpImageFieldInfo, setClip,
							newRect, vdpData, selVDPData);
				} else {
					if (isAllowEdit) {
						Color col = definecolor();
						g2d.setPaint(Color.BLACK);
						BasicStroke basicStroke = new BasicStroke(1.0f);
						if (selVDPData != null && selVDPData.equals(vdpData)) {
							g2d.setPaint(Color.RED);
							basicStroke = new BasicStroke(3.0f);
						}
						g2d.setStroke(basicStroke);
						g2d.setColor(col);
						g2d.draw(newRect);
						g2d.fill(newRect);
					}
				}
			}
			// System.out.println("VDP Image DATA Field Name: " +
			// (String)vdpData.get("VDPFieldName"));
			// System.out.println("VDP Image BBox: " +
			// (((RGPTRectangle)vdpData.get("BBox")).getRectangle2D()).toString());
			ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
			if (imgHldr != null) {
				System.out.println("Image Holder has: " + imgHldr.toString());
			}
			vdpData.put("DeviceBBox", RGPTRectangle.getReactangle(newRect));

			// Since the Background Image is displayed before this loop, no
			// processing is done here.
			if (vdpImageFieldInfo.m_IsBackgroundImage)
				continue;
			BufferedImage image = (BufferedImage) vdpData.get("UserSetImage");
			BufferedImage scaledImage = null;

			BufferedImage clippedImage = null;
			BufferedImage maskedImage = null;
			BufferedImage imageMask = (BufferedImage) vdpImageFieldInfo.m_ImageMask;
			System.out.println("Draw Image to Graphic, Alpha: "
					+ vdpImageFieldInfo.m_ImageAlphaValue);
			if (image == null)
				System.out.println("Image is null");
			if (!vdpImageFieldInfo.m_IsVDPPrepopulated && m_EditImageArea) {
				image = getScaledSubImage(vdpData);
				// vdpData.put("UserSetImage", image);
			}
			if (image != null) {
				image = ImageUtils.CreateCopy(image);
				BufferedImage framedImg = null;
				if (vdpImageFieldInfo.m_ShowPictureFrame
						&& vdpImageFieldInfo.m_PictureFrame != null)
					framedImg = ImageUtils.MaskImageRGB(image,
							vdpImageFieldInfo.m_PictureFrame, 150.0,
							Color.RED.getRGB(),
							vdpImageFieldInfo.m_IsOpaquePictureFrame);
				if (framedImg != null)
					image = framedImg;
				float alpha = (float) vdpImageFieldInfo.m_ImageAlphaValue;
				// Convert to transperent Image
				System.out.println("Transperent Image apha value: " + alpha);
				if (alpha != 1.0)
					image = ImageUtils.ConvToTransparentImage(image, alpha);
				// Drawing Clip Path for this Image
				// g2d = (Graphics2D) image.getGraphics();
				System.out.println("Is Image Background: "
						+ vdpImageFieldInfo.m_IsBackgroundImage);
				if (vdpImageFieldInfo.m_ClipSegmentCount > 0) {
					System.out.println("Has Clip, Count: "
							+ vdpImageFieldInfo.m_ClipSegmentCount);
					setClip = true;
					GeneralPath clipPath = this.drawClipPath(g2d, pgInfo,
							vdpImageFieldInfo, setClip, newRect, vdpData,
							selVDPData);
					// This logic checks if the Clip Path for the Image is
					// contained
					// in the Graphic Display Path. If true then the Clip Path
					// for the
					// Image is drawn else the Clip is set to Graphic Display
					// Clip.
					if (gClip == null || gClip.contains(clipPath.getBounds2D()))
						g2d.setClip(clipPath);
					else
						g2d.setClip(gClip);
				}
				// System.out.println("User Set Image: " + image.toString());
				if (imageMask == null) {
					boolean isSuccess = g2d.drawImage(image,
							(int) newRect.getX(), (int) newRect.getY(),
							(int) newRect.getWidth(),
							(int) newRect.getHeight(), null);
					if (selVDPData != null && selVDPData.equals(vdpData)) {
						BufferedImage dirImg = this.getDirectionIcon();
						if (dirImg != null) {
							int dirx = (int) (newRect.getX() + (newRect
									.getWidth()) / 2);
							int diry = (int) (newRect.getY() + (newRect
									.getHeight()) / 2);
							g2d.drawImage(dirImg, dirx, diry, null);
						}
					}
					continue;
				}

				// In this scenario the Image has Mask and the Masked Image is
				// drawn in the Graphics
				// System.out.println("\n\n Using Image Mask. \n\n");
				// System.out.println("Image Mask: " + imageMask.toString());
				// System.out.println("Image Mask: " + imageMask.getWidth() +
				// ":" +
				// imageMask.getHeight());
				maskedImage = drawMaskImage(image, imageMask);
				// System.out.println("Masked Image: " +
				// maskedImage.toString());
				g2d.drawImage(maskedImage, (int) newRect.getX(),
						(int) newRect.getY(), (int) newRect.getWidth(),
						(int) newRect.getHeight(), null);
			}
		}
		// resetting the Clip
		g2d.setClip(gClip);

	}

	// This is to show Direction Icon to move Images Horizontally or Vertically
	BufferedImage m_DirectionIcon = null;

	public BufferedImage getDirectionIcon() {
		if (m_DirectionIcon != null)
			return m_DirectionIcon;
		try {
			String imgLocation = PDFPageHandler.IMAGE_PATH + "direction.png";
			m_DirectionIcon = ImageUtils.getBufferedImage(imgLocation,
					this.getClass());
			// ImageUtils.displayImage(m_DirectionIcon, "Direction Image");
			return m_DirectionIcon;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private BufferedImage getScaledSubImage(HashMap vdpData) {
		System.out.println("\n\n**************************\n");
		System.out.println("In getScaledSubImage Method for: "
				+ vdpData.get("VDPFieldName"));
		BufferedImage origImg = (BufferedImage) vdpData.get("UserSetImage");
		if (origImg == null)
			return null;
		BufferedImage clipImg = null, dispImg = null;
		ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
		if (imgHldr == null) {
			System.out.println("Image Holder object is null for: "
					+ (String) vdpData.get("VDPFieldName"));
			return null;
		}
		RGPTRectangle devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
		BufferedImage scaledImg = imgHldr.m_ScaledImage;
		double scale = imgHldr.m_Scale;
		double scaledImgWt = 0.0, scaledImgHt = 0.0;
		if (imgHldr.m_DeviceBBox == null
				|| !imgHldr.m_DeviceBBox.equals(devBBox)) {
			if (scaledImg != null) {
				scaledImgWt = scaledImg.getWidth();
				scaledImgHt = scaledImg.getHeight();
			}
			System.out.println("Setting Image Holder Object with DeviceBBOX: "
					+ devBBox.toString());
			if (imgHldr.m_DeviceBBox != null)
				System.out.println("Original Image Holder DeviceBBOX: "
						+ imgHldr.m_DeviceBBox.toString());
			scaledImg = getScaledImage(vdpData);
			if (imgHldr.m_DisplayRect == null)
				imgHldr.m_DisplayRect = new Rectangle(0, 0,
						(int) devBBox.width, (int) devBBox.height);
			else {
				int imgWd = scaledImg.getWidth(), imgHt = scaledImg.getHeight();
				System.out.println("Orig Image Width: " + imgWd
						+ " And Height: " + imgHt);
				System.out.println("Image Holder Old Display Rect: "
						+ imgHldr.m_DisplayRect.toString());
				Rectangle oldDispRect = imgHldr.m_DisplayRect;
				int newX = (int) oldDispRect.getX(), newY = (int) oldDispRect
						.getY();
				if ((oldDispRect.getX() + devBBox.width) > (double) imgWd)
					newX = imgWd - (int) devBBox.width;
				if ((oldDispRect.getY() + devBBox.height) > (double) imgHt)
					newY = imgHt - (int) devBBox.height;
				imgHldr.m_DisplayRect = new Rectangle(newX, newY,
						(int) devBBox.width, (int) devBBox.height);

			}
			imgHldr.m_DeviceBBox = new RGPTRectangle(devBBox);
			System.out.println("Image Holder Display Rect: "
					+ imgHldr.m_DisplayRect.toString());
		}
		boolean isClipped = imgHldr.m_IsClipped;
		Rectangle clipRect = imgHldr.m_ClipRectangle;
		Rectangle dispRect = imgHldr.m_DisplayRect;
		RGPTRectangle imageBBox = imgHldr.m_ImageBBox;
		int startX = (int) dispRect.getX(), startY = (int) dispRect.getY(), endX = 0, endY = 0;
		// double dispWt = scaledImg.getWidth(), dispHt = scaledImg.getHeight();
		// double dispWt = devBBox.width, dispHt = devBBox.height;
		double dispWt = dispRect.width, dispHt = dispRect.height;
		endX = startX + (int) dispWt;
		endY = startY + (int) dispHt;
		System.out.println("startX: " + startX + " startY: " + startY);
		System.out
				.println("Calc DispWt: " + dispWt + " Calc dispHt: " + dispHt);
		System.out.println("endX: " + endX + " endY: " + endY);
		System.out.println("Scaled Img Wt: " + scaledImg.getWidth()
				+ " Scaled Img Ht: " + scaledImg.getHeight());
		System.out.println("Orig Img Wt: " + origImg.getWidth()
				+ " Orig Img Ht: " + origImg.getHeight());
		clipRect = new Rectangle(startX, startY, (int) dispWt, (int) dispHt);
		if (imgHldr.m_DisplayClipRect != null) {
			clipImg = imgHldr.m_ClippedImage;
			dispImg = ImageUtils.getSelectedImage(clipImg, scaledImg, startX,
					startY, endX, endY, 0, 0, clipRect);
			Rectangle dispClipRect = imgHldr.m_DisplayClipRect;
			clipRect.x += dispClipRect.x;
			clipRect.y += dispClipRect.y;
			// clipRect.width = (int) dispWt;
			// clipRect.height = (int) dispHt;
		} else
			dispImg = ImageUtils.getSelectedImage(origImg, scaledImg, startX,
					startY, endX, endY, 0, 0, clipRect);
		System.out
				.println("New Disp Rect: " + imgHldr.m_DisplayRect.toString());
		System.out.println("New Clip Rect: " + clipRect.toString());
		imgHldr.m_IsClipped = true;
		imgHldr.m_ClipRectangle = getActualImageClip(origImg, imgHldr, clipRect);
		System.out.println("Updated Image Holder : " + imgHldr.toString());

		// Drawing the Direction Icon
		// if (selVDPData != null && selVDPData.equals(vdpData))
		// {
		// if (m_ThumbViewImageHandler != null)
		// BufferedImage dirImg = m_ThumbViewImageHandler.getDirectionIcon();
		// if (dirImg != null) ImageUtils.drawImage(dispImg, dirImg, -1, -1);
		// }

		System.out.println("\n\n**************************\n");
		return dispImg;
	}

	private Rectangle getActualImageClip(BufferedImage dispImg,
			ImageHolder imgHldr, Rectangle clipRect) {
		double scale = 0.0;
		double sy = ((double) dispImg.getHeight())
				/ ((double) imgHldr.m_OrigImageHeight);
		double sx = ((double) dispImg.getWidth())
				/ ((double) imgHldr.m_OrigImageWidth);
		if (sx > sy)
			scale = sy;
		else
			scale = sx;
		// Resetting the Clip Rectangle based on High Res Image
		int x = (int) (clipRect.getX() / scale);
		int y = (int) (clipRect.getY() / scale);
		int w = (int) (clipRect.getWidth() / scale);
		int h = (int) (clipRect.getHeight() / scale);
		if (h > imgHldr.m_OrigImageHeight)
			h = imgHldr.m_OrigImageHeight;
		if (w > imgHldr.m_OrigImageWidth)
			w = imgHldr.m_OrigImageWidth;
		imgHldr.m_IsClipped = true;
		return new Rectangle(x, y, w, h);
	}

	public BufferedImage getScaledImage(HashMap vdpData) {
		BufferedImage origImg = (BufferedImage) vdpData.get("UserSetImage");
		if (origImg == null)
			return null;
		BufferedImage clipImg = null, scaledImg = null;
		ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
		RGPTRectangle devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
		imgHldr.m_DeviceBBox = devBBox;
		int imgWt = (int) imgHldr.m_OrigImageWidth;
		int imgHt = (int) imgHldr.m_OrigImageHeight;
		double scale = imgHldr.m_Scale;

		boolean isClipped = imgHldr.m_IsClipped;
		Rectangle clipRect = imgHldr.m_ClipRectangle;
		RGPTRectangle imageBBox = imgHldr.m_ImageBBox;
		double dispWt = 0, dispHt = 0;
		double aspectRatio = ((double) imgHt / (double) imgWt);
		if (imgWt < imgHt) {
			dispWt = devBBox.width;
			dispHt = dispWt * aspectRatio;
			imgHldr.m_ScalingMode = ImageHolder.SCALED_ALONG_WIDTH;
			if (dispHt < devBBox.height) {
				dispHt = devBBox.height;
				dispWt = dispHt / aspectRatio;
				imgHldr.m_ScalingMode = ImageHolder.SCALED_ALONG_HEIGHT;
			}
		} else {
			dispHt = devBBox.height;
			dispWt = dispHt / aspectRatio;
			imgHldr.m_ScalingMode = ImageHolder.SCALED_ALONG_HEIGHT;
			System.out.println("In Else DispWt: " + dispWt + " dispHt: "
					+ dispHt);
			if (dispWt < devBBox.width) {
				dispWt = devBBox.width;
				dispHt = dispWt * aspectRatio;
				;
				imgHldr.m_ScalingMode = ImageHolder.SCALED_ALONG_WIDTH;
			}
			System.out.println("The Raio is : "
					+ ((double) imgHt / (double) imgWt));
			System.out.println("After Else DispWt: " + dispWt + " dispHt: "
					+ dispHt);
		}

		System.out.println("DispWt: " + dispWt + " dispHt: " + dispHt);
		System.out.println("Image Holder: " + imgHldr.toString());
		if (imgHldr.m_DisplayClipRect != null) {
			System.out.println("Disp Clip Rect: "
					+ imgHldr.m_DisplayClipRect.toString());
			this.calcNewDispClipRect(vdpData);
			System.out.println("New Disp Clip Rect: "
					+ imgHldr.m_DisplayClipRect.toString());
			System.out.println("Orig Img Wt: " + origImg.getWidth() + " Ht: "
					+ origImg.getHeight());
			Rectangle dispRect = imgHldr.m_DisplayClipRect;
			int startX = (int) dispRect.getX(), startY = (int) dispRect.getY(), endX = 0, endY = 0;
			double dispClipWt = dispRect.width, dispClipHt = dispRect.height;
			endX = startX + (int) dispClipWt;
			endY = startY + (int) dispClipHt;
			clipImg = ImageUtils.getSelectedImage(origImg, startX, startY,
					endX, endY, 0, 0);
			System.out.println("Clip Img Wt: " + clipImg.getWidth() + " Ht: "
					+ clipImg.getHeight());
			imgHldr.m_ClippedImage = clipImg;
			// ImageUtils.displayImage(clipImg, "Clipped Image");
			scaledImg = ImageUtils.ScaleToSize(clipImg, (int) dispWt,
					(int) dispHt);
		} else
			scaledImg = ImageUtils.ScaleToSize(origImg, (int) dispWt,
					(int) dispHt);
		imgHldr.m_ScaledImage = scaledImg;
		return scaledImg;
	}

	private void calcNewDispClipRect(HashMap vdpData) {
		ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
		RGPTRectangle devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
		Rectangle oldDispRect = imgHldr.m_DisplayClipRect;
		// Since the Device BBox is changed, the ratio of old dev bbox to the
		// new
		// and corr multiplying with old clip pts will result in new clip pts.
		double devWtRatio = imgHldr.m_DeviceBBox.width / devBBox.width;
		double devHtRatio = imgHldr.m_DeviceBBox.height / devBBox.height;
		double dispx = oldDispRect.x * devWtRatio;
		double dispy = oldDispRect.y * devHtRatio;
		double wt = oldDispRect.width * devWtRatio;
		double ht = oldDispRect.height * devHtRatio;
		imgHldr.m_DisplayClipRect = new Rectangle((int) dispx, (int) dispy,
				(int) wt, (int) ht);
	}

	private BufferedImage drawMaskImage(BufferedImage image,
			BufferedImage imageMask) {
		double threshold = ImageUtils.findavg(image);
		BufferedImage maskedImg = null;
		maskedImg = ImageUtils.Pixel3DRGBtoBufferedImage(ImageUtils.MaskImage(
				image, imageMask, threshold));
		return maskedImg;
	}

	// g2d.setPaint(fg);
	// g2d.setStroke(clipPath.m_PathStroke);
	// g2d.setClip(clipShape);
	// g2d.drawImage(image, (int) imgbbox.getX(), (int) imgbbox.getY(),
	// (int) imgbbox.getWidth(), (int) imgbbox.getHeight(), null);
	// g2d.draw(imgbbox);

	private GeneralPath drawClipPath(Graphics2D g2d, PDFPageInfo pgInfo,
			VDPImageFieldInfo vdpImgFieldInfo, boolean setClip,
			Rectangle2D.Double newRect, HashMap vdpData, HashMap selVDPData) {
		ClipPath clipPath = null;
		GeneralPath clipShape = null;
		GeneralPath finalClipShape = null;

		int clipPathCount = vdpImgFieldInfo.m_ClipSegmentCount;
		Point2D.Double startPt = new Point2D.Double(newRect.getX(),
				newRect.getY());
		for (int i = 0; i < vdpImgFieldInfo.m_ClipPath.size(); i++) {
			clipPath = (ClipPath) vdpImgFieldInfo.m_ClipPath.elementAt(i);
			if (clipPath == null)
				continue;
			// m_FillColor m_StrokeColor m_PathStroke
			// g2d.setPaint(bg);
			// g2d.fill(clipShape);
			g2d.setPaint(clipPath.m_StrokeColor);
			if (clipPath.m_PathStroke != null)
				g2d.setStroke(clipPath.m_PathStroke);
			if (setClip) {
				// Clip Shape is drawn relative to Image Points
				// clipShape = clipPath.getClipShape(m_PDFPageInfo, startPt);
				clipShape = clipPath.getClipShape(pgInfo, new Point2D.Double(
						0.0, 0.0));
				if (i == 0)
					finalClipShape = clipShape;
				else
					finalClipShape.append(clipShape, true);
			} else {
				Color col = definecolor();
				g2d.setColor(col);
				g2d.setPaint(Color.BLACK);
				BasicStroke basicStroke = new BasicStroke(1.0f);
				if (selVDPData != null && selVDPData.equals(vdpData)) {
					g2d.setPaint(Color.RED);
					basicStroke = new BasicStroke(2.0f);
				}
				g2d.setStroke(basicStroke);
				// Clip Shape is drawn relative to Page Points
				clipShape = clipPath.getClipShape(pgInfo, new Point2D.Double(
						0.0, 0.0));
				g2d.draw(clipShape);
				g2d.setColor(col);
				g2d.fill(clipShape);
			}
		}
		// if (finalClipShape != null) g2d.setClip(finalClipShape);
		return finalClipShape;
	}

	private Vector<Point2D.Double> calcScreenPoints(PDFPageInfo pgInfo,
			Vector<Point2D.Double> pgPts) {
		Point2D.Double pgPt = null, scrPt = null;
		AffineTransform finalDevCTM = null;
		Vector<Point2D.Double> scrPts = new Vector<Point2D.Double>();

		// Assigning the Final Device CTM
		finalDevCTM = (AffineTransform) pgInfo.m_FinalDeviceCTM.clone();

		// Deriving Screen Points from Page Points
		for (int i = 0; i < pgPts.size(); i++) {
			scrPt = new Point2D.Double();
			pgPt = pgPts.elementAt(i);
			finalDevCTM.transform(pgPt, scrPt);
			// RGPTLogger.logToFile("Page PT " + pgPt.toString() +
			// ": Screen PT " + scrPt.toString());
			scrPts.addElement(scrPt);
		}
		// RGPTLogger.logToFile("Shape Points Inside: " + scrPts);
		return scrPts;
	}

	public AffineTransform getScreenPts(PDFPageInfo pdfPgInfo,
			AffineTransform elemCTM, AffineTransform textCTM,
			Point2D.Double srcPt, Point2D.Double desPt) {

		return this.getScreenPts(pdfPgInfo, pdfPgInfo.m_PageCTM, elemCTM,
				textCTM, srcPt, desPt);
	}

	public AffineTransform getScreenPts(PDFPageInfo pdfPgInfo,
			AffineTransform pageCTM, AffineTransform elemCTM,
			AffineTransform textCTM, Point2D.Double srcPt, Point2D.Double desPt) {

		// RGPTLogger.logToFile("\n\n--- IN getScreenPts Method ----\n\n");
		// This holds the reference to the clone of text, element, page and
		// Device CTM
		// Clone m_CalcDeviceCTM m_TextMatrix m_ElementCTM m_PageCTM m_StartX
		// m_StartY
		AffineTransform textCTMClone = null, elemCTMClone = null;
		AffineTransform pgCTMClone = null, devCTMClone = null;
		AffineTransform finalDevCTM = null;

		pgCTMClone = (AffineTransform) pageCTM.clone();
		// RGPTLogger.logToFile("Orig Page CTM : " + pgCTMClone.toString());
		devCTMClone = (AffineTransform) pdfPgInfo.m_CalcDeviceCTM.clone();
		// RGPTLogger.logToFile("Device CTM : " + devCTMClone.toString());

		AffineTransform rotCTM = null;
		rotCTM = pdfPgInfo.getRotationMatrix();

		// Transforming to Screen Coordinate System. Essentially moving from
		// bottom left PDF
		// Coordinate System to top Left Java Coord System. If the DPI is set to
		// 72 then 1 Pixel
		// is equal to 1 User Space.

		elemCTMClone = (AffineTransform) elemCTM.clone();
		textCTMClone = (AffineTransform) textCTM.clone();

		// Getting the Final CTM which is DeviceCTM x PageCTM x ElementCTM x
		// TextCTM
		// To Calculate the Bounds need the First and Last Character Bounding
		// Box
		AffineTransform rotTextCTM = pdfPgInfo.getTextRotationMatrix();
		textCTMClone.concatenate(rotTextCTM);
		elemCTMClone.concatenate(textCTMClone);

		// RGPTLogger.logToFile("ElementCTM Concat Text Met : " +
		// elemCTMClone.toString());

		pgCTMClone.concatenate(elemCTMClone);

		// RGPTLogger.logToFile("Page CTM Concat ElementCTM and Text Met : " +
		// pgCTMClone.toString());

		rotCTM.concatenate(pgCTMClone);
		devCTMClone.concatenate(rotCTM);

		finalDevCTM = devCTMClone;

		// RGPTLogger.logToFile("Final TEXT CTM: " + finalDevCTM.toString());

		// finalDevCTM = textCTMClone;

		// finalDevCTM = pdfPgInfo.m_FinalDeviceCTM;

		// RGPTLogger.logToFile("Dev CTM Cocat Page CTM and ElementCTM and Text Met : "
		// + finalDevCTM.toString());

		// Calculating Start Position

		finalDevCTM.transform(srcPt, desPt);

		// RGPTLogger.logToFile("Text Src Pt: " + srcPt.toString() +
		// " Text Des Pt: " + desPt.toString());
		// Calculating the Source Point from Destination Point
		try {
			RGPTLogger.logToFile("Calc Inv Transform");
			AffineTransform invTransform = finalDevCTM.createInverse();
			RGPTLogger.logToFile("Inv Transform: " + invTransform.toString());
			Point2D.Double desPt1 = new Point2D.Double(desPt.getX(),
					desPt.getY());

			Point2D.Double srcPt1 = new Point2D.Double();
			invTransform.transform(desPt1, srcPt1);

			RGPTLogger.logToFile("Text New Src Pt: " + srcPt1.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return finalDevCTM;
	}

	public void calcTextStartPt(HashMap vdpData, Rectangle2D.Double bbox) {
		double x = bbox.getX(), y = bbox.getY(), w = bbox.getWidth(), h = bbox
				.getHeight();
		Point2D.Double scrPt1 = new Point2D.Double(x, y);
		Point2D.Double scrPt2 = new Point2D.Double(x, y + h);
		Point2D.Double scrPt3 = new Point2D.Double(x + w, y);
		Point2D.Double scrPt4 = new Point2D.Double(x + w, y + h);
		Point2D.Double pgPt1 = new Point2D.Double();
		Point2D.Double pgPt2 = new Point2D.Double();
		Point2D.Double pgPt3 = new Point2D.Double();
		Point2D.Double pgPt4 = new Point2D.Double();
		AffineTransform invTextCTM = (AffineTransform) vdpData
				.get("FinalInvCTM");
		invTextCTM.transform(scrPt1, pgPt1);
		invTextCTM.transform(scrPt2, pgPt2);
		invTextCTM.transform(scrPt3, pgPt3);
		invTextCTM.transform(scrPt4, pgPt4);
		// RGPTLogger.logToFile("Txt Page Pt 1: " + pgPt1.toString());
		// RGPTLogger.logToFile("Txt Page Pt 2: " + pgPt2.toString());
		// RGPTLogger.logToFile("Txt Page Pt 3: " + pgPt3.toString());
		// RGPTLogger.logToFile("Txt Page Pt 4: " + pgPt4.toString());
		double txtPtX = Math.min(pgPt1.getX(), pgPt3.getX());
		// double txtPtY = Math.min(pgPt1.getY(), pgPt2.getY());
		double txtPtY = pgPt2.getY();
		// vdpData.put("StartPtX", txtPtX);
		// if (m_ViewMode == MOVE_MODE) {
		// vdpData.put("StartPtY", txtPtY);
		// AffineTransform textCTM = (AffineTransform)
		// vdpData.get("TextMatrix");
		// textCTM.translate(0.0, txtPtY);
		// }
	}

	public Color definecolor() {
		float rcol, gcol, bcol, alpha;
		rcol = new Float(0.5);
		gcol = new Float(0.1);
		bcol = new Float(0.7);
		alpha = new Float(0.2);
		Color col = new Color(rcol, gcol, bcol, alpha);
		return col;
	}

	// Narayan - Finally Preparing the VDP IMage and Text Fields bfor generating
	// the PDF
	public void processVDPImageFields(Vector vdpImageFields,
			UserPageData userPgData) {
		HashMap vdpImagedata = null;
		VDPImageFieldInfo vdpImageFieldInfo;
		RGPTLogger.logToFile("In processVDPImageFields #Images: "
				+ vdpImageFields.size());
		for (int iter = 0; iter < vdpImageFields.size(); iter++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) vdpImageFields
					.elementAt(iter);
			RGPTLogger.logToFile("Processing Image: "
					+ vdpImageFieldInfo.m_FieldName);
			vdpImagedata = userPgData.getVDPData("Image",
					vdpImageFieldInfo.m_PageRectangle);
			if (vdpImagedata == null) {
				RGPTLogger.logToFile("No User Image found for: "
						+ vdpImageFieldInfo.m_FieldName);
				continue;
			}
			RGPTLogger.logToFile("Found VDP Data for: "
					+ (String) vdpImagedata.get("VDPFieldName"));
			ImageHolder imgHldr = (ImageHolder) vdpImagedata.get("ImageHolder");
			if (imgHldr == null)
				continue;
			// if(imgHldr.m_UseCachedImages) continue;
			RGPTLogger.logToFile("IMAGE HOLDER FOUND: " + imgHldr.toString());
			BufferedImage buffImg = null;
			try {
				buffImg = ImageUtils.getPrintableImage(imgHldr,
						ImageHandlerInterface.IMAGE_PRINT_DPI);
				imgHldr.m_ImgStr = ImageUtils.getImageStream(buffImg, "PNG");
				vdpImagedata.put("UserSetImage", buffImg);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void processVDPTextFields(PDFPageInfo pgInfo, Vector vdpTextFields,
			UserPageData userPgData, Map<RGPTRectangle, Vector> multiWordSel) {
		HashMap vdpData = null;
		VDPTextFieldInfo vdpTextFieldInfo;
		RGPTRectangle lineBBox = null;
		for (int iter = 0; iter < vdpTextFields.size(); iter++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) vdpTextFields.elementAt(iter);
			int vdpTextMode = vdpTextFieldInfo.m_VDPTextMode;
			if (vdpTextMode == StaticFieldInfo.PARA)
				continue;
			RGPTLogger.logToFile("Processing VDP Text for: "
					+ vdpTextFieldInfo.m_FieldName);
			vdpData = userPgData.getVDPData(vdpTextFieldInfo.m_FieldName);
			// vdpData = userPgData.getVDPData(vdpTextFieldInfo.m_FieldType,
			// vdpTextFieldInfo.m_PageRectangle);
			if (vdpData == null)
				continue;
			String vdpText = (String) vdpData.get("UserSetValue");
			RGPTLogger.logToFile("User Set Value: " + vdpText);
			if (vdpTextFieldInfo.m_FieldType.equals("TextOnGraphics")) {

				boolean isFieldFixed = vdpTextFieldInfo.m_IsFieldFixed;
				if (!isFieldFixed)
					if (vdpText == null || vdpText.length() == 0)
						continue;
				Polygon poly = (Polygon) vdpData.get("Polygon");
				Rectangle2D scrRect = RGPTUtil.getRectangle2D(poly.getBounds());
				BufferedImage textImg = (BufferedImage) vdpData
						.get("UserSetImage");
				vdpData.put("UserSetFinalImage", textImg);
				AffineTransform finDevCTM = (AffineTransform) vdpData
						.get("FinalDevCTM");
				Rectangle2D.Double pgRect = calcPageRect4TextOnGraphics(
						finDevCTM, scrRect);
				vdpData.put("UserSetFinalImage", textImg);
				vdpData.put("NewBBox", RGPTRectangle.getReactangle(pgRect));
				continue;
			}

			AffineTransform textCTM = (AffineTransform) vdpData
					.get("TextMatrix");
			AffineTransform origTextMat = vdpTextFieldInfo.m_TextMatrix;
			RGPTLogger.logToFile("Orig Text Mat Final 0: "
					+ origTextMat.toString());
			RGPTLogger.logToFile("VDPData Text Mat 0: " + textCTM.toString());
			// String vdpText = (String) vdpData.get("UserSetValue");
			if (vdpText == null || vdpText.length() == 0) {
				vdpText = getAltVDPText(userPgData, vdpData);
				if (vdpText == null) {
					vdpData.put("UserSetFinalValue", "");
					continue;
				}
			}
			// NEW: Setting the user value in all the Fields having the same
			// Field Name
			userPgData.setUserValue(vdpData);
			// if (vdpTextFieldInfo.m_TextAllignment.equals("LEFT")) continue;
			this.calcStartPoint(pgInfo, vdpTextFieldInfo, vdpData);
			lineBBox = vdpTextFieldInfo.m_LineBBox;
			if (vdpTextMode != StaticFieldInfo.WORD || lineBBox == null)
				continue;
			this.getLineTxt4MultiWdSel(multiWordSel, lineBBox, vdpData);
		}
	}

	public void calcStartPoint(PDFPageInfo pgInfo,
			VDPTextFieldInfo vdpTextFieldInfo, HashMap vdpData) {
		String allign = (String) vdpData.get("TextAllignment");
		if (allign.equals(PDFPageHandler.ALLIGN_LEFT))
			return;
		Rectangle2D.Double screenRect = null;
		Point2D.Double srcPt, desPt = null;
		Font font = vdpTextFieldInfo.m_Font;
		srcPt = new Point2D.Double(vdpTextFieldInfo.m_StartX,
				vdpTextFieldInfo.m_StartY);
		desPt = new Point2D.Double();
		AffineTransform finalCTM = null;

		AffineTransform textCTM = (AffineTransform) vdpData.get("TextMatrix");
		AffineTransform origTextMat = vdpTextFieldInfo.m_TextMatrix;
		RGPTLogger.logToFile("Orig Text Mat Final: " + origTextMat.toString());
		RGPTLogger.logToFile("VDPData Text Mat: " + textCTM.toString());
		finalCTM = getScreenPts(pgInfo, pgInfo.m_OrigPageCTM,
				vdpTextFieldInfo.m_ElementCTM, textCTM, srcPt, desPt);
		RGPTLogger.logToFile("Final CTM After Screen Pts: "
				+ finalCTM.toString());
		font = font.deriveFont(finalCTM);
		Hashtable fontAttrib = vdpTextFieldInfo.m_DeriveFontAttrib;
		RGPTLogger.logToFile("Font Attribute: " + fontAttrib.toString());
		font = font.deriveFont(fontAttrib);
		RGPTLogger.logToFile("Font Data: " + font.toString());
		FontRenderContext frc = new FontRenderContext(null, true, true);
		String vdpText = (String) vdpData.get("UserSetFinalValue");
		if (vdpText == null || vdpText.length() == 0)
			return;
		Rectangle2D textRect = font.getStringBounds(vdpText, frc);
		// Rectangle2D textRect = fm.getStringBounds(vdpText, g2d);
		// screenRect = this.calcScreenBounds(vdpTextFieldInfo.m_PageRectangle);

		double textWidth = textRect.getWidth();
		// double panelWidth = screenRect.getWidth();
		// double panelWidth = vdpTextFieldInfo.m_PageRectangle.getWidth();
		Rectangle2D.Double newBBox = ((RGPTRectangle) vdpData.get("NewBBox"))
				.getRectangle2D();
		double panelWidth = newBBox.getWidth();
		RGPTLogger.logToFile("VDP Text: " + vdpText + " Text Width: "
				+ textWidth + " Orig Panel Width: "
				+ vdpTextFieldInfo.m_PageRectangle.getWidth()
				+ " Trans Panel Width: " + panelWidth);

		// Center or Right Allign text horizontally
		double startx = 0.0;
		// if (vdpTextFieldInfo.m_TextAllignment.equals("CENTER"))
		// String allign = (String) vdpData.get("TextAllignment");
		if (allign.equals(PDFPageHandler.ALLIGN_CENTER))
			startx = (panelWidth - textWidth) / 2;
		// else if (vdpTextFieldInfo.m_TextAllignment.equals("RIGHT"))
		else if (allign.equals(PDFPageHandler.ALLIGN_RIGHT))
			startx = (panelWidth - textWidth);
		RGPTLogger.logToFile("StartPtX: " + startx);
		vdpData.put("StartPtX", startx);
	}

	public String getAltVDPText(UserPageData userPgData, HashMap vdpData) {
		String altVDPFld = (String) vdpData.get("AlternateVDPField");
		if (altVDPFld.equals("None"))
			return null;
		RGPTLogger.logToFile("Alt VDP Field to be Used: " + altVDPFld);
		HashMap altVDPData = userPgData.getVDPData(altVDPFld);
		if (altVDPData == null)
			return null;
		String vdpText = (String) altVDPData.get("UserSetValue");
		if (vdpText == null || vdpText.length() == 0) {
			vdpText = getAltVDPText(userPgData, altVDPData);
			if (vdpText == null)
				return null;
		}
		String vdpFinalText = (String) altVDPData.get("UserSetFinalValue");
		RGPTLogger.logToFile("Alt VDP Text: " + vdpText
				+ " Fianl Formatted Text: " + vdpFinalText);
		vdpData.put("UserSetValue", vdpText);
		vdpData.put("UserSetFinalValue", vdpFinalText);
		altVDPData.put("UserSetValue", "");
		altVDPData.put("UserSetFinalValue", "");
		String altvdpText = getAltVDPText(userPgData, altVDPData);
		if (altvdpText == null) {
			altVDPData.put("UserSetFinalValue", "");
		}

		return vdpText;
	}

	private Rectangle2D.Double calcPageRect4TextOnGraphics(
			AffineTransform finalDevCTM, Rectangle2D srcRect) {
		Point2D.Double ptSrc, ptDst1, ptDst2;
		double desX1, desY1, desWidth, desHeight;
		AffineTransform pageCTM = null;
		Rectangle2D.Double desRect = null;

		ptSrc = new Point2D.Double();
		ptDst1 = new Point2D.Double();
		ptDst2 = new Point2D.Double();

		// Assigning the Final Device CTM
		// finalDevCTM = (AffineTransform)
		// m_PDFPageInfo.m_FinalDeviceCTM.clone();
		try {
			pageCTM = finalDevCTM.createInverse();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(
					"Exception at calcPageRect4TextOnGraphics: "
							+ ex.getMessage());
		}
		RGPTLogger.logToFile("Final Screen Device CTM: "
				+ finalDevCTM.toString());
		RGPTLogger.logToFile("Final Page CTM: " + pageCTM.toString());

		// Deriving x1, y1 of the Rectangle
		ptSrc.setLocation(srcRect.getX(), srcRect.getY());
		pageCTM.transform(ptSrc, ptDst1);
		RGPTLogger
				.logToFile("Screen PT " + ptSrc + ": Page PT DEST1 " + ptDst1);

		// Deriving x2, y2 of the Rectangle
		ptSrc.setLocation(srcRect.getX() + srcRect.getWidth(), srcRect.getY()
				+ srcRect.getHeight());
		pageCTM.transform(ptSrc, ptDst2);
		RGPTLogger.logToFile("Screen PT " + ptSrc + ": Pg PT DEST2 " + ptDst2);

		desX1 = Math.min(ptDst1.getX(), ptDst2.getX());
		desY1 = Math.min(ptDst1.getY(), ptDst2.getY());
		desWidth = Math.abs(ptDst1.getX() - ptDst2.getX());
		desHeight = Math.abs(ptDst1.getY() - ptDst2.getY());
		;

		desRect = new Rectangle2D.Double(desX1, desY1, desWidth, desHeight);
		return desRect;
	}

	private void getLineTxt4MultiWdSel(Map<RGPTRectangle, Vector> multiWordSel,
			RGPTRectangle lineBBox, HashMap vdpTxtData) {
		double txtWt = 0.0, totTxtWt = 0.0;
		String vdpText = "", origVDPTxt = "";
		HashMap vdpData = null;
		Rectangle2D textRect = null;
		if (multiWordSel == null)
			return;
		Vector<HashMap> vdpTxtWords = multiWordSel.get(lineBBox);
		if (vdpTxtWords == null)
			return;
		if (!vdpTxtWords.elementAt(0).equals(vdpTxtData))
			return;
		for (int i = 0; i < vdpTxtWords.size(); i++) {
			vdpData = vdpTxtWords.elementAt(i);
			origVDPTxt = (String) vdpData.get("UserSetFinalValue");
			if (i < vdpTxtWords.size() - 1)
				vdpText = vdpText + origVDPTxt.trim() + " ";
			else
				vdpText = vdpText + origVDPTxt;
			vdpData.put("UserSetFinalValue", "");
		}
		vdpData = vdpTxtWords.elementAt(0);
		vdpData.put("UserSetFinalValue", vdpText);
	}

}

/*
 * 
 * private void calcStartPoint(PDFPageInfo pgInfo, VDPTextFieldInfo
 * vdpTextFieldInfo, HashMap vdpData) { String allign = (String)
 * vdpData.get("TextAllignment"); if (allign.equals(PDFPageHandler.ALLIGN_LEFT))
 * return; Rectangle2D.Double screenRect = null; Point2D.Double srcPt, desPt =
 * null; RGPTLogger.logToFile("VDP Text Object: " +
 * vdpTextFieldInfo.toString()); Font font = vdpTextFieldInfo.m_Font; srcPt =
 * new Point2D.Double(vdpTextFieldInfo.m_StartX, vdpTextFieldInfo.m_StartY);
 * desPt = new Point2D.Double(); AffineTransform finalCTM = null; finalCTM =
 * this.getScreenPts(pgInfo, vdpTextFieldInfo.m_ElementCTM,
 * vdpTextFieldInfo.m_TextMatrix, srcPt, desPt);
 * RGPTLogger.logToFile("Final CTM After Screen Pts: " + finalCTM.toString());
 * font = font.deriveFont(finalCTM); Hashtable fontAttrib =
 * vdpTextFieldInfo.m_DeriveFontAttrib; RGPTLogger.logToFile("Font Attribute: "
 * + fontAttrib.toString()); font = font.deriveFont(fontAttrib);
 * RGPTLogger.logToFile("Font Data: " + font.toString()); FontRenderContext frc
 * = new FontRenderContext(null, true, true); String vdpText = (String)
 * vdpData.get("UserSetValue"); Rectangle2D textRect =
 * font.getStringBounds(vdpText, frc);
 * 
 * double textWidth = textRect.getWidth(); double panelWidth =
 * vdpTextFieldInfo.m_PageRectangle.getWidth();
 * RGPTLogger.logToFile("VDP Text: " + vdpText + " Text Width: " + textWidth +
 * " Orig Panel Width: " + vdpTextFieldInfo.m_PageRectangle.getWidth() +
 * "Trans Panel Width: " + panelWidth);
 * 
 * // Center or Right Allign text horizontally double startx = 0.0; if
 * (vdpTextFieldInfo.m_TextAllignment.equals(PDFPageHandler.ALLIGN_CENTER))
 * startx = (panelWidth - textWidth)/2; else if
 * (vdpTextFieldInfo.m_TextAllignment.equals(PDFPageHandler.ALLIGN_RIGHT))
 * startx = (panelWidth - textWidth); vdpData.put("StartPtX", startx);
 * RGPTLogger.logToFile("Start X Pos: " + startx); }
 */

