import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BuildIconListRenderer extends DefaultListCellRenderer {
	
	public class BuildLabel extends JPanel {
		private JLabel buildName;
		private LoLCustomBuild build;
		public BuildLabel(LoLCustomBuild build) {
			setLayout(new FlowLayout(FlowLayout.LEFT));
			this.build = build;
			this.buildName = new JLabel(build.getBuildName());
			
			add(ItemsRendererEditor.getItemsPanel(build, null));
			add(buildName);
		}
		public String getBuildName() {
			return buildName.getText();
		}
		public LoLCustomBuild getBuild() {
			return build;
		}
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus){
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		// return our BuildLabel
		return (value == null) ? this : (Component)value;
	}
}
