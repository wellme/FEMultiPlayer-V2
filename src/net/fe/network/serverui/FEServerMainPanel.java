package net.fe.network.serverui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.fe.Session;
import net.fe.network.Server;

public class FEServerMainPanel extends JPanel {
	
	private static final long serialVersionUID = 6495278416009469008L;
	
	private JSpinner spnPort;
	private Runnable serverStart;
	
	public FEServerMainPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel_1.add(panel, BorderLayout.CENTER);
		
		JLabel label = new JLabel("Port:");
		label.setToolTipText("Don't change this unless you know what you're doing!");
		panel.add(label);
		
		spnPort = new JSpinner();
		spnPort.setToolTipText("Don't change this unless you know what you're doing!");
		spnPort.setModel(new SpinnerNumberModel(Server.DEFAULT_PORT, 0, 65565, 1));
		panel.add(spnPort);
		
		JButton button = new JButton("Start server");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(serverStart != null)
					serverStart.run();
			}
		});
		panel_1.add(button, BorderLayout.SOUTH);
	}

	/**
	 * Sets the runnable that should be executed when the "Start server" button is pressed.<BR>
	 * Note : the runnable will not be executed in a separate thread.
	 * @param serverStart the runnable that should be executed when the "Start server" button is pressed.
	 */
	public void setServerStartRunnable(Runnable serverStart) {
		this.serverStart = serverStart;
	}
	
	/**
	 * Returns the port selected.
	 * @return The port.
	 */
	public int getPort() {
		return (Integer) spnPort.getValue();
	}
}
