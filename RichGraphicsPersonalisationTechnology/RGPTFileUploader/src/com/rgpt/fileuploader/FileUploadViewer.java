/*******************************************************************************
 *
 * Rich Graphics Personalization Technology
 *
 ******************************************************************************/

package com.rgpt.fileuploader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.CursorController;
import com.rgpt.util.FileFilterFactory;
import com.rgpt.util.RGPTFileFilter;

/**
 * This File Upload Viewer supports multiple types of File Upload. orresponding
 * to the Upload Type, the business logic will change. They include: - Image
 * Upload (File Type- Image) - Here using the File Chooser, the user will be
 * able to select multiple file which will shown as Thumbview. Initially this
 * will support File upload to Server, Leter it will show thumb view in the PDF
 * Viewer for DragNDrop and further do Image Personalization on it. - Any
 * Document Upload (FileType - ALL) - - PDF Template Upload(FileType - PDF) - C
 * The Image Upload will show
 */

/*
 * FileUploadViewer.java requires these files: ImageFileView.java
 * ImageFilter.java ImagePreview.java Utils.java images/jpgIcon.gif (required by
 * ImageFileView.java) images/gifIcon.gif (required by ImageFileView.java)
 * images/tiffIcon.gif (required by ImageFileView.java) images/pngIcon.png
 * (required by ImageFileView.java)
 */

public class FileUploadViewer extends JPanel implements Runnable {
	// Column Number
	public final static int SR_NO = 0;
	public final static int FILE_ICON = 1;
	public final static int FILE_NAME = 2;
	public final static int FILE_SIZE = 3;
	public final static int FILE_SELECTED = 4;

	// File Filter used in the File Chooser
	public RGPTFileFilter m_FileFilter;

	// FileUIInfo to display information of file
	FileUIInfo m_FileUIInfo;

	// File Chooser, initialized once in the beginning
	// private JFileChooser m_FileChooser;

	// All the Files Selected from the File Choosen is put in this list. Each
	// element
	// of the Vector is the HashMap containing the Rows in the JTable.
	private Vector m_Columns;
	private Vector m_ChoosenFiles;
	private Vector m_TableData;

	// Default Table Model Holding the Table Data
	JTable m_SelectedFileTable;
	TableDataModel m_TableDataModel;

	// Buttons for Adding Files and Uploading Files
	JButton m_AddFileButton;
	JButton m_UploadFileButton;

	// Container holding this Panel
	Container m_Container;

	// Parameters
	AppletParameters m_AppletParameters;

	// File Upload Interface
	FileUploadInterface m_FileUploader;

	public FileUploadViewer(Container container, AppletParameters params,
			FileUploadInterface fileUploader) {
		// super(new BorderLayout());

		m_Container = container;
		m_FileUploader = fileUploader;
		m_AppletParameters = params;
		m_FileFilter = m_AppletParameters.getFileFilter();
		System.out.println("File Filter: " + m_FileFilter.toString());
		m_FileUploader.registerFileUploadViewer(this);

		// Initialzing the Class Variable Instances
		m_TableData = new Vector();
		m_Columns = new Vector();
		m_Columns.addElement(new ColumnData("Sr No", JLabel.class));
		m_Columns.addElement(new ColumnData("File Icon", ImageIcon.class));
		m_Columns.addElement(new ColumnData("Selected File", JLabel.class));
		m_Columns.addElement(new ColumnData("File Size (Kb)", JLabel.class));
		m_Columns.addElement(new ColumnData("Is Selected", Boolean.class));
		m_ChoosenFiles = new Vector();

		// this.initializeFileChooser();
		// Add custom icons for file types.
		m_FileUIInfo = new FileUIInfo(m_FileFilter);
	}

	JPanel m_ContentPane;
	JLabel m_DisplayLabel;

