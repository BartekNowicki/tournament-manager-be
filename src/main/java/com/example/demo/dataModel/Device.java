package com.example.demo.dataModel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "deviceId")
    private long deviceId;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "ownedDevices")
    Set<Tester> owners;


    //constructor needed by the CSVHelper
    public Device(long deviceId, String description) {
        this.deviceId = deviceId;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Device [deviceId=" + deviceId + ", description=" + description + "]";
    }
}





