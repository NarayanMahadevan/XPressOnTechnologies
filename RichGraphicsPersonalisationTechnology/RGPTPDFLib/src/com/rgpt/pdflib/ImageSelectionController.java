package com.rgpt.pdflib;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Vector;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.SelectedImageHandler;
import com.rgpt.viewer.ThumbviewToolBar;

/**
 * This class is predominantly used when a multiple VDP images are selected for
 * a mouse click. In this scenario the ThumbViewImageFrame is used to Select
 * appropriate Image. On Selection the updateImageData method is called. This
 * calls the PDFViewer to add the selection.
 * 
 * @author Narayan
 * 
 */

public class ImageSelectionController implements SelectedImageHandler {

	Vector<Map<String, Object>> m_SelectedImages;

	// This Object is used to add VDP Image to the Selection and to display
	// selected image in the PDF View
	PDFElemSelHandler m_PDFSelViewer;

	// Once the Work is done the ImageSelection Window is disposed.
	Window m_ImageSelectionUI;

	public ImageSelectionController(Window selUI, PDFElemSelHandler viewer,
			Vector<Map<String, Object>> selImages) {
		m_PDFSelViewer = viewer;
		m_SelectedImages = selImages;
		m_ImageSelectionUI = selUI;
	}

	public void setImageSelectionUI(ThumbviewToolBar imgSelUI) {
		m_ImageSelectionUI = imgSelUI;
	}

	@Override
	public void updateImageData(BufferedImage image, ImageHolder imgHldr) {
		// TODO Auto-generated method stub

	}

	// This method call is implemented to show the selected Image on the screen
	// by drawing lines around the Image. This method is invoked when there are
	// multiple images for a particular selection to be handled.
	@Override
	public void showImage(int assetId) {
		System.out.println("Show Image Asset: " + assetId);
		Map<String, Object> selImageElement = getSelectedImage(assetId);
		if (selImageElement == null)
			return;

		Rectangle2D.Double imageBBox = null;
		try {
			// Displaying all the Selections in the Page
			imageBBox = (Rectangle2D.Double) selImageElement.get("ElementBBox");
			m_PDFSelViewer.update();
			m_PDFSelViewer.displaySelection(imageBBox);
			// m_ThumbviewToolBar.repaint();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// This method call is implemented to further process the Selected Image.
	// This method is invoked when there are multiple images for a particular
	// selection to be handled.
	@Override
	public void selectImage(int assetId) {
		System.out.println("Select Image Asset: " + assetId);
		Map<String, Object> selImageElement = getSelectedImage(assetId);
		if (selImageElement == null)
			return;
		try {
			// Displaying all the Selections in the Page
			m_PDFSelViewer.update();
			m_PDFSelViewer.addSelectedImage(selImageElement);
		} catch (PDFLibException ex) {
			throw new RuntimeException("Unable to add the Selected Image: "
					+ ex.getMessage());
		}
		if (m_ImageSelectionUI != null)
			m_ImageSelectionUI.dispose();
	}

	// This method is called when the Display Box is closed. In this scenario,
	// the this object does the necessary clean-up and refresh operations.
	@Override
	public void close() {
		System.out.println("In windowClosing Method");
		this.cleanMemory();
	}

	public void cleanMemory() {
		if (m_SelectedImages != null && !m_SelectedImages.isEmpty())
			m_SelectedImages.removeAllElements();
		m_SelectedImages = null;
		if (m_ImageSelectionUI != null) {
			m_ImageSelectionUI.dispose();
			m_ImageSelectionUI = null;
		}
	}

	@Override
	public boolean containsVDPImage(MouseEvent me) {
		return true;
	}

	@Override
	public boolean containsUserSelImage() {
		return true;
	}

	@Override
	public void showBusyCursor() {
	}

	@Override
	public void setDefaultCursor() {
	}

	// Returns Images based on Asset Id
	private Map<String, Object> getSelectedImage(int selAssetId) {
		Map<String, Object> selImageElement = null;
		if (m_SelectedImages == null)
			return null;
		for (int i = 0; i < m_SelectedImages.size(); i++) {
			if (m_PDFSelViewer == null)
				break;
			selImageElement = m_SelectedImages.elementAt(i);
			int assetId = ((Integer) selImageElement.get("DigitalAssetId"))
					.intValue();
			if (assetId == selAssetId)
				return selImageElement;
		}
		return null;
	}

}
