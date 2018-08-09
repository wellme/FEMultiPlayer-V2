package net.fe.network;

import java.util.ArrayList;

/**
 * An object capable of handling clients and the message sent by them.
 * @author wellme
 */
public interface MessageHandler {

	/**
	 * Add a message to be processed by the handler.
	 * @param message The message to process.
	 */
	public void addMessage(Message message);
	
	/**
	 * Returns the list of messages broadcasted by this handler.
	 * @return The list of messages.
	 */
	public ArrayList<Message> getBroadcastedMessages();
	
	/**
	 * Sends a message to every listener handled by this object.
	 * @param message The message to send.
	 */
	public void broadcastMessage(Message message);
	
	/**
	 * Adds a listener to this handler. Note: a listener
	 * should only be a part of one handler at a time.
	 */
	public void addListener(ServerListener listener);
}
