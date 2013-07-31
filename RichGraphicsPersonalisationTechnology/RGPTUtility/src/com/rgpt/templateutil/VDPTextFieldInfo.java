// RGPT PACKAGES
package com.rgpt.templateutil;

// Handling Fonts and Setting Font Attributes
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
// Text Metrices
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
// Handling Font Files
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
// This files are added to support serialization
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.serverhandler.PDFPageHandler;
import com.rgpt.util.FontStreamHolder;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.StaticFieldInfo;
import com.rgpt.util.TextFormatter;

public class VDPTextFieldInfo extends VDPFieldInfo implements Serializable {
	// This defines the current RGPT PDF VERSION
	public static final long serialVersionUID = 6235235076079270866L;

	// AWT Color
	public int m_FillColor;
	// public String m_FillColorSpace;
	public String m_FontName;
	public double m_FontSize;
	public int m_FontWeight;
	public String m_FontFamily;
	public int m_FontType;
	public String m_FontFlags;
	public String m_FontBBox;
	public String m_GenFontName;
	public String m_GenFontWeight;
	public String m_TextAllignment;

	// Text In Rect Fields and Text Along Graphics Fields
	public String m_VertTextAllignment = "";
	public double m_RotationAngle = 0.0;
	public double m_ShearX = 0.0;
	public double m_ShearY = 0.0;
	public boolean m_AutoFitText = false;
	public boolean m_TextWidthFixed = false;
	public boolean m_IsFieldFixed = false;
	public boolean m_DrawTextOutline = false;
	public int m_ShapeType = -1;
	public Vector<Point2D.Double> m_GraphicPathPoints = null;
	public double m_AdjustTextX = 0.0;
	public double m_AdjustTextY = 0.0;
	public String m_FillShapeLogic = "";
	public int m_FillShapeColor = -1;
	public int m_FillTransperancy = -1;
	public ImageHolder m_ClipImageHolder = null;
	public ImageHolder m_FillShapeImage = null;

	// NEW FIELD - IsFieldOptional. If set then the User need not provide Entry
	// while personalizing the Templatized PDF. Added as of Sept 13, 2009
	public boolean m_IsFieldOptional = false;

	// NEW FIELD - To store the original value - Jun 25,2010
	public String m_OriginalValue = "";

	// Font Flags
	public boolean m_IsItalic = false;
	public boolean m_IsSarif = false;
	public boolean m_IsSymbolic = false;
	public boolean m_IsSimple = false;
	public boolean m_IsHorizontalMode = false;

	// NEW Flags
	public boolean m_IsCFF = false;
	public boolean m_IsAllCap = false;
	public boolean m_IsForceBold = false;

	// New VDP Text Attributes
	public long m_SDFObjNum;
	// PDF Standard Font
	public int m_StdType1Font;
	public String m_EmbFontName;
	public String m_BaseFontName;
	public boolean m_IsFontEmbedded = false;

	// Setting the Exact Fonts
	// m_UseFontFile, m_FontFile, m_UseExactMatchFont, m_DerivedFont
	// m_FontStyle, m_UseServerFont, m_ServerFont

	// If set to true then Font File used to set the Font
	public boolean m_UseFontFile = false;
	public String m_FontFile;

	// If the Font is one of the PDF Standard Fonts or the exact match is found
	// then this is pre-defined font and can be retrieved from the server.
	public String m_FontStyle;

	// This exact Font can also be set by the user in the PDF Viewer
	public boolean m_UseServerFont = false;
	public String m_ServerFont;

	// PDF Standard Font
	public boolean m_UsePDFStdFont = false;

	// Deriving AWT Font
	public transient static HashMap m_FontStreamHolder = null;
	public transient byte[] m_FontStream;

	// Font Types - Only True Type and Type 1 is Supported.
	public boolean m_IsTrueType = false;
	public boolean m_IsType0 = false;
	public boolean m_IsType1 = false;
	public boolean m_IsType1C = false;
	public boolean m_IsType3 = false;

	// Text Metrices
	public AffineTransform m_TextMatrix;

	public double m_StartX;
	public double m_StartY;
	// public double m_EndX;
	// public double m_EndY;

	// This is the User Specified Value
	public String m_SetValue;

	// This indicates if this VDP Field is of type WORD, LINE or PARA.
	public int m_VDPTextMode;

