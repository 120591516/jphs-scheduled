package com.jinpaihushi.nurserank.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jinpaihushi.jphs.nurse.model.NurseRank;
import com.jinpaihushi.jphs.nurse.service.NurseRankService;
import com.jinpaihushi.jphs.order.model.Order;
import com.jinpaihushi.jphs.order.service.OrderService;
import com.jinpaihushi.utils.CycleTimeUtils;
import com.jinpaihushi.utils.MyPredicate;
import com.jinpaihushi.utils.UUIDUtils;

@Component
@Configurable
@EnableScheduling
public class NurseRankScheduled {

    @Autowired
    private OrderService orderService;

    @Autowired
    private NurseRankService nurseRankService;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron = "${updateNurseRank}")
    @Transactional
    public void updateNurseRank() {
        try {
            logger.info("开始更新护士排名！！");
            String dateStr = CycleTimeUtils.getPastDate(2);
            Map<String, Object> query = new HashMap<>();
            query.put("confirmTime", dateStr);
            query.put("schedule", 5);
            //获取昨天已完成的所有订单
            List<Order> orderList = orderService.getCompletedOrder(query);
            //获取护士列表中的所有的数据
            NurseRank nurseRankQuery = new NurseRank();
            if (orderList.size() > 0) {
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
            //更新服务完成但是不在护士列表的护士信息

            List<Map<String, Object>> notIn = orderService.getNotInRank();
            for (Map<String, Object> map : notIn) {
                nurseRankQuery = new NurseRank();
                //生成一个0-3的随机数
                int num = (int) (Math.random() * 4);
                //更新护士的排序
                //昨天获得的分数
                int degreeHeat = Integer.parseInt(map.get("number").toString()) * 5;
                nurseRankQuery.setId(UUIDUtils.getId());
                nurseRankQuery.setBaseServerNumber(num);
                nurseRankQuery.setDegreeHeat(degreeHeat);
                nurseRankQuery.setRealServerNumer(Integer.parseInt(map.get("number").toString()));
                nurseRankQuery.setStatus(1);
                nurseRankQuery.setCreateTime(new Date());
                nurseRankQuery.setUpdateTime(new Date());
                nurseRankService.update(nurseRankQuery);
            }
            logger.info("更新护士排名结束！！");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
