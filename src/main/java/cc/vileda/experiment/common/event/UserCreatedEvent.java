package cc.vileda.experiment.common.event;

import static cc.vileda.experiment.common.Globals.USER_CREATED_EVENT_ADDRESS;

public class UserCreatedEvent extends SourcedEvent {
	public UserCreatedEvent() {
		super(USER_CREATED_EVENT_ADDRESS);
	}
}
