import java.io.Serializable;


public class BuildItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private Item item;
	private int count;
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public BuildItem(Item item, int count) {
		this.item = item;
		this.count = count;
	}
	public BuildItem(String item, int count) {
		this.item = LoLCustomBuildsManager.getItem(item);
		this.count = count;
	}
	public String getId() {
		return item.getId();
	}
	public Item getItemData() {
		return item;
	}
}