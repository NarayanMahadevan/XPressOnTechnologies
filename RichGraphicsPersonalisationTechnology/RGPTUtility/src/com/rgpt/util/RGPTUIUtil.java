// RGPT PACKAGES
package com.rgpt.util;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.layoututil.BasicGridLayout;
import com.rgpt.layoututil.ParagraphLayout;
import com.rgpt.viewer.ImagePreview;

public class RGPTUIUtil {

	// This static variable indicates no draging of path pts or boundary pts
	public final static int NOT_DRAGGING = -1;
	// This indicates if the mouse click is within the neighbourhood of path pts
	// or boundary pts
	public final static int NEIGHBORHOOD = 15;

	public static Map<String, Object> findBoundaryDrag(boolean isScale,
			Rectangle pathBounds, Point setPt) {
		Map<String, Object> dragData = new HashMap<String, Object>();
		int dragCursor = Cursor.DEFAULT_CURSOR, dragIndex = NOT_DRAGGING;
		dragData.put("DragIndex", dragIndex);
		dragData.put("DragCursor", dragCursor);
		// Only the Mouse Point outside the Shape Bounds is considered
		if (pathBounds == null || pathBounds.contains(setPt.x, setPt.y))
			return null;
		Point2D.Double[] boundPts = null;
		if (isScale)
			boundPts = RGPTUtil.getRectPoints(pathBounds, 8);
		else
			boundPts = RGPTUtil.getRectPoints(pathBounds, 4);
		int minDistance = Integer.MAX_VALUE, indexOfClosestPoint = -1;
		for (int i = 0; i < boundPts.length; i++) {
			double deltaX = boundPts[i].x - setPt.x;
			double deltaY = boundPts[i].y - setPt.y;
			int distance = (int) (Math.sqrt(deltaX * deltaX + deltaY * deltaY));
			if (distance < minDistance) {
				minDistance = distance;
				indexOfClosestPoint = i;
			}
		}
		if (minDistance > NEIGHBORHOOD)
			return null;

		dragIndex = indexOfClosestPoint;
		if (isScale) {
			if (dragIndex == 0)
				dragCursor = Cursor.NW_RESIZE_CURSOR;
			else if (dragIndex == 1)
				dragCursor = Cursor.NE_RESIZE_CURSOR;
			else if (dragIndex == 2)
				dragCursor = Cursor.SE_RESIZE_CURSOR;
			else if (dragIndex == 3)
				dragCursor = Cursor.SW_RESIZE_CURSOR;
			else if (dragIndex == 4)
				dragCursor = Cursor.N_RESIZE_CURSOR;
			else if (dragIndex == 5)
				dragCursor = Cursor.E_RESIZE_CURSOR;
			else if (dragIndex == 6)
				dragCursor = Cursor.S_RESIZE_CURSOR;
			else if (dragIndex == 7)
				dragCursor = Cursor.W_RESIZE_CURSOR;
			else
				dragIndex = NOT_DRAGGING;
		} else {
			if (dragIndex == 0)
				dragCursor = CursorController.NW_ROTATE_CURSOR;
			else if (dragIndex == 1)
				dragCursor = CursorController.NE_ROTATE_CURSOR;
			else if (dragIndex == 2)
				dragCursor = CursorController.SE_ROTATE_CURSOR;
			else if (dragIndex == 3)
				dragCursor = CursorController.SW_ROTATE_CURSOR;
			else
				dragIndex = NOT_DRAGGING;
		}
		dragData.put("DragIndex", dragIndex);
		dragData.put("DragCursor", dragCursor);
		return dragData;
	}

	public static Map<String, Object> processBoundaryDrag(boolean isScale,
			Rectangle dispPanelArea, Rectangle pathBounds,
			Map<String, Object> dragData, Point currPt) {
		AffineTransform affine = new AffineTransform();
		affine.setToIdentity();
		double rotAngle = 0.0D;
		if (dragData.get("RotAngle") != null)
			rotAngle = ((Double) dragData.get("RotAngle")).doubleValue();
		dragData.put("AffineTransform", affine);
		dragData.put("RotAngle", rotAngle);
		int dragIndex = ((Integer) dragData.get("DragIndex")).intValue();
		int dragCursor = ((Integer) dragData.get("DragCursor")).intValue();
		if (dragCursor == 0 || dragIndex == NOT_DRAGGING)
			return dragData;
		Point2D.Double[] boundPts = null;

		// This part of the code is executed if Rotation is performed on the
		// element
		if (!isScale && dragCursor >= 100) {
			Point centerPt = new Point(), dragPt = new Point();
			centerPt.x = pathBounds.x + (int) (pathBounds.width / 2);
			centerPt.y = pathBounds.y + (int) (pathBounds.height / 2);
			if (dragCursor == CursorController.NE_ROTATE_CURSOR)
				dragPt.move(currPt.x, currPt.y + pathBounds.height);
			dragPt.move(currPt.x, currPt.y);
			double angle = RGPTUtil.getAngle(centerPt, dragPt);
			if (dragCursor == CursorController.NW_ROTATE_CURSOR
					|| dragCursor == CursorController.SW_ROTATE_CURSOR)
				angle = angle - 180;
			if (dragCursor == CursorController.NE_ROTATE_CURSOR)
				angle = angle + 90;
			int angleDiff = (int) angle - (int) rotAngle;
			if (angleDiff == 0)
				return dragData;
			if (angleDiff < 0)
				angleDiff = 360 + angleDiff;
			affine = RGPTUtil.getAffineTransform(affine, pathBounds, 1.0, 1.0,
					angleDiff);
			dragData.put("AffineTransform", affine);
			dragData.put("RotAngle", angle);
			return dragData;
		}

		// This part of the code is executed if Scale is performed on the
		// element
		boundPts = RGPTUtil.getRectPoints(pathBounds, 8);
		Point2D.Double dragPt = boundPts[dragIndex];
		int dx = currPt.x - (int) dragPt.x, dy = currPt.y - (int) dragPt.y;
		if (!RGPTUtil.isTransformValid(dispPanelArea, boundPts, dx, dy))
			return dragData;

		// Calculating the Impact on the Boundary Box to calculate scale
		int width = 0, height = 0;
		Rectangle newRect = new Rectangle(pathBounds);
		switch (dragCursor) {
		case Cursor.NW_RESIZE_CURSOR:
			if (dx > dy)
				dy = dx;
			else
				dx = dy;
			width = newRect.width - dx;
			height = newRect.height - dy;
			newRect.setRect(newRect.x + dx, newRect.y + dy, width, height);
			break;
		case Cursor.NE_RESIZE_CURSOR:
			if (Math.abs(dx) > Math.abs(dy))
				dy = -dx;
			else
				dx = Math.abs(dy);
			width = newRect.width + dx;
			height = newRect.height - dy;
			newRect.setRect(newRect.x + dx, newRect.y + dy, width, height);
			break;
		case Cursor.SE_RESIZE_CURSOR:
			if (dx > dy)
				dy = dx;
			else
				dx = dy;
			width = newRect.width + dx;
			height = newRect.height + dy;
			newRect.setRect(newRect.x + dx, newRect.y + dy, width, height);
			break;
		case Cursor.SW_RESIZE_CURSOR:
			if (Math.abs(dx) > Math.abs(dy))
				dy = Math.abs(dx);
			else
				dx = -dy;
			width = newRect.width - dx;
			height = newRect.height + dy;
			newRect.setRect(newRect.x + dx, newRect.y + dy, width, height);
			break;
		case Cursor.N_RESIZE_CURSOR:
			dx = 0;
			height = newRect.height - dy;
			newRect.setRect(newRect.x, newRect.y + dy, newRect.width, height);
			break;
		case Cursor.E_RESIZE_CURSOR:
			dy = 0;
			width = newRect.width + dx;
			newRect.setRect(newRect.x + dx, newRect.y, width, newRect.height);
			break;
		case Cursor.S_RESIZE_CURSOR:
			dx = 0;
			height = newRect.height + dy;
			newRect.setRect(newRect.x, newRect.y + dy, newRect.width, height);
			break;
		case Cursor.W_RESIZE_CURSOR:
			dy = 0;
			width = newRect.width - dx;
			newRect.setRect(newRect.x + dx, newRect.y, width, newRect.height);
			break;
		default:
			System.out.println("unexpected type: " + dragCursor);
		}
		// No Impact is done to the Image if the size goes below 5 pixel
		if (newRect.width <= 5 || newRect.height <= 5)
			return dragData;
		double sx = (double) newRect.width / (double) pathBounds.width;
		double sy = (double) newRect.height / (double) pathBounds.height;
		affine = RGPTUtil.getAffineTransform(affine, pathBounds, sx, sy, 0);
		dragData.put("AffineTransform", affine);
		dragData.put("RotAngle", 0.0D);
		return dragData;
	}

