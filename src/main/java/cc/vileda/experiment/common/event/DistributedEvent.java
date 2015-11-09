package cc.vileda.experiment.common.event;

public class DistributedEvent
{
	private final String address;

	public DistributedEvent(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
