/*******************************************************************************
 *
 * Rich Graphics Personalization Technology
 *
 ******************************************************************************/

package com.rgpt.fileuploader;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.util.RGPTFileFilter;

/* FileUIInfo.java is used by FileUploaderView.java. */
public class FileUIInfo extends FileView {
	// File Filter used in the File Chooser
	public RGPTFileFilter m_FileFilter;

	// Image Icons corresponding to the File Extensions
	public HashMap m_FileIcon;

	public FileUIInfo(RGPTFileFilter filter) {
		m_FileFilter = filter;

		// Populating inital set of file icons
		m_FileIcon = new HashMap();
		m_FileIcon.put("jpg", createFileIcon("jpgIcon.gif"));
		m_FileIcon.put("gif", createFileIcon("gifIcon.gif"));
		m_FileIcon.put("tiff", createFileIcon("tiffIcon.gif"));
		m_FileIcon.put("png", createFileIcon("pngIcon.png"));
		m_FileIcon.put("pdf", createFileIcon("pdfIcon.gif"));
		m_FileIcon.put("zip", createFileIcon("zipIcon.gif"));
		m_FileIcon.put("default", createFileIcon("defaultIcon.gif"));
	}

	public ImageIcon createFileIcon(String fileName) {
		boolean useJarResource = true;
		ImageIcon imgIcon = null;
		String filePath = "fileUploadUI/res/" + fileName;
		System.out.println("In FileUIInfo createFileIcon: " + filePath);

		try {
			imgIcon = new ImageIcon(filePath);
			Image img = imgIcon.getImage();
			if (img.getWidth(null) <= 0 || img.getHeight(null) <= 0)
				useJarResource = true;
			else
				useJarResource = false;
			System.out.println("Image Wt: " + img.getWidth(null) + " Ht: "
					+ img.getHeight(null));
		} catch (Throwable th) {
			useJarResource = true;
			// th.printStackTrace();
		}
		if (useJarResource) {
			filePath = "/res/" + fileName;
			byte[] buf = ImageUtils.loadImage(filePath, this.getClass());
			imgIcon = new ImageIcon(Toolkit.getDefaultToolkit()
					.createImage(buf));
		}
		return imgIcon;
	}

	// let the L&F FileView figure this out
	public String getName(File f) {
		return null;
	}

	// let the L&F FileView figure this out
	public String getDescription(File f) {
		return null;
	}

	// let the L&F FileView figure this out
	public Boolean isTraversable(File f) {
		return null;
	}

	public String getTypeDescription(File f) {
		return m_FileFilter.getDescription();
	}

	public Icon getIcon(File f) {
		if (f == null || f.isDirectory())
			return null;
		String ext = m_FileFilter.getExtension(f);
		if (ext == null || ext.equals("lnk"))
			return null;
		Icon fileIcon = (Icon) m_FileIcon.get(ext);
		if (fileIcon != null)
			return fileIcon;
		System.out.println("No Icon available for Ext: " + ext);
		if (ext == null)
			return null;
		// Assigning default icon
		fileIcon = (Icon) m_FileIcon.get("default");
		m_FileIcon.put(ext, fileIcon);
		return fileIcon;
	}
}