	// This field is populated if the VDP Text Mode is PARA. This holds the
	// information for each line like the Start Position, Text Attributes, etc.
	public Vector m_VDPLineAttrList;

	// This is a static field and holds VDP Text Fields that need to be
	// prepopulated
	public static Vector<String> m_VDPPrepopulatedFields;

	// New Field Jan 26 2010. This field is predominantly used when VDP Text
	// Mode
	// is Word. This is used to capture multiple words in a line.
	public RGPTRectangle m_LineBBox = null;

	// New Field Nov 26 2010. This field is predominantly used whenever there is
	// a
	// text BBox Adjustments
	public RGPTRectangle m_NewBBox = null;

	// Do Not Serualize
	public transient Hashtable<TextAttribute, Object> m_DeriveFontAttrib;
	public transient Font m_Font;
	public static transient final HashMap m_JavaFontWeights;
	public transient boolean m_SelectionProcessed;

	// This variable is used to Logs the Font Family only once the first time.
	// This is used only in method buildProperFields.
	private static transient boolean PrintFontFamily = true;

	// This variable is used to check if TextInRect VDP Type is used in this
	// Template.
	public static transient boolean UseTextInRect = false;

	// NEW FIELD - UseTitleCase. This lets the user eneterd text to be in Title
	// Case Oct 13, 2009
	public boolean m_UseTitleCase = false;

	// NEW FIELD - FieldLengthFixed. This lets the user eneter only upto the
	// Length specified Oct 13, 2009
	public boolean m_FieldLengthFixed = false;

	// NEW FIELD for specifying the Data Type of Text Entered. This can be Text
	// or Number Oct 13, 2009
	public String m_TextDataType = "TEXT";

	// New Fields for Overflow Text
	public boolean m_IsOverFlowField = false;
	public String m_OverFlowVDPField = "";

	// NEW FIELD for specifying the Predefined Text after which the user can
	// type. Oct 13, 2009 and Dec 21 2009
	public String m_PrefixValue = "";
	public String m_SuffixValue = "";
	public String m_TextFormatName = TextFormatter.SUPPORTED_TEXT_FORMAT
			.get(TextFormatter.NO_FORMAT);
	public int m_TextFormatType = TextFormatter.NO_FORMAT;
	public String m_TextFormatValue = "";
	public String m_AlternateVDPField = "None";
	public int m_SequenceId = -1;
	public static boolean m_UseSequenceId = true;

	/**
	 * Weight Mapping Between PDF Font and Java Font NOTE : Currently
	 * WEIGHT_DEMIBOLD and WEIGHT_EXTRABOLD are not considered Weight 100 -
	 * WEIGHT_EXTRA_LIGHT = 0.5, Weight 200 - WEIGHT_LIGHT = 0.75, Weight 300 -
	 * WEIGHT_DEMILIGHT = 0.875, Weight 400 - WEIGHT_REGULAR = 1.0, Weight 500 -
	 * WEIGHT_SEMIBOLD = 1.25, Weight 600 - WEIGHT_MEDIUM = 1.5, Weight NA -
	 * WEIGHT_DEMIBOLD = 1.75, Weight 700 - WEIGHT_BOLD = 2.0, Weight 800 -
	 * WEIGHT_HEAVY = 2.25, Weight NA - WEIGHT_EXTRABOLD = 2.5, Weight 900 -
	 * WEIGHT_ULTRABOLD = 2.75
	 */
	static {
		m_VDPPrepopulatedFields = new Vector<String>();
		m_JavaFontWeights = new HashMap();
		m_JavaFontWeights.put(new Integer(100),
				TextAttribute.WEIGHT_EXTRA_LIGHT);
		m_JavaFontWeights.put(new Integer(200), TextAttribute.WEIGHT_LIGHT);
		m_JavaFontWeights.put(new Integer(300), TextAttribute.WEIGHT_DEMILIGHT);
		m_JavaFontWeights.put(new Integer(400), TextAttribute.WEIGHT_REGULAR);
		m_JavaFontWeights.put(new Integer(500), TextAttribute.WEIGHT_SEMIBOLD);
		m_JavaFontWeights.put(new Integer(600), TextAttribute.WEIGHT_MEDIUM);
		m_JavaFontWeights.put(new Integer(700), TextAttribute.WEIGHT_BOLD);
		m_JavaFontWeights.put(new Integer(800), TextAttribute.WEIGHT_HEAVY);
		m_JavaFontWeights.put(new Integer(900), TextAttribute.WEIGHT_ULTRABOLD);
	}

