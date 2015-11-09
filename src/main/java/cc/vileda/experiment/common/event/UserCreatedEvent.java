package cc.vileda.experiment.common.event;

import static cc.vileda.experiment.common.Globals.USER_CREATED_EVENT_ADDRESS;

public class UserCreatedEvent extends SourcedEvent {
	private final String userId;

	public UserCreatedEvent(String userId) {
		super(USER_CREATED_EVENT_ADDRESS);
		this.userId = userId;
	}

	public String getUserId()
	{
		return userId;
	}
}
