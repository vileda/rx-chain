package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import org.junit.Test;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class CreateUserProcessTest {

	@Test
	public void testCreateUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.createUser(new CreateUserRequest("foo", "bar", null)).subscribe(testSubscriber);
		testSubscriber.assertNoErrors();
	}

	@Test
	public void testCreateAddress() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<Address> testSubscriber = new TestSubscriber<>();
		process.createAddress(new User(), new Address("", "city1", "")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testCreateForbiddenAddress() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<Address> testSubscriber = new TestSubscriber<>();
		process.createAddress(new User(), new Address("", "", "")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testAddKnownAdminUserToGroup() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.addUserToGroup(createUser("admin")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testAddKnownUserToGroup() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.addUserToGroup(createUser("user")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testAddUnknownUserToGroup() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.addUserToGroup(createUser("anon")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testSendMail() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.sendMail(createUser("admin", "foo@bar.de")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testRunCreateUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		Response response = process.run("admin");
		assertThat(response, instanceOf(SuccessResponse.class));
		assertNotNull(((SuccessResponse) response).getUserId());
		assertThat(((SuccessResponse) response).getUserId().length(), is(36));
	}

	@Test
	public void testRunCreateCityFailingUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		CreateUserRequest createUserRequest = createUserRequest("user1@example.com");
		Response response = process.runCreateUser(createUserRequest);
		assertThat(response, instanceOf(ErrorResponse.class));
	}

	@Test
	public void testRunCreateUnknownFailingUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		Response response = process.run("anon");
		assertThat(response, instanceOf(ErrorResponse.class));
	}

	@Test
	public void testRunCreateFailingSpamUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		Response response = process.run("anon", "foo@trashmail.com");
		assertThat(response, instanceOf(ErrorResponse.class));
		assertThat(((ErrorResponse)response).getMessage(), containsString("email"));
	}

	@Test
	public void testThrowIfSpamEmail() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserRequest> testSubscriber = new TestSubscriber<>();
		CreateUserRequest createUserRequest = createUserRequest("user1@trashmail.com");
		process.throwIfSpamEmail(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testDoNotThrowIfNotSpamEmail() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserRequest> testSubscriber = new TestSubscriber<>();
		CreateUserRequest createUserRequest = createUserRequest("user1@example.com");
		process.throwIfSpamEmail(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testThrowIfForbiddenName() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserRequest> testSubscriber = new TestSubscriber<>();
		CreateUserRequest createUserRequest = createUserRequest("vader", "foo@bar.cc");
		process.throwIfForbiddenName(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	private CreateUserRequest createUserRequest(String email) {
		return createUserRequest("admin", email);
	}

	private CreateUserRequest createUserRequest(String name, String email) {
		return new CreateUserRequest(
				name,
				email,
				new Address("", "city2", "12345")
		);
	}

	private User createUser(String name, String email) {
		return new User(null, name, email, null, null);
	}

	private User createUser(String name) {
		return createUser(name, null);
	}
}