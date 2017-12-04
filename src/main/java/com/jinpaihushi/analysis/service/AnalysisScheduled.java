package com.jinpaihushi.analysis.service;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jinpaihushi.jphs.analysis.model.AnalysisResult;
import com.jinpaihushi.jphs.analysis.model.AnalysisTask;
import com.jinpaihushi.jphs.analysis.service.AnalysisResultService;
import com.jinpaihushi.jphs.analysis.service.AnalysisTaskService;
import com.jinpaihushi.utils.CycleTimeUtils;
import com.jinpaihushi.utils.IpAddressUtils;
import com.jinpaihushi.utils.UUIDUtils;

@Component
@Configurable
@EnableScheduling
public class AnalysisScheduled {
    @Autowired
    private AnalysisTaskService analysisTaskService;

    @Autowired
    private AnalysisResultService analysisResultService;

    @Value("${base_path}")
    private String base_path;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * 执行中的日志分析任务   每天凌晨 0:10执行
     */
    @Scheduled(cron = "${updateNurseRank}")
    @Transactional
    public void analysisTask() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date time = cal.getTime();
        String yesterday = sdf.format(time);
        AnalysisTask param = new AnalysisTask();
        param.setStatus(1);
        List<AnalysisTask> list = analysisTaskService.list(param);
        String path = "";
        for (AnalysisTask item : list) {
            path = item.getUrl();
            int startIndex = path.indexOf("://");
            int endIndex = path.indexOf(".goldnurse");
            String packageName = path.substring(startIndex + 3, endIndex);
            //文件路径为 base_path + packageName
            String filePath = base_path + "/" + packageName;
            //获取要匹配的url 
            // https://wap.goldnurse.com/activity/bfm.html 
            // /activity/bfm.html  
            int urlSuffixIndex = path.indexOf(".com");
            String urlSuffix = path.substring(urlSuffixIndex + 4);
            String fileName = filePath + "/access_" + yesterday.replaceAll("-", "") + ".log";
            System.out.println(fileName);
            readFileByLines(item.getId(), yesterday, fileName, urlSuffix);
        }
    }

    /**
     * 定时查询需要开启的日志分析任务 五分钟执行一次
     */
    @Scheduled(cron = "${analysis_to_start}")
    @Transactional
    public void queryTaskToStart() {
        String nowStr = sdf.format(new Date());
        logger.info("开始执行日志分析任务！！！！");
        List<AnalysisTask> analysisTask = analysisTaskService.queryTaskToStart();
        String path = "";
        for (int i = 0; i < analysisTask.size(); i++) {
            AnalysisTask analysisTaskEle = analysisTask.get(i);
            //获取到分析的日志
            path = analysisTaskEle.getUrl();
            int startIndex = path.indexOf("://");
            int endIndex = path.indexOf(".goldnurse");
            String packageName = path.substring(startIndex + 3, endIndex);
            //文件路径为 base_path + packageName
            String filePath = base_path + "/" + packageName;
            //开始循环读取每天的文件内容
            String startDay = "2017-09-05";
            List<String> dateList = CycleTimeUtils.getDatesBetweenTwoDate(startDay, nowStr);
            //把今天的日期移除
            dateList.remove(dateList.size() - 1);
            //获取要匹配的url 
            // https://wap.goldnurse.com/activity/bfm.html 
            // /activity/bfm.html  
            int urlSuffixIndex = path.indexOf(".com");
            String urlSuffix = path.substring(urlSuffixIndex + 4);
            for (String string : dateList) {
                //拼接文件的路径
                String fileName = filePath + "/access_" + string.replaceAll("-", "") + ".log";
                System.out.println(fileName);
                readFileByLines(analysisTask.get(i).getId(), string, fileName, urlSuffix);
            }
            //执行结束将任务状态改为执行中
            analysisTaskEle.setStatus(1);
            analysisTaskService.update(analysisTaskEle);
        }
    }

    /**
     * @param analysisTaskId 任务id
     * @param dateStr 日志时间
     * @param fileName 文件名称
     * @param urlSuffix 
     */
    @Transactional
    public void readFileByLines(String analysisTaskId, String dateStr, String fileName, String urlSuffix) {
        //        String fileName = wxPath + "access.log";
        List<AnalysisResult> resultList = new ArrayList<AnalysisResult>();
        try {
            AnalysisResult al = null;
            long count = 0;
            int num = 300;
            while (true) {
                List<String> readLine = readLine(fileName, num, count);
                if (!readLine.isEmpty()) {
                    if (readLine.size() > num) {
                        count = Long.parseLong(readLine.get(readLine.size() - 1));
                        readLine.remove(readLine.size() - 1);
                    }
                    for (int i = 0; i < readLine.size(); i++) {
                        // 获取产品地址
                        int urlStart = readLine.get(i).indexOf("]");
                        int urlEnd = readLine.get(i).indexOf("HTTP");
                        String urladdress = readLine.get(i).substring(urlStart + 6, urlEnd);
                        urladdress = urladdress.trim();
                        if (urladdress.contains(urlSuffix)) {
                            al = new AnalysisResult();
                            al.setId(UUIDUtils.getId());
                            al.setAnalysisTaskId(analysisTaskId);
                            al.setCreateTime(new Date());
                            int timeIndex = readLine.get(i).indexOf(":");
                            String hourse = readLine.get(i).substring(timeIndex + 1, timeIndex + 3);
                            String startTime = hourse + ":00:00";
                            String endTime = hourse + ":59:59";
                            al.setStarttime(timeFormat.parse(startTime));
                            al.setEndtime(timeFormat.parse(endTime));
                            // 获取ip地址 根据ip判断pv、uv
                            String visit_time = readLine.get(i).substring(timeIndex + 1, timeIndex + 9);
                            al.setVisitTime(parse.parse(dateStr + " " + visit_time));
                            int ipindex = readLine.get(i).indexOf("-");
                            String ipaddress = readLine.get(i).substring(0, ipindex - 1);
                            // 访问的商品的id有两位、三位，统一按三位截取，然后两位的去前后空格
                            String province = IpAddressUtils.getAddress("ip=" + ipaddress, "utf-8", "region");
                            al.setProvince(province);
                            al.setIp(ipaddress);
                            resultList.add(al);

                        }
                    }

                    if (readLine.size() < num)
                        break;
                }
                if (readLine.size() == 0)
                    break;
            }
            System.out.println(resultList.size());
            analysisResultService.inserts(resultList);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<String> readLine(String fileName, int num, long count) {
        List<String> list = new ArrayList<>();
        LineNumberReader reader = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            // RandomAccessFile raf = new RandomAccessFile(fileName);
            reader = new LineNumberReader(fileReader);
            if (count > 0) {
                reader.skip(count);
            }
            while (true) {
                String tempString = reader.readLine();
                if (StringUtils.isNotEmpty(tempString)) {
                    count += tempString.length() + 1;
                    list.add(tempString);
                }
                if (num == list.size()) {
                    list.add(count + "");
                    break;
                }
                if (StringUtils.isEmpty(tempString))
                    break;
            }
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println(new Date("01/Oct/2017:00:28:17"));
    }
}
