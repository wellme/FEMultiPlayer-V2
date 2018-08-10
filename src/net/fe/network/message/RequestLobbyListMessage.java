package net.fe.network.message;

import net.fe.network.Message;

public class RequestLobbyListMessage extends Message {

	private static final long serialVersionUID = 601681370217766711L;
	
	public RequestLobbyListMessage(int origin) {
		super(origin);
	}
}