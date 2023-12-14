package com.iot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_plant")
@Entity
@ToString
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_id")
    private Long id;

    private String name;
    @Column(name = "science_name")
    private String scienceName;
    @Column(name = "max_soil")
    private int maxSoil;
    @Column(name = "min_soil")
    private int minSoil;
    @Column(name = "max_temperature")
    private int maxTemperature;
    @Column(name = "min_temperature")
    private int minTemperature;
}
