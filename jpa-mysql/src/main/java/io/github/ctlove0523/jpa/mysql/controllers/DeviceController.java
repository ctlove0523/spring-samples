package io.github.ctlove0523.jpa.mysql.controllers;

import io.github.ctlove0523.jpa.mysql.db.Device;
import io.github.ctlove0523.jpa.mysql.db.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @RequestMapping(value = "/v1/devices", method = RequestMethod.POST)
    public ResponseEntity<Void> batchInsertDevice() {
        for (int i = 0; i < 10; i++) {
            String appId = UUID.randomUUID().toString();

            for (int j = 0; j < 10; j++) {
                String deviceId = UUID.randomUUID().toString();
                Device device = new Device();
                device.setDeviceId(deviceId);
                device.setAppId(appId);
                deviceRepository.save(device);
            }

        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/devices", method = RequestMethod.GET)
    public ResponseEntity<List<Device>> findDeviceByAppId(@RequestParam(value = "appId", required = true) String appId) {
        List<Device> devices = deviceRepository.findByAppId(appId);

        return new ResponseEntity<>(devices, HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/devices/{device_id}", method = RequestMethod.HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deviceExistsByIdUserHead(@PathVariable("device_id") String deviceId) {

        boolean exists = deviceRepository.existsByDeviceId(deviceId);

        return ResponseEntity.ok(exists);
    }

    @RequestMapping(value = "/v1/devices/{device_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deviceExistsByIdUseGet(@PathVariable("device_id") String deviceId) {

        Device device = deviceRepository.findByDeviceId(deviceId);

        return ResponseEntity.ok(device != null);
    }
}
