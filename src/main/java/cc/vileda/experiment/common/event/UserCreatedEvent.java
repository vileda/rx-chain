package cc.vileda.experiment.common.event;

import cc.vileda.resx.event.SourcedEvent;
import lombok.Getter;

import static cc.vileda.experiment.common.Globals.USER_CREATED_EVENT_ADDRESS;

@Getter
public class UserCreatedEvent extends SourcedEvent
{;
	private String name;
	private String email;

	public UserCreatedEvent() {
		super(USER_CREATED_EVENT_ADDRESS, null);
	}

	public UserCreatedEvent(String id, String name, String email) {
		super(USER_CREATED_EVENT_ADDRESS, id);
		this.name = name;
		this.email = email;
	}
}
