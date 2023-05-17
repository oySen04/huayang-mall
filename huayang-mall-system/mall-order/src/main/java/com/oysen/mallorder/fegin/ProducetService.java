package com.oysen.mallorder.fegin;

import com.oysen.mallcommons.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 调用mall-producet服务
 */
@FeignClient(name = "mall-producet")
public interface ProducetService {
    /**
     * 需要访问的远程方法
     * @return
     */
    @GetMapping("/mallproducet/brand/all")
    public R queryAllBrand();
}
