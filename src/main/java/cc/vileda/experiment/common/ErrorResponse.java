package cc.vileda.experiment.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse implements Response {
	private String message;
}
