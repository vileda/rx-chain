package cc.vileda.experiment.common;

import java.util.HashMap;
import java.util.Map;

public class UserStore {
	private Map<String, User> userList = new HashMap<>();

	public User get(String key) {
		return userList.get(key);
	}

	public User put(String key, User value) {
		return userList.put(key, value);
	}
}