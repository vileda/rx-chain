package cc.vileda.experiment.common.event;

public class Event<T> {
	private T clazz;
	private String payload;

	public Event(T clazz, String payload) {
		this.clazz = clazz;
		this.payload = payload;
	}

	public T getClazz() {
		return clazz;
	}

	public String getPayload() {
		return payload;
	}
}
