package cc.vileda.experiment.common.command;

import cc.vileda.resx.command.Command;
import lombok.Getter;
import lombok.Setter;

import static cc.vileda.experiment.common.Globals.CREATE_USER_COMMAND_ADDRESS;

@Getter
@Setter
public class CreateUserCommand extends Command
{
	private String name;
	private String email;

	public CreateUserCommand()
	{
		super(CREATE_USER_COMMAND_ADDRESS);
	}

	public CreateUserCommand(String name, String email)
	{
		super(CREATE_USER_COMMAND_ADDRESS);
		this.name = name;
		this.email = email;
	}
}