	public static Shape getTextOutline(FontRenderContext frc, Font font,
			String text) {
		if (frc == null)
			frc = new FontRenderContext(null, true, true);
		TextLayout textLayout = new TextLayout(text, font, frc);
		Shape outline = textLayout.getOutline(null);
		return outline;
	}

	public static float getMaxFontSize(Font font, float fontSz, String text,
			double panelWt, double panelHt) {
		return getFontSize(font, fontSz, text, panelWt, panelHt, true);
	}

	public static float getMinFontSize(Font font, float fontSz, String text,
			double panelWt, double panelHt) {
		return getFontSize(font, fontSz, text, panelWt, panelHt, false);
	}

	public static float getActualFontSize(Font font, float fontSz, String text,
			double panelWt, double panelHt) {
		float maxFontSz = getFontSize(font, fontSz, text, panelWt, panelHt,
				true);
		float minFontSz = getFontSize(font, fontSz, text, panelWt, panelHt,
				false);
		// System.out.println("Initial Font Sz: " + fontSz + " Max Font Sz: " +
		// maxFontSz + " Min Font Sz: " + minFontSz);
		Font newfont = font.deriveFont(maxFontSz);
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(newfont);
		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		// System.out.println("Font Wt: " + width + " Panel Wt: " + panelWt);
		if (width <= panelWt && (panelHt == -1 || height <= panelHt))
			return maxFontSz;
		else
			return minFontSz;
	}

	public static float getFontSize(Font font, float fontSz, String text,
			double panelWt, double panelHt, boolean getMax) {
		// Get fontmetrics and calculate position.
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		double percMargin = 0.01;
		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		int ascent = fm.getMaxAscent();
		int descent = fm.getDescent();
		int leading = fm.getLeading();
		boolean brealLoop = false;
		Font newfont = font;
		float newFontSz = fontSz;
		while (true) {
			if (getMax) {
				if (width > panelWt || (panelHt != -1 && height > panelHt))
					break;
				else
					fontSz = newFontSz;
				newFontSz = newFontSz
						+ (float) (newFontSz * (float) percMargin);
			} else {
				if (width < panelWt)
					break;
				else
					fontSz = newFontSz;
				newFontSz = newFontSz
						- (float) (newFontSz * (float) percMargin);
			}
			newfont = newfont.deriveFont(newFontSz);
			fm = Toolkit.getDefaultToolkit().getFontMetrics(newfont);
			width = fm.stringWidth(text);
			height = fm.getHeight();
		}
		// System.out.println("New Font Size: " + fontSz + " Max Font Sz: " +
		// newFontSz);
		return fontSz;
	}

