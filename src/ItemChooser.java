import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class ItemChooser extends JDialog implements ActionListener, KeyListener, MouseListener {
	 private JTextField search;
	 private JSpinner spinner;
	 private int itemCount = 1;
     private JList list;
     private DefaultListModel sampleModel;
     private List<Item> lst;
     private String selection;
     private ArrayList<String> filters;
     private JButton clear;
     private JButton close;
     private JPanel checks;
     private String filterChamp;
     private GameMode filterMode;
     
	 public ItemChooser(LoLResources lol) {
		 this.setSize(490, 534);
		 this.setLocationRelativeTo(null);
		 this.setResizable(false);
         this.setModal(true);
         this.setTitle("Item Chooser");
		 JPanel pan = new JPanel();
		 
		 pan.add(new JLabel(LocaleString.string("Filter") + " : "));
		 
		 search = new JTextField("", 7);
		 search.addKeyListener(this);
		 pan.add(search);
		 
		 pan.add(new JLabel(LocaleString.string("Quantity") + " : "));
		 
		 spinner = new JSpinner(new SpinnerNumberModel(1,1,8,1));
		 pan.add(spinner);

		 clear = new JButton(LocaleString.string("Clear"));
		 clear.addActionListener(this);
		 pan.add(clear);
		 
		 close = new JButton(LocaleString.string("Close"));
		 close.addActionListener(this);
		 pan.add(close);
		 
		 filters = new ArrayList<String>();
		 GridLayout grid = new GridLayout(0,3);
		 checks = new JPanel(grid);
		 checks.setBorder(BorderFactory.createTitledBorder(LocaleString.string("Categories")));
		 checks.setPreferredSize(new Dimension(475, 132));
		 
		 // sort by category name
         Map<String,String> mapf = new TreeMap<String,String>();
         for (Entry<String,String> entry: lol.filters.entrySet()) {
        	 mapf.put(entry.getValue(), entry.getKey());
         }
         // and add to the model
         for (Entry<String,String> e : mapf.entrySet()) {
			 JCheckBox j = new JCheckBox(e.getKey());
			 j.setName(e.getValue());
			 j.setToolTipText(e.getKey());
			 checks.add(j);
				 
			 j.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					JCheckBox c = (JCheckBox)e.getSource();
					String tag = c.getName();
					if (c.isSelected()) filters.add(tag);
					else filters.remove(tag);
					filterList();
				}
			 });
		 }
		 pan.add(checks);
		 
		 sampleModel = new DefaultListModel();
         list = new JList(sampleModel);
         list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         list.setVisibleRowCount(-1);
         list.addMouseListener(this);
         list.setCellRenderer(new DataIconListRenderer(lol, LoLResources.Type.ITEM, null));
         list.setBackground(Color.black);
         JScrollPane listPane = new JScrollPane(list);
         listPane.setPreferredSize(new Dimension(468, 324));
         pan.add(listPane);
         
         lst = new ArrayList<Item>(LoLCustomBuildsManager.getItems().values());
         Collections.sort(lst, new Comparator<Item>() {
			public int compare(Item o1, Item o2) {
				return o1.getName().compareTo(o2.getName());
			}
         });
         
         for (Item item : lst) {
        	 sampleModel.addElement(item);
         }
                  
         this.setContentPane(pan);
         
         this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				selection = "";
			}
			public void windowClosed(WindowEvent e) {}
			public void windowActivated(WindowEvent arg0) {}
		});
	 }
	 
	 public void filter(String champion, GameMode mode) {
		 filterChamp = champion;
		 filterMode = mode;
	 }
	 
	 public void filterList() {
		 sampleModel.clear();
		 boolean skip;
		 // for each item
			for (Item item : lst) {
				// check for items not allowed with this champ/mode
				if (!LoLCustomBuildsManager.isItemAllowed(filterChamp, filterMode, item)) {
					continue;
				}
				
				skip = false;
				// if we have categories filters
				if (filters.size() > 0) {
					for (String tag : filters) {
						// if the cur item is not part of one of the cat, skip it
						if (!item.getFilters().contains(tag)) {
							skip = true;
							break;
						}
					}
				}
				// skip this item
				if (skip) continue;
				
				// filter item using the filter textfield
				if (search.getText().length() == 0 || item.getName().toLowerCase().contains(search.getText().toLowerCase()))
					sampleModel.addElement(item);
			}
	 }
	 
	 public String getSelection() {
		 return selection;
	 }
	 
	 public int getSelectionCount() {
		 return itemCount;
	 }
	 
	 public boolean hasSelection() {
		 return selection != null && !selection.equals("");
	 }
	 
	 public void clearFilter() {
		 spinner.setValue(1);
		 search.setText("");
		 for (Component c : checks.getComponents())
			 ((JCheckBox)c).setSelected(false);
		filterList();
	 }

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			selection = "";
			this.setVisible(false);
		}
		else if (e.getSource() == clear) {
			clearFilter();
		}
	}

	public void keyPressed(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	public void keyReleased(KeyEvent e) {
		filterList();
	}

	public void mouseClicked(MouseEvent arg0) {
		if (list.getSelectedIndex() != -1) {
        	selection = ((Item)sampleModel.getElementAt(list.getSelectedIndex())).getId();
        	 try {
    			 itemCount = (Integer)spinner.getValue();
    			 if (itemCount < 1) itemCount = 1;
    		 } catch (Exception e) {
    			 itemCount = 1;
    		 }
        	this.setVisible(false);
        	clearFilter();
        }		
	}
	public void mouseEntered(MouseEvent e) {
		list.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	public void mouseExited(MouseEvent arg0) {
		list.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
 }