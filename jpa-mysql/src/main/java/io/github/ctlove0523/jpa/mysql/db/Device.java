package io.github.ctlove0523.jpa.mysql.db;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "t_device", indexes = @Index(name = "device_id", columnList = "device_id"))
public class Device {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    @Column(name = "app_id", nullable = false)
    private String appId;
}
