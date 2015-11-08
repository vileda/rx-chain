package cc.vileda.experiment.common.event;

public class SourcedEvent {
	private final String address;

	public SourcedEvent(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
