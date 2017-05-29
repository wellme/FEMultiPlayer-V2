package net.fe.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

public class ZipFileIO {

	
	private ZipFile zipFile;
	private ArrayList<ZipEntry> entries = new ArrayList<ZipEntry>();
	private File file;
	
	public static void main(String[] args) throws FileNotFoundException, ZipException, IOException {
		new ZipFileIO(new File("resources/H.zip")).save(new File("resources/H.zip"));
	}
	
	public ZipFileIO(File file) throws ZipException, IOException {
		this.file = file;
		zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		for(ZipEntry entry = entries.nextElement(); entries.hasMoreElements(); entry = entries.nextElement())
			this.entries.add(entry);
	}

	/**
	 * Saves the zip entries to 
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void save(File file) throws FileNotFoundException, IOException {
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
	}
	
	/**
	 * Generate a Swing TreeModel representing the folder structure of this zip file.
	 * @return A tree representation of the zip file.
	 */
	public TreeModel asTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		for(int i = 0; i < entries.size(); i++) {
			DefaultMutableTreeNode currentNode = root;
			String[] names = entries.get(i).getName().split("/");
			for(int j = 0; j < names.length; j++) {
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
}
