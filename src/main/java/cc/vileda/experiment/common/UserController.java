package cc.vileda.experiment.common;

import lombok.extern.java.Log;

import java.util.UUID;

@Log
public class UserController {
	private UserStore store = new UserStore();

	public User save(String name, String email) {
		String id = UUID.randomUUID().toString();
		User user = new User(id, name, email, null, null);
		store.put(id, user);
		return user;
	}

	public User setGroup(User user, String group) {
		user.setGroup(group);
		log.fine(String.format("adding user %s to group %s", user.getName(), group));
		return user;
	}

	public UserStore getStore() {
		return store;
	}
}
