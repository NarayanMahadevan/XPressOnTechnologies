// RGPT PACKAGES
package com.rgpt.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JComponent;

public class RGPTUtil {

	// Get Date is specified Format. If data format is not specified then the
	// yyyy.MM.dd'-'HH:mm:ss Format is used.
	public static String getDateString(long dateTime, String format) {
		if (format == null || format.isEmpty())
			format = "yyyy.MM.dd HH:mm:ss";
		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(format);
		java.util.Date date = new java.util.Date(dateTime);
		String currDate = dateFormat.format(date);
		return currDate;
	}

	// Check for P3 Server
	public static boolean isP3Server() {
		if (RGPTParams.m_RGPTParamValues != null) {
			String serverName = RGPTParams.getVal("ServerName");
			if (serverName != null && !serverName.isEmpty()
					&& serverName.equals("P3Server"))
				return true;
		}
		return false;
	}

	// Get Date is specified Format. If data format is not specified then the
	// yyyy.MM.dd'-'HH:mm:ss Format is used.
	public static String getCurrentDate(String format) {
		if (format == null || format.isEmpty())
			format = "yyyy.MM.dd'-'HH:mm:ss";
		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(format);
		java.util.Date date = new java.util.Date();
		String currDate = dateFormat.format(date);
		return currDate;
	}

	public static String getTextForWidth(Vector<String> splitTxt,
			double bboxWth, Font font) {
		String selTxt = "", prevSelTxt = "";
		StringBuffer txt4Wt = new StringBuffer();
		for (int i = 0; i < splitTxt.size(); i++) {
			txt4Wt.append(splitTxt.elementAt(i));
			selTxt = getTextForWidth(bboxWth, txt4Wt.toString(), font);
			if (selTxt.length() < txt4Wt.toString().length()) {
				selTxt = prevSelTxt;
				break;
			}
			prevSelTxt = selTxt;
		}
		// System.out.println("Prev Sel Txt: " + prevSelTxt + " selTxt: " +
		// selTxt);
		return selTxt;
	}

	public static String getHTMLTextForWidth(JComponent comp, double bboxWth,
			String txt, Font font) {
		boolean useJComp = false;
		Vector<String> multiLineText = null;
		int htmlTextIndex = txt.indexOf("<html>");
		if (htmlTextIndex == -1)
			htmlTextIndex = txt.indexOf("<HTML>");
		if (htmlTextIndex != -1)
			return txt;
		if (useJComp)
			multiLineText = getMultiLineTextForBBox(comp, bboxWth, txt, font);
		else
			multiLineText = getMultiLineTextForWidth(bboxWth, txt, font);
		StringBuffer htmlText = new StringBuffer("<html>");
		for (int i = 0; i < multiLineText.size(); i++) {
			htmlText.append(multiLineText.elementAt(i));
			if (i < multiLineText.size() - 1)
				htmlText.append("<br>");
		}
		htmlText.append("</html>");
		return htmlText.toString();
	}

	public static Vector<String> getMultiLineTextForBBox(JComponent comp,
			double bboxWth, String txt, Font font) {
		int startPos = 0, currPos = 0;
		String origText = txt;
		Graphics2D g2d = (Graphics2D) comp.getGraphics();
		Hashtable<TextAttribute, Object> fontAttrib = new Hashtable<TextAttribute, Object>();
		fontAttrib.put(TextAttribute.FONT, font);

		Vector<String> multiLine = new Vector<String>();
		while (true) {
			currPos = getText4BBox(g2d, txt, startPos, bboxWth, fontAttrib,
					true);
			System.out.println("Start Pos: " + startPos + " :Curr Pos: "
					+ currPos);
			String newText = origText.substring(startPos, currPos);
			multiLine.addElement(newText);
			if (currPos == origText.length())
				break;
			startPos = currPos;
			if (origText.charAt(startPos) == '\n')
				startPos = startPos + 1;
		}
		return multiLine;
	}

