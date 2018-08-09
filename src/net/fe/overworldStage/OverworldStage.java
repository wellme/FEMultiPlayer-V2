package net.fe.overworldStage;

import java.util.ArrayList;
import java.util.List;

import chu.engine.Stage;
import net.fe.Player;
import net.fe.Session;
import net.fe.network.Message;
import net.fe.network.message.CommandMessage;
import net.fe.network.message.EndGame;
import net.fe.network.message.EndTurn;
import net.fe.network.message.KickMessage;
import net.fe.network.message.QuitMessage;
import net.fe.overworldStage.objective.Objective;
import net.fe.rng.RNG;
import net.fe.unit.Unit;
import net.fe.unit.UnitIdentifier;

public interface OverworldStage extends Stage {

	// Blame Java for not allowing multiple inheritance.
	public int getCurrentPlayerIndex();
	public void setCurrentPlayerIndex(int index);
	public Player[] getTurnOrder();
	public int getTurnCount();
	public void setTurnCount(int count);
	public Grid getGrid();
	public Session getSession();
	public void beforeTriggerHook(TerrainTrigger t, int x, int y);
	public void processCommands(CommandMessage message);
	public void checkEndGame();
	
	public default Player getCurrentPlayer() {
		return getTurnOrder()[0];
	}
	
	public default Terrain getTerrain(int x, int y) {
		return getGrid().getTerrain(x, y);
	}

	public default Unit getUnit(int x, int y) {
		return getGrid().getUnit(x, y);
	}
	

	public default boolean addUnit(Unit u, int x, int y) {
		return getGrid().addUnit(u, x, y);
	}

	public default Unit removeUnit(int x, int y) {
		Unit u = getGrid().removeUnit(x, y);
		return u;
	}
	
	public default void removeUnit(Unit u) {
		getGrid().removeUnit(u.getXCoord(), u.getYCoord());
	}
	
	public abstract void loadLevel(String levelName);

	/** Perform an action in response to receiving the message */
	public default void executeMessage(Message message) {
		if(message instanceof CommandMessage) {
			processCommands((CommandMessage)message);
		}
		else if(message instanceof EndTurn) {
			//Only end the turn if it is this player's turn to end. (Or, if for some reason we want to let
			//the server end turns in the future.
			System.out.println("" + message.origin + " " + getCurrentPlayerIndex());
			if(message.origin == getCurrentPlayer().getID() || message.origin == 0){
				((EndTurn) message).checkHp((ui) -> this.getUnit(ui));
				doEndTurn();
				setCurrentPlayerIndex(getCurrentPlayerIndex() + 1);
				if(getCurrentPlayerIndex() >= getTurnOrder().length) {
					setCurrentPlayerIndex(0);
				}
				doStartTurn();
			}
		}
		else if(message instanceof QuitMessage || message instanceof KickMessage) {
			this.checkEndGame();
		}
		else if(message instanceof EndGame) {
			this.checkEndGame();
		}
	}
	
	/**
	 * Perform actions that happen at the end of a phase
	 * <p>
	 * The phase to end is determined by the class's `getCurrentPlayer()`
	 */
	public default void doEndTurn() {
		// perform terrain effects
		for(int x = 0; x < getGrid().width; x++){
			for(int y = 0; y < getGrid().height; y++){
				for(TerrainTrigger t: getGrid().getTerrain(x, y).getTriggers()){
					if(t.attempt(this, x, y, getCurrentPlayer()) && !t.start){
						beforeTriggerHook(t, x, y);
						t.endOfTurn(this, x, y);
					}
				}
			}
		}
		
		// Refresh unit's `moved` status
		for(Player p : getSession().getPlayers()) {
			for(Unit u : p.getParty()) {
				u.setMoved(false);
			}
		}
		
		checkEndGame();
	}
	
	/**
	 * Perform actions that happen at the start of a phase.
	 * <p>
	 * The phase to start is determined by the class's `getCurrentPlayer()`
	 */
	public default void doStartTurn() {
		// increment turn count if the starting phase is the first phase of a turn
		if (getCurrentPlayerIndex() == 0) {
			setTurnCount(getTurnCount() + 1);
		}
		
		// perform terrain effects
		for(int x = 0; x < getGrid().width; x++){
			for(int y = 0; y < getGrid().height; y++){
				for(TerrainTrigger t: getGrid().getTerrain(x, y).getTriggers()){
					if(t.attempt(this, x, y, getCurrentPlayer()) && t.start){
						beforeTriggerHook(t, x, y);
						t.startOfTurn(this, x, y);
					}
				}
			}
		}
	}
	
	public default Unit getUnit(UnitIdentifier id) {
		for(Player p: getSession().getPlayers()){
			if(!p.isSpectator() && p.getParty().getColor().equals(id.partyColor)){
				return p.getParty().search(id.name);
			}
		}
		return null;
	}

	/**
	 * Returns a list of players, filtered to not include players that are spectators
	 */
	public default Player[] getNonSpectators() {
		return getSession().getNonSpectators();
	}
	
	public default Objective getObjective(){
		return getSession().getObjective();
	}
	
	public default List<Unit> getAllUnits() {
		List<Unit> units = new ArrayList<Unit>();
		for(Player p : getSession().getPlayers()) {
			for(int i=0; i<p.getParty().size(); i++) {
				units.add(p.getParty().getUnit(i));
			}
		}
		return units;
	}

	public default RNG getHitRNG() {
		return getSession().getHitRNG();
	}

	public default RNG getCritRNG() {
		return getSession().getCritRNG();
	}

	public default RNG getSkillRNG() {
		return getSession().getSkillRNG();
	}

}
