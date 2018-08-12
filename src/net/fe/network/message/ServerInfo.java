package net.fe.network.message;

import net.fe.network.Lobby.LobbyInfo;
import net.fe.network.Message;

public class ServerInfo extends Message {
	
	private static final long serialVersionUID = -7744711270239125901L;
	
	public final LobbyInfo[] lobbies;
	
	public ServerInfo(LobbyInfo[] lobbies) {
		this.lobbies = lobbies;
	}
}
