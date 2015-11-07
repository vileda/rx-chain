package cc.vileda.experiment.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Address {
	private String id;
	private String city;
	private String zip;
}
