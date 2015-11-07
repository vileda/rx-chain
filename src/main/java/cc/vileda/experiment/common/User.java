package cc.vileda.experiment.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
	private String id;
	private String name;
	private String email;
	private Address address;
	private String group;
}
