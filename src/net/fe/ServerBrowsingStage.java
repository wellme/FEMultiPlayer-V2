package net.fe;

import java.util.List;

import org.newdawn.slick.Color;

import chu.engine.ClientStage;
import chu.engine.Entity;
import net.fe.lobbystage.ClientLobbyStage;
import net.fe.network.Lobby.LobbyInfo;
import net.fe.network.Message;
import net.fe.network.message.JoinLobby;
import net.fe.network.message.LobbyListMessage;
import net.fe.network.message.ReadyMessage;

public class ServerBrowsingStage extends ClientStage {

	public ServerBrowsingStage() {
		super(null);
		addEntity(new RunesBg(Color.blue));
	}

	@Override
	public void beginStep(List<Message> messages) {
		for(Message message : messages)
			executeMessage(message);
		for(Entity e : entities)
			e.beginStep();
	}

	@Override
	public void onStep() {
		for(Entity e : entities)
			e.onStep();
	}

	@Override
	public void endStep() {
		for(Entity e : entities)
			e.endStep();
	}
	
	private void executeMessage(Message message) {
		if(message instanceof LobbyListMessage) {
			LobbyListMessage list = (LobbyListMessage) message;
			joinLobby(list.lobbies[0]);
		}
	}
	
	private void joinLobby(LobbyInfo info) {
		FEMultiplayer.lobby = new ClientLobbyStage(info.session);
		FEMultiplayer.getClient().setSession(info.session);
		FEMultiplayer.setCurrentStage(FEMultiplayer.lobby);
		FEMultiplayer.getClient().sendMessage(new JoinLobby(info.id, FEMultiplayer.getLocalPlayer().getName()));
	}
}
