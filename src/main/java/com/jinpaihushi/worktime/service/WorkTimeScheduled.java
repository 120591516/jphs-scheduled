package com.jinpaihushi.worktime.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jinpaihushi.jphs.worktime.service.WorktimeService;

@Component
@Configurable
@EnableScheduling
public class WorkTimeScheduled {

    @Autowired
    private WorktimeService worktimeService;

    @Scheduled(cron = "${USER_PERIOD_DAY}")
    public void updateUserWorkTime() {
        worktimeService.updateUserWorkTime();
    }

    @Scheduled(cron = "${NURSE_PERIOD_DAY}")
    public void updateAllNurseWorkTime() {
        worktimeService.updateAllNurseWorkTime();
    }

    @Scheduled(cron = "${test}")
    public void test() {
        System.out.println(111);
    }
}
