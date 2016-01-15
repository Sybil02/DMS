package dms.quartz.job;

import dms.quartz.core.QuartzSchedulerSingleton;
import dms.quartz.utils.RowReader;

import dms.quartz.utils.DBConnUtils;

import java.io.Serializable;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import org.hexj.excelhandler.reader.ExcelReaderUtil;
import dms.quartz.core.QuartzSchedulerSingleton;

import java.util.Date;

public class DBJob implements Job, Serializable {
    private static final long serialVersionUID = 1L;

    public DBJob() {
        super();
    }

    public void execute(JobExecutionContext context) {


        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();


        String connType = jobDataMap.getString("connType");


        if ("jdbcURL".equals(connType)) {

            String hostName = jobDataMap.getString("hostName");
            String port = jobDataMap.getString("port");
            String sid = jobDataMap.getString("sid");
            String username = jobDataMap.getString("username");
            String pwd = jobDataMap.getString("pwd");
            String jobName = jobDataMap.getString("jobName");
            String jobGroup = jobDataMap.getString("jobGroup");
            
            DBConnUtils dbUtils = new DBConnUtils();

            try {
                Connection conn =
                    dbUtils.getJDBCConn(hostName, port, sid, username, pwd);
                System.out.println("ssss:"+conn);
                //start by wtg

                RowReader reader = new RowReader(conn,2,"tempexcel","testC","DCM_TEMPTABLE10",4,"wtg","TestTemplate");
                ExcelReaderUtil.readExcel(reader, "C:\\Users\\wtg\\AppData\\Roaming\\JDeveloper\\system11.1.1.7.40.64.93\\DefaultDomain\\DMS\\UPLOAD\\模板部门维护\\模板部门维护_Admin_20160114115345.xlsx", true);
                reader.close();
                conn.commit();
                CallableStatement cs = conn.prepareCall("{CALl DCM_COMMON.HANDLE_PROGRAM(?,?,?,?,?)}");
                cs.setString(1, "2d105329f3b54799803aed38b69e8617");
                cs.setString(2, null);
                cs.setString(3, "wtg");
                cs.setString(4, "INCREMENT");
                cs.setString(5, "zh_CN");
                cs.execute();
                
                //end by wtg
                conn.close();
                long i = context.getJobRunTime();
                Date date = new Date(i);
                System.out.println("RunTime:"+date);
                context.getScheduler().unscheduleJob(context.getTrigger().getKey());
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("DBJob 数据库存储过程调用失败，请检查jdbc信息!");
            }

        } else if ("jdbcDS".equals(connType)) {

            String jndiName = jobDataMap.getString("jndiName");
            String plsql2 = jobDataMap.getString("plsql2");

            DBConnUtils dbUtils = new DBConnUtils();

            try {

                Connection conn =
                    dbUtils.getJNDIConnectionByContainer(jndiName);

                CallableStatement stmt = conn.prepareCall(plsql2);
                stmt.execute();
                conn.close();

            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("DBJob 数据库存储过程调用失败，请检查jdbc DS信息!");
            }

        }


    }
}
