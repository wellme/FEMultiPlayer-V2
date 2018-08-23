package net.fe.browser;

import java.util.List;

import org.newdawn.slick.Color;

import chu.engine.ClientStage;
import chu.engine.Entity;
import chu.engine.Game;
import chu.engine.menu.Notification;
import net.fe.FEMultiplayer;
import net.fe.FEResources;
import net.fe.RunesBg;
import net.fe.Session;
import net.fe.lobbystage.ClientLobbyStage;
import net.fe.network.Lobby.LobbyInfo;
import net.fe.network.Message;
import net.fe.network.message.CreateLobby;
import net.fe.network.message.JoinLobby;
import net.fe.network.message.LobbyListMessage;
import net.fe.network.message.RequestLobbyListMessage;

public class ServerBrowsingStage extends ClientStage {
	
	private ServerBrowsingFrame frame;
	private Runnable action;
	
	private static final String TEXT = "Another window containing a lobby browser was opened";
	private static final String FONT = "default_med";
	private static final float TEXT_X = Game.getWindowWidth() / 2 - FEResources.getBitmapFont(FONT).getStringWidth(TEXT) / 2;
	private static final float TEXT_Y = Game.getWindowHeight() / 2;
	

	public ServerBrowsingStage() {
		super(null);
		addEntity(new RunesBg(Color.gray));
		addEntity(new Notification(TEXT_X, TEXT_Y, FONT, TEXT, Float.MAX_VALUE, 1));
		frame = new ServerBrowsingFrame(this);
		frame.setVisible(true);
		FEMultiplayer.getClient().sendMessage(new RequestLobbyListMessage());
	}

	@Override
	public void beginStep(List<Message> messages) {
		for(Message message : messages)
			executeMessage(message);
		if(getAction() != null) {
			getAction().run();
			setAction(null);
		}
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
			frame.setLobbies(list.lobbies);
		}
	}
	
	public void joinLobby(LobbyInfo info, String password) {
		FEMultiplayer.lobby = new ClientLobbyStage(info.id, info.session);
		FEMultiplayer.getClient().setSession(info.session);
		FEMultiplayer.setCurrentStage(FEMultiplayer.lobby);
		FEMultiplayer.getClient().sendMessage(new JoinLobby(info.id, FEMultiplayer.getLocalPlayer().getName(), password));
		frame.dispose();
	}
	
	public void createLobby(Session session, String name, String password) {
		FEMultiplayer.lobby = new ClientLobbyStage(session);
		FEMultiplayer.getClient().setSession(session);
		FEMultiplayer.setCurrentStage(FEMultiplayer.lobby);
		FEMultiplayer.getClient().sendMessage(new CreateLobby(session, name, password));
		frame.dispose();
	}

	public Runnable getAction() {
		return action;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}
	
}
