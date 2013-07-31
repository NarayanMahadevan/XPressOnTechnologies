/*******************************************************************************
 *
 * Rich Graphics Personalization Technology
 *
 ******************************************************************************/

package com.rgpt.viewer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.util.FileUIInfo;
import com.rgpt.util.RGPTFileFilter;
import com.rgpt.util.RGPTLogger;

/* ImagePreview.java used by FileUploadViewer.java. */
public class FontPreview extends JComponent implements PropertyChangeListener {
	ImageIcon m_ImageThumbnail = null;
	File m_FontFile = null;
	FileUIInfo m_FileUIInfo;

	public FontPreview(FileUIInfo fileInfo) {
		setPreferredSize(new Dimension(100, 100));
		m_FileUIInfo = fileInfo;
	}

	public void loadImage() {
		m_ImageThumbnail = null;
		if (m_FontFile == null)
			return;
		RGPTLogger.logToConsole("In loadImage 4 font: " + m_FontFile);
		String ext = RGPTFileFilter.getExtension(m_FontFile);
		m_ImageThumbnail = (ImageIcon) m_FileUIInfo.m_FileIcon.get(ext);

		if (ext.equals("ttf")) {
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, m_FontFile);
				Image fontImg = ImageUtils.createFontImage(font,
						java.awt.Color.BLACK.getRGB(), 70, 200);
				m_ImageThumbnail = new ImageIcon(fontImg);
			} catch (Exception ex) {
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		boolean update = false;
		String prop = e.getPropertyName();
		// System.out.println("Property Name: " + prop);
		if (!isShowing())
			return;

		// If the directory changed, don't show an image.
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			m_FontFile = null;
			m_ImageThumbnail = null;
			repaint();
			return;
		}

		// If a file became selected, find out which one.
		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			m_FontFile = (File) e.getNewValue();
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
