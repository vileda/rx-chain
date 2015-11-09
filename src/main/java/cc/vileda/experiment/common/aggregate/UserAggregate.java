package cc.vileda.experiment.common.aggregate;

import cc.vileda.experiment.common.event.UserCreatedEvent;
import cc.vileda.experiment.common.event.UserEmailChangedEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAggregate extends Aggregate
{
	private String id;
	private String name;
	private String email;

	public UserAggregate()
	{
	}

	public void on(UserCreatedEvent event)
	{
		id = event.getId();
		name = event.getName();
		email = event.getEmail();
	}

	public void on(UserEmailChangedEvent event)
	{
		email = event.getEmail();
	}
}
