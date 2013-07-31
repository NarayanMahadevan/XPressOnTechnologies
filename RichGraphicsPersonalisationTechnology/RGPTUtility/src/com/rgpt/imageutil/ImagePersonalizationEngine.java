// RGPT PACKAGES
package com.rgpt.imageutil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Vector;

import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.StaticFieldInfo;
import com.rgpt.util.TextStroke;

public class ImagePersonalizationEngine {
	static int COLORTYPE = BufferedImage.TYPE_INT_ARGB;

	public static HashMap createPersonalizedImage(String vdpText, Font font,
			Rectangle dispRect, HashMap vdpData, Component comp,
			boolean useLowResImage) throws Exception {
		return createPersonalizedImage(vdpText, font, dispRect, vdpData, comp,
				useLowResImage, null);
	}

	public static HashMap createPersonalizedImage(String vdpText, Font font,
			Rectangle dispRect, HashMap vdpData, Component comp,
			boolean useLowResImage, Vector<Point2D.Double> shPts)
			throws Exception {
		HashMap result = new HashMap();
		Rectangle bounds;
		Shape txtOnShape = null, relTxtOnShape = null;

		double rotAng = ((Double) vdpData.get("Rotation")).doubleValue();
		double shx = ((Double) vdpData.get("ShearX")).doubleValue();
		double shy = ((Double) vdpData.get("ShearY")).doubleValue();
		boolean isRotFirst = ((Boolean) vdpData.get("IsRotationFirst"))
				.booleanValue();
		int shapeType = ((Integer) vdpData.get("ShapeType")).intValue();
		double panelWt = (double) dispRect.width, panelHt = (double) dispRect.height;

		// System.out.println("ADJUST_TEXT IN DRAW: " + tx + " y: " + ty);
		// RGPTLogger.logToFile("Font File: " + fontFile + " Font Sz: " + fontSz
		// +
		// " Font Clr: " + fontClr);
		// RGPTLogger.logToFile("Derived Font: " + font.toString());
		// fitTxt = false;
		if (shapeType != -1) {
			txtOnShape = RGPTUtil.getShape(shPts, shapeType);
		}
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		float factor = 1f;
		int dx, dy;
		dx = dy = (int) (factor * fm.getHeight());
		if (shapeType == -1)
			dx = dy = 0;
		// rotAng = 12.0;
		Rectangle newDispRect = new Rectangle(dispRect.x - dx, dispRect.y - dy,
				dispRect.width + 2 * dx, dispRect.height + 2 * dy);
		AffineTransform affine = new AffineTransform();
		Polygon poly = RGPTUtil.getTransformPolygon(affine, newDispRect,
				rotAng, shx, shy, isRotFirst);

		bounds = poly.getBounds();
		if (shapeType != -1) {
			Point2D.Double startPt = new Point2D.Double(dx, dy);
			relTxtOnShape = RGPTUtil.getShape(shPts, shapeType, startPt);
			if (shapeType != StaticFieldInfo.LINE_PATH) {
				affine = RGPTUtil.getRectTransform(dispRect, affine, rotAng,
						0.0, 0.0, isRotFirst);
			}
		} else
			relTxtOnShape = txtOnShape;
		java.awt.Image textPersImg;
		textPersImg = createPersonalizedImage(comp, vdpText, font,
				relTxtOnShape, poly, dispRect, vdpData, useLowResImage);
		result.put("FinalPolygon", poly);
		result.put("FinalTransform", affine);
		result.put("FinalImage", textPersImg);
		result.put("FinalShape", txtOnShape);
		result.put("FinalDisplayRect", newDispRect);
		// System.out.println("Creating Image Personalization Output");
		return result;
	}

	private static Image createPersonalizedImage(Component comp, String text,
			Font font, Shape textOnShape, Polygon poly, Rectangle dispRect,
			HashMap vdpData, boolean useLowResImage) {
		// Get fontmetrics and calculate position.
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);

		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		int ascent = fm.getMaxAscent();
		int leading = fm.getLeading();