	public static int getText4BBox(Graphics2D g2d, String text, int startPos,
			double width, Hashtable fontAttrib, Boolean isParaSelection) {

		AttributedString textString = new AttributedString(text, fontAttrib);
		AttributedCharacterIterator charIter = textString.getIterator();
		int paragraphStart = charIter.getBeginIndex();
		int paragraphEnd = charIter.getEndIndex();
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(charIter,
				g2d.getFontRenderContext());

		// This Margin is provided to avoid text overlap. If 1.0 No Margin set.
		double margin = 1.0;
		double formatWidth = width * margin;

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
					+ currPos + " VDP Text Length: " + text.length());
			break;
		}

		// The New Lines are supported only when the VDP Text Mode is Para.
		if (!isParaSelection) {
			return currPos;
		}
		// Checking for New Line
		int charIndex = 0;
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

	public static Vector<String> getMultiLineTextForWidth(double bboxWth,
			String txt, Font font) {
		Vector<String> multiLine = new Vector<String>();
		while (true) {
			String newText = getTextForWidth(bboxWth, txt, font);
			multiLine.addElement(newText);
			if (newText.length() == txt.length())
				break;
			txt = txt.substring(newText.length(), txt.length());
		}
		return multiLine;
	}

	public static int getWidthForText(String txt, Font font) {
		java.awt.FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(
				font);
		int width = fm.stringWidth(txt);
		return width;
	}

	public static String getTextForWidth(double bboxWth, String txt, Font font) {
		java.awt.FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(
				font);
		int width = fm.stringWidth(txt);
		String origText = txt;
		while (true) {
			if (width < bboxWth)
				break;
			txt = txt.substring(0, txt.length() - 1);
			width = fm.stringWidth(txt);
		}

		return txt;
	}

	public static Vector<String> getSplitedText(String text, String delim) {
		Vector<String> splittedText = new Vector<String>();
		String[] splittedStrings = text.split(delim);
		for (int i = 0; i < splittedStrings.length; i++)
			splittedText.addElement(splittedStrings[i]);
		return splittedText;
	}

	public static Vector<String> getSplitedText(String original) {
		String word = "", dotTxt = "", atTxt = "";
		Vector<String> splittedText = new Vector<String>();
		String[] words = original.split(" ");
		String[] dottedSplits = original.split("\\.");
		// System.out.println("OrigTxt: " + original + " Num of Words: " +
		// words.length);
		// System.out.println("#of Dots: " + dottedSplits.length);
		for (int i = 0; i < words.length; i++) {
			word = words[i];
			if (i < words.length - 1)
				word = word + " ";
			String[] dotSplits = word.split("\\.");
			// System.out.println("Word: " + word + " #OfdotSplits: " +
			// dotSplits.length);
			for (int j = 0; j < dotSplits.length; j++) {
				dotTxt = dotSplits[j];
				if (j < dotSplits.length - 1)
					dotTxt = dotTxt + ".";
				String[] atSplits = dotTxt.split("@");
				// System.out.println("dotSplits: " + dotTxt + " #OfAtSplits: "
				// +
				// atSplits.length);
				for (int k = 0; k < atSplits.length; k++) {
					atTxt = atSplits[k];
					if (k < atSplits.length - 1)
						atTxt = atTxt + "@";
					// atTxt = atSplits[k]; if (k != 0) atTxt = "@" + atTxt;
					splittedText.addElement(atTxt);
				}
			}
		}
		return splittedText;
	}

	public static boolean checkEquals(String arg1, String arg2) {
		arg1 = join(arg1.trim().split(" "), "");
		arg2 = join(arg2.trim().split(" "), "");
		if (arg1.equalsIgnoreCase(arg2))
			return true;
		return false;
	}

	public static String toSentenceCase(String original) {
		String[] lines = original.split("\\.");
		for (int i = 0; i < lines.length; i++) {
			lines[i] = toInitialCap(lines[i]);
		}
		return join(lines, ".");
	}

	public static String toTitleCase(String original) {
		String[] words = original.split(" ");
		for (int i = 0; i < words.length; i++) {
			words[i] = toInitialCap(words[i]);
		}
		return join(words, " ");
	}

	public static String getHTTPText(String original) {
		String httpTxt = replaceSpaceWithHex(original);
		return httpTxt;
	}

	public static String replaceSpaceWithHex(String original) {
		return replaceSpace(original, "%20");
	}

	public static String replaceSpace(String original, String delim) {
		StringBuffer httpTxt = new StringBuffer();
		String[] words = original.split(" ");
		for (int i = 0; i < words.length; i++) {
			httpTxt.append(words[i]);
			if (i < words.length - 1)
				httpTxt.append(delim);
		}
		return httpTxt.toString();
	}

	public static String toInitialCap(String original) {
		if (original.trim().length() == 0)
			return "";
		return original.substring(0, 1).toUpperCase()
				+ original.substring(1).toLowerCase();
	}

	public static String join(String[] text, String appendChar) {
		StringBuffer sentance = new StringBuffer();
		for (int i = 0; i < text.length; i++) {
			sentance.append(text[i]);
			if (i != text.length - 1)
				sentance.append(appendChar);
		}
		return sentance.toString();
	}

	public static String toString(String[] arrStrs) {
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < arrStrs.length; i++) {
			strBuf.append(arrStrs[i] + ", ");
		}
		return strBuf.toString();
	}

	// This first convert origArr to Vector and then if any new elements are
	// there add
	// it to the Vector
	public static Vector<String> createList(String[] origArr,
			String[] addNewElems) {
		Vector<String> newArrayList = new Vector<String>();
		for (int i = 0; i < origArr.length; i++)
			newArrayList.addElement(origArr[i]);
		if (addNewElems == null || addNewElems.length == 0)
			return newArrayList;
		for (int j = 0; j < addNewElems.length; j++)
			newArrayList.addElement(addNewElems[j]);
		return newArrayList;
	}

	public static Vector<Float> createVector(float[] array) {
		return (new Vector<Float>(floatArrayAsList(array)));
	}

	public static Vector<String> createVector(String[] array) {
		return (new Vector<String>(Arrays.asList(array)));
	}

	public static void createVector(Vector<String> currList, String[] array) {
		currList.addAll((new Vector<String>(Arrays.asList(array))));
	}

	public static String[] toArray(Vector<String> vector) {
		return vector.toArray(new String[0]);
	}

	public static Vector<Integer> createVector(int[] array) {
		return (new Vector<Integer>(intArrayAsList(array)));
	}

	// Helper method to convert float arrays into Lists
	public static java.util.List<Float> floatArrayAsList(final float[] a) {
		if (a == null)
			throw new NullPointerException();
		return new AbstractList<Float>() {
			@Override
			public Float get(int i) {
				return a[i];// autoboxing
			}

			@Override
			public Float set(int i, Float val) {
				final Float old = a[i];
				a[i] = val;// auto-unboxing
				return old;// autoboxing
			}

			@Override
			public int size() {
				return a.length;
			}
		};
	}

	// Helper method to convert int arrays into Lists
	public static java.util.List<Integer> intArrayAsList(final int[] a) {
		if (a == null)
			throw new NullPointerException();
		return new AbstractList<Integer>() {
			@Override
			public Integer get(int i) {
				return a[i];// autoboxing
			}

			@Override
			public Integer set(int i, Integer val) {
				final int old = a[i];
				a[i] = val;// auto-unboxing
				return old;// autoboxing
			}

			@Override
			public int size() {
				return a.length;
			}
		};
	}

	public static int[] toArray(Vector<Integer> vector) {
		return toIntArray(Arrays.asList(vector.toArray(new Integer[0])));
	}

	public static int[] toIntArray(java.util.List<Integer> list) {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer e : list)
			ret[i++] = e.intValue();
		return ret;
	}

	public static float[] toArray(Vector<Float> vector, float[] res) {
		return toFloatArray(Arrays.asList(vector.toArray(new Float[0])), res);
	}

	public static float[] toFloatArray(java.util.List<Float> list, float[] res) {
		if (res == null)
			res = new float[list.size()];
		int i = 0;
		for (Float e : list)
			res[i++] = e.floatValue();
		return res;
	}

	public static boolean createZIPStream(String srcDir, String fileName,
			Vector<File> pdfFiles) throws Exception {
		File pdfFile = null;
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(srcDir + fileName));
			out.setLevel(9);
			for (int i = 0; i < pdfFiles.size(); i++) {
				pdfFile = (File) pdfFiles.elementAt(i);
				createZIPEntry(out, pdfFile);
			}
		} finally {
			if (out != null) {
				out.finish();
				out.close();
			}
		}
		return true;
	}

	public static void createZIPEntry(ZipOutputStream out, File file)
			throws Exception {
		InputStream in = null;
		ZipEntry entry = null;
		try {
			entry = new ZipEntry(file.getName());
			out.putNextEntry(entry);
			in = new FileInputStream(file);
			int read;
			byte[] buf = new byte[1024];
			while ((read = in.read(buf)) > 0) {
				out.write(buf, 0, read);
			}
		} finally {
			out.closeEntry();
			if (in != null)
				in.close();
		}
	}

	public static File createDir(String filePath, boolean addNewDirFile) {
		File assetFile = new File(filePath);
		if (!assetFile.exists()) {
			if (!addNewDirFile)
				throw new RuntimeException("No Search Directory: " + filePath);
			boolean result = assetFile.mkdir();
			if (!result)
				throw new RuntimeException(
						"Unable to Create Search Directory: "
								+ filePath.toString());
		}
		return assetFile;
	}

	public static void deleteDir(String filePath, java.io.FileFilter filter)
			throws Exception {
		File delDir = new File(filePath);
		File[] delFiles = null;
		if (filter == null)
			delFiles = delDir.listFiles();
		else
			delFiles = delDir
					.listFiles((FileFilter) FileFilterFactory.PDF_FILE_FILTER);
		for (int i = 0; i < delFiles.length; i++) {
			File delFile = delFiles[i];
			delFile.delete();
		}
		delDir.delete();
		RGPTLogger.logToFile("Checking if Dir Exist: " + delDir.exists());
	}

	public static byte[] getBytes(String fileName) {
		return getBytes(new File(fileName));
	}

	public static byte[] getBytes(File file) {
		byte[] outputStr = null;
		BufferedInputStream buffStr = null;
		String fileName = "";
		try {
			fileName = file.getName();
			FileInputStream fileStr = new FileInputStream(file);
			System.out.println("File Stream: " + fileStr.available());
			buffStr = new BufferedInputStream(fileStr);
			System.out.println("Buffered Stream: " + buffStr.available());
			outputStr = new byte[buffStr.available()];
			buffStr.read(outputStr, 0, outputStr.length);
			buffStr.close();
		} catch (Exception ex) {
			System.out.print("Unable to extract bytes from File: " + fileName);
			System.out.println(" : " + ex.getMessage());
			// ex.printStackTrace();
			throw new RuntimeException("Unable to extract bytes from File: "
					+ fileName);
		}
		return outputStr;
	}

	public static Object makeObj(final String item) {
		return new Object() {
			public String toString() {
				return item;
			}
		};
	}

	// Serializes the Object.
	public static void serializeObject(String absFilePath, Object obj)
			throws Exception {
		FileOutputStream fileStream = null;
		ObjectOutputStream objStream = null;
		try {
			fileStream = new FileOutputStream(absFilePath);
			objStream = new ObjectOutputStream(fileStream);
			objStream.writeObject(obj);
		} finally {
			if (objStream != null)
				objStream.close();
			if (fileStream != null)
				fileStream.close();
		}
	}

	// Retrieve Object from Serialized File
	public static Object getSerializeObject(String absFilePath)
			throws Exception {
		Object obj = null;
		FileInputStream fileStream = null;
		ObjectInputStream objStr = null;
		try {
			fileStream = new FileInputStream(absFilePath);
			objStr = new ObjectInputStream(fileStream);
			obj = objStr.readObject();
		} finally {
			if (objStr != null)
				objStr.close();
			if (fileStream != null)
				fileStream.close();
		}
		return obj;
	}

	public static String generateInQuery(Vector queryData) {
		// Determining the Data Type of the First Element in the Vector
		Object firstElem = queryData.firstElement();
		String className = firstElem.getClass().getSimpleName();
		if (className.equals("Integer"))
			return generateIntInQuery((Vector<Integer>) queryData);
		else if (className.equals("String"))
			return generateStringInQuery((Vector<String>) queryData);
		return null;
	}

	public static String generateIntInQuery(Vector<Integer> queryData) {
		String eachQuery = "";
		StringBuffer queryBuf = new StringBuffer();
		int dataId = -1;
		Integer[] dataIds = null;
		dataIds = queryData.toArray(new Integer[0]);
		for (int i = 0; i < dataIds.length; i++) {
			dataId = dataIds[i].intValue();
			if (i == 0 && dataIds.length == 1)
				eachQuery = "( " + dataId + " )";
			else if (i == 0)
				eachQuery = "( " + dataId + ", ";
			else if (i == dataIds.length - 1)
				eachQuery = dataId + " )";
			else
				eachQuery = dataId + ", ";
			queryBuf.append(eachQuery);
		}
		return queryBuf.toString();
	}

	public static String generateStringInQuery(Vector<String> queryData) {
		String eachQuery = "";
		StringBuffer queryBuf = new StringBuffer();
		String dataId = "";
		String[] dataIds = null;
		dataIds = queryData.toArray(new String[0]);
		for (int i = 0; i < dataIds.length; i++) {
			dataId = "'" + dataIds[i] + "'";
			if (i == 0)
				eachQuery = "( " + dataId;
			if (i == dataIds.length - 1)
				eachQuery = dataId + " )";
			else
				eachQuery = dataId + ", ";
			queryBuf.append(eachQuery);
		}
		return queryBuf.toString();
	}

	public static Map<String, Integer> calcScaledImageSize(double origImgWt,
			double origImgHt, double reqImgWt, double reqImgHt) {
		double newImgWt = 0.0, newImgHt = 0.0;
		double aspectRat = origImgWt / origImgHt;
		if (origImgWt > origImgHt) {
			newImgWt = reqImgWt;
			newImgHt = newImgWt / aspectRat;
		} else {
			newImgHt = reqImgHt;
			newImgWt = newImgHt * aspectRat;
		}
		Map<String, Integer> newImgSize = new HashMap<String, Integer>();
		newImgSize.put("Width", (int) newImgWt);
		newImgSize.put("Height", (int) newImgHt);
		return newImgSize;
	}

	public static Rectangle getRectangle(RGPTRectangle rect) {
		return getRectangle(rect.getRectangle2D());
	}

	public static Rectangle getRectangle(Rectangle2D rect2D) {
		Rectangle rect = new Rectangle((int) Math.round(rect2D.getX()),
				(int) Math.round((int) rect2D.getY()), (int) Math.round(rect2D
						.getWidth()), (int) Math.round(rect2D.getHeight()));
		return rect;
	}

	public static Rectangle2D getRectangle2D(Rectangle rect) {
		Rectangle2D.Double rect2D = null;
		rect2D = new Rectangle2D.Double((double) rect.x, (double) rect.y,
				(double) rect.width, (double) rect.height);
		return rect2D;
	}

	public static int containsPoint(Vector<Shape> ptList, Point2D.Double pt) {
		for (int i = 0; i < ptList.size(); i++) {
			Shape shPt = ptList.elementAt(i);
			if (shPt.contains(pt))
				return i;
		}
		return -1;
	}

	public static boolean isImageActionEqual(String action1, String action2) {
		Vector<String> actionIds = createVector(RGPTUtil
				.enumToStringArray(RGPTActionListener.ImageActions.values()));
		if (!actionIds.contains(action1) || !actionIds.contains(action2))
			return false;
		if (action1.equals(action2))
			return true;
		return false;
	}

	public static boolean isWFActionEqual(String action1, String action2) {
		Vector<String> actionIds = createVector(RGPTUtil
				.enumToStringArray(RGPTActionListener.WFActions.values()));
		if (!actionIds.contains(action1) || !actionIds.contains(action2))
			return false;
		if (action1.equals(action2))
			return true;
		return false;
	}

	public static boolean isImageFilterActionPerformed(String action) {
		String[] imgFilterActions = RGPTUtil
				.enumToStringArray(RGPTActionListener.ImageFilterActions
						.values());
		return contains(imgFilterActions, action);
	}

	public static boolean isIDAppsActionPerformed(String action) {
		String[] idAppsActions = RGPTUtil
				.enumToStringArray(RGPTActionListener.IDAppsActions.values());
		return contains(idAppsActions, action);
	}

	public static boolean isWFActionPerformed(String action) {
		String[] wfActions = RGPTUtil
				.enumToStringArray(RGPTActionListener.WFActions.values());
		return contains(wfActions, action);
	}

	public static boolean isDialogActionPerformed(String action) {
		String[] wfActions = RGPTUtil
				.enumToStringArray(RGPTActionListener.DialogActions.values());
		return contains(wfActions, action);
	}

	public static Object containsValue(HashMap dataMap, Object value) {
		return containsValue(dataMap, value, false);
	}

	public static Object containsValue(HashMap dataMap, Object value,
			boolean useRecursion) {
		if (!useRecursion)
			if (!dataMap.containsValue(value))
				return null;
		Object[] keys = dataMap.keySet().toArray(new Object[0]);
		for (int i = 0; i < keys.length; i++) {
			Object keyVal = dataMap.get(keys[i]);
			// RGPTLogger.logToFile("Key Val: "+keyVal.getClass().getName());
			if (keyVal instanceof HashMap) {
				// RGPTLogger.logToFile("Geting Contents for: "+keys[i]);
				Object retval = containsValue((HashMap) keyVal, value);
				if (retval != null)
					return retval;
			} else if (keyVal.equals(value))
				return keys[i];
		}
		return null;
	}

	public static String containsValue(Map<String, String> dataMap, String value) {
		if (!dataMap.containsValue(value))
			return null;
		String[] keys = dataMap.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++) {
			String keyVal = dataMap.get(keys[i]);
			if (keyVal.equals(value))
				return keys[i];
		}
		return null;
	}

	public static int containsValue(Map<Integer, String> dataMap, String value) {
		if (!dataMap.containsValue(value))
			return -1;
		Integer[] keys = dataMap.keySet().toArray(new Integer[0]);
		for (int i = 0; i < keys.length; i++) {
			String keyVal = dataMap.get(keys[i]);
			if (keyVal.equals(value))
				return keys[i].intValue();
		}
		return -1;
	}

	public static Vector<String> extractFields(String patStr, String txtFormat) {
		int count = 0;
		String newText = txtFormat;
		boolean found = true;
		Vector<String> varFlds = new Vector<String>();
		Pattern pattern = Pattern.compile(patStr);
		System.out.println("Get Reg Exp Pattern: " + pattern.pattern());
		while (found) {
			Matcher matcher = pattern.matcher(newText);
			if (matcher.find()) {
				System.out.println("Found Match " + matcher.group()
						+ " starting at index " + matcher.start()
						+ "and ending at index " + matcher.end());
				found = true;
				int length = matcher.end() - matcher.start();
				String fldName = matcher.group().substring(2, length - 2); // Pattern
																			// string
																			// is
																			// of
																			// the
																			// format
																			// @#fldName#@
				System.out.println("fldName " + fldName + " length " + length);
				varFlds.addElement(fldName);
				newText = replaceFieldWithEmptyData(newText, fldName,
						matcher.start(), matcher.end());
				count++;
			} else
				found = false;
		}
		return varFlds;
	}

	public static String populateTextFormat(String patStr, String txtFormat,
			HashMap nvPair) {
		int count = 0;
		String newText = txtFormat;
		boolean found = true;

		Pattern pattern = Pattern.compile(patStr);
		System.out.println("Get Reg Exp Pattern: " + pattern.pattern());
		while (found) {
			Matcher matcher = pattern.matcher(newText);
			if (matcher.find()) {
				System.out.println("Found Match " + matcher.group()
						+ " starting at index " + matcher.start()
						+ "and ending at index " + matcher.end());
				found = true;
				int length = matcher.end() - matcher.start();
				String fldName = matcher.group().substring(2, length - 2); // Pattern
																			// string
																			// is
																			// of
																			// the
																			// format
																			// @#fldName#@
				System.out.println("fldName " + fldName + " length " + length);
				newText = replaceFieldWithData(newText, nvPair, fldName,
						matcher.start(), matcher.end());
				count++;
			} else
				found = false;
		}
		return newText;
	}

	public static String replaceFieldWithEmptyData(String matchStr,
			String fldName, int pos_start, int pos_end) {
		String leftpart, rightpart, value;
		String newStr;

		value = "";
		leftpart = matchStr.substring(0, pos_start);
		rightpart = matchStr.substring(pos_end);
		newStr = new String(leftpart + value + rightpart);
		return newStr;
	}

	public static String replaceFieldWithData(String matchStr, HashMap fldVal,
			String fldName, int pos_start, int pos_end) {
		String leftpart, rightpart, value;
		String newStr;

		value = (String) fldVal.get(fldName);
		if (value == null) {
			System.out.println("Null value for fldName" + fldName);
			throw new RuntimeException("UNABLE TO FIND FIELD: " + fldName
					+ " IN NAME VALUE PAIR");
		}
		leftpart = matchStr.substring(0, pos_start);
		rightpart = matchStr.substring(pos_end);
		newStr = new String(leftpart + value + rightpart);
		return newStr;
	}

	public static String[] getTokenVals(String input, String delim) {
		Vector<String> tokens = new Vector<String>();
		StringTokenizer st = new StringTokenizer(input, delim);
		String[] tokenList = { input };
		if (st == null || st.countTokens() == 0)
			return tokenList;

		while (st.hasMoreTokens()) {
			tokens.addElement(new String(st.nextToken()));
		}

		tokenList = tokens.toArray(new String[0]);

		return tokenList;
	}

	public static String getHashMapDataStr(Map<String, String> mapData) {
		StringBuffer hashMapData = new StringBuffer();
		String[] mapDataKeys = mapData.keySet().toArray(new String[0]);
		for (int i = 0; i < mapDataKeys.length; i++) {
			hashMapData.append(mapDataKeys[i] + "="
					+ mapData.get(mapDataKeys[i]));
			if (i < mapDataKeys.length - 1)
				hashMapData.append("::");
		}
		return hashMapData.toString();
	}

	// Here Object are Basic Data Type
	public static String getHashMapData(Map<String, Object> mapData) {
		StringBuffer hashMapData = new StringBuffer();
		String[] mapDataKeys = mapData.keySet().toArray(new String[0]);
		for (int i = 0; i < mapDataKeys.length; i++) {
			Object objVal = mapData.get(mapDataKeys[i]);
			if (objVal.getClass().getName().equals("java.lang.String"))
				hashMapData.append(mapDataKeys[i] + "=" + (String) objVal);
			else if (objVal.getClass().getName().equals("java.lang.Integer"))
				hashMapData.append(mapDataKeys[i] + "="
						+ ((Integer) objVal).toString());
			else if (objVal.getClass().getName().equals("java.lang.Double"))
				hashMapData.append(mapDataKeys[i] + "="
						+ ((Double) objVal).toString());
			else if (objVal.getClass().getName().equals("java.lang.Float"))
				hashMapData.append(mapDataKeys[i] + "="
						+ ((Float) objVal).toString());
			else if (objVal.getClass().getName().equals("java.lang.Long"))
				hashMapData.append(mapDataKeys[i] + "="
						+ ((Long) objVal).toString());
			else
				hashMapData.append(mapDataKeys[i] + "=" + objVal.toString());
			if (i < mapDataKeys.length - 1)
				hashMapData.append("::");
		}
		return hashMapData.toString();
	}

	public static File writeToFile(String absFilePath, byte[] data)
			throws Exception {
		File dataFile = new File(absFilePath);
		FileOutputStream out = null;
		if (dataFile.isDirectory())
			throw new RuntimeException("Pass File instead of Dir");
		try {
			out = new FileOutputStream(dataFile);
			out.write(data);
			System.out.println("Ind File Size Read: " + data.length
					+ " Actual File Size: " + dataFile.length());
			out.flush();
			out.close();
		} finally {
			if (out != null)
				out.close();
		}
		return dataFile;
	}

	public static Point2D.Double[] getRectPoints(Rectangle rect, int numPts) {
		return getRectPoints(rect, numPts, null);
	}

	public static Point2D.Double[] getRectPoints(Rectangle rect, int numPts,
			Vector<Point2D.Double> pathPts) {
		if (numPts != 4)
			if (numPts != 8)
				numPts = 4;
		Point2D.Double[] pts = new Point2D.Double[numPts];
		int x = rect.x, y = rect.y, wt = rect.width, ht = rect.height;
		pts[0] = new Point2D.Double(x, y);
		pts[1] = new Point2D.Double(x + wt, y);
		pts[2] = new Point2D.Double(x + wt, y + ht);
		pts[3] = new Point2D.Double(x, y + ht);
		if (numPts == 8) {
			pts[4] = new Point2D.Double((x + wt / 2), y);
			pts[5] = new Point2D.Double(x + wt, (y + ht / 2));
			pts[6] = new Point2D.Double((x + wt / 2), (y + ht));
			pts[7] = new Point2D.Double(x, (y + ht / 2));
		}
		if (pathPts != null) {
			for (int i = 0; i < pts.length; i++)
				pathPts.addElement(pts[i]);
		}
		return pts;
	}

	public static boolean isTransformValid(Rectangle dispRect,
			Point2D.Double pt, int tx, int ty) {
		Point2D.Double[] pts = { pt };
		return isTransformValid(dispRect, pts, tx, ty);
	}

	public static boolean isTransformValid(Rectangle dispRect,
			Point2D.Double[] pts, int tx, int ty) {
		for (int i = 0; i < pts.length; i++) {
			Point2D.Double transPt = new Point2D.Double(pts[i].x + tx, pts[i].y
					+ ty);
			if (dispRect.contains(transPt))
				continue;
			return false;
		}
		return true;
	}

	public static Vector<Point2D.Double> getTransformedPt(
			AffineTransform affine, Vector<Point2D.Double> ptsVector) {
		return getTransformedPt(affine, ptsVector, null);
	}

	public static Vector<Point2D.Double> getTransformedPt(
			AffineTransform affine, Vector<Point2D.Double> ptsVector,
			Vector<Point2D.Double> transPtsVector) {
		if (transPtsVector == null)
			transPtsVector = new Vector<Point2D.Double>();
		else
			transPtsVector.clear();
		Point2D.Double[] pts = ptsVector.toArray(new Point2D.Double[0]);
		Point2D.Double[] transPt = new Point2D.Double[pts.length];
		for (int i = 0; i < pts.length; i++) {
			transPt[i] = (Point2D.Double) affine.transform(pts[i], null);
			transPtsVector.addElement(transPt[i]);
		}
		return transPtsVector;
	}

	public static Point2D.Double adjustPt(Point2D.Double origPt, double dx,
			double dy) {
		origPt.x = origPt.x - dx;
		origPt.y = origPt.y - dy;
		return origPt;
	}

	public static Point2D.Double[] adjustPt(Vector<Point2D.Double> vecPts,
			int tx, int ty) {
		return adjustPt(vecPts.toArray(new Point2D.Double[0]), tx, ty);
	}

	public static Point2D.Double[] adjustPt(Point2D.Double[] pts, int tx, int ty) {
		AffineTransform affine = new AffineTransform();
		affine.setToIdentity();
		return adjustPt(affine, pts, tx, ty);
	}

	public static Point2D.Double[] adjustPt(AffineTransform affine,
			Point2D.Double[] pts, int tx, int ty) {
		return adjustPt(affine, pts, tx, ty, true);
	}

	public static Point2D.Double[] adjustPt(AffineTransform affine,
			Point2D.Double[] pts, int tx, int ty, boolean updatePt) {
		Point2D.Double[] transPt = new Point2D.Double[pts.length];
		for (int i = 0; i < pts.length; i++) {
			transPt[i] = (Point2D.Double) affine.transform(pts[i], null);
			transPt[i].x = transPt[i].x + tx;
			transPt[i].y = transPt[i].y + ty;
			if (updatePt) {
				pts[i].x = transPt[i].x;
				pts[i].y = transPt[i].y;
			}
		}
		return transPt;
	}

	public static AffineTransform getAffineTransform(AffineTransform affine,
			Rectangle rect, double sx, double sy, double rotAng) {
		if (affine == null) {
			affine = new AffineTransform();
			affine.setToIdentity();
		}
		if (rotAng == 0.0 && sx == 1.0 && sy == 1.0)
			return affine;
		double theta = Math.toRadians(rotAng);

		double c1 = rect.x + rect.width / 2;

		// setting the point at which the rectangle will rotate.
		double c2 = rect.y + rect.height / 2;

		// starting point of the rectangle added with the 1/2 of the width
		// and height of the rectangle.
		affine.translate(c1, c2);
		if (sx != 1.0 || sy != 1.0)
			affine.scale(sx, sy);
		if (rotAng > 0)
			affine.rotate(theta);
		affine.translate(-c1, -c2);
		return affine;
	}

	public static AffineTransform transform(AffineTransform affine,
			double rotAng, double shx, double shy, boolean isRotFirst) {
		if (rotAng == 0.0 && shx == 0.0 && shy == 0.0)
			return affine;
		double theta = Math.toRadians(rotAng);
		if (isRotFirst) {
			affine.rotate(theta);
			affine.shear(shx, shy);
		} else {
			affine.shear(shx, shy);
			affine.rotate(theta);
		}
		return affine;
	}

	public static AffineTransform getRotationTransform(Rectangle rect,
			AffineTransform affine, double rotAng, double rtx, double rty) {
		if (rotAng == 0.0)
			return affine;
		double theta = Math.toRadians(rotAng);
		// setting the point at which the rectangle will rotate.
		double c1 = rect.x + rect.width / 2, c2 = rect.y + rect.height / 2;
		// c1 = rect.x; c2 = rect.y;
		// starting point of the rectangle added with the 1/2 of the width
		// and height of the rectangle.
		affine.translate(c1, c2);
		affine.rotate(theta);
		double dx = 0, dy = rect.y + rect.height / 2;
		affine.translate(-c1 + rtx, -c2 + rty);
		return affine;
	}

	public static AffineTransform getRotationTransform(Rectangle rect,
			int imgWt, int imgHt, AffineTransform affine, double rotAng,
			double rtx, double rty) {
		if (rotAng == 0.0)
			return affine;
		System.out.println("Img Wt: " + imgWt + " Ht: " + imgHt
				+ " And Bounds: " + rect);
		double theta = Math.toRadians(rotAng);
		// set the translation to the mid of the component
		double tx = 0, ty = 0;
		tx = rect.x + (rect.width - imgWt) / 2 + rtx;
		ty = rect.y + (rect.height - imgHt) / 2 + rty;
		// tx = (imgWt)/2, ty = (imgHt)/2;
		System.out.println("Tx: " + tx + " Ty: " + ty);
		affine.setToTranslation(tx, ty);
		// rotate with the anchor point as the mid of the image
		affine.rotate(theta, imgWt / 2, imgHt / 2);
		return affine;
	}

	public static AffineTransform getRectTransform(Rectangle rect,
			AffineTransform affine, double rotAng, double shx, double shy,
			boolean isRotFirst) {
		if (rotAng == 0.0 && shx == 0.0 && shy == 0.0)
			return affine;
		double theta = Math.toRadians(rotAng);
		if (isRotFirst) {
			double c1 = rect.x + rect.width / 2;
			// double c1 = rect.x;
			// setting the point at which the rectangle will rotate.
			double c2 = rect.y + rect.height / 2;
			// double c2 = rect.y;
			// starting point of the rectangle added with the 1/2 of the width
			// and height of the rectangle.
			affine.translate(c1, c2);
			// affine.scale(scx,scy);
			affine.rotate(theta);
			affine.shear(shx, shy);
			affine.translate(-c1, -c2);
			// c1 = rect.x; c2 = rect.y;
		} else {
			affine.shear(shx, shy);
			affine.rotate(theta);
		}
		return affine;
	}

	public static GeneralPath getTransformRect(AffineTransform affine,
			Rectangle rect, double rotAng, double shx, double shy,
			boolean isRotFirst) {
		GeneralPath gPath = null;
		Point2D.Double[] pt = new Point2D.Double[4];
		gPath = getRectGPath((double) rect.x, (double) rect.y,
				(double) rect.width, (double) rect.height, pt);
		// System.out.println(printGeneralPath(gPath));
		affine = getRectTransform(rect, affine, rotAng, shx, shy, isRotFirst);
		// System.out.println("Transformed Path: " + affine.toString());
		// gPath.transform(affine);
		gPath = (GeneralPath) gPath.createTransformedShape(affine);
		// System.out.println("/n/n********************/n/n");
		// System.out.println("PRINTING NEW GPATH\n");
		// System.out.println(printGeneralPath(gPath));
		return gPath;
	}

	public static Polygon getTransformPolygon(AffineTransform affine,
			Rectangle2D.Double rect, double rotAng, double shx, double shy,
			boolean isRotFirst) {
		return getTransformPolygon(affine, getRectangle(rect), rotAng, shx,
				shy, isRotFirst);
	}

	public static Polygon getTransformPolygon(AffineTransform affine,
			Rectangle rect, double rotAng, double shx, double shy,
			boolean isRotFirst) {
		Polygon poly = null;
		Point2D.Double[] pt = new Point2D.Double[4];
		poly = getRectPoly((double) rect.x, (double) rect.y,
				(double) rect.width, (double) rect.height, pt);
		// if (poly != null) return poly;
		// System.out.println(printGeneralPath(gPath));
		affine = getRectTransform(rect, affine, rotAng, shx, shy, isRotFirst);
		// affine = transform(affine, rotAng, shx, shy, isRotFirst);
		Point2D.Double[] transPt = new Point2D.Double[4];
		affine.transform(pt, 0, transPt, 0, 4);
		return getRectPoly(transPt);
	}

	public static GeneralPath getRectGPath(double x, double y, double wt,
			double ht, Point2D.Double[] pt) {
		pt[0] = new Point2D.Double(x, y);
		pt[1] = new Point2D.Double(x + wt, y);
		pt[2] = new Point2D.Double(x + wt, y + ht);
		pt[3] = new Point2D.Double(x, y + ht);
		return getRectGPath(pt);
	}

	public static GeneralPath getRectGPath(Point2D.Double[] pt) {
		GeneralPath gPath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
		gPath.moveTo(pt[0].x, pt[0].y);
		gPath.lineTo(pt[1].x, pt[1].y);
		gPath.lineTo(pt[2].x, pt[2].y);
		gPath.lineTo(pt[3].x, pt[3].y);
		gPath.closePath();
		return gPath;
	}

	public static Polygon getRectPoly(double x, double y, double wt, double ht,
			Point2D.Double pt[]) {
		int npoints = 4;
		pt[0] = new Point2D.Double(x, y);
		pt[1] = new Point2D.Double(x + wt, y);
		pt[2] = new Point2D.Double(x + wt, y + ht);
		pt[3] = new Point2D.Double(x, y + ht);
		return getRectPoly(pt);
	}

	public static Polygon getRectPoly(Point2D.Double pt[]) {
		java.awt.Polygon poly = new java.awt.Polygon();
		poly.addPoint((int) pt[0].x, (int) pt[0].y);
		poly.addPoint((int) pt[1].x, (int) pt[1].y);
		poly.addPoint((int) pt[2].x, (int) pt[2].y);
		poly.addPoint((int) pt[3].x, (int) pt[3].y);
		return poly;
	}

	public static Point2D.Double getStartPoint(Shape shape) {
		System.out.println("In getStartPoint Shape Bounds: "
				+ shape.getBounds());
		PathIterator pIter = new FlatteningPathIterator(
				shape.getPathIterator(null), 1);
		double x = 0.0, y = 0.0;
		double[] coords = new double[6];
		while (!pIter.isDone()) {
			if (pIter == null)
				break;
			int currSeg = pIter.currentSegment(coords);
			if (currSeg == PathIterator.SEG_MOVETO) {
				for (int iter = 0; iter < coords.length; iter++) {
					if (iter > 1)
						break;
					if (iter == 0)
						x = coords[iter];
					if (iter == 1)
						y = coords[iter];
				}
				return new Point2D.Double(x, y);
			}
			pIter.next();
		} // while(pIter.next())
		return null;
	}

	public static String printShape(Shape shape) {
		System.out.println("In printShape Shape Bounds: " + shape.getBounds());
		StringBuffer mesg = new StringBuffer();
		PathIterator pIter = new FlatteningPathIterator(
				shape.getPathIterator(null), 1);
		// PathIterator pIter = shape.getPathIterator(null);
		mesg.append("\n----PRINTING PATH DATA POINTS----\n");
		int data_index = 0;
		double[] coords = new double[6];
		String pathSeg = "";
		while (!pIter.isDone()) {
			if (pIter == null)
				break;
			int currSeg = pIter.currentSegment(coords);
			if (currSeg == PathIterator.SEG_MOVETO)
				pathSeg = "MOVE TO ";
			else if (currSeg == PathIterator.SEG_LINETO)
				pathSeg = "LINE TO ";
			else if (currSeg == PathIterator.SEG_CUBICTO)
				pathSeg = "CUBIC TO ";
			else if (currSeg == PathIterator.SEG_QUADTO)
				pathSeg = "QUAD TO ";
			else if (currSeg == PathIterator.SEG_CLOSE)
				pathSeg = "CLOSE PATH ";
			mesg.append(pathSeg);
			for (int iter = 0; iter < coords.length; iter++) {
				mesg.append(coords[iter] + " ");
			}
			mesg.append("\n");
			pIter.next();
		} // while(pIter.next())
		return mesg.toString();
	} // End of Function printClipPath(StringBuffer mesg)

	// THis is the newer method tested with Shapes Rotation
	public static double calcAngle(Point centerPt, Point dragPt) {
		// System.out.println("centerPt: " + centerPt+" New Point: " + dragPt);
		int dx = dragPt.x - centerPt.x, dy = dragPt.y - centerPt.y;
		// int reldx = dragPt.x - prevDragPt.x, reldy = dragPt.y - prevDragPt.y;
		// System.out.println("dx: " + dx + " dy: " + dy);
		double angle = 0.0d;
		double ang_90 = Math.PI / (double) 2;
		double raw_ang = Math.atan(Math.abs((double) dy)
				/ Math.abs((double) dx));
		double raw_ang_inv = Math.atan(Math.abs((double) dx)
				/ Math.abs((double) dy));
		// System.out.println("raw_ang in radians: " + raw_ang);
		// System.out.println("raw_ang in Degrees: " + Math.toDegrees(raw_ang));
		// System.out.println("raw_ang_inv in Degrees: " +
		// Math.toDegrees(raw_ang_inv));

		if (dx > 0 && dy != 0) // Ist or 4th Quadrant
		{
			if (dy > 0)
				angle = raw_ang; // 1st Quadrant
			if (dy < 0) {
				angle = 3 * ang_90 + raw_ang_inv; // 4th Quadrant
			}
		} else if (dx < 0 && dy != 0) // 2nd or 3rd Quadrant
		{
			if (dy > 0)
				angle = ang_90 + raw_ang_inv; // 2nd Quadrant
			if (dy < 0)
				angle = 2 * ang_90 + raw_ang; // 3rd Quadrant
		} else if (dx == 0) {
			if (dy > 0)
				angle = ang_90;
			else if (dy < 0)
				angle = 3 * ang_90;
		} else if (dy == 0) {
			if (dx > 0)
				angle = 0;
			else if (dx < 0)
				angle = 2 * ang_90;
		}
		// System.out.println("Angle in radians: " + angle);
		// angle = (angle * 180) / Math.PI;
		double angleInDeg = Math.toDegrees(angle);
		// System.out.println("Angle In Degrees: " + angleInDeg);
		return angleInDeg;
	}

	// Return angle in radians
	public static double getAngle(Point2D.Double vertPt, Point2D.Double edgePt) {
		// if loop is 1st and 4th qudrant, else loop is 2nd and 3rd quadrant
		double dx = Math.abs(edgePt.x - vertPt.x), dy = Math.abs(edgePt.y
				- vertPt.y);
		if (dx == 0.0D) {
			if (edgePt.y > vertPt.y)
				return Math.PI / 2.0D;
			else
				return 3 * Math.PI / 2.0D;
		} else if (dy == 0.0D) {
			if (edgePt.x > vertPt.x)
				return 0.0D;
			else
				return Math.PI;
		}
		if (edgePt.x > vertPt.x) {
			// If loop is 1st Quadrant and else is 4th Quadrant
			if (edgePt.y > vertPt.y)
				return Math.atan(dy / dx);
			else
				return (3 * Math.PI / 2.0D + Math.atan(dy / dx));
		} else {
			// If loop is 2nd Quadrant and else is 3rd Quadrant
			if (edgePt.y > vertPt.y)
				return (Math.PI / 2.0D + Math.atan(dx / dy));
			else
				return (Math.PI + Math.atan(dy / dx));
		}
	}

	public static double getAngle(java.awt.Point a, java.awt.Point b) {
		double dx = b.getX() - a.getX();
		double dy = b.getY() - a.getY();
		System.out.println("dx: " + dx + " dy: " + dy);
		double angle = 0.0d;

		if (dx == 0.0) {
			if (dy == 0.0)
				angle = 0.0;
			else if (dy > 0.0)
				angle = Math.PI / 2.0;
			else
				angle = (Math.PI * 3.0) / 2.0;
		} else if (dy == 0.0) {
			if (dx > 0.0)
				angle = 0.0;
			else
				angle = Math.PI;
		} else {
			if (dx < 0.0)
				angle = Math.atan(dy / dx) + Math.PI;
			else if (dy < 0.0)
				angle = Math.atan(dy / dx) + (2 * Math.PI);
			else
				angle = Math.atan(dy / dx);
		}
		System.out.println("angle: " + angle);
		angle = (angle * 180) / Math.PI;
		System.out.println("Recalc Angle: " + angle);
		return angle;
	}

	public static Rectangle getRectangle(Point pt, int margin) {
		Rectangle rect = new Rectangle(pt.x - margin, pt.y - margin,
				2 * margin, 2 * margin);
		return rect;
	}

	public static Rectangle getScaledRect(Rectangle bounds, int margin) {
		bounds = new Rectangle(bounds.x - margin, bounds.y - margin,
				bounds.width + (int) Math.round(1.5 * margin), bounds.height
						+ (int) Math.round(1.5 * margin));
		return bounds;
	}

	public static Shape getShape(Point2D.Double pt1, Point2D.Double pt2,
			int shapeMode) {
		Vector<Point2D.Double> shPts = new Vector<Point2D.Double>();
		shPts.addElement(pt1);
		shPts.addElement(pt2);
		return getShape(shPts, shapeMode, null);
	}

	public static Shape getShape(Vector<Point2D.Double> shPts, int shapeMode) {
		return getShape(shPts, shapeMode, null);
	}

	public static Shape getShape(Vector<Point2D.Double> shPts, int shapeMode,
			Point2D.Double newStartPt) {
		double wt = 0.0, ht = 0.0;
		Point2D.Double pt1 = shPts.elementAt(0), pt2 = shPts.elementAt(1);
		double startX = pt1.x, startY = pt1.y, lastX = pt2.x, lastY = pt2.y;
		double newStartX = startX, newStartY = startY;
		double newLastX = lastX, newLastY = lastY;
		double dx = 0.0, dy = 0.0;
		if (newStartPt != null) {
			// System.out.println("New StartPt: " + newStartPt);
			newStartX = newStartPt.x;
			newStartY = newStartPt.y;
		}
		if (shapeMode == StaticFieldInfo.LINE_PATH) {
			wt = lastX - startX;
			ht = lastY - startY;
			Line2D.Double line = null;
			if (newStartPt != null) {
				if (startX < lastX)
					dx = 0.0;
				if (startX > lastX)
					dx = startX - lastX;
				if (startY < lastY)
					dy = 0.0;
				if (startY > lastY)
					dy = startY - lastY;
				newStartX = newStartPt.x + dx;
				newStartY = newStartPt.y + dy;
				newLastX = newStartPt.x + wt + dx;
				newLastY = newStartPt.y + ht + dy;
			}
			line = new Line2D.Double(newStartX, newStartY, newLastX, newLastY);
			// System.out.println("Line pt1 : " + line.getP1() + " and pt2: " +
			// line.getP2());
			return line;
		}
		if (shapeMode == StaticFieldInfo.CIRCLE_PATH) {
			wt = lastX - startX;
			ht = wt;
			return new Ellipse2D.Double(newStartX, newStartY, wt, ht);
		}
		if (shapeMode == StaticFieldInfo.ELLIPSE_PATH) {
			wt = lastX - startX;
			ht = lastY - startY;
			return new Ellipse2D.Double(newStartX, newStartY, wt, ht);
		}
		if (shapeMode == StaticFieldInfo.SQUARE_PATH) {
			wt = lastX - startX;
			ht = wt;
			return new Rectangle2D.Double(newStartX, newStartY, wt, ht);
		}
		if (shapeMode == StaticFieldInfo.RECTANGLE_PATH) {
			wt = lastX - startX;
			ht = lastY - startY;
			return new Rectangle2D.Double(newStartX, newStartY, wt, ht);
		}
		if (shapeMode == StaticFieldInfo.GRAPHIC_PATH) {
			return createGeneralPath(shPts, newStartPt);
		}
		return null;
	}

	public static GeneralPath createGeneralPath(
			Vector<Point2D.Double> pathPoints) {
		return createGeneralPath(pathPoints, null);
	}

	public static GeneralPath createGeneralPath(
			Vector<Point2D.Double> pathPoints, Point2D.Double newStartPt) {
		GeneralPath path = new GeneralPath();
		Point2D.Double start0 = pathPoints.elementAt(0);
		double dx = 0.0, dy = 0.0;
		if (newStartPt != null) {
			dx = start0.x - newStartPt.x;
			dy = start0.y - newStartPt.y;
			start0 = newStartPt;
		}
		path.moveTo(start0.x, start0.y);
		for (int i = 1; i < pathPoints.size(); i = i + 3) {
			Point2D.Double cntrl1 = pathPoints.elementAt(i);
			cntrl1.x = cntrl1.x - dx;
			cntrl1.y = cntrl1.y - dy;

			if (i + 1 > pathPoints.size() - 1)
				continue;
			Point2D.Double cntrl2 = pathPoints.elementAt(i + 1);
			cntrl2.x = cntrl2.x - dx;
			cntrl2.y = cntrl2.y - dy;

			if (i + 2 > pathPoints.size() - 1)
				continue;
			Point2D.Double end = pathPoints.elementAt(i + 2);
			end.x = end.x - dx;
			end.y = end.y - dy;
			path.curveTo(cntrl1.x, cntrl1.y, cntrl2.x, cntrl2.y, end.x, end.y);
		}
		// System.out.println("****\n\n GPath Bounds: " + path.getBounds() +
		// "\n\n*****");
		return path;
	}

	public static float measurePathLength(Shape shape) {
		float FLATNESS = 1;
		PathIterator it = new FlatteningPathIterator(
				shape.getPathIterator(null), FLATNESS);
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		float total = 0;
		while (!it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				break;

			case PathIterator.SEG_CLOSE:
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....

			case PathIterator.SEG_LINETO:
				thisX = points[0];
				thisY = points[1];
				float dx = thisX - lastX;
				float dy = thisY - lastY;
				total += (float) Math.sqrt(dx * dx + dy * dy);
				lastX = thisX;
				lastY = thisY;
				break;
			}
			it.next();
		}
		return total;
	}

	public static String StackTraceToString(Throwable ex) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteStream);
		ex.printStackTrace(printStream);
		printStream.flush();
		String stackTrace = byteStream.toString();
		printStream.close();
		return stackTrace;
	}

	// Retrieves the Color based on Delim
	public static Color getColVal(String rgb, String delim) {
		RGPTLogger.logToConsole("RGB Color: " + rgb);
		String[] rgbCol = rgb.split(delim);
		Color color = new Color((new Integer(rgbCol[0]).intValue()),
				(new Integer(rgbCol[1]).intValue()),
				(new Integer(rgbCol[2]).intValue()));
		return color;
	}

	// Retrieves the Text after removing the Delim
	public static String replaceTextWithDelim(String text, String delim,
			String newDelim) {
		StringBuffer textWithNewDelim = new StringBuffer();
		String[] textList = text.split(delim);
		for (int i = 0; i < textList.length; i++) {
			textWithNewDelim.append(textList[i]);
			if (i < textList.length)
				textWithNewDelim.append(newDelim);
		}
		// RGPTLogger.logToConsole("New Text: "+textWithNewDelim.toString());
		return textWithNewDelim.toString();
	}

	public static final int STRING_STARTS_WITH = 1;
	public static final int STRING_ENDS_WITH = 2;
	public static final int STRING_ANYWHERE = 3;
	public static final int STRING_MATCH_STARTS_WITH = 4;
	public static final int STRING_MATCH_ENDS_WITH = 5;
	public static final int STRING_MATCH_ANYWHERE = 6;

	public static boolean contains(String fullText, String subText, int logic) {
		// Starts with
		if (logic == STRING_STARTS_WITH)
			return fullText.startsWith(subText);
		// Ends with
		if (logic == STRING_ENDS_WITH)
			return fullText.endsWith(subText);
		// Anywhere
		if (logic == STRING_ANYWHERE)
			return fullText.indexOf(subText) > 0;

		// To ignore case, regular expressions must be used
		// Starts with
		if (logic == STRING_MATCH_STARTS_WITH)
			return fullText.matches("(?i)" + subText + ".*");
		// Ends with
		if (logic == STRING_MATCH_ENDS_WITH)
			return fullText.matches("(?i).*" + subText);
		// Anywhere
		if (logic == STRING_MATCH_ANYWHERE)
			return fullText.matches("(?i).*" + subText + ".*");

		// Uses String Contains Method
		return fullText.contains(subText);
	}

	public static Vector<String> filterActionValues(String[] actionValues,
			String filter) {
		Vector<String> actionIds = new Vector<String>();
		for (int i = 0; i < actionValues.length; i++)
			if (actionValues[i].startsWith(filter))
				actionIds.addElement(actionValues[i]);
		return actionIds;
	}

	public static String[] enumToStringArray(Object[] enumValues) {
		String[] strValues = new String[enumValues.length];
		for (int i = 0; i < enumValues.length; i++)
			strValues[i] = enumValues[i].toString();
		return strValues;
	}

	public static boolean contains(Object[] objArrays, String key) {
		String[] strValues = enumToStringArray(objArrays);
		return contains(strValues, key);
	}

	public static boolean contains(String[] mainList, String key) {
		Arrays.sort(mainList);
		int index = Arrays.binarySearch(mainList, key);
		if (index >= 0)
			return true;
		return false;
	}

	public static int getKeyIndex(String[] mainList, String key) {
		Vector<String> vect = createVector(mainList);
		return vect.indexOf(key);
	}

	// This not only gets the Shapes Directory Structure but also creates one if
	// not existent

	public static String[] getShapeFiles(String userType) {
		String shapeOutDir = getShapesDirectory(userType);
		File shapeDir = new File(shapeOutDir);
		String[] files = shapeDir.list(FileFilterFactory.XON_SHAPE_FILE_FILTER);
		return files;
	}

	public static String getShapesDirectory(String userType) {
		String outDir = RGPTParams.getVal("XONOutDir");
		createDir(outDir, true);
		String shapeDir = RGPTParams.getVal("XONUserShapesDir");
		if (userType.equals("admin"))
			shapeDir = RGPTParams.getVal("XONSystemShapesDir");
		String shapeOutDir = outDir + shapeDir;
		createDir(shapeOutDir, true);
		return shapeOutDir;
	}

	// This not only gets the Shapes Directory Structure but also creates one if
	// not existent
	public static String getXONImageDir() {
		String outDir = RGPTParams.getVal("XONOutDir");
		createDir(outDir, true);
		String imgDir = RGPTParams.getVal("XONImageDesigner");
		String imgOutDir = outDir + imgDir;
		createDir(imgOutDir, true);
		return imgOutDir;
	}

	public static String saveImageData(String fileName, byte[] imgStr,
			String imgSrcDir) throws Exception {
		if (imgSrcDir == null) {
			String xonImgDir = getXONImageDir();
			String currDate = getCurrentDate("dd-MM-yyyy");
			String origFileName = RGPTFileFilter.getFileName(fileName);
			imgSrcDir = xonImgDir + origFileName + "-" + currDate + "/";
			createDir(imgSrcDir, true);
		}
		writeToFile(imgSrcDir + fileName, imgStr);
		return imgSrcDir;
	}

	public static Dimension getCompSize(String propName) {
		String[] val = RGPTParams.getVal(propName).split("::");
		return new Dimension((new Integer(val[0])).intValue(), (new Integer(
				val[1])).intValue());
	}

	public static Map<String, String> getPropertyValues(String propName) {
		String[] propValues = RGPTParams.getVal(propName).split("::");
		Map<String, String> propValMap = new HashMap<String, String>();
		LocalizationUtil lu = new LocalizationUtil();
		for (int i = 0; i < propValues.length; i++) {
			propValMap.put(propValues[i], lu.getText(propValues[i]));
		}
		return propValMap;
	}

	public static double getDistance(Point2D.Double pt1, Point2D.Double pt2) {
		double dx = pt2.x - pt1.x, dy = pt2.y - pt1.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static boolean isEqual(java.util.List list1, java.util.List list2) {
		ListIterator e1 = list1.listIterator();
		ListIterator e2 = list2.listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			Object o1 = e1.next();
			Object o2 = e2.next();
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	// This static variable indicates no draging of path pts or boundary pts
	public final static int NOT_DRAGGING = -1;
	// This indicates if the mouse click is within the neighbourhood of path pts
	// or boundary pts
	public final static int NEIGHBORHOOD = 15;

	public static Map<String, Object> findDraggingPt(
			Vector<Point2D.Double> gPathPoints, Point pt) {
		Map<String, Object> dragData = new HashMap<String, Object>();
		int dragIndex = NOT_DRAGGING;
		int minDistance = Integer.MAX_VALUE;
		int indexOfClosestPoint = -1;
		Point2D.Double[] points = gPathPoints.toArray(new Point2D.Double[0]);
		for (int i = 0; i < points.length; i++) {
			double deltaX = points[i].x - pt.x;
			double deltaY = points[i].y - pt.y;
			int distance = (int) (Math.sqrt(deltaX * deltaX + deltaY * deltaY));
			if (distance < minDistance) {
				minDistance = distance;
				indexOfClosestPoint = i;
			}
		}
		if (minDistance > NEIGHBORHOOD)
			return null;
		dragIndex = indexOfClosestPoint;
		dragData.put("DragIndex", dragIndex);
		return dragData;
	}

	public static int contains(Vector<Shape> shapes, double x, double y) {
		Shape shape = null;
		for (int i = 0; i < shapes.size(); i++) {
			shape = shapes.elementAt(i);
			if (shape.contains(x, y))
				return i;
		}
		return -1;
	}

	// Using the String can be converted to Int, Float or Double
	public static float roundDecimals(double d, int numOfDec) {
		if (numOfDec <= 0)
			return Math.round(d);
		String decFormatStr = "#.";
		for (int i = 0; i < numOfDec; i++)
			decFormatStr += "#";
		DecimalFormat decFormat = new DecimalFormat(decFormatStr);
		return Float.valueOf(decFormat.format(d)).floatValue();
	}

	public static double getDoubleValue(String str) {
		return (Double.valueOf(str)).doubleValue();
	}

	public static float getFloatValue(String str) {
		return (Float.valueOf(str)).floatValue();
	}

	public static int getIntValue(String str) {
		return (Integer.valueOf(str)).intValue();
	}

	public static Color[] getDifferentColors(int n) {
		Color[] cols = new Color[n];
		for (int i = 0; i < n; i++)
			cols[i] = Color.getHSBColor((float) i / n, 1, 1);
		return cols;
	}

	public static int[] RGB_COLORS = null;

	public static int getRGBColor(int totCols, int col) {
		if (totCols < col)
			totCols = col;
		if (RGB_COLORS == null || RGB_COLORS.length < totCols)
			getRGBColors(totCols);
		return RGB_COLORS[col];
	}

	public static int[] getRGBColors(int n) {
		if (RGB_COLORS != null && RGB_COLORS.length >= n)
			return RGB_COLORS;
		RGB_COLORS = new int[n];
		Color col = null;
		for (int i = 0; i < n; i++) {
			if (i == 0)
				col = Color.BLACK;
			else
				col = Color.getHSBColor((float) i / n, 1, 1);
			RGB_COLORS[i] = col.getRGB();
		}
		return RGB_COLORS;
	}

	public static Rectangle getRectangle(Point2D p1, Point2D p2) {
		double x1, y1, x2, y2;
		double resX1, resY1, resWidth, resHeight;
		x1 = p1.getX();
		y1 = p1.getY();
		x2 = p2.getX();
		y2 = p2.getY();
		resX1 = Math.min(x1, x2);
		resY1 = Math.min(y1, y2);
		resWidth = Math.abs(x1 - x2);
		resHeight = Math.abs(y1 - y2);
		Rectangle rect = new Rectangle((int) resX1, (int) resY1,
				(int) resWidth, (int) resHeight);
		return rect;
	}

}