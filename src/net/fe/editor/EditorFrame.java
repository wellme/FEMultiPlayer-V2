package net.fe.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

public class EditorFrame extends JFrame {
	
	private static final long serialVersionUID = 5668737481915340413L;
	
	private LevelEditorStage stage;
	private LevelEditor editor;
	
	private JMenuItem mntmControls;
	private JMenu mnHelp;
	private JMenuItem mntmRedo;
	private JMenuItem mntmUndo;
	private JMenu mnEdit;
	private JMenuItem mntmExit;
	private JSeparator separator;
	private JMenuItem mntmSaveAs;
	private JMenuItem mntmSave;
	private JMenuItem mntmOpen;
	private JMenuItem mntmNew;
	private JMenu mnFile;
	private JMenuBar menuBar;
	
	private KeybindsFrame keybinds;
	private JScrollPane pnlConsole;
	private JTextArea txtConsole;
	private JPanel pnlInfo;
	private JLabel lblName;
	private JTextField txtName;
	private JLabel lblWidth;
	private JLabel lblHeight;
	private JSpinner spnWidth;
	private JSpinner spnHeight;
	private JSeparator separator_1;
	private JMenuItem mntmClearConsole;
	private JSeparator separator_2;
	
	private ChangeListener resizeListener;

	public static void main(String[] args) {
		EditorFrame frame = new EditorFrame();
		frame.editor = new LevelEditor();
		frame.editor.init(960, 640, "Fire Emblem Level Editor");
		frame.editor.setCloseRequestedListener(() -> frame.exitRequested());
		frame.setVisible(true);
		//frame.useAsConsoleOutput();
		frame.setStage(frame.editor.getStage());
		
		frame.editor.loop();
	}
	
