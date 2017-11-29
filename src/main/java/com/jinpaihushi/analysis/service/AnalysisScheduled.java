package com.jinpaihushi.analysis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.jinpaihushi.jphs.analysis.service.AnalysisTaskService;

@Component
@Configurable
@EnableScheduling
public class AnalysisScheduled {
    @Autowired
    private AnalysisTaskService analysisTaskService;
}
