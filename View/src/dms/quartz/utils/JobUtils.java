package dms.quartz.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JobUtils {
    public JobUtils() {
        
    }
    
    private Connection getConn(String jndiName){
        DBConnUtils dbUtils = new DBConnUtils();
        Connection conn =
            dbUtils.getJNDIConnectionByContainer(jndiName);    
        return conn;
    }
    
    public void updateJobStatus(String jndiName,String jobId){
        try {
            Connection conn = this.getConn(jndiName);
            Statement stat = conn.createStatement();
            String sql = "UPDATE DMS_JOB_DETAILS T SET T.JOB_STATUS = 'D',T.END_TIME=SYSDATE WHERE T.JOB_ID = '" + jobId + "'";
            stat.executeUpdate(sql);
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
