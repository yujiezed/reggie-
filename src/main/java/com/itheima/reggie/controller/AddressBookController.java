package com.itheima.reggie.controller;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 地址管理 前端控制器
 * </p>
 *
 * @author anyi
 * @since 2022-05-25
 */
@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);

        // 获取过滤器保存的当前登录用户ID
        Long userId = BaseContext.getCurrentId();

        if (userId == null) {
            return R.error("用户未登录");
        }

        // 设置该地址属于哪个用户
        addressBook.setUserId(userId);

        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        log.info("addressBook:{}",addressBook);

        Long userId = BaseContext.getCurrentId();

        // 先取消当前用户原有的默认地址
        LambdaUpdateWrapper<AddressBook> cancelDefaultWrapper = new LambdaUpdateWrapper<>();
        cancelDefaultWrapper.eq(AddressBook::getUserId, userId);
        cancelDefaultWrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(cancelDefaultWrapper);

        // 再将本次选择的地址设为默认，同时限制只能修改当前用户的地址
        LambdaUpdateWrapper<AddressBook> setDefaultWrapper = new LambdaUpdateWrapper<>();
        setDefaultWrapper.eq(AddressBook::getId, addressBook.getId());
        setDefaultWrapper.eq(AddressBook::getUserId, userId);
        setDefaultWrapper.set(AddressBook::getIsDefault, 1);
        boolean updated = addressBookService.update(setDefaultWrapper);
        if (!updated) {
            return R.error("地址不存在或不属于当前用户");
        }

        addressBook.setIsDefault(1);
        return R.success(addressBook);
    }
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if(addressBook == null){
            return R.error("没有找到该对象");
        }else{
            return R.success(addressBook);
        }
    }

    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}",addressBook);
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        return R.success(addressBookService.list(queryWrapper));
    }
}

