package com.itheima.reggie.utils;

import com.alibaba.fastjson.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;

import java.util.Collections;

/**
 * 短信发送工具，提供与教程一致的调用入口。
 * 密钥从环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和
 * ALIBABA_CLOUD_ACCESS_KEY_SECRET 读取。
 */
public final class SMSUtils {

    private SMSUtils() {
    }

    /**
     * 发送验证码，短信模板中的变量名应为 ${code}。
     * 接口返回成功表示阿里云已受理，不代表手机已经收到。
     *
     * @param signName 已审核通过的短信签名
     * @param templateCode 已审核通过的模板编码
     * @param phoneNumbers 接收短信的手机号
     * @param param 验证码文本，例如 "123456"
     */
    public static void sendMessage(String signName, String templateCode,
                                   String phoneNumbers, String param) {
        requireText(signName, "短信签名不能为空");
        requireText(templateCode, "短信模板编码不能为空");
        requireText(phoneNumbers, "手机号不能为空");
        requireText(param, "验证码不能为空");
        try {
            Config config = new Config()
                    .setAccessKeyId(getRequiredEnvironmentVariable("ALIBABA_CLOUD_ACCESS_KEY_ID"))
                    .setAccessKeySecret(getRequiredEnvironmentVariable("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
            config.endpoint = "dysmsapi.aliyuncs.com";

            Client client = new Client(config);
            SendSmsRequest request = new SendSmsRequest()
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setPhoneNumbers(phoneNumbers)
                    .setTemplateParam(JSON.toJSONString(Collections.singletonMap("code", param)));

            SendSmsResponse response = client.sendSms(request);
            if (response == null || response.getBody() == null) {
                throw new IllegalStateException("阿里云短信接口未返回结果");
            }
            if (!"OK".equals(response.getBody().getCode())) {
                throw new IllegalStateException("短信发送失败：" + response.getBody().getMessage());
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            // 将调用失败交给业务层处理，避免失败后仍提示发送成功。
            throw new IllegalStateException("短信接口调用失败", ex);
        }
    }

    private static String getRequiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("未配置环境变量：" + name);
        }
        return value;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
