package cc.vileda.experiment.common.event;

public class SourcedEvent extends DistributedEvent
{
	public SourcedEvent(String address)
	{
		super(address);
	}
}
