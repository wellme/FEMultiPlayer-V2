package net.fe;

import chu.engine.Entity;
import chu.engine.ClientStage;

// TODO: Auto-generated Javadoc
/**
 * Manages a delayed transition between two stages.
 * @author Shawn
 *
 */
public abstract class Transition extends Entity {
	
	/** The to. */
	protected ClientStage to;
	
	/**
	 * Instantiates a new transition.
	 *
	 * @param to the to
	 */
	public Transition(ClientStage to) {
		super(0,0);
		this.to = to;
	}
	
	/**
	 * Done.
	 */
	public void done() {
		destroy();
		stage.processRemoveStack();
		FEMultiplayer.setCurrentStage(to);
	}
}
