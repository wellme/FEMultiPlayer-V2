package net.fe.resources.files.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;

import net.fe.resources.files.ZipFileIO;
import net.fe.unit.Weapon;

public class ResourceEditor extends JFrame {

	private static final long serialVersionUID = -1299817269714345762L;
	
	private ZipFileIO zip = new ZipFileIO(new File("resources/H.zip"), new File("working_directory"));
	
	
	private boolean hasChanges = false;
	
	private ArrayList<Weapon> weapons = new ArrayList<Weapon>();
	
	private File sourceFile;
	
	private JTextField txtName;
	private JTextField txtVersion;
	private JTextField txtAuthor;
	
	public static void main(String[] args) throws ZipException, IOException {
		new ResourceEditor().setVisible(true);
	}
	
	
	public ResourceEditor() throws ZipException, IOException {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if(hasChanges)
					switch (JOptionPane.showConfirmDialog(null, "Do it?")) {
						case JOptionPane.YES_OPTION:
							try {
								zip.save(sourceFile);
								//Falls through
							} catch (IOException e) {
								JOptionPane.showMessageDialog(null, "Failed to save : " + e +": " + e.getMessage());
								break;
							}
						case JOptionPane.NO_OPTION:
							try {
								zip.close();
							} catch (IOException e) {
								JOptionPane.showMessageDialog(null, "Failed to clean up : " + e +": " + e.getMessage());
							}
							exit();
							return;
						default: //Canceling 
							return;
					}
				else
					exit();
			}
			
