package com.jinpaihushi.wechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jinpaihushi.jphs.wechat.service.WechatService;
@Component
@Configurable
@EnableScheduling
public class WechatScheduled {

	@Autowired
	private	WechatService wechatService;
	
	@Scheduled(cron = "${GET_WECTH_TOKEN}")
	public void getWechatTokens(){
		wechatService.getTokens();
	}
}
