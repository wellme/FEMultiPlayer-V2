package net.fe.lobbystage;

import java.util.List;

import chu.engine.ClientStage;
import net.fe.Player;
import net.fe.Session;
import net.fe.network.Lobby;
import net.fe.network.Message;
import net.fe.network.ServerStage;
import net.fe.network.message.ClientInit;
import net.fe.network.message.ReadyMessage;
import net.fe.network.message.StartPicking;

// TODO: Auto-generated Javadoc
/**
 * Version of LobbyStage used by Server without extraneous entities.
 *
 * @author Shawn
 */
public class LobbyStage extends ClientStage implements ServerStage {
	
	/** The session. */
	protected Session session;
	
	/**
	 * Instantiates a new lobby stage.
	 *
	 * @param s the s
	 */
	public LobbyStage(Session s) {
		super("main");
		session = s;
	}
	
	/* (non-Javadoc)
	 * @see chu.engine.Stage#beginStep()
	 */
	@Override
	public void beginStep(List<Message> messages) {
		for(Message message : messages) {
			if(message instanceof ClientInit) {		// Only clients will get this
				ClientInit init = (ClientInit)message;
				session = init.session;
			}
			else if(message instanceof ReadyMessage) {
				boolean ready = !session.getPlayer(message.origin).ready;
				session.getPlayer(message.origin).ready = ready;
				if(ready)
					session.getChatlog().add(session.getPlayer(message.origin), "Ready!");
				else
					session.getChatlog().add(session.getPlayer(message.origin), "Not ready!");
			}
		}
	}

	/* (non-Javadoc)
	 * @see chu.engine.Stage#onStep()
	 */
	@Override
	public void onStep() {
		
	}

	/* (non-Javadoc)
	 * @see chu.engine.Stage#endStep()
	 */
	@Override
	public void endStep() {
		if(session.numPlayers() <=1 ) return;
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
			Lobby.getServer().broadcastMessage(new StartPicking(0));
			session.getPickMode().setUpServer(getLobby(), session);
		}
		
	}

	@Override
	public Lobby getLobby() {
		return Lobby.getLobby();
	}

}
