package common;

import dms.quartz.core.QuartzSchedulerSingleton;

import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

public class AppInitListener implements ServletContextListener{
    public AppInitListener() {
        super();
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println(".......................................................");
        System.out.println("....................Application Start..................");
        System.out.println(".......................................................");
        
        this.resumeAllJob();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("......................................................");
        System.out.println("....................Application Stop..................");
        System.out.println("......................................................");
        
        this.pauseAllJob();
    }
    
    
    public void resumeAllJob(){
        QuartzSchedulerSingleton qss = QuartzSchedulerSingleton.getInstance();
        Scheduler _scheduler = qss.getScheduler();
        try {
            _scheduler.resumeAll();
            System.out.println("*******************恢复系统所有定时任务成功*****************");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void pauseAllJob(){
        QuartzSchedulerSingleton qss = QuartzSchedulerSingleton.getInstance();
        Scheduler _scheduler = qss.getScheduler();
        try {
            _scheduler.pauseAll();
            System.out.println("*******************暂停系统所有定时任务成功*****************");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
