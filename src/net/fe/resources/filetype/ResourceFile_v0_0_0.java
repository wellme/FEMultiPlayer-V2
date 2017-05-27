package net.fe.resources.filetype;

import java.nio.file.Path;

import net.fe.resources.ResourceFile;
import net.fe.resources.Version;

public class ResourceFile_v0_0_0 extends ResourceFile {

	private String name;
	private Version packVersion;
	private int randomInt;
	private String author;

	public ResourceFile_v0_0_0(Path path) {
		super(path);
		System.out.println("loaded!");
	}

	
	@Override
	public boolean canUpdate() {
		return false;
	}

	@Override
	public ResourceFile update() {
		return null;
	}

	@Override
	public void save(Path path) {
		
	}

}
