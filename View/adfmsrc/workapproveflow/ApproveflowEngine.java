package workapproveflow;

import common.DmsUtils;

import common.MailSender;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

public class ApproveflowEngine {

    private static ADFLogger _logger =
        ADFLogger.createADFLogger(ApproveflowEngine.class);
    private Person curUser;

    public ApproveflowEngine() {
        super();
        this.curUser =
                (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    }
    //开启审批流当中一个部门，审批人
    //审批状态  未开启：CLOSE 开启：APPROVEING 通过：Y 拒绝：N 重审：R

    public void startApproveEntity(String runId, String templateId,
                                   String comId) throws AddressException,
                                                        MessagingException {
        String userId = "";
        String seq = "";
        DBTransaction db =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stmt = db.createStatement(DBTransaction.DEFAULT);
        //查询部门对应的审核人及顺序
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct t2.entity_id,t2.approval_status,t2.person_id,t1.seq from ");
        sql.append("dms_approvalflow_entitys t1,approve_template_status t2 ");
        sql.append("where t1.code = t2.entity_id and t1.user_id = t2.person_id and t1.locale = t2.locale ");
        sql.append("and t2.locale = '").append(this.curUser.getLocale()).append("' ");
        sql.append("and t2.run_id = '").append(runId).append("' ");
        sql.append("and t2.template_id = '").append(templateId).append("' ");
        sql.append("and t2.com_id = '").append(comId).append("' ");
        sql.append("order by t1.seq");
        try {
            ResultSet rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                if (rs.getString("APPROVAL_STATUS").equals("CLOSE") ||
                    rs.getString("APPROVAL_STATUS").equals("R")) {
                    userId = rs.getString("PERSON_ID");
                    seq = rs.getString("SEQ");
                    break;
                } else {
                    this._logger.severe("部门启动第一个审批人异常");
                }
            }
            rs.close();
            if (userId != "") {
                //打开第一个审批
                StringBuffer openSql = new StringBuffer();
                openSql.append("UPDATE APPROVE_TEMPLATE_STATUS SET APPROVAL_STATUS = 'APPROVEING' ");
                openSql.append("WHERE RUN_ID = '").append(runId).append("' ");
                openSql.append("AND TEMPLATE_ID = '").append(templateId).append("' ");
                openSql.append("AND COM_ID = '").append(comId).append("' ");
                openSql.append("AND PERSON_ID = '").append(userId).append("' ");
                stmt.executeUpdate(openSql.toString());
                db.commit();
                //发送邮件
                this.sendMail(userId, comId, "这是一封测试邮件！");
//                StringBuffer mailSql = new StringBuffer();
//                mailSql.append("SELECT MAIL FROM DMS_USER WHERE ID = '").append(userId).append("'");
//                ResultSet mailRs = stmt.executeQuery(mailSql.toString());
//                if (mailRs.next()) {
//                    MailSender sender = new MailSender();
//                    sender.send(mailRs.getString("MAIL"), comId + "审核",
//                                "测试邮件"); //收件人地址、主题、消息
//                }
//                mailRs.close();
            }
            stmt.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //发送邮件
    public void sendMail(String userId,String subject,String context){
        DBTransaction db =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stmt = db.createStatement(DBTransaction.DEFAULT);
        //发送邮件
        StringBuffer mailSql = new StringBuffer();
        mailSql.append("SELECT MAIL FROM DMS_USER WHERE ID = '").append(userId).append("'");
        try{
        ResultSet mailRs = stmt.executeQuery(mailSql.toString());
        if (mailRs.next()) {
            MailSender sender = new MailSender();
            sender.send(mailRs.getString("MAIL"), subject + "审核",context); //收件人地址、主题、消息
        }
        mailRs.close();
        stmt.close();
        }catch(Exception e){
            this._logger.severe(e);
        }
    }
    //审批人审批

//    public void nextApproveEntity(String runId, String templateId,
//                                  String entityId, int stepId, String personId,
//                                  String approvalStatus,
//                                  String comdId) throws AddressException,
//                                                        MessagingException {
//        DBTransaction db =
//            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
//        Statement stmt = db.createStatement(DBTransaction.DEFAULT);
//        StringBuffer sql = new StringBuffer();
//        sql.append("update APPROVE_TEMPLATE_STATUS set APPROVAL_STATUS = \'");
//        sql.append(approvalStatus).append("\' where run_id = \'");
//        sql.append(runId).append("\' and TEMPLATE_ID = \'");
//        sql.append(templateId).append("\' and ENTITY_ID = \'");
//        sql.append("\' and STEP_NO = ");
//        sql.append(stepId);
//        sql.append(" person_id = \'").append(personId).append("\'");
//        try {
//            stmt.executeUpdate(sql.toString());
//            if (approvalStatus.equals("Y")) {
//                //startApproveEntity(runId,templateId,entityId,stepId);
//            }
//            if (approvalStatus.equals("N")) {
//                StringBuffer usql = new StringBuffer();
//                usql.append("select c.user_id \"userId\" from dms_approvalflow_entitys c where c.seq <( select b.seq  from dms_approvalflowinfo a, dms_approvalflow_entitys b where a.template_Id = \'");
//                usql.append(templateId).append("\' a.id = b.APPROVAL_ID and a.locale = b.locale and a.locale = \'");
//                usql.append(curUser.getLocale()).append("\' ");
//                usql.append("and b.user_id = \'");
//                usql.append(personId).append("\')");
//                usql.append(" and c.locale = \'");
//                usql.append(curUser.getLocale()).append("\' ");
//                ResultSet rs = stmt.executeQuery(sql.toString());
//                while (rs.next()) { //修改审批状态
//                    StringBuffer sqltemp = new StringBuffer();
//                    sqltemp.append("update APPROVE_TEMPLATE_STATUS t set APPROVAL_STATUS = 'R' where run_id = \'");
//                    sqltemp.append(runId).append("\' and TEMPLATE_ID = \'");
//                    sqltemp.append(templateId).append("\' and ENTITY_ID = \'");
//                    sqltemp.append("\' and STEP_NO = ");
//                    sqltemp.append(stepId);
//                    sqltemp.append(" and person_id = \'");
//                    sqltemp.append(rs.getString("userId")).append("\'");
//                    stmt.executeUpdate(sqltemp.toString());
//                }
//                //打开组合，dcm_template_combi ，template_id，com_id 修改状态为OPEN
//                StringBuffer sqlcomb = new StringBuffer();
//                sqlcomb.append("update dcm_template_combination d set d.status = \'OPEN\' where TEMPLATE_ID = \'");
//                sqlcomb.append(templateId).append("\'");
//                sqlcomb.append(" and COM_RECORD_ID = \'");
//                sqlcomb.append(comdId).append("\'");
//                stmt.executeUpdate(sqlcomb.toString());
//
//
//            }
//        } catch (SQLException e) {
//            this._logger.severe(e);
//        }
//    }
    
    //启动下一个审批人
    public void nextApproveUser(String runId, String templateId, String comId){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询部门对应的审核状态为未审核的审核人及顺序
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct t2.id,t2.entity_id,t2.approval_status,t2.person_id,t1.seq from ");
        sql.append("dms_approvalflow_entitys t1,approve_template_status t2 ");
        sql.append("where t1.code = t2.entity_id and t1.user_id = t2.person_id and t1.locale = t2.locale ");
        sql.append("and t2.approval_status = 'CLOSE' ");
        sql.append("and t2.locale = '").append(this.curUser.getLocale()).append("' ");
        sql.append("and t2.run_id = '").append(runId).append("' ");
        sql.append("and t2.template_id = '").append(templateId).append("' ");
        sql.append("and t2.com_id = '").append(comId).append("' ");
        sql.append("order by t1.seq");
        try {
            String nextUserId = "";
            ResultSet rs = stat.executeQuery(sql.toString());
            if(rs.next()){
                //存在下一审批人,update status
                nextUserId = rs.getString("person_id");
                StringBuffer nextSql = new StringBuffer();
                nextSql.append("UPDATE APPROVE_TEMPLATE_STATUS SET APPROVAL_STATUS = 'APPROVEING' ");
                nextSql.append("WHERE RUN_ID = '").append(runId).append("' ");
                nextSql.append("AND TEMPLATE_ID = '").append(templateId).append("' ");
                nextSql.append("AND COM_ID = '").append(comId).append("' ");
                nextSql.append("AND PERSON_ID = '").append(nextUserId).append("'");
                stat.executeUpdate(nextSql.toString());
                trans.commit();
                //send mail to user
                this.sendMail(nextUserId, comId, "你是下一个审批人！");
            }else{
                //不存在下一个审批人，部门审批结束。
                
            }
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //审批通过
    public void approvePass(String runId, String templateId, String comId,
                            String userId) {
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //改变审批状态表状态
        StringBuffer uaSql = new StringBuffer();
        uaSql.append("UPDATE APPROVE_TEMPLATE_STATUS SET APPROVAL_STATUS = 'Y' ");
        uaSql.append("WHERE RUN_ID = '").append(runId).append("' ");
        uaSql.append("AND TEMPLATE_ID = '").append(templateId).append("' ");
        uaSql.append("AND COM_ID = '").append(comId).append("' ");
        uaSql.append("AND PERSON_ID = '").append(userId).append("' ");
        try {
            stat.executeUpdate(uaSql.toString());
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        //启动下一个审批人
        this.nextApproveUser(runId, templateId, comId);
    }
    
    //审批拒绝
    public void approveRefuse(String runId, String templateId, String comId,
                            String userId){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //修改部门所有审批人状态为CLOSE
        StringBuffer uaSql = new StringBuffer();
        uaSql.append("UPDATE APPROVE_TEMPLATE_STATUS SET APPROVAL_STATUS = 'CLOSE' ");
        uaSql.append("WHERE RUN_ID = '").append(runId).append("' ");
        uaSql.append("AND TEMPLATE_ID = '").append(templateId).append("' ");
        uaSql.append("AND COM_ID = '").append(comId).append("' ");
        //修改组合打开
        StringBuffer comSql = new StringBuffer();
        comSql.append("UPDATE DCM_TEMPLATE_COMBINATION SET STATUS = 'OPEN' ");
        comSql.append("WHERE TEMPLATE_ID = '").append(templateId).append("' ");
        comSql.append("AND COM_RECORD_ID = '").append(comId).append("'");
        //修改模板输入状态表
        StringBuffer wsSql = new StringBuffer();
        wsSql.append("UPDATE WORKFLOW_TEMPLATE_STATUS SET WRITE_STATUS = 'N' ");
        wsSql.append("WHERE RUN_ID = '").append(runId).append("' ");
        wsSql.append("AND TEMPLATE_ID = '").append(templateId).append("' ");
        wsSql.append("AND COM_ID = '").append(comId).append("'");
        try {
            stat.executeUpdate(uaSql.toString());
            stat.executeUpdate(comSql.toString());
            stat.executeUpdate(wsSql.toString());
            trans.commit();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
}
