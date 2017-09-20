package com.jinpaihushi.nurserank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.jinpaihushi.jphs.order.service.OrderService;

@Component
@Configurable
@EnableScheduling
public class NurseRankScheduled {
    @Autowired
    private OrderService orderService;
}
