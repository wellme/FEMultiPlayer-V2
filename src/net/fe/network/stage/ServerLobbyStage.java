package net.fe.network.stage;

import java.util.List;

import net.fe.Player;
import net.fe.Session;
import net.fe.lobbystage.LobbyStage;
import net.fe.network.Lobby;
import net.fe.network.Message;
import net.fe.network.message.ReadyMessage;
import net.fe.network.message.StartPicking;

public class ServerLobbyStage implements LobbyStage, ServerStage {
	
	private Lobby lobby;
	private Session session;
	
	public ServerLobbyStage(Lobby lobby, Session session) {
		this.lobby = lobby;
		this.session = session;
	}

	@Override
	public void endStep() {
		if(session.numPlayers() <=1)
			return;
		int activeBlue = 0;
		int activeRed = 0;
		boolean allPlayersReady = true;
		for(Player p : session.getPlayers()) {
			if(!p.ready && !p.isSpectator())
				allPlayersReady = false;
		
			int team = p.getTeam();
			if (team == Player.TEAM_BLUE)
				activeBlue++;
			else if (team == Player.TEAM_RED)
				activeRed++;
		}
		
		// Teams are valid
		if (activeBlue == 1 && activeRed == 1 && allPlayersReady) {
			lobby.broadcastMessage(new StartPicking(0));
			session.getPickMode().setUpServer(lobby, session);
		}
	}

	@Override
	public void onStep() {
		
	}

	@Override
	public void beginStep(List<Message> messages) {
		for(Message message : messages) {
			if(message instanceof ReadyMessage) {
				boolean ready = !session.getPlayer(message.origin).ready;
				session.getPlayer(message.origin).ready = ready;
				if(ready)
					session.getChatlog().add(session.getPlayer(message.origin), "Ready!");
				else
					session.getChatlog().add(session.getPlayer(message.origin), "Not ready!");
			}
		}
	}

	@Override
	public Lobby getLobby() {
		return lobby;
	}

	@Override
	public Session getSession() {
		return session;
	}

}
