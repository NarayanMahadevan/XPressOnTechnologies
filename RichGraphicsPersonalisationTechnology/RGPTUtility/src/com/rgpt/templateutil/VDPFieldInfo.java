// RGPT PACKAGES
package com.rgpt.templateutil;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
// This files are added to support serialization
import java.io.Serializable;
import java.util.Vector;

import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.StaticFieldInfo;

public class VDPFieldInfo implements Serializable {
	static final long serialVersionUID = -7503125934615955512L;
	// This are common for all VDP Field Type either Text, Image and future
	// Clipping Path. They are Field Type, Name, Length, and Value
	public String m_FieldType;
	public String m_FieldLength;
	public String m_FieldName;
	public String m_FieldValue;
	public boolean m_IsVDPPrepopulated = false;
	public AffineTransform m_ElementCTM;

	// This field is used to hide or show the Image Element
	public boolean m_ShowElement = false;

	// This will be useful during Processing. Default is set to true because all
	// the processing is
	// currently done assuming it is a PDF Element
	public boolean m_IsPDFElement = true;

	// This are in Image Points of where the field is located w.r.t to the
	// Canvas Image.
	public Vector<Point2D.Double> m_GPathPoints = null;

	// Path Selection to Draw the Grahic Path Points. This corresponds to
	// ImageActions
	// in RGPTActionListener
	public String m_PathSelection = RGPTActionListener.ImageActions.SHAPE_LINE_PATH_MENU
			.toString();

	public double m_AlphaValue = 1.0;

	// This is only temporary maintained for Screen Selection
	public transient java.awt.Rectangle m_ScreenBounds = null;

	// This maintainds the position if the image in the Preview Section of the
	// screen.
	// Its a transient object
	public transient Vector<Point2D.Double> m_ScreenPathPoints = null;
	public transient java.awt.Rectangle m_PreviewPos = null;

	// Transient Preview Image
	public transient BufferedImage m_PreviewImage = null;

	// This is a Transient field which indicates if the Field is Selected for
	// modification
	public transient boolean m_IsFieldSelected = false;
	public transient boolean m_IsBGCanvasSelected = false;

	// This is a Transient field which indicates to show the Boundary Points for
	// scale
	public transient boolean m_ShowScaleBounds = false;

	// This is a Transient field which indicates to show the Boundary Points for
	// Rotation
	public transient boolean m_ShowRotBounds = false;

	// This is a Transient field which indicates to show the Path Points
	public transient boolean m_ShowPathPoints = false;

	// This is a Transient Field that specifies the Field Action Selected by the
	// User
	private transient String m_FieldAction = null;

	// public boolean m_AllowResizeNMove;
	// public boolean m_AllowDelete;
	// public boolean m_AllowAllignmentWRTBBox;
	// public boolean m_AllowAllignmentWRTPage;
	// public boolean m_UseMappedData;

	// Page, Screen and Canvas coordinates of where this VDP Field is
	// located in the Page
	public transient Rectangle2D.Double m_PageRectangle;

	public VDPFieldInfo() {
	}

	public VDPFieldInfo(String type, String name, String length, String value,
			boolean isPDFElem) {
		m_ShowElement = false;
		m_FieldName = name;
		m_FieldType = type;
		m_FieldLength = length;
		m_FieldValue = value;
		m_IsPDFElement = isPDFElem;
	}

	public void buildProperFields() {
	}

	public BufferedImage getPreviewImage() {
		return m_PreviewImage;
	}

	public void setFieldSelection() {
		m_IsFieldSelected = true;
		m_ShowElement = true;
	}

	public void resetFields() {
		m_FieldAction = null;
		m_ShowScaleBounds = false;
		m_ShowRotBounds = false;
		m_ShowPathPoints = false;
	}

	public void setFieldActions(RGPTActionListener.IDAppsActions action) {
		m_FieldAction = null;
		m_ShowScaleBounds = false;
		m_ShowRotBounds = false;
		m_ShowPathPoints = false;
		switch (action) {
		case TB_SCALE_SHAPE:
			m_ShowScaleBounds = true;
			m_FieldAction = action.toString();
			break;
		case TB_ROTATE_SHAPE:
			m_ShowRotBounds = true;
			m_FieldAction = action.toString();
			break;
		case TB_EDIT_SHAPE:
			m_ShowPathPoints = true;
			m_FieldAction = action.toString();
			break;
		}
	}

	public boolean isTextField() {
		if (m_FieldType.equals(StaticFieldInfo.VDPFieldType.Text.toString()))
			return true;
		return false;
	}

	public boolean isImageField() {
		if (m_FieldType.equals(StaticFieldInfo.VDPFieldType.Image.toString()))
			return true;
		return false;
	}

	public boolean isShapeField() {
		if (m_FieldType.equals(StaticFieldInfo.VDPFieldType.Shape.toString()))
			return true;
		return false;
	}

	public void setGPathPts(AffineTransform screenToImageCTM, boolean isScaled)
			throws Exception {
		throw new RuntimeException("setGPathPts method is not implemented by: "
				+ this.getClass().getName());
	}

	public void save(ObjectOutputStream objstream) throws IOException {
		if (m_IsPDFElement)
			RGPTRectangle.getReactangle(m_PageRectangle).save(objstream);
	}

	// De-Serialization
	public Object load(ObjectInputStream objstream) throws Exception {
		if (m_IsPDFElement)
			this.m_PageRectangle = (Rectangle2D.Double) RGPTRectangle
					.load(objstream);
		return this;
	}

	public String toString() {
		return this.toString(new StringBuffer());
	}

	public String toString(StringBuffer mesg) {
		mesg.append("\n-----VDP FIELD INFO.-----\n");
		mesg.append("Field Type: " + m_FieldType);
		mesg.append(" : Field Name: " + m_FieldName);
		mesg.append(" : Field Length: " + m_FieldLength);
		mesg.append(" : Field Value: " + m_FieldValue);
		mesg.append(" : Is VDP Field Prepopulated: " + m_IsVDPPrepopulated);
		try {
			mesg.append("\n   Field Page Rect: " + m_PageRectangle.toString());
			mesg.append("\n   Element CTM: " + m_ElementCTM.toString());
		} catch (Exception ex) {
		}
		return mesg.toString();
	}
}
