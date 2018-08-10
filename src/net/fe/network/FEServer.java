package net.fe.network;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import net.fe.Session;
import net.fe.network.Lobby.LobbyInfo;
import net.fe.network.message.CreateLobby;
import net.fe.network.message.JoinLobby;
import net.fe.network.message.JoinServer;
import net.fe.network.message.KickMessage;
import net.fe.network.message.LobbyListMessage;
import net.fe.network.message.RequestLobbyListMessage;
import net.fe.unit.UnitFactory;
import net.fe.unit.WeaponFactory;

public class FEServer extends ServerListenerHandler {

	private Server server;
	private IDManager manager = new IDManager();
	private TreeMap<Integer, Lobby> lobbies = new TreeMap<>();
	private LobbyListMessage lobbyListMessage = new LobbyListMessage(new LobbyInfo[0]);
	
	public static void main(String[] args) {
		new FEServer(Server.DEFAULT_PORT).start();
	}
	
	public FEServer(int port) {
		this.server = new Server(port, this, manager);
	}
	
	private void init() {
		WeaponFactory.loadWeapons();
		UnitFactory.loadUnits();
		
		new Thread(server::start).start();
	}
	
	public void start() {
		init();
		new Thread(this::loop).start();
	}
	
	private void loop() {
		while(true) {
			synchronized (messages) {
				try {
					messages.wait(1000l);
				} catch (InterruptedException e) {
					
				}
				for(Message message : messages) {
					try {
						processMessage(message);
					} catch (Throwable e) {
						e.printStackTrace();
						ServerListener listener = getListener(message.origin);
						if(listener != null)
							listener.sendMessage(new KickMessage(0, message.origin, "Server sided error while processing message"));
					}
				}
				messages.clear();
			}
		}
	}
	
	private void processMessage(Message message) {
		if(message instanceof CreateLobby) {
			createLobby(manager.generateLobbyID(), ((CreateLobby) message).session);
		} else if(message instanceof JoinServer) {
			//Congrats I guess. Since we don't care about the name here, it's kinda pointless.
		}
		if(message instanceof RequestLobbyListMessage) {
			getListener(message.origin).sendMessage(lobbyListMessage);
		} else if(message instanceof JoinLobby) {
			int id = ((JoinLobby) message).id;
			synchronized(lobbies) {
				if(lobbies.containsKey(id)) {
					Lobby lobby = lobbies.get(id);
					transferOwnership(this, getListener(message.origin), lobby);
					lobby.addMessage(message);
				} else {
					getListener(message.origin).sendMessage(new KickMessage(0, message.origin, "No such lobby"));
				}
			}
		}
	}

	@Override
	public ArrayList<Message> getBroadcastedMessages() {
		return new ArrayList<>(0);
	}

	@Override
	public void broadcastMessage(Message message) {
		super.broadcastMessage(message);
		for(Lobby lobby : lobbies.values())
			lobby.broadcastMessage(message);
	}

	private void createLobby(int id, Session session) {
		synchronized (lobbies) {
			lobbies.put(id, new Lobby(id, session));
			new Thread(lobbies.get(id)::loop).start();
		}
		updateLobbyList();
	}
	
	public void removeLobby(int id) {
		synchronized(lobbies) {
			lobbies.remove(id);
		}
		updateLobbyList();
	}

	private void updateLobbyList() {
		synchronized(lobbies) {
			LobbyInfo[] info = new LobbyInfo[lobbies.size()];
			Iterator<Lobby> iterator = lobbies.values().iterator();
			for(int i = 0; i < info.length; i++)
				info[i] = iterator.next().getLobbyInfo();
			lobbyListMessage = new LobbyListMessage(info);
		}
	}

}
