package com.lab.equipment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UtilizationTrendDTO {
    @Setter
    private String date;
    private double utilizationRate;

    public void setUtilizationRate(double utilizationRate) {
        this.utilizationRate=utilizationRate;
    }

}
