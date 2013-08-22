// RGPT PACKAGES
package com.rgpt.imageutil;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.awt.image.ShortLookupTable;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rgpt.imagefilters.ImageFilterUtils;
import com.rgpt.imagefilters.SharpenFilter;
import com.rgpt.imagefilters.UnsharpFilter;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTShapeUtil;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.StaticFieldInfo;

public class ImageUtils {

	static int ALPHA = 3; // ignored in RGB
	static int RED = 0;
	static int GREEN = 1;
	static int BLUE = 2;
	static int COLORTYPE = BufferedImage.TYPE_INT_ARGB;

	public static void setRenderingHints(Graphics2D g2d) {
		// This time, we want to use anti-aliasing if possible to avoid the
		// jagged edges
		// With the Java 2D rendering engine (Graphics2D) to do this using a
		// "rendering hint".
		RenderingHints rh = g2d.getRenderingHints();
		rh.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		// rh.put(RenderingHints.KEY_COLOR_RENDERING,
		// RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		// rh.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
		// RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		// rh.put(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// rh.put(RenderingHints.KEY_FRACTIONALMETRICS,
		// RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHints(rh);
	}

	public static void setTextRenderingHints(Graphics2D g2d) {
		// This time, we want to use anti-aliasing if possible to avoid the
		// jagged edges
		// With the Java 2D rendering engine (Graphics2D) to do this using a
		// "rendering hint".
		// RenderingHints rh = g2d.getRenderingHints();
		// Initially set to VALUE_TEXT_ANTIALIAS_GASP
		RenderingHints rh = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		rh.put(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_DEFAULT);
		rh.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		rh.put(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		rh.put(RenderingHints.KEY_TEXT_LCD_CONTRAST, new Integer(140));
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_DEFAULT);
		// rh.put(RenderingHints.KEY_COLOR_RENDERING,
		// RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		// rh.put(RenderingHints.KEY_STROKE_CONTROL,
		// RenderingHints.VALUE_STROKE_NORMALIZE);
		// rh.put(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		// System.out.println("\n\nRendering Hints:\n" + rh.toString());
		g2d.setRenderingHints(rh);
	}

	public static BufferedImage MaskImageComposite(BufferedImage origImg,
			BufferedImage picFrameImg) {
		int i = origImg.getHeight();
		int j = origImg.getWidth();
		picFrameImg = ScaleToSize(picFrameImg, j, i);
		BufferedImage newImg = new BufferedImage(origImg.getWidth(),
				origImg.getHeight(), origImg.getType());
		Graphics2D g2d = newImg.createGraphics();
		setRenderingHints(g2d);
		g2d.drawImage(origImg, 0, 0, null);
		g2d.setComposite(AlphaComposite.getInstance(3, 1.0F));
		g2d.drawImage(picFrameImg, 0, 0, null);
		g2d.dispose();
		return newImg;
	}

	public static BufferedImage MaskImageRGB(BufferedImage myimg,
			BufferedImage maskimg, double threshold, int color,
			boolean isOpaquePicFrame) {
		int MaxRows = myimg.getHeight();
		int MaxCols = myimg.getWidth();
		maskimg = scaleImage(maskimg, MaxCols, MaxRows, false);
		for (int row = 0; row < MaxRows; row++) {
			for (int col = 0; col < MaxCols; col++) {
				int pixel = 0;
				try {
					pixel = maskimg.getRGB(col, row);
				} catch (Throwable ex) {
					return null;
				}
				int alpha = (pixel >> 24) & 0xFF;
				int red = (pixel >> 16) & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int blue = (pixel) & 0xFF;
				double value = ((red * 0.299) + (green * 0.587) + (blue * 0.114));
				if (isOpaquePicFrame) {
					if (pixel != 0)
						myimg.setRGB(col, row, maskimg.getRGB(col, row));
				} else {
					if (color == -1)
						color = (int) findavg(myimg);
					if (value <= threshold)
						myimg.setRGB(col, row, color);
				}
			}
		}
		return (myimg);
	}

	public static double findavg(BufferedImage bimg) {
		double avg = 0;
		int avgarry[] = { 0, 32, 64, 128, 160, 192, 224, 255 };
		int max = 0;

		int maxCols = bimg.getWidth();
		int maxRows = bimg.getHeight();
		int[][][] pixel_rgb = BufferedImagetoPixel3DRGB(bimg);

		for (int row = 0; row < maxRows; row++) {
			for (int col = 0; col < maxCols; col++) {
				max = (pixel_rgb[row][col][RED] + pixel_rgb[row][col][BLUE] + pixel_rgb[row][col][GREEN]);
				avg = (avg + max / 3) / 2;
			}
		}
		// System.out.println("Average of Image " + avg);
		for (int i = 1; i < 8; i++) {
			if (avg < avgarry[i]) {
				avg = avgarry[i - 1];
				// System.out.println("Average of Image -final " + avg);
				return (avg);
			}
		}
		// System.out.println("Average of Image -final " + avg);
		return (avg);
	}

	public static void displayImage(Image img, String mesg) {
		JDialog imgBox = displayImage(img, mesg, 100, 100, 500, 500);
		imgBox.setVisible(true);
	}

	public static JDialog displayImage(Image img, String mesg, int locx,
			int locy, int sizex, int sizey) {
		JDialog imgBox = new JDialog(new java.awt.Frame(), mesg, false);
		JPanel imgPanel = new JPanel(new BorderLayout());
		imgBox.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		imgBox.setLocation(locx, locy);
		imgBox.setSize(sizex, sizey);
		imgBox.setContentPane(imgPanel);
		ImageIcon imgIcon = new ImageIcon(img, "");
		JLabel imgLabel = new JLabel(imgIcon);
		imgBox.add(imgLabel, BorderLayout.CENTER);
		return imgBox;
	}

	public static JDialog displayImage(URL resourcePath, String mesg, int locx,
			int locy, int sizex, int sizey) {
		JDialog imgBox = new JDialog(new java.awt.Frame(), mesg, true);
		JPanel imgPanel = new JPanel(new BorderLayout());
		imgBox.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		imgBox.setLocation(locx, locy);
		imgBox.setSize(sizex, sizey);
		imgBox.setContentPane(imgPanel);
		ImageIcon imgIcon = new ImageIcon(resourcePath, mesg);
		JLabel imgLabel = new JLabel(imgIcon);
		imgBox.add(imgLabel, BorderLayout.CENTER);
		return imgBox;
	}

	// This function loads image from the Jar File
	public static byte[] loadImage(String imgPath, Class caller) {
		int MAX_IMAGE_SIZE = 2400; // Change this to the size of
									// your biggest image, in bytes.
		byte buf[] = null;
		int count = 0;
		System.out.println("Load Image Stream: " + imgPath);
		BufferedInputStream imgStream = null;
		imgStream = new BufferedInputStream(caller.getResourceAsStream(imgPath));
		if (imgStream == null) {
			System.err.println("Couldn't find file: " + imgPath);
			return null;
		}

		// Creating the Image Icon from the stream
		try {
			System.out.println("Image Stream is Not Null: " + imgStream);
			System.out.println("Image Stream 4: " + imgPath + " is: "
					+ imgStream.available());
			buf = new byte[imgStream.available()];
			count = imgStream.read(buf, 0, buf.length);
			System.out.println("Buffered Size: " + buf.length + " count: "
					+ count);
			imgStream.close();
		} catch (IOException ioe) {
			System.err.println("Couldn't read stream from file: " + imgPath);
			return null;
		} catch (Throwable th) {
			th.printStackTrace();
			System.out.println("Cought Exception while reading stream");
			return null;
		}
		if (count <= 0) {
			System.err.println("Empty file: " + imgPath);
			return null;
		}
		return buf;
	}

	public static BufferedImage getBufferedImage(String imgPath)
			throws Exception {
		File f = new File(imgPath);
		BufferedImage img = null;
		img = ImageIO.read(f);
		return img;
	}

	public static BufferedImage getFinalBufferedImage(String imgPath,
			Class caller) {
		try {
			return getBufferedImage(imgPath, caller);
		} catch (Exception ex) {
			RGPTLogger.logToFile("Exception at getFinalBufferedImage", ex);
		}
		return null;
	}

