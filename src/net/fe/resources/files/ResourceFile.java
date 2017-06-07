package net.fe.resources.files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;


/**
 * A resource file is a zip file containing all resources used by the game.
 * The format of the file may vary by version, but every file must contain a file named "VERSION" in the root of
 * the zip file containing a single string of a {@link Version} indicating the version of the game that goes with
 * the file.
 * <p>
 * All other information about the resources themselves is read by one of the subclasses, whose sole purpose
 * is to read and store the information in said file. All subclasses must be in the package rnet.fe.resources.filetype
 * and must be named ResourceFile_[Version], where [Version] is the version of the resource file the class is
 * designed to read, formatted as described in {@link Version} but with dots (.) replaced by underscores (_). 
 * This format doesn't respect the Java naming convention, but deal with it, it's the easiest way to do this.
 * Additionally, they must have declared a constructor that takes a {@link Path} pointing towards the file to be read.
 * </p>
 * <p>
 * Subclasses should obviously document their inner structure to facilitate further development of resource packs.
 * </p>
 * <p>
 * When a new version of a resource file is available, the previous version must implement a way to update 
 * an older file to the new version by implementing the {@link #update()} and {@link #canUpdate()} methods.
 * </p>
 * 
 * @author wellme
 *
 */
public abstract class ResourceFile {
	
	private static final String RESOURCE_FILE_CLASS_PACKAGE_NAME = "net.fe.resources.filetype";

	/**
	 * Reminds the coder to implement a constructor taking one (and only one!) {@link Path} as parameter.
	 * @param path The path of the file.
	 */
	protected ResourceFile(Path path) {
		
	}
	
	/**
	 * Reads the version of a resource file.
	 * @param path The path of the file to read the version.
	 * @return The version of the file.
	 * @throws IOException If an IOExceptino occurs while loading the file.
	 */
	public static Version readVersion(Path path) throws IOException {
		return new Version(Files.newBufferedReader(FileSystems.newFileSystem(path, null).getPath("VERSION")).readLine());
	}
	
	/**
	 * Loads a file.
	 * @param path The path of the file to load.
	 * @return A ressource file coresponding to the file read.
	 * @throws IOException If an IOExceptino occurs while loading the file.
	 * @throws IllegalArgumentException If the version of the file is not valid (i.e. there's
	 * no reader available) or another error occurs while reading the file.
	 */
	public static ResourceFile loadFile(Path path) throws IOException, IllegalArgumentException {
		Version version = readVersion(path);
		String className = String.format("ResourceFile_%s", version);
		
		try {
			@SuppressWarnings("unchecked")
			//Java is made of magic and dreams
			Class<?> loadedClass = ResourceFile.class.getClassLoader().loadClass(RESOURCE_FILE_CLASS_PACKAGE_NAME + "." + className.replace('.', '_'));
			try {
				return (ResourceFile) loadedClass.getConstructor(Path.class).newInstance(path);
			} catch (ClassCastException e) {
				throw new RuntimeException(String.format("%s doesn't extend ResourceFile", loadedClass.getName()));
			}
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.format("%s's version is not valid (%s)", path, version), e);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(String.format("Failed to load the file (Version : %s)", version), e);
		} 
	}
	
	/**
	 * Modifies the resources declared in {@link net.fe.resources.objects.FEResources FEResources}.
	 * @throws OutdatedVersionException If the version is no longer compatible with the current
	 * version of the game. 
	 */
	public abstract void modifyResources() throws OutdatedVersionException;
	
	/**
	 * Save this file to the path specified.
	 * @param path The path to save the file.
	 */
	public abstract void save(Path path);
	
	/**
	 * Returns true if a newer version of a resource file is available and an implementation to
	 * smoothly transition to the newer version exists.
	 * @return Returns true if the file can be updated.
	 */
	public boolean canUpdate() {
		return false;
	}
	
	/**
	 * Returns true if the current version of the resource file is outdated and no longer supported
	 * by the game.
	 * @return True if the resource pack must be updated.
	 */
	public boolean mustUpdate() {
		return false;
	}
	
	/**
	 * A method that returns a resource file containing the same information as this one, but updated
	 * to a newer version. This method doesn't need to update the file to the <em>newest</em> version.
	 * @return A newer version of the resource file.
	 * @throws UnsupportedOperationException If a newer version of the file doesn't exist.
	 */
	public ResourceFile update() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("No newer version available");
	}

	
	public static class OutdatedVersionException extends Exception {

		private static final long serialVersionUID = 4111695848664064373L;
		
	}
}
