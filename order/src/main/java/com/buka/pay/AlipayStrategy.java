package com.buka.pay;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.buka.dto.PayInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lhb
 * @version 1.0
 * @description: 支付宝支付具体策略
 * @date 2025/3/18 下午5:46
 */
@Slf4j
@Service
public class AlipayStrategy implements PayStrategy {
    /**
    * @Author: lhb
    * @Description: 支付
    * @DateTime: 下午2:48 2025/3/22
    * @Params: [payInfoDTO]
    * @Return java.lang.String
    */
    @Override
    public String unifiedorder(PayInfoDTO payInfoDTO) {
        // 初始化SDK
        String pageRedirectionData = null;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());

            // 构造请求参数以调用接口
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();

            // 设置商户订单号
            model.setOutTradeNo(payInfoDTO.getOutTradeNo());

            // 设置订单总金额
            model.setTotalAmount(payInfoDTO.getPayFee().toString());

            // 设置订单标题
            model.setSubject(payInfoDTO.getTitle());


            // 设置用户付款中途退出返回商户网站的地址
            model.setQuitUrl("http://www.baidu.com");


            // 获取当前时间
            LocalDateTime currentTime = LocalDateTime.now();

            // 加上14分钟
            LocalDateTime newTime = currentTime.plus(14, ChronoUnit.MINUTES);

            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 格式化输出
            String formattedTime = newTime.format(formatter);

            // 设置订单绝对超时时间
            model.setTimeExpire(formattedTime);


