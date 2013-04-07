package mpc.location;

public class APEntry {

	private int id;
	private String mac_address;
	private String network_name;
	private int point_id;
	private int strength;

	public APEntry(int id, String mac_address, String network_name, int point_id,
			int strength) {
		this.id = id;
		this.mac_address = mac_address;
		this.network_name = network_name;
		this.point_id = point_id;
		this.strength = strength;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.network_name = name;
	}

	public String getName() {
		return network_name;
	}

	public String getMac_address() {
		return mac_address;
	}

	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}

	public int getPointId() {
		return point_id;
	}

	public void setPointId(int point_id) {
		this.point_id = point_id;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}
}
