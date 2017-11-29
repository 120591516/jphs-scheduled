package com.jinpaihushi.order.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.jinpaihushi.jphs.account.model.Account;
import com.jinpaihushi.jphs.account.service.AccountService;
import com.jinpaihushi.jphs.commodity.model.CommodityOrder;
import com.jinpaihushi.jphs.commodity.service.CommodityOrderService;
import com.jinpaihushi.jphs.order.model.Order;
import com.jinpaihushi.jphs.order.service.OrderService;
import com.jinpaihushi.jphs.system.service.DoPostSmsService;
import com.jinpaihushi.jphs.transaction.model.Transaction;
import com.jinpaihushi.jphs.transaction.service.TransactionService;
import com.jinpaihushi.utils.DoubleUtils;
import com.jinpaihushi.utils.UUIDUtils;

//@Component
//@Configurable
//@EnableScheduling
public class OrderScheduled {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OrderService orderService;

    @Autowired
    private DoPostSmsService doPostSmsService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CommodityOrderService commodityOrderService;

    @Value("${SMS_user_Unpaid_Order}")
    private String SMS_user_Unpaid_Order;

    /**
     * 定时查询未付款订单信息，发送短信
     */
    @Scheduled(cron = "${check_order_Unpaid}")
    public void getOrderUnpaid() {
        logger.info("定时查询未付款订单信息，发送短信");
        String condition = "= 15";
        List<Map<String, Object>> orderUnpaid = orderService.getOrderUnpaid(condition);
        for (int i = 0; i < orderUnpaid.size(); i++) {
            String goodsName = (String) orderUnpaid.get(i).get("title");
            String phone = (String) orderUnpaid.get(i).get("phone");
            doPostSmsService.sendSms(phone, SMS_user_Unpaid_Order, "{\"name\":\"" + goodsName + "\"}");
        }
    }

    @Scheduled(cron = "${check_order_Unpaid}")
    public void updateOrderToExpired() {
        String condition = "= 120";
        List<Map<String, Object>> orderUnpaid = orderService.getOrderUnpaid(condition);
        Order order = null;
        for (int i = 0; i < orderUnpaid.size(); i++) {
            order = orderService.loadById(orderUnpaid.get(i).get("id").toString());
            orderService.updateOrderToExpired(order);
        }
    }

    /**
     * 定时任务处理白富美的奖励记录
     */
    @Scheduled(cron = "${test}")
    public void sendTransaction() {
        logger.info("开始执行白富美活动！！！");
        List<Transaction> insertList = new ArrayList<>();
        Transaction tr = null;
        //处理服务订单的奖励记录
        Account account = null;
        logger.info("开始执行服务订单！！！");
        List<Map<String, Object>> sendServiceTransaction = orderService.getSendTransaction();
        for (Map<String, Object> map : sendServiceTransaction) {
            account = new Account();
            double payPrice = Double.parseDouble(map.get("payPrice").toString());
            tr = new Transaction();
            tr.setId(UUIDUtils.getId());
            tr.setOrderId(map.get("orderId").toString());
            tr.setScore(0);
            tr.setOperate(4);
            tr.setOperateSource(1);
            tr.setRemark("[活动奖励]" + map.get("goodsName"));
            tr.setWithdraw(0);
            tr.setAmount(DoubleUtils.mul(payPrice, 0.15));
            tr.setCreatorId(map.get("nurseId").toString());
            tr.setCreatorName(map.get("nurseName").toString());
            tr.setCreateTime(new Date());
            tr.setStatus(1);
            tr.setType(1);
            account.setCreatorId(map.get("nurseId").toString());
            account = accountService.load(account);
            account.setBalance(DoubleUtils.add(account.getBalance(), DoubleUtils.mul(payPrice, 0.15)));
            accountService.update(account);
            insertList.add(tr);
        }
        logger.info("开始执行商品订单！！！");
        //商品订单的记录
        List<Map<String, Object>> sendCommodityTransaction = commodityOrderService.getSendTransaction();
        for (Map<String, Object> map : sendCommodityTransaction) {
            account = new Account();
            double profit = Double.parseDouble(map.get("profit").toString());
            int number = Integer.parseInt(map.get("number").toString());
            tr = new Transaction();
            tr.setId(UUIDUtils.getId());
            tr.setOrderId(map.get("id").toString());
            tr.setScore(0);
            tr.setOperate(4);
            tr.setOperateSource(2);
            String remark = "[活动奖励]" + map.get("title");
            remark += number == 1 ? "" : "X" + number;
            tr.setRemark(remark);
            tr.setWithdraw(0);
            tr.setAmount(DoubleUtils.mul(profit, number));
            tr.setCreatorId(map.get("user_id").toString());
            tr.setCreatorName(map.get("name").toString());
            tr.setCreateTime(new Date());
            tr.setStatus(1);
            tr.setType(2);
            account.setCreatorId(map.get("user_id").toString());
            account = accountService.load(account);
            account.setBalance(DoubleUtils.add(account.getBalance(), DoubleUtils.mul(profit, number)));
            accountService.update(account);
            insertList.add(tr);
        }
        transactionService.inserts(insertList);
        logger.info("执行白富美活动结束！！！");
    }

    @Scheduled(cron = "${com_balance}")
    public void comBalance() {
        logger.info("订单任务--护士分成");
        CommodityOrder com = new CommodityOrder();
        boolean result = commodityOrderService.commodityPayNurse(com);
        System.out.println(result);
    }

    @Scheduled(cron = "${com_balance}")
    public void proxyRecipient() {
        logger.info("订单任务自动收货");
        commodityOrderService.proxyRecipientss();
    }
}
