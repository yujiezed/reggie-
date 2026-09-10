package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自定义元对象处理器，用于自动填充公共字段。
 */
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {//重点是重写这个类的两个方法，当insert和update的时候自动填充公共字段
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());
        Date now = new Date();
        setValueIfPresent(metaObject, "createTime", now);
        setValueIfPresent(metaObject, "updateTime", now);
        setValueIfPresent(metaObject, "createUser", BaseContext.getCurrentId());
        setValueIfPresent(metaObject, "updateUser", BaseContext.getCurrentId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());
        setValueIfPresent(metaObject, "updateTime", new Date());
        setValueIfPresent(metaObject, "updateUser", BaseContext.getCurrentId());
    }

    private void setValueIfPresent(MetaObject metaObject, String fieldName, Object value) {
        if (metaObject.hasSetter(fieldName)) {
            metaObject.setValue(fieldName, value);
        }
    }
}
