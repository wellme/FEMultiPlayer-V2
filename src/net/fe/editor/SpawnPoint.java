package net.fe.editor;

import java.io.Serializable;

import org.newdawn.slick.Color;

/**
 * Represents a location where a unit will be placed upon level initialization
 */
public final class SpawnPoint implements Serializable {
	
	private static final long serialVersionUID = 8955139984944016201L;
	
	public final int x;
	public final int y;
	public final Color team;
	
	public SpawnPoint(int x, int y, Color team) {
		this.x = x;
		this.y = y;
		this.team = team;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpawnPoint other = (SpawnPoint) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		// LevelEditorStage depends on the team not counting in equals
		// so that both teams can't have a spawn on the same tile
		return true;
	}
	

}