		// Specify Offsets to draw text in Image.
		boolean drawStroke = false;
		double rtx = 0.0, rty = 0.0;
		double rotAng = ((Double) vdpData.get("Rotation")).doubleValue();
		double shx = ((Double) vdpData.get("ShearX")).doubleValue();
		double shy = ((Double) vdpData.get("ShearY")).doubleValue();
		double tx = ((Double) vdpData.get("AdjustTextX")).doubleValue();
		double ty = ((Double) vdpData.get("AdjustTextY")).doubleValue();
		boolean drawTextOutline = ((Boolean) vdpData.get("DrawTextOutline"))
				.booleanValue();
		double panelWt = (double) dispRect.width, panelHt = (double) dispRect.height;
		if (rotAng != 0) {
			if (drawTextOutline)
				drawStroke = true;
			rtx = tx;
			rty = ty;
			tx = 0.0;
			ty = 0.0;
		}
		int xoffset = 0, yoffset = 0;
		if (textOnShape == null) {
			int[] xPts = poly.xpoints, yPts = poly.ypoints;
			xoffset = Math.abs(xPts[0] - xPts[3]);
			if (shx > 0)
				xoffset = (int) xoffset / 4;
		}
		xoffset += (int) Math.round(tx);
		yoffset = (int) Math.round(ty);
		// System.out.println("Text Adj: " + Math.round(tx) + " y: " +
		// Math.round(ty));
		// System.out.println("XOffset: " + xoffset + " YOffset: " + yoffset);
		// yoffset = yPts[3] - yPts[2];
		BufferedImage textImg = null;

		Rectangle bounds = null;
		Shape fillShape = null;
		boolean rotateFillImg = true;
		vdpData.put("RotateFillImage", rotateFillImg);
		if (textOnShape == null) {
			textImg = drawTextInBox(text, font, dispRect, xoffset, yoffset,
					drawStroke, vdpData, useLowResImage);
			bounds = new Rectangle((int) panelWt, (int) panelHt);
			fillShape = bounds;
		} else {
			textImg = drawTextAlongGraphics(text, font, textOnShape, dispRect,
					xoffset, yoffset, drawStroke, vdpData, useLowResImage);
			fillShape = textOnShape;
			bounds = textOnShape.getBounds();
			// return textImg;
		}
		vdpData.remove("RotateFillImage");

		if (rotAng == 0) {
			if (!rotateFillImg) {
				Graphics2D g2d = (Graphics2D) textImg.createGraphics();
				AffineTransform affine = g2d.getTransform();
				affine.translate(xoffset, yoffset);
				g2d.setTransform(affine);
				fillShape(g2d, fillShape, vdpData, bounds, useLowResImage);
			}
			return textImg;
		}
		int imgWt = textImg.getWidth(null), imgHt = textImg.getHeight(null);
		// Rectangle rect = poly.getBounds();
		Rectangle rect = new Rectangle(imgWt, imgHt);
		BufferedImage rotImg = (BufferedImage) ImageUtils.rotateImage(textImg,
				-rotAng, rect, rtx, rty);
		// return rotateImage(textImg, -rotAng, comp);
		// com.rgpt.image_filter.RotateFilter rotFil = null;
		// rotFil = new com.rgpt.image_filter.RotateFilter((new
		// Double(-rotAng)).floatValue());
		// BufferedImage rotImg = null;
		// rotImg = rotFil.filter(textImg, rotImg);
		if (rotateFillImg)
			return rotImg;

