package cc.vileda.experiment.common;

import java.util.UUID;

public class UserController {
	private UserStore store = new UserStore();

	public User save(String name, String email) {
		String id = UUID.randomUUID().toString();
		User user = new User(id, name, email, null, null);
		store.put(id, user);
		return user;
	}
}