	public JPanel createContentPane(int w, int ht) {
		m_ContentPane = new JPanel();
		m_ContentPane.setPreferredSize(new Dimension(w, ht));
		System.out.println("Created Content Pane with wt: " + w + " Ht: " + ht);
		// m_ContentPane.setPreferredSize(m_PDFViewContainer.getPreferredSize());

		// Set the Layout to Border Layout
		m_ContentPane.setLayout(new BorderLayout());

		// Create the Table first to show the Files Selected, because the action
		// listener
		// needs to refer to it.
		m_SelectedFileTable = this.createTable();
		JScrollPane tableScrollPane = new JScrollPane(m_SelectedFileTable);

		JPanel addUploadFilePanel = this.addUploadFileButton();

		// Adding the Components to the File Upload Panel
		m_ContentPane.add(addUploadFilePanel, BorderLayout.NORTH);
		m_ContentPane.add(tableScrollPane, BorderLayout.CENTER);
		// m_ContentPane.add(tableScrollPane, BorderLayout.CENTER);
		return m_ContentPane;
	}

	private JTable createTable() {
		m_TableDataModel = new TableDataModel();
		JTable table = new JTable(m_TableDataModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Setting the Label Cell Renderer for displaying the File Name
		table.getColumnModel().getColumn(SR_NO)
				.setCellRenderer(new CellDataRenderer(false));
		table.getColumnModel().getColumn(FILE_NAME)
				.setCellRenderer(new CellDataRenderer(true));
		table.getColumnModel().getColumn(FILE_SIZE)
				.setCellRenderer(new CellDataRenderer(true));

		// table.setFillsViewportHeight(true);
		// Setting the Width of the Sr Num, Image Icon and the CheckBox
		table.getColumnModel().getColumn(SR_NO).setPreferredWidth(50);
		table.getColumnModel().getColumn(FILE_ICON).setPreferredWidth(75);
		table.getColumnModel().getColumn(FILE_NAME).setPreferredWidth(175);
		table.getColumnModel().getColumn(FILE_SIZE).setPreferredWidth(100);
		table.getColumnModel().getColumn(FILE_SELECTED).setPreferredWidth(100);
		table.setRowHeight(50);
		return table;
	}

	private JPanel addUploadFileButton() {
		// Defining a Panel for Add and Upload File
		int hgap = 5;
		int vgap = 5;
		JPanel addUploadFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
				hgap, vgap));
		// Setting the too tip for the Button
		String tip = "";
		if (m_FileFilter.m_FileType.equals("PDF"))
			tip = "PDF FILE";
		else if (m_FileFilter.m_FileType.equals("IMAGE"))
			tip = "IMAGE FILES";
		else if (m_FileFilter.m_FileType.equals("ZIP"))
			tip = "ZIP FILES";
		else if (m_FileFilter.m_FileType.equals("CSV"))
			tip = "CSV FILES";
		else
			tip = "ANY FILES";