		Graphics2D g2d = (Graphics2D) rotImg.createGraphics();
		// AffineTransform affine = new AffineTransform();
		AffineTransform affine = g2d.getTransform();
		double ftx = 0, fty = 0;
		ftx = (rect.width - imgWt) / 2;
		fty = (rect.height - imgHt) / 2;
		// ftx = (imgWt)/2, fty = (imgHt)/2;
		System.out.println("Fill Shape Tx: " + ftx + " Ty: " + fty);
		fty = 0;
		affine.setToTranslation(ftx, fty);
		// affine.translate(xoffset, yoffset);
		g2d.setTransform(affine);
		fillShape(g2d, fillShape, vdpData, bounds, useLowResImage);
		return rotImg;
	}

	private static BufferedImage drawTextInBox(String text, Font font,
			Rectangle dispRect, int xoffset, int yoffset, boolean drawStroke,
			HashMap vdpData, boolean useLowResImage) {
		int fontClr = ((Integer) vdpData.get("FillColor")).intValue();
		String hallign = (String) vdpData.get("TextAllignment");
		String vallign = (String) vdpData.get("VerticalTextAllignment");
		double panelWt = (double) dispRect.width, panelHt = (double) dispRect.height;
		double rotAng = 0.0;
		double shx = ((Double) vdpData.get("ShearX")).doubleValue();
		double shy = ((Double) vdpData.get("ShearY")).doubleValue();
		boolean isRotFirst = ((Boolean) vdpData.get("IsRotationFirst"))
				.booleanValue();
		boolean rotateFillImg = ((Boolean) vdpData.get("RotateFillImage"))
				.booleanValue();
		boolean drawTextOutline = ((Boolean) vdpData.get("DrawTextOutline"))
				.booleanValue();
		ImageHolder clipImgHldr = (ImageHolder) vdpData.get("ClipImageHolder");
		String fillShapeLogic = (String) vdpData.get("FillShapeLogic");
		int fillColor = ((Integer) vdpData.get("FillShapeColor")).intValue();
		ImageHolder fillShapeImg = (ImageHolder) vdpData.get("FillShapeImage");
		int percTransarent = ((Integer) vdpData.get("FillTransperancy"))
				.intValue();

		// Get fontmetrics and calculate position.
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);

		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		int ascent = fm.getMaxAscent();
		int leading = fm.getLeading();
		int descent = fm.getDescent();

		BufferedImage textImg = new BufferedImage((int) panelWt, (int) panelHt,
				COLORTYPE);
		// BufferedImage textImg = new BufferedImage(width, height, COLORTYPE);
		Graphics2D g2d = (Graphics2D) textImg.createGraphics();
		AffineTransform affine = g2d.getTransform();

		affine = RGPTUtil.transform(affine, rotAng, shx, shy, isRotFirst);
		ImageUtils.setTextRenderingHints(g2d);
		g2d.setFont(font);
		g2d.setTransform(affine);
		Point2D.Double startPt = new Point2D.Double(0.0, (double) height);
		startPt = RGPTUIUtil.getStartPts(g2d, font, text, hallign, vallign,
				panelWt, panelHt);
		double x = startPt.getX() + xoffset;
		if (x == 0)
			x = 4;
		// x = x + Math.abs(shx);
		// int y = (int)startPt.getY() + ascent + leading;
		// int y = (int)startPt.getY();
		int y = (int) startPt.getY() + yoffset - descent;
		RGPTLogger.logToFile("Start Ptx: " + x + " Pt y: " + y);

		// Fill Shape Logic is executed here
		if (fillShapeLogic != null && fillShapeLogic.length() > 0
				&& rotateFillImg) {
			Rectangle fillShape = new Rectangle(0, 0, (int) panelWt,
					(int) panelHt);
			fillShape(g2d, fillShape, vdpData, fillShape, useLowResImage);
		}
		// boolean drawTextOutline = true;
		g2d.setPaint(new java.awt.Color(fontClr));
		if (!drawTextOutline) {
			g2d.drawString(text, (int) Math.round(x), y);
			return textImg;
		}

		// Creating Font Outline
		FontRenderContext frc = g2d.getFontRenderContext();
		Shape textOutline = RGPTUIUtil.getTextOutline(null, font, text);
		AffineTransform transform = g2d.getTransform();
		transform.translate((int) Math.round(x), y);
		g2d.transform(transform);
		g2d.setColor(new java.awt.Color(fontClr));
		// if (drawStroke) {
		// BasicStroke basicStroke = new BasicStroke(1.0f);
		// g2d.setStroke(basicStroke);
		// }
		if (clipImgHldr == null)
			g2d.draw(textOutline);
		else {
			try {
				// BufferedImage clipImg =
				// ImageUtils.getBufferedImage(clipImgHldr.m_ImgStr);
				Rectangle bounds = new Rectangle(0, 0, (int) panelWt,
						(int) panelHt);
				BufferedImage clipImg = clipImgHldr.getImage(useLowResImage,
						bounds);
				// System.out.println("Clip Image Data: " +
				// clipImgHldr.toString());
				// System.out.println("Clip Image: " + clipImg.toString());
				Rectangle r = textOutline.getBounds();
				// BufferedImage scClipImg = scaleImage(clipImg, new
				// Dimension((int)panelWt,
				// (int)panelHt));
				g2d.draw(textOutline);
				g2d.setClip(textOutline);
				g2d.drawImage(clipImg, r.x, r.y, r.width, r.height, null);
			} catch (Exception ex) {
				ex.printStackTrace();
				g2d.draw(textOutline);
			}
		}
		return textImg;
	}

	private static BufferedImage drawTextAlongGraphics(String text, Font font,
			Shape textOnShape, Rectangle dispRect, int xoffset, int yoffset,
			boolean drawStroke, HashMap vdpData, boolean useLowResImage) {
		int fontClr = ((Integer) vdpData.get("FillColor")).intValue();
		String hallign = (String) vdpData.get("TextAllignment");
		String vallign = (String) vdpData.get("VerticalTextAllignment");
		double panelWt = (double) dispRect.width, panelHt = (double) dispRect.height;
		double rotAng = 0.0;
		double shx = ((Double) vdpData.get("ShearX")).doubleValue();
		double shy = ((Double) vdpData.get("ShearY")).doubleValue();
		boolean isRotFirst = ((Boolean) vdpData.get("IsRotationFirst"))
				.booleanValue();
		boolean drawTextOutline = ((Boolean) vdpData.get("DrawTextOutline"))
				.booleanValue();
		boolean stretchToFit = ((Boolean) vdpData.get("AutoFitText"))
				.booleanValue();
		ImageHolder clipImgHldr = (ImageHolder) vdpData.get("ClipImageHolder");

		AffineTransform affine = new AffineTransform();
		affine.setToShear(shx, shy);
		Font newfont = font.deriveFont(affine);
		TextStroke ts = new TextStroke(text, newfont, stretchToFit, false);
		Shape textStrSh = ts.createStrokedShape(textOnShape);
		Rectangle bounds = textOnShape.getBounds();
		// System.out.println("Shape Bounds: " + bounds.toString());
		BufferedImage textImg = new BufferedImage(bounds.width + 100,
				bounds.height + 100, COLORTYPE);
		Graphics2D g2d = (Graphics2D) textImg.createGraphics();
		affine = g2d.getTransform();
		affine.translate(xoffset, yoffset);
		g2d.setTransform(affine);

		boolean rotateFillImg = ((Boolean) vdpData.get("RotateFillImage"))
				.booleanValue();
		if (rotateFillImg)
			fillShape(g2d, textOnShape, vdpData, bounds, useLowResImage);

		g2d.setPaint(new Color(fontClr));
		// System.out.println("Text Stroke Bounds: " + textStrSh.getBounds());
		// Point2D.Double strtPt = RGPTUtil.getStartPoint(textOnShape);
		// if (strtPt != null) {
		// System.out.println("Negate Translation: " + strtPt);
		// }
		boolean drawShape = false;
		if (drawShape)
			g2d.draw(textOnShape);
		if (!drawTextOutline) {
			g2d.setColor(new Color(fontClr));
			g2d.fill(textStrSh);
			g2d.draw(textStrSh);
		} else {
			// Showing Font Outline
			if (clipImgHldr == null)
				g2d.draw(textStrSh);
			else {
				try {
					// BufferedImage clipImg =
					// ImageUtils.getBufferedImage(clipImgHldr.m_ImgStr);
					BufferedImage clipImg = clipImgHldr.getImage(
							useLowResImage, bounds);
					System.out.println("Clip Image Data: "
							+ clipImgHldr.toString());
					System.out.println("Clip Image: " + clipImg.toString());
					Rectangle r = textStrSh.getBounds();
					g2d.draw(textStrSh);
					g2d.setClip(textStrSh);
					g2d.drawImage(clipImg, r.x, r.y, r.width, r.height, null);
				} catch (Exception ex) {
					ex.printStackTrace();
					g2d.draw(textStrSh);
				}
			}
		}
		return textImg;
		/*
		 * BufferedImage finalImg = textImg; if (rotAng != 0) { Image img =
		 * rotateImage(textImg, -rotAng, comp); finalImg = CreateCopy(img); } //
		 * com.rgpt.image_filter.RotateFilter rotFil = null; // rotFil = new
		 * com.rgpt.image_filter.RotateFilter((new
		 * Double(-rotAng)).floatValue()); // BufferedImage rotImg = null; //
		 * rotImg = rotFil.filter(textImg, rotImg); // return rotImg;
		 * 
		 * // Fill Shape Logic is executed here if (fillShapeLogic != null &&
		 * fillShapeLogic.length() > 0) { g2d = (Graphics2D)
		 * finalImg.createGraphics(); fillShape(g2d, fillShapeLogic,
		 * textOnShape, fillColor, fillShapeImg, percTransarent, bounds); }
		 * 
		 * return finalImg;
		 */
	}

	public static void fillShape(Graphics2D g2d, Shape fillShape,
			HashMap vdpData, Rectangle bounds, boolean useLowResImage) {
		String fillShapeLogic = (String) vdpData.get("FillShapeLogic");
		if (fillShapeLogic == null)
			return;
		if (fillShapeLogic.length() == 0)
			return;
		int fillColor = ((Integer) vdpData.get("FillShapeColor")).intValue();
		ImageHolder fillShapeImg = (ImageHolder) vdpData.get("FillShapeImage");
		int percTransarent = ((Integer) vdpData.get("FillTransperancy"))
				.intValue();
		if (fillShapeLogic.equals(StaticFieldInfo.COLOR_FILL)) {
			Color transperantColor = ImageUtils.getTransparentColor(new Color(
					fillColor), percTransarent);
			// g2d.draw(new Rectangle(xoffset, yoffset, (int)panelWt,
			// (int)panelHt));
			g2d.setColor(transperantColor);
			g2d.fill(fillShape);
		} else if (fillShapeLogic.equals(StaticFieldInfo.IMAGE_FILL)) {
			try {
				// BufferedImage fillImg =
				// ImageUtils.getBufferedImage(fillShapeImg.m_ImgStr);
				BufferedImage fillImg = fillShapeImg.getImage(useLowResImage,
						bounds);
				if (percTransarent != -1) {
					float alpha = (float) percTransarent / 100;
					fillImg = ImageUtils.ConvToTransparentImage(fillImg, alpha);
				}
				// fillImg = clipImageByShape(fillImg, bounds.width,
				// bounds.height, textOnShape);
				g2d.setClip(fillShape);
				// System.out.println("Print Shape: " +
				// RGPTUtil.printShape(textOnShape));
				g2d.drawImage(fillImg, bounds.x, bounds.y, bounds.width,
						bounds.height, null);
				g2d.setClip(null);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}