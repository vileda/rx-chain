package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Response;
import cc.vileda.experiment.common.SuccessResponse;
import rx.Observable;

abstract public class ProcessChain {
	protected Response response;

	protected Observable<Response> success(Object ...results) {
		response = new SuccessResponse();
		return Observable.just(results)
				.map(result -> new SuccessResponse());
	}

	protected void setResponse(Response response) {
		this.response = response;
	}
}
