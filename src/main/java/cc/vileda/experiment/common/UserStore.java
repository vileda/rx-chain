package cc.vileda.experiment.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserStore {
	private Map<String, User> userList = new HashMap<>();

	public User put(String key, User value) {
		return userList.put(key, value);
	}

	public Optional<User> getUserByName(String name) {
		return userList.values().stream().filter(user -> name.equals(user.getName())).findFirst();
	}
}
