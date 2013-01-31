import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class CustomIcon extends JPanel {
	private BufferedImage image;
	private int size;
	private boolean isSelected = false;
	int smartcastState = 0;

	public CustomIcon(String file, int size) {
		super();
		this.size = size;
		image = ResourcesManager.getInst().getImage(file);
		setPreferredSize(new Dimension(size, size));
	}
	
	public void setSelected(boolean state) {
		isSelected = state;
	}
	
	public void setSmartcastState(int state) {
		smartcastState = state;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g.drawImage(image, 0, 0, null);
		g2d.setStroke(new BasicStroke(8.0f));
		// smartcast for this icon
		if (smartcastState == 1) {
			Color co = new Color(0.7f, 0.7f, 0.7f, 0.45f);
			g.setColor(co);
			g2d.drawRect(2, 2, size-4, size-4);
		}
		// active smartcast
		else if (smartcastState == 2) {
			Color co = new Color(1.0f, 1.0f, 0.0f, 0.5f);
			g.setColor(co);
			g2d.drawRect(2, 2, size-4, size-4);
		}
		if (isSelected) {
			g2d.setStroke(new BasicStroke(0.0f));
			g.setColor(Color.gray);
			g.drawRect(0, 0, size-1, size-1);
		}
	}
}