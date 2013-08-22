package com.rgpt.templatemaker;

//---------------------------------------------------------------------------------------
// Copyright (c) 2012-2013 by XPressOn Technologies Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.rgpt.pdflib.PDFLibException;

public class PasswordDialog {
	public static int NUM_TRIES = 3;

	static boolean isSuccessful(Container parent, com.rgpt.pdflib.PDFDoc doc)
			throws PDFLibException {
		for (int i = 0; i < NUM_TRIES; i++) {
			final JPasswordField jpf = new JPasswordField();
			JOptionPane jop = new JOptionPane(jpf,
					JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			JDialog dialog = jop.createDialog(parent, "Password:");
			dialog.addComponentListener(new ComponentAdapter() {
				public void componentShown(ComponentEvent e) {
					jpf.requestFocusInWindow();
				}
			});
			dialog.setVisible(true);
			int result = 0;
			if (jop.getValue() != null)
				result = ((Integer) jop.getValue()).intValue();
			else
				result = JOptionPane.CANCEL_OPTION;
			dialog.dispose();
			String password = null;
			if (result == JOptionPane.OK_OPTION) {
				password = new String(jpf.getPassword());
				if (doc.isAuthorizedToView(password))
					return true;
				else {
					JOptionPane.showMessageDialog(parent, "Authorize Failed.");
				}
			} else
				return false;
		}
		return false;
	}
}
