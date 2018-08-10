package net.fe.network.message;

public final class JoinServer extends RequestLobbyListMessage {
	
	private static final long serialVersionUID = 4749147769637678401L;
	
	public JoinServer(int origin) {
		super(origin);
	}
	
}
