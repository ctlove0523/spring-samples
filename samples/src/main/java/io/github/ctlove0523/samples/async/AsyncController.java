package io.github.ctlove0523.samples.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsyncController {

	@RequestMapping(value = "/v5/iot/instances/{instance_id}", method = RequestMethod.GET)
	public CompletableFuture<IotInstance> showIotApplication(@PathVariable(name = "instance_id") String instanceId) {
		IotInstance application = new IotInstance();
		application.setInstanceId(instanceId);
		application.setClusterId(new StringBuilder(instanceId).reverse().toString());

		return CompletableFuture.supplyAsync(() -> application);
	}
}
