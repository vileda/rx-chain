package cc.vileda.experiment.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SuccessResponse extends Response {
	public SuccessResponse(String message) {
		super(200, message);
	}
}
