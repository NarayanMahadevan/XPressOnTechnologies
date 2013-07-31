// RGPT PACKAGES
package com.rgpt.templateutil;

// This files are added to support serialization
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;

class RotationMatrix {
	public static final int ROTATION_0 = 0;
	public static final int ROTATION_90 = 1;
	public static final int ROTATION_180 = 2;
	public static final int ROTATION_270 = 3;

	public static AffineTransform getRotationMatrix(int rot, double wd,
			double ht) {
		AffineTransform rotMat = null;
		switch (rot) {
		case ROTATION_0:
			rotMat = new AffineTransform(1.0, 0.0, 0.0, 1.0, 1.0, 1.0);
			break;
		case ROTATION_90:
			rotMat = new AffineTransform(-1.0, 0.0, 0.0, 1.0, wd, 1.0);
			break;
		case ROTATION_180:
			rotMat = new AffineTransform(-1.0, 0.0, 0.0, -1.0, ht, wd);
			break;
		case ROTATION_270:
			rotMat = new AffineTransform(1.0, 0.0, 0.0, -1.0, 1, ht);
			break;
		default:
			rotMat = new AffineTransform(1.0, 0.0, 0.0, 1.0, 1.0, 1.0);
			break;
		}
		return rotMat;
	}
}

public class PDFPageInfo implements Serializable {
	// This defines the current RGPT PDF VERSION
	// public static final long serialVersionUID = 8542721947703465060L;
	public static final long serialVersionUID = 5293013250085507480L;

	// PDF Page as Image - Do Not Serialize
	public transient BufferedImage m_BufferedImage;
	// public transient BufferedImage m_HighQualityPage;
	// public transient BufferedImage m_BestQualityPage;

	// Page Properties
	public int m_PageNum;
	public int m_PageWidth;
	public int m_PageHeight;
	public int m_ImageWidth;
	public int m_ImageHeight;
	public int m_PageRotation;

	// This is comming from the Private Objects. This are set while creating the
	// Template.
	// The Device CTM is the fixed value calculated
	public AffineTransform m_OrigPageCTM;
	public AffineTransform m_PageCTM;
	public AffineTransform m_DeviceCTM;
	public double m_PageScale = -1.0;

	public boolean m_IsVDPFieldDefined = false;

	// This holds all the Variable Text Data Defined for the Page
	public transient Vector m_VDPTextFieldInfo;

	// This holds all the Variable Image Data Defined for the Page
	public transient Vector m_VDPImageFieldInfo;

	// This is the Device Display Position in X Coord - Do not Serialize
	public transient Point2D.Double m_DeviceDisplayPt;

	// This is Device CTM calculated based on scaling factor
	public transient AffineTransform m_CalcDeviceCTM;

	// This is calculated based on scaling factor and Page CTM
	public transient AffineTransform m_FinalDeviceCTM;

	// This is used for only drawing the PDF Page Image in the Panel
	public transient AffineTransform m_WindowDeviceCTM;

	// This is used only to check if the sequence for VDP Text Field is set for
	// every
	// page
	public transient static Map<Integer, Boolean> m_IsPageSequenceSet4Text = new HashMap<Integer, Boolean>();

	// This is the new Scaled PDF Page HT and Width
	public transient Rectangle2D.Double m_PageRect;
	public transient Rectangle2D.Double m_ScreenRect;
	public transient double m_ScaledPDFPageHt;
	public transient double m_ScaledPDFPageWd;
	public transient double m_Scale;
	public transient boolean m_HasBackgroundImage;
	public transient VDPImageFieldInfo m_BackgroundImageField;

	// SCALLING FACTOR

	public PDFPageInfo() {
		// m_DeviceDisplayPt = new Point2D.Double(10.0,10.0);
		m_HasBackgroundImage = false;
		m_VDPTextFieldInfo = new Vector();
		m_VDPImageFieldInfo = new Vector();
	}

