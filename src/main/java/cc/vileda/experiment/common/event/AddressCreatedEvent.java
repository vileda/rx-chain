package cc.vileda.experiment.common.event;

import cc.vileda.resx.event.SourcedEvent;
import lombok.Getter;

import static cc.vileda.experiment.common.Globals.ADDRESS_CREATED_EVENT_ADDRESS;

@Getter
public class AddressCreatedEvent extends SourcedEvent
{
	public AddressCreatedEvent() {
		super(ADDRESS_CREATED_EVENT_ADDRESS, null);
	}

	public AddressCreatedEvent(String id) {
		super(ADDRESS_CREATED_EVENT_ADDRESS, id);
	}
}
