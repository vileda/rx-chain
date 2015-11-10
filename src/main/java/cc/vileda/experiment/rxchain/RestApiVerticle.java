package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;
import cc.vileda.experiment.common.User;
import cc.vileda.experiment.common.aggregate.UserAggregate;
import cc.vileda.experiment.common.command.ChangeUserEmailCommand;
import cc.vileda.resx.command.Command;
import cc.vileda.experiment.common.command.CreateAddressCommand;
import cc.vileda.experiment.common.command.CreateUserCommand;
import cc.vileda.resx.EventStore;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import rx.Observable;

public class RestApiVerticle extends AbstractVerticle {
	public void start() {
		EventBus eventBus = vertx.eventBus();
		EventStore eventStore = new EventStore(vertx, eventBus);

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		router.get("/users").handler(routingContext -> {
			eventStore.getPersistableEventList().subscribe(persistableEvents -> {
				routingContext.response().end(Json.encode(persistableEvents));
			});
		});

		router.get("/users/:id").handler(routingContext -> {
			final String id = routingContext.request().getParam("id");
			eventStore.load(id, UserAggregate.class)
				.subscribe(user -> {
					routingContext.response().end(Json.encode(user));
				});
		});

		router.post("/users/:id/email").handler(routingContext -> {
			final String id = routingContext.request().getParam("id");
			String newEmail = routingContext.getBodyAsString();
			ChangeUserEmailCommand command = new ChangeUserEmailCommand(id, Json.decodeValue(newEmail, String.class));
			publishCommand(command, eventStore, routingContext, String.class);
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
