package net.fe.browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.fe.network.serverui.SessionPanel;

public class LobbyCreationFrame extends JFrame {
		
	private static final long serialVersionUID = -3656992178775893254L;
	
	private SessionPanel sessionPanel;
	
	public LobbyCreationFrame(ServerBrowsingFrame serverBrowserFrame) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		sessionPanel = new SessionPanel();
		getContentPane().add(sessionPanel, BorderLayout.CENTER);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serverBrowserFrame.getStage().setAction(() -> serverBrowserFrame.getStage().createLobby(sessionPanel.getSession()));
				dispose();
			}
		});
		getContentPane().add(btnCreate, BorderLayout.SOUTH);
		pack();
	}

}