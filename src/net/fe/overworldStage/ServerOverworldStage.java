package net.fe.overworldStage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.ResourceLoader;

import net.fe.FEMultiplayer;
import net.fe.Party;
import net.fe.Player;
import net.fe.Session;
import net.fe.editor.Level;
import net.fe.editor.SpawnPoint;
import net.fe.fightStage.AttackRecord;
import net.fe.modifier.Modifier;
import net.fe.network.Lobby;
import net.fe.network.Message;
import net.fe.network.message.CommandMessage;
import net.fe.network.message.EndGame;
import net.fe.network.stage.ServerStage;
import net.fe.unit.Unit;

public class ServerOverworldStage implements OverworldStage, ServerStage {

	private Session session;
	private Lobby lobby;
	private Grid grid;
	private ArrayList<Player> turnOrder;
	private int currentPlayer;
	private int turnCount;
	
	public ServerOverworldStage(Lobby lobby, Session session) {
		this.lobby = lobby;
		this.session = session;
		turnOrder = new ArrayList<Player>();
		for(Player p : getSession().getNonSpectators())
			turnOrder.add(p);
		Collections.sort(turnOrder, (a, b) -> a.getID() - b.getID());
		currentPlayer = 0;
		turnCount = 1;
		loadLevel(getSession().getMap());
		for(Modifier m : getSession().getModifiers())
			m.initOverworldUnits(getAllUnits());
		//getAllUnits().forEach(unit -> unit.setStage(this));
	}

	@Override
	public Lobby getLobby() {
		return lobby;
	}
	
	@Override
	public void checkEndGame() {
		// Objective evaluation
		int winner = getSession().getObjective().evaluate(this);
		if(getSession().numPlayers()==1){//players have left
			winner = getSession().getPlayers()[0].getID();//whoever's left wins
		}else if (getSession().numPlayers()<1){
			FEMultiplayer.disconnectGame("All players have disconnected");
		}
		if(winner > 0 && Lobby.getServer() != null) {
			Lobby.getServer().broadcastMessage(new EndGame(0, winner));
			Lobby.resetToLobby();
		}
	}

	@Override
	public void onStep() {
		
	}

	@Override
	public void endStep() {
		
	}

	@Override
	public void processCommands(CommandMessage message) {
		//TODO: command validation
		// After validation, update the unit position
		// Move it instantly since this is the server stage
		final Unit unit = (message.unit == null ? null : getUnit(message.unit));
		
		for(int i=0; i<message.commands.length; i++) {
			try {
				ArrayList<AttackRecord> record = message.commands[i].applyServer(this, unit);
				if (record != null) {
					if (message.attackRecords != null) {
						throw new IllegalStateException("Two attacks in the same move");
					} else {
						message.attackRecords = record;
					}
				}
			} catch (IllegalStateException e) {
				
				throw e;
			}
		}
		if(unit != null) {
			unit.setMoved(true);
		}
		lobby.getServer().broadcastMessage(message);
		checkEndGame();
	}

	@Override
	public void beforeTriggerHook(TerrainTrigger t, int x, int y) {
		
	}

	@Override
	public void beginStep(List<Message> messages) {
		for(Message message : messages)
			this.executeMessage(message);
	}

	@Override
	public void loadLevel(String levelName) {
		try {
			InputStream in = ResourceLoader.getResourceAsStream("levels/"+levelName+".lvl");
			ObjectInputStream ois = new ObjectInputStream(in);
			Level level = (Level) ois.readObject();
			Set<SpawnPoint> spawns = new HashSet<>(level.spawns);
			grid = new Grid(level.width, level.height, Terrain.NONE);
			for(int i=0; i<level.tiles.length; i++) {
				for(int j=0; j<level.tiles[0].length; j++) {
					getGrid().setTerrain(j, i, Tile.getTerrainFromID(level.tiles[i][j]));
					if(Tile.getTerrainFromID(level.tiles[i][j]) == Terrain.THRONE) {
						int blue = 0;
						int red = 0;
						for(SpawnPoint sp : spawns) {
							if(sp.team.equals(Party.TEAM_BLUE)) {
								blue += Math.abs(sp.x-j) + Math.abs(sp.y-i);
							} else {
								red += Math.abs(sp.x-j) + Math.abs(sp.y-i);
							}
						}
						if(blue < red) {
							System.out.println(blue + " "+ red);
							getGrid().setThronePos(Party.TEAM_BLUE, j, i);
						} else {
							System.out.println(blue + " "+ red);
							getGrid().setThronePos(Party.TEAM_RED, j, i);
						}
					}
				}
			}
			
			// Add units
			for(Player p : getSession().getPlayers()) {
				Color team = p.getParty().getColor();
				for(int i=0; i<p.getParty().size(); i++) {
					SpawnPoint remove = null;
					for(SpawnPoint sp : spawns) {
						if(sp.team.equals(team)) {
							Unit u = p.getParty().getUnit(i);
							addUnit(u, sp.x, sp.y);
							remove = sp;
							break;
						}
					}
					spawns.remove(remove);
				}
			}
			ois.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public Grid getGrid() {
		return grid;
	}

	@Override
	public int getTurnCount() {
		return turnCount;
	}

	@Override
	public void setTurnCount(int count) {
		turnCount = count;
	}

	@Override
	public Player[] getTurnOrder() {
		Player[] t = new Player[turnOrder.size()];
		for(int i=0; i<t.length; i++) {
			t[i] = turnOrder.get((currentPlayer+ i) % t.length);
		}
		return t;
	}

	@Override
	public int getCurrentPlayerIndex() {
		return currentPlayer;
	}

	@Override
	public void setCurrentPlayerIndex(int index) {
		currentPlayer = index;
	}
}
