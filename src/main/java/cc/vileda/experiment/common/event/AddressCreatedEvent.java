package cc.vileda.experiment.common.event;

import lombok.Getter;

import static cc.vileda.experiment.common.Globals.ADDRESS_CREATED_EVENT_ADDRESS;

@Getter
public class AddressCreatedEvent extends SourcedEvent {
	private final String addressId;
	public AddressCreatedEvent(String addressId) {
		super(ADDRESS_CREATED_EVENT_ADDRESS);
		this.addressId = addressId;
	}
}
