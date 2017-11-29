package com.jinpaihushi.worktime.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.jinpaihushi.jphs.worktime.service.WorktimeService;

//@Component
//@Configurable
//@EnableScheduling
public class WorkTimeScheduled {

    @Autowired
    private WorktimeService worktimeService;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron = "${USER_PERIOD_DAY}")
    public void updateUserWorkTime() {
        logger.info("开始更新用户日程");
        worktimeService.updateUserWorkTime();
        logger.info("更新用户日程结束");

    }

    @Scheduled(cron = "${NURSE_PERIOD_DAY}")
    public void updateAllNurseWorkTime() {
        logger.info("开始更新护士日程");
        worktimeService.updateAllNurseWorkTime();
        logger.info("更新护士日程结束");
    }

}
