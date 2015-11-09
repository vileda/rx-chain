package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;
import cc.vileda.experiment.common.User;
import cc.vileda.experiment.common.command.Command;
import cc.vileda.experiment.common.command.CreateAddressCommand;
import cc.vileda.experiment.common.command.CreateUserCommand;
import cc.vileda.experiment.common.event.AddressCreatedEvent;
import cc.vileda.experiment.common.event.Event;
import cc.vileda.experiment.common.event.UserCreatedEvent;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import rx.Observable;

import java.util.List;

import static cc.vileda.experiment.common.Globals.CREATE_ADDRESS_COMMAND_ADDRESS;
import static cc.vileda.experiment.common.Globals.CREATE_USER_COMMAND_ADDRESS;

public class RestApiVerticle extends AbstractVerticle {
	public void start() {
		EventBus eventBus = vertx.eventBus();
		EventStore eventStore = new EventStore(eventBus);

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		router.get("/users").handler(routingContext -> {
			List<Event> events = eventStore.getEventList();
			routingContext.response().end(Json.encode(events));
		});

		router.post("/users").handler(routingContext -> {
			CreateUserRequest createUserRequest = Json.decodeValue(routingContext.getBodyAsString(), CreateUserRequest.class);
			CreateUserCommand command = new CreateUserCommand(createUserRequest.getName(), createUserRequest.getEmail());
			publishCommand(command, eventStore, routingContext, User.class);
		});

		router.post("/addresses").handler(routingContext -> {
			CreateUserRequest createUserRequest = Json.decodeValue(routingContext.getBodyAsString(), CreateUserRequest.class);
			Address createAddressRequest = createUserRequest.getAddress();
			CreateAddressCommand createAddressCommand = new CreateAddressCommand();
			createAddressCommand.setCity(createAddressRequest.getCity());
			createAddressCommand.setZip(createAddressRequest.getZip());
			publishCommand(createAddressCommand, eventStore, routingContext, Address.class);
		});

		server.requestHandler(router::accept).listen(8080);
	}

	private <T extends Command, R> void publishCommand(T payload, EventStore eventStore, RoutingContext routingContext, Class<R> clazz) {
		HttpServerResponse response = routingContext.response();

		eventStore.publish(payload, clazz)
				.onErrorResumeNext(message -> {
					response.setStatusCode(500).end(message.getMessage());
					return Observable.empty();
				})
				.subscribe(reply -> {
					response.setStatusCode(200).end(Json.encode(reply));
				});
	}
}
