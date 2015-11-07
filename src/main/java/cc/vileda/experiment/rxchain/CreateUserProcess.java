package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import rx.Observable;

public class CreateUserProcess extends ProcessChain {
	private UserController userController = new UserController();
	private AddressController addressController = new AddressController();

	public Response run(String name) {
		CreateUserRequest createUserRequest = new CreateUserRequest(
				name,
				"user1@example.com",
				new Address("", "city1", "12345")
		);

		runCreateUser(createUserRequest);

		return response;
	}

	Response runCreateUser(CreateUserRequest... createUserRequests) {
		Observable
				.from(createUserRequests)
				.flatMap(userRequest -> createUser(userRequest)
						.flatMap(user -> createAddress(user, userRequest.getAddress())
								.flatMap(address -> addUserToGroup(user)
										.flatMap(user1 -> sendMail(user1)
												.flatMap(this::success)))))
				.onErrorResumeNext(throwable -> {
					setResponse(new ErrorResponse(throwable.getMessage()));
					return Observable.empty();
				})
				.subscribe()
		;

		return response;
	}

	Observable<User> createUser(CreateUserRequest ...userRequests) {
		return Observable.from(userRequests)
				.flatMap(userRequest -> {
					System.out.println("making user " + userRequest);
					return Observable.just(userController.save(userRequest.getName(), userRequest.getEmail()));
				});
	}

	Observable<Address> createAddress(User user, Address ...addresses) {
		return Observable.from(addresses)
				.flatMap(address -> {
					if (addressController.isForbiddenCity(address.getCity())) {
						System.out.println("not making address " + address);
						throw new RuntimeException("forbidden city");
					}

					System.out.println("making address " + address);
					Address save = addressController.save(address.getCity(), address.getZip());
					user.setAddress(save);
					return Observable.just(address);
				});
	}

	Observable<User> addUserToGroup(User ...users) {
		return Observable.from(users)
				.flatMap(user -> {
					if(getAdminGroup(user)) {
						user.setGroup("admin");
						System.out.println("adding user to group " + user);
						return Observable.just(user);
					}
					return Observable.empty();
				}).switchIfEmpty(Observable.from(users).flatMap(user -> {
					if(getUserGroup(user)) {
						user.setGroup("user");
						System.out.println("adding user to group " + user);
						return Observable.just(user);
					}
					return Observable.empty();
				}).switchIfEmpty(Observable.from(users).flatMap(user1 -> {
					throw new RuntimeException("no group found for user");
				})));
	}

	Observable<String> sendMail(User ...users) {
		return Observable.from(users)
				.flatMap(user -> {
					System.out.println("sending mail to " + user.getEmail());
					return Observable.just((user.getEmail() + "foo"));
				});
	}

	private boolean getUserGroup(User user) {
		return "user".equals(user.getName());
	}

	private boolean getAdminGroup(User user) {
		return "admin".equals(user.getName());
	}
}