	public static BufferedImage getBufferedImage(String imgPath, Class caller)
			throws Exception {
		// System.out.println("**ENTERED loadImage**");
		// System.out.println("Inside loadImage pathname is:- " + imgPath);
		File f = new File(imgPath);
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
		} catch (Throwable e) {
			String resImgPath = "/" + imgPath;
			img = getBufferedImage(loadImage(resImgPath, caller));
		}
		return img;
	}

	public static BufferedImage getBufferedImage(byte[] imgStr)
			throws Exception {
		// System.out.println("Buffered Size: " + imgStr.length);
		return ImageIO.read(new java.io.ByteArrayInputStream(imgStr));
	}

	// This return s a BufferedImage scaled to the size mentioned
	public static BufferedImage getBufferedImage(byte[] imgStr, int imageSize)
			throws Exception {
		BufferedImage bufImg = getBufferedImage(imgStr);
		if (imageSize == -1 || bufImg.getWidth(null) <= imageSize)
			return bufImg;
		// Reseampling the Image to create Thumbview. The getScaledInstance
		// method
		// maintains the Aspect Ratio.
		bufImg = (BufferedImage) bufImg.getScaledInstance(imageSize, -1,
				Image.SCALE_DEFAULT);
		return bufImg;
	}

	public static Image loadImage(byte[] imgStr, int imageSize) {
		ImageIcon imgIcon = new ImageIcon(imgStr);
		if (imgIcon == null)
			return null;
		return getScaledImage(imgIcon.getImage(), imageSize);
	}

	public static Image getScaledImage(Image image, int imageSize) {
		if (imageSize == -1 || image.getWidth(null) <= imageSize)
			return image;
		// Reseampling the Image to create Thumbview. The getScaledInstance
		// method
		// maintains the Aspect Ratio.
		image = image.getScaledInstance(imageSize, -1, Image.SCALE_DEFAULT);
		return image;
	}

	public static Image loadImage(String filePath, int imageSize) {
		ImageIcon imgIcon = new ImageIcon(filePath);
		if (imgIcon == null)
			return null;
		return getScaledImage(imgIcon.getImage(), imageSize);
	}

	public static ImageHolder loadImageHolder(String srcPath, String fileName,
			int imageSize) {
		ImageHolder imgHldr = null;
		Image thumbImage = null;
		ImageIcon imgIcon = new ImageIcon(srcPath + fileName);
		if (imgIcon == null)
			return null;
		Image bigImage = imgIcon.getImage();

		// Reseampling the Image to create Thumbview. The getScaledInstance
		// method
		// maintains the Aspect Ratio.
		if (imageSize != -1)
			thumbImage = bigImage.getScaledInstance(imageSize, -1,
					Image.SCALE_DEFAULT);
		int assetId = -1;
		// int dpi = FindResolution(bigImage, w, h);
		// System.out.println("DPI of the Image: " + dpi);
		imgHldr = new ImageHolder(assetId, thumbImage, null);
		imgHldr.m_SrcDir = srcPath;
		imgHldr.m_FileName = fileName;
		imgHldr.m_UseCachedImages = true;
		imgHldr.m_OrigImageWidth = bigImage.getWidth(null);
		imgHldr.m_OrigImageHeight = bigImage.getHeight(null);

		return imgHldr;
	}

	public static BufferedImage CreateCopy(Image srcImg) {
		return ScaleToSize(srcImg, -1, -1);
	}

	// If Width and Height are set to -1 then the Image width and height is
	// taken
	// and java.awt.Image is converted to BufferedImage
	public static BufferedImage ScaleToSize(Image srcImg, int w, int h) {
		if (w == -1)
			w = srcImg.getWidth(null);
		if (h == -1)
			h = srcImg.getHeight(null);
		if (w == -1 || h == -1) {
			srcImg = new ImageIcon(srcImg).getImage();
			w = srcImg.getWidth(null);
			h = srcImg.getHeight(null);
		}
		System.out.println("AWT Image Wt: " + w + " And Ht: " + h);
		BufferedImage buffImg = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = buffImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, null);
		g2.dispose();
		return buffImg;
	}

	// If Width and Height are set to -1 then the Image width and height is
	// taken
	// and java.awt.Image is converted to BufferedImage
	public static void drawImage(BufferedImage srcImg, BufferedImage img2Draw,
			int w, int h) {
		if (w == -1)
			w = (int) (srcImg.getWidth() / 2);
		if (h == -1)
			h = (int) (srcImg.getHeight() / 2);
		System.out.println("AWT Image Wt: " + w + " And Ht: " + h);
		Graphics2D g2 = srcImg.createGraphics();
		g2.drawImage(img2Draw, w, h, null);
		g2.dispose();
	}

	// This function Saves the altered image of set size to File and returns the
	// image.
	public static BufferedImage ScaleToSize(String inFileName, int maxCols,
			int maxRows, String outFileName, String format) {
		BufferedImage img = LoadImage(inFileName);
		BufferedImage altImg = ScaleToSize(img, maxCols, maxRows);
		SaveImageToFile(altImg, outFileName, format);
		return altImg;
	}

	public static BufferedImage ScaleToSize(BufferedImage bimg, int maxCols,
			int maxRows) {
		if (maxCols == -1)
			maxCols = bimg.getWidth(null);
		if (maxRows == -1)
			maxRows = bimg.getHeight(null);
		if (maxCols == -1 || maxRows == -1) {
			Image srcImg = new ImageIcon(bimg).getImage();
			maxCols = srcImg.getWidth(null);
			maxRows = srcImg.getHeight(null);
		}
		BufferedImage bimgnew = new BufferedImage(maxCols, maxRows, COLORTYPE);
		Graphics g = bimgnew.createGraphics();
		g.drawImage(bimg, 0, 0, maxCols, maxRows, null);
		// g.drawImage(bimg, 0, 0, null);
		g.dispose();
		return (bimgnew);
	}

	public static BufferedImage ClipImagebyPath(BufferedImage bimg,
			GeneralPath arbshape) {
		int ht = bimg.getHeight();
		int wd = bimg.getWidth();
		BufferedImage bimgnew = new BufferedImage(wd, ht, COLORTYPE);
		Graphics g = bimgnew.createGraphics();
		g.setClip(arbshape);
		g.drawImage(bimg, 0, 0, null);
		g.dispose();
		return (bimgnew);
	}

	public static BufferedImage clipImageByShape(BufferedImage bimg, int wd,
			int ht, Shape arbshape) {
		if (ht == -1)
			ht = bimg.getHeight();
		if (wd == -1)
			wd = bimg.getWidth();
		BufferedImage bimgnew = new BufferedImage(wd, ht, COLORTYPE);
		Graphics g = bimgnew.createGraphics();
		g.setClip(arbshape);
		g.drawImage(bimg, 0, 0, null);
		g.dispose();
		return (bimgnew);
	}

	public static BufferedImage ClipImagebyRect(BufferedImage bimg, int startx,
			int starty, int width, int height) {
		BufferedImage bimgnew = new BufferedImage(width, height, COLORTYPE);
		Graphics g = bimgnew.createGraphics();
		g.drawImage(bimg, startx, starty, width, height, null);
		g.dispose();
		return (bimgnew);
	}

	public static int[][][] MaskImage(BufferedImage bimg,
			BufferedImage maskimg, double threshold) {
		int maxCols = bimg.getWidth();
		int maxRows = bimg.getHeight();
		maskimg = ScaleToSize(maskimg, maxCols, maxRows);

		int[][][] pixel_rgb = BufferedImagetoPixel3DRGB(bimg);
		BufferedImage bimgnew = new BufferedImage(maxCols, maxRows, COLORTYPE);
		int[][][] newpixel_rgb = ImageUtils.BufferedImagetoPixel3DRGB(bimgnew);
		int[][][] maskpixel_rgb = ImageUtils.BufferedImagetoPixel3DRGB(maskimg);

		for (int row = 0; row < maxRows; row++) {
			for (int col = 0; col < maxCols; col++) {

				double maskavg = (maskpixel_rgb[row][col][RED]
						+ maskpixel_rgb[row][col][BLUE] + maskpixel_rgb[row][col][GREEN]) / 3;

				if (maskavg > threshold) {
					newpixel_rgb[row][col][RED] = pixel_rgb[row][col][RED];
					newpixel_rgb[row][col][GREEN] = pixel_rgb[row][col][GREEN];
					newpixel_rgb[row][col][BLUE] = pixel_rgb[row][col][BLUE];
				} else {
					newpixel_rgb[row][col][RED] = Color.WHITE.getRGB();
					newpixel_rgb[row][col][GREEN] = Color.WHITE.getRGB();
					newpixel_rgb[row][col][BLUE] = Color.WHITE.getRGB();
				}
			}
		}
		return (newpixel_rgb);
	}

	public static BufferedImage copyImagePixels(BufferedImage srcImg) {
		int[][][] pixel_rgb = ImageUtils.BufferedImagetoPixel3DRGB(srcImg);
		return ImageUtils.Pixel3DRGBtoBufferedImage(pixel_rgb);
	}

	public static int[][][] BufferedImagetoPixel3DRGB(BufferedImage bimg) {

		// System.out.println("Loaded Image : Image Type " + bimg.getType());
		int imgCols = bimg.getWidth();
		int imgRows = bimg.getHeight();
		// System.out.println("Image Rows " + imgRows + " Cols " + imgCols);
		int[] pixel = new int[imgCols * imgRows];
		pixel = bimg.getRGB(0, 0, imgCols, imgRows, pixel, 0, imgCols);

		// Create the One Dimensional array of type int to be populated with
		// pixel data, one int value
		// per pixel, with four color and alpha bytes per int value.

		int[][][] pixel_rgb = new int[imgRows][imgCols][4];
		for (int row = 0; row < imgRows; row++) {
			for (int col = 0; col < imgCols; col++) {
				int element = row * imgCols + col;
				// Alpha data
				pixel_rgb[row][col][ALPHA] = (pixel[element] >> 24) & 0xFF;
				// Red data
				pixel_rgb[row][col][RED] = (pixel[element] >> 16) & 0xFF;
				// Green data
				pixel_rgb[row][col][GREEN] = (pixel[element] >> 8) & 0xFF;
				// Blue data
				pixel_rgb[row][col][BLUE] = (pixel[element]) & 0xFF;
			}
		}
		return pixel_rgb;
	}

	public static BufferedImage LoadImage(String filename) {
		return LoadImage(new File(filename));
	}

	public static BufferedImage LoadImage(File imgFile) {
		BufferedImage bimg = null;
		try {
			System.out.println("Image File Size: " + imgFile.length());
			bimg = ImageIO.read(imgFile);
		} catch (Exception e) {
			System.out.println("Exception at LoadImage");
			e.printStackTrace();
		}
		return bimg;
	}

	public static BufferedImage Pixel3DRGBtoBufferedImage(int[][][] pixel_rgb) {
		return Pixel3DRGBtoBufferedImage(pixel_rgb, true, false);
	}

	public static BufferedImage Pixel3DRGBtoBufferedImage(int[][][] pixel_rgb,
			boolean hasAlpha, boolean translucent) {
		int imgRows = pixel_rgb.length;
		int imgCols = pixel_rgb[0].length;
		// System.out.println("Image Rows " + imgRows + " Cols " + imgCols);
		int[] pixel = new int[imgCols * imgRows * 4];
		for (int row = 0, cnt = 0; row < imgRows; row++) {
			for (int col = 0; col < imgCols; col++) {
				pixel[cnt] = ((pixel_rgb[row][col][ALPHA] << 24) & 0xFF000000)
						| ((pixel_rgb[row][col][RED] << 16) & 0x00FF0000)
						| ((pixel_rgb[row][col][GREEN] << 8) & 0x0000FF00)
						| ((pixel_rgb[row][col][BLUE]) & 0x000000FF);
				cnt++;
			}
		}
		BufferedImage bimg = new BufferedImage(imgCols, imgRows, COLORTYPE);
		// Create Image is not working for DOGFilter
		// BufferedImage bimg = createImage(imgCols, imgRows, hasAlpha,
		// translucent);
		bimg.setRGB(0, 0, imgCols, imgRows, pixel, 0, imgCols);
		return (bimg);
	}

	public static BufferedImage createFontImage(Font font, int fontClr, int wt,
			int ht) {
		BufferedImage fontImg = null;
		// System.out.println("Panel Wt: " + wt + " Ht: " + ht);
		String fontName = font.getName();
		BufferedImage finalFontImg = new BufferedImage(wt, ht, COLORTYPE);
		Graphics2D g2d = (Graphics2D) finalFontImg.createGraphics();

		String[] words = fontName.split(" ");
		// System.out.println("Font Name: " + fontName + " #ofWords: " +
		// words.length);
		float fontSz = 0.0f;
		for (int i = 0; i < words.length; i++) {
			float indFontSz = RGPTUIUtil.getActualFontSize(font, 10, words[i],
					wt, ht / words.length);
			// System.out.println("Ind Font Sz: " + indFontSz);
			if (fontSz == 0.0f || fontSz > indFontSz)
				fontSz = indFontSz;
		}
		// System.out.println("Derived Font Size: " + fontSz);
		font = font.deriveFont(fontSz);
		int posCnt = 0;
		for (int i = 0; i < words.length; i++) {
			fontImg = createImageFromText(font, fontClr, words[i], wt, ht
					/ words.length, "LEFT", "CENTER");
			g2d.drawImage(fontImg, 0, (int) (posCnt * ht / words.length), null);
			posCnt++;
		}
		return finalFontImg;
	}

	public static BufferedImage createImageFromText(Font font, int fontClr,
			String text, double panelWt, double panelHt, String hallign,
			String vallign) {
		// Get fontmetrics and calculate position.
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);

		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		int ascent = fm.getMaxAscent();
		int descent = fm.getDescent();
		int leading = fm.getLeading();

		BufferedImage textImg = new BufferedImage((int) panelWt, (int) panelHt,
				COLORTYPE);
		Graphics2D g2d = (Graphics2D) textImg.createGraphics();
		setTextRenderingHints(g2d);
		g2d.setFont(font);
		Point2D.Double startPt = new Point2D.Double(0.0, (double) height);
		startPt = RGPTUIUtil.getStartPts(g2d, font, text, hallign, vallign,
				panelWt, panelHt);
		double x = startPt.getX();
		if (x == 0)
			x = 2;
		int y = (int) startPt.getY() - descent;
		RGPTLogger.logToFile("Start Ptx: " + x + " Pt y: " + y);

		g2d.setPaint(new java.awt.Color(fontClr));
		g2d.drawString(text, (int) Math.round(x), y);
		return textImg;
	}

	// public static BufferedImage createTextImageInPoly(Polygon poly, Font
	// font,
	// int fontClr, String text, String hallign,
	// String vallign, double panelWt, double panelHt,
	// double rotAng, double shx, double shy,
	// boolean isRotFirst)
	// {
	// int [] xPts = poly.xpoints, yPts = poly.ypoints;
	// int minXPt = xPts[0], minYPt = yPts[0], maxXPt = xPts[0], maxYPt =
	// yPts[0];
	// System.out.println("Pt: 0 x: " + xPts[0] + " y: " + yPts[0]);
	// for (int i = 1; i < poly.npoints; i++)
	// {
	// System.out.println("Pt: " + i + " x: " + xPts[i] + " y: " + yPts[i]);
	// if (xPts[i] < minXPt) minXPt = xPts[i];
	// if (yPts[i] < minYPt) minYPt = yPts[i];
	// if (xPts[i] > maxXPt) maxXPt = xPts[i];
	// if (yPts[i] > maxYPt) maxYPt = yPts[i];
	// }
	// int polyPanelWt = maxXPt - minXPt, polyPanelHt = maxYPt - minYPt;
	// BufferedImage polyImg = new BufferedImage(polyPanelWt, polyPanelHt,
	// COLORTYPE);
	// BufferedImage textImg = null;
	// textImg = createImageFromText(font, fontClr, text, hallign, vallign,
	// panelWt, panelHt, rotAng, shx, shy, isRotFirst);
	// Graphics2D g2d = (Graphics2D) polyImg.createGraphics();
	// Point2D.Double startPt = null;
	// startPt = RGPTUIUtil.getStartPts(g2d, font, text, hallign,
	// vallign, poly);
	// System.out.println("Text Start Point: " + startPt.toString());
	// int startx = (int) startPt.x, starty = (int) startPt.y;
	// g2d.drawImage(textImg, startx, starty, polyPanelWt, polyPanelHt, null);
	// return polyImg;
	// }

	public static Image rotateImage(BufferedImage rotImg, double rotAng,
			Component comp) {
		double radiansPerDegree = Math.PI / 180.0;
		rotAng *= radiansPerDegree;
		// image = this.createImage(width + 8, height);

		// Create an imagefilter to rotate the image.
		ImageFilter filter = new com.rgpt.util.RotateFilter(rotAng);

		// Produce the rotated image.
		ImageProducer producer = new FilteredImageSource(rotImg.getSource(),
				filter);

		// Create the rotated image.
		BufferedImage image = ImageFilterUtils.createImage(producer);
		BufferedImage finalImg = image;
		BufferedImage unSharpImg = null;
		unSharpImg = (new UnsharpFilter()).filter(image, unSharpImg);
		// BufferedImage redNoiseImg = null;
		// redNoiseImg = (new ReduceNoiseFilter()).filter(image, redNoiseImg);
		BufferedImage sharpImg = null;
		sharpImg = (new SharpenFilter()).filter(unSharpImg, sharpImg);
		finalImg = sharpImg;
		finalImg = cloneImage(finalImg, true);
		// Image image = comp.createImage(producer);

		// System.out.println("Image is: " + image.getClass().getName());
		return finalImg;
	}

	public static BufferedImage rotateImage(BufferedImage image, double angle) {
		double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
		int w = image.getWidth(), h = image.getHeight();
		int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math
				.floor(h * cos + w * sin);
		GraphicsConfiguration gc = getDefaultConfiguration();
		BufferedImage result = gc.createCompatibleImage(neww, newh,
				Transparency.TRANSLUCENT);
		Graphics2D g = result.createGraphics();
		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(angle, w / 2, h / 2);
		g.drawRenderedImage(image, null);
		g.dispose();
		return result;
	}

	public static Image rotateImage(BufferedImage inputImage, double rotAng,
			Rectangle bounds, double rtx, double rty) {
		int imgWt = inputImage.getWidth(null), imgHt = inputImage
				.getHeight(null);
		BufferedImage sourceBI = null;
		sourceBI = new BufferedImage(imgWt, imgHt, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) sourceBI.getGraphics();
		g.drawImage(inputImage, 0, 0, null);

		AffineTransform at = new AffineTransform();

		// scale image
		// at.scale(2.0, 2.0);

		// rotate 45 degrees around image center
		// at.rotate(rotAng * Math.PI / 180.0, sourceBI.getWidth()/2.0,
		// sourceBI.getHeight()/2.0);

		// translate to make sure the rotation doesn't cut off any image data
		// AffineTransform translationTransform;
		// translationTransform = findTranslation(at, sourceBI);
		// at.preConcatenate(translationTransform);

		// instantiate and apply affine transformation filter

		// at = RGPTUtil.getRotationTransform(new Rectangle(imgWt, imgHt), at,
		// -rotAng);
		// at = RGPTUtil.getRotationTransform(bounds, at, -rotAng, rtx, rty);
		at = RGPTUtil.getRotationTransform(bounds, imgWt, imgHt, at, -rotAng,
				rtx, rty);
		BufferedImageOp bio = new AffineTransformOp(at,
				AffineTransformOp.TYPE_BILINEAR);

		BufferedImage destinationBI = null;
		destinationBI = bio.filter(sourceBI, null);
		// destinationBI = new BufferedImage(imgWt, imgHt,
		// BufferedImage.TYPE_INT_ARGB);
		// Graphics2D g2d = (Graphics2D) destinationBI.getGraphics();
		// g2d.setTransform(at);
		// g2d.drawImage(sourceBI, 0, 0, null);
		return destinationBI;
	}

	// find proper translations to keep rotated image correctly displayed
	private static AffineTransform findTranslation(AffineTransform at,
			BufferedImage bi) {
		Point2D p2din, p2dout;

		p2din = new Point2D.Double(0.0, 0.0);
		p2dout = at.transform(p2din, null);
		double ytrans = p2dout.getY();

		p2din = new Point2D.Double(0, bi.getHeight());
		p2dout = at.transform(p2din, null);
		double xtrans = p2dout.getX();

		AffineTransform tat = new AffineTransform();
		tat.translate(-xtrans, -ytrans);
		return tat;
	}

	public static BufferedImage cloneImage(BufferedImage image,
			boolean isTextEmb) {
		BufferedImage newImage = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		RenderingHints rh = null;
		if (isTextEmb) {
			rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			rh.put(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			rh.put(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			// rh.put(RenderingHints.KEY_DITHERING,
			// RenderingHints.VALUE_DITHER_ENABLE);
			rh.put(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE);
		} else {
			rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			rh.put(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			// rh.put(RenderingHints.KEY_DITHERING,
			// RenderingHints.VALUE_DITHER_ENABLE);
			rh.put(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE);
		}
		g.setRenderingHints(rh);
		g.drawRenderedImage(image, null);
		g.dispose();
		return newImage;
	}

	public static Rectangle drawImageInPolygon(Graphics g2d, BufferedImage img,
			Polygon poly, double xfactor, double yfactor) {
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
		int panelWt = maxXPt - minXPt, panelHt = maxYPt - minYPt;
		// System.out.println("MinXPt: " + minXPt + " MinYPt: " + minYPt);
		// System.out.println("MaxXPt: " + maxXPt + " MaxYPt: " + maxYPt);
		// System.out.println("Width: " + panelWt + " Height: " + panelHt);

		Rectangle bounds = poly.getBounds();
		panelWt = bounds.width;
		panelHt = bounds.height;
		// System.out.println("Poly Bounds: " + bounds.toString());

		panelWt = xPts[1] - xPts[0];
		panelHt = yPts[2] - yPts[1];
		// System.out.println("New Width: " + panelWt + " Height: " + panelHt);

		int x = xPts[0] + (int) (panelWt * xfactor);
		int y = yPts[0] + (int) (panelHt * yfactor);
		// System.out.println("xFact: " + xfactor + " yFact: " + yfactor);
		// System.out.println("Poly Wt: " + panelWt + " Ht: " + panelHt + " x: "
		// + x + " y: " + y);
		g2d.drawImage(img, x, y, null);
		Rectangle imgRect = new Rectangle(x, y, img.getWidth(), img.getHeight());
		// System.out.println("Image: " + img.toString());
		// System.out.println("Image Rect: " + imgRect.toString());
		return imgRect;
	}

	public static Rectangle drawImageInRect(Graphics g2d, BufferedImage img,
			Rectangle rect, double xfactor, double yfactor) {
		int panelWt = rect.width;
		int panelHt = rect.height;
		// System.out.println("Poly Bounds: " + bounds.toString());

		int x = rect.x + (int) (panelWt * xfactor);
		int y = rect.y + (int) (panelHt * yfactor);
		g2d.drawImage(img, x, y, null);
		Rectangle imgRect = new Rectangle(x, y, img.getWidth(), img.getHeight());
		return imgRect;
	}

	public static Rectangle drawImageInRect(Graphics g2d, BufferedImage img,
			Rectangle rect, double xfactor, double yfactor,
			AffineTransform affine) {
		int panelWt = rect.width;
		int panelHt = rect.height;
		// System.out.println("Poly Bounds: " + bounds.toString());
		Point2D.Double[] pt = new Point2D.Double[1];
		Point2D.Double[] transPt = new Point2D.Double[1];

		double x = rect.x + panelWt * xfactor;
		double y = rect.y + panelHt * yfactor;
		pt[0] = new Point2D.Double(x, y);
		affine.transform(pt, 0, transPt, 0, 1);
		g2d.drawImage(img, (int) transPt[0].x, (int) transPt[0].y, null);
		Rectangle imgRect = new Rectangle((int) transPt[0].x,
				(int) transPt[0].y, img.getWidth(), img.getHeight());
		return imgRect;
	}

	public static BufferedImage createImageFromRect(Rectangle rect,
			int fillColor, double rotAng, double shx, double shy,
			boolean isRotFirst) {
		BufferedImage rectImg = new BufferedImage(rect.width, rect.height,
				COLORTYPE);
		Graphics2D g2d = (Graphics2D) rectImg.createGraphics();
		AffineTransform affine = g2d.getTransform();
		g2d.setColor(new java.awt.Color(fillColor));
		GeneralPath gPath = RGPTUtil.getTransformRect(affine, rect, rotAng,
				shx, shy, isRotFirst);
		g2d.fill(gPath);
		return rectImg;
	}

	public static BufferedImage ConvToTransparentImage(BufferedImage src,
			float alpha) {
		// System.out.println("Conv to Transperent Image, apha value: " +
		// alpha);
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dest.createGraphics();
		int rule = AlphaComposite.SRC_OVER;
		AlphaComposite ac = AlphaComposite.getInstance(rule, alpha);
		g2.setComposite(ac);
		g2.drawImage(src, null, 0, 0);
		g2.dispose();
		return dest;
	}

	// This generates the file name with out extension and saves the image with
	// the
	// generated name
	public static void SaveImage(BufferedImage bimg, String filename,
			String outformat) {
		System.out.println(filename);
		int index = filename.lastIndexOf('.');
		filename = filename.substring(0, index);
		System.out.println(filename);
		filename = filename + "-out." + outformat;
		SaveImageToFile(bimg, filename, outformat);
	}

	public static void SaveImageToFile(Image img, String filename,
			String outformat) {
		BufferedImage bimg = ScaleToSize(img, -1, -1);
		if (outformat.equalsIgnoreCase("JPEG")
				|| outformat.equalsIgnoreCase("JPG"))
			SaveJPEG(bimg, new File(filename));
		else
			SaveImageToFile(bimg, filename, outformat);
	}

	public static void SaveImageToFile(BufferedImage bimg, String filename,
			String outformat) {
		File f = new File(filename);
		if (outformat.equalsIgnoreCase("JPEG")
				|| outformat.equalsIgnoreCase("JPG"))
			SaveJPEG(bimg, f);
		else
			SaveImageToFile(bimg, f, outformat);
	}

	public static void SaveImageToFile(BufferedImage bimg, File f,
			String outformat) {
		if (outformat.equalsIgnoreCase("JPEG")
				|| outformat.equalsIgnoreCase("JPG"))
			SaveJPEG(bimg, f);
		try {
			ImageIO.write(bimg, outformat, f);
		} catch (Exception e) {
			System.out.println("SaveImage");
			e.printStackTrace();
		}
	}

	public static void SaveJPEG(BufferedImage bimg, File file) {
		// *********************** For JPEG output files, this code is required.
		AffineTransform s = new AffineTransform();
		AffineTransformOp sop = new AffineTransformOp(s,
				AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		BufferedImage jpgimage = new BufferedImage(bimg.getWidth(),
				bimg.getHeight(), BufferedImage.TYPE_INT_RGB);
		sop.filter(bimg, jpgimage);
		// *********************************************************
		Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter writer = (ImageWriter) iter.next();

		// instantiate an ImageWriteParam object with default compression
		// options

		ImageWriteParam iwp = writer.getDefaultWriteParam();

		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(1); // an integer between 0 and 1
		// 1 specifies minimum compression and maximum quality

		try {
			FileImageOutputStream output = new FileImageOutputStream(file);
			writer.setOutput(output);
			IIOImage image = new IIOImage(jpgimage, null, null);
			writer.write(null, image, iwp);
			output.close();
		} catch (Exception e) {
			System.out.println("SaveJPEG");
			e.printStackTrace();
		}
	}

	public static BufferedImage getPrintableImage(ImageHolder imgHldr,
			int printDPI) throws Exception {
		System.out.println("In getPrintableImage. Print DPI: " + printDPI);
		String imageabspath = imgHldr.m_SrcDir + imgHldr.m_FileName;
		BufferedImage bimg = LoadImage(imageabspath);
		BufferedImage buffImg = bimg;
		Rectangle clipRect = imgHldr.m_ClipRectangle;
		if (imgHldr.m_IsClipped && clipRect != null) {
			buffImg = bimg.getSubimage((int) clipRect.getX(),
					(int) clipRect.getY(), (int) clipRect.getWidth(),
					(int) clipRect.getHeight());
		}
		// Get the DPI of the Sub Image. If More then the Printable DPI
		// needed, then reduction is done to the Printable DPI
		double w = imgHldr.m_ImageBBox.width / 72;
		double h = imgHldr.m_ImageBBox.height / 72;
		int dpi = FindResolution(buffImg, w, h);
		System.out.println("Initial DPI of the Image: " + dpi);
		// if the Image dpi is less then IMAGE_DIAPLAY_DPI then the image
		// will not be down sampled
		if (dpi > printDPI)
			buffImg = DownSampleImage(buffImg, w, h, printDPI);
		dpi = FindResolution(buffImg, w, h);
		System.out.println("New DPI of the Image: " + dpi);
		return buffImg;
	}

	public static byte[] getImageStream(BufferedImage bimg, String outformat) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ImageIO.write(bimg, outformat, byteStream);
			return byteStream.toByteArray();
		} catch (Exception e) {
			System.out.println("SaveImage");
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static BufferedImage getSelectedImage(BufferedImage dispimg,
			int startx, int starty, int lastx, int lasty, double scalex,
			double scaley, Rectangle rect) {
		int x, y, w, h;
		x = Math.min(startx, lastx);
		y = Math.min(starty, lasty);
		w = Math.abs(startx - lastx);
		h = Math.abs(starty - lasty);

		int origx = (int) (x / scalex);
		int origy = (int) (y / scaley);
		int origw = (int) (w / scalex);
		int origh = (int) (h / scaley);

		try {
			BufferedImage buffImg = dispimg.getSubimage(x, y, w, h);
			rect.setBounds(origx, origy, origw, origh);
			return buffImg;
		} catch (Exception ex) {
			// ex.printStackTrace();
			return null;
		}
	}

	public static BufferedImage getSelectedImage(BufferedImage origimg,
			BufferedImage scimg, int startx, int starty, int lastx, int lasty,
			int start_offsetx, int start_offsety, Rectangle rect) {
		int x, y, w, h;
		x = Math.min(startx, lastx);
		y = Math.min(starty, lasty);
		w = Math.abs(startx - lastx);
		h = Math.abs(starty - lasty);

		// System.out.println("Sel x: " + x + " y: " + y +
		// " w: " + w + " h: " + h);

		int scwd = scimg.getWidth();
		int scht = scimg.getHeight();
		int origwd = origimg.getWidth();
		int oright = origimg.getHeight();

		System.out.println("Sc Wd: " + scwd + " Sc Ht: " + scht + " OrWd: "
				+ origwd + " OrHt: " + oright);
		double scalex = 0.0, scaley = 0.0;

		scalex = (double) origwd / (double) scwd;
		scaley = (double) oright / (double) scht;

		System.out.println("Scale X: " + scalex + " scaley: " + scaley);

		// start_offsetx = 5; start_offsety = 5;
		int image_startx = startx - start_offsetx;
		int image_starty = starty - start_offsety;

		int origx = (int) (image_startx * scalex);
		int origy = (int) (image_starty * scaley);
		int origw = (int) (w * scalex);
		int origh = (int) (h * scaley);

		System.out.println("Orig x: " + origx + " y: " + origy + " w: " + origw
				+ " h: " + origh);

		try {
			BufferedImage buffImg = origimg.getSubimage(origx, origy, origw,
					origh);
			rect.setBounds(origx, origy, origw, origh);
			return buffImg;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static BufferedImage getSelectedImage(BufferedImage bimg,
			int startx, int starty, int lastx, int lasty, int start_offsetx,
			int start_offsety) {
		return getSelectedImage(bimg, startx, starty, lastx, lasty,
				start_offsetx, start_offsety, new Rectangle());
	}

	public static BufferedImage getSelectedImage(BufferedImage bimg,
			int startx, int starty, int lastx, int lasty, int start_offsetx,
			int start_offsety, Rectangle rect) {
		int x, y, w, h;
		x = Math.min(startx, lastx);
		y = Math.min(starty, lasty);
		w = Math.abs(startx - lastx);
		h = Math.abs(starty - lasty);

		int wd = bimg.getWidth();
		int ht = bimg.getHeight();

		// start_offsetx = 0; start_offsety = 0;
		int image_startx = x - start_offsetx;
		int image_starty = y - start_offsety;

		try {
			BufferedImage buffImg = bimg.getSubimage(image_startx,
					image_starty, w, h);
			rect.setBounds(image_startx, image_starty, w, h);
			return buffImg;
		} catch (Exception ex) {
			return null;
		}
	}

	public static BufferedImage loadTranslucentImage(BufferedImage loaded,
			float transperancy) {
		// Create the image using the
		System.out.println("Height, width " + loaded.getWidth() + " - "
				+ loaded.getHeight());
		BufferedImage aimg = new BufferedImage(loaded.getWidth(),
				loaded.getHeight(), BufferedImage.TRANSLUCENT);
		// Get the images graphics
		Graphics2D g = aimg.createGraphics();
		// Set the Graphics composite to Alpha
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				transperancy));
		// Draw the LOADED img into the prepared reciver image
		g.drawImage(loaded, null, 0, 0);
		// let go of all system resources in this Graphics
		g.dispose();
		// Return the image
		return aimg;
	}

	public static BufferedImage changeColor(BufferedImage image, Color color,
			Color replacement_color) {

		BufferedImage dimg = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dimg.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(image, null, 0, 0);
		g.dispose();
		for (int i = 0; i < dimg.getHeight(); i++) {
			for (int j = 0; j < dimg.getWidth(); j++) {
				if (dimg.getRGB(j, i) == color.getRGB()) {
					dimg.setRGB(j, i, replacement_color.getRGB());
				}
			}
		}
		return dimg;
	}

	public static BufferedImage horizontalflip(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(w, h, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
		g.dispose();
		return dimg;
	}

	public static BufferedImage verticalflip(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(w, h, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
		g.dispose();
		return dimg;
	}

	public static BufferedImage rotate(BufferedImage img, int angle) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(w, h, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.rotate(Math.toRadians(angle), w / 2, h / 2);
		g.drawImage(img, null, 0, 0);
		return dimg;
	}

	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}

	public static float calculate_fontsize(float startsize, float endsize,
			int character_pos, int no_of_characters) {
		int direction = 0;
		float steps = 0;
		float fontsize = 0;

		if (startsize > endsize)
			direction = 1;
		else if (startsize < endsize)
			direction = -1;
		else
			direction = 0;

		steps = (Math.abs(startsize - endsize)) / no_of_characters;
		if (direction == 1)
			fontsize = (startsize - steps * character_pos);
		if (direction == -1)
			fontsize = (startsize + steps * character_pos);
		if (direction == 0)
			fontsize = startsize;

		return (fontsize);
	}

	public static int FindResolution(BufferedImage img, double print_width,
			double print_height) {
		int img_width = img.getWidth();
		int img_height = img.getHeight();
		System.out.println("Buffered Image Width: " + img_width + " Image Ht: "
				+ img_height);
		int ppi1 = (int) ((double) img_width / (double) print_width);
		int ppi2 = (int) ((double) img_height / (double) print_height);
		int ppi = Math.min(ppi1, ppi2);
		System.out.println(" PPi1 " + ppi1 + " PPi2 " + ppi2 + " PPi " + ppi);
		return (ppi);
	}

	public static int FindResolution(int origImgWt, int origImgHt,
			double print_width, double print_height) {
		System.out.println("Original Image Width: " + origImgWt + " Ht: "
				+ origImgHt);
		int ppi1 = (int) ((double) origImgWt / (double) print_width);
		int ppi2 = (int) ((double) origImgHt / (double) print_height);
		int ppi = Math.min(ppi1, ppi2);
		System.out.println(" PPi1 " + ppi1 + " PPi2 " + ppi2 + " PPi " + ppi);
		return (ppi);
	}

	public static HashMap getImageSize4PPI(int ppi, double print_width,
			double print_height) {
		int img_width = (int) (ppi * print_width);
		int img_height = (int) (ppi * print_height);
		System.out.println("In getImageSize4PPI Width: " + img_width + " Ht: "
				+ img_height);
		HashMap imgSize = new HashMap();
		imgSize.put("Width", img_width);
		imgSize.put("Height", img_height);
		return imgSize;
	}

	public static BufferedImage DownSampleImage(BufferedImage img,
			double print_width, double print_height, int ppi) {
		int resolution = FindResolution(img, print_width, print_height);
		if (resolution <= ppi) {
			System.out.println(" Downsampling not required. Resolution is: "
					+ resolution + " Requested PPI: " + ppi);
			return (img);
		}
		int img_width = img.getWidth();
		int img_height = img.getHeight();
		double aspect = (double) img_width / (double) img_height;
		int pixel_height = (int) (ppi * print_height);
		int pixel_width = (int) (aspect * pixel_height);
		// int pixel_height = (int)(ppi * print_height) ;
		System.out.println(" pixel wd ht " + pixel_width + " " + pixel_height);
		System.out.println(" Image wd ht " + img_width + " " + img_height);
		// ImageIcon imgicon = new ImageIcon((java.awt.Image)img);
		// java.awt.Image scaledimage =
		// imgicon.getImage().getScaledInstance(pixel_width, pixel_height,
		// Image.SCALE_AREA_AVERAGING);
		java.awt.Image scaledimage = img.getScaledInstance(pixel_width,
				pixel_height, Image.SCALE_AREA_AVERAGING);
		if (scaledimage == null) {
			System.out.println("Null scaledimage");
			return (null);
		}
		BufferedImage bufimg = new BufferedImage(pixel_width, pixel_height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufimg.createGraphics();
		g.drawImage(scaledimage, 0, 0, null);
		g.dispose();
		return (bufimg);
	}

	// TODO scaleDown()
	public static BufferedImage scaleImage(BufferedImage origImg,
			Dimension dispDim) {
		int imgWt = origImg.getWidth(), imgHt = origImg.getHeight();
		double dispWt = 0, dispHt = 0;
		double aspectRatio = ((double) imgHt / (double) imgWt);
		System.out.println("Orig Img Wt: " + imgWt + " ImgHt: " + imgHt
				+ " AspectRat: " + aspectRatio);
		if (imgWt < imgHt) {
			dispWt = dispDim.width;
			dispHt = dispWt * aspectRatio;
			if (dispHt > dispDim.height) {
				dispHt = dispDim.height;
				dispWt = dispHt / aspectRatio;
			}
		} else {
			dispHt = dispDim.height;
			dispWt = dispHt / aspectRatio;
			System.out.println("Inside DispWt: " + dispWt + " dispHt: "
					+ dispHt);
			if (dispWt > dispDim.width) {
				dispWt = dispDim.width;
				dispHt = dispWt * aspectRatio;
				;
				System.out.println("Inside2 DispWt: " + dispWt + " dispHt: "
						+ dispHt);
			}
		}

		System.out.println("DispWt: " + dispWt + " dispHt: " + dispHt);
		return ScaleToSize(origImg, (int) dispWt, (int) dispHt);
	}

	public static BufferedImage scaleImage(BufferedImage bi, int img_width,
			int img_height, boolean aspectratio) {
		return scaleImage(bi, img_width, img_height, aspectratio, true);
	}

	public static BufferedImage scaleImage(BufferedImage bi, int img_width,
			int img_height, boolean aspectratio,
			boolean useTransparencyImageType) {
		double scal_wd = 0, scal_ht = 0;
		double origimgwidth = bi.getWidth();
		double origimgheight = bi.getHeight();
		if (img_width == -1 && img_height == -1) {
			RGPTLogger
					.logToFile("Atlease scaled image width or image ht should be mentioned");
			return null;
		}
		if (!aspectratio) {
			scal_wd = img_width;
			scal_ht = img_height;
		} else {
			if (origimgwidth > origimgheight) {
				if (img_width != -1) {
					scal_wd = img_width;
					scal_ht = scal_wd * (origimgheight / origimgwidth);
				} else {
					scal_ht = img_height;
					scal_wd = scal_ht * (origimgwidth / origimgheight);
				}
			} else {
				if (img_height != -1) {
					scal_ht = img_height;
					scal_wd = scal_ht * (origimgwidth / origimgheight);
				} else {
					scal_wd = img_width;
					scal_ht = scal_wd * (origimgheight / origimgwidth);
				}
			}
		}
		System.out.println("scal_wd n scal_ht is :-  " + (int) scal_wd + ","
				+ (int) scal_ht + "inside if....");
		int type = BufferedImage.TYPE_INT_ARGB;
		if (!useTransparencyImageType)
			type = BufferedImage.TYPE_INT_RGB;
		BufferedImage img = new BufferedImage((int) scal_wd, (int) scal_ht,
				type);
		Graphics2D g2d = (Graphics2D) img.createGraphics();
		g2d.drawImage(bi, 0, 0, (int) scal_wd, (int) scal_ht, null);
		g2d.dispose();
		return img;
	}

	public static int getImageType(Image image) {
		if (image instanceof BufferedImage)
			return ((BufferedImage) image).getType();
		boolean hasAlpha = hasAlpha(image);
		int type = BufferedImage.TYPE_INT_RGB;
		if (hasAlpha)
			type = BufferedImage.TYPE_INT_ARGB;
		return type;
	}

	public static Image makeColorTransparent(Image im, final Color color) {
		ImageFilter filter = new RGBImageFilter() {
			// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	public static BufferedImage compressImage(BufferedImage image, float quality)
			throws Exception {
		if (hasAlpha(image))
			return image;
		if (quality < 0)
			quality = 0.5f;
		// byte[] imgStr = getImageStream(image, "jpeg");
		// RGPTLogger.logToFile("Original Image Sz: "+imgStr.length);
		// Get a ImageWriter for jpeg format.
		ImageWriter writer = getJPEGImageWriter();
		// Create the ImageWriteParam to compress the image.
		ImageWriteParam param = createImageWriteParam(writer, quality);
		// The output will be a ByteArrayOutputStream (in memory)
		ByteArrayOutputStream bos = new ByteArrayOutputStream(32768);
		ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
		writer.setOutput(ios);
		writer.write(null, new IIOImage(image, null, null), param);
		ios.flush(); // otherwise the buffer size will be zero!
		// From the ByteArrayOutputStream create a RenderedImage.
		ByteArrayInputStream in = new ByteArrayInputStream(bos.toByteArray());
		BufferedImage bimg = ImageIO.read(in);
		int size = bos.toByteArray().length;
		System.out.println("Compressed Image Size: " + size);
		return bimg;
	}

	// Create the ImageWriteParam to compress the image.
	public static ImageWriteParam createImageWriteParam(ImageWriter writer,
			float quality) {
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality);
		return param;
	}

	public static ImageWriter getJPEGImageWriter() {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
		if (!writers.hasNext())
			throw new IllegalStateException("No writers found");
		ImageWriter writer = (ImageWriter) writers.next();
		return writer;
	}

	// Compresses and writes Image to File
	public static void saveAsJPEG(BufferedImage image, String fileName,
			float quality) throws IOException {
		File file = new File(fileName);
		FileImageOutputStream output = new FileImageOutputStream(file);
		ImageWriter writer = getJPEGImageWriter();
		ImageWriteParam param = createImageWriteParam(writer, quality);
		writer.setOutput(output);
		writer.write(null, new IIOImage(image, null, null), param);
	}

	public static BufferedImage fillTransparentPixels(BufferedImage image,
			Color fillColor) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage image2 = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image2.createGraphics();
		g.setColor(fillColor);
		g.fillRect(0, 0, w, h);
		g.drawRenderedImage(image, null);
		g.dispose();
		return image2;
	}

	// This method returns true if the specified image has transparent pixels
	public static boolean hasAlpha(Image image) {
		// Get the image's color model
		ColorModel cm = getColorModel(image);
		// System.out.println("Color Model Class: "+(cm.getClass().getName())+" Data: "+cm);
		return cm.hasAlpha();
	}

	public static ColorModel getColorModel(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel();
		}

		// Use a pixel grabber to retrieve the image's color model, grabbing a
		// single
		// pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm;
	}

	public static BufferedImage fadeImage(BufferedImage origImage,
			float fadePerc) {
		// Fade Type is UNIFORM_FADE, BOX_FADE, FIXED_REGION_FADE
		// shapeType - RECTANGULAR_SHAPE, ELLIPTICAL_SHAPE
		BufferedImage fadeImg = fadeImage(origImage, fadePerc, false,
				"UniformFadeFilter", 1);
		// Fade Type is FULL_FADE, VERTICAL_FADE, HORIZONTAL_FADE, DIAGONAL_FADE
		// BufferedImage fadeImg = linearFadeImage(origImage, fadePerc, 0);

		// SRC_ATOP - The part of the source lying inside of the destination is
		// composited onto the destination - DOES NOT WORK
		// SRC_IN - The part of the source lying inside of the destination
		// replaces the destination - DOES NOT WORK
		// SRC_OUT - The part of the source lying outside of the destination
		// replaces the destination - WORKS
		// SRC_OVER - The source is composited over the destination - WORKS
		// SRC_OVER, SRC_OUT produce the same output. There output is also same
		// as the above FadeImage method.
		// Using AlphaComposite is very slower and hence commented
		// BufferedImage fadeImg = fadeImage(origImage, fadePerc,
		// AlphaComposite.SRC_OVER);
		return fadeImg;
	}

	public static BufferedImage fadeImage(BufferedImage origImage,
			float fadePerc, int rule) {
		BufferedImage img = createImageCopy(origImage, true);
		int imgWt = origImage.getWidth(), imgHt = origImage.getHeight();
		int fadeRegions = RGPTParams.getIntVal("NumOfFadeRegion");
		double x = 0.0, y = 0.0, w = 0.0, h = 0.0; // int fadeIter = 0, i = 0;
		double dx = 0.0, dy = 0.0, fadeIterPerc = 0.0;
		double fadeWt = fadePerc * imgWt, fadeHt = fadePerc * imgHt;
		double newWt = (double) imgWt - fadeWt, newHt = (double) imgHt - fadeHt;
		double dxFade = fadeWt / 2.0D, dyFade = fadeHt / 2.0D;
		// Creating Faded Regions
		Vector<Shape> fadeRect = new Vector<Shape>(fadeRegions);
		Map<Integer, Double> fadePerc4Rect = new HashMap<Integer, Double>();
		Rectangle2D.Double newImgRect = new Rectangle2D.Double(dxFade, dyFade,
				newWt, newHt);
		Rectangle2D.Double newFadeRect = null, prevFadeRect = null, clipRect = null;
		Vector<BufferedImage> fadedImages = new Vector<BufferedImage>();
		String pathSel = RGPTActionListener.ImageActions.SHAPE_LINE_PATH_MENU
				.toString();
		GeneralPath gPath = null;
		Vector<GeneralPath> fadePath = new Vector<GeneralPath>();
		// RGPTLogger.logToFile("imgWt: "+imgWt+" imgHt: "+imgHt+" fadeWt: "+fadeWt+" fadeHt: "+fadeHt);
		// RGPTLogger.logToFile("fadePerc: "+fadePerc+" Non Faded Rect: "+newImgRect);
		int fadeIter = fadeRegions;
		int i = 0;
		while (fadeIter != 0) {
			fadeIterPerc = (double) fadeIter / (double) fadeRegions;
			x = fadeIterPerc * dxFade;
			y = fadeIterPerc * dyFade;
			w = (double) imgWt - 2 * fadeIterPerc * dxFade;
			h = (double) imgHt - 2 * fadeIterPerc * dyFade;
			if (fadeIter == 1) {
				x = 0.0;
				y = 0.0;
				w = (double) imgWt;
				h = (double) imgHt;
			}
			fadeRect.addElement(new Rectangle2D.Double(x, y, w, h));
			// RGPTLogger.logToFile("Fade Iter: "+fadeIter+" fadeIterPerc: "+fadeIterPerc+
			// " FadeRect: "+(new Rectangle2D.Double(x, y, w, h)).toString());
			fadeIter--;
		}

		int[][][] pixel_rgb = BufferedImagetoPixel3DRGB(origImage);
		int[][][][] fadeImagePix = new int[fadeRegions][imgHt][imgWt][4];
		for (int irow = 0; irow < imgHt; irow++) {
			for (int icol = 0; icol < imgWt; icol++) {
				fadeIter = RGPTUtil.contains(fadeRect, (double) icol,
						(double) irow);
				if (fadeIter == -1)
					continue;
				// RGPTLogger.logToFile("fadeIter 1: "+fadeIter);
				fadeIter = fadeRegions - fadeIter;
				fadeIterPerc = (double) fadeIter / (double) fadeRegions;
				fadePerc4Rect.put(fadeIter, fadeIterPerc);
				for (i = 0; i < fadeRegions; i++) {
					if (i + 1 != fadeIter) {
						int[] col = pixel_rgb[irow][icol];
						fadeImagePix[i][irow][icol][RED] = (col[RED] << 16) & 0x00FF0000;
						fadeImagePix[i][irow][icol][RED] = (col[GREEN] << 8) & 0x0000FF00;
						fadeImagePix[i][irow][icol][RED] = (col[BLUE]) & 0x000000FF;
						fadeImagePix[i][irow][icol][ALPHA] = ((0 << 24) & 0xFF000000);
					} else
						fadeImagePix[i][irow][icol] = pixel_rgb[irow][icol];
				}
				// if(irow == icol) {
				// RGPTLogger.logToFile("For row: "+irow+" col: "+icol+" fadeIter: "+fadeIter+
				// " Pixel Val: "+fadeImagePix[fadeIter-1][irow][icol]);
				// }
			}
		}
		BufferedImage fadeImg = null;
		for (i = 0; i < fadeRegions; i++) {
			fadeImg = Pixel3DRGBtoBufferedImage(fadeImagePix[i]);
			// displayImage(fadeImg, "FadeImage: "+fadePerc4Rect.get(i+1));
			// RGPTLogger.logToFile("Iter: "+(i+1)+" Fade Img Perc: "+fadePerc4Rect.get(i+1));
			fadedImages.addElement(applyFadeImage(fadeImg,
					fadePerc4Rect.get(i + 1), rule));
		}
		fadeImg = mergeImage(origImage, fadedImages);
		// displayImage(fadeImg, "FadeImage");
		return fadeImg;
	}

	public static BufferedImage applyFadeImage(BufferedImage bimg,
			double alpha, int rule) {
		return fadeImageByShape(bimg, null, alpha, rule);
	}

	public static BufferedImage fadeImageByShape(BufferedImage bimg,
			Shape arbshape, double alpha, int rule) {
		int ht = bimg.getHeight(), wd = bimg.getWidth();
		BufferedImage bimgnew = new BufferedImage(wd, ht, COLORTYPE);
		Graphics2D g2d = (Graphics2D) bimgnew.createGraphics();
		if (arbshape != null)
			g2d.setClip(arbshape);
		AlphaComposite ac = AlphaComposite.getInstance(rule, (float) alpha);
		g2d.setComposite(ac);
		g2d.drawImage(bimg, 0, 0, null);
		g2d.dispose();
		// displayImage(bimgnew, "FadeImage: "+alpha);
		// String fileName = "test_output/fadeImg"+alpha+".png";
		// SaveImageToFile(bimgnew, fileName, "PNG");
		return bimgnew;
	}

	public static BufferedImage mergeImage(BufferedImage origImage,
			Vector<BufferedImage> fadedImages) {
		int ht = origImage.getHeight(), wd = origImage.getWidth();
		BufferedImage fadeImg = new BufferedImage(wd, ht, COLORTYPE), clipFadeImg = null;
		Graphics g = fadeImg.createGraphics();
		for (int i = 0; i < fadedImages.size(); i++) {
			clipFadeImg = fadedImages.elementAt(i);
			g.drawImage(clipFadeImg, 0, 0, wd, ht, null);
		}
		g.dispose();
		// displayImage(fadeImg, "FadeImage");
		return fadeImg;
	}

	// Not Used:- FullFadeFilter, RandomFadeFilter, GaussianRandomFadeFilter
	public static enum LinearFadeType {
		VerticalFadeFilter, HorizontalFadeFilter, DiagonalFadeFilter, FullFadeFilter, RandomFadeFilter, GaussianRandomFadeFilter
	}

	public static BufferedImage linearFadeImage(BufferedImage origImage,
			float fadePerc, int fadeTypeInd) {
		LinearFadeType[] fadeTypes = LinearFadeType.values();
		LinearFadeType fadeType = LinearFadeType.HorizontalFadeFilter;
		if (fadeTypeInd >= 0) {
			int mod = fadeTypeInd % fadeTypes.length;
			fadeType = fadeTypes[mod];
		}
		return linearFadeImage(origImage, fadePerc, fadeType);
	}

	public static BufferedImage linearFadeImage(BufferedImage origImage,
			float fadePerc, String fadeType) {
		return linearFadeImage(origImage, fadePerc,
				LinearFadeType.valueOf(fadeType));
	}

	public static BufferedImage linearFadeImage(BufferedImage origImage,
			float fadePerc, LinearFadeType fadeType) {
		int imgWt = origImage.getWidth(), imgHt = origImage.getHeight();
		double x = 0.0, y = 0.0, w = 0.0, h = 0.0, dx = 0.0, dy = 0.0, fadeIterPerc = 0.0;
		double fadeWt = fadePerc * imgWt, fadeHt = fadePerc * imgHt, pixelDelta = 0.0;
		double newWt = (double) imgWt - fadeWt, newHt = (double) imgHt - fadeHt;
		double dxFade = fadeWt / 2.0D, dyFade = fadeHt / 2.0D, splitFade = (double) imgHt / 2.0D;
		if (fadeType == LinearFadeType.VerticalFadeFilter)
			splitFade = (double) imgWt / 2.0D;
		double delta = Math.sqrt((Math.pow(((double) imgWt / 2.0D), 2))
				+ (Math.pow(((double) imgHt / 2.0D), 2)));
		// Creating Faded Regions
		int[][][] pixel_rgb = BufferedImagetoPixel3DRGB(origImage);
		Random rand = null;
		for (int irow = 0; irow < imgHt; irow++) {
			for (int icol = 0; icol < imgWt; icol++) {
				// RGPTLogger.logToFile("fadeIter 1: "+fadeIter);
				// RGPTLogger.logToFile("fadeIter 2: "+fadeIter);
				if ((irow > dyFade && icol > dxFade)
						&& (irow < (dyFade + newHt) && icol < (dxFade + newWt)))
					continue;
				int color = origImage.getRGB(icol, irow);
				int alpha = color >> 24 & 0xFF; // Alpha
				if (fadeType == LinearFadeType.HorizontalFadeFilter) {
					if (irow < splitFade)
						fadeIterPerc = irow / splitFade;
					else if (irow > splitFade)
						fadeIterPerc = 1 - ((double) irow - splitFade)
								/ splitFade;
				} else if (fadeType == LinearFadeType.VerticalFadeFilter) {
					if (icol < splitFade)
						fadeIterPerc = icol / splitFade;
					else if (icol > splitFade)
						fadeIterPerc = 1 - ((double) icol - splitFade)
								/ splitFade;
				} else if (fadeType == LinearFadeType.DiagonalFadeFilter) {
					pixelDelta = Math
							.sqrt((Math.pow(((double) icol / 2.0D), 2))
									+ (Math.pow(((double) irow / 2.0D), 2)));
					if (pixelDelta < delta)
						fadeIterPerc = pixelDelta / delta;
					else
						fadeIterPerc = 1 - (pixelDelta - delta) / delta;
				} else if (fadeType == LinearFadeType.FullFadeFilter)
					fadeIterPerc = 0.0;
				else if (fadeType == LinearFadeType.RandomFadeFilter) {
					if (rand == null)
						rand = new Random();
					fadeIterPerc = rand.nextDouble();
				} else if (fadeType == LinearFadeType.GaussianRandomFadeFilter) {
					if (rand == null)
						rand = new Random();
					fadeIterPerc = rand.nextGaussian();
				}
				pixel_rgb[irow][icol][ALPHA] = (int) Math.round(alpha
						* fadeIterPerc);
			}
		}
		return Pixel3DRGBtoBufferedImage(pixel_rgb, true, true);
	}

	public static enum FadeType {
		UniformFadeFilter, BoxFadeFilter, FixedRegionFadeFilter
	}

	public static enum ShapeType {
		RECTANGULAR_SHAPE, ELLIPTICAL_SHAPE
	}

	public static Vector<String> getShapes(String userType) {
		Vector<String> shapeTypes = RGPTUtil.createVector(RGPTUtil
				.enumToStringArray(ShapeType.values()));
		String[] shapeFiles = RGPTUtil.getShapeFiles(userType);
		shapeTypes.addAll(RGPTUtil.createVector(shapeFiles));
		return shapeTypes;
	}

	public static GeneralPath getShape(HashMap<String, Object> shapeTypes,
			double wt, double ht, double x, double y) {
		Vector<Point2D.Double> gPathPoints = null;
		GeneralPath gPath = null;
		gPath = getGraphicPath(shapeTypes, null);
		if (gPath == null)
			throw new RuntimeException("Unable to create initial Shape");
		Rectangle pathBounds = gPath.getBounds();
		// RGPTLogger.logToFile("Original PathBounds: "+pathBounds+" Fit wt:"+wt+" Ht: "+ht+
		// " x: "+x+" y: "+y);
		double sx = wt / (double) pathBounds.width;
		double sy = 1.0;
		if (pathBounds.height > 0)
			sy = ht / (double) pathBounds.height;
		// RGPTLogger.logToFile("ScaleX: "+sx+" sy: "+sy);
		if (sy > sx)
			sy = sx;
		else
			sx = sy;
		AffineTransform affine = RGPTUtil.getAffineTransform(null, pathBounds,
				sx, sy, 0);
		gPathPoints = (Vector<Point2D.Double>) shapeTypes.get("PathPoints");
		gPath = getGraphicPath(shapeTypes, affine);
		if (gPath == null)
			throw new RuntimeException("Unable to create Scaled Shape");
		gPathPoints = (Vector<Point2D.Double>) shapeTypes.get("PathPoints");
		if (gPathPoints == null)
			throw new RuntimeException("Unable to create Scaled Path Pts");
		// RGPTLogger.logToFile("GPath Pts After Scaling: "+ gPathPoints);
		int tx = (int) x, ty = (int) y;
		pathBounds = gPath.getBounds();
		// RGPTLogger.logToFile("Scaled PathBounds: "+pathBounds);
		Point2D.Double[] points = gPathPoints.toArray(new Point2D.Double[0]);
		tx = tx - pathBounds.x;
		ty = ty - pathBounds.y;
		// RGPTLogger.logToFile("New Traslation tx: "+tx+" ty: "+ty);
		RGPTUtil.adjustPt(affine, points, tx, ty);
		gPath = getGraphicPath(shapeTypes, null);
		if (gPath == null)
			throw new RuntimeException("Unable to create Translated Shape");
		// RGPTLogger.logToFile("Translated PathBounds: "+gPath.getBounds());
		return gPath;
	}

	public static GeneralPath getGraphicPath(Map<String, Object> shapeTypes,
			AffineTransform affine) {
		Vector<Point2D.Double> gPathPoints = null;
		GeneralPath gPath = null;
		String[] keys = shapeTypes.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].equals("ShapeType") || keys[i].equals("ShapeName"))
				continue;
			String pathSel = keys[i];
			// Drawing the GPath to get the
			// The functions in Shape Util draw the shape using the Graphics and
			// Path Points
			if (StaticFieldInfo.GraphicsPaths.isLinePath(pathSel)) {
				gPathPoints = (Vector<Point2D.Double>) shapeTypes.get(pathSel);
				gPath = RGPTShapeUtil.drawLine(affine, gPathPoints, null);
			} else if (StaticFieldInfo.GraphicsPaths.isQuadPath(pathSel)) {
				gPathPoints = (Vector<Point2D.Double>) shapeTypes.get(pathSel);
				gPath = RGPTShapeUtil.drawQuadCurve(affine, gPathPoints, null);
			} else if (StaticFieldInfo.GraphicsPaths.isCubicPath(pathSel)) {
				gPathPoints = (Vector<Point2D.Double>) shapeTypes.get(pathSel);
				gPath = RGPTShapeUtil
						.drawBezierCurve(affine, gPathPoints, null);
			}
		}
		if (gPathPoints != null)
			shapeTypes.put("PathPoints",
					(Vector<Point2D.Double>) gPathPoints.clone());
		if (gPath != null)
			shapeTypes.put("GPath", gPath);
		return gPath;
	}

	public static BufferedImage fadeImage(BufferedImage origImage,
			float fadePerc, boolean useSetRGB, String fadeType, int shapeTypeInd) {
		Vector<String> shapeTypes = getShapes(RGPTParams
				.getVal("UserType"));
		// RGPTLogger.logToFile("Shape Types: "+shapeTypes);
		String shapeType = ShapeType.RECTANGULAR_SHAPE.toString();
		if (shapeTypeInd >= 0) {
			int mod = shapeTypeInd % shapeTypes.size();
			shapeType = shapeTypes.elementAt(mod);
			// RGPTLogger.logToFile("ShapeTypeInd: "+shapeTypeInd+" Mod: "+mod+
			// " ShapeType: "+shapeType);
		}
		return fadeImage(origImage, fadePerc, useSetRGB,
				FadeType.valueOf(fadeType), shapeType);
	}

	public static BufferedImage fadeImage(BufferedImage origImage,
			float fadePerc, boolean useSetRGB, FadeType fadeType,
			String shapeType) {
		BufferedImage img = null;
		if (useSetRGB)
			img = createImageCopy(origImage, true);
		int imgWt = origImage.getWidth(), imgHt = origImage.getHeight();
		int minFadeRegions = RGPTParams.getIntVal("NumOfFadeRegion");
		int maxFadeRegions = RGPTParams.getIntVal("MaxNumOfFadeRegion");
		float fadeRegionSize = RGPTParams.getFloatVal("FadeRegionSize");
		double x = 0.0, y = 0.0, w = 0.0, h = 0.0, dx = 0.0, dy = 0.0, fadeIterPerc = 0.0;
		double fadeWt = fadePerc * imgWt, fadeHt = fadePerc * imgHt;
		double newWt = (double) imgWt - fadeWt, newHt = (double) imgHt - fadeHt;
		double dxFade = fadeWt / 2.0D, dyFade = fadeHt / 2.0D;
		int fadeRegions = 0;
		if (fadeType == FadeType.UniformFadeFilter) {
			fadeRegions = (int) Math.round(dxFade);
			if (dyFade > dxFade)
				fadeRegions = (int) Math.round(dyFade);
			if (fadeRegions < minFadeRegions)
				fadeRegions = minFadeRegions;
			if (fadeRegions > maxFadeRegions)
				fadeRegions = maxFadeRegions;
		} else if (fadeType == FadeType.BoxFadeFilter) {
			fadeRegions = (int) Math.round(dxFade / fadeRegionSize);
			if (dyFade > dxFade)
				fadeRegions = (int) Math.round(dyFade / fadeRegionSize);
			if (fadeRegions < minFadeRegions)
				fadeRegions = minFadeRegions;
			if (fadeRegions > maxFadeRegions)
				fadeRegions = maxFadeRegions;
		} else
			fadeRegions = minFadeRegions; // Default FIXED_REGION_FADE
		// Creating Faded Regions
		Vector<Shape> fadeShape = new Vector<Shape>(fadeRegions);
		Rectangle2D.Double newImgRect = new Rectangle2D.Double(dxFade, dyFade,
				newWt, newHt);
		// RGPTLogger.logToFile("imgWt: "+imgWt+" imgHt: "+imgHt+" fadeWt: "+fadeWt+" fadeHt: "+fadeHt);
		// RGPTLogger.logToFile("fadePerc: "+fadePerc+" Non Faded Rect: "+newImgRect);
		int fadeIter = fadeRegions;
		HashMap<String, Object> shapeTypeOrig = null, shapeTypeNew = null;
		// RGPTLogger.logToFile("Image Wt: "+imgWt+" Ht: "+imgHt);
		while (fadeIter != 0) {
			boolean useDefShape = false;
			fadeIterPerc = (double) fadeIter / (double) fadeRegions;
			x = fadeIterPerc * dxFade;
			y = fadeIterPerc * dyFade;
			w = (double) imgWt - 2 * fadeIterPerc * dxFade;
			h = (double) imgHt - 2 * fadeIterPerc * dyFade;
			if (fadeIter == 1) {
				x = 0.0;
				y = 0.0;
				w = (double) imgWt;
				h = (double) imgHt;
			}
			if (shapeType.equals(ShapeType.ELLIPTICAL_SHAPE.toString()))
				fadeShape.addElement(new Ellipse2D.Double(x, y, w, h));
			else if (shapeType.equals(ShapeType.RECTANGULAR_SHAPE.toString()))
				fadeShape.addElement(new Rectangle2D.Double(x, y, w, h));
			else {
				// Apply Shape Files
				if (shapeTypeOrig == null) {
					String shapeDir = RGPTUtil
							.getShapesDirectory(RGPTParams
									.getVal("UserType"));
					String serFilePath = shapeDir + "/" + shapeType;
					try {
						shapeTypeOrig = (HashMap<String, Object>) RGPTUtil
								.getSerializeObject(serFilePath);
					} catch (Exception ex) {
						RGPTLogger
								.logToFile("Unable to extract Shape File", ex);
						useDefShape = true;
					}
				}
				shapeTypeNew = (HashMap<String, Object>) shapeTypeOrig.clone();
				try {
					fadeShape.addElement(getShape(shapeTypeNew, w, h, x, y));
				} catch (Exception ex) {
					RGPTLogger.logToFile("Unable to create Shape", ex);
					useDefShape = true;
				}
				if (useDefShape)
					fadeShape.addElement(new Rectangle2D.Double(x, y, w, h));
			}
			// RGPTLogger.logToFile("Fade Iter: "+fadeIter+" fadeIterPerc: "+fadeIterPerc+
			// " fadeShape: "+(new Rectangle2D.Double(x, y, w, h)).toString());
			fadeIter--;
		}

		int[][][] pixel_rgb = null;
		if (!useSetRGB)
			pixel_rgb = BufferedImagetoPixel3DRGB(origImage);
		for (int irow = 0; irow < imgHt; irow++) {
			for (int icol = 0; icol < imgWt; icol++) {
				fadeIter = RGPTUtil.contains(fadeShape, (double) icol,
						(double) irow);
				if (fadeIter == -1)
					fadeIterPerc = 0.0;
				else {
					fadeIter = fadeRegions - fadeIter;
					fadeIterPerc = (double) fadeIter / (double) fadeRegions;
				}
				if (useSetRGB)
					setAlphaValue(img, irow, icol, fadeIterPerc);
				else {
					int color = origImage.getRGB(icol, irow);
					int alpha = color >> 24 & 0xFF; // Alpha
					pixel_rgb[irow][icol][ALPHA] = (int) Math.round(alpha
							* fadeIterPerc);
				}
			}
		}
		if (useSetRGB)
			return img;
		return Pixel3DRGBtoBufferedImage(pixel_rgb, true, true);
	}

	public static Color getTransparentColor(Color inptClr, int percTransarent) {
		int r = inptClr.getRed(), g = inptClr.getGreen(), b = inptClr.getBlue();
		if (percTransarent == -1)
			percTransarent = 100;
		int alpha = (int) 255 * percTransarent / 100;
		// System.out.println("Perc Transperancy: " + percTransarent +
		// " Alpha: " + alpha);
		return new Color(r, g, b, alpha);
	}

	public static void setAlphaValue(BufferedImage paramBufferedImage, int row,
			int col, double paramDouble) {
		int i = -1;
		try {
			i = paramBufferedImage.getRGB(col, row); // Get the ARGB Color Value
		} catch (Throwable localThrowable) {
			System.out.println("Row: " + row + " Col: " + col);
			return;
		}
		int j = i >> 24 & 0xFF; // Alpha
		int k = i >> 16 & 0xFF; // Red
		int m = i >> 8 & 0xFF; // Green
		int n = i & 0xFF; // blue
		// j = (int)(j * paramDouble);
		j = (int) Math.round(paramDouble * j);

		// i = j << 24 & 0xFF000000 | k << 16 & 0xFF0000 | m << 8 & 0xFF00 | n &
		// 0xFF;
		i = ((j << 24) & 0xFF000000) | ((k << 16) & 0x00FF0000)
				| ((m << 8) & 0x0000FF00) | ((n) & 0x000000FF);

		// System.out.println("Setting Alpha Value: "+row+"x"+col+" = "+i);
		paramBufferedImage.setRGB(col, row, i);
	}

	public static int setAlphaValue(int argb, double alpha) {
		int a = argb >> 24 & 0xFF; // Alpha
		int r = argb >> 16 & 0xFF; // Red
		int g = argb >> 8 & 0xFF; // Green
		int b = argb & 0xFF; // blue
		a = (int) Math.round(alpha * a);

		// argb = a << 24 & 0xFF000000 | r << 16 & 0xFF0000 | g << 8 & 0xFF00 |
		// b & 0xFF;
		argb = ((a << 24) & 0xFF000000) | ((r << 16) & 0x00FF0000)
				| ((g << 8) & 0x0000FF00) | ((b) & 0x000000FF);
		return argb;
	}

	public static BufferedImage getTransperentImage(BufferedImage origImage,
			BufferedImage compImg) {
		int i = origImage.getWidth();
		int j = origImage.getHeight();
		// int[][][] origPixels = BufferedImagetoPixel3DRGB(origImage);
		BufferedImage compTransImg = new BufferedImage(i, j,
				BufferedImage.TYPE_INT_ARGB);

		for (int k = 0; k < j; k++) {
			for (int m = 0; m < i; m++) {
				int color = origImage.getRGB(m, k);
				int compColor = compImg.getRGB(m, k);
				// The result is also a value ranging from 0 (completely
				// transparent) to 255 (completely opaque).
				int alpha = (color >> 24) & 0xff;
				if (alpha == 0) {
					byte alpha_byte = 0; // Range will 0 to 255
					setAlphaValue(compTransImg, k, m, 0.0D);
				} else
					compTransImg.setRGB(m, k, compColor);
			}
		}
		displayImage(compTransImg, "Test");
		return compTransImg;
	}

	public static void setAlpha(BufferedImage img, byte alpha) {
		alpha %= 0xff;
		for (int cx = 0; cx < img.getWidth(); cx++) {
			for (int cy = 0; cy < img.getHeight(); cy++) {
				int color = img.getRGB(cx, cy);
				int mc = (alpha << 24) | 0x00ffffff;
				int newcolor = color & mc;
				img.setRGB(cx, cy, newcolor);

			}
		}
	}

	// If findLightColor is set to true, then Find a Dark Color Pixels and
	// Convert it into Light Color. If the Light Color
	// Pixel is unique and does not exist in the Image Raster, that Color is
	// returned.
	public static Color getUniqueColor(BufferedImage origImg,
			boolean findLightColor) {
		BufferedImage img = createImageCopy(origImg);
		Color pixelColor = Color.WHITE;
		Color uniqueColor = Color.WHITE;
		int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer())
				.getData();
		Arrays.sort(pixels);
		int iter = 0;
		// System.out.println("Pixel Length: "+pixels.length);
		for (int i = 0; i < pixels.length; i++) {
			iter++;
			int pixel = pixels[i];
			int alpha = pixel >> 24 & 0xFF; // Alpha
			if (alpha < 200)
				continue;
			if (findLightColor) {
				if (isLightColor(pixel))
					continue;
			} else if (isDarkColor(pixel))
				continue;
			pixelColor = new Color(pixel);
			uniqueColor = getInverseColor(pixelColor);
			if (findLightColor) {
				if (isDarkColor(uniqueColor.getRGB()))
					continue;
			} else if (isLightColor(uniqueColor.getRGB()))
				continue;
			int index = Arrays.binarySearch(pixels, uniqueColor.getRGB());
			if (index < 0)
				break;
		}
		// System.out.println("Unique Color RGB: "+getColor(uniqueColor.getRGB()));
		// System.out.println("Pixel Color: "+pixelColor+" Found Unique Color: "+
		// uniqueColor+" after iter: "+iter);
		return uniqueColor;
	}

	// blue = bits 0 - 7
	// green = bits 8-15
	// red = bits 16-23
	// alpha = bits 24-31
	public static boolean isLightColor(int pixel) {
		int red = pixel >> 16 & 0xFF; // Red
		int green = pixel >> 8 & 0xFF; // Green
		int blue = pixel & 0xFF; // blue
		if (red > 200 && blue > 200)
			return true;
		if (red > 200 && green > 200)
			return true;
		if (blue > 200 && green > 200)
			return true;
		return false;
	}

	public static boolean isDarkColor(int pixel) {
		int red = pixel >> 16 & 0xFF; // Red
		int green = pixel >> 8 & 0xFF; // Green
		int blue = pixel & 0xFF; // blue
		if (red < 100)
			return true;
		if (green < 100)
			return true;
		if (blue < 100)
			return true;
		return false;
	}

	public static String getColor(int pixel) {
		int alpha = pixel >> 24 & 0xFF; // Alpha
		int red = pixel >> 16 & 0xFF; // Red
		int green = pixel >> 8 & 0xFF; // Green
		int blue = pixel & 0xFF; // blue
		return alpha + ":" + red + ":" + green + ":" + blue;
	}

	public static Color getInverseColor(Color color) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		int alpha = color.getAlpha();
		return new Color(255 - red, 255 - green, 255 - blue, alpha);
	}

	public static GraphicsConfiguration getDefaultConfiguration() {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		return gd.getDefaultConfiguration();
	}

	// This method returns a buffered image with the contents of an image
	public static BufferedImage createImageCopy(Image image) {
		return createImageCopy(image, false);
	}

	public static BufferedImage createImageCopy(Image image,
			boolean isTranslucent) {
		// if (image instanceof BufferedImage) return (BufferedImage)image;
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// System.out.println("Image Has Alpha: "+hasAlpha);
		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = createImage(image.getWidth(null),
				image.getHeight(null), hasAlpha, isTranslucent);

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	public static BufferedImage createTransperentImage(int imgWt, int imgHt) {
		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage transImg = createImage(imgWt, imgHt, true);
		return ConvToTransparentImage(transImg, 0.0F);
	}

	public static BufferedImage createImage(int imgWt, int imgHt,
			boolean hasAlpha) {
		return createImage(imgWt, imgHt, hasAlpha, false);
	}

	public static BufferedImage createImage(int imgWt, int imgHt,
			boolean hasAlpha, boolean translucent) {
		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha)
				transparency = Transparency.BITMASK;
			if (translucent)
				transparency = Transparency.TRANSLUCENT;
			// Create the buffered image
			GraphicsConfiguration gc = getDefaultConfiguration();
			bimage = gc.createCompatibleImage(imgWt, imgHt, transparency);
			// RGPTLogger.logToFile("Created Image using GraphicsConfiguration");
		} catch (HeadlessException e) {
			e.printStackTrace();
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(imgWt, imgHt, type);
		}
		return bimage;
	}

	// This method returns an Image object from a buffered image
	public static Image toImage(BufferedImage bufferedImage) {
		return Toolkit.getDefaultToolkit().createImage(
				bufferedImage.getSource());
	}

	public static BufferedImage brightenImage(BufferedImage srcImg) {
		short brighten[] = new short[256];
		for (int i = 0; i < 256; i++) {
			short pixelValue = (short) (i + 10);
			if (pixelValue > 255)
				pixelValue = 255;
			else if (pixelValue < 0)
				pixelValue = 0;
			brighten[i] = pixelValue;
		}
		LookupTable lookupTable = new ShortLookupTable(0, brighten);
		return applyFilter(lookupTable, srcImg);
	}

	public static BufferedImage applyFilter(LookupTable lookupTable,
			BufferedImage srcImg) {
		LookupOp lop = new LookupOp(lookupTable, null);
		return lop.filter(srcImg, null);
	}

	// public static double findColorRange(Color color1, Color color2)
	// {
	// int r1 = color1.getRed(), r2 = color2.getRed();
	// int g1 = color1.getGreen(), g2 = color2.getGreen();
	// int b1 = color1.getBlue(), b2 = color2.getBlue();
	// double distance = Math.sqrt((Math.pow((r1-r2),2))+(Math.pow((g1-g2),2))+
	// (Math.pow((b1-b2),2)));
	// return distance;
	// }

	// This function finds the color range between the two colors
	public static double findColorRange(Color paramColor1, Color paramColor2) {
		return findColorRange(getARGBColor(paramColor1.getRGB()),
				getARGBColor(paramColor2.getRGB()));
	}

	public static double findColorRange(int paramPixel1, int paramPixel2) {
		return findColorRange(getARGBColor(paramPixel1),
				getARGBColor(paramPixel2));
	}

	// This function finds the color range between the two colors
	public static double findColorRange(int[] paramColor1, int[] paramColor2) {
		int r1 = paramColor1[RED], r2 = paramColor2[RED];
		int g1 = paramColor1[GREEN], g2 = paramColor2[GREEN];
		int b1 = paramColor1[BLUE], b2 = paramColor2[BLUE];
		double distance = Math.sqrt((Math.pow((r1 - r2), 2))
				+ (Math.pow((g1 - g2), 2)) + (Math.pow((b1 - b2), 2)));
		return distance;
	}

	public static int[] getARGBColor(Color color) {
		return getARGBColor(color.getRGB());
	}

	public static int[] getARGBColor(int pixel) {
		int[] argbColor = new int[4];
		argbColor[ALPHA] = pixel >> 24 & 0xFF; // Alpha
		argbColor[RED] = pixel >> 16 & 0xFF; // Red
		argbColor[GREEN] = pixel >> 8 & 0xFF; // Green
		argbColor[BLUE] = pixel & 0xFF; // blue
		return argbColor;
	}

	public static Color findPanelColor(BufferedImage paramBufferedImage) {
		int boundThickness = 5, colorRange = 25;
		Color mainColor = findDominantColor(paramBufferedImage);
		RGPTLogger.logToFile("Main Color: " + mainColor);
		Color boundColor = findBoundaryColor(paramBufferedImage, boundThickness);
		RGPTLogger.logToFile("Boundary Color: " + boundColor);
		RGPTLogger.logToFile("Color Range: "
				+ findColorRange(mainColor, boundColor));
		if (findColorRange(mainColor, boundColor) <= (double) colorRange)
			return getInverseColor(mainColor);
		else
			return mainColor;
	}

	public static Color findDominantColor(BufferedImage paramBufferedImage) {
		return findDominantColor(paramBufferedImage, false, -1);
	}

	public static Color findBoundaryColor(BufferedImage paramBufferedImage,
			int boundThickness) {
		return findDominantColor(paramBufferedImage, true, boundThickness);
	}

	public static Color findDominantColor(BufferedImage paramBufferedImage,
			boolean getBoundaryColor, int boundThickness) {
		// margin here indicates boundary thickness
		int margin = boundThickness;
		int totRed = 0;
		int totGreen = 0;
		int totBlue = 0;
		int totAlpha = 0;
		int imgWt = paramBufferedImage.getWidth(), imgHt = paramBufferedImage
				.getHeight();
		Rectangle north = null, south = null, west = null, east = null;
		if (margin > 0) {
			north = new Rectangle(0, 0, imgWt, margin);
			west = new Rectangle(0, 0, margin, imgHt);
			south = new Rectangle(0, imgHt - margin, imgWt, margin);
			east = new Rectangle(imgWt - margin, 0, margin, imgHt);
		}
		for (int irow = 0; irow < imgHt; irow++) {
			for (int icol = 0; icol < imgWt; icol++) {
				if (getBoundaryColor && margin > 0) {
					boolean validPixels = false;
					if (north.contains(icol, irow)
							|| south.contains(icol, irow)
							|| west.contains(icol, irow)
							|| east.contains(icol, irow)) {
						validPixels = true;
					}
					if (!validPixels)
						continue;
				}
				if (paramBufferedImage.getRGB(icol, irow) == 0)
					totAlpha++;
				else {
					Color pixelColor = new Color(paramBufferedImage.getRGB(
							icol, irow));
					totRed += pixelColor.getRed();
					totGreen += pixelColor.getGreen();
					totBlue += pixelColor.getBlue();
				}
			}
		}
		int totPixels = (imgHt * imgWt - totAlpha);
		if (getBoundaryColor)
			totPixels = 2 * (imgHt * margin) + 2 * (imgWt * margin) - totAlpha;
		int red = totRed / totPixels, green = totGreen / totPixels, blue = totBlue
				/ totPixels;

		Color localColor2 = new Color(red, green, blue);
		return localColor2;
	}

	public static BufferedImage cutoutImage(BufferedImage origImage,
			Shape cutoutShape) {
		int imgWt = origImage.getWidth(), imgHt = origImage.getHeight(), margin = 5;
		GeneralPath cutoutPath = new GeneralPath(cutoutShape);
		Rectangle pathBounds = cutoutPath.getBounds();
		Rectangle imgRect = new Rectangle(0, 0, imgWt, imgHt);
		if (!imgRect.contains(pathBounds)) {
			RGPTLogger.logToFile("Image Rect: " + imgRect
					+ " does not contain PathBouns: " + pathBounds);
			// return null;
		}
		int startx = pathBounds.x, starty = pathBounds.y;
		int totHt = starty + pathBounds.height, totWt = startx
				+ pathBounds.width;
		BufferedImage cutoutImg = new BufferedImage(pathBounds.width + margin,
				pathBounds.height + margin, BufferedImage.TYPE_INT_ARGB);

		for (int irow = 0; irow < totHt; irow++) {
			for (int icol = 0; icol < totWt; icol++) {
				int origx = startx + icol, origy = starty + irow;
				if (cutoutPath.contains(origx, origy)) {
					try {
						cutoutImg.setRGB(icol, irow,
								origImage.getRGB(origx, origy));
					} catch (Exception ex) {
						RGPTLogger.logToFile("Orig Img does not contain ptx: "
								+ origx + " and pty: " + origy, ex);
					}
				}
			}
		}
		return cutoutImg;
	}

	public static Map<String, Object> deriveDeviceCTM(
			ScalingFactor scaleFactor, Dimension panelSize, int specStartYPt,
			double newImgWt, double newImgHt) {
		// Scaling Parameters
		double sx = 0.0, sy = 0.0, scale = 0.0;
		// Translation parameters
		double tx = 0.0, ty = 0.0;

		// Derived Data Map
		Map<String, Object> derivedData = new HashMap<String, Object>();

		// Defining the Viewable Area
		double startXPt = 0.0;
		double startYPt = 0.0;
		if (panelSize.getHeight() > newImgHt) {
			startYPt = (panelSize.getHeight() - newImgHt) / 2;
		}
		// RGPTLogger.logToFile("startYPt: " + startYPt +
		// " Panel Size: " + panelSize.toString());
		if (startYPt < (double) specStartYPt)
			startYPt = (double) specStartYPt;
		double bottomPanelHt = (double) specStartYPt; // Bottom Margin same as
														// the Top Margin
		double viewablePanelHt = (double) panelSize.getHeight() - startYPt
				- bottomPanelHt;
		double viewablePanelWidth = (double) panelSize.getWidth() - startXPt;
		if (scaleFactor == null) {
			Dimension dispSize = new Dimension((int) viewablePanelWidth,
					(int) viewablePanelHt);
			scaleFactor = ScalingFactor.getScalingFactor(dispSize, newImgWt,
					newImgHt);
		}
		// RGPTLogger.logToFile("viewablePanelHt: " + viewablePanelHt
		// +" ImageHeight: " + newImgHt);
		if (scaleFactor.isFitHeight()) {
			sy = viewablePanelHt / newImgHt;
			if (sy > 1.0)
				sy = 1.0;
			scale = sy;
		} else if (scaleFactor.isFitPage()) {
			sy = viewablePanelHt / newImgHt;
			sx = viewablePanelWidth / newImgWt;
			if (sx > sy)
				scale = sy;
			else
				scale = sx;
		} else if (scaleFactor.isFitWidth()) {
			sx = viewablePanelWidth / newImgWt;
			if (sx > 1.0)
				sx = 1.0;
			scale = sx;
		} else if (scaleFactor.isZoomInOut())
			scale = ((double) scaleFactor.m_ZoomValue) / 100;

		double scaledImageWt = newImgWt * scale, scaledImageHt = newImgHt
				* scale;
		// RGPTLogger.logToFile("Calculated WIDTH AND HEIGHT: " + scaledImageWt
		// +
		// ": " + scaledImageHt);

		if (panelSize.getHeight() > scaledImageHt)
			startYPt = (panelSize.getHeight() - scaledImageHt) / 2;
		if (startYPt < (double) specStartYPt)
			startYPt = (double) specStartYPt;
		// RGPTLogger.logToFile("New startYPt: " + startYPt);

		// Calculating Translations in X and Y dirn.
		ty = startYPt + scaledImageHt;
		// RGPTLogger.logToFile("Scale: " + scale + " Ty: " + ty +
		// " Scaled PageHt: "
		// + scaledImageHt + " Viewable PanelHt: " + viewablePanelHt);
		if (scaledImageWt < viewablePanelWidth) {
			// Devided by 2 to give equal padding on either side of the Image
			// Width
			tx = startXPt + (viewablePanelWidth - scaledImageWt) / 2;
		} else
			tx = startXPt;
		// RGPTLogger.logToFile("Scale: " + scale + "Ty: " + ty + " Tx: " + tx);

		// Setting the Windows Device CTM
		// AffineTransform(double m00, double m10, double m01, double m11,
		// double m02, double m12)
		AffineTransform windowDeviceCTM = null, screenToImageCTM = null;
		windowDeviceCTM = new AffineTransform(scale, 0, 0, scale, tx, startYPt);
		try {
			screenToImageCTM = windowDeviceCTM.createInverse();
		} catch (Exception ex) {
			RGPTLogger.logToFile("ScreenToImageCTM Exception", ex);
		}
		// RGPTLogger.logToFile("Window Device CTM: " +
		// windowDeviceCTM.toString());

		// Calculating the Screen Page Rectangle
		Rectangle2D.Double screenRect2D = new Rectangle2D.Double(tx, startYPt,
				newImgWt * scale, newImgHt * scale);
		Rectangle screenRect = RGPTUtil.getRectangle(screenRect2D);
		// RGPTLogger.logToFile("Screen Rect is: " + screenRect.toString());

		// RGPTLogger.logToFile("PT TEST SRC " + ptTstSrc.toString() +
		// ": Dest: " + ptTstDes.toString());
		derivedData.put("Scale", scale);
		derivedData.put("ScalingFactor", scaleFactor);
		derivedData.put("ScaledImageWt", scaledImageWt);
		derivedData.put("ScaledImageHt", scaledImageHt);
		derivedData.put("WindowDeviceCTM", windowDeviceCTM);
		derivedData.put("ScreenToImageCTM", screenToImageCTM);
		derivedData.put("ScreenImageRect", screenRect);
		derivedData.put("CanvasImageRect", screenRect2D);
		return derivedData;
	}

	// This functions makes the image pixel contained in the shape transperent
	// or sets it to the param color supplied in the function
	public static BufferedImage tranformImage(BufferedImage origImage,
			Shape cutoutShape, Color color) {
		int imgWt = origImage.getWidth(), imgHt = origImage.getHeight(), margin = 5;
		GeneralPath cutoutPath = new GeneralPath(cutoutShape);
		Rectangle pathBounds = cutoutPath.getBounds();
		Rectangle imgRect = new Rectangle(0, 0, imgWt, imgHt);
		if (!imgRect.contains(pathBounds))
			return null;
		int startx = pathBounds.x, starty = pathBounds.y;
		BufferedImage finalImg = createImageCopy(origImage);

		for (int irow = starty; irow < imgHt; irow++) {
			for (int icol = startx; icol < imgWt; icol++) {
				if (cutoutPath.contains(icol, irow)) {
					if (color == null)
						finalImg.setRGB(icol, irow, 0);
					else
						finalImg.setRGB(icol, irow, color.getRGB());
					// setAlphaValue(finalImg, irow, icol, 0.0D);
				}
			}
		}
		return finalImg;
	}

	// This functions makes the image pixel transperent or sets it to the param
	// color supplied in the function,
	// if the image pixel is within the color range of the param color
	public static BufferedImage tranformImage(BufferedImage origImg,
			Color paramColor, Color newColor, int colorRangePerc,
			boolean createCopy, Vector<Integer> transPixel) {
		int maxCols = origImg.getWidth(), maxRows = origImg.getHeight();
		double paramColorRange = (double) 255
				* ((double) colorRangePerc / (double) 100);
		int[] argbParamColor = getARGBColor(paramColor.getRGB());
		int[] argbNewColor = getARGBColor(newColor.getRGB());
		int[][][] pixel_rgb = BufferedImagetoPixel3DRGB(origImg);
		BufferedImage finalImg = origImg;
		if (createCopy)
			finalImg = createImageCopy(origImg);

		for (int row = 0; row < maxRows; row++) {
			for (int col = 0; col < maxCols; col++) {
				// if (!isPixalPointValid(maxCols, row, col, transPixel))
				// continue;
				double colorRange = findColorRange(pixel_rgb[row][col],
						argbParamColor);
				if (colorRange <= paramColorRange) {
					if (transPixel != null)
						transPixel.addElement(row * maxCols + col);
					if (newColor == null) {
						finalImg.setRGB(col, row, 0);
						// pixel_rgb[row][col][ALPHA] = 0;
					} else {
						finalImg.setRGB(col, row, newColor.getRGB());
						// pixel_rgb[row][col] = argbNewColor;
					}
					// setAlphaValue(finalImg, irow, icol, 0.0D);
				}
			}
		}
		// SaveImage(finalImg, "test_trans_final.jpg", "jpg");
		// BufferedImage finalImg = Pixel3DRGBtoBufferedImage(pixel_rgb);
		return finalImg;
	}

	public static boolean isPixalPointValid(int imgWt, int row, int col,
			Vector<Integer> transPixel) {
		if (row == 0 && col == 0)
			return true;
		if (transPixel == null || transPixel.size() == 0)
			return true;
		int lastPixal = transPixel.lastElement().intValue();
		int lastRow = lastPixal / imgWt;
		int lastCol = lastPixal - lastRow * imgWt;
		int dx = row - lastRow, dy = col - lastCol;
		double dist = Math.sqrt(dx * dx + dy * dy);
		if (dist <= 500)
			return true;
		return false;
	}

	public static Vector<Integer> convertTransformationPixels(int imgWt,
			int transImgWt, Vector<Integer> transPixel, AffineTransform affine) {
		Vector<Integer> convPixel = new Vector<Integer>();
		Point2D.Double selPixelPt = null;
		if (transPixel == null || transPixel.size() == 0 || affine == null)
			return convPixel;
		for (int i = 0; i < transPixel.size(); i++) {
			int transPixalPos = transPixel.elementAt(i);
			// converting pixal position to points for affine transformation
			int row = transPixalPos / imgWt;
			int col = transPixalPos - row * imgWt;
			selPixelPt = new Point2D.Double(col, row);
			selPixelPt = (Point2D.Double) affine.transform(selPixelPt, null);
			// reconverting pixal points to pixal position
			int convPixelPos = (int) selPixelPt.y * transImgWt
					+ (int) selPixelPt.x;
			convPixel.addElement(convPixelPos);
		}
		return convPixel;
	}

	public static boolean containsPixalPoint(int imgWt,
			Vector<Integer> transPixelList, int pcol, int prow) {
		if (transPixelList == null || transPixelList.size() == 0)
			return false;
		int pixelPos = prow * imgWt + pcol;
		int[] transPixel = RGPTUtil.toArray(transPixelList);
		Arrays.sort(transPixel);
		int index = Arrays.binarySearch(transPixel, pixelPos);
		if (index < 0)
			return false;
		return true;
	}

	// This functions retrieves the image pixel is within the color range of the
	// param color
	public static void setTranformImagePixels(BufferedImage origImg,
			Color paramColor, int colorRangePerc, Vector<Integer> transPixel,
			Shape cutoutShape) {
		if (transPixel == null)
			throw new NullPointerException("Transformation Pizel is Null");
		int maxCols = origImg.getWidth(), maxRows = origImg.getHeight();
		GeneralPath cutoutPath = null;
		int startx = 0, starty = 0;
		if (cutoutShape != null) {
			cutoutPath = new GeneralPath(cutoutShape);
			Rectangle pathBounds = cutoutPath.getBounds();
			Rectangle imgRect = new Rectangle(0, 0, maxCols, maxRows);
			if (!imgRect.contains(pathBounds))
				return;
			// startx = pathBounds.x; starty = pathBounds.y;
			// maxCols = pathBounds.width; maxRows = pathBounds.height;
		}
		// RGPTLogger.logToFile("stx: "+startx+" sty: "+starty+" maxRow: "+maxRows+" macCol:"+maxCols);
		double paramColorRange = (double) 255
				* ((double) colorRangePerc / (double) 100);
		int[] argbParamColor = null;
		if (paramColor != null)
			argbParamColor = getARGBColor(paramColor.getRGB());
		int[][][] pixel_rgb = BufferedImagetoPixel3DRGB(origImg);

		for (int row = starty; row < maxRows; row++) {
			for (int col = startx; col < maxCols; col++) {
				boolean processPt = false;
				if (cutoutPath == null)
					processPt = true;
				else if (cutoutPath.contains(col, row))
					processPt = true;
				if (processPt) {
					// RGPTLogger.logToFile("ProcessPtx: "+col+" pty "+row);
					if (argbParamColor != null) {
						double colorRange = findColorRange(pixel_rgb[row][col],
								argbParamColor);
						if (colorRange <= paramColorRange)
							transPixel.addElement(row * origImg.getWidth()
									+ col);
					} else if (cutoutPath != null)
						transPixel.addElement(row * origImg.getWidth() + col);
				}
			} // for (int col=startx ; col<maxCols ; col++)
		} // for (int row=starty ; row< maxRows ; row++)
	}

	// This functions retrieves the image pixel is within the color range of the
	// param color
	public static BufferedImage tranformImage(BufferedImage origImg,
			Vector<Integer> transPixelList, Color newColor, boolean createCopy) {
		int[] transPixel = RGPTUtil.toArray(transPixelList);
		int maxCols = origImg.getWidth(), maxRows = origImg.getHeight();
		int[] argbNewColor = null;
		if (newColor != null)
			argbNewColor = getARGBColor(newColor.getRGB());
		int[][][] pixel_rgb = BufferedImagetoPixel3DRGB(origImg);
		Arrays.sort(transPixel);
		BufferedImage finalImg = origImg;
		if (createCopy)
			finalImg = createImageCopy(origImg);

		for (int row = 0; row < maxRows; row++) {
			for (int col = 0; col < maxCols; col++) {
				int pixelPos = row * maxCols + col;
				int index = Arrays.binarySearch(transPixel, pixelPos);
				if (index < 0)
					continue;
				if (newColor == null) {
					finalImg.setRGB(col, row, 0);
					// pixel_rgb[row][col][ALPHA] = 0;
				} else {
					finalImg.setRGB(col, row, newColor.getRGB());
					// pixel_rgb[row][col] = argbNewColor;
				}
			}
		}

		return finalImg;
	}

	public static boolean resetTranformPixel(BufferedImage origImg, int ptx,
			int pty, Vector<Integer> transPixelList, boolean addNewPixel,
			int size) {
		// RGPTLogger.logToFile("Sel Size: "+size);
		int[] transPixel = RGPTUtil.toArray(transPixelList);
		int imgWt = origImg.getWidth();
		boolean resetPixels = false;
		Arrays.sort(transPixel);
		int minRow = pty - size, maxRow = pty + size, minCol = ptx - size, maxCol = ptx
				+ size;
		for (int row = minRow; row < maxRow; row++) {
			for (int col = minCol; col < maxCol; col++) {
				int pixelPos = row * imgWt + col;
				int index = Arrays.binarySearch(transPixel, pixelPos);
				if (addNewPixel && index < 0) {
					resetPixels = true;
					transPixelList.addElement(pixelPos);
				} else if (!addNewPixel && index >= 0) {
					resetPixels = true;
					transPixelList
							.removeElement(new Integer(transPixel[index]));
				}
			}
		}
		return resetPixels;
	}

	// Creates Color Image for Supplied Wt and Ht
	public static BufferedImage createColorImage(Color paramColor,
			String propName) {
		int imgWt = 0, imgHt = 0;
		String[] imgSize = RGPTParams.getVal("InfoMesgImageSize").split(
				"x");
		imgWt = Integer.valueOf(imgSize[0]);
		imgHt = imgWt;
		if (imgSize.length == 2)
			imgHt = Integer.valueOf(imgSize[1]);
		return createColorImage(paramColor, imgWt, imgHt);
	}

	public static BufferedImage createColorImage(Color paramColor, int imgWt,
			int imgHt) {
		BufferedImage localBufferedImage = new BufferedImage(imgWt, imgHt, 2);
		Graphics2D localGraphics2D = localBufferedImage.createGraphics();
		setRenderingHints(localGraphics2D);
		localGraphics2D.setColor(paramColor);
		localGraphics2D.fillRect(0, 0, imgWt, imgHt);
		localGraphics2D.dispose();
		// RGPTLogger.logToFile("Image: "+localBufferedImage);
		// SaveImage(localBufferedImage, "test.gif", "gif");
		return localBufferedImage;
	}

}