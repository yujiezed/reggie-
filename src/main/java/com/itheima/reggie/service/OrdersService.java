package com.itheima.reggie.service;

import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.dto.OrdersDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author anyi
 * @since 2022-05-25
 */
public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);

    Page<OrdersDto> userPage(Integer page, Integer pageSize);
}
