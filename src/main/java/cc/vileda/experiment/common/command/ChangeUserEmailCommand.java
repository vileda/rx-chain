package cc.vileda.experiment.common.command;

import cc.vileda.resx.command.Command;
import lombok.Getter;
import lombok.Setter;

import static cc.vileda.experiment.common.Globals.CHANGE_USER_EMAIL_COMMAND_ADDRESS;

@Getter
@Setter
public class ChangeUserEmailCommand extends Command
{
	private String id;
	private String email;

	public ChangeUserEmailCommand()
	{
		super(CHANGE_USER_EMAIL_COMMAND_ADDRESS);
	}

	public ChangeUserEmailCommand(String id, String email)
	{
		super(CHANGE_USER_EMAIL_COMMAND_ADDRESS);
		this.id = id;
		this.email = email;
	}
}
