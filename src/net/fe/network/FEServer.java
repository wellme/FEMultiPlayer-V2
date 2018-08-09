package net.fe.network;

import java.util.ArrayList;
import java.util.TreeMap;

import net.fe.network.message.CreateLobby;
import net.fe.network.message.JoinServer;
import net.fe.network.serverui.FEServerFrame;
import net.fe.unit.UnitFactory;
import net.fe.unit.WeaponFactory;

public class FEServer implements MessageHandler {

	private Server server;
	private TreeMap<Integer, ServerListener> unassociatedClients = new TreeMap<>();
	private TreeMap<Integer, Lobby> lobbies = new TreeMap<>();
	private ArrayList<Message> messages = new ArrayList<Message>();
	
	public static void main(String[] args) {
		new FEServerFrame().setVisible(true);
	}
	
	public FEServer(int port) {
		this.server = new Server(port, this);
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
					processMessage(message);
				}
			}
		}
	}
	
	private void processMessage(Message message) {
		//TODO handle messages (create lobby, etc.)
		if(message instanceof CreateLobby) {
			synchronized (lobbies) {
				//lobbies.put(key, value);
			}
		} else if(message instanceof JoinServer) {
			synchronized (lobbies) {
				//server.sendMessage(message.origin, new LobbyListMessage(lobbies.values().toArray(new Lobby[0])));
			}
		}
	}

	@Override
	public void addMessage(Message message) {
		synchronized (messages) {
			messages.add(message);
			messages.notifyAll();
		}
	}

	@Override
	public ArrayList<Message> getBroadcastedMessages() {
		return null;
	}

	@Override
	public void broadcastMessage(Message message) {
		for(ServerListener listener : unassociatedClients.values())
			listener.sendMessage(message);
		for(Lobby lobby : lobbies.values())
			lobby.broadcastMessage(message);
	}

	@Override
	public void addListener(ServerListener listener) {
		unassociatedClients.put(listener.getId(), listener);
		listener.setDestination(this);
	}

}
