package cc.vileda.resx.command;

import cc.vileda.resx.event.DistributedEvent;

public class Command extends DistributedEvent
{
	public Command(String address)
	{
		super(address);
	}
}
