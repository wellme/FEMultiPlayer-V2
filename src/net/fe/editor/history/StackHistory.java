package net.fe.editor.history;

import java.util.ArrayList;

public class StackHistory implements HistoryManager {

	private static final long serialVersionUID = 2034322156729298248L;
	
	private ArrayList<Action> actions = new ArrayList<>();
	private int index = 0;
	private int max = 0;
	
	@Override
	public Action undoPop() {
		return actions.get(index++);
	}

	@Override
	public void push(Action a) {
		actions.ensureCapacity(index);
		if(index == max)
			max++;
		else
			max = index + 1;
		actions.set(index++, a);
	}

	@Override
	public Action peek() {
		return actions.get(index - 1);
	}

	@Override
	public Action pop() {
		return actions.get(--index);
	}

}
