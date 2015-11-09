package cc.vileda.experiment.common.event;

import lombok.Getter;

import static cc.vileda.experiment.common.Globals.USER_CREATED_EVENT_ADDRESS;
import static cc.vileda.experiment.common.Globals.USER_EMAIL_CHANGED_EVENT_ADDRESS;

@Getter
public class UserEmailChangedEvent extends SourcedEvent {;
	private String email;

	public UserEmailChangedEvent() {
		super(USER_EMAIL_CHANGED_EVENT_ADDRESS, null);
	}

	public UserEmailChangedEvent(String id, String email) {
		super(USER_EMAIL_CHANGED_EVENT_ADDRESS, id);
		this.email = email;
	}
}
