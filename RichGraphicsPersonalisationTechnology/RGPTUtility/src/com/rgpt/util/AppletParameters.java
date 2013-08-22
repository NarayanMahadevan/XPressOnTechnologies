// RGPT PACKAGES
package com.rgpt.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AppletParameters {
	public static String LOAD_APPLET_REQUEST = "Load Applet Request";
	public static String EDIT_PDF_TEMPLATE_REQUEST = "Edit PDF Template";
	public static String VIEW_PDF_REQUEST = "View PDF Request";
	public static String EDIT_SAVED_PDF_REQUEST = "Edit Save PDF Request";
	public static String APPROVE_PDF_REQUEST = "Approve PDF Request";
	public static String VIEW_IMAGE_ASSETS = "View Customer Assets";
	public static String UPLOAD_BATCH_FILE = "Upload Batch File";
	public static String UPLOAD_PSP_ASSET = "Upload PSP Asset";
	public static String UPLOAD_UPDATED_ASSET = "Upload Updated Asset";
	public static String UPLOAD_DIGITAL_ASSETS = "Upload Digital Assets";

	// Asset Types Supported
	public static String PDF_TEMPLATE_ASSET = "PDF Template Asset";
	public static String FAST_SHOPPING_ASSET = "Fast Shopping Asset";

	public static String m_RequestType;
	public static double m_TransferRate;
	public int m_FrameWidth;
	public int m_FrameHeight;
	public static int m_ServiceIdentifier;
	public static int m_DigitalAssetId;
	/*
	 * public int m_CustomerId; public int m_CustomerAccountId; public int
	 * m_TemplateId; public int m_PDFPageCount; public String m_SessionId;
	 * public int m_CustomerBasketId; public int m_SessionAssetId;
	 * 
	 * // This variable is used to identify the type of PSP Asset. This includes
	 * assets // for Fast Shopping Asset, PDF Template Asset, etc public String
	 * m_AssetType;
	 */
	// This indicates the Server that will execute the request in the backend.
	public static String m_ServerName;

	// URL from where the Applet got loaded
	public static URL m_AppletURL;

	// This parameters are added by PDFPageInfoManager as place holders to use
	// in future Server Calls
	public static String m_SrcDir;
	public static String m_SerializedFileName;

	// This variables are used to set the PDF Approval with in EBook Viewer
	public static boolean m_ImageBatchUpload;

	/*
	 * // This indicates the required applet parameters for each type of request
	 * public boolean m_IsEditPDFParametersLoaded;
	 * 
	 * // This indicates if multiple files can be uploaded public boolean
	 * m_AllowMultipleFileUpload;
	 * 
	 * // This is set to do File Upload in the Background or otherwise public
	 * boolean m_WaitForFileUpload;
	 * 
	 * // This variable is used to enable or disable page animation with in
	 * EBook // Viewer. If page animation is enable, animation speed can also be
	 * set. public boolean m_EnablePageAnimation; public int
	 * m_PageAnimationSpeed; public int m_PageAnimationStep;
	 * 
	 * // This variables are used to set the PDF Approval with in EBook Viewer
	 * public boolean m_EnablePDFApproval; public int m_BatchId; public int
	 * m_TemplatePageCount;
	 * 
	 * // File Filter to identify the file types for upload public
	 * RGPTFileFilter m_FileFilter;
	 * 
	 * // Order and Job id Field needed when updated asset for job is uploaded
	 * public int m_OrderId; public int m_OrderJobId;
	 */
	public static Map<String, String[]> m_RequestParamSet = new HashMap<String, String[]>();
	public static Map<String, Boolean> m_RequestReqParamSet = new HashMap<String, Boolean>();
	public static Map<String, String> m_RequestParamValues = new HashMap<String, String>();

	public static final String[] m_EditPDFParameters = { "psp_id",
			"customer_id", "customer_basket_id", "template_id", "page_count",
			"server_name", "data_source_name", "pdf_viewer_name" };

	public static final String[] m_ViewPDFParameters = { "psp_id",
			"digital_asset_id", "page_count", "enable_page_animation",
			"page_animation_speed", "page_animation_step",
			"enable_pdf_approval", "batch_id", "template_id",
			"template_page_count", "server_name" };

	public static final String[] m_ViewImageAsset = { "psp_id", "customer_id",
			"server_name" };

	public static final String[] m_UploadUpdatedDigitalAsset = { "psp_id",
			"customer_account_id", "customer_id", "orig_asset_id",
			"upload_file_path", "order_id", "order_job_id",
			"wait_for_file_upload", "server_name", "file_type" };

	public static final String[] m_UploadDigitalAsset = { "psp_id",
			"customer_account_id", "customer_id", "file_type",
			"allow_multi_file_upload", "wait_for_file_upload", "server_name",
			"upload_file_path" };

	public static final String[] m_UploadBatchAsset = { "psp_id",
			"customer_account_id", "customer_id", "template_id", "file_type",
			"wait_for_file_upload", "server_name" };

	public static final String[] m_UploadPSPAsset = { "psp_id", "session_id",
			"file_type", "asset_type", "wait_for_file_upload", "server_name" };

	public static final String[] m_EditSavedPDFParameters = { "session_id",
			"template_id", "customer_id", "digital_asset_id", "server_name" };

	public static final String[] m_ApprovePDFParameters = { "psp_id",
			"customer_id", "customer_basket_id", "digital_asset_id",
			"page_count", "server_name" };

	static {
		m_RequestParamSet.put(EDIT_PDF_TEMPLATE_REQUEST, m_EditPDFParameters);
		m_RequestParamSet.put(VIEW_PDF_REQUEST, m_ViewPDFParameters);
		m_RequestParamSet.put(VIEW_IMAGE_ASSETS, m_ViewImageAsset);
		m_RequestParamSet.put(UPLOAD_DIGITAL_ASSETS, m_UploadDigitalAsset);
		m_RequestParamSet.put(UPLOAD_PSP_ASSET, m_UploadPSPAsset);
		m_RequestParamSet
				.put(UPLOAD_UPDATED_ASSET, m_UploadUpdatedDigitalAsset);
		m_RequestParamSet.put(UPLOAD_BATCH_FILE, m_UploadBatchAsset);
		m_RequestParamSet.put(EDIT_SAVED_PDF_REQUEST, m_EditSavedPDFParameters);
		m_RequestParamSet.put(APPROVE_PDF_REQUEST, m_ApprovePDFParameters);
		m_RequestReqParamSet.put(EDIT_PDF_TEMPLATE_REQUEST, true);
		m_RequestReqParamSet.put(VIEW_PDF_REQUEST, false);
		m_RequestReqParamSet.put(VIEW_IMAGE_ASSETS, true);
		m_RequestReqParamSet.put(UPLOAD_DIGITAL_ASSETS, true);
		m_RequestReqParamSet.put(UPLOAD_PSP_ASSET, true);
		m_RequestReqParamSet.put(UPLOAD_UPDATED_ASSET, true);
		m_RequestReqParamSet.put(UPLOAD_BATCH_FILE, true);
		m_RequestReqParamSet.put(EDIT_SAVED_PDF_REQUEST, true);
		m_RequestReqParamSet.put(APPROVE_PDF_REQUEST, true);
	}

	// Default Constructor used by Standalone Application
	public AppletParameters() {
	}

	// This constructor is invoked when the call is made from the Applet and
	// needs
	// Server Interaction
	public AppletParameters(String reqType, java.net.URL appletURL) {
		m_AppletURL = appletURL;
		m_RequestType = reqType;
		m_DigitalAssetId = -1;
		m_ServiceIdentifier = -1;
		m_ImageBatchUpload = false;
		/*
		 * m_SessionId = null; m_CustomerId = -1; m_CustomerAccountId = -1;
		 * m_TemplateId = -1; m_PDFPageCount = -1; m_SessionAssetId = -1;
		 * m_CustomerBasketId = -1; m_WaitForFileUpload = true; m_BatchId = -1;
		 * m_TemplatePageCount = -1; m_IsEditPDFParametersLoaded = false;
		 * m_AllowMultipleFileUpload = false; m_PageAnimationSpeed = 10;
		 * m_PageAnimationStep = 16; m_EnablePDFApproval= false;
		 * m_EnablePageAnimation = false; m_OrderId = -1; m_OrderJobId = -1;
		 */
	}

	public static void populateAppletParams() {
		m_ServiceIdentifier = getIntVal("ServiceIdentifier");
		m_ServerName = getVal("ServerName");
		String url = AppletParameters.getVal("CodeBase");
		try {
			m_AppletURL = new URL(url);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isRequestValid(String reqName) {
		String[] reqSet = m_RequestParamSet.get(reqName);
		if (reqSet == null)
			return false;
		return true;
	}

	public boolean uploadRequestParams(HashMap appletParameters,
			StringBuffer errMsg) {
		String value = "";
		boolean isSuccess = true;
		String[] reqParams = m_RequestParamSet.get(m_RequestType);
		boolean isAllParamReq = ((Boolean) m_RequestReqParamSet
				.get(m_RequestType)).booleanValue();
		for (int i = 0; i < reqParams.length; i++) {
			value = (String) appletParameters.get(reqParams[i]);
			if (value == null) {
				if (!isAllParamReq)
					continue;
				isSuccess = false;
				errMsg.append("Invalid " + reqParams[i] + "\n");
				continue;
			}

			if (reqParams[i] == "psp_id")
				m_ServiceIdentifier = Integer.parseInt(value);
			else if (reqParams[i] == "server_name")
				m_ServerName = value;
			else if (reqParams[i] == "digital_asset_id")
				m_DigitalAssetId = Integer.parseInt(value);
			else if (reqParams[i] == "asset_type") {
				if (!isAssetTypeValid(value)) {
					isSuccess = false;
					errMsg.append("Invalid Asset Type Defines" + "\n");
					continue;
				}
			}
			if (reqParams[i] == "file_type") {
				if (!checkFileType(value)) {
					isSuccess = false;
					errMsg.append("Invalid File Type Defined " + "\n");
					continue;
				}
			}

			m_RequestParamValues.put(reqParams[i], value);
		}
		RGPTLogger.logToConsole("Request Params for Request: " + m_RequestType
				+ " are: " + m_RequestParamValues.toString());
		return isSuccess;
	}

	public boolean checkFileType(String value) {
		RGPTFileFilter fileFilter = FileFilterFactory
				.getFileFilter4FileType(value);
		if (fileFilter == null)
			return false;
		if (m_RequestType.equals(UPLOAD_BATCH_FILE)
				&& fileFilter.m_FileType.equals("ZIP"))
			m_ImageBatchUpload = true;
		return true;
	}

	public boolean isAssetTypeValid(String assetType) {
		if (!assetType.equals(PDF_TEMPLATE_ASSET)) {
			if (!assetType.equals(FAST_SHOPPING_ASSET))
				return false;
		}
		return true;
	}

	public static void putVal(String name, String value) {
		m_RequestParamValues.put(name, value);
	}

	public static String getVal(String name) {
		return getVal(name, "");
	}

	public static String getVal(String name, String defVal) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			val = defVal;
		return val;
	}

	public static int getIntVal(String name) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return -1;
		return Integer.parseInt(val);
	}

	public static float getFloatVal(String name) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return 0.0f;
		return Float.parseFloat(val);
	}

	public static boolean getBoolVal(String name) {
		return getBoolVal(name, false);
	}

	public static boolean getBoolVal(String name, boolean defVal) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return defVal;
		return Boolean.parseBoolean(val);
	}

	public static RGPTFileFilter getFileFilter() {
		String name = "file_type";
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return null;
		return FileFilterFactory.getFileFilter4FileType(val);
	}

	public String toString() {
		StringBuffer mesg = new StringBuffer("Applet Parameters for: "
				+ m_RequestType);
		mesg.append(" is: Service Provider Id: " + m_ServiceIdentifier);
		mesg.append(" Server Name: " + m_ServerName);
		mesg.append(" " + m_RequestParamValues.toString());
		if (m_AppletURL != null)
			mesg.append("\n URL: " + m_AppletURL.toString());
		return mesg.toString();
	}
}
