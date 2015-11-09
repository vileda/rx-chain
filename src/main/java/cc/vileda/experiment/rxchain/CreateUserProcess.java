package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import cc.vileda.experiment.common.command.ChangeUserEmailCommand;
import cc.vileda.experiment.common.command.CreateAddressCommand;
import cc.vileda.experiment.common.command.CreateUserCommand;
import cc.vileda.experiment.common.event.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.eventbus.Message;
import lombok.extern.java.Log;
import rx.Observable;

import javax.print.attribute.standard.JobSheets;

import static cc.vileda.experiment.common.Globals.*;

@Log
public class CreateUserProcess {
	private EventStore eventStore;
	private UserController userController = new UserController();
	private AddressController addressController = new AddressController();

	public CreateUserProcess(EventStore eventStore) {
		this.eventStore = eventStore;
		addCommandHandlers();
		addEventHandlers();
	}

	public CreateUserProcess() { }

	private void addCommandHandlers() {
		eventStore.consumer(CreateUserCommand.class, message -> {
			createUserPrechecks(Json.decodeValue(message.body(), CreateUserCommand.class))
				.flatMap(this::createUserChain)
				.onErrorResumeNext(throwable -> {
					return publishFailedEvent(new CreatingUserFailedEvent(throwable.getMessage()), message);
				})
				.subscribe(user -> {
					final UserCreatedEvent userCreatedEvent = new UserCreatedEvent(
							((User)user).getId(),
							((User)user).getName(),
							((User)user).getEmail()
					);
					eventStore.publish(userCreatedEvent, UserCreatedEvent.class);
					message.reply(Json.encode(user));
				});
		});

		eventStore.consumer(CreateAddressCommand.class, message -> {
			createAddress(Json.decodeValue(message.body(), CreateAddressCommand.class))
				.onErrorResumeNext(throwable -> {
					return publishFailedEvent(new CreatingAddressFailedEvent(throwable.getMessage()), message);
				})
				.subscribe(address -> {
					final AddressCreatedEvent addressCreatedEvent = new AddressCreatedEvent(((Address)address).getId());
					eventStore.publish(addressCreatedEvent, AddressCreatedEvent.class);
					message.reply(Json.encode(address));
				});
		});

		eventStore.consumer(ChangeUserEmailCommand.class, message -> {
			ChangeUserEmailCommand newEmail = Json.decodeValue(message.body(), ChangeUserEmailCommand.class);
			UserEmailChangedEvent emailChangedEvent = new UserEmailChangedEvent(newEmail.getId(), newEmail.getEmail());
			eventStore.publish(emailChangedEvent, UserEmailChangedEvent.class);
			message.reply(Json.encode(newEmail.getId()));
		});
	}

	private <T extends FailedEvent> Observable publishFailedEvent(T event, Message<?> message) {
		eventStore.publish(event.getAddress(), event);
		DeliveryOptions deliveryOptions = new DeliveryOptions();
		deliveryOptions.addHeader(ERROR_HEADER, HEADER_TRUE);
		message.reply(Json.encode(event), deliveryOptions);
		return Observable.empty();
	}

	private void addEventHandlers() {
		eventStore.consumer(UserCreatedEvent.class, message -> {
			System.out.println("I have received a message: " + message.body());
		});
		eventStore.consumer(CreatingUserFailedEvent.class, message -> {
			System.out.println("I have received a fail message: " + message.body());
		});

		eventStore.consumer(AddressCreatedEvent.class, message -> {
			System.out.println("I have received a message: " + message.body());
		});
		eventStore.consumer(CreatingAddressFailedEvent.class, message -> {
			System.out.println("I have received a fail message: " + message.body());
		});
	}

	private Observable<CreateUserCommand> createUserPrechecks(CreateUserCommand createUserCommand) {
		return throwIfSpamEmail(createUserCommand)
				.flatMap(this::throwIfForbiddenName)
				.flatMap(this::throwIfNameTaken);
	}

	Observable<User> createUserChain(CreateUserCommand createUserCommand) {
		return createUser(createUserCommand)
				.flatMap(user -> addUserToGroup(user)
					.doOnNext(this::createUserAccount));
	}

	Observable<CreateUserCommand> throwIfSpamEmail(CreateUserCommand createUserCommand) {
		return Observable.just(createUserCommand)
				.flatMap(userRequest -> {
					if(userRequest.getEmail() != null && userRequest.getEmail().contains("trashmail")) {
						return Observable.error(new RuntimeException(ERR_MSG_EMAIL_NOT_ALLOWED));
					}
					return Observable.just(userRequest);
				});
	}

	public Observable<CreateUserCommand> throwIfForbiddenName(CreateUserCommand createUserCommand) {
		return Observable.just(createUserCommand)
				.flatMap(userRequest -> {
					if(isForbiddenName(userRequest.getName())) {
						return Observable.error(new RuntimeException(ERR_MSG_NAME_NOT_ALLOWED));
					}
					return Observable.just(userRequest);
				});
	}

	private boolean isForbiddenName(String name) {
		return name != null && name.contains("vader");
	}

	public Observable<CreateUserCommand> throwIfNameTaken(CreateUserCommand createUserCommand) {
		return Observable.just(createUserCommand)
				.flatMap(userRequest -> {
					if(userController.getStore().getUserByName(userRequest.getName()).isPresent()) {
						return Observable.error(new RuntimeException(ERR_MSG_NAME_IS_TAKEN));
					}
					return Observable.just(userRequest);
				});
	}

	Observable<User> createUser(CreateUserCommand createUserCommand) {
		return Observable.just(createUserCommand)
				.map(userRequest -> {
					log.fine("making user " + userRequest);
					return userController.save(userRequest.getName(), userRequest.getEmail());
				});
	}

	Observable<Address> createAddress(CreateAddressCommand userAddress) {
		return Observable.just(userAddress)
				.flatMap(address -> {
					if (addressController.isForbiddenCity(address.getCity())) {
						log.fine("not making address " + address);
						return Observable.error(new RuntimeException(ERR_MSG_FORBIDDEN_CITY));
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
					.switchIfEmpty(throwUserError(ERR_MSG_NO_GROUP_FOUND_FOR_USER + newUser.getName())));
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

	private boolean getUserGroupFor(User user) {
		return "user".equals(user.getName());
	}

	private boolean getAdminGroupFor(User user) {
		return "admin".equals(user.getName());
	}

	public UserController getUserController() {
		return userController;
	}
}