            request.setBizModel(model);
            // 第三方代调用模式下请设置app_auth_token
            // request.putOtherTextParam("app_auth_token", "<-- 请填写应用授权令牌 -->");

            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");
            // 如果需要返回GET请求，请使用
//             AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "GET");
            pageRedirectionData = response.getBody();
            System.out.println("----------");
            System.out.println(pageRedirectionData);
            System.out.println("----------");
            if (response.isSuccess()) {
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("支付宝调用失败");
        }
        return pageRedirectionData;
    }

    /**
    * @Author: lhb
    * @Description: 退款
    * @DateTime: 下午2:48 2025/3/22
    * @Params: [payInfoDTO]
    * @Return java.lang.String
    */
    @Override
    public String refund(PayInfoDTO payInfoDTO) {
        return PayStrategy.super.refund(payInfoDTO);
    }

    /**
    * @Author: lhb
    * @Description: 查询
    * @DateTime: 下午2:48 2025/3/22
    * @Params: [payInfoDTO]
    * @Return java.lang.String
    */
    @Override
    public String queryPaySuccess(PayInfoDTO payInfoDTO) {
        try {
            // 初始化SDK
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());

            // 构造请求参数以调用接口
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();

            // 设置订单支付时传入的商户订单号
            model.setOutTradeNo(payInfoDTO.getOutTradeNo());


            // 设置查询选项
            List<String> queryOptions = new ArrayList<String>();
            queryOptions.add("trade_settle_info");
            model.setQueryOptions(queryOptions);

            request.setBizModel(model);
            // 第三方代调用模式下请设置app_auth_token
            // request.putOtherTextParam("app_auth_token", "<-- 请填写应用授权令牌 -->");

            AlipayTradeQueryResponse response = alipayClient.execute(request);


            if (response.isSuccess()) {
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }

            //未支付：null    不存在：null   已支付：SUCCESS
            return response.getTradeStatus();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("支付宝查询失败");
        }
    }

    /**
     * @Author: lhb
     * @Description: 支付宝支付获取相关配置
     * @DateTime: 下午2:08 2025/3/24
     * @Params: []
     * @Return com.alipay.api.AlipayConfig
     */
    private AlipayConfig getAlipayConfig() {
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCNCgNAYL81cwfCfIk3zxJPoBTtY3+Jj03N7RuHcnV/LPC/AWOabfwkBgQ5HBekH+mUR9MYqSAuiETQ/RodwszuHGo/juU6pdxJEnSzYo5KN6MlkSesQldXRxrSLwTVVZ9zKGzrYhuj3bfOJW6e0us7K/ekEohJ01c63Q6jUmAvjkoHvJ95pO09USgizBKty4Ki0qVNa9cQeEuhQLqRWnM7jHDiMYi+9iyFTqsP5ln5LFLFm3SRx4cZXX0JWKlWqfjSYm2doqK629V1Bopew9yVRBnlin3RaFx6JV/jiFrR8SZ3s1DHdAwM7J6p0J0SejwNfanoC2/KXl2w9BvXQEGnAgMBAAECggEBAIj9K7FRBwd8RhIycfkiCLAMVadJmCvdY/3bCn0hwFwZI3DygXMS1u+KZHmv/opscKyUosX2J4rq0qSG4eH8Pp4cPVqkiaPQS8kxwuYlmP0fUzh+56DaAwmDx9pUzwYDF097+WcbMKuWBByXoP1k8hPUHcFgOr4yJyzGX5xku4YT4yuTfWavDtzIdapQl6RmCb+Xg+3/3vBM5W+9qG1zSz+R10+D1w6HwaSxGChln7v5dnajKux+OcRCcRTDS8t5ej29AbDh6urn6BWcK70wV0tEU0RDmY5Ir9iDpXp/CrgpKjDLYJlu/1xu7rt3iGyj6+7SuYxhw6AorlGa9hH0r6kCgYEA2JZTPfEjPhJa06ir2sCWOyjkWZ1mZhNHpYKUZelK+SQwYJkIcHuv2UDN9JwzkJ8zufaTfPOlrXnEcT1DS4kfD81ev6DFfsEnPlGy0rc9YKZ5K8YpucMFSz9z1AwCA/hi07Ux6RZTYktt+8VqKn+CUUnRbQcj9Owe56pzqIQCfIMCgYEAprRLJWZ/VmC5BY54cjypZYUMusjOw0rAt/UZxqeSKJizU5VIQq+dkBXsvFFOTOHQh/tKiz0kCnXz15hQnI7gLtv0v9tErvkw/S+f0XfV/7JXILaCXBJ2x/MOskjYdgmBxCSl9N2c7ge4VayhZ+NGpKS0Brv2vgCalyJ91GSLJQ0CgYA7GmqE0LTpT2DxuWmMPFnCsxn9SnKTEFfQ1p1gYhVPf4ykD56580a9Zm3NfoKjyI35BUyiIGrpt/zfWfRvPG/5g/WDHYBHYrYuz2SZE9/v6/3M8DqasplTO3GEP/Kc2r89PojsLmd6v1K4Dds21azeOeKoefZZ88VLOrxtE/kJuQKBgHrpmjKERUUp4aGwLyyDf57Dn+QkZRbnCftYs12edgyKskXhXrsUgwcWs0sSY7oaEUgBxy+sr+MqntSKkH0udyr1sJq5EAY246D6OUsXoWh7VPRgvPMoBf5dPqvDPB6j2dMF9nr531g9xvMcapGPqKmCat5APqhPB6yBmOc+Dr4NAoGACv/JTpkFmX/cYVtAi8D7UTs4/J+ktwTNBoYZDlBExRTZUEG2YLKcxkUPJgrpe0JVCT+WecY5em1mW7hLyP03ucaFcWUjKOvA08dORIId8nfgtVrKcVCI+1SCubxRnfAbz8hjweWv8RX6PU2PNvUC/5ohP2cCRKmn0Vaf+h6oGyE=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9KImMApadUDjRtsSQ4CrzuTYUUyZe+dNnKpUFvj5jYtQsXuUDQBDBZeE2BGaE5woONYaUVcszVmGtDkMd4sI7xh9SF1LU1z1YoR3tr0UWFH6YY+07JFjgfRF75U+qrXN1qxBFahMYq1CGl9PX2Hfxa/JcHtnRoaR34H4exSESl9qPXxmqnq2l3934UkDr5pQIyHYGcI0xqyx+HmGPLZmVMVQ0fbSuJwbQZhIHRzr73tc+1ANQGVN9mBVudoVLgKNojtFmAaBYU4Tn5qL9Dx/RqgAmzg0n8f5kCcywPIEHMSwjzbSWErdY9nzibRIxDMQFy3VrgbDymC9y4Rtq8HdpQIDAQAB";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        alipayConfig.setAppId("2021000146683887");
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }
}
