package com.rgpt.fileuploader;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.serverhandler.ImageHandlerInterface;
import com.rgpt.serverhandler.ImageUploadInterface;
import com.rgpt.util.AppletParameters;

public class ImageUploaderImpl extends JDialog implements ImageUploadInterface,
		FileUploadInterface, Runnable {
	ImageHandlerInterface m_ImageHandler;
	FileUploadViewer m_FileUploadViewer;
	boolean m_IsServerMode;
	AppletParameters m_Params;

	public ImageUploaderImpl(AppletParameters params) {
		super(new java.awt.Frame(), "RGPT FILE UPLOADER", true);
		m_Params = params;
		m_IsServerMode = true;
		m_Params.m_RequestParamValues.put("file_type", "IMAGE");
		m_Params.m_RequestParamValues.put("allow_multi_file_upload", "true");
	}

	public ImageUploaderImpl() {
		super(new java.awt.Frame(), "RGPT FILE UPLOADER", true);
		m_IsServerMode = true;
		this.createParams();
	}

	public ImageUploaderImpl(boolean isServerMode) {
		m_IsServerMode = isServerMode;
		this.createParams();
	}

	private void createParams() {
		String reqType = AppletParameters.UPLOAD_DIGITAL_ASSETS;
		m_Params = new AppletParameters(reqType, null);
		HashMap appletParameters = new HashMap();
		appletParameters.put("file_type", "IMAGE");
		appletParameters.put("allow_multi_file_upload", "true");
		StringBuffer errMsg = new StringBuffer();
		System.out.println("Applet Parameters: " + appletParameters.toString());
		m_Params.uploadRequestParams(appletParameters, errMsg);
	}

	public void addImageFile(ImageHandlerInterface imgHandler,
			boolean useFileUploadUI) throws Exception {
		m_ImageHandler = imgHandler;

		// Constructing the Applet Parameter for Image File Upload
		// AppletParameters origParams =
		// m_PDFPageInfoManager.m_AppletParameters;
		// params.m_FileFilter = FileFilterFactory.IMAGE_FILE_FILTER;
		// params.m_AllowMultipleFileUpload = true;
		// AppletParameters params = new AppletParameters(reqType, null);
		// params.m_ServiceIdentifier = origParams.m_ServiceIdentifier;
		// HashMap appletParameters = new HashMap();

		// appletParameters.put("file_type", "IMAGE");
		// appletParameters.put("allow_multi_file_upload", "true");
		// StringBuffer errMsg = new StringBuffer();
		// System.out.println("Applet Parameters: " +
		// appletParameters.toString());
		// params.uploadRequestParams(appletParameters, errMsg);
		// params.m_CustomerId = origParams.m_CustomerId;
		// params.m_CustomerAccountId = origParams.m_CustomerAccountId;

		// final JFrame frame = new JFrame();
		// JDialog dialogBox = new JDialog(frame, "RGPT FILE UPLOADER", true);
		m_FileUploadViewer = new FileUploadViewer(this, m_Params, this);
		JPanel cp = m_FileUploadViewer.createContentPane(500, 350);
		this.setContentPane(cp);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ImageUploaderImpl.this.setVisible(false);
			}
		});
		this.setSize(500, 350);
		this.setLocation(200, 150);
		if (useFileUploadUI) {
			this.setVisible(true);
			return;
		}
		Vector files = m_FileUploadViewer.showFileChooser(false);
		this.uploadFile(null, files);
	}

	public HashMap uploadFile(AppletParameters params, Vector files)
			throws Exception {
		File imageFile = null;
		ImageHolder imgHldr = null;
		String srcPath = "", fileName = "";
		int thumbWidth = 100, thumbHt = 100;
		BufferedImage bigImage = null, thumbImage = null;
		if (files == null || files.size() == 0)
			return null;
		m_ImageHandler.showBusyCursor();
		m_ImageFileHolders = new Vector(files.size());
		for (int i = 0; i < files.size(); i++) {
			// For each file we will create a new entry in the ZIP archive and
			// stream the file into that entry.
			imageFile = (File) files.elementAt(i);
			srcPath = imageFile.getParentFile().getPath() + "/";
			fileName = imageFile.getName();
			imgHldr = ImageUtils.loadImageHolder(srcPath, fileName, thumbWidth);
			m_ImageFileHolders.addElement(imgHldr);
		}
		m_ImageHandler.handleImagesUploaded(m_ImageFileHolders,
				ImageHandlerInterface.DESKTOP_IMAGES);
		m_FileUploadViewer.finishedUpload(files);
		return null;
	}

	Vector m_ImageFileHolders;

	public void startUpload() {
		/*
		 * Construct an instance of Thread, passing the current class (i.e. the
		 * Runnable) as an argument.
		 */
		Thread t = new Thread(this);
		// t.setPriority(Thread.MAX_PRIORITY);
		t.start();
	}

	public void run() {
		try {
			m_ImageHandler.handleImagesUploaded(m_ImageFileHolders,
					ImageHandlerInterface.DESKTOP_IMAGES);
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void registerFileUploadViewer(FileUploadViewer fuViewer) {
		m_FileUploadViewer = fuViewer;
	}

}
