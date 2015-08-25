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
                //检查父节点是否全部审批通过，全部通过则启动下一个个步骤，否则不操作
                
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
        //查询模板输入步骤编码
        int stepNo = 0;
        StringBuffer snSql = new StringBuffer();
        snSql.append("SELECT DISTINCT STEP_NO FROM WORKFLOW_TEMPLATE_STATUS WHERE ");
        snSql.append("RUN_ID = '").append(runId).append("' ");
        snSql.append("AND TEMPLATE_ID = '").append(templateId).append("'");
        try {
            ResultSet rs = stat.executeQuery(snSql.toString());
            if(rs.next()){
                stepNo = rs.getInt("STEP_NO");        
            }
            rs.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        //修改工作流状态表输入步骤状态
        StringBuffer stepSql = new StringBuffer();
        stepSql.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING' ");
        stepSql.append("WHERE RUN_ID = '").append(runId).append("' ");
        stepSql.append("AND STEP_NO = ").append(stepNo);
        try {
            stat.executeUpdate(uaSql.toString());
            stat.executeUpdate(comSql.toString());
            stat.executeUpdate(wsSql.toString());
            stat.executeUpdate(stepSql.toString());
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
}
