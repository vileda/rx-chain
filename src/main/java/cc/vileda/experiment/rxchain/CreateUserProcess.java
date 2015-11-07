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
		Observable
				.just(createUserRequest)
				.flatMap(this::throwIfSpamEmail)
				.flatMap(this::throwIfForbiddenName)
				.flatMap(this::createUserChain)
				.flatMap(this::sendMail)
				.flatMap(this::success)
				.onErrorResumeNext(throwable -> {
					setResponse(new ErrorResponse(throwable.getMessage()));
					log.warning(throwable.getMessage());
					return Observable.empty();
				})
				.subscribe(this::setResponse)
		;

		return response;
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

	Observable<User> createUser(CreateUserRequest createUserRequest) {
		return Observable.just(createUserRequest)
				.map(userRequest -> {
					log.info("making user " + userRequest);
					return userController.save(userRequest.getName(), userRequest.getEmail());
				});
	}

	Observable<Address> createAddress(User user, Address userAddress) {
		return Observable.just(userAddress)
				.map(address -> {
					if (addressController.isForbiddenCity(address.getCity())) {
						log.info("not making address " + address);
						throw new RuntimeException("forbidden city");
					}

					log.info("making address " + address);
					Address save = addressController.save(address.getCity(), address.getZip());
					user.setAddress(save);
					return address;
				});
	}

	Observable<Account> createUserAccount(User newUser) {
		return Observable.just(newUser)
				.map(user -> {
					log.info("creating account for " + user.getName());
					return new Account(user.getId());
				});
	}

	Observable<User> addUserToGroup(User newUser) {
		return Observable.just(newUser)
				.flatMap(this::getAdminGroupObservable)
				.switchIfEmpty(Observable.just(newUser)
						.flatMap(this::getUserGroupObservable)
						.switchIfEmpty(Observable.just(newUser).flatMap(user -> {
							throw new RuntimeException("no group found for newUser");
						})));
	}

	private Observable<User> getUserGroupObservable(User user) {
		if(getUserGroup(user)) {
			user.setGroup("newUser");
			log.info("adding newUser to group " + user);
			return Observable.just(user);
		}
		return Observable.empty();
	}

	private Observable<User> getAdminGroupObservable(User user) {
		if(getAdminGroup(user)) {
			user.setGroup("admin");
			log.info("adding newUser to group " + user);
			return Observable.just(user);
		}
		return Observable.empty();
	}

	Observable<User> sendMail(User newUser) {
		return Observable.just(newUser)
				.map(user -> {
					log.info("sending mail to " + user.getEmail());
					return user;
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
