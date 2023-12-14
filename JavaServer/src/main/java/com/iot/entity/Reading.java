package com.iot.entity;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_reading")
@Entity
@ToString
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long id;

    private LocalTime time;

    @Column(name = "actual_soil")
    private int actualSoil;

    @Column(name = "actual_temperature")
    private int actualTemperature;

    @Column(name = "soil_threshold")
    private int soilThreshold;

    @Column(name = "temperature_threshold")
    private int temperatureThreshold;

    @Column(name = "pump_on")
    private int pumpOn;

    @Column(name = "pump_time")
    private int pumpTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", referencedColumnName = "device_id", foreignKey = @ForeignKey(name = "fk_reading_device_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Device device;
}
