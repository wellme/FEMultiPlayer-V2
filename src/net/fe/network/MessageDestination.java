package net.fe.network;

/**
 * An object capable of holding messages sent by the client.
 * @author wellme
 */
public interface MessageDestination {

	public void addMessage(Message message);
}
