package my.study.clean;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import my.study.loalbalance.InvokeStatisticsCenter;
import my.study.pojo.ServerInfo;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @Description:
 * @Author: 长灵
 * @Date: 2020-06-15 16:12
 */
@Component
public class Cleaner {


    /**
     * 启动线程定时清理
     */
    @Scheduled(cron = "*/1 * * * * ?")
    public void clean(){
        Date currentTime = new Date();
        for (Map.Entry<ServerInfo, Pair<Boolean, Long>> thisEntrySet : InvokeStatisticsCenter.statisticsMap.entrySet()) {
            Pair<Boolean, Long> pair = thisEntrySet.getValue();
            if(pair.getKey()){
                continue;
            }

            DateTime offsetTime = DateUtil.offsetMinute(new Date(pair.getValue()), 5);

            if(currentTime.after(offsetTime)){
                InvokeStatisticsCenter.statisticsMap.put(thisEntrySet.getKey(),new Pair<>(true,0L));
            }
        }
    }
}
