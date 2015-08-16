package workapproveflow;

import common.DmsUtils;

import common.MailSender;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

public class ApproveflowEngine {
    
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(ApproveflowEngine.class);
    private Person curUser;
    public ApproveflowEngine() {
        super();
    }
        //开启审批流当中一个部门，审批人
        public void startApproveEntity(String runId,String templateId,String entityId,int stepId) throws AddressException,
                                             MessagingException{
            DBTransaction db =
                (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stmt = db.createStatement(DBTransaction.DEFAULT);
            StringBuffer sql = new StringBuffer();
            sql.append("select b.user_id \"uesrid\",d.mail \"mail\",b.seq \"seq\",c.APPROVAL_STATUS \"status\" " +
                "from dms_approvalflowinfo a, dms_approvalflow_entitys b,APPROVE_TEMPLATE_STATUS c,dms_user d " +
                "where a.id = b.approval_id and b.user_id = c.person_id and b.user_id = d.id  and a.template_id = \'");
            sql.append(templateId);
            sql.append("\' and b.code = \'");
            sql.append(entityId);
            sql.append("\' and a.locale = b.locale and a.locale = c.locale and a.locale = \'");
            sql.append(curUser.getLocale()).append("\' order by b.seq");
            try{
                ResultSet rs = stmt.executeQuery(sql.toString());
                while(rs.next()){
                    if(rs.getString("status") == null||rs.getString("status").equals("R")||rs.getString("status").equals("N")){
                        MailSender sender=new MailSender();
                        sender.send(rs.getString("mail"), "", "");//邮箱地址、主题、消息
                        StringBuffer sqlstatus = new StringBuffer();
                        sqlstatus.append("update APPROVE_TEMPLATE_STATUS set APPROVAL_STATUS = \'approving");
                        sqlstatus.append("\' where run_id = \'");
                        sqlstatus.append(runId).append("\' and TEMPLATE_ID = \'");
                        sqlstatus.append(templateId).append("\' and ENTITY_ID = \'");
                        sqlstatus.append(entityId).append("\' and STEP_NO = ");
                        sqlstatus.append(stepId);
                        sqlstatus.append(" PERSON_ID = \'").append(rs.getString("uesrid"));
                        sqlstatus.append("\'");
                        stmt.executeUpdate(sqlstatus.toString());
                        db.commit();
                        break;
                    }
                    rs.close();
                    stmt.close();
                }
            }catch (SQLException e) {
                this._logger.severe(e);
            }
            
           // String sql = "select *";
        }
        //审批人审批
        public void nextApproveEntity(String runId,String templateId,String entityId,int stepId,String personId,String approvalStatus,String comdId) throws AddressException,
                                                                MessagingException {
            DBTransaction db =
                (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stmt = db.createStatement(DBTransaction.DEFAULT);
            StringBuffer sql = new StringBuffer();
            sql.append("update APPROVE_TEMPLATE_STATUS set APPROVAL_STATUS = \'");
            sql.append(approvalStatus).append("\' where run_id = \'");
              sql.append(runId).append("\' and TEMPLATE_ID = \'");
              sql.append(templateId).append("\' and ENTITY_ID = \'");
              sql.append("\' and STEP_NO = ");
              sql.append(stepId);
              sql.append(" person_id = \'").append(personId).append("\'");
      try {
            stmt.executeUpdate(sql.toString());
                     if(approvalStatus.equals("Y")){
             startApproveEntity(runId,templateId,entityId,stepId);
            }
          if(approvalStatus.equals("N")){
              StringBuffer  usql = new StringBuffer();
              usql.append("select c.user_id \"userId\" from dms_approvalflow_entitys c where c.seq <( select b.seq  from dms_approvalflowinfo a, dms_approvalflow_entitys b where a.template_Id = \'");
              usql.append(templateId).append("\' a.id = b.APPROVAL_ID and a.locale = b.locale and a.locale = \'");
              usql.append(curUser.getLocale()).append("\' ");
              usql.append("and b.user_id = \'");
              usql.append(personId).append("\')");
              usql.append(" and c.locale = \'");
              usql.append(curUser.getLocale()).append("\' ");
              ResultSet rs = stmt.executeQuery(sql.toString());
              while(rs.next()){//修改审批状态
                  StringBuffer sqltemp = new StringBuffer();
                  sqltemp.append("update APPROVE_TEMPLATE_STATUS t set APPROVAL_STATUS = 'R' where run_id = \'");
                  sqltemp.append(runId).append("\' and TEMPLATE_ID = \'");
                  sqltemp.append(templateId).append("\' and ENTITY_ID = \'");
                  sqltemp.append("\' and STEP_NO = ");
                  sqltemp.append(stepId);
                  sqltemp.append(" and person_id = \'");
                  sqltemp.append(rs.getString("userId")).append("\'");
                  stmt.executeUpdate(sqltemp.toString());
              }
              //打开组合，dcm_template_combi ，template_id，com_id 修改状态为OPEN
              StringBuffer sqlcomb = new StringBuffer();
              sqlcomb.append("update dcm_template_combination d set d.status = \'OPEN\' where TEMPLATE_ID = \'");
              sqlcomb.append(templateId).append("\'");
              sqlcomb.append(" and COM_RECORD_ID = \'");
              sqlcomb.append(comdId).append("\'");
              stmt.executeUpdate(sqlcomb.toString());
             
              
              
          }
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
}
