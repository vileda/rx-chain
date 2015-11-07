package cc.vileda.experiment.common;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends Response {
	public ErrorResponse(String message) {
		super(500, message);
	}
}
