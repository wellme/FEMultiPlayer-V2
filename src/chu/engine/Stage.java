package chu.engine;

import java.util.List;

import net.fe.network.Message;

public interface Stage {

	public abstract void beginStep(List<Message> messages);
	public abstract void onStep();
	public abstract void endStep();
}
