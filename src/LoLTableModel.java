import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
class LoLTableModel extends AbstractTableModel {

	private String[] title;
	private LoLCustomBuildsManager manager = LoLCustomBuildsManager.getInst();
	private Map<GameMode, ArrayList<String>> champsMap;
	
	public LoLTableModel(String[] title){
		this.title = title;
		champsMap = new HashMap<GameMode, ArrayList<String>>();
		for (GameMode mode : GameMode.values()) {
			champsMap.put(mode, new ArrayList<String>());
		}
	}
	
	public void setMode(GameMode m) {
		manager.setMode(m);
		this.fireTableDataChanged();
	}
	
	public String getFilter() {
		return manager.getFilter().getFilterValue();
	}
	
	public int getFilterMatches() {
		return manager.getFilter().getCount();
	}
	
	public LoLCustomBuildsManager getManager() {
		return manager;
	}
	
	public GameMode getMode() {
		return manager.getMode();
	}
	
	public void updateData(String filter) {
		// nb of matches
		int count = 0;
		// for each mode
		for (GameMode mode : GameMode.values()) {
			// delete champ list
			champsMap.get(mode).clear();
			// we see how many match the filter (all for an empty filter)
			Map<String, List<LoLCustomBuild>> map = manager.getFilteredBuilds(mode);
			count += getFilterMatches();
			// if there are matches
			if (manager.getFilter().getCount() > 0) {
				List<String> okChamp = new ArrayList<String>();
				for (String champ : map.keySet()) {
					// if the champ has at least one build matching, add it to the list
					if (map.get(champ).size() > 0) {
						okChamp.add(champ);
					}
				}
				// update the list with the correct champs
				champsMap.get(mode).addAll(okChamp);
			}
		}
		// do not forget to update the count
		manager.getFilter().setCount(count);

		// rebuild the table
		fireTableDataChanged();
	}
	
	public void clear() {
		for (List<String> lst : champsMap.values()) {
			lst.clear();
		}
		fireTableDataChanged();
	}
	
	public void editBuild(LoLCustomBuild build) {
		// display it in our editor
		ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.CHAMPION, build.getChampName(), 0));
		ItemsRendererEditor ird = new ItemsRendererEditor(build.copyBuild(), true);
		int res = JOptionPane.showConfirmDialog(null, ird.getPanel(),
				LocaleString.string("Edit build"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
		String buildName = ird.getBuildName();
		// if a build name has been entered
		if (res == JOptionPane.OK_OPTION && buildName != null && !buildName.trim().isEmpty()) {
			// update build data
			ird.updateBuild();
			// update the edited build
			build.updateBuild(ird.getBuild(), false);
			// change the build name to the right one
			build.setBuildName(buildName);
			// update the table
			ArrayList<String> curList = currentList();
			// get champ index in list
			int index = curList.indexOf(build.getChampName());
			fireTableRowsUpdated(index, index);
		}
	}
	
	public void createBuild(String champ, GameMode mode) {
		// create default build for champ
		LoLCustomBuild build = LoLCustomBuildsManager.getDefaultBuild(champ, mode).copyBuild();
		build.setEnabled(true);
		
		// display it in our editor
		ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.CHAMPION, champ, 0));
		ItemsRendererEditor ird = new ItemsRendererEditor(build, false);
		int res = JOptionPane.showConfirmDialog(null, ird.getPanel(),
				LocaleString.string("Create new build"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
		String buildName = ird.getBuildName();
		// if a build name has been entered
		if (res == JOptionPane.OK_OPTION && buildName != null && !buildName.trim().isEmpty()) {
			// if the new build is from a global build
			if (ird.isGlobalBuild()) {
				boolean state = build.isEnabled();
				build.updateBuild(ird.getGlobalBuild(), false);
				build.setEnabled(state);
			}
			else {
				// update build data
				ird.updateBuild();
				// add it to global build if wanted
				if (ird.addToGlobalBuilds()) {
					LoLCustomBuildsManager.addGlobalBuild(build);
				}
			}
			// add build
			LoLCustomBuildsManager.getInst().addBuild(build);
			// make the build the active one
			if (build.isEnabled()) LoLCustomBuildsManager.getInst().enableActiveBuild(build);
			// update the table
			ArrayList<String> curList = currentList();
			// inserer le new champ a la bonne position dans la currentList() puis lancer le fireXXX
			if (curList.indexOf(champ) == -1) {
				int i, val = -1;
				for (i=0; i<curList.size(); i++) {
					if (curList.get(i).compareTo(champ) > 0) {
						val = i; break;
					}
				}
				if (val == -1) val = i;
				curList.add(val, champ);
				fireTableRowsInserted(val, val);
			}
		}
	}

	public void removeBuild(String champ, GameMode mode, String buildName) {
		ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.CHAMPION, champ, 0));
		JLabel msg = new JLabel(LocaleString.string("delete build label",
				new String[]{champ, manager.getMode().toString(), buildName}));
		int retour = JOptionPane.showConfirmDialog(null, msg, LocaleString.string("Deleting build"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
		
		if (retour != JOptionPane.OK_OPTION) {
			return;
		}
		
		int n = manager.removeBuild(champ, mode, buildName);
		ArrayList<String> curList = currentList();
		int row = curList.indexOf(champ);
		if (n == 0) {
			curList.remove(champ);
			fireTableRowsDeleted(row, row);
		}
		else {
			fireTableRowsUpdated(row, row);
		}
	}
	
	public ArrayList<String> currentList() {
		return currentList(getMode());
	}
	public ArrayList<String> currentList(GameMode mode) {
		return champsMap.get(mode);
	}
	
	// number of columns
	public int getColumnCount() {
		return title.length;
	}
	
	// return number of lines
	public int getRowCount() {
		return currentList().size();
	}
	
	public boolean isCellEditable(int row, int col) {
		return (col > 0);
	}
	
	// return value at coordinates row col
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return currentList().get(row);
		case 1:
			List<LoLCustomBuild> builds = manager.getBuilds(currentList().get(row), getMode());
			LoLCustomBuild fb = null;
			for (LoLCustomBuild b : builds) {
				if (manager.buildMatchFilter(b, manager.getFilter().getFilterValue())) {
					if (b.isEnabled()) {
						return b.getBuildName();
					}
					fb = b;
				}
			}
			
			// no filter and no build, means display default build
			if (manager.getFilter().getFilterValue() == null) return "Default";
			else {
				if (fb != null) {
					return fb.getBuildName();
				}
				// this happens when found builds for specific filter text
				// but then replace this item with another one
				// it causes an exception but its okay
				updateData(getFilter());
			}
		case 2:
			return new Object[]{currentList().get(row), getValueAt(row, 1)} ;
		case 3:
			return "";
		}
		return null;
	}
	
	// return column col name
	public String getColumnName(int col) {
		  return this.title[col];
	}
	
}