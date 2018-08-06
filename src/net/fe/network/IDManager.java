package net.fe.network;

import java.util.Random;
import java.util.TreeSet;

/**
 * Creates and manages IDs.
 * An ID is a 32-bit binary string that can represent the server (id == 0),
 * a lobby (id < -1), a player (id > 0), or an unknown/undefined entity (id == -1)
 * @author wellme
 *
 */
public class IDManager {
	
	private TreeSet<Integer> usedIDs = new TreeSet<>();
	private Random rng = new Random();
	
	public IDManager() {
		
	}
	
	public synchronized int newPlayerID() {
		int val = generatePlayerID();
		while(usedIDs.contains(val))
			val = generatePlayerID();
		usedIDs.add(val);
		return val;
	}
	
	private int generatePlayerID() {
		return rng.nextInt(Integer.MAX_VALUE) + 1;
	}

	public synchronized int newLobbyID() {
		int val = generatePlayerID();
		while(usedIDs.contains(val))
			val = generatePlayerID();
		usedIDs.add(val);
		return val;
	}
	
	public int generateLobbyID() {
		return ~(rng.nextInt(Integer.MAX_VALUE) + 1);
	}
	
	public synchronized void freeID(int id) {
		usedIDs.remove(id);
	}
	
	public synchronized boolean inUse(int id) {
		return usedIDs.contains(id);
	}
	
	public static boolean isPlayer(int id) {
		return id > 0;
	}
	
	public static boolean isServer(int id) {
		return id == 0;
	}
	
	public static boolean isValid(int id) {
		return id != -1;
	}

	public static boolean isLobby(int id) {
		return id < -1;
	}
	
}
