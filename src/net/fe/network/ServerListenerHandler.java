package net.fe.network;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.fe.network.message.KickMessage;
import net.fe.network.message.RejoinMessage;

/**
 * An object capable of handling clients and the message sent by them.
 * @author wellme
 */
public abstract class ServerListenerHandler {
	
	private final ArrayList<Message> broadcastedMessages = new ArrayList<>();
	private final TreeMap<Long, ServerListener> disconnectedListeners = new TreeMap<>();
	private final TreeMap<Integer, ServerListener> listeners = new TreeMap<>();
	protected final ArrayList<Message> messages = new ArrayList<>();

	/**
	 * Add a message to be processed by the handler.
	 * @param message The message to process.
	 */
	public final void addMessage(Message message) {
		synchronized(messages) {
			messages.add(message);
			messages.notifyAll();
		}
	}
	
	/**
	 * Returns the list of messages broadcasted by this handler.
	 * @return The list of messages.
	 */
	public ArrayList<Message> getBroadcastedMessages() {
		return broadcastedMessages;
	}
	
	/**
	 * Sends a message to every listener handled by this object.
	 * @param message The message to send.
	 */
	public void broadcastMessage(Message message) {
		broadcastedMessages.add(message);
		for(ServerListener listener : listeners.values())
			listener.sendMessage(message);
	}
	
	/**
	 * Adds a listener to this handler. Note: a listener
	 * should only be a part of one handler at a time.
	 */
	public final void addListener(ServerListener listener) {
		synchronized(listeners) {
			listeners.put(listener.getId(), listener);
			listener.setDestination(this);
		}
	}
	
	public ServerListener getListener(int id) {
		return listeners.get(id);
	}
	
	public int getListenerCount() {
		return listeners.size();
	}
	
	public static void transferOwnership(ServerListenerHandler source, ServerListener listener, ServerListenerHandler destination) {
		destination.addListener(listener);
		source.listeners.remove(listener.getId());
	}
	
	public void removeListener(boolean allowReconnection, ServerListener listener) {
		synchronized (listeners) {
			listeners.remove(listener.getId());
		}
		if(allowReconnection) {
			synchronized (disconnectedListeners) {
				disconnectedListeners.put(System.currentTimeMillis(), listener);
			}
		}
	}

	public void timeoutClients() {
		long minTimestamp = System.currentTimeMillis() - Server.TIMEOUT;
		synchronized(disconnectedListeners) {
			while(!disconnectedListeners.isEmpty() && disconnectedListeners.firstKey() <= minTimestamp) {
				ServerListener listener = disconnectedListeners.pollFirstEntry().getValue();
				KickMessage kick = new KickMessage(0, listener.getId(), "Timed out");
				broadcastMessage(kick);
				addMessage(kick);
			}
		}
	}

	public boolean validateRejoinRequest(RejoinMessage message) {
		timeoutClients();
		synchronized(disconnectedListeners) {
			Iterator<Entry<Long, ServerListener>> iterator = disconnectedListeners.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Long, ServerListener> entry = iterator.next();
				if(entry.getValue().getId() == message.origin && entry.getValue().getToken() == message.getToken()) {
					disconnectedListeners.remove(entry.getKey());
					return true;
				}
			}
		}
		return false;
	}
}
