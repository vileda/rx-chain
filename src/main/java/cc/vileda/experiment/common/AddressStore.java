package cc.vileda.experiment.common;

import java.util.HashMap;
import java.util.Map;

public class AddressStore {
	private Map<String, Address> addressList = new HashMap<>();

	public Address put(String key, Address value) {
		return addressList.put(key, value);
	}
}
