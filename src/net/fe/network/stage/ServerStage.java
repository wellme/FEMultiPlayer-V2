package net.fe.network.stage;

import chu.engine.Stage;
import net.fe.network.Lobby;

public interface ServerStage extends Stage {
	
	public Lobby getLobby();
}