	public void buildProperFields() {
		super.buildProperFields();
		if (m_OverFlowVDPField == null)
			m_OverFlowVDPField = "";
		if (m_NewBBox == null)
			m_NewBBox = RGPTRectangle.getReactangle(m_PageRectangle);
		if (m_VertTextAllignment == null)
			m_VertTextAllignment = "";
		if (m_TextMatrix == null && m_FieldType.equals("Text"))
			throw new RuntimeException("Text Matrix not defined for: "
					+ m_FieldName);
		else if (m_FieldType.equals("TextOnGraphics")) {
			m_TextMatrix = new AffineTransform(); // Created Identity Matrix
			m_ElementCTM = new AffineTransform(); // Created Identity Matrix
			UseTextInRect = true;
			// m_RotationAngle = -5.0;
			// m_ShearX = 0.5;
		}
		if (m_IsVDPPrepopulated) {
			if (!m_VDPPrepopulatedFields.contains(m_FieldName))
				m_VDPPrepopulatedFields.addElement(m_FieldName);
		}
		if (m_SequenceId == -1 || m_SequenceId == 0)
			m_UseSequenceId = false;

		if (m_PrefixValue == null)
			m_PrefixValue = "";
		if (m_TextAllignment.equals("LEFT"))
			m_TextAllignment = PDFPageHandler.ALLIGN_LEFT;
		else if (m_TextAllignment.equals("CENTER"))
			m_TextAllignment = PDFPageHandler.ALLIGN_CENTER;
		else if (m_TextAllignment.equals("RIGHT"))
			m_TextAllignment = PDFPageHandler.ALLIGN_RIGHT;

		// Setting the Flags using m_FontFlags
		int len = m_FontFlags.length();
		if (len < 8)
			throw new RuntimeException(
					"\n\n---ERROR - FONT FLAG NOT SET PROPERLY\n");
		char[] dst = new char[len];
		boolean[] bool = new boolean[len];
		m_FontFlags.getChars(0, len, dst, 0);
		for (int i = 0; i < len; i++) {
			if (dst[i] == '0')
				bool[i] = false;
			else
				bool[i] = true;
		}
		m_IsItalic = bool[0];
		m_IsSarif = bool[1];
		m_IsSymbolic = bool[2];
		m_IsSimple = bool[3];
		m_IsHorizontalMode = bool[4];
		m_IsCFF = bool[5];
		m_IsAllCap = bool[6];
		m_IsForceBold = bool[7];

		// Setting Type 1C font
		if (m_IsType1 && m_IsCFF)
			m_IsType1C = true;

		if (!PrintFontFamily)
			this.printFontFamily();

		// Setting the Font Attribute. This will be used to derive the actual
		// font
		// for the Text Field
		this.setFontAttrib();
		// RGPTLogger.logToFile("VDP Text Data: " + this.toString());
	}

	// Currently this application only handles True Type and Type 1 Font.
	public void setFontAttrib() {
		Font font = null;
		m_DeriveFontAttrib = new Hashtable<TextAttribute, Object>();
		Hashtable<TextAttribute, Object> tempAttrib = new Hashtable<TextAttribute, Object>();
		int fontFormat = -1;

		if (m_IsTrueType)
			fontFormat = Font.TRUETYPE_FONT;
		else if (m_IsType1)
			fontFormat = Font.TYPE1_FONT;

		if (m_IsFontEmbedded)
			font = this.deriveEmbFont();
		if (m_UseFontFile)
			font = this.deriveFontFile(fontFormat);
		else if (m_UsePDFStdFont)
			font = derivePDFFont(fontFormat);
		else if (m_UseServerFont)
			font = deriveSelectedFont(fontFormat, m_ServerFont);

		RGPTLogger.logToFile("CREATED FONT SUCCESSFULLY: " + font.toString());

		// tempAttrib.put(TextAttribute.SIZE, new Float(1.0f));
		// tempAttrib.put(TextAttribute.FOREGROUND, Color.RED);
		// tempAttrib.put(TextAttribute.BACKGROUND, Color.RED);
		tempAttrib.put(TextAttribute.WEIGHT,
				(Float) m_JavaFontWeights.get(new Integer(m_FontWeight)));
		font = font.deriveFont(tempAttrib);
		if (m_IsItalic)
			font = font.deriveFont(Font.ITALIC);
		font = font.deriveFont((new Float(m_FontSize)).floatValue());

		m_Font = font;

		RGPTLogger.logToFile("DERIVED FONT SUCCESSFULLY: " + m_Font.toString());
		// m_DeriveFontAttrib.put(TextAttribute.WEIGHT,
		// TextAttribute.WEIGHT_BOLD);
		// (Float) m_JavaFontWeights.get(new Integer(m_FontWeight)));
		m_DeriveFontAttrib.put(TextAttribute.SIZE, new Float(m_FontSize));
		m_DeriveFontAttrib
				.put(TextAttribute.FOREGROUND, new Color(m_FillColor));
		// m_DeriveFontAttrib.put(TextAttribute.FONT, font );
	}

