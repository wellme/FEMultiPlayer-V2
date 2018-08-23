package net.fe.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import net.fe.Player;
import net.fe.Session;
import net.fe.lobbystage.LobbyStage;
import net.fe.network.message.CommandMessage;
import net.fe.network.message.JoinLobby;
import net.fe.network.message.JoinTeam;
import net.fe.network.message.KickMessage;
import net.fe.network.message.PartyMessage;
import net.fe.network.message.ReadyMessage;
import net.fe.network.stage.ServerLobbyStage;
import net.fe.network.stage.ServerStage;

/**
 * A game that does not render anything. Manages logic only
 * @author Shawn
 *
 */
public class Lobby extends ServerListenerHandler {

	private Session session;
	private int id;
	private String name;
	private String password;

	private ServerStage currentStage;
	private ServerLobbyStage lobbyStage;
	
	public Lobby(int id, Session session, String name, String password) {
		this.session = session;
		this.id = id;
		this.name = name;
		this.password = password;
		init();
	}

	public void init() {
		lobbyStage = new ServerLobbyStage(this, session);
		currentStage = lobbyStage;
	}

	public void loop() {
		while (true) {
			final ArrayList<Message> messages = new ArrayList<>();
			synchronized (this.messages) {
				if(this.messages.isEmpty()) {
					try {
						this.messages.wait(10000);
					} catch (InterruptedException e) {
						// No, really. Has there ever been a meaningful response to an InterruptedException?
					}
				}
				messages.addAll(this.messages);
				timeoutClients();
				for (Message message : messages) {
					if (message instanceof JoinTeam || message instanceof ReadyMessage) {
						if (!(currentStage instanceof LobbyStage)) {
							// ignore message to prevent late-joining players from switching teams or readying up
						} else
							// TODO: percelate broadcasting of these up to stages
							broadcastMessage(message);
					} else if (message instanceof CommandMessage || message instanceof PartyMessage) {
						// If the unit attacked, we need to generate battle results
						// If party; don't tell others until all have selected their party
					} else if (message instanceof KickMessage) {
						// Clients are not allowed to do this.
					} else
						// TODO: percelate broadcasting of these up to stages
						broadcastMessage(message);

					this.messages.remove(message);
				}
			}
			for (Message m : messages)
				session.handleMessage(m);
			currentStage.beginStep(messages);
			currentStage.onStep();
			currentStage.endStep();
		}
	}
	
	public void setCurrentStage(ServerStage stage) {
		currentStage = stage;
	}

	private HashMap<Integer, Player> getPlayers() {
		return session.getPlayerMap();
	}

	public void resetToLobby() {
		for (Player p : getPlayers().values())
			p.ready = false;
		currentStage = lobbyStage;
	}

	public void resetToLobbyAndKickPlayers() {
		resetToLobby();
		kickPlayers("Reseting server");
	}
	
	public void kickPlayers(String reason) {
		ArrayList<Integer> ids = new ArrayList<>();
		for (Player p : getPlayers().values())
			ids.add(p.getID());
		synchronized (messages) {
			for (int i : ids) {
				final KickMessage kick = new KickMessage(0, i, reason);
				broadcastMessage(kick);
				messages.add(kick);
			}
		}
	}

	public LobbyInfo getLobbyInfo() {
		return new LobbyInfo(this);
	}
	
	public boolean validateJoinRequest(JoinLobby message) {
		if(password == null)
			return true;
		return password.equals(message.password);
	}

	public static class LobbyInfo implements Serializable {
		
		private static final long serialVersionUID = -5552698955020089829L;
		
		public final int id;
		public final Session session;
		public final String name;
		public final boolean hasPassword;
		
		private LobbyInfo(Lobby lobby) {
			this.id = lobby.id;
			this.session = lobby.session;
			this.name = lobby.name;
			hasPassword = lobby.password != null;
		}
	}

	public int getID() {
		return id;
	}
}