	public EditorFrame() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(exitRequested())
					dispose();
			}
		});
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Failed to set look and feel");
		}

		setBounds(100, 100, 541, 226);
		
		setTitle("Level editor");
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = promtUnsavedChanges("New file", "Create a new file?");
				
				switch(result) {
					case JOptionPane.YES_OPTION:
						//TODO save
						saveLevel();
						//falls through
					case JOptionPane.NO_OPTION:
						//TODO new file
						break;
					case JOptionPane.CANCEL_OPTION:
						//Nothing to do.
						break;
				}
			}
		});
		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		mnFile.add(mntmNew);
		
		mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = promptFile();
				editor.changeMap(file.toString());
				setStage(editor.getStage());
			}
		});
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnFile.add(mntmOpen);
		
		mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveLevel();
			}
		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnFile.add(mntmSave);
		
		mntmSaveAs = new JMenuItem("Save as...");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO
			}
		});
		mnFile.add(mntmSaveAs);
		
		separator = new JSeparator();
		mnFile.add(separator);
		
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(exitRequested())
					dispose();
			}
		});
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnFile.add(mntmExit);
		
		mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		mntmUndo = new JMenuItem("Undo");
		mntmUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stage.undo();
			}
		});
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		mnEdit.add(mntmUndo);
		
		mntmRedo = new JMenuItem("Redo");
		mntmRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stage.redo();
			}
		});
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		mnEdit.add(mntmRedo);
		
		mntmClearConsole = new JMenuItem("Clear console");
		mntmClearConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtConsole.setText("");
			}
		});
		
		separator_2 = new JSeparator();
		mnEdit.add(separator_2);
		mntmClearConsole.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		mnEdit.add(mntmClearConsole);
		
		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		mntmControls = new JMenuItem("Controls");
		mntmControls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keybinds.setVisible(true);
			}
		});
		mnHelp.add(mntmControls);
		
		pnlConsole = new JScrollPane();
		getContentPane().add(pnlConsole, BorderLayout.CENTER);
		
		txtConsole = new JTextArea();
		txtConsole.setLineWrap(true);
		txtConsole.setEditable(false);
		pnlConsole.setViewportView(txtConsole);
		
		pnlInfo = new JPanel();
		getContentPane().add(pnlInfo, BorderLayout.WEST);
		GridBagLayout gbl_pnlInfo = new GridBagLayout();
		gbl_pnlInfo.columnWidths = new int[]{0, 0, 0};
		gbl_pnlInfo.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlInfo.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_pnlInfo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlInfo.setLayout(gbl_pnlInfo);
		
		lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlInfo.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		txtName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stage.setLevelName(txtName.getText());
				setTitle(txtName.getText());
			}
		});
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlInfo.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		
		lblWidth = new JLabel("Width");
		GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.anchor = GridBagConstraints.EAST;
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 0;
		gbc_lblWidth.gridy = 1;
		pnlInfo.add(lblWidth, gbc_lblWidth);
		
		resizeListener = e -> {
			stage.setSize((Integer)spnWidth.getValue(), (Integer)spnHeight.getValue());
		};
		
		spnWidth = new JSpinner();
		spnWidth.addChangeListener(resizeListener);
		spnWidth.setModel(new SpinnerNumberModel(10, 0, null, 1));
		GridBagConstraints gbc_spnWidth = new GridBagConstraints();
		gbc_spnWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnWidth.insets = new Insets(0, 0, 5, 0);
		gbc_spnWidth.gridx = 1;
		gbc_spnWidth.gridy = 1;
		pnlInfo.add(spnWidth, gbc_spnWidth);
		
		lblHeight = new JLabel("Height");
		GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.anchor = GridBagConstraints.EAST;
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 0;
		gbc_lblHeight.gridy = 2;
		pnlInfo.add(lblHeight, gbc_lblHeight);
		
		spnHeight = new JSpinner();
		spnHeight.addChangeListener(resizeListener);
		spnHeight.setModel(new SpinnerNumberModel(new Integer(10), new Integer(0), null, new Integer(1)));
		GridBagConstraints gbc_spnHeight = new GridBagConstraints();
		gbc_spnHeight.insets = new Insets(0, 0, 5, 0);
		gbc_spnHeight.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnHeight.gridx = 1;
		gbc_spnHeight.gridy = 2;
		pnlInfo.add(spnHeight, gbc_spnHeight);
		
		separator_1 = new JSeparator();
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.insets = new Insets(0, 0, 5, 5);
		gbc_separator_1.gridx = 0;
		gbc_separator_1.gridy = 3;
		pnlInfo.add(separator_1, gbc_separator_1);
		
		keybinds = new KeybindsFrame();
		keybinds.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	private boolean exitRequested() {
		int result = promtUnsavedChanges("Exit", "");
		
		switch(result) {
			case JOptionPane.YES_OPTION:
				saveLevel();
				//falls through
			case JOptionPane.NO_OPTION:
				//TODO this is kinda silly, but it doesn't really matter if System.exit() is called 
				//instead of actually relying on the caller to end the program?
				System.exit(0);
				return true;
			case JOptionPane.CANCEL_OPTION:
			default:
				//Nothing to do.
				return false;
		}
	}

	private void saveLevel() {
		stage.save();
	}

	public void useAsConsoleOutput() {
		System.setOut(new PrintStream(new TextAreaOutputStream()));
		System.setErr(new PrintStream(new TextAreaOutputStream()));
	}
	
	public void setStage(LevelEditorStage stage) {
		this.stage = stage;
		setTitle(stage.getLevelName());
		spnWidth.removeChangeListener(resizeListener);
		spnWidth.setValue(stage.getWidth());
		spnWidth.addChangeListener(resizeListener);
		spnHeight.setValue(stage.getHeight());
		txtName.setText(stage.getLevelName());
	}
	
	private int promtUnsavedChanges(String title, String content) {
		if(stage.hasChange()) {
			//TODO change default option to cancel instead of yes.
			int result = JOptionPane.showOptionDialog(this, 
					(content == null || content.equals("") ? "" : content + "\n") + "Unsaved changes will be lost.", title,
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
			return result;
		}
		//No action needs to be taken if there are no changes.
		return JOptionPane.NO_OPTION;
	}
	
	private static File promptFile() {
		JFileChooser chooser = new JFileChooser(new File("./levels"));
		chooser.showDialog(null, "Open");
		return chooser.getSelectedFile();
	}
	
	private static class KeybindsFrame extends JFrame {

		private static final long serialVersionUID = -4376848978555760917L;
		
		private JPanel contentPane;

		public KeybindsFrame() {
			
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
				System.err.println("Failed to set look and feel");
			}
			
			setTitle("Keybinds");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 274, 178);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new BorderLayout(0, 0));
			setContentPane(contentPane);
			
			JTextPane txtpnWShrinks = new JTextPane();
			txtpnWShrinks.setEditable(false);
			txtpnWShrinks.setText(
				"W - Shrinks the grid vertically\r\n " +
				"A - Shriks the grid horizontally\r\n" + 
				"S - Expands the grid vertically\r\n" +
				"D - Expands the grid horizontally\r\n"+
				"\r\n" + 
				"Z - Add a blue spawn point at target position\r\n" + 
				"X - Add a red spawn point at target position\r\n" +
				"C - Add a green spawn point at target position\r\n" +
				"V - Remove the spawn point at target position");
			contentPane.add(txtpnWShrinks, BorderLayout.CENTER);
		}
	}
	
	private class TextAreaOutputStream extends OutputStream {

		@Override
		public void write(int b) throws IOException {
			txtConsole.append(""+(char)b);
			pnlConsole.getVerticalScrollBar().setValue(pnlConsole.getVerticalScrollBar().getMaximum());
		}
		
	}
}
