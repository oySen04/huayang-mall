package com.oysen.mallorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * basePackages 指定fegin接口路径
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.oysen.mallorder.dao")
@EnableFeignClients(basePackages = "com.oysen.mallorder.fegin")
public class MallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }

}
