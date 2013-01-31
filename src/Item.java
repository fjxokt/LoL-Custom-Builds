import java.io.Serializable;


public class Item implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;
	private int price;
	private String desc;
	private String filters;
	
	public Item(String id, String name, int price, String desc, String filters) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.desc = desc;
		this.filters = filters;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Item [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", price=");
		builder.append(price);
		builder.append(", desc=");
		builder.append(desc);
		builder.append(", filters=");
		builder.append(filters);
		builder.append("]");
		return builder.toString();
	}

}
