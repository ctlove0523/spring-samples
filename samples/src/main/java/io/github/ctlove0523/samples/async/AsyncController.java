package io.github.ctlove0523.samples.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsyncController {

	@RequestMapping(value = "/v5/iot/applications/{app_id}", method = RequestMethod.GET)
	public CompletableFuture<IotApplication> showIotApplication(@PathVariable(name = "app_id") String appId) {
		IotApplication application = new IotApplication();
		application.setId(appId);
		application.setName("default application");
		application.setCreatedTime("2021-02-28");

		return CompletableFuture.supplyAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(3);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			return application;
		});
	}
}
