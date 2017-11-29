package com.jinpaihushi.wechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.jinpaihushi.jphs.wechat.service.WechatService;

//@Component
//@Configurable
//@EnableScheduling
public class WechatScheduled {

    @Autowired
    private WechatService wechatService;

    @Scheduled(cron = "${GET_WECTH_TOKEN}")
    public void getWechatTokens() {
        wechatService.getTokens();
    }
}
