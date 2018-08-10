package net.fe.network.message;

import net.fe.network.Lobby.LobbyInfo;
import net.fe.network.Message;

public class LobbyListMessage extends Message {

	private static final long serialVersionUID = 5773117997419682090L;

	public final LobbyInfo[] lobbies;
	
	public LobbyListMessage(LobbyInfo[] lobbies) {
		this.lobbies = lobbies;
	}
}