			void exit() {
				dispose();
				System.exit(0);
			}
		});
		setTitle("Resource file editor v0.0.0");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((int) screenSize.getWidth()/2, (int) screenSize.getHeight()/2, 386, 284);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Failed to set look and feel");
		}
		
		//Closing operation logic handled by the event handler.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JPanel pnlInfo = new JPanel();
		getContentPane().add(pnlInfo, BorderLayout.WEST);
		GridBagLayout gbl_pnlInfo = new GridBagLayout();
		gbl_pnlInfo.columnWidths = new int[]{142, 0};
		gbl_pnlInfo.rowHeights = new int[] {0, 0, 0};
		gbl_pnlInfo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlInfo.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlInfo.setLayout(gbl_pnlInfo);
		
		JPanel pnlDescription = new JPanel();
		pnlDescription.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Description", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_pnlDescription = new GridBagConstraints();
		gbc_pnlDescription.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDescription.anchor = GridBagConstraints.NORTH;
		gbc_pnlDescription.gridx = 0;
		gbc_pnlDescription.gridy = 0;
		pnlInfo.add(pnlDescription, gbc_pnlDescription);
		GridBagLayout gbl_pnlDescription = new GridBagLayout();
		gbl_pnlDescription.columnWidths = new int[] {0, 0};
		gbl_pnlDescription.rowHeights = new int[] {0, 0, 0};
		gbl_pnlDescription.columnWeights = new double[]{0.0, 1.0};
		gbl_pnlDescription.rowWeights = new double[]{0.0, 0.0, 0.0};
		pnlDescription.setLayout(gbl_pnlDescription);
		
		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlDescription.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		txtName.setText("Whateverpack");
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(5, 0, 5, 0);
		gbc_txtName.anchor = GridBagConstraints.NORTHWEST;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlDescription.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		
		JLabel lblAuthor = new JLabel("Author");
		GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
		gbc_lblAuthor.anchor = GridBagConstraints.WEST;
		gbc_lblAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_lblAuthor.gridx = 0;
		gbc_lblAuthor.gridy = 1;
		pnlDescription.add(lblAuthor, gbc_lblAuthor);
		
		txtAuthor = new JTextField();
		txtAuthor.setText("author");
		GridBagConstraints gbc_txtAuthor = new GridBagConstraints();
		gbc_txtAuthor.anchor = GridBagConstraints.WEST;
		gbc_txtAuthor.insets = new Insets(0, 0, 5, 0);
		gbc_txtAuthor.gridx = 1;
		gbc_txtAuthor.gridy = 1;
		pnlDescription.add(txtAuthor, gbc_txtAuthor);
		txtAuthor.setColumns(10);
		
		JLabel lblVersion = new JLabel("Version");
		GridBagConstraints gbc_lblVersion = new GridBagConstraints();
		gbc_lblVersion.anchor = GridBagConstraints.WEST;
		gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
		gbc_lblVersion.gridx = 0;
		gbc_lblVersion.gridy = 2;
		pnlDescription.add(lblVersion, gbc_lblVersion);
		
		txtVersion = new JTextField();
		GridBagConstraints gbc_txtVersion = new GridBagConstraints();
		gbc_txtVersion.anchor = GridBagConstraints.WEST;
		gbc_txtVersion.insets = new Insets(0, 0, 5, 0);
		gbc_txtVersion.gridx = 1;
		gbc_txtVersion.gridy = 2;
		pnlDescription.add(txtVersion, gbc_txtVersion);
		txtVersion.setColumns(10);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		pnlInfo.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblSize = new JLabel("Size");
		GridBagConstraints gbc_lblSize = new GridBagConstraints();
		gbc_lblSize.anchor = GridBagConstraints.WEST;
		gbc_lblSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblSize.gridx = 0;
		gbc_lblSize.gridy = 0;
		panel.add(lblSize, gbc_lblSize);
		
		JLabel lblSizeOut = new JLabel("Size");
		GridBagConstraints gbc_lblSizeOut = new GridBagConstraints();
		gbc_lblSizeOut.anchor = GridBagConstraints.WEST;
		gbc_lblSizeOut.insets = new Insets(0, 0, 5, 0);
		gbc_lblSizeOut.gridx = 1;
		gbc_lblSizeOut.gridy = 0;
		panel.add(lblSizeOut, gbc_lblSizeOut);
		
		JLabel lblFileCount = new JLabel("File count");
		GridBagConstraints gbc_lblFileCount = new GridBagConstraints();
		gbc_lblFileCount.anchor = GridBagConstraints.WEST;
		gbc_lblFileCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblFileCount.gridx = 0;
		gbc_lblFileCount.gridy = 1;
		panel.add(lblFileCount, gbc_lblFileCount);
		
		JLabel lblFileCountOut = new JLabel("File count");
		GridBagConstraints gbc_lblFileCountOut = new GridBagConstraints();
		gbc_lblFileCountOut.anchor = GridBagConstraints.WEST;
		gbc_lblFileCountOut.insets = new Insets(0, 0, 5, 0);
		gbc_lblFileCountOut.gridx = 1;
		gbc_lblFileCountOut.gridy = 1;
		panel.add(lblFileCountOut, gbc_lblFileCountOut);
		
		JLabel lblFrameCount = new JLabel("Frame count");
		GridBagConstraints gbc_lblFrameCount = new GridBagConstraints();
		gbc_lblFrameCount.anchor = GridBagConstraints.WEST;
		gbc_lblFrameCount.insets = new Insets(0, 0, 0, 5);
		gbc_lblFrameCount.gridx = 0;
		gbc_lblFrameCount.gridy = 2;
		panel.add(lblFrameCount, gbc_lblFrameCount);
		
		JLabel lblFrameCountOut = new JLabel("Frame count");
		GridBagConstraints gbc_lblFrameCountOut = new GridBagConstraints();
		gbc_lblFrameCountOut.anchor = GridBagConstraints.WEST;
		gbc_lblFrameCountOut.gridx = 1;
		gbc_lblFrameCountOut.gridy = 2;
		panel.add(lblFrameCountOut, gbc_lblFrameCountOut);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JTree tree = new JTree();
		tree.setModel(getModel());
		scrollPane.setViewportView(tree);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				System.out.println(e);
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnFile.add(mntmOpen);
		
		JMenuItem mntmOpenRecent = new JMenuItem("Open recent");
		mnFile.add(mntmOpenRecent);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnFile.add(mntmSave);
		
		JMenuItem mntmSaveAs = new JMenuItem("Save as...");
		mnFile.add(mntmSaveAs);
		
		JMenuItem mntmClose = new JMenuItem("Close");
		mnFile.add(mntmClose);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmAddWeapon = new JMenuItem("Add weapon");
		mntmAddWeapon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addWeapon();
			}
		});
		mnEdit.add(mntmAddWeapon);
		
		JMenuItem mntmAddMap = new JMenuItem("Add map");
		mnEdit.add(mntmAddMap);
		
		JMenuItem mntmAddUnit = new JMenuItem("Add unit");
		mnEdit.add(mntmAddUnit);
		
		JMenuItem mntmAddClass = new JMenuItem("Add class");
		mnEdit.add(mntmAddClass);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
	}

	
	private void addWeapon() {
		
	}


	private TreeModel getModel() throws ZipException, IOException {
		return zip.asTree();
	}
}