		// Adding File Button
		String addFileTxt = "ADD " + tip;
		m_AddFileButton = new JButton(addFileTxt);
		// addFileButton = makeNavigationButton("addFile", addFileTxt);
		m_AddFileButton.setToolTipText(addFileTxt);
		addUploadFilePanel.add(m_AddFileButton);
		m_AddFileButton.setEnabled(true);
		m_AddFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showFileChooser(true);
				int rowCnt = m_TableDataModel.getRowCount();
				if (rowCnt == 0)
					m_UploadFileButton.setEnabled(false);
				else
					m_UploadFileButton.setEnabled(true);
				repaintUI();
			}
		});

		// Upload File Button to upload Server
		String uploadFileTxt = "UPLOAD " + tip;
		m_UploadFileButton = new JButton(uploadFileTxt);
		// addFileButton = makeNavigationButton("addFile", addFileTxt);
		m_UploadFileButton.setToolTipText(uploadFileTxt);
		addUploadFilePanel.add(m_UploadFileButton);
		m_MinimizeWindow = false;
		m_UploadFileButton.setEnabled(false);
		// m_UploadFileButton.addActionListener(new ActionListener() {
		ActionListener upldPDFAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_UploadFileButton.setEnabled(false);
				m_PauseUploadButton.setEnabled(true);
				m_PauseUpload = false;
				startUpload();
				if (!m_AppletParameters.getBoolVal("wait_for_file_upload")) {
					m_MinimizeWindow = true;
					// m_Container.setVisible(false);
				}
			}
		};
		ActionListener setCursorAction = null;
		setCursorAction = CursorController.createListener(m_Container,
				upldPDFAction, CursorController.BUSY_CURSOR);
		m_UploadFileButton.addActionListener(setCursorAction);

		// Upload File Button to upload Server
		String txt = "PAUSE UPLOAD";
		m_PauseUploadButton = new JButton(txt);
		// addFileButton = makeNavigationButton("addFile", addFileTxt);
		m_PauseUploadButton.setToolTipText(txt);
		addUploadFilePanel.add(m_PauseUploadButton);
		m_PauseUpload = false;
		m_PauseUploadButton.setEnabled(false);
		m_PauseUploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (m_PauseUpload)
					m_PauseUpload = false;
				else
					m_PauseUpload = true;
				if (!m_PauseUpload)
					m_PauseUploadButton.setText("RESUME UPLOAD");
				else
					m_PauseUploadButton.setText("PAUSE UPLOAD");
			}
		});

		// Upload File Button to upload Server
		txt = "Exit";
		JButton exitButton = new JButton(txt);
		// addFileButton = makeNavigationButton("addFile", addFileTxt);
		exitButton.setToolTipText("Close the existing Window");
		addUploadFilePanel.add(exitButton);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeUI();
			}
		});

		return addUploadFilePanel;
	}

	public boolean m_MinimizeWindow;
	boolean m_PauseUpload;
	JButton m_PauseUploadButton;

	public void repaintUI() {
		m_SelectedFileTable.updateUI();
		m_SelectedFileTable.repaint();
		m_Container.repaint();
	}

	public void startUpload() {
		/*
		 * Construct an instance of Thread, passing the current class (i.e. the
		 * Runnable) as an argument.
		 */
		Thread t = new Thread(this);
		// t.setPriority(Thread.MAX_PRIORITY);
		t.start();
	}

	Vector m_TempFiles;

	public void finishedUpload(Vector files) {
		int rowCnt = m_TableDataModel.getRowCount();
		if (rowCnt > 0)
			m_UploadFileButton.setEnabled(true);
		// Deleting the Zip File created for Image Batch Upload
		if (m_AppletParameters.m_ImageBatchUpload) {
			m_TempFiles = files;
			Boolean tempFileDel = new Boolean(
					(Boolean) java.security.AccessController
							.doPrivileged(new java.security.PrivilegedAction() {
								public Object run() {
									for (int i = 0; i < m_TempFiles.size(); i++) {
										// For each file we will create a new
										// entry in the ZIP archive and
										// stream the file into that entry.
										File f = (File) m_TempFiles
												.elementAt(i);
										RGPTFileFilter fileFilter = FileFilterFactory
												.getFileFilter(f);
										if (fileFilter == null)
											continue;
										if (fileFilter.m_FileType.equals("ZIP")) {
											System.out
													.println("Deleting file: "
															+ f.getName());
											try {
												f.delete();
											} catch (Exception ex) {
												ex.printStackTrace();
											}
										}
									}
									return new Boolean(true);
								}
							}));
		} // if (m_AppletParameters.m_ImageBatchUpload)

		m_PauseUploadButton.setEnabled(false);
		repaintUI();
		closeUI();
	}

	// This is called when this UI is shown in Applet Mode especially after the
	// PDF Approval or when the Main window is closed. This is called for
	// necessary clean up.
	public void closeUI() {
		m_Container.setVisible(false);
	}

	public void run() {
		this.uploadFile();
	}

	private void uploadFile() {
		System.out.println("In Upload File, Table Size: " + m_TableData.size());
		File file = null;
		boolean rowDileted = false;
		Vector uploadFiles = new Vector();
		for (int row = 0; row <= m_TableData.size(); row++) {
			if (rowDileted)
				row = row - 1;
			rowDileted = false;
			if (row == m_TableData.size())
				break;
			Vector rowData = (Vector) m_TableData.elementAt(row);
			Boolean boolVal = (Boolean) rowData.elementAt(FILE_SELECTED);
			if (boolVal == null || !boolVal.booleanValue())
				continue;
			uploadFiles.addElement(m_ChoosenFiles.elementAt(row));
			m_TableDataModel.removeRow(row);
			rowDileted = true;
		}
		System.out.println("Upload File: " + uploadFiles.toString());
		try {
			if (uploadFiles.size() == 0)
				return;
			m_FileUploader.uploadFile(m_AppletParameters, uploadFiles);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private HashMap selectFiles() {
		HashMap files = null;
		files = new HashMap(
				(HashMap) java.security.AccessController
						.doPrivileged(new java.security.PrivilegedAction() {
							public Object run() {
								JFileChooser fc = new JFileChooser();
								fc.addChoosableFileFilter(m_FileFilter);
								fc.setAcceptAllFileFilterUsed(false);

								// Add custom icons for file types.
								fc.setFileView(m_FileUIInfo);

								// Setting the Preview for Image Filter
								// if (m_FileFilter.m_FileType.equals("IMAGE"))
								ImagePreview imgPreview = new ImagePreview(
										m_FileUIInfo);
								fc.setAccessory(imgPreview);
								fc.addPropertyChangeListener(imgPreview);

								// If ths PDF needs to be uploaded, this is the
								// case of Template being
								// uploaded. Only one Template can be uploaded
								// at any time and hence
								// set to false. All other cases for Images and
								// ALL Doucument Types like
								// PDF, PPT, Word, etc the multi-selection value
								// is set to true.
								if (m_FileFilter.m_FileType.equals("PDF")
										|| !m_AppletParameters
												.getBoolVal("allow_multi_file_upload")) {
									fc.setMultiSelectionEnabled(false);
									// If the File Chooser is used for PDF Batch
									// Processing using
									// Image then only Directory Selection is
									// allowed.
									if (m_AppletParameters.m_ImageBatchUpload)
										fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
									else
										fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
								} else {
									// In case of "ALL" or "Image" Mode many
									// files and Directory can be selected.
									// Hence the settings.
									fc.setMultiSelectionEnabled(true);
									fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
								}
								// Show the File Chooser with Appropriate text
								// to add Files
								int returnVal = -1;
								returnVal = fc.showDialog(
										FileUploadViewer.this,
										m_AddFileButton.getToolTipText());
								Vector files = new Vector();
								HashMap filesHolder = new HashMap();
								filesHolder.put("Files", files);
								if (returnVal != JFileChooser.APPROVE_OPTION)
									return filesHolder;
								File file = fc.getSelectedFile();
								// This is executed when only one file can be
								// uploaded at a time
								if (!m_AppletParameters
										.getBoolVal("allow_multi_file_upload")
										&& !file.isDirectory()) {
									files.addElement(file);
									fc.setSelectedFile(null);
									return filesHolder;
								}
								File[] selFiles = null;
								if (m_AppletParameters.m_ImageBatchUpload) {
									selFiles = file.listFiles();
									// Create a Temporary File
									try {
										File zipBatchFile = File
												.createTempFile("RGPTBatch_"
														+ file.getName(),
														".zip", file);
										System.out.println("Directory Name: "
												+ file.getName()
												+ " ZIP File Name: "
												+ zipBatchFile.getName());
										filesHolder.put("TempBatchFile",
												zipBatchFile);
										// Deleting the Temp File on exit
										// zipBatchFile.deleteOnExit();
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								} else if (file.isDirectory())
									selFiles = file
											.listFiles((FileFilter) m_FileFilter);
								else
									selFiles = fc.getSelectedFiles();
								for (int i = 0; i < selFiles.length; i++) {
									File selFile = selFiles[i];
									String ext = RGPTFileFilter
											.getExtension(selFile);
									System.out.println("Ext Found: " + ext
											+ " for File: " + selFile.getName());
									RGPTFileFilter fileFilter = FileFilterFactory
											.getFileFilter(selFile);
									if (fileFilter == null)
										continue;
									if (fileFilter.m_FileType.equals("ZIP")) {
										if (selFile.getName().startsWith(
												"RGPTBatch_"))
											selFile.delete();
										continue;
									}
									files.addElement(selFiles[i]);
								}
								fc.setSelectedFile(null);
								return filesHolder;
							} // public Object run()
						} // java.security.PrivilegedAction()
						) // java.security.AccessController.doPrivileged
		); // Return Vectors from Security Handler and creates a new Vector
			// Object
		System.out.println("Files Data: " + files.toString());
		return files;
	}

	public Vector showFileChooser(boolean showFileUploadUI) {
		// This is the scenario when Template PDF is selected for upload
		int row = 0;
		File file = null;
		HashMap files = this.selectFiles();
		Vector selFiles = (Vector) files.get("Files");
		if (selFiles == null || selFiles.size() == 0)
			return selFiles;
		if (!showFileUploadUI)
			return selFiles;
		int rowCnt = m_TableDataModel.getRowCount();
		if (m_FileFilter.m_FileType.equals("PDF")) {
			file = (File) selFiles.firstElement();
			// Restricting Multiple Files to be uploaded
			if (rowCnt == 1)
				m_TableDataModel.removeRow(row);
			this.addSelectedFile(file, row);
			return null;
		}

		// Process the results for Images or multi-file upload.
		row = rowCnt;
		try {
			if (m_AppletParameters.m_ImageBatchUpload) {
				File f = null;
				ZipEntry entry = null;
				// Get the Temporary Batch File
				File zipBatchFile = (File) files.get("TempBatchFile");
				// Zip all the Files in the Directory and write it to the Temp
				// File
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
						zipBatchFile, true));
				for (int i = 0; i < selFiles.size(); i++) {
					// For each file we will create a new entry in the ZIP
					// archive and
					// stream the file into that entry.
					f = (File) selFiles.elementAt(i);
					entry = new ZipEntry(f.getName());
					out.putNextEntry(entry);
					InputStream in = new FileInputStream(f);
					int read;
					byte[] buf = new byte[1024];
					double avgBuffSize = 0.0;
					int counter = 0;
					while ((read = in.read(buf)) > 0) {
						counter++;
						avgBuffSize = (avgBuffSize + read) / 2;
						out.write(buf, 0, read);
					}
					System.out.println("Write: " + counter + " times"
							+ " Avg Buff Size: " + avgBuffSize
							+ " Total Size: " + counter * avgBuffSize);
					out.closeEntry();
				}

				// Once we are done writing out our stream we will finish
				// building the
				// archive and close the stream.
				out.finish();
				out.close();

				// Add the selected file for upload
				this.addSelectedFile(zipBatchFile, row);
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		for (int i = 0; i < selFiles.size(); i++) {
			this.addSelectedFile((File) selFiles.elementAt(i), row);
			row++;
		}
		return selFiles;
	}

	private void addSelectedFile(File file, int row) {
		boolean isFileSel = true;
		Vector rowData = new Vector(m_Columns.size());
		String ext = m_FileFilter.getExtension(file);
		ImageIcon fileIcon = null;
		if (FileFilterFactory.IMAGE_FILE_FILTER.accept(ext)) {
			Image scaledImg = ImageUtils.loadImage(file.getPath(), 50);
			fileIcon = new ImageIcon(scaledImg);
		} else
			fileIcon = (ImageIcon) m_FileUIInfo.m_FileIcon.get(ext);
		// This is used if the actual Image Icon is loaded into JTable
		// Image scaledImg = ImageUtils.loadImage(m_ImageFile.getPath(), 90);
		// m_ImageThumbnail = new ImageIcon(scaledImg);
		rowData.addElement(String.valueOf(row + 1));
		rowData.addElement(fileIcon);
		rowData.addElement(file.getName());
		rowData.addElement(String.valueOf(file.length() / 1000));
		rowData.addElement(new Boolean(isFileSel));
		m_TableDataModel.insertRow(row, rowData, file);
		this.setImageObserver(row, FILE_ICON);
	}

	private void setImageObserver(int row, int col) {
		ImageIcon icon = (ImageIcon) m_TableDataModel.getValueAt(row, col);
		if (icon != null)
			icon.setImageObserver(new CellImageObserver(row, col));
	}

	class ColumnData {
		String m_ColumnName;
		Class m_ColumnClass;

		ColumnData(String colName, Class colClass) {
			m_ColumnName = colName;
			m_ColumnClass = colClass;
		}
	}

	class TableDataModel extends AbstractTableModel {
		public int getColumnCount() {
			return m_Columns.size();
		}

		public int getRowCount() {
			return m_TableData.size();
		}

		public String getColumnName(int col) {
			ColumnData colData = (ColumnData) m_Columns.elementAt(col);
			return colData.m_ColumnName;
		}

		public Object getValueAt(int row, int col) {
			Vector rowData = (Vector) m_TableData.elementAt(row);
			return rowData.elementAt(col);
		}

		public Class getColumnClass(int col) {
			ColumnData colData = (ColumnData) m_Columns.elementAt(col);
			return colData.m_ColumnClass;
		}

		public void removeRow(int row) {
			m_TableData.removeElementAt(row);
			m_ChoosenFiles.removeElementAt(row);
		}

		public void insertRow(int row, Vector rowData, File file) {
			m_TableData.insertElementAt(rowData, row);
			m_ChoosenFiles.insertElementAt(file, row);
		}

		public boolean isCellEditable(int row, int col) {
			ColumnData colData = (ColumnData) m_Columns.elementAt(col);
			// System.out.println("Col Data: " + colData.m_ColumnName +
			// " ClassName: " + colData.m_ColumnClass.getName());
			if (!(colData.m_ColumnClass.getName().equals(Boolean.class
					.getName())))
				return false;
			// System.out.println("ClassName: " + Boolean.class.getName());
			// fireTableCellUpdated(row, col);
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			// System.out.println("Setting value at " + row + "," + col + " to "
			// + value
			// + " (an instance of " + value.getClass() + ")");
			Vector rowData = (Vector) m_TableData.elementAt(row);
			Boolean boolVal = (Boolean) rowData.remove(FILE_SELECTED);
			if (boolVal == null)
				return;
			if (boolVal.booleanValue())
				boolVal = new Boolean(false);
			else
				boolVal = new Boolean(true);
			rowData.addElement(boolVal);
			System.out.println("Row Data: " + m_TableData.toString());
		}
	}

	class CellDataRenderer extends DefaultTableCellRenderer {
		boolean m_DisplayFileName;

		public CellDataRenderer(boolean displayFileName) {
			m_DisplayFileName = displayFileName;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			// System.out.println("Cell Class: " + value.getClass().toString() +
			// " Value: " + value.toString());
			setHorizontalAlignment(JLabel.CENTER);
			setText((String) value);
			if (!m_DisplayFileName)
				return this;
			File file = (File) m_ChoosenFiles.elementAt(row);
			setToolTipText(file.getAbsolutePath());
			return this;
		}
	}

	class CellImageObserver implements ImageObserver {
		int row;
		int col;

		CellImageObserver(int row, int col) {
			this.row = row;
			this.col = col;
		}

		public boolean imageUpdate(Image img, int flags, int x, int y, int w,
				int h) {
			if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
				Rectangle rect = m_SelectedFileTable.getCellRect(row, col,
						false);
				m_SelectedFileTable.repaint(rect);
			}
			return (flags & (ALLBITS | ABORT)) == 0;
		}
	}

	public static void main(String[] args) {
		String url = "http://localhost:8080/RGPTServices/FileUploadServer";
		JFrame frame = new JFrame("RGPT FILE UPLOAD VIEWER");
		FileUploadViewer fuViewer = null;
		String reqType = AppletParameters.UPLOAD_BATCH_FILE;
		// java.net.URL urlObj = new java.net.URL(url);
		// System.out.println("URL: " + urlObj.toString());

		AppletParameters params = new AppletParameters(reqType, null);
		params.m_ServiceIdentifier = 1;
		HashMap appletParameters = new HashMap();
		appletParameters.put("customer_id", 1);
		appletParameters.put("customer_account_id", 1);
		appletParameters.put("template_id", 1);
		StringBuffer errMsg = new StringBuffer();
		System.out.println("Applet Parameters: " + appletParameters.toString());
		params.uploadRequestParams(appletParameters, errMsg);
		try {
			fuViewer = new FileUploadViewer(frame, params, new HTTPFileUpload());
			JPanel cp = fuViewer.createContentPane(500, 350);
			frame.setContentPane(cp);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			frame.setSize(500, 350);
			frame.setLocation(100, 100);
			frame.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
