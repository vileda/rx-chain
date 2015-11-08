package cc.vileda.experiment.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCreatedResponse extends SuccessResponse {
	private String userId;

	public UserCreatedResponse() {
		super("");
	}

	public UserCreatedResponse(User user) {
		super(user.getId());
		userId = user.getId();
	}
}
