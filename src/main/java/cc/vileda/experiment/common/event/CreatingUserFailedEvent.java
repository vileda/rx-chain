package cc.vileda.experiment.common.event;

import cc.vileda.resx.event.FailedEvent;

import static cc.vileda.experiment.common.Globals.CREATING_USER_FAILED_EVENT_ADDRESS;

public class CreatingUserFailedEvent extends FailedEvent
{
	public CreatingUserFailedEvent(String message) {
		super(CREATING_USER_FAILED_EVENT_ADDRESS, message);
	}
	public CreatingUserFailedEvent() {
		super(CREATING_USER_FAILED_EVENT_ADDRESS, "");
	}
}
