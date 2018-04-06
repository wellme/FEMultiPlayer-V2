package net.fe.editor.history;

import java.io.Serializable;

public interface HistoryManager extends Serializable {

	public void push(Action a);
	public Action peek();
	public Action pop();
	
	public Action undoPop();
}
