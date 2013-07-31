// RGPT PACKAGES
package com.rgpt.viewer;

//import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.SelectedImageHandler;

public class ThumbviewToolBar extends JDialog {
	static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;
	HashMap m_ImageAssets;

	// This component display the full image
	private JToolBar m_ThumbviewToolBar;

	// This Object is used for further processing of Selected Images.
	SelectedImageHandler m_SelectedImageHandler;

	// This identifies the selected Image Object
	ThumbnailAction m_SelectedImageObject;

	// As explained above null can be a valid argument if Image Assets are
	// completely populated. In this case useImageAssets is set to true
	public ThumbviewToolBar(SelectedImageHandler imgHdlr, HashMap imageAssets) {
		super(new java.awt.Frame(), "Image Selection Box");

		m_SelectedImageHandler = imgHdlr;

		// ...Then set the window size or call pack...
		setSize(600, 100);

		m_ImageAssets = imageAssets;
		m_ThumbviewToolBar = new JToolBar();

		// Set the window's location.
		// setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

		setContentPane(this.createContentPane());

		// We add two glue components. Later in process() we will add thumbnail
		// buttons
		// to the toolbar inbetween thease glue compoents. This will center the
		// buttons in the toolbar.
		m_ThumbviewToolBar.add(Box.createGlue());
		m_ThumbviewToolBar.add(Box.createGlue());

		// this centers the frame on the screen
		// setLocationRelativeTo(null);

		// start the image loading SwingWorker in a background thread
		this.loadImages();
	}

	public JPanel createContentPane() {
		JScrollPane imageScroller = null;
		JPanel contentPane = new JPanel();
		// Set the Layout to Border Layout
		contentPane.setLayout(new BorderLayout());
		imageScroller = new JScrollPane(m_ThumbviewToolBar,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		imageScroller.setViewportBorder(BorderFactory
				.createLineBorder(Color.black));
		contentPane.add(imageScroller, BorderLayout.CENTER);
		return contentPane;
	}

	public void processWindowEvent(WindowEvent e) {
		// System.out.println("Windows Event: " + e.getID());
		if (e.getID() == WindowEvent.WINDOW_CLOSED
				|| e.getID() == WindowEvent.WINDOW_CLOSING)
			m_SelectedImageHandler.windowClosing();
	}

	public void repaintContent() {
		this.repaint();
	}

	// This method is invoked when execute methos id called on SwingWorker
	// Object
	// Creates thumbnail versions of the target image files.
	private Void loadImages() {
		Object[] assetIds = m_ImageAssets.keySet().toArray();
		ThumbnailAction defaultThumbIcon = null;
		for (int i = 0; i < assetIds.length; i++) {
			int assetId = ((Integer) assetIds[i]).intValue();
			ThumbnailAction thumbAction;
			ImageHolder imgHldr = (ImageHolder) m_ImageAssets.get(assetId);
			ImageIcon thumbnailIcon = null;
			if (imgHldr.m_UseCachedImages)
				thumbnailIcon = new ImageIcon(imgHldr.m_ThumbviewImage,
						imgHldr.m_ImgCaption);
			else
				thumbnailIcon = new ImageIcon(imgHldr.m_ImgStr,
						imgHldr.m_ImgCaption);
			thumbAction = new ThumbnailAction(imgHldr, thumbnailIcon);
			if (i == 0)
				defaultThumbIcon = thumbAction;

			// This incalls the process method on this object
			loadThumbView(thumbAction);
		}
		return null;
	}

	/**
	 * Process all loaded images.
	 */
	private void loadThumbView(ThumbnailAction thumbAction) {
		JButton thumbButton = new JButton(thumbAction);
		thumbButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					processImage(true, false);
				}
			}
		});

		// add the new button BEFORE the last glue
		// this centers the buttons in the toolbar
		// System.out.println("Adding Image to Toolbar: " +
		// thumbAction.m_ImgHldr.m_AssetId);
		// System.out.println("The Component Count: " +
		// m_ThumbviewToolBar.getComponentCount());
		m_ThumbviewToolBar.add(thumbButton,
				m_ThumbviewToolBar.getComponentCount() - 1);
	}

	private void processImage(boolean selectImage, boolean drawImage) {
		int assetId = m_SelectedImageObject.m_ImgHldr.m_AssetId;
		if (selectImage)
			m_SelectedImageHandler.selectImage(assetId);
		else if (drawImage)
			m_SelectedImageHandler.showImage(assetId);
		return;
	}

	/**
	 * Action class that shows the image specified in it's constructor.
	 */
	private class ThumbnailAction extends AbstractAction {
		// The icon if the full image we want to display.
		// public ImageIcon m_DisplayImage;
		public BufferedImage m_DisplayImage;
		public BufferedImage m_ScaledImage;

		// ImageHolder Objects which holds the Thumbview Image and location of
		// Image in Server
		public ImageHolder m_ImgHldr;

		/**
		 * @param Icon
		 *            - The full size photo to show in the button.
		 * @param Icon
		 *            - The thumbnail to show in the button.
		 * @param String
		 *            - The descriptioon of the icon.
		 */
		public ThumbnailAction(ImageHolder imgHldr, Icon thumb) {
			m_ImgHldr = imgHldr;
			m_DisplayImage = null;

			// The LARGE_ICON_KEY is the key for setting the
			// icon when an Action is applied to a button.
			putValue(LARGE_ICON_KEY, thumb);
		}

		// Shows the full image in the main area and sets the application title.
		public void actionPerformed(ActionEvent e) {
			m_SelectedImageObject = this;
			processImage(false, true);
		}

		public void selectDefaultImage() {
			m_SelectedImageObject = this;
		}

	}

	public void showError(String mesg, String title) {
		JOptionPane.showMessageDialog(this, mesg, title,
				JOptionPane.ERROR_MESSAGE);
	}
}
