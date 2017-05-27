package net.fe.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;

import net.fe.unit.Unit;
import net.fe.unit.UnitClass;
import net.fe.unit.Weapon;

public final class Loader {
	
	
	public static void main(String[] args) throws IOException {
		ResourceFile.loadFile(new File("resources\\H.zip").toPath());
	}
	
	private static final String RESOURCE_FOLDER = "resources";
	private static final String PATH_UNIT_CLASSES = "unit_classes";
	private static final String PATH_UNITS = "units";
	private static final String PATH_WEAPONS = "weapons";
	private static final String PATH_MAPS = "maps";
	
	private static UnitClass[] unitClasses;
	private static Unit[] units;
	private static Weapon[] weapons;
	
	private Loader() {
		
	}
	
	public static void loadAll() throws IOException {
		//Blame Streams for not being Iterable.
		for(File file : new File(RESOURCE_FOLDER).listFiles()) {
			try {
				Path filePath = file.toPath();
				System.out.println("Openning file " + file);
				try (FileSystem fs = FileSystems.newFileSystem(filePath, null)) {
					Path path = fs.getPath(PATH_UNIT_CLASSES);
					//System.out.println(Files.newInputStream(fs.getPath(PATH_UNIT_CLASSES)));
					System.out.println(Files.exists(path));
					PathMatcher matcher = fs.getPathMatcher("glob:/*");
					Iterator<Path> iterator = Files.find(fs.getPath("/"), 1, (p, basicFileAttributes) -> matcher.matches(p)).iterator();
					while(iterator.hasNext())
						System.out.println(iterator.next());
				}
			} catch (Throwable e) {
				//TODO log
			}
		}
	}
	
}
