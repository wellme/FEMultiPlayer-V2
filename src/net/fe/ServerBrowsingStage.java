package net.fe;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

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

import org.newdawn.slick.Color;

import chu.engine.ClientStage;
import chu.engine.Entity;
import chu.engine.Game;
import chu.engine.menu.Notification;
import net.fe.lobbystage.ClientLobbyStage;
import net.fe.network.Lobby.LobbyInfo;
import net.fe.network.Message;
import net.fe.network.message.CreateLobby;
import net.fe.network.message.JoinLobby;
import net.fe.network.message.LobbyListMessage;
import net.fe.network.message.RequestLobbyListMessage;
import net.fe.network.serverui.SessionPanel;

public class ServerBrowsingStage extends ClientStage {
	
	private ServerBrowserFrame frame;
	private Runnable action;
	
	private static final String TEXT = "Another window containing a lobby browser was opened";
	private static final String FONT = "default_med";
	private static final float TEXT_X = Game.getWindowWidth() / 2 - FEResources.getBitmapFont(FONT).getStringWidth(TEXT) / 2;
	private static final float TEXT_Y = Game.getWindowHeight() / 2;
	

	public ServerBrowsingStage() {
		super(null);
		addEntity(new RunesBg(Color.gray));
		addEntity(new Notification(TEXT_X, TEXT_Y, FONT, TEXT, Float.MAX_VALUE, 1));
		frame = new ServerBrowserFrame(this);
		frame.setVisible(true);
		FEMultiplayer.getClient().sendMessage(new RequestLobbyListMessage());
	}

	@Override
	public void beginStep(List<Message> messages) {
		for(Message message : messages)
			executeMessage(message);
		if(action != null) {
			action.run();
			action = null;
		}
		for(Entity e : entities)
			e.beginStep();
	}

	@Override
	public void onStep() {
		for(Entity e : entities)
			e.onStep();
	}

	@Override
	public void endStep() {
		for(Entity e : entities)
			e.endStep();
	}
	
	private void executeMessage(Message message) {
		if(message instanceof LobbyListMessage) {
			LobbyListMessage list = (LobbyListMessage) message;
			frame.setLobbies(list.lobbies);
		}
	}
	
	private void joinLobby(LobbyInfo info) {
		FEMultiplayer.lobby = new ClientLobbyStage(info.id, info.session);
		FEMultiplayer.getClient().setSession(info.session);
		FEMultiplayer.setCurrentStage(FEMultiplayer.lobby);
		FEMultiplayer.getClient().sendMessage(new JoinLobby(info.id, FEMultiplayer.getLocalPlayer().getName()));
		frame.dispose();
	}
	
	private void createLobby(Session session) {
		FEMultiplayer.lobby = new ClientLobbyStage(session);
		FEMultiplayer.getClient().setSession(session);
		FEMultiplayer.setCurrentStage(FEMultiplayer.lobby);
		FEMultiplayer.getClient().sendMessage(new CreateLobby(session));
		frame.dispose();
	}

	
	
	public static class ServerBrowserFrame extends JFrame {
		
		private static final long serialVersionUID = -4575785384747470381L;
		
		private JTextField txtSearch;
		private JTable table;
		
		private LobbyCreationFrame lobbyCreation = new LobbyCreationFrame(this);
		private ServerBrowsingStage stage;
		
		private LobbyInfo[] lobbies;
		
		public ServerBrowserFrame(ServerBrowsingStage stage) {
			this.stage = stage;
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
						stage.action = () -> stage.joinLobby(getLobbyInfo(((IntModulo)table.getModel().getValueAt(table.getSelectedRow(), 0)).i));
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

	public static class LobbyCreationFrame extends JFrame {
		
		private static final long serialVersionUID = -3656992178775893254L;
		
		private SessionPanel sessionPanel;
		
		public LobbyCreationFrame(ServerBrowserFrame serverBrowserFrame) {
	
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
					serverBrowserFrame.stage.action = () -> serverBrowserFrame.stage.createLobby(sessionPanel.getSession());
					dispose();
				}
			});
			getContentPane().add(btnCreate, BorderLayout.SOUTH);
			pack();
		}
	
	}
}
