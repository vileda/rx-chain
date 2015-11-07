package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import lombok.extern.java.Log;
import rx.Observable;

import java.util.List;

@Log
public class CreateUserProcess extends ProcessChain {
	private UserController userController = new UserController();
	private AddressController addressController = new AddressController();

	public Response run(String name, String email, String city) {
		CreateUserRequest createUserRequest = new CreateUserRequest(
				name,
				email,
				new Address("", city, "12345")
		);

		runCreateUser(createUserRequest);

		return response;
	}

	public Response run(String name, String email) {
		return run(name, email, "city1");
	}

	public Response run(String name) {
		return run(name, "user1@example.com", "city1");
	}

	Response runCreateUser(List<CreateUserRequest> createUserRequests) {
		Observable.from(createUserRequests)
				.doOnNext(this::runCreateUser)
				.subscribe();

		return response;
	}

	Response runCreateUser(CreateUserRequest createUserRequest) {
		runCreateUserObservable(createUserRequest)
				.subscribe(this::setResponse);

		return response;
	}

	Observable<Response> runCreateUserObservable(CreateUserRequest createUserRequest) {
		return throwIfSpamEmail(createUserRequest)
				.doOnNext(this::throwIfForbiddenName)
				.doOnNext(this::throwIfNameTaken)
				.flatMap(this::createUserChain)
				.doOnNext(this::sendMail)
				.flatMap(this::success)
				.onErrorResumeNext(throwable -> {
					setResponse(new ErrorResponse(throwable.getMessage()));
					log.warning(throwable.getMessage());
					return Observable.empty();
				});
	}

	Observable<User> createUserChain(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
			.flatMap(userRequest -> createUser(userRequest)
				.flatMap(user -> createAddress(user, userRequest.getAddress())
						.flatMap(ignored -> createUserAccount(user))
						.flatMap(ignored -> addUserToGroup(user))));
	}

	Observable<CreateUserRequest> throwIfSpamEmail(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.flatMap(userRequest -> {
					if(userRequest.getEmail() != null && userRequest.getEmail().contains("trashmail")) {
						return Observable.error(new RuntimeException("email not allowed"));
					}
					return Observable.just(userRequest);
				});
	}

	public Observable<CreateUserRequest> throwIfForbiddenName(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.flatMap(userRequest -> {
					if(isForbiddenName(userRequest.getName())) {
						return Observable.error(new RuntimeException("name not allowed"));
					}
					return Observable.just(userRequest);
				});
	}

	private boolean isForbiddenName(String name) {
		return name != null && name.contains("vader");
	}

	public Observable<CreateUserRequest> throwIfNameTaken(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.flatMap(userRequest -> {
					if(userController.getStore().getUserByName(userRequest.getName()).isPresent()) {
						return Observable.error(new RuntimeException("name is taken"));
					}
					return Observable.just(userRequest);
				});
	}

	Observable<User> createUser(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.map(userRequest -> {
					log.fine("making user " + userRequest);
					return userController.save(userRequest.getName(), userRequest.getEmail());
				});
	}

	Observable<Address> createAddress(User user, Address userAddress) {
		return Observable.just(userAddress)
				.flatMap(address -> {
					if (addressController.isForbiddenCity(address.getCity())) {
						log.fine("not making address " + address);
						return Observable.error(new RuntimeException("forbidden city"));
					}

					log.fine("making address " + address);
					Address save = addressController.save(address.getCity(), address.getZip());
					user.setAddress(save);
					return Observable.just(address);
				});
	}

	Observable<Account> createUserAccount(User newUser) {
		return Observable.just(newUser)
				.map(user -> {
					log.fine("creating account for " + user.getName());
					return new Account(user.getId());
				});
	}

	Observable<User> addUserToGroup(User newUser) {
		return createUserGroupObservable(newUser)
				.switchIfEmpty(createAdminGroupObservable(newUser)
					.switchIfEmpty(throwUserError("no group found for newUser")));
	}

	private Observable<User> throwUserError(String s) {
		return Observable.just(s).flatMap(s1 -> Observable.error(new RuntimeException(s1)));
	}

	private Observable<User> createUserGroupObservable(User user) {
		if(getUserGroupFor(user)) {
			return Observable.just(userController.setGroup(user, "user"));
		}
		return Observable.empty();
	}

	private Observable<User> createAdminGroupObservable(User user) {
		if(getAdminGroupFor(user)) {
			return Observable.just(userController.setGroup(user, "admin"));
		}
		return Observable.empty();
	}

	Observable<User> sendMail(User newUser) {
		return Observable.just(newUser)
				.map(user -> {
					log.fine("sending mail to " + user.getEmail());
					return user;
				});
	}

	protected Observable<Response> success(User user) {
		return Observable.just(user)
				.map(result -> new UserCreatedResponse(user));
	}

	private boolean getUserGroupFor(User user) {
		return "user".equals(user.getName());
	}

	private boolean getAdminGroupFor(User user) {
		return "admin".equals(user.getName());
	}
}
