package net.fe.network.serverui;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.fe.Session;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SessionFrame extends JFrame {
	
	private Object lock;
	private boolean ready;
	private boolean closed;

	private static final long serialVersionUID = -5530674318359911255L;
	private SessionPanel sessionPanel;
	
	public static void main(String[] args) {
		System.out.println(SessionFrame.showSessionFrame());
	}
	
	public SessionFrame() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closed = true;
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		});
		setTitle("Lobby configuration");
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Failed to set look and feel");
		}
		
		sessionPanel = new SessionPanel();
		getContentPane().add(sessionPanel, BorderLayout.CENTER);
		
		JButton btnConfirm = new JButton("Confirm");
		btnConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ready = true;
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		});
		sessionPanel.add(btnConfirm, BorderLayout.SOUTH);
		pack();
	}

	private Session getSession() {
		if(!ready)
			return null;
		return sessionPanel.getSession();
	}


	private void setLock(Object lock) {
		this.lock = lock;
	}
	
	private boolean isReady() {
		return ready || closed;
	}

	/**
	 * Prompt the user to configure a Session.
	 * @return The session inputed by the user, or null if the frame
	 * was closed before the session was confirmed.
	 */
	public static Session showSessionFrame() {
		Object lock = new Object();
		SessionFrame sessionFrame = new SessionFrame();
		sessionFrame.setLock(lock);
		sessionFrame.setVisible(true);
		while(!sessionFrame.isReady()) {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		sessionFrame.dispose();
		return sessionFrame.getSession();
	}
}
