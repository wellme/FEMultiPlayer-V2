package net.fe.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

public class ZipFileIO implements AutoCloseable {

	private File workingDirectory;
	private File destination;
	
	public ZipFileIO(File file, File workingDirectory) throws ZipException, IOException {
		this(file, workingDirectory, file.exists());
	}
	
	public ZipFileIO(File file, File workingDirectory, boolean fileContainsEntries) throws ZipException, IOException {
		this.destination = file;
		this.workingDirectory = workingDirectory;
		if(workingDirectory.exists()) {
			deleteWorkingDirectory();
		}
		workingDirectory.mkdirs();
		if(fileContainsEntries) {
			try (ZipFile zipFile = new ZipFile(file)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				for(ZipEntry entry = entries.nextElement(); entries.hasMoreElements(); entry = entries.nextElement()) {
					File entryFile = new File(workingDirectory.getPath() + File.separator + entry.getName());
					try {
						if(entry.isDirectory()) {
							file.mkdir();
						} else {
							entryFile.getParentFile().mkdirs();
							entryFile.createNewFile();
							transferStream(zipFile.getInputStream(entry), new FileOutputStream(entryFile));
						}
					} catch (Exception e) {
						//TODO log
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	private void deleteWorkingDirectory() throws IOException {
		listAllFiles().forEach(file -> file.delete());
		ArrayList<File> folders = listAllFolders();
		folders.sort((a, b) -> a.getPath().split(Pattern.quote(File.separator)).length - b.getPath().split(Pattern.quote(File.separator)).length);
		folders.forEach(file -> file.delete());
	}

	/**
	 * Saves the zip entries to 
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	/*public void save(File file) throws FileNotFoundException, IOException {
		if(this.file.equals(file)) {
			File temp = File.createTempFile("tmp", ".zip");
			save(temp);
			zipFile.close();
			file.delete();
			temp.renameTo(file);
			zipFile = new ZipFile(file);
			return;
		}
		if(file.exists())
			file.delete();
		file.createNewFile();
		try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file))) {
			entries.forEach(entry -> {
				try {
					entry.setCompressedSize(-1);
					InputStream input = zipFile.getInputStream(entry);
					output.putNextEntry(entry);
					byte[] buffer = new byte[1 << 11];
					int byteCount = -1;
					while((byteCount = input.read(buffer)) != -1)
						output.write(buffer, 0, byteCount);
				} catch (IOException e) {
					e.printStackTrace();
					//TODO log
				}
			});
			output.flush();
			
		}
	}*/
	
	public void save(File destination) throws IOException {
		if(destination.exists())
			destination.delete();
		destination.createNewFile();
		try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(destination))) {
			for(File file : listAllFiles()) {
				output.putNextEntry(new ZipEntry(file.getPath()));
				transferStream(new FileInputStream(file), output);
			}
		}
			
	}
	
	private ArrayList<File> listAllFiles() {
		final ArrayList<File> files = new ArrayList<File>();
		final Stack<File> folders = new Stack<File>(); //Wow!! A stack!
		
		folders.push(workingDirectory);
		
		while(!folders.isEmpty())
			for(File element : folders.pop().listFiles())
				if(element.isDirectory())
					folders.push(element);
				else
					files.add(element);
		return files;
	}
	
	private ArrayList<File> listAllFolders() {
		final ArrayList<File> folders = new ArrayList<File>();
		final Stack<File> stack = new Stack<File>();
		
		stack.push(workingDirectory);
		
		while(!stack.isEmpty()) {
			folders.add(stack.peek());
			for(File folder : stack.pop().listFiles())
				if(folder.isDirectory()) {
					folders.add(folder);
					stack.push(folder);
				}
		}
		return folders;
	}
	
	/**
	 * Generate a Swing TreeModel representing the folder structure of this zip file.
	 * @return A tree representation of the zip file.
	 */
	public TreeModel asTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		for(File file : listAllFiles()) {
			DefaultMutableTreeNode currentNode = root;
			String[] names = file.getPath().split("\\Q" + File.separator + "\\E");
			for(int j = 1; j < names.length; j++) {
				DefaultMutableTreeNode futureNode = null;
				for(int k = 0; k < currentNode.getChildCount(); k++) {
					if(((DefaultMutableTreeNode)currentNode.getChildAt(k)).getUserObject().equals(names[j])) {
						futureNode = ((DefaultMutableTreeNode)currentNode.getChildAt(k));
						break;
					}
				}
				if(futureNode == null) {
					DefaultMutableTreeNode temp = new DefaultMutableTreeNode(names[j]);
					currentNode.add(temp);
					futureNode = temp;
				}
				currentNode = futureNode;
			}
		}
		return new DefaultTreeModel(root);
	}
	
	/**
	 * Adds a file to the working directory.
	 * @param file The file to add.
	 * @param destination The destinatio, relative to the root of the zip file.
	 * @throws IOException If an IOException occurs while adding the file.
	 */
	public void addFile(File file, File destination) throws IOException {
		Files.copy(file.toPath(), new File(workingDirectory.getPath() + File.separatorChar + destination.toPath()).toPath());
	}
	
	
	/**
	 * Transfers the content of an input stream in an output stream until no more data can be read from the input.
	 * This method closes neither streams.
	 * @param input The input stream.
	 * @param output The output stream.
	 * @throws IOException If an IOException occurs while transferring data.
	 */
	public static void transferStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1 << 11];
		int byteCount = -1;
		while((byteCount = input.read(buffer)) != -1)
			output.write(buffer, 0, byteCount);
	}

	@Override
	public void close() throws IOException {
		deleteWorkingDirectory();
	}
}
