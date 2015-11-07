package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Benchmark {
	public static void main(String[] args) {
		int requests = 200000;
		CreateUserProcess processChain = new CreateUserProcess();

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < requests; i++) {
			CreateUserRequest createUserRequest =
					new CreateUserRequest("admin", UUID.randomUUID().toString(), new Address(UUID.randomUUID().toString(), "city1", "12345"));
			processChain.runCreateUser(createUserRequest);
		}
		System.out.println(System.currentTimeMillis() - startTime);

		processChain = new CreateUserProcess();

		startTime = System.currentTimeMillis();
		List<CreateUserRequest> requestList = new ArrayList<>();
		for (int i = 0; i < requests; i++) {
			CreateUserRequest createUserRequest =
					new CreateUserRequest("admin", UUID.randomUUID().toString(), new Address(UUID.randomUUID().toString(), "city1", "12345"));
			requestList.add(createUserRequest);
		}
		processChain.runCreateUser(requestList);
		System.out.println(System.currentTimeMillis() - startTime);

		processChain = new CreateUserProcess();

		startTime = System.currentTimeMillis();
		for (int i = 0; i < requests; i++) {
			CreateUserRequest createUserRequest =
					new CreateUserRequest("admin", UUID.randomUUID().toString(), new Address(UUID.randomUUID().toString(), "city1", "12345"));
			processChain.runCreateUser(createUserRequest);
		}
		System.out.println(System.currentTimeMillis() - startTime);

		processChain = new CreateUserProcess();

		startTime = System.currentTimeMillis();
		requestList = new ArrayList<>();
		for (int i = 0; i < requests; i++) {
			CreateUserRequest createUserRequest =
					new CreateUserRequest("admin", UUID.randomUUID().toString(), new Address(UUID.randomUUID().toString(), "city1", "12345"));
			requestList.add(createUserRequest);
		}
		processChain.runCreateUser(requestList);
		System.out.println(System.currentTimeMillis() - startTime);
	}
}