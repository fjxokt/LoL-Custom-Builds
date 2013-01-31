import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class LoLSmartcastSwitcherWin extends JDialog 
									implements KeyListener, MouseListener, MouseMotionListener, WindowListener {
	
	private LoLWin win;
	private LoLResources resources;
	private LoLSmartcastSwitcher lolSwitcher;
	
	private ArrayList<JPanel> champPans;
	
	private JList menuList;
	private JTextField filter;
	private JCheckBox onlySmartcast;
	private JCheckBox showRangeIndicator;
	private JButton enable;
	private DefaultListModel sampleModel;
	
	class ConfigureKey extends JDialog {
		StringBuffer res;
		int spellPos;
		public ConfigureKey(StringBuffer sb) {
			res = sb;
			spellPos = Integer.parseInt(res.toString()) - 1;
			this.setSize(350, 100);
			this.setLocationRelativeTo(null);
			this.setResizable(false);
			this.setModal(true);
            this.setTitle(LocaleString.string("Configure key"));
			JPanel pan = new JPanel();
			pan.add(new JLabel(LocaleString.string("Press key $0$", res.toString())));
			res.setLength(0);
			JButton def = new JButton(LocaleString.string("Set default key"));
			def.setFocusable(false);
			def.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
					res.append(lolSwitcher.getDefaultKey(spellPos));
					ConfigureKey.this.dispose();
				}
			});
			pan.add(Box.createVerticalStrut(25));
			pan.add(def);
			this.setContentPane(pan);
			this.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {
					if ((e.getKeyChar() >= 'a' &&  e.getKeyChar() <= 'z') ||
							e.getKeyChar() >= 'A' &&  e.getKeyChar() <= 'Z' ||
							e.getKeyChar() >= '0' && e.getKeyChar() <= '9') {
						res.append(new String(new char[]{e.getKeyChar()}).toLowerCase());
					}
					ConfigureKey.this.dispose();
				}
				public void keyReleased(KeyEvent arg0) {}
				public void keyPressed(KeyEvent arg0) {}
			});
			this.setVisible(true);
		}
	}
	
	public LoLSmartcastSwitcherWin(LoLWin w, LoLResources res, LoLSmartcastSwitcher lswitcher) {
		this(w, res, lswitcher, null);
	}
	
	@SuppressWarnings("unchecked")
	public LoLSmartcastSwitcherWin(LoLWin w, LoLResources res, LoLSmartcastSwitcher lswitcher, String selChamp) {
		win = w;
		resources = res;
		lolSwitcher = lswitcher;
		
		// building the window
		this.setSize(340, 540);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setTitle("LoL SmartCast Switcher");
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(this);
	
		JPanel pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));

		JPanel span = new JPanel();
		JLabel status = new JLabel(LocaleString.string("Status smartcast") + " : ");
		span.add(status);
		pan.add(span);
		
		JPanel pbe = new JPanel();
		enable = new JButton();
		enable.setHorizontalTextPosition(SwingConstants.LEFT);
		enable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lolSwitcher.setCurChampSmartcast(null);
				updateButton(null);
			}
		});
		pbe.add(enable);
		pan.add(pbe);
		
		sampleModel = new DefaultListModel();
		menuList = new JList(sampleModel) {
		    // this method is called as the cursor moves within the list
		    public String getToolTipText(MouseEvent evt) {
		    	JLabel sel = findLabel(evt.getPoint());
		    	if (sel != null) {
		    		if (!sel.getName().equals("champ")) {
		    			String[] vals = sel.getToolTipText().split("#");
		    			int skillNb = Integer.parseInt(sel.getName());
		    			int skillType = lolSwitcher.getSkills(sel.getParent().getName()).getSkill(skillNb);
		    			String type = (skillType == LoLSmartcastSwitcher.LoLSkills.CAST) ? "" : (skillType == LoLSmartcastSwitcher.LoLSkills.SMARTCAST) ? " [Smartcast]" : " [Self+Smartcast]";
						return "<html><div style=\"width: 200px\"><b>"+vals[0]+type+"</b><br/>"+vals[1]+"</div></html>";
		    		}
		    		return sel.getToolTipText();
		    	}
		    	return null;
		    }

		};
		menuList.setBackground(Color.black);

		menuList.setCellRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof JPanel) {
					Component component = (Component) value;
					component.setForeground(Color.white);
					component.setBackground(Color.black);
					return component;
				}
				return null;
			}			
		});
		
		menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		menuList.setFixedCellHeight(LoLResources.IMAGE_NORMAL_SIZE);
		menuList.addMouseListener(this);
		menuList.addMouseMotionListener(this);

		// put our JList in a JScrollPane
		JScrollPane menuScrollPane = new JScrollPane(menuList);
		menuScrollPane.setMaximumSize(new Dimension(310, 350));
		menuScrollPane.setPreferredSize(new Dimension(310, 350));

		// sort champion list
		ArrayList<String>champList = (ArrayList<String>) resources.champList.clone();
		Collections.sort(champList);
		
		JPanel keys = new JPanel(new FlowLayout(FlowLayout.LEFT));
		keys.setPreferredSize(new Dimension(250, 30));
		JLabel empty = new JLabel();
		empty.setPreferredSize(new Dimension(75, 30));
		keys.add(empty);
		for (int i=0; i<LoLSmartcastSwitcher.keys.length(); i++) {
			String key = "" + LoLSmartcastSwitcher.keys.charAt(i);
			JLabel l = new JLabel("<html><div style=\"text-align: center; font-weight: bold;\">" + key.toUpperCase() + "</html>", JLabel.CENTER);
			l.setBorder(BorderFactory.createLineBorder(Color.darkGray, 2));
			l.setForeground(Color.gray);
			l.setPreferredSize(new Dimension(51, 30));
			l.setName(key);
			l.setToolTipText(LocaleString.string("Click to define key $0$", "" + (i+1)));
			l.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					JLabel label = (JLabel)e.getSource();
					int pos = LoLSmartcastSwitcher.keys.indexOf(label.getName());
					StringBuffer r = new StringBuffer("" + (pos+1));
					new ConfigureKey(r);
					String res = r.toString();
					if (!res.isEmpty() && !LoLSmartcastSwitcher.keys.contains(res)) {
						lolSwitcher.replaceKey(pos, res.charAt(0));
						label.setText("<html><div style=\"text-align: center; font-weight: bold;\">" + res.toUpperCase() + "</html>");
						label.setName(res);
					}
				}
				public void mouseEntered(MouseEvent e) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
				public void mouseExited(MouseEvent e) {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent arg0) {}
				
			});
			keys.add(l);
		}
		pan.add(keys);
		
		champPans = new ArrayList<JPanel>();
		for (int i=0; i<champList.size(); i++) {
			String champ = champList.get(i);
			String[] skills = resources.champSkills.get(resources.champList.indexOf(champ)).split("§");
			
			JPanel champPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
			champPan.setPreferredSize(new Dimension(250, LoLResources.IMAGE_NORMAL_SIZE));
			champPan.setName(champ);
			JLabel c = new JLabel(ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.CHAMPION, champ)), JLabel.LEFT);
			c.setPreferredSize(new Dimension(LoLResources.IMAGE_NORMAL_SIZE, LoLResources.IMAGE_NORMAL_SIZE));
			c.setToolTipText(champ);
			c.setName("champ");
			champPan.add(c);
			
			for (int j=0; j<skills.length; j++) {
				String[] vals = skills[j].split("#");
				String skill = vals[0];
				c = new JLabel(ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.SKILL, skill)));
				c.setPreferredSize(new Dimension(50, 50));
				c.setToolTipText(skills[j]);
				c.setName("" + j);
				if (lolSwitcher.getChampSkills(champ) != null) {
					if (lolSwitcher.getChampSkills(champ).isSmartcast(j)) {
						c.setBorder(BorderFactory.createLineBorder(new Color(204,204,85), 3));
					}
					else if (lolSwitcher.getChampSkills(champ).isSelfSmartcast(j)) {
						c.setBorder(BorderFactory.createLineBorder(Color.red, 3));
					}
					else {
						c.setBorder(BorderFactory.createLineBorder(Color.darkGray));
					}
				}
				else {
					c.setBorder(BorderFactory.createLineBorder(Color.darkGray));
				}
				champPan.setBounds(0, LoLResources.IMAGE_NORMAL_SIZE*i, 250, LoLResources.IMAGE_NORMAL_SIZE);
				champPan.add(c);	
			}	
			sampleModel.addElement(champPan);
			champPans.add(champPan);
		}
		
		pan.add(menuScrollPane);
		
		JPanel fpan = new JPanel();
		fpan.add(new JLabel(LocaleString.string("Filter") + " : "));
		filter = new JTextField();
		filter.setMaximumSize(new Dimension(255, 30));
		filter.setPreferredSize(new Dimension(255, 30));
		filter.addKeyListener(this);
		fpan.add(filter);
		pan.add(fpan);
		
		JPanel pp = new JPanel();
		JPanel pn = new JPanel();
		pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
		
		showRangeIndicator = new JCheckBox(LocaleString.string("Enable range indicator"));
		Boolean bool = lolSwitcher.getShowRangeIndicator();
		if (bool != null) {
			showRangeIndicator.setSelected(bool);
		} else {
			showRangeIndicator.setEnabled(false);
			showRangeIndicator.setToolTipText(LocaleString.string("Missing file $0$", "game.cfg"));
		}
		showRangeIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);
		pn.add(showRangeIndicator);
		
		onlySmartcast = new JCheckBox(LocaleString.string("Only champ smartcast"));
		onlySmartcast.setToolTipText(LocaleString.string("Only champ smartcast"));
		onlySmartcast.setAlignmentX(Component.LEFT_ALIGNMENT);
		onlySmartcast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				filter();
			}
		});
		pn.add(onlySmartcast);
		pp.add(pn);
		pn.setPreferredSize(new Dimension(320, pn.getPreferredSize().height));
		pan.add(pp);
		
		JPanel pb = new JPanel();
		JButton save = new JButton(LocaleString.string("Save"));
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lolSwitcher.setSmartcast();
				lolSwitcher.saveSmartCasts();
				lolSwitcher.setShowRangeIndicator(showRangeIndicator.isSelected());
				lolSwitcher.saveRangeIndicator();
				JOptionPane.showMessageDialog(null, LocaleString.string("Saving smartcasts text"),
						 LocaleString.string("Saving smartcasts"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		JButton close = new JButton(LocaleString.string("Close"));
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoLSmartcastSwitcherWin.this.dispose();
			}
		});
		pb.add(save);
		pb.add(close);
		pan.add(pb);
		
		// update switcher status
		updateButton(lolSwitcher.getCurChampSmartcast());

		// set the active item visible
		if (selChamp != null) {
			menuList.ensureIndexIsVisible(champList.indexOf(selChamp));
		}
		else if (lolSwitcher.getCurChampSmartcast() != null) {
			menuList.ensureIndexIsVisible(champList.indexOf(lolSwitcher.getCurChampSmartcast()));
		}
	
		this.setContentPane(pan);
		this.setVisible(true);
	}
	
	private void updateButton(String champ) {
		if (champ != null && !champ.isEmpty()) {
			enable.setText(LocaleString.string("Enabled for $0$", champ));
			enable.setIcon(ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.CHAMPION, champ, 1)));
			enable.setEnabled(true);
			enable.setToolTipText(LocaleString.string("Click to disable"));
		}
		else {
			if (lolSwitcher.getStatus()) {
				enable.setText(LocaleString.string("Disabled"));
				enable.setToolTipText(LocaleString.string("Select to enable"));
			}
			else {
				enable.setToolTipText(LocaleString.string("Missing file $0$", "input.ini"));
				enable.setText(LocaleString.string("Cant enable"));
			}
			enable.setIcon(null);
			enable.setEnabled(false);
		}
	}
	
	private JLabel findLabel(Point p) {
		int index = menuList.locationToIndex(p);
		if (index > -1) {
	        JPanel item = (JPanel)menuList.getModel().getElementAt(index);
	        for (Component c : item.getComponents()) {
	        	Rectangle b = c.getBounds();
	        	b.y += (menuList.getFixedCellHeight()*index);
	        	if (b.contains(p)) {
	        		return (JLabel)c;
	        	}
	        }
		}
        return null;
	}

	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		filter();
	}
	
	private void filter() {
		sampleModel.clear();
		for (int i=0; i<champPans.size(); i++) {
			if (onlySmartcast.isSelected()) {
				LoLSmartcastSwitcher.LoLSkills s = lolSwitcher.getChampSkills(champPans.get(i).getName());
				if (s == null || (s != null && !s.hasSmartcasts())) {
					continue;
				}
			}
			if (filter.getText().length() == 0)
				sampleModel.addElement(champPans.get(i));
			else if (champPans.get(i).getName().toLowerCase().contains(filter.getText().toLowerCase())) {
				sampleModel.addElement(champPans.get(i));
			}
		}
	}

	public void mouseClicked(MouseEvent evt) {
		JLabel sel = findLabel(evt.getPoint());
		if (sel == null) return;
		
		String champ = sel.getParent().getName();
		
		// click on the champ picture
		if (sel.getName().equals("champ")) {
			if (lolSwitcher.getStatus() && lolSwitcher.hasSmartCasts(champ)) {
				if (lolSwitcher.getCurChampSmartcast().equals(champ)) {
					champ = null;
				}
				lolSwitcher.setCurChampSmartcast(champ);
				updateButton(champ);
			}
		}
		// click on skill picture
		else {
			LoLSmartcastSwitcher.LoLSkills skills = lolSwitcher.getSkills(champ);
			int pos = Integer.parseInt(sel.getName());
			// set to the next state (or p revious if right click)
			int next = (evt.getButton() == MouseEvent.BUTTON3) ? 2 : 1;
			int nextNum = (skills.getSkill(pos) + next) % 3;
			skills.setSkill(pos, nextNum);
			lolSwitcher.setChampSkills(champ, skills);
			if (nextNum == LoLSmartcastSwitcher.LoLSkills.SMARTCAST) {
				sel.setBorder(BorderFactory.createLineBorder(new Color(204,204,85), 3));
			}
			else if (nextNum == LoLSmartcastSwitcher.LoLSkills.SELFSMARTCAST) {
				sel.setBorder(BorderFactory.createLineBorder(Color.red, 3));
			}
			else {
				sel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
			}
			if (!skills.hasSmartcasts()) {
				lolSwitcher.removeChamp(champ);
				if (lolSwitcher.getCurChampSmartcast().equals(champ)) {
					lolSwitcher.setCurChampSmartcast(null);
					updateButton(null);
				}
			}
		}
		menuList.repaint();
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void mouseDragged(MouseEvent arg0) {}
	public void mouseMoved(MouseEvent evt) {
		JLabel l = findLabel(evt.getPoint());
		if (l != null) {
			if (l.getName().equals("champ")) {
				if (lolSwitcher.getStatus() && lolSwitcher.hasSmartCasts(l.getParent().getName())) {
					menuList.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
				else {
					menuList.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
			else {
				menuList.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		}
		else {
			menuList.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {
		win.colorSmartCastedChampions();
	}
	public void windowClosing(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}

}
