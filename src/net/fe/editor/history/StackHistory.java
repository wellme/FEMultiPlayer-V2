package net.fe.editor.history;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class StackHistory implements HistoryManager {

	private static final long serialVersionUID = 2034322156729298248L;
	
	private ArrayList<Action> actions = new ArrayList<>();
	private int index = 0;
	private int max = 0;
	
	@Override
	public Action undoPop() {
		if(!hasNext())
			throw new ArrayIndexOutOfBoundsException("No next element");
		return actions.get(index++);
	}

	@Override
	public void push(Action a) {
		if(index == max) {
			max++;
			actions.add(a);
		} else {
			max = index + 1;
			actions.set(index, a);
		}
		index++;
	}

	@Override
	public Action peek() {
		return actions.get(index - 1);
	}

	@Override
	public Action pop() {
		if(!hasPrevious())
			throw new ArrayIndexOutOfBoundsException("No previous element");
		return actions.get(--index);
	}
	
	public boolean hasPrevious() {
		return index != 0;
	}
	
	public boolean hasNext() {
		return index != max;
	}

}
