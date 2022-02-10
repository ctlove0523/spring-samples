package io.github.ctlove0523.jpa.mysql.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    Device findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}
