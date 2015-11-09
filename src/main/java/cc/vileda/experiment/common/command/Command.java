package cc.vileda.experiment.common.command;

import cc.vileda.experiment.common.event.DistributedEvent;

public class Command extends DistributedEvent
{
	public Command(String address)
	{
		super(address);
	}
}
