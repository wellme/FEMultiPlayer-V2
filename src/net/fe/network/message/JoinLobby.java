package net.fe.network.message;

import net.fe.network.Message;

public class JoinLobby extends Message {

	private static final long serialVersionUID = -7242561190752585559L;
	
	public final int id;
	
	public JoinLobby(int id) {
		this.id = id;
	}
}
