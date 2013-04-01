package mpc.location;

public class APEntry {

	private long id;
	private String mac_address;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setMacAddress(String mac_address) {
		this.mac_address = mac_address;
	}

	public String getMacAddress() {
		return mac_address;
	}

}
