package mailTimer;

import common.ADFUtils;
import common.DmsUtils;

import common.JSFUtils;

import dms.quartz.core.QuartzSchedulerSingleton;

import javax.faces.event.ActionEvent;

import oracle.adf.share.ADFContext;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import oracle.jbo.server.ViewRowImpl;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

public class TempTimerBean {

    private QuartzSchedulerSingleton qss;
    private Scheduler _scheduler;

    public TempTimerBean() {
        if (_scheduler == null) {
            this.qss = QuartzSchedulerSingleton.getInstance();
            this._scheduler = qss.getScheduler();
        }
    }

    public void saveAndModify(ActionEvent actionEvent) {
        ViewObject timerVo =
            ADFUtils.findIterator("DmsTemplateTimerVOIterator").getViewObject();
        
        for(Row tRow : timerVo.getAllRowsInRange()){
            if(tRow.getAttribute("TempId") == null || tRow.getAttribute("TimerCron") == null){
                JSFUtils.addFacesErrorMessage("模板名称或Cron表达式不能为空！");
                return;
            }
        }
        
        for (Row tRow : timerVo.getAllRowsInRange()) {
            ViewRowImpl row = (ViewRowImpl)tRow;
            if (row.getEntities()[0].getEntityState() == 0) {
                row.setAttribute("JobName",
                                 "MAIL-" + java.util.UUID.randomUUID().toString().replace("-",
                                                                                          ""));
                //0：新增   新建调度作业
                this.createJob(row);
            } else if (row.getEntities()[0].getEntityState() == 2) {
                //2: 修改   修改作业tigger
                this.modifyJob(row);
            }
        }
        //保存数据
        DmsUtils.getDmsApplicationModule().getTransaction().commit();
    }

    public void deleteTimer(ActionEvent actionEvent) {
        ViewObject timerVo =
            ADFUtils.findIterator("DmsTemplateTimerVOIterator").getViewObject();
        Row row = timerVo.getCurrentRow();

        if (row == null) {
            return;
        }

        //删除作业
        this.deleteJob(row);
        //保存到数据库
        row.remove();
        timerVo.getApplicationModule().getTransaction().commit();
    }


    public void createJob(Row row) {

        System.out.println("new job......" + row.getAttribute("MailContent"));

        String jobName = row.getAttribute("JobName").toString();

        Class className;
        try {
            className = Class.forName("dms.quartz.job.TimerMailJob");
            //create job
            JobDetail jobDetail =
                JobBuilder.newJob(className).withIdentity(jobName).build();
            //dataMap
            jobDetail.getJobDataMap().put("jobName",jobName);
            jobDetail.getJobDataMap().put("locale",ADFContext.getCurrent().getLocale().toString());
            //create tigger
            CronScheduleBuilder cronBuilder =
                CronScheduleBuilder.cronSchedule(row.getAttribute("TimerCron").toString());
            CronTrigger trigger =
                TriggerBuilder.newTrigger().withIdentity(jobName).withSchedule(cronBuilder).build();
            this._scheduler.scheduleJob(jobDetail, trigger);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    public void modifyJob(Row row) {
        System.out.println("Modify job......." + row.getAttribute("MailContent"));
        String jobName = row.getAttribute("JobName").toString();
        String timerCron = row.getAttribute("TimerCron").toString();
        
        TriggerKey tKey = TriggerKey.triggerKey(jobName);
        CronTrigger ct;
        try {
            ct = (CronTrigger)this._scheduler.getTrigger(tKey);
            if(!ct.getCronExpression().equals(timerCron)){
                //更改时间    
                CronTrigger newTrigger = TriggerBuilder.newTrigger().withIdentity(jobName).withSchedule(CronScheduleBuilder.cronSchedule(timerCron)).build();
                this._scheduler.rescheduleJob(tKey, newTrigger);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void deleteJob(Row row) {
        System.out.println("Delete job......." + row.getAttribute("MailContent"));
        String jobName = row.getAttribute("JobName").toString();
        JobKey key = JobKey.jobKey(jobName);
        TriggerKey tKey = TriggerKey.triggerKey(jobName);
        try {
            this._scheduler.pauseTrigger(tKey);
            this._scheduler.unscheduleJob(tKey);
            this._scheduler.deleteJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
