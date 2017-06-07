package net.fe.resources.files.types;

import java.nio.file.Path;

import net.fe.resources.files.ResourceFile;
import net.fe.resources.files.Version;

/**
 * v0.0.0
 * <BR>
 * A resource file, containing all the information required by the game to function.
 * 
 * <p>
 * The root of the folder contains four folders, one for each of the following (with the name of the 
 * folder in parenthesis) :
 * <ul>
 * <li>Weapons (weapons)</li>
 * <li>Units (units)</li>
 * <li>Maps (maps)</li>
 * <li>Classes (unit_classes)</li>
 * </ul>
 * 
 * Each folder is organized differently.
 * </p>
 * <p>
 * The weapons folder contains a single file containing all the information about the weapons in a pseudo-csv type of file.
 * The reading of this file is handled by {@link net.fe.unit.WeaponFactory}.
 * </p>
 * <p>
 * The units folder contains a subfolder for every units present in the pack. The name of the folder is irrelevant, but it 
 * may be a good idea for the creator to name it according to the unit it represents.
 * This subfolder contains a JSON file (info.json) with information regarding it's name, gender, class, base stats, growths skills, etc.
 * It also contains the mugshot of the unit (mugshot.png).
 * </p>
 * <p>
 * The folder for classes contains three items, a JSON containing the information about every class (
 * </p>
 * 
 * @author wellme
 *
 */
public class ResourceFile_v0_0_0 extends ResourceFile {

	private static final String PATH_UNIT_CLASSES = "unit_classes";
	private static final String PATH_UNITS = "units";
	private static final String PATH_WEAPONS = "weapons";
	private static final String PATH_MAPS = "maps";
	
	private String name;
	private Version packVersion;
	private int randomInt;
	private String author;

	public ResourceFile_v0_0_0(Path path) {
		super(path);
	}

	@Override
	public void save(Path path) {
		
	}

	@Override
	public void modifyResources() {
		
	}

}
