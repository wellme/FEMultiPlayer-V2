package net.fe.browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.fe.network.serverui.SessionPanel;

public class LobbyCreationFrame extends JFrame {
		
	private static final long serialVersionUID = -3656992178775893254L;
	
	private SessionPanel sessionPanel;
	private JTextField txtName;
	private JPasswordField passwordField;
	
	public static void main(String[] args) {
		new LobbyCreationFrame(null).setVisible(true);;
	}
	
	public LobbyCreationFrame(ServerBrowsingStage stage) {
		setTitle("Create lobby");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		sessionPanel = new SessionPanel();
		getContentPane().add(sessionPanel, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		sessionPanel.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		
		JLabel lblName = new JLabel("Name:");
		panel_1.add(lblName);
		
		txtName = new JTextField();
		panel_1.add(txtName);
		txtName.setText("Let's play FEMP!");
		txtName.setColumns(15);
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		
		JLabel lblPassword = new JLabel("Password:");
		panel_2.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setColumns(10);
		panel_2.add(passwordField);
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3);
		
		JCheckBox chckbxHidePassword = new JCheckBox("Hide password");
		chckbxHidePassword.setSelected(true);
		chckbxHidePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				passwordField.setEchoChar(chckbxHidePassword.isSelected() ? '●' : (char) 0);
			}
		});
		panel_3.add(chckbxHidePassword);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			//passwordField.getText() is deprecated because of security, but we don't care about that.
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				stage.setAction(() -> stage.createLobby(sessionPanel.getSession(), txtName.getText(), passwordField.getText()));
				dispose();
			}
		});
		getContentPane().add(btnCreate, BorderLayout.SOUTH);
		pack();
	}

}