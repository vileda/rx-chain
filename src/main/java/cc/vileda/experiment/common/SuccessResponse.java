package cc.vileda.experiment.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuccessResponse implements Response {
	private String userId;
}
