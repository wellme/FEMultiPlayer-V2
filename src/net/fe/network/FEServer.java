package net.fe.network;

import java.util.ArrayList;
import java.util.TreeMap;

import net.fe.network.message.CreateLobby;
import net.fe.network.message.JoinServer;
import net.fe.unit.UnitFactory;
import net.fe.unit.WeaponFactory;

public class FEServer implements MessageDestination {

	private Server server;
	private TreeMap<Integer, Lobby> lobbies = new TreeMap<>();
	private ArrayList<Message> messages = new ArrayList<Message>();
	
	
	public FEServer(Server server) {
		this.server = server;
		init();
		new Thread(this::loop).start();
	}
	
	private void init() {
		WeaponFactory.loadWeapons();
		UnitFactory.loadUnits();
		
		new Thread(server::start).start();
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

}
