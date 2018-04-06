package net.fe.editor.history;

/**
 * Abstract class representing an action performed on the map
 * (changing a tile, the title, spawns, etc.)
 * Used to undo/redo previous actions.
 * @author wellme
 *
 */
public abstract class Action {
	/**
	 * Performs the action. Even though the method is named "redo", it
	 * may be called even if the action was not undone.
	 */
	public abstract void redo();
	/**
	 * Do the opposite of the action.
	 */
	public abstract void undo();
}