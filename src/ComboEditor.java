import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;


@SuppressWarnings("serial")
class ComboEditor extends DefaultCellEditor {
	static JComboBox jcb = new JComboBox();
	LoLTable tableau;
	int line;
	
	public ComboEditor() {
		super(jcb);
		jcb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    if (e.getStateChange() != ItemEvent.SELECTED)
			    	return;
			    
		    	String oldBuild = (String)tableau.getValueAt(line, 1);
                String buildName = (String)jcb.getSelectedItem();
                if (oldBuild.equals(buildName))
                	return;
                
		    	String champ = (String)tableau.getValueAt(line, 0);
                LoLCustomBuildsManager manager = LoLCustomBuildsManager.getInst();
                manager.enableActiveBuild(champ, manager.getMode(), buildName);
                // update build items cell (no need to update the whole row)
                ((LoLTableModel)tableau.getModel()).fireTableCellUpdated(line, 2);
			}
		});
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		tableau = (LoLTable)table;
		line = row;
		
		LoLTable t = (LoLTable)table;
		LoLTableModel m = (LoLTableModel)t.getModel();
		String champ = m.currentList().get(row);
		
		jcb.removeAllItems();

		List<LoLCustomBuild> builds = LoLCustomBuildsManager.getInst().getBuilds(champ, m.getMode());
		String filter = m.getFilter();
		if (filter == null) {
			jcb.addItem("Default");
			for (LoLCustomBuild b : builds)
				jcb.addItem(b.getBuildName());
		}
		else {
			for (LoLCustomBuild b : builds) {
				if (LoLCustomBuildsManager.getInst().buildMatchFilter(b, filter)) {
					jcb.addItem(b.getBuildName());
				}
			}
		}
				
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
     }
}