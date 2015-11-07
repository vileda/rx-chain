package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import rx.Observable;

import java.util.List;

public class CreateUserProcess extends ProcessChain {
	private UserController userController = new UserController();
	private AddressController addressController = new AddressController();

	public Response run(String name, String email) {
		CreateUserRequest createUserRequest = new CreateUserRequest(
				name,
				email,
				new Address("", "city1", "12345")
		);

		runCreateUser(createUserRequest);

		return response;
	}

	public Response run(String name) {
		return run(name, "user1@example.com");
	}

	Response runCreateUser(List<CreateUserRequest> createUserRequests) {
		Observable.from(createUserRequests)
				.doOnNext(this::runCreateUser)
				.subscribe();

		return response;
	}

	Response runCreateUser(CreateUserRequest createUserRequest) {
		Observable
				.just(createUserRequest)
				.flatMap(this::throwIfSpamEmail)
				.flatMap(this::throwIfForbiddenName)
				.flatMap(userRequest -> createUser(userRequest)
						.flatMap(user -> createAddress(user, userRequest.getAddress())
								.flatMap(address -> addUserToGroup(user)
										.flatMap(user1 -> sendMail(user1)
												.flatMap(this::success)))))
				.onErrorResumeNext(throwable -> {
					setResponse(new ErrorResponse(throwable.getMessage()));
					return Observable.empty();
				})
				.subscribe(this::setResponse)
		;

		return response;
	}

	Observable<User> createUser(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.flatMap(userRequest -> {
					//System.out.println("making user " + userRequest);
					return Observable.just(userController.save(userRequest.getName(), userRequest.getEmail()));
				});
	}

	Observable<CreateUserRequest> throwIfSpamEmail(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.doOnNext(userRequest -> {
					if(userRequest.getEmail() != null && userRequest.getEmail().contains("trashmail")) {
						throw new RuntimeException("email not allowed");
					}
				});
	}

	public Observable<CreateUserRequest> throwIfForbiddenName(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.doOnNext(userRequest -> {
					if(userRequest.getName() != null && userRequest.getName().contains("vader")) {
						throw new RuntimeException("name not allowed");
					}
				});
	}

	Observable<Address> createAddress(User user, Address userAddress) {
		return Observable.just(userAddress)
				.flatMap(address -> {
					if (addressController.isForbiddenCity(address.getCity())) {
						//System.out.println("not making address " + address);
						throw new RuntimeException("forbidden city");
					}

					//System.out.println("making address " + address);
					Address save = addressController.save(address.getCity(), address.getZip());
					user.setAddress(save);
					return Observable.just(address);
				});
	}

	Observable<User> addUserToGroup(User newUser) {
		return Observable.just(newUser)
				.flatMap(user -> {
					if(getAdminGroup(user)) {
						user.setGroup("admin");
						//System.out.println("adding newUser to group " + user);
						return Observable.just(user);
					}
					return Observable.empty();
				}).switchIfEmpty(Observable.just(newUser).flatMap(user -> {
					if(getUserGroup(user)) {
						user.setGroup("newUser");
						//System.out.println("adding newUser to group " + user);
						return Observable.just(user);
					}
					return Observable.empty();
				}).switchIfEmpty(Observable.just(newUser).flatMap(user -> {
					throw new RuntimeException("no group found for newUser");
				})));
	}

	Observable<User> sendMail(User newUser) {
		return Observable.just(newUser)
				.flatMap(user -> {
					//System.out.println("sending mail to " + user.getEmail());
					return Observable.just(user);
				});
	}

	protected Observable<Response> success(User user) {
		return Observable.just(user)
				.map(result -> new SuccessResponse(user.getId()));
	}

	private boolean getUserGroup(User user) {
		return "user".equals(user.getName());
	}

	private boolean getAdminGroup(User user) {
		return "admin".equals(user.getName());
	}
}
