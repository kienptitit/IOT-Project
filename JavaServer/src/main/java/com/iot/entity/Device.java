package com.iot.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "tbl_device")
@Entity
@ToString
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long id;

    @Builder.Default
    private String name = "unknown";

    @Column(nullable = false, unique = true)
    private String ip;

    @Column(name = "device_code", nullable = false, unique = true)
    private String deviceCode;

    @Column(name = "soil_threshold")
    @Builder.Default
    @NotNull(message = "Không được để trống")
    @Min(value = 1, message = "Ngưỡng tối thiểu là 1")
    @Max(value = 100, message = "Ngưỡng tối đa là 100")
    private int soilThreshold = 50;

    @Column(name = "temerature_threshold")
    @Builder.Default
    @NotNull(message = "Không được để trống")
    @Min(value = 1, message = "Ngưỡng tối thiểu là 1")
    @Max(value = 100, message = "Ngưỡng tối đa là 100")
    private int temperatureThreshold = 20;

    @Column(name = "pump_time_minute")
    @Builder.Default
    @NotNull(message = "Không được để trống")
    @Min(value = 1, message = "Thời gian bơm tối thiểu là 1 phút")
    @Max(value = 30, message = "Thời gian bơm tối đa là 30 phút")
    private int pumpTimeMinute = 10; // minute

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_device_user_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private List<Reading> readings;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device", fetch = FetchType.EAGER)
    private List<TimeCheck> timeChecks;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plant_id", referencedColumnName = "plant_id")
    private Plant plant;
}
