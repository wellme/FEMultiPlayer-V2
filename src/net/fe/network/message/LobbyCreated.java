package net.fe.network.message;

import net.fe.network.Message;

public class LobbyCreated extends Message {

	private static final long serialVersionUID = -5123034861861317089L;

	public final int id;
	
	private LobbyCreated(int id) {
		this.id = id;
	}
}
