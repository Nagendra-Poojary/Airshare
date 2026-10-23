package com.ewit.airshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AirShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirShareApplication.class, args);
        System.out.println("=================================================");
        System.out.println("🚀 AirShare Dispatch Engine running on port 8080");
        System.out.println("   PostGIS Corridor Clustering Active");
        System.out.println("=================================================");
    }
}
