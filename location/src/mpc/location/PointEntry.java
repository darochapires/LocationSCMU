package mpc.location;

public class PointEntry {

	private int id;
	private String point_name;

	public PointEntry(int id, String name) {
		this.id = id;
		this.point_name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return point_name;
	}

	public void setName(String name) {
		this.point_name = name;
	}

}