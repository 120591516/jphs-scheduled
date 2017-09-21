package com.jinpaihushi.order.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jinpaihushi.jphs.order.service.OrderService;
import com.jinpaihushi.jphs.system.service.DoPostSmsService;

@Component
@Configurable
@EnableScheduling
public class OrderScheduled {
    @Autowired
    private OrderService orderService;

    @Autowired
    private DoPostSmsService doPostSmsService;

    @Value("${SMS_user_Unpaid_Order}")
    private String SMS_user_Unpaid_Order;

    /**
     * 定时查询未付款订单信息，发送短信
     */
    @Scheduled(cron = "${check_order_Unpaid}")
    public void getOrderUnpaid() {
        List<Map<String, Object>> orderUnpaid = orderService.getOrderUnpaid();
        for (int i = 0; i < orderUnpaid.size(); i++) {
            String goodsName = (String) orderUnpaid.get(i).get("title");
            String phone = (String) orderUnpaid.get(i).get("phone");
            doPostSmsService.sendSms(phone, SMS_user_Unpaid_Order, "{\"name\":\"" + goodsName + "\"}");
        }
    }
}
