package net.fe.network;

import java.util.ArrayList;
import java.util.HashMap;

import chu.engine.Game;
import chu.engine.Stage;
import net.fe.Player;
import net.fe.Session;
import net.fe.lobbystage.LobbyStage;
import net.fe.network.message.CommandMessage;
import net.fe.network.message.JoinTeam;
import net.fe.network.message.KickMessage;
import net.fe.network.message.PartyMessage;
import net.fe.network.message.ReadyMessage;
import net.fe.network.serverui.FEServerFrame;
import net.fe.unit.UnitFactory;
import net.fe.unit.WeaponFactory;

// TODO: Auto-generated Javadoc
/**
 * A game that does not render anything. Manages logic only
 * @author Shawn
 *
 */
public class Lobby extends Game {

	private static Session session;
	private static int id = -2;

	/** The server. */
	private static Server server;

	/** The current stage. */
	private static Stage currentStage;

	/** The lobby. */
	public static LobbyStage lobby;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		new FEServerFrame().setVisible(true);
	}

	/**
	 * Instantiates a new FE server.
	 */
	public Lobby(Session session) {
		this(session, Server.DEFAULT_PORT);
	}

	public Lobby(Session session, int port) {
		server = new Server(port);
		this.session = session;
	}

	/**
	 * Inits the.
	 */
	public void init() {
		WeaponFactory.loadWeapons();
		UnitFactory.loadUnits();

		Thread serverThread = new Thread(server::start);
		lobby = new LobbyStage(session);
		currentStage = lobby;
		serverThread.start();
	}

	/* (non-Javadoc)
	 * @see chu.engine.Game#loop()
	 */
	@Override
	public void loop() {
		while (true) {
			final long time = System.nanoTime();
			final ArrayList<Message> messages = new ArrayList<>();
			synchronized (server.messagesLock) {
				try {
					server.messagesLock.wait(1000);
				} catch (InterruptedException e) {
					// No, really. Has there ever been a meaningful response to an InterruptedException?
				}
				server.timeoutClients();
				messages.addAll(server.messages);
				for (Message message : messages) {
					if (message instanceof JoinTeam || message instanceof ReadyMessage) {
						if (!(currentStage instanceof LobbyStage)) {
							// ignore message to prevent late-joining players from switching teams or readying up
						} else
							// TODO: percelate broadcasting of these up to stages
							server.broadcastMessage(message);
					} else if (message instanceof CommandMessage || message instanceof PartyMessage) {
						// If the unit attacked, we need to generate battle results
						// If party; don't tell others until all have selected their party
					} else if (message instanceof KickMessage) {
						// Clients are not allowed to do this.
					} else
						// TODO: percelate broadcasting of these up to stages
						server.broadcastMessage(message);

					server.messages.remove(message);
				}
			}
			for (Message m : messages)
				session.handleMessage(m);
			currentStage.beginStep(messages);
			currentStage.onStep();
			currentStage.endStep();
			timeDelta = System.nanoTime() - time;
		}
	}

	/**
	 * Sets the current stage.
	 *
	 * @param stage the new current stage
	 */
	public static void setCurrentStage(Stage stage) {
		currentStage = stage;
	}

	/**
	 * Gets the server.
	 *
	 * @return the server
	 */
	public static Server getServer() {
		return server;
	}

	/**
	 * Gets the players.
	 *
	 * @return the players
	 */
	private static HashMap<Integer, Player> getPlayers() {
		return session.getPlayerMap();
	}

	/**
	 * Reset to lobby.
	 */
	public static void resetToLobby() {
		for (Player p : getPlayers().values())
			p.ready = false;
		currentStage = lobby;
	}

	/**
	 * Reset to lobby and kick players.
	 */
	public static void resetToLobbyAndKickPlayers() {
		resetToLobby();
		kickPlayers("Reseting server");
	}
	
	public static void kickPlayers(String reason) {
		ArrayList<Integer> ids = new ArrayList<>();
		for (Player p : getPlayers().values())
			ids.add(p.getID());
		synchronized (server.messagesLock) {
			for (int i : ids) {
				final KickMessage kick = new KickMessage(0, i, reason);
				server.broadcastMessage(kick);
				server.messages.add(kick);
			}
		}
	}

	public static Session getSession() {
		return session;
	}

	public static class LobbyInfo {
		
		public final int id;
		public final Session session;
		
		private LobbyInfo(Lobby lobby) {
			this.id = lobby.id;
			this.session = lobby.session;
		}
	}
}
