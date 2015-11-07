package cc.vileda.experiment.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUserRequest {
	private String name;
	private String email;
	private Address address;
}
