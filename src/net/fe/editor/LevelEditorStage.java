package net.fe.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import chu.engine.Game;
import chu.engine.KeyboardEvent;
import chu.engine.Stage;
import chu.engine.anim.Renderer;
import chu.engine.anim.Tileset;
import net.fe.Party;
import net.fe.editor.history.Action;
import net.fe.network.Message;

public class LevelEditorStage extends Stage {

	private static Texture palette;
	private static Tileset tileset;
	private int selectedID;
	private int[][] tiles;
	private String levelName;
	private HashSet<SpawnPoint> spawns;
	private Stack<Action> history = new Stack<>();

	static {
		try {
			palette = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/terrain_tiles.png"));
			tileset = new Tileset(palette, 16, 16);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LevelEditorStage(int x, int y, String levelName) {
		super(null);
		selectedID = 0;
		this.levelName = levelName;
		tiles = new int[y][x];
		spawns = new HashSet<SpawnPoint>();
		try {
			FileInputStream in = new FileInputStream(new File("levels/" + levelName + ".lvl"));
			ObjectInputStream ois = new ObjectInputStream(in);
			Level level = (Level) ois.readObject();
			tiles = level.tiles;
			spawns = new HashSet<>(level.spawns);
			if(spawns == null) spawns = new HashSet<SpawnPoint>();
			ois.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void beginStep(List<Message> messages) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			if (Mouse.isButtonDown(0))
				selectedID = Math.min((Game.getWindowHeight() - Mouse.getY()) / 16 * 25 + Mouse.getX() / 16, 25 * 40 - 1);
		} else {
			
			int tile = Mouse.isButtonDown(0) ? selectedID : Mouse.isButtonDown(1) ? 0 : -1;
			if(tile != -1)
				if(!trySet(getMousePosition(), tile))
					System.err.println("Tried to place tile out of bounds");
			
			List<KeyboardEvent> keys = Game.getKeys();
			for (KeyboardEvent ke : keys) {
				if (ke.state) {
					switch(ke.key) {
						case Keyboard.KEY_S: modifySize(0, 1); break;
						case Keyboard.KEY_W: modifySize(0, -1); break;
						case Keyboard.KEY_A: modifySize(-1, 0); break;
						case Keyboard.KEY_D: modifySize(1, 0); break;
						
						case Keyboard.KEY_Z: addSpawn(Party.TEAM_BLUE); break;
						case Keyboard.KEY_X: addSpawn(Party.TEAM_RED); break;
						case Keyboard.KEY_C: addSpawn(Party.TEAM_GREEN); break;
						
						case Keyboard.KEY_V: {
							Point pos = getMousePosition();
							spawns.remove(new SpawnPoint(pos.x, pos.y, null));
							break;
						}
						
						case Keyboard.KEY_F1: save(new File(levelName)); break;
					}
				}
			}
		}
	}

	@Override
	public void render() {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			
			Renderer.render(palette, 0, 0, 1, 1, 0, 0, 400, 640, 0.1f);
			
			int x = selectedID % 25 * 16;
			int y = selectedID / 25 * 16;
			
			Renderer.drawLine(x - 1, y - 1, x + 17, y - 1, 1, 0, Color.red,	Color.red);
			Renderer.drawLine(x + 17, y - 1, x + 17, y + 17, 1, 0, Color.red, Color.red);
			Renderer.drawLine(x - 1, y + 17, x + 17, y + 17, 1, 0, Color.red, Color.red);
			Renderer.drawLine(x - 1, y - 1, x - 1, y + 17, 1, 0, Color.red, Color.red);
			
		} else {
			
			for (int i = 0; i < tiles.length; i++)
				for (int j = 0; j < tiles[0].length; j++)
					tileset.render(j * 16, i * 16, tiles[i][j] % 25, tiles[i][j] / 25, 0.5f);
			
			for(SpawnPoint sp : spawns) {
				Color c = new Color(sp.team);
				c.a = 0.3f;
				Renderer.drawSquare(sp.x*16, sp.y*16, 16, 0.4f, c);
			}

			int x = tiles[0].length * 16;
			int y = tiles.length * 16;
			
			Renderer.drawLine(0, 0, x, 0, 1, 0, Color.red, Color.red);
			Renderer.drawLine(x, 0, x, y, 1, 0, Color.red, Color.red);
			Renderer.drawLine(0, y, x, y, 1, 0, Color.red, Color.red);
			Renderer.drawLine(0, 0, 0, y, 1, 0, Color.red, Color.red);
		}
	}
	
	private boolean isValid(Point p) {
		return p.x >= 0 && p.y >= 0 && p.x < tiles[0].length && p.y < tiles.length;
	}
	
	private Point getMousePosition() {
		return new Point(Mouse.getX() / 16, (Game.getWindowHeight() - Mouse.getY()) / 16);
	}
	
	public void set(Point p, int tile) {
		history.push(new SetTileAction(p, tile, tiles[p.y][p.x]));
		history.peek().redo();
	}
	
	/**
	 * A fail-safe version of the set method. If the operation fails, 
	 * the method returns false instead of throwing an exception.
	 * @param p The point to modify.
	 * @param tile The tile to set.
	 * @return True, if successful.
	 */
	private boolean trySet(Point p, int tile) {
		if(isValid(p)) {
			set(p, tile);
			return true;
		}
		return false;
	}
	
	private void addSpawn(Color color) {
		Point pos = getMousePosition();
		SpawnPoint spawn = new SpawnPoint(pos.x, pos.y, color);
		if(!spawns.add(spawn))
			System.err.println("Spawnpoint already exists");
		else
			System.out.printf("Spawnpoint added at (%s, %s)%n", spawn.x, spawn.y);
	}
	
	private void removeSpawn(Color color) {
		
	}
	
	public void modifySize(int dx, int dy) {
		history.push(new ChangeSizeAction(dx, dy));
		history.peek().redo();
	}

	
	public void undo() {
		history.pop().undo();
	}
	
	public void redo() {
		//TODO
	}
	
	public void save(File file) {
		Level level = new Level(tiles[0].length, tiles.length, tiles, spawns);
		FileOutputStream fo;
		ObjectOutputStream oos;
		try {
			fo = new FileOutputStream(file);
			oos = new ObjectOutputStream(fo);
			oos.writeObject(level);
			oos.close();
			System.out.println("Level serialization successful.");
		} catch (FileNotFoundException e) {
			System.out.println("Invalid file path!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to create object output stream");
			e.printStackTrace();
		}
	}

	@Override
	public void onStep() {}

	@Override
	public void endStep() {}
	
	private static class Point {
		
		public final int x, y;
		
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	private class ChangeSizeAction extends Action {
		public final int dx;
		public final int dy;
		
		public ChangeSizeAction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}

		@Override
		public void redo() {
			modifySize(-dx, -dy);
		}

		@Override
		public void undo() {
			modifySize(dx, dy);
		}
		
		private void modifySize(int dx, int dy) {
			int width = Math.max(0, tiles[0].length + dx);
			int height = Math.max(0, tiles.length + dy);
			
			int[][] newTiles = new int[height][width];
			
			int a = Math.min(height, tiles.length);
			int b = Math.min(width, tiles[0].length);
			
			for (int i = 0; i < a; i++)
				for (int j = 0; j < b; j++)
					newTiles[i][j] = tiles[i][j];
			
			tiles = newTiles;
		}
		
		
	}
	
	private class SetTileAction extends Action {

		public final Point position;
		public final int tile;
		public final int prev;
		
		public SetTileAction(Point position, int tile, int prev) {
			this.position = position;
			this.tile = tile;
			this.prev = prev;
		}

		@Override
		public void redo() {
			tiles[position.y][position.x] = tile;
		}

		@Override
		public void undo() {
			tiles[position.y][position.x] = prev;
		}
	}
	}