	// Font createFont(int fontFormat,File fontFile) Times_New_Roman_Bold
	// TRUETYPE_FONT TYPE1_FONT

	private Font deriveFontFile(int fontFormat) {
		Font font = null;
		try {
			File fontFile = new File(m_FontFile);
			font = Font.createFont(fontFormat, fontFile);
		} catch (Exception ex) {
			RGPTLogger.logToFile(this.toString());
			ex.printStackTrace();
			throw new RuntimeException("Font File Could not be created");
		}
		return font;
	}

	// Narayan - This method is never used
	private Font derivePDFFont(int fontFormat) {
		Font font = null;
		String fontFileStr;
		File fontFile;
		// Read properties file. This will be moved out into a seperate Utility
		// class in future
		Properties properties = new Properties();
		String propFile = "C:/Programs/PDFNetC/RGPTViewer/font.properties";
		try {
			properties.load(new FileInputStream(propFile));
		} catch (Exception e) {
		}

		String fontName = properties.getProperty(new Integer(m_StdType1Font)
				.toString());
		m_FontFile = properties.getProperty("filename." + fontName);

		try {
			fontFile = new File(m_FontFile);
			// Hardcording to TrueType in future this can be Type1 Font
			font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
		} catch (Exception ex) {
			RGPTLogger.logToFile(this.toString());
			ex.printStackTrace();
			throw new RuntimeException("Font File Could not be created");
		}
		return font;
	}

	private Font deriveSelectedFont(int fontFormat, String fontName) {
		Font font = this.matchFont(fontName);
		if (font == null) {
			RGPTLogger.logToFile(this.toString());
			// Popup Window for User to Select Appropriate Font.
			throw new RuntimeException(
					"\n\n---ERROR - NOT A SUPPORTED TRUE TYPE FONT.------\n");
		}
		RGPTLogger.logToFile("Found FONT " + fontName);
		// Font font = new Font(fontName, Font.PLAIN, m_FontSize);
		return font;
	}

