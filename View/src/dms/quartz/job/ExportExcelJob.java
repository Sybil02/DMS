package dms.quartz.job;

import dcm.ColumnDef;

import dms.quartz.utils.DBConnUtils;

import dms.quartz.utils.JobUtils;
import dms.quartz.utils.QrzExcel2007WriterImpl;

import java.io.Serializable;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class ExportExcelJob implements Job, Serializable{
    private static final long serialVersionUID = 1L;
    public ExportExcelJob() {
        super();
    }

    public void execute(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        
        String jobId = jobDataMap.getString("jobName");
        String jndiName = jobDataMap.getString("jndiName");
        String querySql = jobDataMap.getString("querySql");
        String tempId = jobDataMap.getString("tempId");
        String fileName = jobDataMap.getString("fileName");
        String sheetName = jobDataMap.getString("sheetName");
        int startLine = Integer.parseInt(jobDataMap.getString("startLine"));
        
        List<ColumnDef> colDefList = this.getColList(tempId,jndiName); 
        //导出Excel
        QrzExcel2007WriterImpl writer = new QrzExcel2007WriterImpl(querySql,jndiName,startLine,colDefList);
        try {
            writer.process(fileName, sheetName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JobUtils jobUtils = new JobUtils();
        jobUtils.updateJobStatus(jndiName, jobId);
    }
    
    public List<ColumnDef> getColList(String tempId,String jndiName){
        DBConnUtils dbUtils = new DBConnUtils();
        Connection conn =
            dbUtils.getJNDIConnectionByContainer(jndiName);
        
        String sql = "SELECT T.COLUMN_LABEL,T.DB_TABLE_COL,T.IS_PK,T.READONLY,T.DATA_TYPE,T.VISIBLE,T.VALUE_SET_ID "
            +"FROM DCM_TEMPLATE_COLUMN T WHERE T.LOCALE='zh_CN' AND T.TEMPLATE_ID = '" + tempId +"' ORDER BY T.SEQ";
        
        List<ColumnDef> colDefList = new ArrayList<ColumnDef>();
        Statement stat = null;
        ResultSet rs = null;
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            
            while(rs.next()){
                ColumnDef colDef = new ColumnDef(rs.getString("COLUMN_LABEL"), rs.getString("DB_TABLE_COL"), rs.getString("IS_PK"),
                     rs.getString("READONLY"), rs.getString("DATA_TYPE"), rs.getString("VISIBLE"),rs.getString("VALUE_SET_ID"));  
                colDefList.add(colDef);
            }
            rs.close();
            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return colDefList; 
    }
}
