package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Response;

abstract public class ProcessChain {
	protected Response response;

	protected void setResponse(Response response) {
		this.response = response;
	}
}
