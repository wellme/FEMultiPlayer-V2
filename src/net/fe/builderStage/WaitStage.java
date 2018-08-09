package net.fe.builderStage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import net.fe.Player;
import net.fe.Session;
import net.fe.network.Lobby;
import net.fe.network.Message;
import net.fe.network.message.KickMessage;
import net.fe.network.message.PartyMessage;
import net.fe.network.message.QuitMessage;
import net.fe.network.message.StartGame;
import net.fe.network.stage.ServerStage;
import net.fe.overworldStage.ServerOverworldStage;
import net.fe.unit.Unit;

// TODO: Auto-generated Javadoc
/**
 * Wait for all players to select.
 *
 * @author Shawn
 */
public final class WaitStage implements ServerStage {
	
	/** The ready status. */
	private final HashMap<Integer, Boolean> readyStatus;
	
	/** The messages. */
	private final ArrayList<PartyMessage> messages;
	
	/** The sent start message. */
	private boolean sentStartMessage;
	
	/** The session. */
	private final Session session;
	
	private final Lobby lobby;
	
	/**
	 * Instantiates a new wait stage.
	 *
	 * @param s the s
	 */
	public WaitStage(Lobby lobby, Session session) {
		this.session = session;
		this.lobby = lobby;
		sentStartMessage = false;
		readyStatus = new HashMap<>();
		for(Player p : session.getNonSpectators()) {
			readyStatus.put(p.getID(), false);
		}
		messages = new ArrayList<PartyMessage>();
	}
	
	/* (non-Javadoc)
	 * @see chu.engine.Stage#beginStep()
	 */
	@Override
	public void beginStep(List<Message> messages) {
		for(Message message : messages) {
			if(message instanceof PartyMessage) {
				PartyMessage pm = (PartyMessage)message;
				java.util.Optional<String> validationResult = pm.validateTeam(
					net.fe.unit.UnitFactory::getUnit,
					net.fe.unit.Item.getAllItems(),
					session.getModifiers()
				);
				validationResult.ifPresent(new Consumer<String>() {
					@Override public void accept(String validationError) {
						synchronized(lobby.getServer().messagesLock) {
							final KickMessage kick = new KickMessage(0, pm.origin, validationError);
							lobby.getServer().broadcastMessage(kick);
							lobby.getServer().messages.add(kick);
						}
					}
				});
				for(Player p : session.getPlayers()){ 
					if(p.getID() == message.origin) {
						p.getParty().clear();
						for(Unit u : pm.teamData)
							p.getParty().addUnit(u);
						readyStatus.put(p.getID(), true);
					}
				}
				this.messages.add(pm);
			}
			else if(message instanceof QuitMessage || message instanceof KickMessage) {
				if (this.session.getNonSpectators().length < 2) {
					// player has left
					lobby.resetToLobby();
				}
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
		if(!sentStartMessage) {
			for(boolean b : readyStatus.values()) {
				if(!b) return;
			}
			for(PartyMessage pm : messages) {
				lobby.getServer().broadcastMessage(pm);
			}
			lobby.getServer().broadcastMessage(new StartGame(0));
			for(Player p : session.getPlayers()) {
				for(Unit u : p.getParty()) {
					u.initializeEquipment();
				}
			}
			lobby.setCurrentStage(new ServerOverworldStage(lobby, session));
			sentStartMessage = true;
		}
	}

	@Override
	public Lobby getLobby() {
		return lobby;
	}
	
}
