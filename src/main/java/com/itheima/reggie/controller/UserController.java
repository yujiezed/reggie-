package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Value("${aliyun.sms.sign-name:}")
    private String smsSignName;

    @Value("${aliyun.sms.template-code:}")
    private String smsTemplateCode;

    @Value("${aliyun.sms.enabled:false}")
    private boolean smsEnabled;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.hasText(phone) && phone.matches("1[3-9][0-9]{9}")){
            String code= ValidateCodeUtils.generateValidateCode(4).toString();

            // 学习阶段使用模拟短信，在 IDEA 运行控制台查看验证码。
            if (!smsEnabled) {
                session.setAttribute(phone, code);
                log.info("【模拟短信】手机号：{}，验证码：{}（5分钟内有效）", phone, code);
                return R.success("模拟验证码已生成，请查看IDEA运行控制台");
            }

            if (!StringUtils.hasText(smsSignName) || !StringUtils.hasText(smsTemplateCode)) {
                return R.error("请配置短信签名和模板编码：aliyun.sms.sign-name、aliyun.sms.template-code");
            }
            try {
                SMSUtils.sendMessage(smsSignName, smsTemplateCode, phone, code);
            } catch (Exception ex) {
                log.error("短信发送失败", ex);
                return R.error("短信发送失败，请检查短信配置、密钥权限和阿里云发送记录");
            }

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);
            return R.success("短信验证码发送成功");
        }



        return R.error("短信验证码发送失败");
    }
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info("map:{}",map);

        //获取手机号
        String phone=map.get("phone").toString();
        //获取验证码
        String code=map.get("code").toString();

        //从Session中获取保存的验证码
        Object codeInSession=session.getAttribute(phone);

        if(codeInSession!=null&&codeInSession.equals(code))
        {
            //如果能够比对成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user=userService.getOne(queryWrapper);
            if(user==null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
