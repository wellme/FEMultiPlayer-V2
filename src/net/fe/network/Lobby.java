package net.fe.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import chu.engine.Game;
import net.fe.Player;
import net.fe.Session;
import net.fe.lobbystage.LobbyStage;
import net.fe.network.message.CommandMessage;
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
public class Lobby extends Game implements MessageHandler {

	private Session session;
	private int id;

	private ArrayList<Message> messages = new ArrayList<>();

	private ServerStage currentStage;
	public ServerLobbyStage lobbyStage;
	
	private ArrayList<Message> broadcastedMessages = new ArrayList<>();
	private ArrayList<ServerListener> listeners = new ArrayList<>();

	public Lobby(int id, Session session) {
		this.session = session;
		this.id = id;
		init();
	}

	public void init() {
		lobbyStage = new ServerLobbyStage(this, session);
		currentStage = lobbyStage;
	}

	@Override
	public void loop() {
		while (true) {
			final long time = System.nanoTime();
			final ArrayList<Message> messages = new ArrayList<>();
			synchronized (this.messages) {
				try {
					this.messages.wait(10000);
				} catch (InterruptedException e) {
					// No, really. Has there ever been a meaningful response to an InterruptedException?
				}
				messages.addAll(this.messages);
				System.out.println(this.messages.toString() + "\t" + messages.toString());
				//timeoutClients();
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
			timeDelta = System.nanoTime() - time;
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

	@Override
	public void addMessage(Message message) {
		synchronized(messages) {
			messages.add(message);
			messages.notifyAll();
		}
	}

	@Override
	public ArrayList<Message> getBroadcastedMessages() {
		return broadcastedMessages;
	}

	@Override
	public void broadcastMessage(Message message) {
		broadcastedMessages.add(message);
		System.out.println("Lobby broadcasting message " + message);
		synchronized (listeners) {
			for(ServerListener listener : listeners)
				listener.sendMessage(message);
		}
	}

	@Override
	public void addListener(ServerListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
			listener.setDestination(this);
		}
	}
	
	public LobbyInfo getLobbyInfo() {
		return new LobbyInfo(this);
	}

	public static class LobbyInfo implements Serializable {
		
		private static final long serialVersionUID = -5552698955020089829L;
		
		public final int id;
		public final Session session;
		
		private LobbyInfo(Lobby lobby) {
			this.id = lobby.id;
			this.session = lobby.session;
		}
	}
}
