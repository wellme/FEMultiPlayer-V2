package net.fe.network.message;

import net.fe.Session;
import net.fe.network.Message;

public class CreateLobby extends Message {
	
	private static final long serialVersionUID = 6720114775271670707L;
	
	public final Session session;

	public CreateLobby(Session session) {
		this.session = session;
	}

}