	public void buildProperFields() {
		m_ScaledPDFPageHt = 0.0;
		m_ScaledPDFPageWd = 0.0;
		m_IsVDPFieldDefined = false;

		VDPTextFieldInfo vdpTextFieldInfo;
		VDPImageFieldInfo vdpImageFieldInfo;

		// PDF Page Image Ht and Width
		if (m_BufferedImage != null) {
			m_ImageWidth = m_BufferedImage.getWidth(null);
			m_ImageHeight = m_BufferedImage.getHeight(null);
		}
		m_PageRect = new Rectangle2D.Double(0.0, 0.0, m_ImageWidth,
				m_ImageHeight);
		m_DeviceDisplayPt = new Point2D.Double(10.0, 10.0);
		// if (m_PageCTM != null && m_PageScale == -1.0)
		// m_OrigPageCTM = (AffineTransform) m_PageCTM.clone();
		RGPTLogger.logToFile("Page Wth: " + m_PageWidth + " Ht: "
				+ m_PageHeight + " Img Wth: " + m_ImageWidth + " Ht: "
				+ m_ImageHeight + " Pg Scale: " + m_PageScale);
		if (m_OrigPageCTM != null && m_PageScale == -1.0) {
			m_PageCTM = (AffineTransform) m_OrigPageCTM.clone();
			m_PageScale = (double) m_ImageWidth / (double) m_PageWidth;
			double scaleY = (double) m_ImageHeight / (double) m_PageHeight;
			RGPTLogger.logToFile("Scale X: " + m_PageScale + " ScaleY: "
					+ scaleY);
			RGPTLogger.logToFile("Prior PageCTM: " + m_PageCTM.toString()
					+ "Setting scale: " + m_PageScale);
			m_PageCTM.scale(m_PageScale, m_PageScale);
			RGPTLogger.logToFile("PageCTM After Scaling: "
					+ m_PageCTM.toString());
		}
		// Derive Final Device CTM. This is used to get the Screen Points
		// from the Page Points
		// this.deriveFinalDeviceCTM();
		boolean isTextSeqSet = true;
		for (int i = 0; i < m_VDPTextFieldInfo.size(); i++) {
			m_IsVDPFieldDefined = true;
			vdpTextFieldInfo = (VDPTextFieldInfo) m_VDPTextFieldInfo
					.elementAt(i);
			int seqId = vdpTextFieldInfo.m_SequenceId;
			if (seqId == -1 || seqId == 0)
				isTextSeqSet = false;
			vdpTextFieldInfo.buildProperFields();
		}
		for (int i = 0; i < m_VDPImageFieldInfo.size(); i++) {
			m_IsVDPFieldDefined = true;
			vdpImageFieldInfo = (VDPImageFieldInfo) m_VDPImageFieldInfo
					.elementAt(i);
			if (vdpImageFieldInfo.m_IsBackgroundImage) {
				m_HasBackgroundImage = true;
				m_BackgroundImageField = vdpImageFieldInfo;
			}
			vdpImageFieldInfo.buildProperFields();
		}
		m_IsPageSequenceSet4Text.put(m_PageNum, isTextSeqSet);
	}

	public void buildFontStream() {
		VDPTextFieldInfo vdpTextFieldInfo;
		for (int i = 0; i < m_VDPTextFieldInfo.size(); i++) {
			m_IsVDPFieldDefined = true;
			vdpTextFieldInfo = (VDPTextFieldInfo) m_VDPTextFieldInfo
					.elementAt(i);
			vdpTextFieldInfo.setFontAttrib();
		}
	}

	public VDPFieldInfo getVDPFieldInfo(String vdpFieldType, String vdpFldName) {
		Vector vdpFieldList = null;

		if (vdpFieldType == "Text")
			vdpFieldList = m_VDPTextFieldInfo;
		else if (vdpFieldType == "Image")
			vdpFieldList = m_VDPImageFieldInfo;
		else
			throw new RuntimeException("Wrong VDP Field Type: " + vdpFieldType);

		VDPFieldInfo vdpFldInfo = null;

		// Finding the Match
		Rectangle2D.Double intersectBBox = null;
		// Vector selImgData = new Vector();
		for (int i = 0; i < vdpFieldList.size(); i++) {
			vdpFldInfo = (VDPFieldInfo) vdpFieldList.elementAt(i);
			if (vdpFldInfo.m_FieldName.equals(vdpFldName))
				return vdpFldInfo;
		}
		return null;
	}

	public VDPFieldInfo getVDPFieldInfo(String vdpFieldType,
			Rectangle2D.Double bbox) {
		Vector vdpFieldList = null;
		Rectangle2D.Double vdpbbox = null;

		if (vdpFieldType == "Text")
			vdpFieldList = m_VDPTextFieldInfo;
		else if (vdpFieldType == "Image")
			vdpFieldList = m_VDPImageFieldInfo;
		else
			throw new RuntimeException("Wrong VDP Field Type: " + vdpFieldType);

		VDPFieldInfo vdpFldInfo = null;

		// Finding the Match
		Rectangle2D.Double intersectBBox = null;
		// Vector selImgData = new Vector();
		for (int i = 0; i < vdpFieldList.size(); i++) {
			vdpFldInfo = (VDPFieldInfo) vdpFieldList.elementAt(i);
			vdpbbox = vdpFldInfo.m_PageRectangle;
			if (vdpbbox.equals(bbox))
				return vdpFldInfo;
			Rectangle bboxRect = RGPTUtil.getRectangle(bbox);
			Rectangle vdpbboxRect = RGPTUtil.getRectangle(vdpbbox);
			if (vdpbboxRect.equals(bboxRect))
				return vdpFldInfo;
		}
		return null;
	}

