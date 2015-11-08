package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;
import io.vertx.core.json.Json;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.IOUtils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RestApiIT {

	@BeforeClass
	public static void beforeClass() {
		new VertxMain().run();
	}

	@Test
	public void testCreateUser() throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8080/users");
		String admin = Json.encode(new CreateUserRequest("admin", "foo@fff.com", null));
		httpPost.setEntity(new StringEntity(admin));
		HttpResponse execute = httpclient.execute(httpPost);
		assertThat(execute.getStatusLine().getStatusCode(), is(200));
		assertThat(IOUtils.readFully(execute.getEntity().getContent(), -1, false).length, is(36));
	}

	@Test
	public void testCreateUserWithSpamMail() throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8080/users");
		String admin = Json.encode(new CreateUserRequest("admin", "foo@trashmail.com", null));
		httpPost.setEntity(new StringEntity(admin));
		HttpResponse execute = httpclient.execute(httpPost);
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
	}

	@Test
	public void testCreateAddress() throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8080/addresses");
		Address address = new Address("", "city1", "12345");
		String admin = Json.encode(new CreateUserRequest("admin", "foo@ddd.com", address));
		httpPost.setEntity(new StringEntity(admin));
		HttpResponse execute = httpclient.execute(httpPost);
		assertThat(execute.getStatusLine().getStatusCode(), is(200));
		assertThat(IOUtils.readFully(execute.getEntity().getContent(), -1, false).length, is(36));
	}

	@Test
	public void testCreateForbiddenAddress() throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8080/addresses");
		Address address = new Address("", "city2", "12345");
		String admin = Json.encode(new CreateUserRequest("admin", "foo@dsaf.com", address));
		httpPost.setEntity(new StringEntity(admin));
		HttpResponse execute = httpclient.execute(httpPost);
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
	}

}