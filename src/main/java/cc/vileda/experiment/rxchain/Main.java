package cc.vileda.experiment.rxchain;

public class Main {
	public static void main(String[] args) {
		CreateUserProcess processChain = new CreateUserProcess();
		System.out.println(processChain.run("admin"));
		System.out.println(processChain.run("user"));
		System.out.println(processChain.run("anon"));
		System.out.println(processChain.run("anon", "foo@trashmail.com"));
	}
}
