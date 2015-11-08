package cc.vileda.experiment.common;

import lombok.Data;

@Data
public class AddressCreatedResponse {
	private String addressId;
	public AddressCreatedResponse(Address address) {
		addressId = address.getId();
	}
}