	private Font deriveEmbFont() {
		Object fontStrObj = null;
		boolean raiseExp = false;
		if (m_IsType0 || m_IsType1C || m_IsType3) {
			raiseExp = true;
			RGPTLogger.logToFile("ERROR - NOT A SUPPORTED FONT IN: "
					+ m_FieldName);
			// throw new
			// RuntimeException("\n\n---ERROR - NOT A SUPPORTED FONT.------\n"
			// );
		}
		String fontName;
		Font font = null;

		if (m_FontStreamHolder == null)
			raiseExp = true;
		else {
			fontStrObj = m_FontStreamHolder.get(m_SDFObjNum);
			if (fontStrObj == null) {

				raiseExp = true;
			}
		}

		// The code is changed to accomodate to Retrive Font File as seperate
		// Server Call
		// Instead of raising Exception, Default Arial Font is used.
		if (raiseExp) {
			// The code is changed to accomodate to Retrive Font File as
			// seperate Server Call
			// Instead of raising Exception, Default Arial Font is used.
			RGPTLogger.logToFile("NO FONT STREAMS SPECIFIED");
			System.out
					.println("NO FONT STREAMS SPECIFIED. SELECTING DEFAULT FONT");
			font = new Font("Arial", Font.PLAIN, 1);
			return font;
		}

		m_FontStream = ((FontStreamHolder) fontStrObj).m_FontStream;
		if (m_IsTrueType) {
			try {
				font = Font.createFont(Font.TRUETYPE_FONT,
						new ByteArrayInputStream(m_FontStream));
				RGPTLogger.logToFile("Created a TRUE TYPE Font: "
						+ font.toString());
				return font;
			} catch (Exception ex) {
				// Popup Window for User to Select Appropriate Font.
				ex.printStackTrace();
				raiseExp = true;
				RGPTLogger.logToFile("ERROR - NOT A SUPPORTED TRUETYPE FONT "
						+ m_FontName);
				// throw new
				// RuntimeException("\n\n---ERROR - NOT A SUPPORTED TRUETYPE FONT.------\n"
				// +
				// ex.getMessage());
			}
		} else if (m_IsType1) {
			try {
				font = Font.createFont(Font.TYPE1_FONT,
						new ByteArrayInputStream(m_FontStream));
				RGPTLogger
						.logToFile("Created a TYPE1 Font: " + font.toString());
				return font;
			} catch (Exception ex) {
				// Popup Window for User to Select Appropriate Font.
				ex.printStackTrace();
				raiseExp = true;
				RGPTLogger.logToFile("OTF Font not supported " + m_FontName
						+ " showing with Substitute Font");
				// throw new
				// RuntimeException("\n\n---ERROR - NOT A SUPPORTED TYPE1 FONT.------\n"
				// );
			}
		} else {
			font = this.matchFont(m_FontName);
			if (font == null) {
				// Popup Window for User to Select Appropriate Font.
				raiseExp = true;
				RGPTLogger.logToFile("ERROR - NOT A SUPPORTED FONT: "
						+ m_FontName);
			} else {
				RGPTLogger.logToFile("Found FONT " + m_FontName);
				return font;
			}
			// font = new Font(m_FontName, Font.PLAIN, m_FontSize);
		}
		// If the specified Font is not Constructed then Default Arial Font is
		// used.
		RGPTLogger
				.logToFile("SPECIFIED FONT NOT SUPPORTED. SELECTING DEFAULT FONT");
		font = new Font("Arial", Font.PLAIN, 1);
		return font;
	}

	public Font matchFont(String fontName) {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();

		// Checking if the Font Exists in this Font Array
		Font[] fonts = ge.getAllFonts();
		for (int iter = 0; iter < fonts.length; iter++) {
			if (fontName.compareTo(fonts[iter].getFontName()) == 0)
				return fonts[iter];
		}

		return null;
	}

	// Narayan - This method is not used
	public void printFontFamily() {
		StringBuffer mesg = new StringBuffer("\n----FONT PROPERTY----\n");
		Properties properties = new Properties();
		String propFile = "C:/Programs/PDFNetC/RGPTViewer/font.properties";
		try {
			properties.load(new FileInputStream(propFile));
		} catch (Exception e) {
		}

		mesg.append("\n" + properties.toString() + "\n");

		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		String[] fontFamilies = ge.getAvailableFontFamilyNames();
		mesg.append("\n----FONT FAMILIES----\n");
		// Direct Match
		for (int i = 0; i < fontFamilies.length; i++) {
			mesg.append(" : " + fontFamilies[i]);
		}

		// Printing to Logs the Font Family only once the first time.
		mesg.append("\n----PRINTING FONTS----\n");
		Font[] fonts = ge.getAllFonts();
		for (int iter = 0; iter < fonts.length; iter++)
			mesg.append(" : " + fonts[iter].getFontName());

		RGPTLogger.logToFile(mesg.toString());
		PrintFontFamily = true;
	}

	public AffineTransform getTextMatrix(PDFPageInfo pdfPage) {
		if (m_TextMatrix == null)
			return null;

		double sx = 0.0, sy = 0.0;
		sx = this.m_TextMatrix.getScaleX()
				* pdfPage.m_FinalDeviceCTM.getScaleX();
		sy = this.m_TextMatrix.getScaleY()
				* pdfPage.m_FinalDeviceCTM.getScaleY();

		AffineTransform newTexMatrix = new AffineTransform();
		newTexMatrix.setToScale(sx, sy);
		return newTexMatrix;
	}

	// Serialization.
	// public transient static boolean m_IsFontStreamSaved = false;
	public void save(ObjectOutputStream objstream) throws IOException {
		FontStreamHolder fontStrHldr = null;
		objstream.writeObject(this);
		super.save(objstream);
	}

