import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

 
@SuppressWarnings("serial")
public class LoLWin extends JFrame
					implements ActionListener, ListSelectionListener, KeyListener, FocusListener, MouseListener {
	
	private LoLWin self;
	private JPanel pan;
	private JComboBox combo;
	private JLabel label;
	private JButton addClient;
	private JButton save;
	private JButton delete;
	private JButton exit;
	private String filterText;
	private JTextField search;
	private JTextField filter;
	private String filterItems;
	private JLabel filterLabel;
	private JList list;
	private ChampionListModel sampleModel;
	private JComboBox modeSel;
	private LoLTableModel tableModel;
	private JButton createBuild;
	private LoLTable tableau;
	private JScrollPane tableauScroll;
	private JMenu clientMenu;
	private JMenuItem synch;
	private ButtonGroup group;
	private LoLResources LoL;
	public LoLSmartcastSwitcher smartcastSwitcher;
	private ArrayList<String>champList;
	private IniFile config;
	private int sizeHeight;
	private int sizeWidth;
	private LoLClientManager clientsManager = LoLClientManager.getInst();
	private LoLCustomBuildsManager buildsManager = LoLCustomBuildsManager.getInst();
	
	public static JPopupMenu menu;
     
	// TODO: remove comment from Application.java file before deploying
	final static String version = "1.3.7";
	final static String appName = "LoL Custom Builds";
	final static String appFullName = appName + " (" + version + ")";
	final static String website = "http://lcb.spreadeas.com";
	final static String configFile = "lolbuilds.ini";
	final static String dataFile = "data.ini";
	final static String clientsBuildsFile = "clientsbuilds.ini";
	final static String globalBuildsFile = "builds.ini";
	final static String recItemsFile = "recItems.ini";
     
     // Class Loading
     public static class Loading extends JFrame {
    	 private JProgressBar barre = new JProgressBar();
    	 private JLabel label = new JLabel(LocaleString.string("Loading"));
    	 public Loading() {
    		 this.setSize(480, 90);
    		 this.setLocationRelativeTo(null);
             this.setUndecorated(true);
    		 JPanel pan = new JPanel();
    		 barre.setIndeterminate(true);
             barre.setPreferredSize(new Dimension(430, 40));
             pan.add(barre);
    		 pan.add(label);
    		 this.setContentPane(pan);
             this.setVisible(true);
    	 }
    	 public void setLabel(String str) {
    		 label.setText(str);
    	 }
     }
     
     public class ChampionListModel extends DefaultListModel {
    	 public void refresh() {
    		 this.fireContentsChanged(this, 0, getSize());
    	 }
     }
     
     // this method can contains version/dependant code, and will just be executed when new version is run for the first time
     public void updateMethod() {
    	 String oldV = config.getValue("general", "version");
    	 // versions prior to 1.1.0 :
    	 // delete the champions section in lolbuilds.ini file to get skills name and desc
    	 if (LoLUpdater.compareVersions(oldV, "1.1.0") < 0) {
    		 IniFile ini = new IniFile(LoLWin.dataFile);
        	 ini.removeSection("champions");
        	 ini.save(LoLWin.dataFile);
    	 }
    	 // versions prior to 1.3.0
    	 // riot patch that completly changed the way custom build work
    	 // added v 1.3.1 because some items have to be deleted as well from the data file
    	 if (LoLUpdater.compareVersions(oldV, "1.3.1") < 0) {
    		 // global builds
    		 // clients builds
    		 // remove items as we now also fetch their price
    		 IniFile ini = new IniFile(LoLWin.dataFile);
        	 ini.removeSection("items");
        	 ini.save(LoLWin.dataFile);
        	 // TODO: champion will also probably have to be deleted
        	 // as the recItem system also changed
    	 }
    	 // versions prior to 1.3.2
    	 // riot changed stuff on their website, so i had to rewrite champions and items fetching process
    	 if (LoLUpdater.compareVersions(oldV, "1.3.2") < 0) {
    		 // we delete the data file
    		 File f = new File(LoLWin.dataFile);
    		 if (f.exists()) {
    			 f.delete();
    		 }
    	 }
    	 // versions prior to 1.3.3
    	 // riot changed stuff on their website again, so i had to rewrite totally and items fetching process
    	 // but here we just remove the cookie that is not a "real" item
    	 if (LoLUpdater.compareVersions(oldV, "1.3.3") < 0) {
    		 IniFile ini = new IniFile(LoLWin.dataFile);
    		 IniFile.IniSection sec = ini.getSection("items");
    		 if (sec != null) {
    			 sec.remove("2050");
    		 }
        	 ini.save(LoLWin.dataFile);
    	 }
     }
     
     @SuppressWarnings("unchecked")
     public LoLWin() {
    	 self = this;
    	 // load config data
         File conf = new File(configFile);
                  
         // if no file create and put default settings
         if (!conf.exists()) {
			createDefaultConfigFile(conf);
         }
         config = new IniFile(conf);
         
         // set language
    	 LocaleString.getInst().initLocale(config.getValue("general", "language"));
         
         // if auto update mode on, check for update
         if (config.getValue("general", "autoupdate").equals("1")) {
        	 LoLUpdater.checkUpdate(true);
         }
         
    	 // if a new version has just been installed
         if (LoLUpdater.compareVersions(config.getValue("general", "version"), version) < 0) {
        	 // call a method in which we can perform specific action for this last release
        	 updateMethod();
         	// update version number
         	config.addValue("general", "version", version);
         	config.save(configFile);
         	Log.getInst().info("Program correctly updated to version " + version);
         }
         
         // loading frame
         Loading win = new Loading();
         
         // initialization of the data
         LoL = new LoLResources(win);
         LoL.init();
         buildsManager.setResources(LoL);
         clientsManager.loadClients(configFile);
         smartcastSwitcher = new LoLSmartcastSwitcher();
         champList = (ArrayList<String>)LoL.champList.clone();
         Collections.sort(champList);
         
         // building window
         this.setTitle(appFullName);
         sizeHeight = 760;
         sizeWidth = 640;
         this.setSize(sizeWidth, sizeHeight);
         this.setMinimumSize(new Dimension(sizeWidth, sizeHeight));
         this.setPreferredSize(new Dimension(sizeWidth, sizeHeight));
         this.setLocationRelativeTo(null);
         this.setResizable(true);
         this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         this.setJMenuBar(createMenus());
         this.setIconImage(ResourcesManager.getInst().getIcon("lcb.png", true).getImage());
         // have to do that otherwise popup menu isn't closed on focus lost
         this.addWindowFocusListener(new WindowFocusListener() {
			public void windowLostFocus(WindowEvent arg0) {
				if (menu != null) menu.setVisible(false);
			}
			public void windowGainedFocus(WindowEvent arg0) {}
         });
         
         // adding components
         pan = new JPanel(new BorderLayout());
         
         JPanel topPan = new JPanel();
         topPan.setPreferredSize(new Dimension(sizeWidth, 390));
         
         label = new JLabel(LocaleString.string("List of your LoL clients"));
         topPan.add(label);
         
         combo = new JComboBox();
         combo.setPreferredSize(new Dimension(535, 30));
         combo.setRenderer(new IconListRenderer());
         combo.setEnabled(false);
         topPan.add(combo);
         
         delete = new JButton(ResourcesManager.getInst().getIcon("remove.png", true));
         delete.addActionListener(this);
         delete.setToolTipText(LocaleString.string("Delete client"));
         delete.setEnabled(false);
         topPan.add(delete);
         
         //Ajout du bouton a notre contentPane
         addClient = new JButton(LocaleString.string("Add new LoL Client"),
        		 ResourcesManager.getInst().getIcon("add.png", true));
         addClient.setPreferredSize(new Dimension(addClient.getPreferredSize().width, 35));
         addClient.addActionListener(this);
         topPan.add(addClient);
         
         // save button
         save = new JButton(LocaleString.string("Save builds"), ResourcesManager.getInst().getIcon("save.png", true));
         save.addActionListener(this);
         save.setPreferredSize(new Dimension(save.getPreferredSize().width, 35));
         save.setEnabled(false);
         topPan.add(save);
         
         //Ajout du bouton a notre contentPane
         exit =	new JButton(LocaleString.string("Exit $0$", appName),
        		 ResourcesManager.getInst().getIcon("exit.png", true));
         exit.setPreferredSize(new Dimension(exit.getPreferredSize().width, 35));
         exit.addActionListener(this);
         topPan.add(exit);
         
         // champions list
         sampleModel = new ChampionListModel();
         list = new JList(sampleModel);
         list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         list.setVisibleRowCount(-1);
         list.addListSelectionListener(this);
         list.addMouseListener(this);
         list.setCellRenderer(new DataIconListRenderer(LoL, LoLResources.Type.CHAMPION, smartcastSwitcher));
         list.setBackground(Color.black);
         JScrollPane listPane = new JScrollPane(list);
         listPane.setPreferredSize(new Dimension(565, 245));
         topPan.add(listPane);
         
         // search textfield
         filterText = LocaleString.string("Filter champions list");
         search = new JTextField(filterText, 20);
         search.addKeyListener(this);
         search.addFocusListener(this);
         search.addMouseListener(this);
         search.setEnabled(false);
         
         Box box = Box.createHorizontalBox();
         box.setPreferredSize(new Dimension(570, 30));
         box.add(search);
         box.add(Box.createHorizontalStrut(15));

         JLabel modeLb = new JLabel(LocaleString.string("Game Mode") + " : ");
         modeSel = new JComboBox(GameMode.values());
         modeSel.addActionListener(this);
         box.add(modeLb);
         box.add(modeSel);
         
         box.add(Box.createHorizontalStrut(15));

         createBuild = new JButton(LocaleString.string("Create new build"));
         createBuild.setEnabled(false);
         createBuild.addActionListener(this);
         box.add(createBuild);    
         topPan.add(box);

         // tableau
         String  title[] = {LocaleString.string("Champion"),
        		 			LocaleString.string("Active Build"),
        		 			LocaleString.string("Build Items"), ""};
         tableModel = new LoLTableModel(title);
         tableau = new LoLTable(tableModel);  
         tableauScroll = new JScrollPane(tableau);
         
         pan.add(topPan, BorderLayout.NORTH);
         
         pan.add(tableauScroll, BorderLayout.CENTER);
         
         
         FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
         fl.setHgap(1);
         fl.setVgap(1);
         JPanel botPan = new JPanel(fl);
         botPan.setPreferredSize(new Dimension(sizeWidth, 30));
         
         filterItems = LocaleString.string("Build Items") + "...";
         filter = new JTextField(filterItems);
         filter.setPreferredSize(new Dimension(294, 25));
         filter.setToolTipText(LocaleString.string("Filter build items"));
         filter.addKeyListener(this);
         filter.addFocusListener(this);
         filter.addMouseListener(this);
         filter.setEnabled(false);
         botPan.add(filter);
         
         botPan.add(Box.createHorizontalStrut(4));
         
         filterLabel = new JLabel();
         botPan.add(filterLabel);
         pan.add(botPan, BorderLayout.SOUTH);
         
         // fill the clients combo
         for (LoLClient c : clientsManager.getClients()) {
        	 combo.addItem(c);
         }

         // enable delete button if clients, synch if at least 2 clients
         if (clientsManager.getClientCount() > 0) {
        	 delete.setEnabled(true);
        	 save.setEnabled(true);
        	 if (clientsManager.getClientCount() > 1)
        		 synch.setEnabled(true);
         }
         
         // add the combo listener and select active client
         combo.addActionListener(this);
         if (clientsManager.getActiveClient() != null) {
        	 combo.setSelectedItem(clientsManager.getActiveClient());
         }
      
         // fill the champions list
         for (int i=0; i<LoL.champList.size(); i++) {
        	 sampleModel.addElement(champList.get(i));
         }
         
         this.setContentPane(pan);
         
         this.addWindowStateListener(new WindowAdapter() {
        	 public void windowStateChanged(WindowEvent e) {
        		 if (e.getNewState() == JFrame.MAXIMIZED_BOTH) {
        			 self.setExtendedState(JFrame.NORMAL);
        			 Toolkit.getDefaultToolkit().beep();
        		 }
        	 }
         });
         
         win.dispose();
         this.setVisible(true);
         
         addWindowListener(new WindowAdapter() {
        	 public void windowClosing(WindowEvent e) {
        		 closeWindow();
        	 }
         });
         
         // TODO: upload builds from current client to website for stats, when to do that ?
         //upload();
		
		//readRecommendedBuilds();
         
     }
     
     public JMenuBar createMenus() {
    	 JMenuBar menuBar;
    	 JMenu options, language;
    	 JMenuItem reset, update;
    	 JCheckBoxMenuItem autoUpdate;

    	 //Create the menu bar.
    	 menuBar = new JMenuBar();
    	 
    	 clientMenu = new JMenu(LocaleString.string("Client"));
    	 
    	 JMenuItem smartcast = new JMenuItem("Smartcast Switcher");
    	 smartcast.addActionListener(new ActionListener() {
    		 public void actionPerformed(ActionEvent e) {
    			 new LoLSmartcastSwitcherWin(LoLWin.this, LoL, smartcastSwitcher);
    		 }
    	 });
    	 clientMenu.add(smartcast);

    	 clientMenu.add(new JSeparator());
    	 
    	 JMenuItem importM = new JMenuItem(LocaleString.string("Import") + "...");
    	 importM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				// select the file to import
				JFileChooser chooser = new JFileChooser(new File("."));
				// filter for our special file format
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".lcb"));
					}
					public String getDescription() {
						return appName + " (*.lcb)";
					}
				});
			    // display the dialog
			    int returnVal = chooser.showOpenDialog(null);
			    if (returnVal == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().exists()) {
			    	
			    	int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Import warning"),
							 LocaleString.string("Import"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		    		 
			    	if (res == JOptionPane.OK_OPTION) {
			    		buildsManager.importBuilds(chooser.getSelectedFile());
			    		tableModel.updateData(null);
			    	}
			    }
			}
    	 });
    	 clientMenu.add(importM);
    	 
    	 JMenuItem exportM = new JMenuItem(LocaleString.string("Export") + "...");
    	 exportM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// select the file to export
				JFileChooser chooser = new JFileChooser(new File("."));
				// filter for our special file format
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".lcb"));
					}
					public String getDescription() {
						return appName + " (*.lcb)";
					}
				});
			    // display the dialog
			    int returnVal = chooser.showSaveDialog(null);
			    if (returnVal == JOptionPane.OK_OPTION) {
			    	String file = chooser.getSelectedFile().getAbsolutePath();
			    	if (!file.endsWith(".lcb")) file += ".lcb";
			    	if (new File(file).exists()) {
			    		int res = JOptionPane.showConfirmDialog(null, LocaleString.string("File exists desc $0$", file.replace("\\", "\\\\")),
			    				LocaleString.string("File exists"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				    	if (res != JOptionPane.OK_OPTION)
				    		return;
			    	}
			    	buildsManager.exportBuilds(file);
		    	}
			}
    	 });
    	 clientMenu.add(exportM);
    	 
    	 clientMenu.add(new JSeparator());
    	 
    	 JMenuItem clientClr = new JMenuItem(LocaleString.string("Delete all"));
    	 clientClr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoLClient c = (LoLClient)combo.getSelectedItem();
				String cname = new File(c.getClientPath()).getName();
				int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Delete all confirm $0$", cname),
						LocaleString.string("Delete all"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
	    		 
		    	if (res == JOptionPane.OK_OPTION) {
					buildsManager.clear();
		    		tableModel.updateData(null);
		    	}
			}
    	 });
    	 clientMenu.add(clientClr);
    	 
    	 clientMenu.setEnabled(false);
    	 menuBar.add(clientMenu);
    	 
    	 //Build the first menu.
    	 options = new JMenu(LocaleString.string("Options"));
    	 
    	 reset = new JMenuItem(LocaleString.string("Reset data"), KeyEvent.VK_R);
    	 reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Reset data confirm"),
						LocaleString.string("Reset data"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	    		 if (res == JOptionPane.OK_OPTION) {
	    			 File f = new File(LoLWin.dataFile);
	    			 // delete data file
	    			 if (f.exists()) f.deleteOnExit();
	    			 // delete pictures
	    			 for (File img : new File("images").listFiles()) {
	    				 img.deleteOnExit();
	    			 }
	    			 JOptionPane.showMessageDialog(null, LocaleString.string("Reset restart"),
							 LocaleString.string("Reset data"), JOptionPane.INFORMATION_MESSAGE);
	    		 }
			} 
    	 });
    	 options.add(reset);
    	 options.addSeparator();
    	
    	 synch = new JMenuItem(LocaleString.string("Synch clients") + "...", KeyEvent.VK_S);
    	 synch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	
				new LoLSynchronizer(self, config);
			}
    	 });
    	 synch.setEnabled(false);
    	 // TODO: for future release !
    	 //options.add(synch);
    	 
    	 JMenu globuilds = new JMenu(LocaleString.string("Global builds"));
    	 
    	 JMenuItem importGlob = new JMenuItem(LocaleString.string("Import simple") + "...");
    	 importGlob.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// select the file to import
				JFileChooser chooser = new JFileChooser(new File("."));
				// filter for our special file format
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".lcbg"));
					}
					public String getDescription() {
						return "LCB Global Builds" + " (*.lcbg)";
					}
				});
			    // display the dialog
			    int returnVal = chooser.showOpenDialog(null);
			    if (returnVal == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().exists()) {
			    	int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Global_build_import_warning_$0$", chooser.getSelectedFile().getName()),
							 LocaleString.string("Import"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		    		 
			    	if (res == JOptionPane.OK_OPTION) {
			    		LoLCustomBuildsManager.importGlobalBuilds(chooser.getSelectedFile().getAbsolutePath());

			    	}
			    }
			}
    	 });
    	 globuilds.add(importGlob);
    	 
    	 JMenuItem exportGlob = new JMenuItem(LocaleString.string("Export simple") + "...");
    	 exportGlob.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// select the file to export
				JFileChooser chooser = new JFileChooser(new File("."));
				// filter for our special file format
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".lcbg"));
					}
					public String getDescription() {
						return "LCB Global Builds" + " (*.lcbg)";
					}
				});
			    // display the dialog
			    int returnVal = chooser.showSaveDialog(null);
			    if (returnVal == JOptionPane.OK_OPTION) {
			    	String file = chooser.getSelectedFile().getAbsolutePath();
			    	if (!file.endsWith(".lcbg")) file += ".lcbg";
			    	if (new File(file).exists()) {
			    		int res = JOptionPane.showConfirmDialog(null, LocaleString.string("File exists desc $0$", file.replace("\\", "\\\\")),
			    				LocaleString.string("File exists"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				    	if (res != JOptionPane.OK_OPTION)
				    		return;
			    	}
			    	LoLCustomBuildsManager.exportGlobalBuilds(file);
		    	}
			}
    	 });
    	 globuilds.add(exportGlob);
    	 options.add(globuilds);
    	 options.addSeparator();
    	 
    	 // language menu
    	 language = createLanguageMenu();
    	 options.add(language);
    	 
    	 update = new JMenuItem(LocaleString.string("Check update") + "...", KeyEvent.VK_W);
    	 update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				LoLUpdater.checkUpdate(false);
			} 
    	 });
    	 options.add(update);
    	 
    	 autoUpdate = new JCheckBoxMenuItem(LocaleString.string("Autoupdate"));
    	 autoUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String autoupdate = (((JCheckBoxMenuItem)e.getSource()).isSelected()) ? "1" : "0";
				config.addValue("general", "autoupdate", autoupdate);
				config.save(new File(configFile));
			}
    	 });
    	 autoUpdate.setSelected(config.getValue("general", "autoupdate").equals("1"));
    	 options.add(autoUpdate);
    	 
    	 menuBar.add(options);
    	
    	 // help menu
    	 JMenu help = new JMenu(LocaleString.string("Help"));
    	 
    	 JMenuItem about = new JMenuItem(LocaleString.string("About"));
    	 about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String thanks = "<br/><br/><b>Special thanks to :</b><br/>";
				thanks += "- badaboomxx (spanish translation)<br/>";
				thanks += "- Isandrithrawiel [EU-NE] (romanian translation)<br/>";
				thanks += "- RiisK (german translation)<br/>";
				thanks += "- Ham3D Hodjati (persian translation)<br/>";
				thanks += "- Keerg (italian translation)<br/>";
				thanks += "- Jerakin (swedish translation)<br/>";
				thanks += "- Chjimmy (traditional chinese translation)<br/>";
				thanks += "</html>";
				JOptionPane.showMessageDialog(null, "<html><b>" + appFullName + "</b><br/>" + website + "<br/><br/>By fjxokt (fjxokt@gmail.com)" + thanks,
						 LocaleString.string("About"), JOptionPane.INFORMATION_MESSAGE);
			}
    	 });
    	 help.add(about);

    	 menuBar.add(help);
    	    	 
    	 return menuBar;
     }
     
     public void upload() {
    	 String filename = "upload_builds.ini";
    	 File file = new File(filename);
    	 buildsManager.exportBuilds(filename);
    	 try {
    		 // TODO: replace this with the actual website adress
	    	 //HttpURLConnection httpUrlConnection = (HttpURLConnection)new URL(website + "/upload.php").openConnection();
	    	 HttpURLConnection httpUrlConnection = (HttpURLConnection)new URL("http://localhost:8888/LoLCustomBuilds/upload.php").openConnection();
	         httpUrlConnection.setDoOutput(true);
	         httpUrlConnection.setRequestMethod("POST");
	         OutputStream os = httpUrlConnection.getOutputStream();
	         Thread.sleep(1000);
	         
	         // send file
	         BufferedInputStream fis = new BufferedInputStream(new FileInputStream(filename));
	         for (int i = 0; i < file.length(); i++) {
	             os.write(fis.read());
	         }
	         os.close();
	         
	         // get server response (useful ?)
	         BufferedReader in = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
	         String s = null;
	         while ((s = in.readLine()) != null) {
	             System.out.println(s);
	         }
	         in.close();
	         fis.close();
    	 } catch (Exception e) {
    		 e.printStackTrace();
    	 }
    	 file.delete();
     }
     
     public JMenu createLanguageMenu() {
    	 // create the menu
    	 JMenu language = new JMenu(LocaleString.string("Language"));
    	 // get current language
    	 String curLang = config.getValue("general", "language");
    	 // button group
    	 group = new ButtonGroup();
    	 // create the different languages menu items
    	 JMenuItem len;
    	 // get the languages list
    	 for (Languages.Lang lang : Languages.getList()) {
    		 len = new JRadioButtonMenuItem(lang.name);
    		 len.setActionCommand(lang.locale);
    		 // listener
    		 len.addActionListener(new ActionListener() {
    			 public void actionPerformed(ActionEvent e) {
    				 JRadioButtonMenuItem m = (JRadioButtonMenuItem)e.getSource();
    				 // if selected language is already active, nothing to do
    				 if (m.getActionCommand().equals(LocaleString.locale)) return;
    				 // select the menu item
    				 group.setSelected(m.getModel(), true);
    				 // change the current locale in LocaleString
    				 LocaleString.locale = m.getActionCommand();
    				 // save the config file with the changes
    				 config.addValue("general", "language", m.getActionCommand());
    				 config.save(configFile);
    				 // erase items part in the data file 
    				 IniFile data = new IniFile(dataFile);
    				 data.emptySection("items");
    				 data.save(dataFile);
    				 // show restart program dialog
    				 JOptionPane.showMessageDialog(null, LocaleString.string("Change language text"),
    						 LocaleString.string("Change language title"), JOptionPane.INFORMATION_MESSAGE);
    			 }
    		 });
    		 language.add(len);
    		 group.add(len);
    		// if active language, just check it
    		 if (curLang.equals(lang.locale)) {
    			 len.setSelected(true);
    		 }
    	 }
    	 return language;
     }
     
     public void createDefaultConfigFile(File conf) {
    	 try {
			conf.createNewFile();
	        IniFile config = new IniFile(conf);
	        IniFile.IniSection sec = config.createSection("general");
	        sec.put("version", version);
	        sec.put("clients", "0");
	        sec.put("autoupdate", "1");
	        sec.put("language", "en_US");
	        config.save(conf); 
		} catch (IOException e) {
			e.printStackTrace();
		}
     }
     
     public void createBuild() {
    	 String champ = (String)list.getSelectedValue();
    	 tableModel.createBuild(champ, buildsManager.getMode());
    	 tableau.scrollRectToVisible(new Rectangle(0,
				tableau.getRowHeight()*tableModel.currentList().indexOf(champ),
				400, tableau.getRowHeight()));
     }
     
     public void refresh() {
    	 combo.setSelectedIndex(combo.getSelectedIndex());
     }
     
     public void resetUI() {
    	 delete.setEnabled(false);
    	 save.setEnabled(false);
    	 combo.setEnabled(false);
    	 createBuild.setEnabled(false);
    	 clientMenu.setEnabled(false);
    	 synch.setEnabled(false);
    	 tableModel.clear();
     }
     
     public void deleteClient(int index) {
    	 // check
    	 if (index == -1) return;
    	 
    	 LoLClient remClient = (LoLClient)combo.getItemAt(index);
    	 clientsManager.removeClient(remClient);
    	 config.removeSection(remClient.getClientId());
    	 
    	 int nbClients = clientsManager.getClientCount();
    	 config.addValue("general", "clients", nbClients);
    	 clientsManager.addClientsToIni(config);
    	 
 		// if no more client
 		if (nbClients == 0) {
 			config.removeValue("general", "activeClient");
 			resetUI();
 		}
 		else if (nbClients == 1) {
 			synch.setEnabled(false);
 		}
    	 
    	 
//		int nbClients = Integer.parseInt(config.getValue("general", "clients"));
//		config.addValue("general", "clients", Integer.toString(nbClients-1));
//		// remove the client
//		config.removeSection("client" + index);
//		// rename the clients above the client deleted
//		for (int i=index+1; i<nbClients; i++) {
//			config.renameSection("client" + i, "client" + (i-1));
//		}
//		// if no more client
//		if (nbClients-1 == 0) {
//			config.removeValue("general", "activeClient");
//			resetUI();
//		}
//		else if (nbClients-1 == 1) {
//			synch.setEnabled(false);
//		}
		// save data
		config.save(configFile);
    	// remove string from combo
		combo.removeItemAt(index);
     }
     
     private void closeWindow() {
    	 int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Exit $0$ text", new String[]{appName}),
					LocaleString.string("Exit"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		 	// if ok, exit
		 	if (res == JOptionPane.OK_OPTION) {
		 		Log.getInst().info("Exiting " + appFullName);
		 		System.exit(DISPOSE_ON_CLOSE);
		 	}
     }
     
     //////////////////////
     // EVENTS HANDLING
     //////////////////////

     public void actionPerformed(ActionEvent e) {
    	 if (e.getSource() == addClient) {
    		 JFileChooser chooser = new JFileChooser();
    		 chooser.setDialogTitle(LocaleString.string("Select League of legends folder"));
    		 chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    		 int returnVal = chooser.showOpenDialog(null);
    		 if (returnVal == JFileChooser.APPROVE_OPTION) {
    			 File dir = chooser.getSelectedFile();
			       
    			 // look is the client is correct
    			 boolean isClient = LoL.isClient(dir);
    			 
    			 if (isClient) {
    				 // create new client
    				 LoLClient newC = clientsManager.createClient(LoLResources.inputFile.getAbsolutePath(), dir.getAbsolutePath(), LoL.found.getAbsolutePath());
    				 clientsManager.setActiveClient(newC);
    				 config.addValue("general", "clients", clientsManager.getClientCount());
    				 config.addValue("general", "activeClient", newC.getClientId());
    				 clientsManager.addClientToIni(newC, config);
    				 config.save(configFile);
    				 
    				 // update combo
    				 combo.addItem(newC);
    				 combo.setSelectedIndex(combo.getItemCount()-1);
    				 delete.setEnabled(true);

    				 // TODO : Ddelete
    				 // save this new client
//    				 int nbClients = Integer.parseInt(config.getValue("general", "clients")) + 1;
//    				 config.addValue("general", "clients", nbClients);
//    				 String section = "client" + (nbClients-1);
//    				 config.addValue("general", "activeClient", section);
//    				 config.createSection(section);
//    				 config.addValue(section, "clientPath", dir.getAbsolutePath());
//    				 config.addValue(section, "clientDataPath", LoL.found.getAbsolutePath());
//    				 // TODO: check this !
//    				 config.addValue(section, "clientInputPath", LoLResources.inputFile.getAbsolutePath());
//    				 config.save(configFile);
//    				 
//    				 combo.addItem(dir.getAbsolutePath());
//    				 combo.setSelectedIndex(combo.getItemCount()-1);
//    				 delete.setEnabled(true);
    			 }
    			 else {
    				 Log.getInst().warning("No client found from: " + dir);
    				 JOptionPane.showMessageDialog(null, LocaleString.string("No client found $0$", dir.toString().replace("\\", "\\\\")),
   						 "Error", JOptionPane.ERROR_MESSAGE);
    			 }
    		 }
    	 }
    	 else if (e.getSource() == delete) {
    		 int index = combo.getSelectedIndex();
    		 if (index != -1) {
    			 // get client
    			 LoLClient c = (LoLClient)combo.getSelectedItem();
    			 // confirmation box
    			 String cname = new File(c.getClientPath()).getName();
    			 int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Delete client $0$", cname),
						LocaleString.string("Delete client"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    			 // if ok, delete client
	    		 if (res == JOptionPane.OK_OPTION) {
	    			 deleteClient(index);
	    		 }
    		 }
    	 }
    	 else if (e.getSource() == save) {
    		 LoLCustomBuildsManager.getInst().saveBuilds();
    		 JOptionPane.showMessageDialog(null, LocaleString.string("Save confirmation"),
					 LocaleString.string("Save builds"), JOptionPane.INFORMATION_MESSAGE);
    	 }
    	 else if (e.getSource() == exit) {
    		 closeWindow();
    	 }
    	 else if (e.getSource() == createBuild) {
    		 createBuild();
    	 }
    	 else if (e.getSource() == combo) {
    		 // load
    		 LoLClient c = null;
    		// empty combo
    		 if (combo.getSelectedItem() == null) {
    			 // trick for when just deleted a client
    			 c = clientsManager.getClientAt(combo.getSelectedIndex()-1);
    			 if (c == null) {
 					// no client, reset the ui
 					resetUI();
 					return;
    			 }
    		 }
    		 c = (LoLClient)combo.getSelectedItem();
//    		 // load
//    		 IniFile.IniSection s = config.getSection("client" + combo.getSelectedIndex());
//    		 // empty combo
//    		 if (s == null) {
//    			 // trick for when just deleted a client
//    			 s = config.getSection("client" + (combo.getSelectedIndex()-1));
//				 if (s == null) {
//					// reset the ui
//					resetUI();
//					return;
//				 }
//			}
			
			// enable items
			save.setEnabled(true);
			combo.setEnabled(true);
			clientMenu.setEnabled(true);
			// only if at least 2 clients
			if (clientsManager.getClientCount() > 1)
				synch.setEnabled(true);
			
			// check for changes in files path
			int changes = LoL.updateClient(c);
			// if client not existing anymore
			if (changes == 1) {
				// TODO: what to do when clientPath doesnt exist ? remove it from the file ? autre ?
				// ask if we should delete it
				int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Client not existing $0$", c.getClientPath().replace("\\", "\\\\")),
	    				LocaleString.string("Client not existing"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				// delete client
		    	if (res == JOptionPane.OK_OPTION) {
		    		deleteClient(combo.getSelectedIndex());
		    	}
		    	else {
		    		// TODO: check that the button are enabled if select a working client after selecting a bad one
		    		// disable everything
					resetUI();
					// except the combo and the delete button
					delete.setEnabled(true);
					combo.setEnabled(true);
		    	}
				
			}
			// if changes, we update both dataPath and propPath
			else if (changes == 2) {
				c.setClientDataPath(LoL.found.getAbsolutePath());
			}
			
			// load the client data
			Log.getInst().info("Loading data from client \"" + combo.getSelectedItem() + "\"");
			LoL.load(c);
			buildsManager.init();
	    	tableModel.updateData(null);
	    	
	    	// load smartcasts and color champs icons
	    	String inputFile = c.getClientInputPath();
	    	// if no input file, try to find it from lol client path
	    	if (inputFile == null) {
	    		File fi = LoLResources.Finder.isLoLFolder(new File(c.getClientDataPath()));
	    		if (fi != null) {
	    			inputFile = fi.getAbsolutePath();
	    			c.setClientInputPath(inputFile);
	    		}
	    	}
	    	smartcastSwitcher.load(inputFile);
	    	colorSmartCastedChampions();
	    	
	    	// set as active client in conf file
	    	config.addValue("general", "activeClient", c.getClientId());
	    	// save config file
	    	clientsManager.addClientToIni(c, config);
			config.save(new File(configFile));
		}
		else if (e.getSource() == modeSel) {
			// change the game mode
			tableModel.setMode((GameMode)modeSel.getSelectedItem());
		}
     }
     
     
    public void colorSmartCastedChampions() {
    	sampleModel.refresh();
    }
     

	public void valueChanged(ListSelectionEvent e) {
		if (combo.getSelectedIndex() == -1) return;
		if (e.getValueIsAdjusting() == false) {
			createBuild.setEnabled((list.getSelectedIndex() != -1));
	    }
	}

	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		if (e.getSource() == search) {
			ArrayList<String>res = champList;
			sampleModel.clear();
			for (int i=0; i<res.size(); i++) {
				if (search.getText().length() == 0)
					sampleModel.addElement(res.get(i));
				else if (res.get(i).toLowerCase().contains(search.getText().toLowerCase())) {
					sampleModel.addElement(res.get(i));
				}
			}
		}
		else if (e.getSource() == filter) {
			tableModel.getManager().getFilter().setFilterValue(filter.getText());
    		tableModel.updateData(null);
    		int nb = tableModel.getFilterMatches();
    		if (!filter.getText().isEmpty()) {
    			filterLabel.setText((nb == 0 ? "No" : nb) + " build" + ((nb > 1) ? "s match" : " matches") + " '" + filter.getText() + "'");
    		}
    		else {
    			filterLabel.setText("");
    		}
		}
	}

	public void focusGained(FocusEvent e) {}
	public void focusLost(FocusEvent e) {
		if (e.getSource() == search) {
			if (search.getText().equals("")) {
				search.setText(filterText);
				search.setEnabled(false);
			}
		}
		else if (e.getSource() == filter) {
			if (filter.getText().equals("")) {
				filter.setText(filterItems);
				filter.setEnabled(false);
			}
		}
	}
	
	public JPopupMenu showSmartcastPopup(MouseEvent e, final String champ) {
		boolean isEnabled = smartcastSwitcher.isCurChampSmartCast(champ);
		boolean hasSc = smartcastSwitcher.hasSmartCasts(champ);
		
		JPopupMenu popup = new JPopupMenu();
		menu = popup;
		JMenuItem name = new JMenuItem(LocaleString.string("Smartcasts of $0$", champ));
		name.setEnabled(false);
		popup.add(name);
		popup.add(new JSeparator());
		
		JMenuItem open = new JMenuItem(LocaleString.string("Configure") + "...");
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new LoLSmartcastSwitcherWin(LoLWin.this, LoL, smartcastSwitcher, champ);
			}
		});
		popup.add(open);

		if (isEnabled) {
			JMenuItem disable = new JMenuItem(LocaleString.string("Disable smartcasts"));
			disable.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					smartcastSwitcher.setCurChampSmartcast(null);
					smartcastSwitcher.setSmartcast();
					sampleModel.refresh();
				}
			});
			popup.add(disable);
		}
		else {
			// if champ has smartcasts configured
			if (hasSc) {
				JMenuItem enable = new JMenuItem(LocaleString.string("Enable smartcasts"));
				enable.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						smartcastSwitcher.setCurChampSmartcast(champ);
						smartcastSwitcher.setSmartcast();
						sampleModel.refresh();
					}
				});
				if (!smartcastSwitcher.getStatus()) {
					enable.setEnabled(false);
				}
				popup.add(enable);
			}
		}
		
		// display popup
		popup.show(e.getComponent(), e.getX(), e.getY());
		return popup;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == search) {
			search.setEnabled(true);
			search.grabFocus();
			if (search.getText().equals(filterText))
				search.setText("");
		}
		else if (e.getSource() == filter) {
			filter.setEnabled(true);
			filter.grabFocus();
			if (filter.getText().equals(filterItems))
				filter.setText("");
		}
		else if (e.getSource() == list) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				// select the cham under mouse cursor in the list
				list.setSelectedIndex(list.locationToIndex(e.getPoint()));
		    	String champ = (String)list.getSelectedValue();
		    	if (champ == null) return;
		    	//create popup
		    	showSmartcastPopup(e, champ);
			}
			else {
				if (e.getClickCount() == 2 && combo.getSelectedIndex() != -1 && list.getSelectedIndex() != -1) {
					createBuild();
				}
			}
		}
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

}