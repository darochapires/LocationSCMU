package mpc.location;

public class APEntry {

	private long id;
	private long location_id;
	private int strength;
	
	private String mac_address;
	private String name;
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setLocationId(long location_id) {
		this.location_id = location_id;
	}
	
	public long getLocationId() {
		return location_id;
	}
	
	public void setStrength(int strength) {
		this.strength = strength;
	}
	
	public int getStrength() {
		return strength;
	}
	
	public void setMacAddress(String mac_address) {
		this.mac_address = mac_address;
	}
	
	public String getMacAddress() {
		return mac_address;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
