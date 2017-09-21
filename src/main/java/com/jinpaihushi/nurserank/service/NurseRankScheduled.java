package com.jinpaihushi.nurserank.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jinpaihushi.jphs.nurse.model.NurseRank;
import com.jinpaihushi.jphs.nurse.service.NurseRankService;
import com.jinpaihushi.jphs.order.model.Order;
import com.jinpaihushi.jphs.order.service.OrderService;
import com.jinpaihushi.utils.MyPredicate;

@Component
@Configurable
@EnableScheduling
public class NurseRankScheduled {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private OrderService orderService;

    @Autowired
    private NurseRankService nurseRankService;

    @Scheduled(cron = "${updateNurseRank}")
    public void updateNurseRank() {
        try {
            String dateStr = sdf.format(new Date());
            Map<String, Object> query = new HashMap<>();
            query.put("confirmTime", dateStr);
            query.put("schedule", 5);
            //获取昨天已完成的所有订单
            List<Order> orderList = orderService.getCompletedOrder(query);
            //获取护士列表中的所有的数据
            NurseRank nurseRankQuery = new NurseRank();
            nurseRankQuery.setStatus(1);
            List<NurseRank> nurseList = nurseRankService.list(nurseRankQuery);
            for (NurseRank nurseRank : nurseList) {
                nurseRankQuery = new NurseRank();
                //生成一个0-3的随机数
                int num = (int) (Math.random() * 4);
                Predicate predicate = new MyPredicate("acceptUserId", nurseRank.getUserId());
                @SuppressWarnings("unchecked")
                List<Order> select = (List<Order>) CollectionUtils.select(orderList, predicate);
                //更新护士的排序
                //昨天获得的分数
                int degreeHeat = select.size() * 5;
                BeanUtils.copyProperties(nurseRankQuery, nurseRank);
                nurseRankQuery.setBaseServerNumber(nurseRank.getBaseServerNumber() + num);
                nurseRankQuery.setDegreeHeat(nurseRank.getDegreeHeat() + degreeHeat);
                nurseRankQuery.setRealServerNumer(nurseRank.getRealServerNumer() + select.size());
                nurseRankQuery.setUpdateTime(new Date());
                nurseRankService.update(nurseRankQuery);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
