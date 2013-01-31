import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class ChampionRenderer extends JLabel implements TableCellRenderer {
	
	public ChampionRenderer() {
		super();
		JLabel count = new JLabel("");
		count.setFont(count.getFont().deriveFont(Font.BOLD, 15f));
		count.setForeground(new Color(230,230,230));
		count.setBounds(34, 12, 150, 50);
 	   	add(count);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
													boolean isSelected, boolean isFocus,
													int row, int col)
	{
		String champ = (String)value;

		LoLTable t = (LoLTable)table;
		LoLTableModel m = (LoLTableModel)t.getModel();
		
		// update builds count
		List<LoLCustomBuild> builds = LoLCustomBuildsManager.getInst().getFilteredBuilds(champ, m.getMode());
   		JLabel count = (JLabel)this.getComponent(0);
   		int c = builds.size();
   		count.setText(String.valueOf(c));

 	   	ImageIcon icon = ResourcesManager.getInst().getIcon(LoLResources.getImagePath(LoLResources.Type.CHAMPION, champ, 1));
 	   	if (icon != null)
 	   		setIcon(icon);
 	   	setText(champ);
		setToolTipText(champ + " (" + c + " build" + ((c > 1) ? "s" : "") + ")");
		return this;
	}
}