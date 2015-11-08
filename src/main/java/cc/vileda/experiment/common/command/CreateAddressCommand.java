package cc.vileda.experiment.common.command;

import cc.vileda.experiment.common.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAddressCommand {
	private String userId;
	private Address address;
}
