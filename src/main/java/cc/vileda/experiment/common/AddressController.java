package cc.vileda.experiment.common;

import java.util.UUID;

public class AddressController {
	private AddressStore store = new AddressStore();

	public Address save(String city, String zip) {
		String id = UUID.randomUUID().toString();
		Address address = new Address(id, city, zip);
		store.put(id, address);
		return address;
	}

	public boolean isForbiddenCity(String city) {
		return !"city1".equals(city);
	}
}
