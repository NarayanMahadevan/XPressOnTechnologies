// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rgpt.layoututil.ParagraphLayout;
import com.rgpt.serverhandler.LoginHandlerInterface;
import com.rgpt.util.RGPTUIManager;

public class RGPTLoginUI extends JDialog implements ActionListener {
	JTextField m_UserName;
	JPasswordField m_UserPassword;
	JButton m_LoginButton;
	LoginHandlerInterface m_LoginHandler;

	public RGPTLoginUI(LoginHandlerInterface loginHdlr) {
		// Setting the UI Component
		m_LoginHandler = loginHdlr;
		JLabel userLabel = new JLabel("Username");
		JLabel passLabel = new JLabel("Password");
		m_UserName = new JTextField(15);
		m_UserName.setText(m_LoginHandler.getUserName());
		m_UserPassword = new JPasswordField(15);
		m_UserPassword.setText(m_LoginHandler.getUserPassword());
		m_LoginButton = new JButton("login");
		m_LoginButton.setMargin(new Insets(0, 0, 0, 0));

		// Defining the Layout
		JPanel login_Panel = new JPanel();
		JPanel button_Panel = new JPanel();
		JPanel main_Panel = new JPanel();
		login_Panel.setLayout(new ParagraphLayout());
		login_Panel.add(userLabel, ParagraphLayout.NEW_PARAGRAPH);
		login_Panel.add(m_UserName);
		login_Panel.add(passLabel, ParagraphLayout.NEW_PARAGRAPH);
		login_Panel.add(m_UserPassword);
		button_Panel.add(m_LoginButton);
		m_LoginButton.addActionListener(this);
		main_Panel.add(login_Panel);
		main_Panel.add(button_Panel);

		// Setting the User Interface
		this.setSize(300, 150);
		this.setLocation(300, 200);
		this.getContentPane().add(main_Panel);
		this.setVisible(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(m_LoginButton)) {
			String username = m_UserName.getText();
			char[] pass = m_UserPassword.getPassword();
			System.out.println("user name is  " + username);
			System.out.println("password is  " + pass);
			boolean valid = m_LoginHandler.validateUser(username, new String(
					pass));
			if (!valid) {
				System.out.println(" User is not valid ");
			}
		}
	}

	public static void main(String args[]) {
		RGPTUIManager.setLookAndFeel();

		/*
		 * // BLUE SHADES // lightcolor for panel Color colorpanel = new
		 * Color(204, 204, 255); // darkercolor for button Color colorbg = new
		 * Color(153, 153, 255); // darkestcolor for text Color colordark = new
		 * Color(214, 255, 255);
		 * 
		 * 
		 * //DARK BLUE SHADES //lightcolor for panel Color colorpanel = new
		 * Color(65, 160, 255); //darkercolor for button Color colorbg = new
		 * Color(153, 255, 255); //darkestcolor for text Color colordark = new
		 * Color(214, 255,255);
		 */
		// GREEN SHADES
		// lightcolor for panel
		Color colorpanel = new Color(51, 255, 153);
		// darkercolor for button
		Color colorbg = new Color(0, 102, 51);
		// darkestcolor for text
		Color colordark = new Color(214, 255, 255);

		RGPTUIManager.setUIDefaults(colorpanel, colorbg, colordark);

		// new RGPTLoginUI();
	}

}
