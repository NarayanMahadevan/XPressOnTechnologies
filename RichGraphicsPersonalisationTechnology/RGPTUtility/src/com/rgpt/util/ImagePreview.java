/*******************************************************************************
 *
 * Rich Graphics Personalization Technology
 *
 ******************************************************************************/

package com.rgpt.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import com.rgpt.imageutil.ImageUtils;

/* ImagePreview.java used by FileUploadViewer.java. */
public class ImagePreview extends JComponent implements PropertyChangeListener {
	ImageIcon m_ImageThumbnail = null;
	File m_ImageFile = null;
	FileUIInfo m_FileUIInfo;

	public ImagePreview(FileUIInfo fileInfo) {
		setPreferredSize(new Dimension(100, 50));
		m_FileUIInfo = fileInfo;
	}

	public void loadImage() {
		m_ImageThumbnail = null;
		if (m_ImageFile == null)
			return;

		String ext = RGPTFileFilter.getExtension(m_ImageFile);
		if (!FileFilterFactory.IMAGE_FILE_FILTER.accept(ext)) {
			m_ImageThumbnail = (ImageIcon) m_FileUIInfo.m_FileIcon.get(ext);
			return;
		}
		Image scaledImg = ImageUtils.loadImage(m_ImageFile.getPath(), 90);
		m_ImageThumbnail = new ImageIcon(scaledImg);
	}

	public void propertyChange(PropertyChangeEvent e) {
		boolean update = false;
		String prop = e.getPropertyName();
		// System.out.println("Property Name: " + prop);
		if (!isShowing())
			return;

		// If the directory changed, don't show an image.
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			m_ImageFile = null;
			m_ImageThumbnail = null;
			repaint();
			return;
		}

		// If a file became selected, find out which one.
		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			m_ImageFile = (File) e.getNewValue();
			update = true;
		}

		// Update the preview accordingly.
		if (update) {
			m_ImageThumbnail = null;
			loadImage();
			repaint();
		}
	}

	protected void paintComponent(Graphics g) {
		if (m_ImageThumbnail == null)
			loadImage();
		if (m_ImageThumbnail == null)
			return;
		int x = getWidth() / 2 - m_ImageThumbnail.getIconWidth() / 2;
		int y = getHeight() / 2 - m_ImageThumbnail.getIconHeight() / 2;
		if (y < 0)
			y = 0;
		if (x < 5)
			x = 5;
		m_ImageThumbnail.paintIcon(this, g, x, y);
	}
}
