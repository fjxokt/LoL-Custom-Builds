import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


@SuppressWarnings("serial")
public class LoLTable extends JTable {
	
	private class BuildClipboard {
		private LoLCustomBuild build;
		public void copyBuild(LoLCustomBuild build) { 
			this.build = build.copyBuild();
		}
		public void pasteBuild(LoLCustomBuild dest) { 
			dest.updateBuild(build, true);
		}
		public LoLCustomBuild getBuild() {
			return build;
		}
		public boolean isFull() {
			return build != null;
		}
	}
	
	private LoLTableModel model;
	private BuildClipboard buildCb = new BuildClipboard();
	
	public LoLTable(LoLTableModel mod) {
		super(mod);
		model = mod;
		setRowHeight(50);
        getColumnModel().getColumn(0).setPreferredWidth(130);         
        getColumnModel().getColumn(1).setPreferredWidth(160);         
        getColumnModel().getColumn(2).setPreferredWidth(310);         
        getColumnModel().getColumn(3).setPreferredWidth(18);
        getColumnModel().getColumn(3).setMaxWidth(18);
        getTableHeader().setReorderingAllowed(false);
        ((JLabel)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        // create all the different cells used by the table
        initCells();
        
        // allow the tooltip to show for the 'items list' cell
        addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent arg0) {}
			public void mouseMoved(MouseEvent e) {
				java.awt.Point p = e.getPoint();
		        int row = rowAtPoint(p);
		        int col = columnAtPoint(p);
		        if (col > 1) {
		        	// changes cell being edited only if mouse cursor moved to another cell
		        	if (getEditingColumn() != col || getEditingRow() != row) {
		       			editCellAt(row, col);
		        	}
		        }
			}
        });
        
        addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {
				// this 'trick' is used because of the scrolling that screws the jtable bounds and the cursor pos
				Rectangle rec = ((JTable)e.getSource()).getBounds();
				Point cursor = new Point(e.getX() + rec.x, e.getY() + rec.y);
				rec.x = rec.y = 0;
				// if not in the jtable cancel editing mode
				if (!rec.contains(cursor)) {
					editingCanceled(null);
				}
			}
			public void mousePressed(MouseEvent e) {}
	        // popup menu
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) return;
				int row = rowAtPoint(e.getPoint());
				String buildName = (String)getValueAt(row, 1);
				String champ = (String)getValueAt(row, 0);

				JMenuItem header = new JMenuItem("[" + buildName + " - " + model.getMode() + "]");
				header.setEnabled(false);
				
				JMenuItem edit = new JMenuItem(LocaleString.string("Edit build"));
				edit.setEnabled(!buildName.equals("Default"));
				edit.addActionListener(new PopupListener(row, 10));
				
				JMenuItem rename = new JMenuItem(LocaleString.string("Rename build"));
				rename.setEnabled(!buildName.equals("Default"));
				rename.addActionListener(new PopupListener(row, 1));
				
				JMenuItem delete = new JMenuItem(LocaleString.string("Delete this build"));
				delete.setEnabled(!buildName.equals("Default"));
				delete.addActionListener(new PopupListener(row, 2));
				
				JMenuItem create = new JMenuItem(LocaleString.string("Create new build"));
				create.addActionListener(new PopupListener(row, 3));
				
				JMenu menuGlob = createGlobalBuildsMenu(row);
				
				JMenuItem copy = new JMenuItem(LocaleString.string("Copy build items"));
				copy.addActionListener(new PopupListener(row, 4));

				JMenuItem paste = new JMenuItem(LocaleString.string("Paste build items"));
				paste.setEnabled(buildCb.isFull() && !buildName.equals("Default") 
						&& LoLCustomBuildsManager.isBuildAllowed(champ, model.getMode(), buildCb.getBuild()));
				paste.addActionListener(new PopupListener(row, 5));

				//Ajout du menu contextuel
				JPopupMenu menu = new JPopupMenu();
		         // have to do that otherwise popup menu isn't closed on LoLWin focus lost
				LoLWin.menu = menu;
				menu.add(header);
				menu.add(new JPopupMenu.Separator());
				menu.add(edit);
				menu.add(rename);
				menu.add(delete);
				menu.add(create);
				menu.add(new JPopupMenu.Separator());
				menu.add(menuGlob);
				menu.add(new JPopupMenu.Separator());
				menu.add(copy);
				menu.add(paste);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
        });
	}
	
	private JMenu createGlobalBuildsMenu(int row) {
		JMenu menuGlob = new JMenu(LocaleString.string("Global builds"));
		
		// add to global list menuitem
		JMenuItem addM = new JMenuItem(LocaleString.string("Add global list short"));
		addM.addActionListener(new PopupListener(row, 6));
		menuGlob.add(addM);
		
		String champ = (String)getValueAt(row, 0);
		GameMode mode = model.getManager().getMode();
		
		// get allowed global builds for this champ/mode
		List<LoLCustomBuild> globalBuilds = new ArrayList<LoLCustomBuild>();
		for (LoLCustomBuild b : LoLCustomBuildsManager.getGlobalBuilds().values()) {
			if (LoLCustomBuildsManager.isBuildAllowed(champ, mode, b)) {
				globalBuilds.add(b);
			}
		}
		
		if (!globalBuilds.isEmpty())
			menuGlob.add(new JSeparator());

		for (LoLCustomBuild bui : globalBuilds) {
			JPanel pan = ItemsRendererEditor.getItemsPanel(bui, null);
			int nbItems = bui.getItemsCount();
		
			JMenu it = new JMenu(bui.getBuildName());
			JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p2.add(pan);
			p2.add(new JLabel(bui.getBuildName()));
			it.setPreferredSize(new Dimension(it.getPreferredSize().width + 5 + LoLResources.IMAGE_SMALL_SIZE * nbItems + bui.getCategoryCount() * 4, 40));
			it.add("pan", p2);

			JMenuItem it1 = new JMenuItem(LocaleString.string("Global build use"));
			it1.setActionCommand(bui.getBuildName());
			it1.addActionListener(new PopupListener(row, 7));
			JMenuItem it3 = new JMenuItem(LocaleString.string("Edit build"));
			it3.setActionCommand(bui.getBuildName());
			it3.addActionListener(new PopupListener(row, 9));
			JMenuItem it2 = new JMenuItem(LocaleString.string("Global build remove"));
			it2.setActionCommand(bui.getBuildName());
			it2.addActionListener(new PopupListener(row, 8));
			
			it.add(it1);
			it.add(it3);
			it.add(it2);
			
			menuGlob.add(it);
		}
		return menuGlob;
	}
	
	class PopupListener implements ActionListener {
		int row;
		int id;
		public PopupListener(int row, int id) {
			this.row = row;
			this.id = id;
		}
		public void actionPerformed(ActionEvent e) {
			String champ = (String)getValueAt(row, 0);
			String buildName = (String)getValueAt(row, 1);
			GameMode mode = model.getManager().getMode();
			switch (id) {
			case 10:
				LoLCustomBuild b = LoLCustomBuildsManager.getInst().getBuild(champ, mode, buildName);
				model.editBuild(b);
				break;
			case 1:
				ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.CHAMPION, champ, 0));
				String newName = (String)JOptionPane.showInputDialog(null,
						LocaleString.string("Rename $0$ build $1$ to", new String[]{champ, buildName}),
						LocaleString.string("Rename build"), JOptionPane.INFORMATION_MESSAGE,
						icon, null, null);
				if (newName == null || newName.length() == 0 || newName.equals(buildName)) return;
				model.getManager().renameBuild(champ, mode, buildName, newName);
				model.fireTableCellUpdated(row, 1);
				break;
			case 2:
				model.removeBuild(champ, mode, buildName);
				break;
			case 3:
				model.createBuild(champ, mode);
				break;
			case 4:
				buildCb.copyBuild(model.getManager().getBuild(champ, mode, buildName));
				break;
			case 5:
				buildCb.pasteBuild(model.getManager().getBuild(champ, mode, buildName));
				model.fireTableCellUpdated(row, 2);
				break;
			case 6:
				LoLCustomBuildsManager.addGlobalBuild(model.getManager().getBuild(champ, mode, buildName));
				break;
			case 7:
				String buildN = ((JMenuItem)e.getSource()).getActionCommand();
				LoLCustomBuild build = LoLCustomBuildsManager.getGlobalBuilds().get(buildN).copyBuild();
				build.setChampName(champ);
				build.setMode(mode);
				LoLCustomBuildsManager.getInst().addBuild(build);
				LoLCustomBuildsManager.getInst().enableActiveBuild(build);
				model.fireTableRowsUpdated(row, row);
				break;
			case 8:
				String build2 = ((JMenuItem)e.getSource()).getActionCommand();
				int retour = JOptionPane.showConfirmDialog(null, LocaleString.string("Deleting global build $0$", new String[]{build2}),
						LocaleString.string("Deleting build"),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if (retour != JOptionPane.OK_OPTION)
					return;
				LoLCustomBuildsManager.getGlobalBuilds().remove(build2);
				LoLCustomBuildsManager.saveGlobalBuilds();
				break;
			case 9:
				String build3 = ((JMenuItem)e.getSource()).getActionCommand();
				LoLCustomBuild gbuild = LoLCustomBuildsManager.getGlobalBuilds().get(build3).copyBuild();
				gbuild.setChampName("Global");
				ItemsRendererEditor ird = new ItemsRendererEditor(gbuild, true);

				int res = JOptionPane.showConfirmDialog(null, ird.getPanel(),
						LocaleString.string("Edit build"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
				String buildNewName = ird.getBuildName();
				// if a build name has been entered
				if (res == JOptionPane.OK_OPTION && buildNewName != null && buildNewName.trim().length() != 0) {
					// different name, remove old build and update build name
					if (!gbuild.getBuildName().equals(buildNewName)) {
						LoLCustomBuildsManager.getGlobalBuilds().remove(gbuild.getBuildName());
					}
					ird.updateBuild();
					LoLCustomBuildsManager.getGlobalBuilds().put(buildNewName, gbuild);
					LoLCustomBuildsManager.saveGlobalBuilds();
				}
				break;
			}
			
		}
	}
	
	public void initCells() {
		// for renderer
		ResourcesManager.getInst().putCell(0, new ChampionRenderer());
		ResourcesManager.getInst().putCell(1, new ComboRenderer());
		ResourcesManager.getInst().putCell(2, new ItemsRendererEditor());
		ResourcesManager.getInst().putCell(3, new OptionsRendererEditor());
		// for editor (can't use same instance than renderer, don't know why)
		ResourcesManager.getInst().putCell(4, new ComboEditor());
		ResourcesManager.getInst().putCell(5, new ItemsRendererEditor());
		ResourcesManager.getInst().putCell(6, new OptionsRendererEditor());
	}
	
	public TableCellRenderer getCellRenderer(int row, int column) {
		switch (column) {
			case 0:
				return (ChampionRenderer)ResourcesManager.getInst().getCell(column);
			case 1:
				return (ComboRenderer)ResourcesManager.getInst().getCell(column);
			case 2:
				return (ItemsRendererEditor)ResourcesManager.getInst().getCell(column);
			case 3:
				return (OptionsRendererEditor)ResourcesManager.getInst().getCell(column);
		}
		return super.getCellRenderer(row, column);
	}
	
	public TableCellEditor getCellEditor(int row, int column) {
		switch (column) {
			case 1:
				return (ComboEditor)ResourcesManager.getInst().getCell(4);
			case 2:
				return (ItemsRendererEditor)ResourcesManager.getInst().getCell(5);
			case 3:
				return (OptionsRendererEditor)ResourcesManager.getInst().getCell(6);
		}
		return super.getCellEditor();
	}

}
