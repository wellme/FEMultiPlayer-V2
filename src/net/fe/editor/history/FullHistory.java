package net.fe.editor.history;

import java.util.ArrayList;

public class FullHistory implements HistoryManager {
	
	//TODO all of this
	
	private static final long serialVersionUID = -487888197764347683L;
	
	private Branch master;
	private Branch head;

	@Override
	public void push(Action a) {
		if(head.branches()) {
			
		}
	}

	@Override
	public Action peek() {
		return null;
	}

	@Override
	public Action pop() {
		return null;
	}

	private static class Branch {
		private ArrayList<Action> content = new ArrayList<>();
		private ArrayList<Branch> branches = new ArrayList<>(0);
		
		
		public boolean branches() {
			return branches.size() != 0;
		}
	}

	@Override
	public Action undoPop() {
		return null;
	}
}
