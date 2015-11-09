package cc.vileda.experiment.common.command;

import lombok.Getter;
import lombok.Setter;

import static cc.vileda.experiment.common.Globals.CREATE_ADDRESS_COMMAND_ADDRESS;

@Getter
@Setter
public class CreateAddressCommand extends Command {
	private String city;
	private String zip;

	public CreateAddressCommand()
	{
		super(CREATE_ADDRESS_COMMAND_ADDRESS);
	}

	public CreateAddressCommand(String city, String zip) {
		super(CREATE_ADDRESS_COMMAND_ADDRESS);
		this.city = city;
		this.zip = zip;
	}
}