	public AffineTransform getRotationMatrix() {
		AffineTransform rotCTM = null;
		rotCTM = RotationMatrix.getRotationMatrix(m_PageRotation,
				(double) m_PageWidth, (double) m_PageHeight);
		return rotCTM;
	}

	public AffineTransform getTextRotationMatrix() {
		AffineTransform rotTextCTM = null;
		if (m_PageRotation == RotationMatrix.ROTATION_0)
			rotTextCTM = new AffineTransform(1.0, 0.0, 0.0, -1.0, 0.0, 0.0);
		else if (m_PageRotation == RotationMatrix.ROTATION_270)
			rotTextCTM = new AffineTransform(1.0, 0.0, 0.0, -1.0, 0.0, 0.0);
		return rotTextCTM;
	}

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize) {
		deriveDeviceCTM(scaleFactor, panelSize, 0);
	}

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize,
			int specStartYPt) {
		// Image Ht and Width after Scaling
		double newImgHt = 0.0, newImgWt = 0.0;

		newImgHt = m_ImageHeight;
		newImgWt = m_ImageWidth;
		this.deriveDeviceCTM(scaleFactor, panelSize, specStartYPt, newImgWt,
				newImgHt);
	}

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize,
			int specStartYPt, double newImgWt, double newImgHt) {
		// RGPTLogger.logToFile("IN deriveDeviceCTM METHOD TO CALCULATE CTM FOR: "
		// +
		// scaleFactor.toString());
		// RGPTLogger.logToFile("PANEL SIZE: " + panelSize.toString());
		AffineTransform devCloneCTM = null;
		AffineTransform finalDevCTM = null;
		AffineTransform pgCloneCTM = null;

		// Scaling Parameters
		double sx = 0.0, sy = 0.0;
		// Translation parameters
		double tx = 0.0, ty = 0.0;

		// Defining the Viewable Area
		// double startXPt = m_DeviceDisplayPt.getX();
		// double startYPt = m_DeviceDisplayPt.getY();
		// System.out.println("Display PtX: " + startXPt + " :PtY: " +
		// startYPt);
		// System.out.println("Panel Size: " + panelSize.toString());
		double startXPt = 0.0;
		double startYPt = 0.0;
		if (panelSize.getHeight() > newImgHt) {
			startYPt = (panelSize.getHeight() - newImgHt) / 2;
		}
		// RGPTLogger.logToFile("startYPt: " + startYPt +
		// " Panel Size: " + panelSize.toString());
		if (startYPt < (double) specStartYPt)
			startYPt = (double) specStartYPt;
		double bottomPanelHt = 0.0;
		double viewablePanelHt = (double) panelSize.getHeight() - startYPt
				- bottomPanelHt;
		double viewablePanelWidth = (double) panelSize.getWidth() - startXPt;
		// RGPTLogger.logToFile("viewablePanelHt: " + viewablePanelHt +
		// " ImageHeight: " + newImgHt);
		if (ScalingFactor.FIT_HEIGHT.equals(scaleFactor)) {
			sy = viewablePanelHt / newImgHt;
			if (sy > 1.0)
				sy = 1.0;
			m_Scale = sy;
		} else if (ScalingFactor.FIT_PAGE.equals(scaleFactor)) {
			sy = viewablePanelHt / newImgHt;
			sx = viewablePanelWidth / newImgWt;
			if (sx > sy)
				m_Scale = sy;
			else
				m_Scale = sx;
		} else if (ScalingFactor.FIT_WIDTH.equals(scaleFactor)) {
			sx = viewablePanelWidth / newImgWt;
			if (sx > 1.0)
				sx = 1.0;
			m_Scale = sx;
		} else if (ScalingFactor.ZOOM_IN_OUT.equals(scaleFactor))
			m_Scale = ((double) scaleFactor.m_ZoomValue) / 100;

		m_ScaledPDFPageWd = newImgWt * m_Scale;
		m_ScaledPDFPageHt = newImgHt * m_Scale;

		if (panelSize.getHeight() > m_ScaledPDFPageHt) {
			startYPt = (panelSize.getHeight() - m_ScaledPDFPageHt) / 2;
		}
		if (startYPt < (double) specStartYPt)
			startYPt = (double) specStartYPt;
		// RGPTLogger.logToFile("New startYPt: " + startYPt);

		// Calculating Translations in X and Y dirn.
		ty = startYPt + m_ScaledPDFPageHt;
		// RGPTLogger.logToFile("Scale: " + m_Scale + " Ty: " + ty +
		// " Scaled PageHt: "
		// + m_ScaledPDFPageHt + " Viewable PanelHt: " + viewablePanelHt);
		if (m_ScaledPDFPageWd < viewablePanelWidth) {
			// Devided by 2 to give equal padding on either side of the Image
			// Width
			tx = startXPt + (viewablePanelWidth - m_ScaledPDFPageWd) / 2;
		} else
			tx = startXPt;
		// RGPTLogger.logToFile("Ty: " + ty + " Tx: " + tx);

		// RGPTLogger.logToFile("Calculated WIDTH AND HEIGHT: " +
		// m_ScaledPDFPageWd +
		// ": " + m_ScaledPDFPageHt);

		// RGPTLogger.logToFile("Calculated Scale: " + m_Scale);
		// RGPTLogger.logToFile("Calculated Translations: " + tx + ": " + ty);

		// Setting the Windows Device CTM
		// AffineTransform(double m00, double m10, double m01, double m11,
		// double m02, double m12)
		m_WindowDeviceCTM = new AffineTransform(m_Scale, 0, 0, m_Scale, tx,
				startYPt);
		// RGPTLogger.logToFile("Window Device CTM: " +
		// m_WindowDeviceCTM.toString());

		// Flipping Across the. This needs to accomodate Rotation
		if (m_PageRotation == RotationMatrix.ROTATION_0)
			m_CalcDeviceCTM = new AffineTransform(m_Scale, 0, 0, -m_Scale, tx,
					ty);
		else if (m_PageRotation == RotationMatrix.ROTATION_270) {
			m_CalcDeviceCTM = new AffineTransform(-m_Scale, 0, 0, -m_Scale, tx
					+ m_ScaledPDFPageWd, ty);
		}
		// RGPTLogger.logToFile("CALCULATED Device CTM: " +
		// m_CalcDeviceCTM.toString());

		if (!m_IsVDPFieldDefined) {
			// RGPTLogger.logToFile("NO VDP FIELDS ARE DEFINED. HENCE NO CTM DEFINED");
			return;
		}

		// Getting the Clone of PageCTM. This is to avoid any manipulation of
		// Page CTM
		pgCloneCTM = (AffineTransform) m_PageCTM.clone();

		// Getting the Clone of Device CTMThis is because the Device CTM will be
		// manipulated after Multiplication with PageCTM
		devCloneCTM = (AffineTransform) m_CalcDeviceCTM.clone();

		// This accomodates the Rotation by multiplying the Rotation Matrix with
		// Page
		// and the resultant with Device
		AffineTransform rotCTM = null;
		rotCTM = RotationMatrix.getRotationMatrix(m_PageRotation,
				(double) newImgWt, (double) newImgHt);

		rotCTM.concatenate(pgCloneCTM);
		devCloneCTM.concatenate(rotCTM);
		// RGPTLogger.logToFile("Device concatenate with Page: " +
		// devCloneCTM.toString());

		// Assigning the Final Device CTM
		m_FinalDeviceCTM = devCloneCTM;
		// RGPTLogger.logToFile("Final Device CTM: " +
		// m_FinalDeviceCTM.toString());

		// Calculating the Screen Page Rectangle
		m_ScreenRect = new Rectangle2D.Double(tx, startYPt, newImgWt * m_Scale,
				newImgHt * m_Scale);
		// RGPTLogger.logToFile("Screen Rect is: " + m_ScreenRect.toString());

		// Testing with Original Point (1,1) to new Derived Point using the
		// final
		// transformation finalDevCTM.
		Point2D.Double ptTstSrc = new Point2D.Double(1.0, 1.0);
		Point2D.Double ptTstDes = new Point2D.Double();
		m_FinalDeviceCTM.transform(ptTstSrc, ptTstDes);
		// RGPTLogger.logToFile("PT TEST SRC " + ptTstSrc.toString() +
		// ": Dest: " + ptTstDes.toString());
	}

	// This methos is invoked only for innitial population of UserPageData
	public UserPageData populateUserPageData() {
		return this.populateUserPageData(null, null, false);
	}

	// This methos is invoked only for innitial population of UserPageData
	public UserPageData populateUserPageData(HashMap servPopulatedFlds) {
		return this.populateUserPageData(null, servPopulatedFlds, true);
	}

	// This method is invoked to populate UserPageData in a Batch mode.
	// If not in Batchmode and for initial population of UserPageData, null can
	// be passed as the argument.
	@SuppressWarnings("unchecked")
	public UserPageData populateUserPageData(String assetPath,
			HashMap vdpDataMap, boolean acceptEmptyFld) {
		int counter = 0, minSeqId = 0, maxSeqId = 0;
		HashMap vdpData = null;
		RGPTRectangle rect = null;
		VDPTextFieldInfo vdpTextFieldInfo;
		VDPImageFieldInfo vdpImageFieldInfo;

		UserPageData userPgData = new UserPageData(m_PageNum);
		if (VDPTextFieldInfo.m_UseSequenceId)
			RGPTLogger.logToFile("Use SequenceId");
		else
			RGPTLogger.logToFile("Generate SequenceId");
		for (int i = 0; i < m_VDPTextFieldInfo.size(); i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) m_VDPTextFieldInfo
					.elementAt(i);
			rect = RGPTRectangle
					.getReactangle(vdpTextFieldInfo.m_PageRectangle);
			vdpData = new HashMap();
			if (VDPTextFieldInfo.m_UseSequenceId)
				counter = vdpTextFieldInfo.m_SequenceId;
			else
				counter++;
			if (i == 0) {
				minSeqId = counter;
				maxSeqId = counter;
			}
			if (counter < minSeqId)
				minSeqId = counter;
			if (counter > maxSeqId)
				maxSeqId = counter;
			vdpData.put("BBox", rect);
			vdpData.put("NewBBox", vdpTextFieldInfo.m_NewBBox);
			vdpData.put("FillWidth", 0.0);
			vdpData.put("Tx", 0.0);
			vdpData.put("Ty", 0.0);
			vdpData.put("Counter", counter);
			vdpData.put("FieldType", vdpTextFieldInfo.m_FieldType);
			// This is used if Default Value has to be populated till the user
			// has not
			// defined the VDP Text
			RGPTLogger.logToFile("VDP Fld Name Processed: "
					+ vdpTextFieldInfo.m_FieldName + "  Seq Id: " + counter
					+ " vdpText Seq: " + vdpTextFieldInfo.m_SequenceId);
			vdpData.put("BlankDefaultText", false);
			String defText = vdpTextFieldInfo.m_FieldValue;
			if (defText == null || defText.trim().length() == 0) {
				defText = "Enter " + vdpTextFieldInfo.m_FieldName;
			}
			vdpData.put("DefaultValue", defText);
			vdpData.put("VDPFieldName", vdpTextFieldInfo.m_FieldName);
			vdpData.put("AlternateVDPField",
					vdpTextFieldInfo.m_AlternateVDPField);
			vdpData.put("AllowFieldEdit", true);
			vdpData.put("VDPViewMode", -1);
			vdpData.put("IsFieldOptional", vdpTextFieldInfo.m_IsFieldOptional);
			vdpData.put("IsVDPPrepopulated",
					vdpTextFieldInfo.m_IsVDPPrepopulated);
			vdpData.put("FieldLength", vdpTextFieldInfo.m_FieldLength);
			vdpData.put("UseTitleCase", vdpTextFieldInfo.m_UseTitleCase);
			vdpData.put("FieldLengthFixed", vdpTextFieldInfo.m_FieldLengthFixed);
			vdpData.put("FieldLength", vdpTextFieldInfo.m_FieldLength);
			vdpData.put("TextWidthFixed", vdpTextFieldInfo.m_TextWidthFixed);
			vdpData.put("AutoFitText", vdpTextFieldInfo.m_AutoFitText);
			vdpData.put("VDPTextMode", vdpTextFieldInfo.m_VDPTextMode);
			vdpData.put("TextDataType", vdpTextFieldInfo.m_TextDataType);
			vdpData.put("PrefixValue", vdpTextFieldInfo.m_PrefixValue);
			vdpData.put("SuffixValue", vdpTextFieldInfo.m_SuffixValue);
			vdpData.put("DerivedFont", vdpTextFieldInfo.m_Font);
			vdpData.put("TextMatrix",
					(AffineTransform) vdpTextFieldInfo.m_TextMatrix.clone());
			AffineTransform textCTM = (AffineTransform) vdpData
					.get("TextMatrix");
			RGPTLogger.logToFile("First Text Matrix: " + textCTM.toString());
			if (vdpDataMap != null) {
				String value = (String) vdpDataMap
						.get(vdpTextFieldInfo.m_FieldName);
				if (value == null) {
					if (vdpTextFieldInfo.m_IsFieldOptional
							|| vdpTextFieldInfo.m_IsOverFlowField
							|| acceptEmptyFld)
						value = "";
					else
						throw new RuntimeException("Field undefined: "
								+ vdpTextFieldInfo.m_FieldName);
				}
				vdpData.put("UserSetValue", value);
			}
			vdpData.put("StartPtX", vdpTextFieldInfo.m_StartX);
			vdpData.put("StartPtY", vdpTextFieldInfo.m_StartY);
			vdpData.put("TextAllignment", vdpTextFieldInfo.m_TextAllignment);
			vdpData.put("IsOverFlowField", vdpTextFieldInfo.m_IsOverFlowField);
			vdpData.put("OverFlowVDPField", vdpTextFieldInfo.m_OverFlowVDPField);

			vdpData.put("VerticalTextAllignment",
					vdpTextFieldInfo.m_VertTextAllignment);
			vdpData.put("Rotation", vdpTextFieldInfo.m_RotationAngle);
			vdpData.put("ShearX", vdpTextFieldInfo.m_ShearX);
			vdpData.put("ShearY", vdpTextFieldInfo.m_ShearY);
			vdpData.put("AdjustTextX", vdpTextFieldInfo.m_AdjustTextX);
			vdpData.put("AdjustTextY", vdpTextFieldInfo.m_AdjustTextY);
			vdpData.put("DrawTextOutline", vdpTextFieldInfo.m_DrawTextOutline);
			vdpData.put("FillShapeLogic", vdpTextFieldInfo.m_FillShapeLogic);
			vdpData.put("FillShapeColor", vdpTextFieldInfo.m_FillShapeColor);
			vdpData.put("FillTransperancy", vdpTextFieldInfo.m_FillTransperancy);
			vdpData.put("ClipImageHolder", vdpTextFieldInfo.m_ClipImageHolder);
			vdpData.put("FillShapeImage", vdpTextFieldInfo.m_FillShapeImage);
			vdpData.put("ShapeType", vdpTextFieldInfo.m_ShapeType);
			vdpData.put("FillColor", vdpTextFieldInfo.m_FillColor);
			vdpData.put("IsFieldFixed", vdpTextFieldInfo.m_IsFieldFixed);
			if (vdpTextFieldInfo.m_IsFieldFixed) {
				vdpData.put("UserSetValue", vdpTextFieldInfo.m_FieldValue);
				vdpData.put("AllowFieldEdit", false);
			}
			vdpData.put("IsRotationFirst", true);
			vdpData.put("PDFElementCreated", false);
			userPgData.m_VDPTextData.addElement(vdpData);
		}
		userPgData.m_TextMinSequenceId = minSeqId;
		userPgData.m_TextMaxSequenceId = maxSeqId;

		RGPTLogger.logToFile("Min Seq Id: " + minSeqId + "Max Seq Id: "
				+ maxSeqId);

		counter = 0;
		for (int i = 0; i < m_VDPImageFieldInfo.size(); i++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) m_VDPImageFieldInfo
					.elementAt(i);
			rect = RGPTRectangle
					.getReactangle(vdpImageFieldInfo.m_PageRectangle);
			vdpData = new HashMap();
			counter++;
			vdpData.put("BBox", rect);
			// vdpData.put("Counter", counter);
			vdpData.put("FieldType", "Image");
			vdpData.put("VDPFieldName", vdpImageFieldInfo.m_FieldName);
			vdpData.put("VDPViewMode", -1);
			vdpData.put("AllowFieldEdit", true);
			vdpData.put("ThemeId", vdpImageFieldInfo.m_ThemeId);
			vdpData.put("AllowUploadWithTheme",
					vdpImageFieldInfo.m_AllowUploadWithTheme);
			vdpData.put("IsVDPPrepopulated",
					vdpImageFieldInfo.m_IsVDPPrepopulated);
			vdpData.put("IsBackgroundImage",
					vdpImageFieldInfo.m_IsBackgroundImage);
			vdpData.put("ImageAlphaValue", vdpImageFieldInfo.m_ImageAlphaValue);
			vdpData.put("ShowPictureFrame",
					vdpImageFieldInfo.m_ShowPictureFrame);
			if (vdpImageFieldInfo.m_ShowPictureFrame) {
				vdpData.put("PictureFrameHolder",
						vdpImageFieldInfo.m_PictureFrameHolder);
				vdpData.put("IsOpaquePictureFrame",
						vdpImageFieldInfo.m_IsOpaquePictureFrame);
			}
			vdpData.put("IsClipped", false);
			if (vdpDataMap != null) {
				String imageAbsPath = "";
				String fileName = (String) vdpDataMap
						.get(vdpImageFieldInfo.m_FieldName);
				if (fileName == null)
					throw new RuntimeException("Field undefined: "
							+ vdpImageFieldInfo.m_FieldName);
				ImageHolder imgHldr = new ImageHolder(assetPath, fileName, rect);
				vdpData.put("ImageHolder", imgHldr);
				// imageAbsPath = assetPath +
				// (String) vdpDataMap.get(vdpImageFieldInfo.m_FieldName);
				// vdpData.put("ImageAbsPath", imageAbsPath);
			}
			userPgData.m_VDPImageData.addElement(vdpData);
		}
		return userPgData;
	}

	// This method is invoked to populate the VDP Field Data needed for form
	// based entry
	public void populateFieldData(Vector<Map> textFldData,
			Vector<Map> imgFldData) {
		VDPTextFieldInfo vdpTextField;
		VDPImageFieldInfo vdpImageField;
		System.out.println("Populating FORM Fields for Page: " + m_PageNum);
		System.out.println("# of Text Fields: " + m_VDPTextFieldInfo.size());
		System.out.println("# of Image Fields: " + m_VDPImageFieldInfo.size());

		int seqId = 0;
		HashMap vdpData = null;
		Map<Integer, VDPTextFieldInfo> vdpTxtSeq = null;
		vdpTxtSeq = new HashMap<Integer, VDPTextFieldInfo>();
		for (int i = 0; i < m_VDPTextFieldInfo.size(); i++) {
			vdpTextField = (VDPTextFieldInfo) m_VDPTextFieldInfo.elementAt(i);
			// Not populate the Forn Field if the vdp text field is an overflow
			// field
			if (vdpTextField.m_IsOverFlowField)
				continue;

			if (VDPTextFieldInfo.m_UseSequenceId)
				seqId = vdpTextField.m_SequenceId;
			else
				seqId++;
			int posId = seqId - 1;
			System.out.println("Use Seq Id: "
					+ VDPTextFieldInfo.m_UseSequenceId + " Pos Id: " + posId);
			vdpTxtSeq.put(seqId, vdpTextField);
		}

		Integer[] keys = vdpTxtSeq.keySet().toArray(new Integer[0]);
		for (int i = 0; i < keys.length; ++i) {
			vdpData = new HashMap();
			vdpTextField = vdpTxtSeq.get(keys[i]);
			String defText = vdpTextField.m_FieldValue;
			if (defText == null || defText.trim().length() == 0) {
				defText = "Enter " + vdpTextField.m_FieldName;
			}

			vdpData.put("DefaultValue", defText);
			vdpData.put("FieldName", vdpTextField.m_FieldName);
			vdpData.put("FieldLength", vdpTextField.m_FieldLength);
			vdpData.put("PrefixValue", vdpTextField.m_PrefixValue);
			vdpData.put("SuffixValue", vdpTextField.m_SuffixValue);
			vdpData.put("VDPTextMode", vdpTextField.m_VDPTextMode);
			vdpData.put("IsFieldOptional", vdpTextField.m_IsFieldOptional);
			textFldData.addElement(vdpData);
		}

		for (int i = 0; i < m_VDPImageFieldInfo.size(); i++) {
			vdpData = new HashMap();
			vdpImageField = (VDPImageFieldInfo) m_VDPImageFieldInfo
					.elementAt(i);
			RGPTRectangle rect = RGPTRectangle
					.getReactangle(vdpImageField.m_PageRectangle);
			vdpData.put("FieldName", vdpImageField.m_FieldName);
			vdpData.put("ImageWidth", rect.width);
			vdpData.put("ImageHeight", rect.height);
			imgFldData.addElement(vdpData);
		}
	}

	// This method is invoked to populate the VDP Data needed for generating
	// a CSV File
	public void populateCSVData(Vector csvData) {
		StringBuffer csvField = new StringBuffer();
		VDPTextFieldInfo vdpTextField;
		VDPImageFieldInfo vdpImageField;
		System.out.println("Populating CSV Fields for Page: " + m_PageNum);
		System.out.println("# of Text Fields: " + m_VDPTextFieldInfo.size());
		System.out.println("# of Image Fields: " + m_VDPImageFieldInfo.size());
		for (int i = 0; i < m_VDPTextFieldInfo.size(); i++) {
			vdpTextField = (VDPTextFieldInfo) m_VDPTextFieldInfo.elementAt(i);
			csvField.setLength(0);
			csvField.append(vdpTextField.m_FieldName);
			csvField.append("-" + vdpTextField.m_FieldType);
			csvField.append("(" + vdpTextField.m_FieldLength + ")");
			System.out.println("CSV Field: " + csvField.toString());
			csvData.addElement(csvField.toString());
		}

		for (int i = 0; i < m_VDPImageFieldInfo.size(); i++) {
			vdpImageField = (VDPImageFieldInfo) m_VDPImageFieldInfo
					.elementAt(i);
			csvField.setLength(0);
			csvField.append(vdpImageField.m_FieldName);
			csvField.append("-" + vdpImageField.m_FieldType);
			csvData.addElement(csvField.toString());
		}
	}

	// Serialization
	// public transient static boolean m_IsFontStreamSaved = false;
	public void save(ObjectOutputStream objstream) throws IOException {
		objstream.writeObject(this);
		VDPTextFieldInfo vdpTextFieldInfo = null;
		int size = m_VDPTextFieldInfo.size();
		objstream.writeObject(new Integer(size));

		for (int i = 0; i < size; i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) m_VDPTextFieldInfo
					.elementAt(i);
			vdpTextFieldInfo.save(objstream);
		}

		VDPImageFieldInfo vdpImageFieldInfo;
		objstream.writeObject(new Integer(m_VDPImageFieldInfo.size()));
		for (int i = 0; i < m_VDPImageFieldInfo.size(); i++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) m_VDPImageFieldInfo
					.elementAt(i);
			vdpImageFieldInfo.save(objstream);
		}
	}

	// De-Serialization
	public static Object load(ObjectInputStream objstream) throws Exception {
		PDFPageInfo pdfPage = (PDFPageInfo) objstream.readObject();

		VDPTextFieldInfo vdpTextFieldInfo = null;
		int vdptxtfldsize = ((Integer) objstream.readObject()).intValue();
		pdfPage.m_VDPTextFieldInfo = new Vector();
		for (int i = 0; i < vdptxtfldsize; i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) objstream.readObject();
			vdpTextFieldInfo.load(objstream);
			pdfPage.m_VDPTextFieldInfo.addElement(vdpTextFieldInfo);
		}

		VDPImageFieldInfo vdpImageFieldInfo;
		int vdpimgfldsize = ((Integer) objstream.readObject()).intValue();
		pdfPage.m_VDPImageFieldInfo = new Vector();
		for (int i = 0; i < vdpimgfldsize; i++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) objstream.readObject();
			vdpImageFieldInfo.load(objstream);
			pdfPage.m_VDPImageFieldInfo.addElement(vdpImageFieldInfo);
		}

		return pdfPage;
	}

	public String toString() {
		VDPTextFieldInfo vdpTextFieldInfo;
		VDPImageFieldInfo vdpImageFieldInfo;

		StringBuffer mesg = new StringBuffer("PDF Page Info for Page# "
				+ m_PageNum);
		mesg.append("\nSerialized Version: " + serialVersionUID + "\n");
		mesg.append(" : Pg Width: " + m_PageWidth);
		mesg.append(" : Pg Height: " + m_PageHeight);
		mesg.append(" : Pg Rotation: " + m_PageRotation);
		mesg.append(" : Pg Scale: " + m_PageScale);
		try {
			if (m_BufferedImage != null) {
				mesg.append("\nImage Width: " + m_BufferedImage.getWidth());
				mesg.append(" : Image Height: " + m_BufferedImage.getHeight());
			}
			mesg.append("\nPage CTM: " + m_PageCTM.toString());
			mesg.append("\nOrig Page CTM: " + m_OrigPageCTM.toString());
			mesg.append("\nDevice CTM: " + m_DeviceCTM.toString());
			mesg.append("\nCALC Device CTM: " + m_CalcDeviceCTM.toString());
			mesg.append("\nWINDOWS Device CTM: " + m_WindowDeviceCTM.toString());
			mesg.append("\nFINAL Device CTM: " + m_FinalDeviceCTM.toString());
		} catch (Exception ex) {
		}

		for (int i = 0; i < m_VDPTextFieldInfo.size(); i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) m_VDPTextFieldInfo
					.elementAt(i);
			vdpTextFieldInfo.toString(mesg);
		}
		for (int i = 0; i < m_VDPImageFieldInfo.size(); i++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) m_VDPImageFieldInfo
					.elementAt(i);
			vdpImageFieldInfo.toString(mesg);
		}
		return mesg.toString();
	}
}
