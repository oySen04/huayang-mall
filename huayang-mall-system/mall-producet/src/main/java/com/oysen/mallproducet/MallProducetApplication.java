package com.oysen.mallproducet;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.oysen.mallproducet.dao")
public class MallProducetApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProducetApplication.class, args);
    }

}
