// RGPT PACKAGES
package com.rgpt.templateutil;

// This files are added to support serialization
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTUtil;

public class UserPageData implements Serializable {
	public int m_PageNum;
	public static final long serialVersionUID = 615419130690177505L;

	// NOTE:
	// The BBox will be RGPTRectangle. This is because Rectangle2D is non
	// Serializable. So Rectangle2D is converted to RGPTRectangle which is
	// reconverted to Rectangle2D in Server

	// This Holds the VDP Text Fields for the Page. Each Element in the Vector
	// is
	// HashMap which holds the following Keys BBox, DefaultValue, and
	// UserSetValue. Make sure the BBox Key is the same for both Text Data
	// and Image Data.
	public Vector m_VDPTextData;

	// This Holds the VDP Image Fields for the Page. Each Element in the Vector
	// is
	// HashMap which holds the following Keys BBox, and AssetId
	public Vector m_VDPImageData;

	public int m_TextMinSequenceId = -1;
	public int m_TextMaxSequenceId = -1;

	public UserPageData(int pageNum) {
		m_PageNum = pageNum;
		m_VDPTextData = new Vector();
		m_VDPImageData = new Vector();
		// m_PDFPageInfoManager = pdfPageInfoManager;
	}

	public HashMap getNextVDPData(int currCounter) {
		HashMap vdpData = null;
		Vector vdpPageData = null;
		// Checking Text Data
		vdpPageData = m_VDPTextData;
		if (vdpPageData.size() == 0)
			return null;
		if (currCounter == -1)
			currCounter = m_TextMinSequenceId;
		RGPTLogger.logDebugMesg("Counter for VDP Sel: " + currCounter
				+ " vdpPageData size: " + vdpPageData.size());
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			String fldName = (String) vdpData.get("VDPFieldName");
			int counter = ((Integer) vdpData.get("Counter")).intValue();
			boolean isVDPPrePopulated = ((Boolean) vdpData
					.get("IsVDPPrepopulated")).booleanValue();
			RGPTLogger.logToFile("VDP Data: " + fldName + " counter: "
					+ counter + "isVDPPrePopulated: " + isVDPPrePopulated);
			if (counter != currCounter)
				continue;
			if (isVDPPrePopulated)
				return getNextVDPData(currCounter + 1);
			boolean isOverFlowFld = ((Boolean) vdpData.get("IsOverFlowField"))
					.booleanValue();
			if (isOverFlowFld)
				return getNextVDPData(currCounter + 1);
			RGPTLogger.logToFile("Returning VDP Data: " + fldName + " for: "
					+ currCounter);
			return vdpData;
		}
		if (currCounter > m_TextMinSequenceId
				&& currCounter < m_TextMaxSequenceId)
			return getNextVDPData(currCounter + 1);
		/*
		 * vdpPageData = m_VDPImageData; for (int i = 0; i < vdpPageData.size();
		 * i++) { vdpData = (HashMap) vdpPageData.elementAt(i); int counter =
		 * ((Integer)vdpData.get("Counter")).intValue(); if (counter !=
		 * currCounter) continue; return vdpData; }
		 */
		// Incase next counter is not found, then return the first counter
		return getNextVDPData(m_TextMinSequenceId);
	}

	public boolean m_AssignedSeqId = false;

	public void assignSeqId() {
		this.assignSeqId(null);
	}

	public void assignSeqId(PDFPageInfo pgInfo) {
		HashMap vdpData = null, vdpDataSeq = new HashMap();
		Vector vdpPageData = null;
		// Checking Text Data
		vdpPageData = m_VDPTextData;
		if (vdpPageData.size() == 0)
			return;
		// System.out.println("VDPData are: " + vdpPageData.toString());
		RGPTLogger.logToFile("Assigning Auto Seq..");
		if (pgInfo != null)
			RGPTLogger.logToFile("For Page: " + pgInfo.m_PageNum);
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			// boolean isOverFlowFld = ((Boolean)
			// vdpData.get("IsOverFlowField")).
			// booleanValue();
			// if (isOverFlowFld) continue;
			String fldName = (String) vdpData.get("VDPFieldName");
			System.out.println("Calculating Seq for VDPData: " + fldName);
			int seqId = getSeqId(vdpData);
			if (seqId == -1)
				return;
			System.out.println("Calculated Seq for VDPData: " + fldName
					+ " is: " + seqId);
			if (vdpDataSeq.get(seqId) != null)
				return;
			vdpDataSeq.put(seqId, vdpData);
		}
		Object[] seqIds = vdpDataSeq.keySet().toArray(new Object[0]);
		int minSeqId = 10000, maxSeqId = -1;
		VDPTextFieldInfo vdpTextFld = null;
		Rectangle2D.Double vdpbbox = null;
		for (int i = 0; i < seqIds.length; i++) {
			int seqId = ((Integer) seqIds[i]).intValue();
			if (minSeqId > seqId)
				minSeqId = seqId;
			if (maxSeqId < seqId)
				maxSeqId = seqId;
			vdpData = (HashMap) vdpDataSeq.get(seqIds[i]);
			vdpData.put("Counter", seqIds[i]);
			RGPTLogger.logToFile("Setting the seq: " + seqId + " for: "
					+ (String) vdpData.get("VDPFieldName"));
			if (pgInfo == null)
				continue;
			vdpbbox = ((RGPTRectangle) vdpData.get("BBox")).getRectangle2D();
			vdpTextFld = (VDPTextFieldInfo) pgInfo.getVDPFieldInfo("Text",
					vdpbbox);
			if (vdpTextFld != null) {
				VDPTextFieldInfo.m_UseSequenceId = true;
				vdpTextFld.m_SequenceId = seqId;
				RGPTLogger.logToFile("The Seq Id: " + vdpTextFld.m_FieldName
						+ " is: " + vdpTextFld.m_SequenceId);
			} else
				RGPTLogger.logToFile("Unable to Retrieve Fld: "
						+ (String) vdpData.get("VDPFieldName"));
		}
		// System.out.println("Calculated Seq for VDPData is: " +
		// vdpDataSeq.toString());
		// System.out.println("Minimum Seq Id: " + m_TextMinSequenceId +
		// " :Max Seq Id: " + m_TextMaxSequenceId);
		m_AssignedSeqId = true;
		m_TextMinSequenceId = minSeqId;
		m_TextMaxSequenceId = maxSeqId;
	}

	private int getSeqId(HashMap vdpData) {
		HashMap nextVDPData = null;
		Vector vdpPageData = null;
		// Checking Text Data
		vdpPageData = m_VDPTextData;
		if (vdpPageData.size() == 0)
			return -1;
		RGPTRectangle devBBox = null, nextDevBBox = null;
		Rectangle devRect = null, nextDevRect = null;
		int seqId = 0;
		devBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
		if (devBBox == null)
			return -1;
		devRect = devBBox.getRectangle();
		for (int i = 0; i < vdpPageData.size(); i++) {
			nextVDPData = (HashMap) vdpPageData.elementAt(i);
			if (vdpData.equals(nextVDPData))
				continue;
			boolean isOverFlowFld = ((Boolean) nextVDPData
					.get("IsOverFlowField")).booleanValue();
			if (isOverFlowFld)
				continue;
			nextDevBBox = (RGPTRectangle) nextVDPData.get("DeviceBBox");
			if (nextDevBBox == null)
				return -1;
			if (devBBox.equals(nextDevBBox))
				continue;
			nextDevRect = nextDevBBox.getRectangle();
			if ((devRect.x == nextDevRect.x && devRect.y > nextDevRect.y)
					|| (devRect.y == nextDevRect.y && devRect.x > nextDevRect.x)
					|| (devRect.x > nextDevRect.x && devRect.y > nextDevRect.y)
					|| (devRect.x < nextDevRect.x && devRect.y > nextDevRect.y))
				seqId++;
		}
		return (seqId + 1);

	}

	// This returns the HashMap of VDP Data with Device Points
	public void mergeVDPDataFields(UserPageData userSpecPageData) {
		String vdpDataFld = "";
		HashMap vdpData = null, usrSpecVDPData = null;
		Vector vdpPageData = m_VDPTextData;
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			vdpDataFld = (String) vdpData.get("VDPFieldName");
			usrSpecVDPData = userSpecPageData.getVDPData(vdpDataFld);
			if (usrSpecVDPData == null)
				continue;
			RGPTLogger.logToFile("Found User Spec Field: " + vdpDataFld
					+ " User Set Value: " + usrSpecVDPData.get("UserSetValue")
					+ " User Set Final Value: "
					+ usrSpecVDPData.get("UserSetFinalValue"));
			Object[] keys = usrSpecVDPData.keySet().toArray(new Object[0]);
			for (int j = 0; j < keys.length; j++) {
				String key = (String) keys[j];
				vdpData.put(key, usrSpecVDPData.get(key));
			}
			RGPTLogger.logToFile("Merged User Set Value: "
					+ vdpData.get("UserSetValue") + " User Set Final Value: "
					+ vdpData.get("UserSetFinalValue"));
		}
		vdpPageData = m_VDPImageData;
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			vdpDataFld = (String) vdpData.get("VDPFieldName");
			usrSpecVDPData = this.getVDPData(vdpDataFld);
			if (usrSpecVDPData == null)
				continue;
			Object[] keys = usrSpecVDPData.keySet().toArray(new Object[0]);
			for (int j = 0; j < keys.length; j++) {
				String key = (String) keys[j];
				vdpData.put(key, usrSpecVDPData.get(key));
			}
		}
	}

	// This returns the HashMap of VDP Data with Device Points
	public HashMap getVDPData(String vdpFieldName) {
		String vdpDataFld = "";
		HashMap vdpData = null;
		Vector vdpPageData = m_VDPTextData;
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			vdpDataFld = (String) vdpData.get("VDPFieldName");
			if (vdpDataFld.equals(vdpFieldName))
				return vdpData;
		}
		vdpPageData = m_VDPImageData;
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			vdpDataFld = (String) vdpData.get("VDPFieldName");
			if (vdpDataFld.equals(vdpFieldName))
				return vdpData;
		}
		return null;
	}

	// This returns the HashMap of VDP Data with Device Points
	public void resetAllPrepopulatedFields(String vdpFieldType) {
		String vdpDataFld = "";
		HashMap vdpData = null;
		Vector vdpPageData = getAllVDPData(vdpFieldType);
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			vdpDataFld = (String) vdpData.get("VDPFieldName");
			boolean isVDPPrePopulated = ((Boolean) vdpData
					.get("IsVDPPrepopulated")).booleanValue();
			if (isVDPPrePopulated) {
				vdpData.put("IsVDPPrepopulated", false);
				vdpData.put("AllowFieldEdit", true);
			}
		}
	}

	// This returns the HashMap of VDP Data with Device Points
	public Vector getAllVDPDataForField(String vdpFieldType, String vdpFieldName) {
		String vdpDataFld = "";
		HashMap vdpData = null;
		Vector vdpSelData = new Vector();
		Vector vdpPageData = getAllVDPData(vdpFieldType);
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			vdpDataFld = (String) vdpData.get("VDPFieldName");
			if (vdpDataFld.equals(vdpFieldName))
				vdpSelData.addElement(vdpData);
		}
		return vdpSelData;
	}

	// This sets User Set Value on all the VDP Data having the same field Name
	public void setUserValue(HashMap origVDPData) {
		String vdpDataFld = "";
		HashMap vdpData = null;
		int origCounter = ((Integer) origVDPData.get("Counter")).intValue();
		String userSetValue = (String) origVDPData.get("UserSetValue");
		String vdpFldName = (String) origVDPData.get("VDPFieldName");
		Vector vdpPageData = getAllVDPDataForField("Text", vdpFldName);
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			int counter = ((Integer) vdpData.get("Counter")).intValue();
			if (counter == origCounter)
				continue;
			String userFinalVal = (String) vdpData.get("UserSetFinalValue");
			// If the User Value is already Set then the changes are not
			// allowed.
			if (userFinalVal == null || userFinalVal.length() == 0)
				continue;
			vdpData.put("UserSetValue", userSetValue);
		}
	}

	// This returns the HashMap of VDP Data with Device Points
	public Vector getAllVDPData(String vdpFieldType) {
		return this.getVDPData(vdpFieldType, -1.0, -1.0, false);
	}

	// This returns the HashMap of VDP Data with Device Points
	public Vector getVDPData(String vdpFieldType, double x, double y) {
		Vector vdpSelData = this.getVDPData(vdpFieldType, x, y, false);
		if (vdpSelData.size() > 0)
			return vdpSelData;
		return this.getVDPData(vdpFieldType, x, y, true);
	}

	public Vector getVDPData(String vdpFieldType, double x, double y,
			boolean useMargin) {
		Vector vdpPageData = null;
		HashMap vdpData = null;
		Rectangle2D.Double bbox = null;

		// This will return multiple selections for x and y when vdp field type
		// is of image. Otherwise single selection is returned.
		Vector vdpSelData = new Vector();
		if (vdpFieldType.equals("Text")
				|| vdpFieldType.equals("TextOnGraphics")) {
			if (!VDPTextFieldInfo.UseTextInRect) {
				if (vdpFieldType.equals("TextOnGraphics"))
					return vdpSelData;
				vdpPageData = m_VDPTextData;
			}
			vdpPageData = new Vector();
			for (int i = 0; i < m_VDPTextData.size(); i++) {
				vdpData = (HashMap) m_VDPTextData.elementAt(i);
				boolean isOverFlowFld = ((Boolean) vdpData
						.get("IsOverFlowField")).booleanValue();
				if (isOverFlowFld)
					continue;
				String fldType = (String) vdpData.get("FieldType");
				if (fldType.equals(vdpFieldType))
					vdpPageData.addElement(vdpData);
			}
		} else if (vdpFieldType == "Image")
			vdpPageData = m_VDPImageData;
		else
			throw new RuntimeException("Wrong VDP Field Type: " + vdpFieldType);

		if (x == -1 && y == -1)
			return vdpPageData;

		// Finding the Match
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			Object obj = vdpData.get("DeviceBBox");
			if (obj == null)
				continue;
			// throw new RuntimeException("DeviceBBox Key Must un-defined");
			bbox = ((RGPTRectangle) obj).getRectangle2D();

			// Match is Found
			if (useMargin) {
				double x1 = bbox.getX(), y1 = bbox.getY(), w = bbox.getWidth(), h = bbox
						.getHeight();
				// This defines the margin to be added to the Rectangle while
				// filling.
				// This fill rectangle are the ones identified for entering VDP
				// text.
				int margin = 5;

				// Applying Margins
				Rectangle2D.Double newBBox = null;
				newBBox = new Rectangle2D.Double(x1 - margin, y1 - margin, w
						+ 1.5 * margin, h + 1.5 * margin);
				if (!newBBox.contains(x, y))
					continue;
			} else if (!bbox.contains(x, y))
				continue;
			if (vdpFieldType.equals("Text")
					|| vdpFieldType.equals("TextOnGraphics")) {
				vdpSelData.addElement(vdpData);
				return vdpSelData;
			}
			boolean isAllowEdit = ((Boolean) vdpData.get("AllowFieldEdit"))
					.booleanValue();
			// Handling VDP Image Type
			if (isAllowEdit)
				vdpSelData.addElement(vdpData);
		}
		return vdpSelData;
	}

	public HashMap getContainedVDPField(Vector<HashMap> vdpDataList) {
		HashMap vdpData = null, selVDPData = vdpDataList.elementAt(0);
		RGPTRectangle vdpBBox = null;
		RGPTRectangle selBBox = (RGPTRectangle) selVDPData.get("DeviceBBox");
		Rectangle vdpRect = null;
		Rectangle selRect = RGPTUtil.getRectangle(selBBox.getRectangle2D());
		for (int i = 1; i < vdpDataList.size(); i++) {
			vdpData = vdpDataList.elementAt(i);
			vdpBBox = (RGPTRectangle) vdpData.get("DeviceBBox");
			vdpRect = RGPTUtil.getRectangle(vdpBBox.getRectangle2D());
			if (vdpBBox.contains(selBBox) || vdpRect.contains(selRect))
				continue;
			if (selBBox.contains(vdpBBox) || selRect.contains(vdpRect)) {
				selVDPData = vdpData;
				selBBox = vdpBBox;
				selRect = vdpRect;
				continue;
			}
			return null;
		}
		return selVDPData;
	}

	// This returns the HashMap of VDP Data with Page Points
	public HashMap getVDPData(String vdpFieldType, Rectangle2D.Double bbox) {
		Vector vdpPageData = null;
		HashMap vdpData = null;
		Rectangle2D.Double vdpbbox = null;

		if (vdpFieldType.equals("Text")
				|| vdpFieldType.equals("TextOnGraphics")) {
			if (!VDPTextFieldInfo.UseTextInRect) {
				if (vdpFieldType.equals("TextOnGraphics"))
					return null;
				vdpPageData = m_VDPTextData;
			}
			vdpPageData = new Vector();
			for (int i = 0; i < m_VDPTextData.size(); i++) {
				vdpData = (HashMap) m_VDPTextData.elementAt(i);
				String fldType = (String) vdpData.get("FieldType");
				if (fldType.equals(vdpFieldType))
					vdpPageData.addElement(vdpData);
			}
		} else if (vdpFieldType == "Image")
			vdpPageData = m_VDPImageData;
		else
			throw new RuntimeException("Wrong VDP Field Type: " + vdpFieldType);

		// Finding the Match
		Rectangle2D.Double intersectBBox = null;
		// Vector selImgData = new Vector();
		for (int i = 0; i < vdpPageData.size(); i++) {
			vdpData = (HashMap) vdpPageData.elementAt(i);
			Object obj = vdpData.get("BBox");
			if (obj == null)
				throw new RuntimeException("BBox Key Must un-defined");

			vdpbbox = ((RGPTRectangle) obj).getRectangle2D();
			intersectBBox = (Rectangle2D.Double) vdpbbox
					.createIntersection(bbox);

			// Checking for Image Field
			if (vdpFieldType == "Image") {
				if (vdpbbox.equals(bbox))
					return vdpData;
				Rectangle elemRect = RGPTUtil.getRectangle(bbox);
				Rectangle imgRect = RGPTUtil.getRectangle(vdpbbox);
				if (imgRect.equals(elemRect))
					return vdpData;
				// selImgData.addElement(vdpData);
				continue;
			}

			// Checking for Text Field
			// RGPTLogger.logToFile("bbox is: "+bbox+" \nvdpbbox is: "+vdpbbox+
			// " \nIntersectBBox: "+intersectBBox);
			// if (vdpbbox.equals(bbox) || vdpbbox.contains(intersectBBox) ||
			// bbox.contains(intersectBBox))
			if (vdpbbox.equals(bbox) || vdpbbox.equals(intersectBBox)
					|| bbox.equals(intersectBBox)) {
				return vdpData;
			}

			if (!vdpbbox.contains(intersectBBox))
				continue;

			// This calculates howmuch percentage of intersect area is
			// coontained
			// inside the BBox. This calculation assumes the Word is smaller
			// then the text.
			double intersectWidth = intersectBBox.getWidth();
			double intersectHt = intersectBBox.getHeight();
			double percentWordInside = (intersectWidth * intersectHt)
					/ (vdpbbox.getHeight() * vdpbbox.getWidth());
			// This calculation assumes the Element is smaller then the Word
			// text.
			double percentElemInside = (intersectWidth * intersectHt)
					/ (bbox.getHeight() * bbox.getWidth());
			// If the intersection Rectangle Occupies 98% of the area of the
			// Word
			// or Elem BBox, then the case 5 is satisfied
			// RGPTLogger.logToFile("% Intersection Rect inside the Word BBox: "
			// +
			// percentWordInside + " :Elem Inside: " + percentElemInside);
			if (percentWordInside > 0.70 || percentElemInside > 0.70)
				return vdpData;

		}
		return null;
	}

	public HashMap getLineSel(Vector vdpTextLines, Rectangle2D.Double elemBBox) {
		HashMap lineAttrs = null;
		Rectangle2D lineBBox = null;
		for (int i = 0; i < vdpTextLines.size(); i++) {
			lineAttrs = (HashMap) vdpTextLines.elementAt(i);
			lineBBox = ((RGPTRectangle) lineAttrs.get("LineBBox"))
					.getRectangle2D();
			Rectangle2D intersectRect = lineBBox.createIntersection(elemBBox);
			if (lineBBox.contains(elemBBox) || elemBBox.contains(lineBBox)
					|| lineBBox.contains(intersectRect)) {
				return lineAttrs;
			}
		}
		return null;
	}

	// This Checks if the VDP Fields both Text and Images are populated for the
	// Page.
	// This function Returns the number of Text and Image fields that are not
	// populated.
	public HashMap getNonPopulatedFields() {
		String vdpText, imageAbsPath;
		int numNonPopTextFld = 0, numNonPopImgFld = 0;
		HashMap vdpData = null;
		HashMap nonPopulatedFields = new HashMap();
		for (int i = 0; i < m_VDPTextData.size(); i++) {
			vdpData = (HashMap) m_VDPTextData.elementAt(i);
			// Checking if BBox is populated
			Object obj = vdpData.get("BBox");
			if (obj == null)
				throw new RuntimeException("BBox Key Must un-defined");
			// If Field is Optional no Check is done.
			boolean isFldOptional = ((Boolean) vdpData.get("IsFieldOptional"))
					.booleanValue();
			if (isFldOptional)
				continue;
			boolean isOverFlowFld = ((Boolean) vdpData.get("IsOverFlowField"))
					.booleanValue();
			if (isOverFlowFld)
				continue;
			vdpText = (String) vdpData.get("UserSetValue");
			boolean isFieldFixed = ((Boolean) vdpData.get("IsFieldFixed"))
					.booleanValue();
			if (!isFieldFixed)
				if (vdpText == null || vdpText.length() == 0)
					numNonPopTextFld++;
		}
		for (int i = 0; i < m_VDPImageData.size(); i++) {
			vdpData = (HashMap) m_VDPImageData.elementAt(i);
			// Checking if BBox is populated
			Object obj = vdpData.get("BBox");
			if (obj == null)
				throw new RuntimeException("BBox Key Must un-defined");
			ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
			if (imgHldr == null)
				numNonPopImgFld++;
		}
		nonPopulatedFields.put("TextField", numNonPopTextFld);
		nonPopulatedFields.put("ImageField", numNonPopImgFld);
		return nonPopulatedFields;
	}

	// Handle Text Prior to Serialization
	private void handleTextData() {
		HashMap vdpData = null;
		for (int i = 0; i < m_VDPTextData.size(); i++) {
			vdpData = (HashMap) m_VDPTextData.elementAt(i);
			// Checking if BBox is populated
			Object obj = vdpData.get("BBox");
			if (obj == null)
				throw new RuntimeException("BBox Key Must un-defined");

			// Removing the DeviceBBox Field from the HahsMap. This is used only
			// for
			// getting VDP Data from Screen Points
			vdpData.remove("DeviceBBox");
			vdpData.remove("VDPViewMode");
			vdpData.remove("SelPt");
			vdpData.remove("MouseDragX");
			vdpData.remove("MouseDragY");
			vdpData.remove("FinalCTM");
			vdpData.remove("FinalInvCTM");
			vdpData.remove("FillWidth");
			vdpData.remove("TextRect");
			vdpData.remove("BlankDefaultText");
			vdpData.remove("DerivedFont");
		}
	}

	// Handle Image Prior to Serialization
	private void handleImageData() throws Exception {
		HashMap vdpData = null;
		System.out.println("Handling Image Data");
		for (int i = 0; i < m_VDPImageData.size(); i++) {
			vdpData = (HashMap) m_VDPImageData.elementAt(i);
			// Checking if BBox is populated
			Object obj = vdpData.get("BBox");
			if (obj == null)
				throw new RuntimeException("BBox Key Must un-defined");

			// Removing the DeviceBBox Field from the HahsMap. This is used only
			// for
			// getting VDP Data from Screen Points
			vdpData.remove("DeviceBBox");
			// Removing the Buffered Image from the HashMap
			vdpData.remove("UserSetImage");
			vdpData.remove("TempAssetId");
		}
	}

	public void cleanUserData() {
		try {
			this.handleTextData();
			this.handleImageData();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Serialization Method is invoked to handle Text and Image Data.
	// Predominantly
	// to check if all the VDP datas are populated. Further in case of Image to
	// remove unwanted fields like the BufferedImage from the HashMap.
	// Note: No De-serialization Method is implemented as all the data in
	// Serialized
	// File need to be populated.
	public void save(ObjectOutputStream objstream) throws Exception {
		// Handling Text and Images Data
		this.handleTextData();
		this.handleImageData();
		objstream.writeObject(this);
	}

	public int getNumVDPFlds() {
		return m_VDPTextData.size() + m_VDPImageData.size();
	}

	public String toString() {
		StringBuffer mesg = new StringBuffer(
				"\n----USER DEFINED DATA FOR PAGE: ");
		mesg.append(m_PageNum + " -----");
		mesg.append("\n---VDP TEXT DATA---\n");
		mesg.append(m_VDPTextData.toString());
		mesg.append("\n---VDP IMAGE DATA---\n");
		mesg.append(m_VDPImageData.toString());
		return mesg.toString();
	}
}