	// De-Serialization.
	// public transient static boolean m_IsFontStreamRead = false;
	public Object load(ObjectInputStream objstream) throws Exception {
		super.load(objstream);
		return this;
	}

	public String toString() {
		return this.toString(new StringBuffer());
	}

	public String toString(StringBuffer mesg) {
		super.toString(mesg);
		mesg.append("\n-----VDP TEXT INFO.------\n");
		mesg.append("SequenceId: " + m_SequenceId);
		mesg.append("Is Field Optional: " + m_IsFieldOptional);
		mesg.append(": Font: " + m_FontName);
		mesg.append(": Base Font Name: " + m_BaseFontName);
		mesg.append(": Font Size: " + m_FontSize);
		mesg.append(": Font Family: " + m_FontFamily);
		mesg.append(": Font Weight: " + m_FontWeight);
		mesg.append(": Color: " + m_FillColor);
		mesg.append(": Text Allignment: " + m_TextAllignment);
		mesg.append(": Alternate VDP Field: " + m_AlternateVDPField);
		mesg.append(": m_IsOverFlowField: " + m_IsOverFlowField);
		mesg.append(": m_OverFlowVDPField: " + m_OverFlowVDPField);

		if (m_VDPTextMode == StaticFieldInfo.WORD) {
			mesg.append(": VDP Text Mode: WORD");
			if (m_LineBBox != null)
				mesg.append(" Line BBox: " + m_LineBBox.toString());
		} else if (m_VDPTextMode == StaticFieldInfo.LINE)
			mesg.append(": VDP Text Mode: LINE");
		else if (m_VDPTextMode == StaticFieldInfo.PARA) {
			mesg.append(": VDP Text Mode: PARA");
			mesg.append("\n Line Attributes: " + m_VDPLineAttrList.toString());
		}

		mesg.append(": Starting X Pos: " + m_StartX);
		mesg.append(": Starting Y Pos: " + m_StartY);
		// mesg.append(": Ending X Pos: " + m_EndX);
		// mesg.append(": Ending Y Pos: " + m_EndY);

		// Printing Additional Info
		mesg.append("\nGen Font Name: " + m_GenFontName);
		mesg.append(": Gen Font Weight: " + m_GenFontWeight);
		mesg.append(": Font BBox: " + m_FontBBox);
		mesg.append(": Font Type: " + m_FontType);
		mesg.append(": SDFObjNum: " + m_SDFObjNum);
		mesg.append(": StdType1Font: " + m_StdType1Font);
		mesg.append(": IsEmbedded: " + m_IsFontEmbedded);
		mesg.append(": EmbFontName: " + m_EmbFontName);

		// FONT TYPE
		mesg.append("\nIsTrueType: " + m_IsTrueType);
		mesg.append(": IsType0: " + m_IsType0);
		mesg.append(": IsType1: " + m_IsType1);
		mesg.append(": IsType1c: " + m_IsType1C);
		mesg.append(": IsType3: " + m_IsType3);

		// Printing Flags
		mesg.append("\nFont Flags: " + m_FontFlags);
		mesg.append(" : IsItalic: " + m_IsItalic);
		mesg.append(" : Sarif: " + m_IsSarif);
		mesg.append(" : IsSymbolic " + m_IsSymbolic);
		mesg.append(" : IsSimple: " + m_IsSimple);
		mesg.append(" : IsHorizontal: " + m_IsHorizontalMode);
		mesg.append(" : IsCFF: " + m_IsCFF);
		mesg.append(" : IsAllCap: " + m_IsAllCap);
		mesg.append(" : IsForceBold: " + m_IsForceBold);

		// FONT Related Values
		mesg.append("\nUse Font File: " + m_UseFontFile);
		mesg.append(" : Font File: " + m_FontFile);
		// mesg.append(" : Use Exact Match Font: " + m_UseExactMatchFont);
		// mesg.append(" : Derived Font: " + m_ServerFont);
		mesg.append(" : Derived Font Style: " + m_FontStyle);
		mesg.append(" : Use Selected Font: " + m_UseServerFont);
		mesg.append(" : Selected Server Font: " + m_ServerFont);

		// Points and CTM

		try {
			mesg.append("\nText Element CTM: " + m_TextMatrix.toString());
		} catch (Exception ex) {
		}

		return mesg.toString();
	}
}