	public static Font deriveFont(String fontFile, float fontSize, int color)
			throws Exception {
		Hashtable<TextAttribute, Object> fontAttrib = null;
		fontAttrib = getFontAttrib(fontSize, color);
		Font font = null;
		if (fontFile != null && fontFile.length() > 0)
			font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFile));
		else
			font = new Font("Arial", Font.PLAIN, 1);
		font = font.deriveFont(fontSize);
		// System.out.println("Font: " + font.toString());
		// System.out.println("Font Attributes: " + fontAttrib.toString());
		Font newFont = font.deriveFont(fontAttrib);
		return newFont;
	}

	public static Font deriveFont(Font font, int color) throws Exception {
		Hashtable<TextAttribute, Object> fontAttrib = null;
		fontAttrib = new Hashtable<TextAttribute, Object>();
		fontAttrib.put(TextAttribute.FOREGROUND, new java.awt.Color(color));
		Font newFont = font.deriveFont(fontAttrib);
		return newFont;
	}

	public static Hashtable<TextAttribute, Object> getFontAttrib(
			float fontSize, int color) {
		Hashtable<TextAttribute, Object> fontAttrib = null;
		fontAttrib = new Hashtable<TextAttribute, Object>();
		fontAttrib.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		fontAttrib.put(TextAttribute.SIZE, new Float(fontSize));
		fontAttrib.put(TextAttribute.FOREGROUND, new java.awt.Color(color));
		return fontAttrib;
	}

	public static Map<String, Object> getDummyFont() throws Exception {
		Map<String, Object> fontAttrib = new HashMap<String, Object>();
		String fontFile = "C:/Windows/Fonts/Arial.ttf";
		File file = new File(fontFile);
		if (!file.exists())
			fontFile = "";
		Font font = deriveFont(fontFile, 1.0f, java.awt.Color.RED.getRGB());
		font.deriveFont(20.0f);
		fontAttrib.put("SelectedFontFile", fontFile);
		fontAttrib.put("Font", font);
		fontAttrib.put("BaseFontName", font.getFontName());
		fontAttrib.put("FontSize", new Float(30.0f));
		fontAttrib.put("FontName", font.getName());
		fontAttrib.put("FontWeight", 400);
		fontAttrib.put("FontFamily", font.getFamily());
		fontAttrib.put("FillColor", java.awt.Color.RED.getRGB());
		return fontAttrib;
	}

	public static Point2D.Double getStartPts(Graphics2D g2d, Font font,
			String vdpText, String hallign, String vallign, double panelWt,
			double panelHt) {
		java.awt.FontMetrics fm = g2d.getFontMetrics(font);
		Rectangle2D textRect = fm.getStringBounds(vdpText, g2d);
		double textWidth = textRect.getWidth();
		double startx = 0.0, starty = 0.0;
		if (hallign.equals("LEFT"))
			startx = 0.0;
		else if (hallign.equals("CENTER"))
			startx = (panelWt - textWidth) / 2;
		else if (hallign.equals("RIGHT"))
			startx = (panelWt - textWidth);
		if (vallign.equals("TOP"))
			starty = 0;
		else if (vallign.equals("BOTTOM"))
			starty = panelHt;
		else if (vallign.equals("CENTER"))
			starty = panelHt / 2;
		return new Point2D.Double(startx, starty);
	}

	public static Point2D.Double getStartPts(Graphics2D g2d, Font font,
			String vdpText, String hallign, String vallign, Polygon poly) {
		Rectangle bounds = poly.getBounds();
		int[] xPts = poly.xpoints, yPts = poly.ypoints;
		int minXPt = xPts[0], minYPt = yPts[0], maxXPt = xPts[0], maxYPt = yPts[0];
		// System.out.println("Pt: 0 x: " + xPts[0] + " y: " + yPts[0]);
		for (int i = 1; i < poly.npoints; i++) {
			// System.out.println("Pt: " + i + " x: " + xPts[i] + " y: " +
			// yPts[i]);
			if (xPts[i] < minXPt)
				minXPt = xPts[i];
			if (yPts[i] < minYPt)
				minYPt = yPts[i];
			if (xPts[i] > maxXPt)
				maxXPt = xPts[i];
			if (yPts[i] > maxYPt)
				maxYPt = yPts[i];
		}
		double panelWt = Math.abs(xPts[1] - xPts[0]);
		double panelHt = Math.abs(yPts[2] - yPts[1]);
		System.out.println("Poly Wt: " + panelWt + " Poly Ht: " + panelHt);
		java.awt.FontMetrics fm = g2d.getFontMetrics(font);
		Rectangle2D textRect = fm.getStringBounds(vdpText, g2d);
		double textWidth = textRect.getWidth();
		double startx = 0.0, starty = 0.0;
		if (xPts[0] == xPts[3] && yPts[0] + panelHt == yPts[3]) {
			startx = 0.0;
			starty = 0.0;
			System.out.println("Case 1: x: " + startx + " y: " + starty);
			return new Point2D.Double(startx, starty);
		}
		if (xPts[0] > xPts[3] && yPts[0] < yPts[3]) {
			startx = 0.0;
			starty = yPts[3] - maxYPt;
			System.out.println("Case 2: x: " + startx + " y: " + starty);
			return new Point2D.Double(startx, starty);
		}
		System.out.println("No Case Satisfied: x: " + startx + " y: " + starty);
		return new Point2D.Double(startx, starty);
	}

	// FUNCTION TO CREATE BUTTON

	// This function is called to set only the image icon
	public static JButton createImageButton(String imageFile,
			String toolTipText, Dimension size, RGPTActionListener listener,
			String action, boolean setBorder) {
		// This calls the createButton function without Rollover Image
		return createButton(imageFile, "", "", toolTipText, size, listener,
				action, setBorder);
	}

	public static JButton createButton(String imageFile, String name,
			String rollOverImage, String toolTipText, Dimension size,
			RGPTActionListener listener, String action, boolean setBorder) {
		// Create and initialize the button.
		JButton button = new JButton();
		setToolTip(button, toolTipText);
		if (size != null)
			button.setSize(size);
		ImageIcon imgIcon = getImageIcon(imageFile);
		ImageIcon rolloverIcon = getImageIcon(rollOverImage);
		if (imgIcon != null) {
			if (name != null && name.length() > 0) {
				button.setLayout(new BoxLayout(button, BoxLayout.Y_AXIS));
				JLabel imglabel = createLabel(imgIcon);
				button.add(imglabel);
				int wt = imgIcon.getIconWidth();
				int ht = AppletParameters.getIntVal("ButtonLabelHeight");
				if (size != null)
					wt = size.width;
				JLabel label = createLabel(name, wt, ht, "ButtonLabelFontSize");
				button.add(label);
			} else {
				if (setBorder) {
					setBorder(button);
					// Border raisedBorder =
					// BorderFactory.createRaisedBevelBorder();
					// button.setBorder(raisedBorder);
				}
				button.setIcon(imgIcon);
			}
			if (rolloverIcon != null) {
				button.setRolloverEnabled(true);
				button.setRolloverIcon(rolloverIcon);
				button.setPressedIcon(rolloverIcon);
			}
		} else
			button.setName(name);
		if (action != null && action.length() > 0)
			button.setActionCommand("Action=" + action);
		if (listener != null) {
			button.addActionListener(listener);
			button.addMouseListener(listener);
		}
		button.setEnabled(false);
		return button;
	}

	// This function is used instead of setMnemonic using VK_XXX keycodes. So
	// essentially
	// for non VK_XXX keycodes. If the certain key combination is pressed when a
	// particular
	// Content Pane is active, then the MnemonicKeyListener is called which in
	// turn calls the
	// RGPTActionListener
	public static void setMnemonicKey(JPanel contentPane,
			RGPTActionListener listener, String action) {
		if (action == null || listener == null)
			return;
		String mnemonicKey = listener.getMnemonicKey(action.toString());
		if (mnemonicKey == null && mnemonicKey.length() == 0)
			return;
		// RGPTLogger.logToConsole("mnemonicKey: "+mnemonicKey);
		String mnemonicName = String.valueOf(action.toString());
		MnemonicKeyListener mnemonicKeyListener = new MnemonicKeyListener(
				action.toString(), mnemonicName, listener);
		contentPane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(mnemonicKey), mnemonicName);
		contentPane.getActionMap().put(mnemonicName, mnemonicKeyListener);
	}

	public static JButton createActionButton(String text, int wt, int ht,
			String toolTipText, RGPTActionListener listener, String action) {
		JButton button = new JButton();
		setToolTip(button, toolTipText);
		setCompSize(button, wt, ht, 1);
		button.setMargin(new Insets(2, 2, 2, 2));
		button.setLayout(new BorderLayout());
		JLabel label = createLabel(text, wt, ht, "ButtonLabelFontSize");
		button.add(label, BorderLayout.CENTER);
		if (action != null && action.length() > 0)
			button.setActionCommand("WFAction=" + action);
		if (listener != null)
			button.addActionListener(listener);
		button.setEnabled(true);
		return button;
	}

	public static JComboBox createComboBox(RGPTListCellRenderer cellRen,
			int wt, int ht, String newItem, String selItem,
			RGPTActionListener listener, String action) {
		JComboBox comboBox = null;
		if (cellRen != null && cellRen.m_Labels != null) {
			comboBox = new JComboBox(cellRen.m_Labels);
			comboBox.setRenderer(cellRen);
		} else
			comboBox = new JComboBox();

		comboBox.setPreferredSize(new Dimension(wt, ht));
		if (newItem != null && newItem.length() > 0) {
			// Add an item to the start of the list
			comboBox.insertItemAt(newItem, 0);
		}
		if (selItem != null && selItem.length() > 0)
			comboBox.setSelectedItem(selItem);
		else if (comboBox.getItemAt(0) != null)
			comboBox.setSelectedIndex(0);
		Object popup = comboBox.getUI().getAccessibleChild(comboBox, 0);
		Component c = ((Container) popup).getComponent(0);
		RGPTLogger.logToFile("Component Type: " + c.getClass().getName());
		if (c instanceof JScrollPane) {
			JScrollPane scrollpane = (JScrollPane) c;
			JScrollBar scrollBar = scrollpane.getVerticalScrollBar();
			Dimension scrollBarDim = new Dimension(20,
					scrollBar.getPreferredSize().height);
			setCompSize(scrollBar, 10, scrollBar.getPreferredSize().height, 1);
			setCompSize(scrollpane, 10, scrollpane.getPreferredSize().height, 1);
			RGPTLogger.logToFile("Combo ScrollBar Size: "
					+ scrollBar.getPreferredSize());
			RGPTLogger.logToFile("Combo ScrollPane Size: "
					+ scrollpane.getPreferredSize());
		}
		if (action != null && action.length() > 0)
			comboBox.setActionCommand("Action=" + action);
		if (listener != null) {
			comboBox.addActionListener(listener);
			comboBox.addMouseListener(listener);
		}
		setBorder(comboBox, LOWERED_BORDER);
		return comboBox;
	}

	public static void setToolTip(JComponent comp, String tip) {
		if (tip == null || tip.length() == 0)
			return;
		int fontSize = AppletParameters.getIntVal("ToolTipFontSize");
		String htmlToolTipText = getHTMLTextForComp(tip, fontSize);
		comp.setToolTipText(htmlToolTipText);
	}

	public static JTextField createTextField(String txt, String tip, int nCols,
			int wt, int ht, int allign, int borderType, boolean setInset,
			RGPTActionListener listener, String action) {
		int fontSize = AppletParameters.getIntVal("TextFieldFontSize");
		JTextField txtFld = new JTextField(txt, nCols);
		Font font = txtFld.getFont();
		font = font.deriveFont((float) fontSize);
		font = font.deriveFont(Font.BOLD);
		txtFld.setFont(font);
		if (setInset)
			txtFld.setMargin(new Insets(1, 10, 1, 1));
		if (allign == -1)
			allign = JTextField.LEFT;
		txtFld.setHorizontalAlignment(allign);
		txtFld.setPreferredSize(new Dimension(wt, ht));
		setToolTip(txtFld, tip);
		if (borderType != -1)
			setBorder(txtFld, borderType);
		if (action != null && action.length() > 0)
			txtFld.setActionCommand("Action=" + action);
		if (listener != null) {
			txtFld.addActionListener(listener);
			txtFld.addMouseListener(listener);
			txtFld.addKeyListener(listener);
		}
		return txtFld;
	}

	public static JMenuItem createMenuItem(String imageFile, String name,
			String toolTip, Dimension size, RGPTActionListener listener,
			String action) {
		// Create and initialize the button.
		JMenuItem menuItem = new JMenuItem();
		RGPTLogger.logToFile("Create MenuItem for: " + name);
		if (size != null)
			menuItem.setPreferredSize(size);
		setToolTip(menuItem, toolTip);
		ImageIcon imgIcon = null;
		if (imageFile != null)
			imgIcon = getImageIcon(imageFile);
		if (imgIcon != null) {
			if (name != null && name.length() > 0) {
				menuItem.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));
				JLabel imglabel = createLabel(imgIcon);
				menuItem.add(imglabel);
				int margin = 20;
				int wt = size.width - imgIcon.getIconWidth() - margin;
				int ht = size.height;
				if (size != null)
					wt = size.width;
				JLabel label = createLabel(name, wt, ht, "ButtonLabelFontSize");
				menuItem.add(label);
			} else {
				menuItem.setIcon(imgIcon);
			}
		} else {
			menuItem.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
			JLabel label = createLabel(name, size.width, size.height,
					"ButtonLabelFontSize");
			menuItem.add(label);
		}
		setBorder(menuItem, LOWERED_BORDER);
		if (action != null && action.length() > 0)
			menuItem.setActionCommand("Action=" + action);
		if (listener != null)
			menuItem.addActionListener(listener);
		return menuItem;
	}

	public static Cursor createCustomCursor(String cursorImg, String cursorName) {
		// Get the default toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		// Load an image for the cursor
		Image image = (getImageIcon(cursorImg)).getImage();
		if (image == null)
			return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		// Create the hotspot for the cursor
		Point hotSpot = new Point(0, 0);
		// Create the custom cursor
		return toolkit.createCustomCursor(image, hotSpot, cursorName);
	}

	public static final int RAISED_BORDER = 0;
	public static final int LOWERED_BORDER = 1;
	public static final int LINE_BORDER = 2;

	public static void setBorder(JComponent comp) {
		setBorder(comp, RAISED_BORDER);
	}

	public static void setBorder(JComponent comp, int borderType) {
		Border border = BorderFactory.createRaisedBevelBorder();
		if (borderType == LOWERED_BORDER)
			border = BorderFactory.createLoweredBevelBorder();
		else if (borderType == LINE_BORDER)
			border = new LineBorder(Color.BLACK);
		comp.setBorder(border);
	}

	public static void setCompSize(Component comp, int prefWt, int prefHt,
			int maxPanelSizeAdj) {
		if (maxPanelSizeAdj <= 0)
			maxPanelSizeAdj = AppletParameters.getIntVal("MaxPanelSizeAdj");
		Dimension prefSize = new Dimension(prefWt, prefHt);
		comp.setMinimumSize(prefSize);
		comp.setPreferredSize(prefSize);
		comp.setMaximumSize(new Dimension(prefWt + maxPanelSizeAdj, prefHt
				+ maxPanelSizeAdj));
	}

	public static JPanel createEmptyPanel(int wt, int ht) {
		JPanel labelPanel = new JPanel(new BorderLayout());
		setCompSize(labelPanel, wt, ht, -1);
		JLabel label = new JLabel("", JLabel.CENTER);
		labelPanel.add(label, BorderLayout.CENTER);
		setCompSize(labelPanel, wt, ht, 1);
		return labelPanel;
	}

	public static JPanel createImagePanel(BufferedImage image) {
		return createImagePanel(image, image.getWidth(), image.getHeight(),
				true, 0, 0);
	}

	public static JPanel createImagePanel(String imageFile, int panelWt,
			int panelHt) {
		ImageIcon imgIcon = getImageIcon(imageFile);
		return createImagePanel(imgIcon.getImage(), panelWt, panelHt, false, 0,
				0);
	}

	public static JPanel createImagePanel(final Image image, int panelWt,
			int panelHt, final boolean fillPanel, final int x, final int y) {
		JPanel imagePanel = new JPanel() {
			public void paint(Graphics g) {
				Rectangle visibleRect = this.getVisibleRect();
				Dimension panelSize = new Dimension(
						(int) visibleRect.getWidth(),
						(int) visibleRect.getHeight());
				int wt = 0, ht = 0;
				if (fillPanel) {
					wt = panelSize.width;
					ht = panelSize.height;
					g.drawImage(image, x, y, wt, ht, null);
				} else
					g.drawImage(image, x, y, null);
			}
		};
		setCompSize(imagePanel, panelWt, panelHt, 1);
		imagePanel.setBorder(new LineBorder(Color.BLACK));
		return imagePanel;
	}

	public static JPanel createPanel(Component comp, boolean setBorder) {
		return createPanel(comp, setBorder, RAISED_BORDER);
	}

	public static JPanel createPanel(Component comp, boolean setBorder,
			int borderType) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(comp, BorderLayout.CENTER);
		if (setBorder)
			setBorder(panel, borderType);
		// setCompSize(labelPanel, wt, ht, 1);
		return panel;
	}

	public static void resetDisplayComponent(JComponent mainComp,
			JComponent nonDispComp, JComponent newDispComp) {
		JComponent[] nonDispComps = { nonDispComp };
		resetDisplayComponent(mainComp, nonDispComps, newDispComp);
	}

	public static void resetDisplayComponent(JComponent mainComp,
			JComponent[] nonDispComps, JComponent newDispComp) {
		int zorder = -1;
		if (mainComp == null || nonDispComps == null || newDispComp == null)
			return;
		// RGPTLogger.logToFile("Resetting the Display Component");
		for (int i = 0; i < nonDispComps.length; i++) {
			zorder = mainComp.getComponentZOrder(nonDispComps[i]);
			if (zorder != -1)
				mainComp.remove(nonDispComps[i]);
		}
		zorder = mainComp.getComponentZOrder(newDispComp);
		if (zorder == -1)
			mainComp.add(newDispComp, BorderLayout.CENTER);
		mainComp.revalidate();
	}

	public static JLabel createLabel(String imageFile) {
		ImageIcon imgIcon = getImageIcon(imageFile);
		if (imgIcon == null)
			return null;
		return createLabel(imgIcon);
	}

	public static JPanel createLabelPanel(String imageFile, int wt, int ht,
			int borderType) {
		JPanel labelPanel = new JPanel(new BorderLayout());
		RGPTUIUtil.setCompSize(labelPanel, wt, ht, -1);
		JLabel iconlabel = createLabel(imageFile, -1);
		labelPanel.add(iconlabel, BorderLayout.CENTER);
		if (borderType >= 0)
			setBorder(labelPanel, borderType);
		return labelPanel;
	}

	public static JLabel createLabel(String imageFile, int borderType) {
		ImageIcon imgIcon = getImageIcon(imageFile);
		if (imgIcon == null)
			return null;
		JLabel imglabel = new JLabel(imgIcon, SwingConstants.CENTER);
		if (borderType >= 0)
			setBorder(imglabel, borderType);
		return imglabel;
	}

	public static JLabel createLabel(ImageIcon imgIcon) {
		Border raisedBorder = BorderFactory.createRaisedBevelBorder();
		JLabel imglabel = new JLabel(imgIcon, SwingConstants.CENTER);
		imglabel.setBorder(raisedBorder);
		return imglabel;
	}

	public static JPanel createWhiteLabelPanel(String txt, int wt, int ht,
			String fontSzPropName) {
		JPanel labelPanel = createLabelPanel(txt, wt, ht, fontSzPropName);
		// labelPanel.setBackground(Color.WHITE);
		Border raisedBorder = BorderFactory.createRaisedBevelBorder();
		labelPanel.setBorder(raisedBorder);
		return labelPanel;
	}

	public static JPanel createLabelPanel(String txt, int wt, int ht,
			String fontSzPropName) {
		JPanel labelPanel = new JPanel(new BorderLayout());
		setCompSize(labelPanel, wt, ht, -1);
		JLabel label = createLabel(txt, wt, ht, fontSzPropName);
		labelPanel.add(label, BorderLayout.CENTER);
		setCompSize(labelPanel, wt, ht, 1);
		return labelPanel;
	}

	public static JLabel createLabel(String txt, int wt, int ht,
			String fontSzProp) {
		JLabel label = new JLabel("", JLabel.CENTER);
		if (fontSzProp == null || fontSzProp.length() == 0)
			fontSzProp = "LabelFontSize";
		setLabelText(label, txt, wt, fontSzProp);
		label.setSize(new Dimension(wt, ht));
		return label;
	}

	public static JLabel createImageLabel(String imageFile, String name,
			int wt, int ht, String fontSzPropName) {
		// if (imageFile == null)
		// return createLabel(name, wt, ht, fontSzPropName);
		JLabel label = new JLabel("");
		label.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		label.setPreferredSize(new Dimension(wt, ht));
		if (imageFile != null) {
			ImageIcon imgIcon = getImageIcon(imageFile);
			JLabel imglabel = createLabel(imgIcon);
			label.add(imglabel);
		}
		JLabel txtlabel = createLabel(name, wt, ht, fontSzPropName);
		RGPTLogger.logToFile("\nTxt Label: " + txtlabel);
		label.add(txtlabel);
		return label;
	}

	public static void setLabelText(JLabel label, String txt, int wt,
			String fontSzPropName) {
		if (txt == null || txt.length() == 0)
			return;
		txt = RGPTUtil.getTextForWidth((double) wt, txt, label.getFont());
		int fontSize = AppletParameters.getIntVal(fontSzPropName);
		String labelHtmlText = getHTMLTextForComp(txt, fontSize);
		label.setText(labelHtmlText);
	}

	public static String getHTMLTextForComp(String txt, int fontSize) {
		StringBuffer htmlText = new StringBuffer("<html><FONT size=" + fontSize
				+ "><B>");
		htmlText.append(txt);
		htmlText.append("</B></FONT></html>");
		return htmlText.toString();
	}

	public static ImageIcon getImageIcon(String imageFile) {
		if (imageFile == null || imageFile.length() == 0)
			return null;
		ImageIcon imgIcon = null;
		// Loading the image.
		String ext = RGPTFileFilter.getExtension(imageFile);
		// RGPTLogger.logToConsole("Image Name: " + imageFile + " Ext is: " +
		// ext);
		String imgLocation = "res/";
		if (ext != null)
			imgLocation = imgLocation + imageFile;
		else
			imgLocation = imgLocation + imageFile + ".gif";

		// Create and initialize the button.
		boolean useStream = false;
		try {
			imgIcon = new ImageIcon(imgLocation);
			// RGPTLogger.logToConsole("Image Size W: " + imgIcon.getIconWidth()
			// +
			// " H: " + imgIcon.getIconHeight());
			if (imgIcon.getIconWidth() <= 0 || imgIcon.getIconHeight() <= 0)
				useStream = true;
		} catch (Throwable th) {
			useStream = true;
		}
		if (useStream) {
			// RGPTLogger.logToConsole("Loading Image Icon from the jar file");
			// String imgPath = "/res/" + imageFile + ".gif";
			String imgPath = "/" + imgLocation;
			byte[] buf = ImageUtils.loadImage(imgPath,
					(new RGPTUIUtil()).getClass());
			imgIcon = new ImageIcon(Toolkit.getDefaultToolkit()
					.createImage(buf));
		}
		return imgIcon;
	}

	// FUNCTION TO CREATE LABEL

	public static JLabel createLabel(String labelName, String relImgPath,
			String imageName, int allignment, Class caller) {
		ImageIcon imgIcon = null;
		// Loading the image.
		String imgLocation = relImgPath + imageName;
		System.out.println("Load Image Direct: " + imgLocation);

		boolean useStream = false;
		try {
			imgIcon = new ImageIcon(imgLocation);
			System.out.println("Direct ImageIcon : " + imgIcon.toString());
			System.out.println("Direct Image : "
					+ imgIcon.getImage().toString());
			System.out.println("Image Size W: " + imgIcon.getIconWidth()
					+ " H: " + imgIcon.getIconHeight());
			if (imgIcon.getIconWidth() <= 0 || imgIcon.getIconHeight() <= 0)
				useStream = true;
		} catch (Throwable th) {
			useStream = true;
		}
		if (useStream) {
			String imgPath = "/" + AppletParameters.IMAGE_PATH + imageName;
			byte[] buf = ImageUtils.loadImage(imgPath, caller);
			try {
				imgIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit()
						.createImage(buf));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// Create and initialize the JLabel
		JLabel label = new JLabel(labelName, imgIcon, allignment);
		return label;
	}

	public static Vector<String> makeLeftAllignLabelNames(FontMetrics fontMat,
			Vector<String> labelNames) {
		Vector<String> allignedLabelNames = new Vector<String>();
		int maxLenElem = -1, maxLen = -1, labelLen = -1, diffLen = -1, maxStrLen = -1;
		String labelName = "", newlabelName = "", fixedSpeces = "   :", varSpaces = "";
		// This loop determines the Max Lenght Element and Max length
		for (int i = 0; i < labelNames.size(); i++) {
			labelName = labelNames.elementAt(i);
			labelLen = labelName.length();
			if (labelLen < maxLen)
				continue;
			maxLen = labelLen;
			maxLenElem = i;
		}
		labelName = labelNames.elementAt(maxLenElem);
		maxStrLen = fontMat.stringWidth(labelName);
		// Left Allign the Label Names in this loop
		for (int j = 0; j < labelNames.size(); j++) {
			varSpaces = "";
			labelName = labelNames.elementAt(j);
			if (j == maxLenElem) {
				newlabelName = labelName + fixedSpeces;
				allignedLabelNames.addElement(newlabelName);
				continue;
			}
			labelLen = fontMat.stringWidth(labelName);
			while (labelLen <= maxStrLen) {
				labelName = labelName + " ";
				labelLen = fontMat.stringWidth(labelName);
			}
			newlabelName = labelName + fixedSpeces;
			allignedLabelNames.addElement(newlabelName);
		}
		return allignedLabelNames;
	}

	public static Vector<Rectangle> drawCornerPoints(Graphics2D g2d,
			Rectangle disprect) {
		Vector<Rectangle> rectPt = new Vector<Rectangle>();
		Rectangle2D rect = RGPTUtil.getRectangle2D(disprect);
		g2d.setPaint(Color.RED);
		BasicStroke basicStroke = new BasicStroke(2.0f);
		double size = 6.0;
		Rectangle2D.Double ptRect = new Rectangle2D.Double();
		ptRect.setRect(rect.getX() - (size / 2), rect.getY() - (size / 2),
				size, size);
		rectPt.addElement(RGPTUtil.getRectangle(ptRect));
		g2d.draw(ptRect);
		g2d.fill(ptRect);
		ptRect.setRect(rect.getX() - (size / 2), rect.getY() + rect.getHeight()
				- (size / 2), size, size);
		rectPt.addElement(RGPTUtil.getRectangle(ptRect));
		g2d.draw(ptRect);
		g2d.fill(ptRect);
		ptRect.setRect(rect.getX() + rect.getWidth() - (size / 2), rect.getY()
				- (size / 2), size, size);
		rectPt.addElement(RGPTUtil.getRectangle(ptRect));
		g2d.draw(ptRect);
		g2d.fill(ptRect);
		ptRect.setRect(rect.getX() + rect.getWidth() - (size / 2), rect.getY()
				+ rect.getHeight() - (size / 2), size, size);
		rectPt.addElement(RGPTUtil.getRectangle(ptRect));
		g2d.draw(ptRect);
		g2d.fill(ptRect);
		return rectPt;
	}

	public static Vector<Rectangle> drawCornerPoints(Graphics2D g2d,
			Polygon poly) {
		Vector<Rectangle> rectPt = new Vector<Rectangle>();
		int[] xPts = poly.xpoints, yPts = poly.ypoints;
		g2d.setPaint(Color.RED);
		BasicStroke basicStroke = new BasicStroke(2.0f);
		int size = 6;
		for (int i = 0; i < poly.npoints; i++) {
			Rectangle ptRect = new Rectangle(xPts[i] - size / 2, yPts[i] - size
					/ 2, size, size);
			g2d.draw(ptRect);
			g2d.fill(ptRect);
			rectPt.addElement(ptRect);
		}
		return rectPt;
	}

	public static void setGradiantPaint(Graphics2D g2d, Point2D pt1, Color c1,
			Point2D pt2, Color c2) {
		GradientPaint gp = new GradientPaint(pt1, c1, pt2, c2);
		g2d.setPaint(gp);
	}

	// FUNCTION TO CREATE MENU ITEM

	public static JPopupMenu createPopupMenu(Dimension meniItemSz,
			String propName, RGPTActionListener listener) {
		return createPopupMenu(null, meniItemSz, 0, 0, propName, listener,
				false);
	}

	// If handleMenuShow is set to true then the ActionListener is set for the
	// Button to
	// Show MenuItem. If false then the program that called this API is handling
	// the
	// capability to show the Menu Item. The Button Object can be null in this
	// case.
	public static JPopupMenu createPopupMenu(final JButton button,
			Dimension meniItemSz, final int menuXPos, final int menuYPos,
			String propName, RGPTActionListener listener, boolean handleMenuShow) {

		LocalizationUtil lu = new LocalizationUtil();
		JMenuItem menuItem = null;
		final JPopupMenu popupMenu = new JPopupMenu(propName);

		String menuData = AppletParameters.getVal(propName);
		RGPTLogger.logToFile("menuData: " + menuData);
		String[] itemDataList = menuData.split(":NL:");
		for (int iitem = 0; iitem < itemDataList.length; iitem++) {
			RGPTLogger.logToFile("menuItemData: " + itemDataList[iitem]);
			String[] itemInfo = itemDataList[iitem].split("::");
			String itemName = lu.getText(itemInfo[0]), action = itemInfo[1];
			String tip = lu.getText("ToolTip_" + itemInfo[0]);
			menuItem = createMenuItem(null, itemName, tip, meniItemSz,
					listener, action);
			popupMenu.add(menuItem);
		}

		if (button != null && handleMenuShow) {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evnt) {
					popupMenu.show(button, menuXPos, menuYPos);
				}
			});
		}
		RGPTLogger.logToFile("popupMenu: " + popupMenu);
		return popupMenu;
	}

	public static Paint getGradientColor(int ht, Color color1, Color color2) {
		if (color2 == null)
			color2 = color1.darker();
		GradientPaint gp = new GradientPaint(0, ht / 2, color2, ht / 2, ht,
				color1);
		return gp;
	}

	// FUNCTION to DISPLAY Image Filter Options

	public static JPanel createImageFilterPanel(String imgFilterAction,
			int panelWt, int vgap, Map<String, JComponent> actionComp,
			RGPTActionListener listener) {
		String imgFile = null, tip = null, action = null, filterAction = null;
		String imgFilter = null, imgFilterName = null, imgFilterContent = null;
		String[] imgFilterOptions = null, imgFilterParam = null;
		String[] imgFilterSpec = null, toolBarBtns = null;
		LocalizationUtil lu = new LocalizationUtil();
		int labelHt = AppletParameters.getIntVal("LabelHeight");
		int emptyPanelHt = AppletParameters.getIntVal("EmptyPanelHeight");
		JPanel mainImgFilterPanel = new JPanel(), emptyPanel = null;
		JPanel imgFilterPanel = null, imgFilterParamPanel = null;
		// mainImgFilterPanel.setLayout(new BoxLayout(mainImgFilterPanel,
		// BoxLayout.Y_AXIS));
		imgFilterContent = AppletParameters.getVal(imgFilterAction);
		RGPTLogger.logToFile("Image Filters: " + imgFilterContent);
		imgFilterOptions = imgFilterContent.split(":NF:");
		mainImgFilterPanel.setLayout(new BasicGridLayout(
				imgFilterOptions.length, 1, 2, vgap));
		Color panelColor = new Color(240, 240, 240); // RGPTUIManager.PANEL_COLOR;
		for (int imgFilterIter = 0; imgFilterIter < imgFilterOptions.length; imgFilterIter++) {
			// emptyPanel = createEmptyPanel(panelWt, 5);
			// mainImgFilterPanel.add(emptyPanel);
			imgFilterParam = imgFilterOptions[imgFilterIter].split(":NP:");
			RGPTLogger.logToFile("Image Filters Param Length: "
					+ imgFilterParam.length);
			imgFilterPanel = new JPanel();
			imgFilterPanel.setBackground(panelColor);
			// imgFilterPanel.setLayout(new
			// BasicGridLayout(imgFilterParam.length, 1, 5, 1));
			imgFilterPanel.setLayout(new BoxLayout(imgFilterPanel,
					BoxLayout.Y_AXIS));
			for (int paramIter = 0; paramIter < imgFilterParam.length; paramIter++) {
				imgFilterParamPanel = new JPanel(new BorderLayout());
				imgFilterParamPanel.setBackground(panelColor);
				setCompSize(imgFilterParamPanel, panelWt, 54, 0);
				RGPTLogger.logToFile("Image Filters Params: "
						+ imgFilterParam[paramIter]);
				imgFilterSpec = imgFilterParam[paramIter].split("::");
				action = imgFilterSpec[0];
				toolBarBtns = imgFilterSpec[1].split(":");
				imgFilterName = lu.getText(action);
				tip = lu.getText("ToolTip_" + action);
				JLabel imgFilterLabel = createLabel(imgFilterName, panelWt,
						labelHt, null);
				setToolTip(imgFilterLabel, tip);
				actionComp.put(action, imgFilterLabel);
				imgFilterLabel.setHorizontalAlignment(JLabel.CENTER);
				listener.setComponentData(action, "", imgFilterLabel);
				imgFilterParamPanel.add(imgFilterLabel, BorderLayout.NORTH);
				JPanel toolBarPanel = new JPanel(new FlowLayout(
						FlowLayout.CENTER, 0, 0));
				// JPanel toolBarPanel = new JPanel(new BorderLayout());
				JToolBar toolbar = new JToolBar(action, JToolBar.HORIZONTAL);
				// toolbar.setMargin(new Insets(0, 0, 0, 0));
				toolbar.setBorder(new EmptyBorder(5, 0, 4, 0));
				// setCompSize(toolbar, panelWt, 40, 0);
				toolbar.setMaximumSize(new Dimension(panelWt, 40));
				toolbar.setFloatable(false);
				// ContrastFilter::Inc:Dec:Def:Cancel
				for (int btnIter = 0; btnIter < toolBarBtns.length; btnIter++) {
					String btnInfo = toolBarBtns[btnIter];
					filterAction = action + "_" + btnInfo;
					if (btnInfo.startsWith("Slider")) {
						String[] sliderInfo = btnInfo.split("-v=");
						filterAction = action + "_" + sliderInfo[0];
						tip = lu.getText("ToolTip_" + filterAction);
						int min = 0, max = 10, def = 0;
						if (sliderInfo.length > 1) {
							min = Integer.valueOf(sliderInfo[1]);
							max = Integer.valueOf(sliderInfo[2]);
							def = Integer.valueOf(sliderInfo[3]);
						}
						setCompSize(imgFilterParamPanel, panelWt, 54, 0);
						JPanel sliderPanel = new JPanel(new FlowLayout(
								FlowLayout.RIGHT, 0, 0));
						JSlider slider = new JSlider();
						sliderPanel.add(slider);
						slider.setBorder(new EmptyBorder(0, 5, 0, 0));
						setCompSize(slider, 90, 24, -1);
						setCompSize(sliderPanel, 90, 28, -1);
						slider.setAlignmentX(JSlider.CENTER_ALIGNMENT);
						slider.setEnabled(true);
						slider.setBackground(RGPTUIManager.PANEL_COLOR);
						slider.addMouseListener(listener);
						toolbar.add(slider);
						slider.setMinimum(min);
						slider.setMaximum(max);
						slider.setValue(def);
						actionComp.put(filterAction, slider);
						listener.setComponentData(filterAction, action, slider);
					} else if (btnInfo.startsWith("TextBox")) {
						String[] txtInfo = btnInfo.split("-v=");
						filterAction = action + "_" + txtInfo[0];
						tip = lu.getText("ToolTip_" + filterAction);
						String val = "0";
						if (txtInfo.length > 1)
							val = txtInfo[1];
						int fldCol = 2, fldWt = 30, fldHt = 28, allign = JTextField.CENTER;
						JTextField fldBox = createTextField(val, tip, fldCol,
								fldWt, fldHt, allign, LINE_BORDER, false,
								listener, filterAction);
						setCompSize(fldBox, fldWt, fldHt, -1);
						toolbar.add(fldBox);
						actionComp.put(filterAction, fldBox);
						listener.setComponentData(filterAction, action, fldBox);
					} else {
						tip = lu.getText("ToolTip_" + filterAction);
						imgFile = "rgpticons/" + btnInfo + ".gif";
						JButton button = createImageButton(imgFile, tip, null,
								listener, filterAction, false);
						setBorder(button);
						button.setEnabled(true);
						button.setBorder(new EmptyBorder(0, 5, 0, 0));
						button.setBackground(RGPTUIManager.PANEL_COLOR);
						actionComp.put(filterAction, button);
						toolbar.add(button);
						listener.setComponentData(filterAction, action, button);
					}
				}
				toolBarPanel.add(toolbar, BorderLayout.CENTER);
				imgFilterParamPanel.add(toolBarPanel, BorderLayout.CENTER);
				imgFilterPanel.add(imgFilterParamPanel);
			}
			// imgFilterPanel.setBorder(new LineBorder(Color.BLACK));
			setBorder(imgFilterPanel, RGPTUIUtil.LOWERED_BORDER);
			mainImgFilterPanel.add(imgFilterPanel);
		}
		return mainImgFilterPanel;
	}

	// FUNCTION TO DISPLAY Toolbar

	public static JToolBar createToolBar(String propName, int orientation,
			boolean isFloatable, RGPTActionListener listener,
			Map<String, JComponent> actionComp) {
		LocalizationUtil lu = new LocalizationUtil();
		JButton button = null;
		JSlider slider = null;
		Dimension size = null;
		String uiComp = null, imgFile = null, uiLabel = null, action = null, txt = null, tip = null, content = null;
		JToolBar toolbar = new JToolBar(propName, orientation);
		size = RGPTUtil.getCompSize(propName + "_Size");
		setCompSize(toolbar, size.width, size.height, 0);
		setBorder(toolbar, RGPTUIUtil.LOWERED_BORDER);
		toolbar.setFloatable(isFloatable);
		content = AppletParameters.getVal(propName);
		RGPTLogger.logToFile("cont: " + content);
		String[] contentPara = content.split(":NP:"), uiInfo = null;
		for (int ipara = 0; ipara < contentPara.length; ipara++) {
			String[] contentLine = contentPara[ipara].split(":NL:");
			for (int iline = 0; iline < contentLine.length; iline++) {
				RGPTLogger.logToFile("Line Content: " + contentLine[iline]);
				uiInfo = contentLine[iline].split("::");
				uiComp = uiInfo[0];
				imgFile = "rgpticons/" + uiInfo[1];
				uiLabel = uiInfo[2];
				action = uiInfo[3];
				txt = lu.getText(uiLabel);
				tip = lu.getText("ToolTip_" + uiLabel);
				// UI Component can be COMBO_BOX, TEXT_FIELD, CHECK_BOX,
				// RADIO_BUTTON,
				// ACTION_BUTTON, FILE_CHOOSER
				if (uiComp.equals("BUTTON")) {
					button = createImageButton(imgFile, tip, null, listener,
							action, false);
					button.setBorder(new EmptyBorder(3, 0, 3, 5));
					button.setBackground(RGPTUIManager.PANEL_COLOR);
					// button.setModel(new DefaultButtonModel() {
					// public void setPressed(boolean b) { super.setPressed(b);
					// }
					// });
					toolbar.add(button);
					actionComp.put(action, button);
					listener.setComponentData(action, uiLabel, button);
					RGPTLogger.logToFile("Create Button with Settings: "
							+ uiComp + " uiLabel: " + uiLabel + " action: "
							+ action);
				} else if (uiComp.equals("SLIDER")) {
					JPanel sliderPanel = new JPanel(new FlowLayout(
							FlowLayout.RIGHT, 0, 0));
					slider = new JSlider();
					sliderPanel.add(slider);
					slider.setBorder(new EmptyBorder(0, 5, 0, 0));
					setCompSize(slider, 24, 100, -1);
					setCompSize(sliderPanel, 32, 100, 5);
					slider.setValue(0);
					slider.setAlignmentX(JSlider.CENTER_ALIGNMENT);
					slider.setEnabled(false);
					slider.setBackground(RGPTUIManager.PANEL_COLOR);
					slider.addMouseMotionListener(listener);
					toolbar.add(sliderPanel);
					actionComp.put(action, slider);
					RGPTLogger.logToFile("Create Slider with Settings: "
							+ uiComp + " uiLabel: " + uiLabel + " action: "
							+ action);
					listener.setComponentData(action, uiLabel, slider);
				}
			}
			toolbar.addSeparator();
		}
		return toolbar;
	}

	// FUNCTION TO DISPLAY DIALOG

	public static Map<String, JComponent> createDialogContent(
			JPanel contentMainPane, String basePropName,
			RGPTActionListener listener) {
		// UI Components
		JComboBox comboBox = null;
		JTextField fldBox = null;

		// Component Sizes
		int comboWt = AppletParameters.getIntVal("ComboWidth");
		int comboHt = AppletParameters.getIntVal("ComboHeight");
		int fldCols = AppletParameters.getIntVal("TextFieldCols");
		int fldWt = AppletParameters.getIntVal("TextFieldWidth");
		int fldHt = AppletParameters.getIntVal("TextFieldHeight");
		Dimension contentPaneSize = RGPTUtil
				.getCompSize((basePropName + "_SIZE"));
		setCompSize(contentMainPane, contentPaneSize.width,
				contentPaneSize.height, 1);

		// Initializing Objects
		LocalizationUtil lu = new LocalizationUtil();
		Map<String, JComponent> actionComp = new HashMap<String, JComponent>();
		contentMainPane.setLayout(new BorderLayout());

		// Adding Content Pane to center and the Dialog Action Button is added
		// to South
		boolean createNewLine = false;
		String txt = "", tip = "";
		Integer newLine = ParagraphLayout.NEW_LINE;
		Integer newPara = ParagraphLayout.NEW_PARAGRAPH;
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new ParagraphLayout(20, 20, 12, 11, 4, 4));
		String contentData = AppletParameters.getVal(basePropName + "_CONTENT");
		RGPTLogger.logToFile("contentData: " + contentData);
		String[] contentPara = contentData.split(":NP:");
		for (int ipara = 0; ipara < contentPara.length; ipara++) {
			String[] lineContents = contentPara[ipara].split(":=");
			String[] contentLine = lineContents[1].split(":NL:");
			String label = lineContents[0];
			contentPane.add(new JLabel(lu.getText(label), SwingConstants.LEFT),
					newPara);
			for (int iline = 0; iline < contentLine.length; iline++) {
				RGPTLogger.logToFile("Label: " + label + " Line Content: "
						+ contentLine[iline]);
				String[] contentUI = contentLine[iline].split(":UI:");
				if (iline != 0)
					createNewLine = true;
				for (int iui = 0; iui < contentUI.length; iui++) {
					String[] uiInfo = contentUI[iui].split("::");
					String uiComp = uiInfo[0], uiLabel = uiInfo[1], action = uiInfo[2];
					txt = lu.getText(uiLabel);
					tip = lu.getText("ToolTip_" + uiLabel);
					// UI Component can be COMBO_BOX, TEXT_FIELD, CHECK_BOX,
					// RADIO_BUTTON,
					// ACTION_BUTTON, FILE_CHOOSER
					if (uiComp.equals("COMBO_BOX")) {
						comboBox = createComboBox(null, comboWt, comboHt, "",
								"", listener, action);
						if (createNewLine)
							contentPane.add(comboBox, newLine);
						else
							contentPane.add(comboBox);
						createNewLine = false;
						actionComp.put(action, comboBox);
						RGPTLogger.logToFile("Create Combo with Settings: "
								+ uiComp + " uiLabel: " + uiLabel + " action: "
								+ action);
					} else if (uiComp.equals("TEXT_FIELD")) {
						fldBox = createTextField(txt, tip, fldCols, fldWt,
								fldHt, -1, LOWERED_BORDER, true, listener,
								action);
						if (createNewLine)
							contentPane.add(fldBox, newLine);
						else
							contentPane.add(fldBox);
						createNewLine = false;
						actionComp.put(action, fldBox);
						RGPTLogger.logToFile("Create TextFld with Settings: "
								+ uiComp + " uiLabel: " + uiLabel + " action: "
								+ action);
					}
				}
			}
		}
		int emptyPanelHt = AppletParameters.getIntVal("EmptyPanelHeight");
		JPanel emptyPanel = RGPTUIUtil.createEmptyPanel(
				contentMainPane.getPreferredSize().width, emptyPanelHt);
		contentMainPane.add(emptyPanel, BorderLayout.NORTH);
		contentMainPane.add(contentPane, BorderLayout.CENTER);

		int wt = AppletParameters.getIntVal("DialogButtonWidth");
		int ht = AppletParameters.getIntVal("DialogButtonHeight");
		int vGap = 10, hGap = 25;
		JPanel actionPane = new JPanel(new FlowLayout(FlowLayout.CENTER, hGap,
				vGap));
		actionPane.setBackground(RGPTUIManager.BG_COLOR);
		String actionData = AppletParameters.getVal(basePropName + "_SUBMIT");
		RGPTLogger.logToFile("actionData: " + actionData);
		String[] actionInfo = actionData.split(":UI:");
		for (int iact = 0; iact < actionInfo.length; iact++) {
			String[] uiInfo = actionInfo[iact].split("::");
			String uiLabel = uiInfo[0], action = uiInfo[1];
			txt = lu.getText(uiLabel);
			tip = lu.getText("ToolTip_" + uiLabel);
			JButton button = createActionButton(txt, wt, ht, tip, listener,
					action);
			actionPane.add(button);
			actionComp.put(action, button);
		}

		contentMainPane.add(actionPane, BorderLayout.SOUTH);
		return actionComp;
	}

	// owner and title can be null,
	// Supports only Modal or Modalless Dialog
	// If disposeOnClose is set to true, then the Dialog box is set to disposed
	// else it is set to hide for reuse.
	public static JDialog createDialogBox(Window owner, String title,
			boolean modal, JPanel contentPane, int wt, int ht, int locx,
			int locy, boolean disposeOnClose) {
		Dialog.ModalityType modalType = Dialog.ModalityType.MODELESS;
		if (modal)
			modalType = Dialog.ModalityType.APPLICATION_MODAL;
		int closeOpern = JDialog.HIDE_ON_CLOSE;
		if (disposeOnClose)
			closeOpern = JDialog.DISPOSE_ON_CLOSE;
		JDialog dialog = new JDialog(owner, title, modalType);
		dialog.setSize(wt, ht);
		dialog.setLocation(locx, locy);
		dialog.getContentPane().add(contentPane);
		dialog.setDefaultCloseOperation(closeOpern);
		return dialog;
	}

	public static boolean getUserApproval(Component comp, String propName) {
		int res = showConfirmMesg(comp, null, propName);
		if (res == JOptionPane.YES_OPTION)
			return true;
		// Map<String, String> valMap =
		// RGPTUtil.getPropertyValues("AutoConfirmValues");
		// String[] values = valMap.values().toArray(new String[0]);
		// String value = showInputDialog(comp, propName, values);
		// String userSel = RGPTUtil.containsValue(valMap, value);
		// if (userSel == null || userSel.equals("NO")) return false;
		return false;
	}

	public static String showInputDialog(Component comp, String propName,
			String[] possibleValues) {
		return showInputDialog(comp, null, propName, possibleValues);
	}

	public static String showInputDialog(Component comp, Image icon,
			String propName, String[] possibleValues) {
		LocalizationUtil lu = new LocalizationUtil();
		String[] err = lu.getText(propName).split("::");
		return showInputDialog(comp, err[1], err[0], icon, possibleValues,
				possibleValues[0]);
	}

	public static String showInputDialog(Component comp, String mesg,
			String title, String[] possibleValues, String defaultVal) {
		return showInputDialog(comp, mesg, title, null, possibleValues,
				defaultVal);
	}

	public static String showInputDialog(Component comp, String mesg,
			String title, Image icon, String[] possibleValues, String defaultVal) {
		int mesgType = JOptionPane.INFORMATION_MESSAGE;
		Object selectedValue = null;
		selectedValue = JOptionPane.showInputDialog(comp, mesg, title,
				mesgType, null, possibleValues, defaultVal);
		return (String) selectedValue;
	}

	// public static void showError(Component comp, String propName)
	// {
	// showError(comp, null, propName);
	// }

	public static int showConfirmMesg(Component comp, BufferedImage icon,
			String propName) {
		LocalizationUtil lu = new LocalizationUtil();
		int opt = JOptionPane.YES_NO_OPTION, mesgType = JOptionPane.INFORMATION_MESSAGE;
		String infoMesg = lu.getText(propName);
		if (infoMesg.length() == 0)
			return 1;
		String[] errorInfo = infoMesg.split("::");
		ImageIcon imgIcon = null;
		if (icon != null)
			imgIcon = new ImageIcon(ImageUtils.getImageStream(icon, "gif"));
		return JOptionPane.showConfirmDialog(comp, errorInfo[1], errorInfo[0],
				opt, mesgType, imgIcon);
	}

	public static void showErrorMesg(Component comp, BufferedImage icon,
			String propName) {
		LocalizationUtil lu = new LocalizationUtil();
		String[] errorInfo = lu.getText(propName).split("::");
		ImageIcon imgIcon = null;
		if (icon != null)
			imgIcon = new ImageIcon(ImageUtils.getImageStream(icon, "gif"));
		JOptionPane.showMessageDialog(comp, errorInfo[1], errorInfo[0],
				JOptionPane.ERROR_MESSAGE, imgIcon);
	}

	public static void showInfoMesg(Component comp, BufferedImage icon,
			String propName) {
		LocalizationUtil lu = new LocalizationUtil();
		String infoMesg = lu.getText(propName);
		if (infoMesg.length() == 0)
			return;
		String[] errorInfo = infoMesg.split("::");
		ImageIcon imgIcon = null;
		if (icon != null)
			imgIcon = new ImageIcon(ImageUtils.getImageStream(icon, "gif"));
		JOptionPane.showMessageDialog(comp, errorInfo[1], errorInfo[0],
				JOptionPane.INFORMATION_MESSAGE, imgIcon);
	}

	public static void showError(Component comp, String mesg, String title) {
		JOptionPane.showMessageDialog(comp, mesg, title,
				JOptionPane.ERROR_MESSAGE, null);
	}

	// FUNCTION TO CREATE IMAGE HOLDER

	public static ImageHolder getImageFile(Component comp) {
		Vector<ImageHolder> imgHldrs = getImageFiles(comp, false);
		if (imgHldrs == null)
			return null;
		return imgHldrs.elementAt(0);
	}

	public static Vector<ImageHolder> getImageFiles(Component comp,
			boolean allowMultiSel) {
		Vector<File> selFiles = null;
		selFiles = getSelectedFiles(comp, FileFilterFactory.IMAGE_FILE_FILTER,
				allowMultiSel);
		if (selFiles == null)
			return null;
		Vector<ImageHolder> imgFiles = new Vector<ImageHolder>();
		for (int i = 0; i < selFiles.size(); i++)
			imgFiles.addElement(getImageHolder(selFiles.elementAt(i)));
		return imgFiles;
	}

	public static File getXONDesignFile(Component comp) {
		Vector<File> files = null;
		files = getSelectedFiles(comp,
				FileFilterFactory.XON_DESIGN_FILE_FILTER, false);
		if (files == null)
			return null;
		return files.elementAt(0);
	}

	public static Vector<File> getSelectedFiles(Component comp,
			RGPTFileFilter fileFilter, boolean allowMultiSel) {
		FileUIInfo fileUIInfo = new FileUIInfo(fileFilter);
		// Show File Chooser to select appropriate font
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(fileFilter);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(allowMultiSel);

		// Add custom icons for file types.
		fc.setFileView(fileUIInfo);

		// Setting the Preview for Image Filter
		ImagePreview imgPreview = new ImagePreview(fileUIInfo);
		fc.setAccessory(imgPreview);
		fc.addPropertyChangeListener(imgPreview);

		int returnVal = fc.showOpenDialog(comp);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;

		Vector<File> files = new Vector<File>();
		if (!allowMultiSel) {
			java.io.File selFile = fc.getSelectedFile();
			files.addElement(selFile);
			fc.setSelectedFile(null);
			return files;
		}
		File[] selFiles = fc.getSelectedFiles();
		for (int i = 0; i < selFiles.length; i++)
			files.addElement(selFiles[i]);
		fc.setSelectedFile(null);
		return files;
	}

	public static ImageHolder getImageHolder(File file) {
		String filepath = file.getAbsolutePath();
		RGPTLogger.logToFile("Image File Choosen: " + filepath);
		String srcDir = file.getParent();
		String fileName = file.getName();
		return new ImageHolder(srcDir, fileName, RGPTUtil.getBytes(filepath));
	}

	public static BasicStroke getDashLineStroke() {
		BasicStroke dashStroke;
		float dashes[] = { 5f, 5f };
		dashStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 10f, dashes, 0f);
		return dashStroke;
	}

	// FUNCTION FOR FILECHOOSER
	public static String chooseFilePath(Component comp, String fileExt,
			String append2FileName, FileFilter filter) {
		return chooseFilePath(comp, fileExt, append2FileName, filter, "");
	}

	public static String chooseFilePath(Component comp, String fileExt,
			String append2FileName, FileFilter filter, String suggestFileName) {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(filter);
		if (suggestFileName != null && suggestFileName.length() > 0) {
			String ext = RGPTFileFilter.getExtension(suggestFileName);
			String newFileName = "";
			if (ext != null) {
				newFileName = RGPTFileFilter.getFileName(suggestFileName);
				newFileName = newFileName + append2FileName + "." + ext;
			} else
				newFileName = suggestFileName + append2FileName;

			fc.setSelectedFile(new File(newFileName));
		}
		int returnVal = fc.showSaveDialog(comp);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;

		String filepath = fc.getSelectedFile().getAbsolutePath();
		File resFile = fc.getSelectedFile();
		String ext = RGPTFileFilter.getExtension(resFile.getName());
		if (ext == null) {
			if (filepath.contains(append2FileName))
				filepath = filepath + fileExt;
			else
				filepath = filepath + append2FileName + fileExt;
			resFile = new File(filepath);
		}

		if (resFile.exists()) {
			if (resFile.isDirectory()) {
				JOptionPane.showMessageDialog(comp,
						"Cannot Overwrite a Directory.", "PDFView Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			} else {
				int res = JOptionPane
						.showConfirmDialog(comp, "File Exists, Overwrite?",
								"Confirm Overwrite", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (!(res == JOptionPane.OK_OPTION))
					return null;
			}
		}
		return filepath;
	}

	// Create UI for WF Steps
	public static JButton getWFStep(Vector<JButton> actionButtons, String action) {
		for (int i = 0; i < actionButtons.size(); i++) {
			JButton button = actionButtons.elementAt(i);
			String[] cmnd = button.getActionCommand().split("=");
			String stepAction = cmnd[1];
			if (stepAction.equals(action))
				return button;
		}
		return null;
	}

	public static void setWFActionMap(Vector<JButton> actionButtons,
			String action, Map<String, String> wfActionMap) {
		for (int i = 0; i < actionButtons.size(); i++) {
			JButton button = actionButtons.elementAt(i);
			String[] cmnd = button.getActionCommand().split("=");
			String stepAction = cmnd[1];
			wfActionMap.put(stepAction, action);
		}
	}

	public static Vector<JButton> createWFSteps(JPanel wfMainPanel,
			JButton wfButton, int panelWt, String wfProp,
			RGPTActionListener listener, Map<String, String> stepSel, int vgap) {
		Vector<JButton> actionButtons = new Vector<JButton>();
		LocalizationUtil lu = new LocalizationUtil();
		int stepWt = AppletParameters.getIntVal("WFStepWidth");
		int stepHt = AppletParameters.getIntVal("WFStepHeight");
		String[] steps = (AppletParameters.getVal(wfProp)).split(":NS:");

		wfMainPanel.setLayout(new BoxLayout(wfMainPanel, BoxLayout.Y_AXIS));

		int emptyPanelHt = vgap;
		JPanel emptyPanel = createEmptyPanel(panelWt, emptyPanelHt);
		// emptyPanel.setBorder(new LineBorder(Color.BLACK));
		wfMainPanel.add(emptyPanel);

		int vGap = 0, hGap = 25;
		JPanel wfPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hGap,
				vGap));
		wfPanel.add(wfButton);
		wfPanel.add(createLabel("rgpticons/WFPointer.png", -1));
		String step, stepCmd, stepTip, actionFld;
		String[] stepData = null;
		RGPTActionListener.WFActions stepAction = null;
		for (int iter = 0; iter < steps.length; iter++) {
			stepData = steps[iter].split("::");
			stepCmd = stepData[0];
			actionFld = stepData[1];
			step = lu.getText(stepCmd);
			stepTip = lu.getText("ToolTip_" + stepCmd);
			stepAction = RGPTActionListener.WFActions.valueOf(actionFld);
			JButton button = createActionButton(step, stepWt, stepHt, stepTip,
					listener, stepAction.toString());
			if (stepSel != null)
				stepSel.put(stepAction.toString(), step);
			if (stepSel != null)
				stepSel.put(stepAction.toString(), step);
			actionButtons.addElement(button);
			// button.setBorder(new LineBorder(Color.BLACK));
			wfPanel.add(button);
			if (iter < steps.length - 1)
				wfPanel.add(createLabel("rgpticons/NextStep.png", -1));
		}
		wfMainPanel.add(wfPanel);

		emptyPanel = createEmptyPanel(panelWt, 2 * emptyPanelHt);
		// emptyPanel.setBorder(new LineBorder(Color.BLACK));
		wfMainPanel.add(emptyPanel);
		// wfPanel.setBorder(new LineBorder(Color.BLACK));
		return actionButtons;
	}

	public static BufferedImage createThumbPreviewImage(BufferedImage origImage) {
		int thumbImgWt = AppletParameters.getIntVal("ThumbviewImageWidth");
		int thumbImgHt = AppletParameters.getIntVal("ThumbviewImageHeight");
		return ImageUtils.scaleImage(origImage, -1, thumbImgHt, true);
	}

}