package net.fe;

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
		loadAll();
	}
	
	private static final String RESSOURCE_FOLDER = "ressources";
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
		File[] files = new File(RESSOURCE_FOLDER).listFiles();
		for(File file : files) {
			Path filePath = file.toPath();
			System.out.println("Openning file " + file);
			try (FileSystem fs = FileSystems.newFileSystem(filePath, null)) {
				Path path;
				if((path = fs.getPath(PATH_UNIT_CLASSES)) != null) {
					
				}
				PathMatcher matcher = fs.getPathMatcher("glob:/*");
				Iterator<Path> iterator = Files.find(fs.getPath("/"), 1, (p, basicFileAttributes) -> matcher.matches(p)).iterator();
				while(iterator.hasNext())
					System.out.println(iterator.next());
			}
		}
	}
}
