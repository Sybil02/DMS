package dms.workflow;

import common.ADFUtils;

import common.DmsUtils;

import dcm.DcmDataDisplayBean;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

public class WorkflowEditBean {
    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(WorkflowEditBean.class);
    //模板标签itemList
    private List<SelectItem> taskObjItemList = new ArrayList<SelectItem>();
    //接口itemlList
    private List<SelectItem> etlObjItemList = new ArrayList<SelectItem>();
    private Person curUser;
    public WorkflowEditBean() {
        super();
        this.curUser =(Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
        this.initObjItem();
    }

    public void setTaskObjItemList(List<SelectItem> taskObjItemList) {
        this.taskObjItemList = taskObjItemList;
    }

    public List<SelectItem> getTaskObjItemList() {
        return taskObjItemList;
    }

    private void initObjItem() {
        System.out.println("sssssssssssssssssssss");
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement state = trans.createStatement(DBTransaction.DEFAULT);
        try {
                //查询模板标签
               String sql = "select distinct template_label from dcm_template where template_label is not null and locale = '" 
                            +this.curUser.getLocale() + "'";
               System.out.println(sql);
               ResultSet rs = state.executeQuery(sql);
               while(rs.next()){
                    SelectItem item = new SelectItem();
                    item.setLabel(rs.getString("TEMPLATE_LABEL"));
                    item.setValue(rs.getString("TEMPLATE_LABEL"));
                    this.taskObjItemList.add(item);
               }
               rs.close();
                //查询接口
               String etlSql = "select id,scene_alias from odi11_scene where locale = '"+this.curUser.getLocale()+"'";
                ResultSet etlrs = state.executeQuery(etlSql);
                while(etlrs.next()){
                    SelectItem item = new SelectItem();
                    item.setLabel(etlrs.getString("SCENE_ALIAS"));
                    item.setValue(etlrs.getString("ID"));
                    this.etlObjItemList.add(item);
                }
               state.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    public void setEtlObjItemList(List<SelectItem> etlObjItemList) {
        this.etlObjItemList = etlObjItemList;
    }

    public List<SelectItem> getEtlObjItemList() {
        return etlObjItemList;
    }
}
