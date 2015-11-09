package cc.vileda.experiment.common.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourcedEvent extends DistributedEvent
{
	private String id;

	public SourcedEvent(String address, String id)
	{
		super(address);
		this.id = id;
	}
}
