package cc.vileda.experiment.rxchain;

import io.vertx.rxjava.core.Vertx;

public class VertxMain {
	public static void main(String[] args) {
		new VertxMain().run(Vertx.vertx());
	}

	public void run(Vertx vertx) {
		vertx.deployVerticle("cc.vileda.experiment.rxchain.RestApiVerticle");
		vertx.deployVerticle("cc.vileda.experiment.rxchain.UserVerticle");
	}
}
