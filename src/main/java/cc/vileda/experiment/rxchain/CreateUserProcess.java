package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import lombok.extern.java.Log;
import rx.Observable;

import java.util.List;

import static cc.vileda.experiment.common.Globals.*;

@Log
public class CreateUserProcess extends ProcessChain {
	private EventBus eventBus;
	private UserController userController = new UserController();
	private AddressController addressController = new AddressController();

	public CreateUserProcess(EventBus eventBus) {
		this.eventBus = eventBus;
		addCommandHandlers();
		addEventHandlers();
	}

	public CreateUserProcess() { }

	private void addCommandHandlers() {
		eventBus.consumer(CREATE_USER_COMMAND_ADDRESS, message -> {
			createUserPrechecks(Json.decodeValue((String) message.body(), CreateUserRequest.class))
					.flatMap(this::createUserChain)
					.onErrorResumeNext(throwable -> {
						return publishFailedEvent(CREATING_USER_FAILED_EVENT_ADDRESS, throwable, message);
					})
					.subscribe(user -> {
						eventBus.publish(USER_CREATED_EVENT_ADDRESS, Json.encode(user));
						message.reply(Json.encode(user));
					});
		});

		eventBus.consumer(CREATE_ADDRESS_COMMAND_ADDRESS, message -> {
			Address newAddress = Json.decodeValue((String) message.body(), Address.class);
			createAddress(newAddress)
					.onErrorResumeNext(throwable -> {
						return publishFailedEvent(CREATING_ADDRESS_FAILED_EVENT_ADDRESS, throwable, message);
					})
					.subscribe(address -> {
						eventBus.publish(ADDRESS_CREATED_EVENT_ADDRESS, Json.encode(address));
						message.reply(Json.encode(address));
					})
			;
		});
	}

	private Observable publishFailedEvent(String event, Throwable throwable, Message<Object> message) {
		eventBus.publish(event, throwable.getMessage());
		DeliveryOptions deliveryOptions = new DeliveryOptions();
		deliveryOptions.addHeader(ERROR_HEADER, HEADER_TRUE);
		message.reply(throwable.getMessage(), deliveryOptions);
		return Observable.empty();
	}

	private void addEventHandlers() {
		eventBus.consumer(USER_CREATED_EVENT_ADDRESS, message -> {
			System.out.println("I have received a message: " + message.body());
		});
		eventBus.consumer(CREATING_USER_FAILED_EVENT_ADDRESS, message -> {
			System.out.println("I have received a fail message: " + message.body());
		});

		eventBus.consumer(ADDRESS_CREATED_EVENT_ADDRESS, message -> {
			System.out.println("I have received a message: " + message.body());
		});
		eventBus.consumer(CREATING_ADDRESS_FAILED_EVENT_ADDRESS, message -> {
			System.out.println("I have received a fail message: " + message.body());
		});
	}

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
		return createUserPrechecks(createUserRequest)
				.flatMap(this::createUserChain)
				.doOnNext(this::sendMail)
				.flatMap(this::success)
				.onErrorResumeNext(throwable -> {
					setResponse(new ErrorResponse(throwable.getMessage()));
					log.warning(throwable.getMessage());
					return Observable.empty();
				});
	}

	private Observable<CreateUserRequest> createUserPrechecks(CreateUserRequest createUserRequest) {
		return throwIfSpamEmail(createUserRequest)
				.flatMap(this::throwIfForbiddenName)
				.flatMap(this::throwIfNameTaken);
	}

	Observable<User> createUserChain(CreateUserRequest createUserRequest) {
		return createUser(createUserRequest)
				.flatMap(user -> addUserToGroup(user)
					.doOnNext(this::createUserAccount));
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

	Observable<Address> createAddress(Address userAddress) {
		return Observable.just(userAddress)
				.flatMap(address -> {
					if (addressController.isForbiddenCity(address.getCity())) {
						log.fine("not making address " + address);
						return Observable.error(new RuntimeException("forbidden city"));
					}

					log.fine("making address " + address);
					return Observable.just(addressController.save(address.getCity(), address.getZip()));
				});
	}

	Observable<Account> createUserAccount(User user) {
		log.fine("creating account for " + user.getName());
		return Observable.just(new Account(user.getId()));
	}

	Observable<User> addUserToGroup(User newUser) {
		return createUserGroupObservable(newUser)
				.switchIfEmpty(createAdminGroupObservable(newUser)
					.switchIfEmpty(throwUserError("no group found for user " + newUser.getName())));
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
