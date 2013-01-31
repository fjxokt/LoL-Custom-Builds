import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


public class ItemsRendererEditor  extends AbstractCellEditor 
								   implements TableCellRenderer, TableCellEditor, ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JPanel panel;
	private LoLCustomBuild build;
	private boolean isEditing;
	private JCheckBox cb;
	private JPanel catPan;
	private JScrollPane scroll;
	private JTextField buildName;
	private JLabel buildCost;
	// global builds
	private ButtonGroup group;
	private JRadioButton rb1;
	private JRadioButton rb2;
	private JComboBox cbt;
	private JCheckBox cb2;
	
	private class CategoryPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JButton delCat;
		private JButton addCat;
		private JPanel itemsPan;
		private JTextField catField;
		private List<BuildItem> items;
		private List<DnDLabel> list = new ArrayList<DnDLabel>();
		private ItemsRendererEditor src;
		private LoLCustomBuild build;
		public CategoryPanel(ItemsRendererEditor src, LoLCustomBuild build, String name) {
			// main panel
			super();
			setName(name);
			this.build = build;
			this.src = src;
			this.items = build.addCategory(name);
			BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(layout);
			setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 0));
			// panel with cat name and button to delete cat
			JPanel catPan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			// category name
			catField = new JTextField(name);
			catField.setPreferredSize(new Dimension(230,25));
			catPan.add(catField);
			// remove button
			delCat = new JButton(ResourcesManager.getInst().getIcon("remove.png", true));
	        delCat.setPreferredSize(new Dimension(24,24));
	        delCat.addActionListener(src);
	        delCat.setName(name);
	        delCat.setActionCommand("del");
	        delCat.setToolTipText(LocaleString.string("Click remove category"));
			catPan.add(delCat);
			// add button
			addCat = new JButton(ResourcesManager.getInst().getIcon("add.png", true));
	        addCat.setPreferredSize(new Dimension(24,24));
	        addCat.addActionListener(src);
	        addCat.setName(name);
	        addCat.setActionCommand("add");
	        addCat.setToolTipText(LocaleString.string("Click add category"));
			catPan.add(addCat);
			// add all
			add(catPan);

			// items panel
			JPanel lstPan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			itemsPan = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			int i = 0;
			for (BuildItem item : items) {
				// TODO: name and list in DnDLabel constructor
				DnDLabel it = new DnDLabel(list, item.getCount());
				list.add(it);
				it.setName("" + i);
				it.setBuild(items);
        		it.setHorizontalAlignment(SwingConstants.CENTER);
				ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.ITEM, item.getId(), 1));
	        	// icon loading failed, load empty one
	        	if (icon.getImageLoadStatus() == MediaTracker.ERRORED) {
	        		icon = new ImageIcon();
	        		it.setText("?");
	    			it.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
	        	}
				it.setIcon(icon);
				it.setPreferredSize(new Dimension(LoLResources.IMAGE_THUMB_SIZE, LoLResources.IMAGE_THUMB_SIZE));
				// CAREFUL: this create a mouselistener ! good to know...
				it.setToolTipText(LoLCustomBuildsManager.createToolTip(item, LocaleString.string("Right click delete")));
				it.addMouseListener(new LoLItemListener(i++));
				
				itemsPan.add(it);
			}
			lstPan.add(itemsPan);
			
			// extra item to create new one
			JLabel newItem = new JLabel("+", SwingConstants.CENTER);
			newItem.setPreferredSize(new Dimension(LoLResources.IMAGE_THUMB_SIZE, LoLResources.IMAGE_THUMB_SIZE));
			newItem.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
			newItem.setToolTipText(LocaleString.string("Click add new item"));
			newItem.addMouseListener(new LoLItemListener(100));
			lstPan.add(newItem);
			
			add(lstPan);
			// set size
			setMaximumSize(new Dimension(getMaximumSize().width, 80));
			setPreferredSize(new Dimension(getPreferredSize().width, 80));
			setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		
		public String getCatField() {
			return catField.getText().trim();
		}
		
		public DnDLabel createItemLabel(int index, BuildItem item) {
			DnDLabel it = new DnDLabel(list, item.getCount());
			it.setName("" + index);
			it.setBuild(items);
			ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.ITEM, item.getId(), 1));
			it.setIcon(icon);
			it.setPreferredSize(new Dimension(LoLResources.IMAGE_THUMB_SIZE,LoLResources.IMAGE_THUMB_SIZE));
			// CAREFUL: this create a mouselistener ! good to know...
			it.setToolTipText(LoLCustomBuildsManager.createToolTip(item, null));
			it.addMouseListener(new LoLItemListener(index));
			return it;
		}
		
		public void addItem(String newItem, int count) {
			BuildItem it = new BuildItem(newItem, count);
			DnDLabel newItemLb = createItemLabel(list.size(), it);

			itemsPan.add(newItemLb);
			list.add(newItemLb);
			items.add(it);
			itemsPan.revalidate();
			itemsPan.repaint();
			
	 	   	// update gui cost label
			src.updateCost();
		}
		
		public void changeItem(int id, String newItem, int count) {
			// create new item
			BuildItem buildItem = new BuildItem(newItem, count);
			DnDLabel item = (DnDLabel)itemsPan.getComponent(id);
			ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.ITEM, newItem, 1));
	 	   	if (icon != null)
	 	   		item.setIcon(icon);
	 	   	item.setToolTipText(LoLCustomBuildsManager.createToolTip(buildItem, null));
	 	   	item.setText("");
	 	   	JLabel countLb = (JLabel)item.getComponent(0);
	 	   	countLb.setText("" + count);
	 	   	
	 	   	// create new item
	 	   	items.remove(id);
	 	   	items.add(id, buildItem);
	 	   	
	 	   	// update gui cost label
			src.updateCost();
		}
		
		public void removeItem(int id) {
			// if only the "add item" label, stop here
			if (id == 100) return;
			// just in case...
			if (id >= itemsPan.getComponentCount()) return;
			
			// get item
			BuildItem item = items.get(id);
			// get label
			DnDLabel lab = (DnDLabel)itemsPan.getComponent(id);
						
			// just update count and repaint if count > 1
			if (item.getCount() > 1) {
				item.setCount(item.getCount()-1);
				lab.updateCount(item.getCount());
				lab.revalidate();
				lab.repaint();
			}
			// else we remove the item
			else {
				itemsPan.remove(lab);
				items.remove(id);
				list.remove(id);
				for (int i=id; i<itemsPan.getComponentCount(); i++) {
					lab = (DnDLabel)itemsPan.getComponent(i);
					lab.setName("" + i);
					for (MouseListener ls : lab.getMouseListeners()) {
						if (ls instanceof LoLItemListener) {
							lab.removeMouseListener(ls);
							break;
						}
					}
					lab.addMouseListener(new LoLItemListener(i));
				}
				itemsPan.revalidate();
				itemsPan.repaint();
			}

	 	   	// update gui cost label
			src.updateCost();
		}
		
		public class LoLItemListener implements MouseListener {
			int id;
			public LoLItemListener(int i) {
				id = i;
				System.out.println("creating listener: " + i);
			}
			public void mouseClicked(MouseEvent e) {
				// change/add item
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (build.getBuildName().equals("Default")) return;
					ItemChooser ic = LoLCustomBuildsManager.getInst().getItemChooser(build.getChampName());
					ic.setVisible(true);
					if (ic.hasSelection()) {
						if (id == 100) {
							addItem(ic.getSelection(), ic.getSelectionCount());
						}
						else {
							changeItem(id, ic.getSelection(), ic.getSelectionCount());
						}
					}
				}
				// remove item
				else if (e.getButton() == MouseEvent.BUTTON3) {
					if (build.getBuildName().equals("Default")) return;
					if (id != 100) removeItem(id);
				}
			}
			public void mouseReleased(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent e) {
				if (((JLabel)e.getSource()).isEnabled())
					panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			public void mouseExited(MouseEvent arg0) {
				panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			public void mousePressed(MouseEvent arg0) {}
		}
		
	}
	
	public ItemsRendererEditor() {
		// create panel
		panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
	}
	
	public ItemsRendererEditor(LoLCustomBuild build, boolean editBuild) {
		this(build, editBuild, false);
	}
	
	public ItemsRendererEditor(LoLCustomBuild build, boolean editBuild, boolean isGlobal) {
		this.build = build;
		this.isEditing = editBuild;

		// create panel
		panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		
		// top pan
		JPanel topPan = new JPanel();
		topPan.setAlignmentX(Component.RIGHT_ALIGNMENT);
		BoxLayout topLayout = new BoxLayout(topPan, BoxLayout.Y_AXIS);
		topPan.setLayout(topLayout);
		// title
		if (!isEditing) {
		    JLabel l1 = new JLabel("<html><b>"+ LocaleString.string("Creating new build $0$ mode for $1$", 
		    		new String[]{LoLCustomBuildsManager.getInst().getMode().toString(), build.getChampName()}) +"</b></html>");
		    topPan.add(l1);
	    }
		
		topPan.add(Box.createVerticalStrut(10));
		
		group = new ButtonGroup();
	    
	    if (!isEditing) {
		    rb1 = new JRadioButton(LocaleString.string("Click on pictures to choose items"));
		    rb1.setAlignmentX(Component.LEFT_ALIGNMENT);
			group.add(rb1);
			rb1.addActionListener(new RadioListener(this));
			topPan.add(rb1);
	    }
	    else {
	    	JLabel clickPic = new JLabel(LocaleString.string("Click on pictures to choose items"));
	    	clickPic.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	topPan.add(clickPic);
	    }
	    
		topPan.add(Box.createVerticalStrut(6));
		
		JPanel namePan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		namePan.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel lb = new JLabel(LocaleString.string("Build name") + " :");
		namePan.add(lb);
		
		namePan.add(Box.createHorizontalStrut(5));
		
		buildName = new JTextField(editBuild ? build.getBuildName() :
			build.getBuildName() + " " + 
				(LoLCustomBuildsManager.getInst().getNumberOfBuilds(build.getChampName(), LoLCustomBuildsManager.getInst().getMode()) + 1));
		buildName.setPreferredSize(new Dimension(150, 25));
		namePan.add(buildName);
		
		namePan.add(Box.createHorizontalStrut(5));
		
		buildCost = new JLabel(ResourcesManager.getInst().getIcon("gold_coin_stacks.png", true));
		buildCost.setHorizontalTextPosition(SwingConstants.LEFT);
		namePan.add(buildCost);
		
		topPan.add(namePan);
		
		topPan.add(Box.createVerticalStrut(5));
				
		panel.add(topPan);
		
		// combo category
		catPan = new JPanel();
		BoxLayout catLayout = new BoxLayout(catPan, BoxLayout.Y_AXIS);
		catPan.setLayout(catLayout);
		for (String cat : build.getBuildMap().keySet()) {
			CategoryPanel p = new CategoryPanel(this, build, cat);
			catPan.add(p);
		}
		
		scroll = new JScrollPane(catPan);
		scroll.setBackground(panel.getBackground());
		TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200,200,200)),
																				LocaleString.string("Categories"));
		scroll.setBorder(b);
		int maxSize = build.getCategoryCount() > 5 ? 83 * 5 + 15 : 83 * build.getCategoryCount() + 15;
		scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width, maxSize));
		scroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		panel.add(scroll);
		
		if (!isEditing) {
			JPanel globWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
			globWrapper.setAlignmentX(Component.RIGHT_ALIGNMENT);

			JPanel globPan = new JPanel();
			BoxLayout globLayout = new BoxLayout(globPan, BoxLayout.Y_AXIS);
			globPan.setLayout(globLayout);
			
			globPan.add(Box.createVerticalStrut(5));
	
		    cb2 = new JCheckBox(LocaleString.string("Add global list"));
		    globPan.add(cb2);
			
		    globPan.add(Box.createVerticalStrut(10));
			
			rb2 = new JRadioButton(LocaleString.string("Use global builds"));
			rb2.setAlignmentX(Component.LEFT_ALIGNMENT);
			group.add(rb2);
			globPan.add(rb2);
			rb2.addActionListener(new RadioListener(this));
			
			// global builds combo
			cbt = new JComboBox();
	        UIManager.put("ComboBox.selectionBackground", UIManager.get("ComboBox.background"));
	        UIManager.put("ComboBox.selectionForeground", UIManager.get("ComboBox.foreground"));
	        cbt.setUI(new MetalComboBoxUI());
	        BuildIconListRenderer br = new BuildIconListRenderer();
	        cbt.setPreferredSize(new Dimension(400, 45));
	        cbt.setRenderer(br);
	        cbt.setAlignmentX(Component.LEFT_ALIGNMENT);
	        globPan.add(cbt);
	        
	        // fill the combo
	        Set<String> keys = LoLCustomBuildsManager.getGlobalBuilds().keySet();
	        for (String key : keys) {
	        	LoLCustomBuild bui = LoLCustomBuildsManager.getGlobalBuilds().get(key);
	        	if (LoLCustomBuildsManager.isBuildAllowed(build.getChampName(), LoLCustomBuildsManager.getInst().getMode(), bui)) {
	        		BuildIconListRenderer.BuildLabel buildLabel = br.new BuildLabel(bui);
	        		buildLabel.setToolTipText(bui.getBuildName());
	        		cbt.addItem(buildLabel);
	        	}
	        }
	        
	        if (cbt.getModel().getSize() == 0) {
	        	rb2.setEnabled(false);
	        	cbt.addItem(new JLabel("  " + LocaleString.string("No global build")));
	        }
	        
	        globWrapper.add(globPan);
	        panel.add(globWrapper);
	        
		}
		
		// checkbox
		panel.add(Box.createVerticalStrut(5));
	    
		JPanel checkPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
		checkPan.setAlignmentX(Component.RIGHT_ALIGNMENT);
	    cb = new JCheckBox(LocaleString.string("Make this build the active build"));
	    cb.setSelected(build.isEnabled());
	    if (!build.getChampName().equals("Global")) {
	    	checkPan.add(cb);
	    }
	    panel.add(checkPan);
	    	    
	    // set/unset this build as active one
	    cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox b = (JCheckBox)e.getSource();
				ItemsRendererEditor.this.build.setEnabled(b.isSelected());
			}
	    });
	    
	    // disable global build part
	    if (!isEditing) {
	    	changeStat(true);	    	
	    }
	    
 	   	// update gui cost label
	    updateCost();
	}
	
	public class RadioListener implements ActionListener {
		private ItemsRendererEditor base;
		public RadioListener(ItemsRendererEditor r) {
			base = r;
		}
		public void actionPerformed(ActionEvent e) {
			base.changeStat(e.getSource() == base.rb1);
		}
	}
	
	public void updateCost() {
		buildCost.setText(LocaleString.string("Cost") + " : " + build.getCost());
	}
	
	public boolean addToGlobalBuilds() {
		return cb2.isSelected();
	}
	
	public boolean isGlobalBuild() {
		return rb2.isSelected();
	}
	
	public LoLCustomBuild getGlobalBuild() {
		BuildIconListRenderer.BuildLabel l = ((BuildIconListRenderer.BuildLabel)cbt.getSelectedItem());
		return l.getBuild();
	}
	
	public void changeStat(boolean newBuild) {
		rb1.setSelected(newBuild);
		rb2.setSelected(!newBuild);
		cbt.setEnabled(!newBuild);
		cb2.setEnabled(newBuild);
		buildName.setEnabled(newBuild);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton src = (JButton)e.getSource();
			if (src.getActionCommand().equals("del")) {
				int catId = 0;
				for (; catId<catPan.getComponentCount(); catId++) {
					CategoryPanel cp = (CategoryPanel)catPan.getComponent(catId);
					if (src.getName().equals(cp.getName())) {
						break;
					}
				}
				catPan.remove(catId);
				build.removeCategory(src.getName());
				
				// if no more cat, create an empty one
				if (catPan.getComponentCount() == 0) {
					catPan.add(new CategoryPanel(this, build, "New category 1"));
				}
				
				panel.revalidate();
				panel.repaint();
				
		 	   	// update gui cost label
				updateCost();
			}
			else if (src.getActionCommand().equals("add")) {
				int catId = 0;
				for (; catId<catPan.getComponentCount(); catId++) {
					CategoryPanel cp = (CategoryPanel)catPan.getComponent(catId);
					if (src.getName().equals(cp.getName())) {
						break;
					}
				}

				catPan.add(new CategoryPanel(this, build, "New category " + (catPan.getComponentCount()+1)), catId+1);
				panel.revalidate();
				panel.repaint();
				catPan.scrollRectToVisible(new Rectangle(0, 80*(catId+1), 400, 80));
			}
		}
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public LoLCustomBuild getBuild() {
		return build;
	}
	
	// update the build with the infos from the window (category name, build name)
	public void updateBuild() {
		for (Component c : catPan.getComponents()) {
			CategoryPanel catPan = (CategoryPanel)c;
			build.renameCategory(catPan.getName(), catPan.getCatField());
		}
		build.setBuildName(getBuildName());
	}
	
	public String getBuildName() {
		return buildName.getText();
	}
	
	public boolean getBuildState() {
		return cb.isSelected();
	}
	
	public static JPanel getItemsPanel(LoLCustomBuild build, String name) {
		// test display all items in a row
		JPanel catPan = new JPanel(new FlowLayout(FlowLayout.CENTER,4,0));
		for (String cat : build.getBuildMap().keySet()) {
			JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
			for (BuildItem item : build.getBuildMap().get(cat)) {
				JLabel it = new JLabel();
				ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.ITEM, item.getId(), 2));
				it.setIcon(icon);
				it.setToolTipText(LoLCustomBuildsManager.createToolTip(item, LocaleString.string("Category") + ": " + cat));
				if (name != null) {
		 	   		it.setEnabled(!name.equals("Default"));
				}
		 	   	it.setPreferredSize(new Dimension(LoLResources.IMAGE_SMALL_SIZE, LoLResources.IMAGE_SMALL_SIZE));

				pan.add(it);
			}
			catPan.add(pan);
		}
		return catPan;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		
		Object[] vals = (Object[])value;
		String champ = (String)vals[0];
		String name = (String)vals[1];
		
		this.build = LoLCustomBuildsManager.getInst().getBuild(champ, LoLCustomBuildsManager.getInst().getMode(), name);
		catPan = ItemsRendererEditor.getItemsPanel(build, name);
		
		JScrollPane pan = new JScrollPane(catPan);
		pan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		return pan;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		return getTableCellRendererComponent(table, value, isSelected, true, row, column);
	}

	public Object getCellEditorValue() {
		return null;
	}

}
