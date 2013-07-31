// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.imageutil.SelectedImageHandler;
import com.rgpt.serverhandler.ImageHandlerInterface;
import com.rgpt.util.FileFilterFactory;

//import javax.swing.filechooser.FileFilter;

// This class is predominantly used when a multiple VDP images  are selected 
// for a mouse click. In this scenario the ThumbViewImageFrame is used to 
// Select appropriate Image. On Selection the updateImageData method is 
// called. This calls the PDFViewer to add the selection.

public class ImageViewer implements SelectedImageHandler {
	// This has the list of Images Selected for a mouse Click
	Vector m_ImageSelected;

	// Once the Work is done the ThumbViewImageFrame is disposed.
	ThumbViewImageFrame m_ThumbViewImageFrame;

	// Directory Settings for Loading Images
	public static String m_ImageDirPath;

	public ImageViewer() {
	}

	// This method is called on Mouse Released to check the VDP Image Field on
	// which the mouse is dropped.
	public boolean containsVDPImage(MouseEvent me) {
		return true;
	}

	// This method is called to check if the User has Selected any Image for the
	// VDP Field.
	public boolean containsUserSelImage() {
		return true;
	}

	public void activateImageViewer() {
		// Populating the Handler and the Image Selection UI for Selecting the
		// Image and adding to the selection
		HashMap userSelImages = loadImages();
		m_ThumbViewImageFrame = new ThumbViewImageFrame(this, userSelImages,
				null, ImageHandlerInterface.DESKTOP_IMAGES, null, null);
		m_ThumbViewImageFrame
				.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		m_ThumbViewImageFrame.setLocation(100, 100);
		m_ThumbViewImageFrame.setVisible(true);
	}

	public void setImageSelectionUI(ThumbViewImageFrame imgSelUI) {
		m_ThumbViewImageFrame = imgSelUI;
	}

	public void updateImageData(BufferedImage image, ImageHolder imgHldr) {
		m_ThumbViewImageFrame.dispose();
	}

	// This method call is implemented to show the selected Image on the screen
	// by drawing lines around the Image. This method is invoked when there are
	// multiple images for a particular selection to be handled.
	public void showImage(int assetId) {
	}

	// This method call is implemented to further process the Selected Image.
	// This method is invoked when there are multiple images for a particular
	// selection to be handled.
	public void selectImage(int assetId) {
	}

	// This API is to show Busy Cursor when the Image is getting uploaded
	public void showBusyCursor() {
	}

	// This API is to reset the Cursor to default Cursor
	public void setDefaultCursor() {
	}

	// This method is called when the Display Box is closed. In this scenario,
	// the this object does the necessary clean-up and refresh operations.
	public void windowClosing() {
		System.out.println("In windowClosing Method");
	}

	public static HashMap loadImages() {
		if (m_ImageDirPath == null)
			m_ImageDirPath = "C:/PersonalizationService/Images";
		int thumbWidth = 100, thumbHt = 100;
		ImageHolder imgHldr = null;
		BufferedImage bigImage = null, thumbImage = null;

		File imgDir = new File(m_ImageDirPath);
		System.out.println("Is Dir: " + imgDir.isDirectory()
				+ " :Is Abs Path: " + imgDir.isAbsolute());
		System.out.println("Image Dir Path: " + imgDir.toString()
				+ " Using Abs Path: " + imgDir.getAbsolutePath());
		File[] imgFiles = imgDir
				.listFiles((FileFilter) FileFilterFactory.IMAGE_FILE_FILTER);

		// The Images userSelImages is passed to ThumviewImageBox for user
		// selection
		HashMap userSelImages = new HashMap();
		if (imgFiles == null)
			return userSelImages;
		for (int i = 0; i < imgFiles.length; i++) {
			System.out.println("Loading Image: " + imgFiles[i].toString());
			// System.out.println("Image File Name: " + imgFiles[i].getName());
			if (imgFiles[i].isDirectory())
				continue;
			bigImage = ImageUtils.LoadImage(imgFiles[i]);
			thumbImage = ImageUtils.ScaleToSize(bigImage, thumbWidth, thumbHt);
			int assetId = i + 1;
			// int dpi = ImageUtils.FindResolution(bigImage, w, h);
			// System.out.println("DPI of the Image: " + dpi);
			imgHldr = new ImageHolder(assetId, thumbImage, null);
			imgHldr.m_SrcDir = imgDir.toString() + "/";
			imgHldr.m_FileName = imgFiles[i].getName();
			imgHldr.m_OrigImageWidth = bigImage.getWidth();
			imgHldr.m_OrigImageHeight = bigImage.getHeight();
			userSelImages.put(assetId, imgHldr);
		}
		System.out.println("Total Loaded Images: " + userSelImages.size());
		return userSelImages;
	}

	public static void main(String[] args) {
		/*
		 * try { // Set System L&F
		 * UIManager.setLookAndFeel("com.easynth.lookandfeel.EaSynthLookAndFeel"
		 * ); } catch (Exception ex) {
		 * System.out.println("Exception while setting L&F: " +
		 * ex.getMessage()); ex.printStackTrace(); }
		 */
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		ImageViewer imgHdlr = null;
		imgHdlr = new ImageViewer();
		imgHdlr.activateImageViewer();
	}
}