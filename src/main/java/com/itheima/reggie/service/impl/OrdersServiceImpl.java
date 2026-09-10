package com.itheima.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author anyi
 * @since 2022-05-25
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<AddressBook> addressQuery = new LambdaQueryWrapper<>();
        addressQuery.eq(AddressBook::getId, orders.getAddressBookId());
        addressQuery.eq(AddressBook::getUserId, userId);
        AddressBook addressBook = addressBookService.getOne(addressQuery);
        if (addressBook == null) {
            throw new CustomException("收货地址不存在");
        }

        LambdaQueryWrapper<ShoppingCart> cartQuery = new LambdaQueryWrapper<>();
        cartQuery.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartItems = shoppingCartService.list(cartQuery);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CustomException("购物车为空，不能下单");
        }

        long orderNumber = IdWorker.getId();
        BigDecimal amount = cartItems.stream()
                .map(item -> item.getAmount().multiply(BigDecimal.valueOf(item.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Date now = new Date();
        orders.setNumber(String.valueOf(orderNumber));
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setOrderTime(now);
        orders.setCheckoutTime(now);
        orders.setAmount(amount);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        this.save(orders);

        List<OrderDetail> orderDetails = cartItems.stream().map(item -> {
            OrderDetail detail = new OrderDetail();
            BeanUtils.copyProperties(item, detail, "id");
            detail.setOrderId(orders.getId());
            return detail;
        }).collect(Collectors.toList());
        orderDetailService.saveBatch(orderDetails);

        shoppingCartService.remove(cartQuery);
    }

    @Override
    public Page<OrdersDto> userPage(Integer page, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Orders::getUserId, userId);
        orderQuery.orderByDesc(Orders::getOrderTime);
        this.page(ordersPage, orderQuery);

        Page<OrdersDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(ordersPage, dtoPage, "records");
        List<OrdersDto> records = ordersPage.getRecords().stream().map(order -> {
            OrdersDto dto = new OrdersDto();
            BeanUtils.copyProperties(order, dto);
            LambdaQueryWrapper<OrderDetail> detailQuery = new LambdaQueryWrapper<>();
            detailQuery.eq(OrderDetail::getOrderId, order.getId());
            dto.setOrderDetails(orderDetailService.list(detailQuery));
            return dto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(records);
        return dtoPage;

    }
}
