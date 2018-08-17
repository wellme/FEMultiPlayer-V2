package net.fe.browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.fe.FEMultiplayer;
import net.fe.network.Lobby.LobbyInfo;
import net.fe.network.message.RequestLobbyListMessage;

public class ServerBrowsingFrame extends JFrame {
		
	private static final long serialVersionUID = -4575785384747470381L;
	
	private JTextField txtSearch;
	private JTable table;
	
	private LobbyCreationFrame lobbyCreation = new LobbyCreationFrame(this);
	private ServerBrowsingStage stage;
	
	private LobbyInfo[] lobbies;
	
	public ServerBrowsingFrame(ServerBrowsingStage stage) {
		this.setStage(stage);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Server browser");
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.EAST);
		
		txtSearch = new JTextField();
		panel_2.add(txtSearch);
		txtSearch.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		panel_2.add(btnSearch);
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.WEST);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblServerAddress = new JLabel("Server IP I guess");
		panel_3.add(lblServerAddress);
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FEMultiplayer.getClient().sendMessage(new RequestLobbyListMessage());
			}
		});
		panel_1.add(btnRefresh);
		
		JButton btnCreateLobby = new JButton("Create lobby");
		btnCreateLobby.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lobbyCreation.setVisible(true);
			}
		});
		panel_1.add(btnCreateLobby);
		
		JCheckBox chckbxHideFull = new JCheckBox("Hide full");
		panel_1.add(chckbxHideFull);
		
		JCheckBox chckbxHideLocked = new JCheckBox("Hide locked");
		panel_1.add(chckbxHideLocked);
		
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					stage.setAction(() -> stage.joinLobby(getLobbyInfo(((IntModulo)table.getModel().getValueAt(table.getSelectedRow(), 0)).i)));
			}
		});
		scrollPane.setViewportView(table);
		pack();
	}
	
	public void setLobbies(LobbyInfo[] lobbies) {
		this.lobbies = lobbies;
		Object[][] objects = new Object[lobbies.length][];
		for(int i = 0; i < lobbies.length; i++) {
			Object[] line = new Object[6];
			objects[i] = line;
			line[0] = new IntModulo(lobbies[i].id);
			line[1] = "Unimplemented";
			line[2] = lobbies[i].session.getNonSpectators().length + "/2";
			line[3] = lobbies[i].session.getMap();
			line[4] = lobbies[i].session.getObjective();
			line[5] = lobbies[i].session.getModifiers().toString();
		}
		table.setModel(new DefaultTableModel(objects, new String[] {"ID", "Name", "Capacity", "Map", "Objective", "Modifiers"}) {
			
			private static final long serialVersionUID = -3180279999702434601L;

			@Override
			public boolean isCellEditable(int x, int y) {
				return false;
			}
		});
		table.setRowSorter(new TableRowSorter<TableModel>(table.getModel()));
	}
	
	public LobbyInfo getLobbyInfo(int id) {
		for(int i = 0; i < lobbies.length; i++)
			if(lobbies[i].id == id)
				return lobbies[i];
		return null;
	}
	
	public ServerBrowsingStage getStage() {
		return stage;
	}

	public void setStage(ServerBrowsingStage stage) {
		this.stage = stage;
	}

	private static class IntModulo {
		
		private static final int MODULO = 10000;
		
		private int i;
		
		public IntModulo(int i) {
			this.i = i;
		}
		
		@Override
		public String toString() {
			return "" + (i % MODULO + MODULO) % MODULO;
		}
	}
}