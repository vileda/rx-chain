package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.CreateUserRequest;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import rx.Observable;

public class VertxMain {
	public static void main(String[] args) {
		CreateUserProcess createUserProcess = new CreateUserProcess();

		Vertx vertx = io.vertx.rxjava.core.Vertx.vertx();

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		router.post("/users").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			CreateUserRequest createUserRequest = Json.decodeValue(routingContext.getBodyAsString(), CreateUserRequest.class);

			createUserProcess
					.runCreateUserObservable(createUserRequest)
					.onErrorResumeNext(throwable -> {
						response.setStatusCode(500).end(throwable.getMessage());
						return Observable.error(throwable);
					})
					.subscribe(createUserResponse -> {
						response.setStatusCode(createUserResponse.getStatus())
								.end(createUserResponse.getMessage());
					});
		});

		server.requestHandler(router::accept).listen(8080);
	}
}
