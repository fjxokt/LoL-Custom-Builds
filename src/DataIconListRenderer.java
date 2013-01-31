import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

@SuppressWarnings("serial")
public class DataIconListRenderer extends DefaultListCellRenderer {
	
	LoLResources ref;
	LoLResources.Type type;
	LoLSmartcastSwitcher smartcastSwitcher;
	
	public DataIconListRenderer(LoLResources lol, LoLResources.Type t, LoLSmartcastSwitcher sw) {
		ref = lol;
		type = t;
		smartcastSwitcher = sw;
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {		
		String c = null;
		if (type == LoLResources.Type.ITEM) {
			c = ((Item)value).getId();
		}
		else {
			c = (String)value;
		}
		CustomIcon cicon = ResourcesManager.getInst().getCustomIcon(c);
		if (cicon == null) {
			int size = (type == LoLResources.Type.ITEM) ? 64 : LoLResources.IMAGE_NORMAL_SIZE;
			cicon = ResourcesManager.getInst().setCustomIcon(c, new CustomIcon(LoLResources.getImagePath(type, c), size));
		}
		cicon.setSelected(isSelected);
		
		if (type == LoLResources.Type.ITEM) {
			Item item = (Item)value;
			cicon.setToolTipText(LoLCustomBuildsManager.createToolTip(item, null));
		}
		else {
			cicon.setSmartcastState(smartcastSwitcher.getChampState(c));
			String smartc = smartcastSwitcher.isCurChampSmartCast(c) ? " [Smartcast enabled]" :
				smartcastSwitcher.hasSmartCasts(c) ? " [Smartcast configured]" : "";
			cicon.setToolTipText(c + smartc);
		}

		return cicon;
	}
}
