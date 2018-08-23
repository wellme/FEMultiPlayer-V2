package net.fe.network.message;

import net.fe.network.Message;

public class JoinLobby extends Message {

	private static final long serialVersionUID = -7242561190752585559L;
	
	public final int id;
	public final String name;
	public final String password;
	
	public JoinLobby(int id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}
}
