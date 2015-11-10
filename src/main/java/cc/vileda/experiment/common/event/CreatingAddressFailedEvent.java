package cc.vileda.experiment.common.event;

import cc.vileda.resx.event.FailedEvent;

import static cc.vileda.experiment.common.Globals.CREATING_ADDRESS_FAILED_EVENT_ADDRESS;

public class CreatingAddressFailedEvent extends FailedEvent
{
	public CreatingAddressFailedEvent(String message) {
		super(CREATING_ADDRESS_FAILED_EVENT_ADDRESS, message);
	}

	public CreatingAddressFailedEvent() {
		super(CREATING_ADDRESS_FAILED_EVENT_ADDRESS, "");
	}
}
