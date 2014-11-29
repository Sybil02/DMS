package dcm.template;

import common.ADFUtils;

import dcm.DcmDataTableModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;

import java.util.Set;

import javax.faces.event.ActionEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTableUtils;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;

import oracle.jbo.ApplicationModule;
import oracle.jbo.AttributeDef;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.RowImpl;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

public class TemplateBackingBean {

    private String templateId;

    private String tablename;

    private List<Map<String, Object>> data;

    private List<String> columns;
    
    private static Set dcmRemainAttr=new HashSet();
    static{
        dcmRemainAttr.add("IDX");
        dcmRemainAttr.add("COM_RECORD_ID");
        dcmRemainAttr.add("CREATED_AT");
        dcmRemainAttr.add("UPDATED_AT");
        dcmRemainAttr.add("CREATED_BY");
        dcmRemainAttr.add("UPDATED_BY");
        dcmRemainAttr.add("ID");
    }

    public ViewObject getCombinationRecordView(ActionEvent actionEvent) {
        String combinationId = "";
        DCIteratorBinding tempIter =
            ADFUtils.findIterator("DcmTemplateView1Iterator");
        String template_Id =
            (String)tempIter.getCurrentRow().getAttribute("Id");
        DCIteratorBinding iter =
            ADFUtils.findIterator("DcmCombinationView1Iterator");
        Row combinationRow = iter.getCurrentRow();
        String tablename = (String)combinationRow.getAttribute("Code");
        ApplicationModule am =
            ADFUtils.getApplicationModuleForDataControl("DcmModuleDataControl");
        String sql =
            "select * from Dcm_Template_Combination t,DFDF t1 where t.template_id=:1 " +
            "and t1.id=t.combination_record_id";
        ViewObjectImpl vo =
            (ViewObjectImpl)am.createViewObjectFromQueryStmt("", sql);

        vo.setWhereClauseParam(0, template_Id);
        vo.executeQuery();
        AttributeDef attr[] = vo.getAttributeDefs();
        while (vo.hasNext()) {
            Row row = vo.next();

            for (int i = 0; i < attr.length; i++)
                row.getAttribute(attr[i].getName());
        }

        return vo;

    }


    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getTablename() {
        return tablename;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void createTemplate(ActionEvent actionEvent) throws SQLException {
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateView1Iterator").getViewObject();
        ViewObject columnVo =
            ADFUtils.findIterator("DcmTemplateColumnView1Iterator").getViewObject();
            for (Row templateRow:templateVo.getAllRowsInRange()) {
            ViewRowImpl template = (ViewRowImpl)templateRow;
            //如果是新增模版则从数据库里生成列信息
            if (template.getEntities()[0].getEntityState() == 0) {
                DBTransaction db =
                    (DBTransaction)columnVo.getApplicationModule().getTransaction();
                Statement stmt = db.createStatement(DBTransaction.DEFAULT);
                StringBuffer sql = new StringBuffer(200);
                sql.append("select upper(t.COLUMN_NAME) \"COL_NAME\",upper(t.DATA_TYPE) \"COL_TYPE\",t.COLUMN_ID \"COL_ID\" from user_tab_columns t");
                sql.append(" where upper(t.TABLE_NAME)=upper('").append(template.getAttribute("DbTable")).append("')");
                sql.append(" order by t.COLUMN_ID");
                ResultSet rs = stmt.executeQuery(sql.toString());
                while (rs.next()) {
                    if(dcmRemainAttr.contains(rs.getString("COL_NAME"))){
                        continue;
                    }
                    Row row = columnVo.createRow();
                    row.setAttribute("ColumnLabel", rs.getString("COL_NAME"));
                    row.setAttribute("DbTableCol", rs.getString("COL_NAME"));
                    row.setAttribute("Seq", rs.getString("COL_ID"));
                    String dataType=rs.getString("COL_TYPE");
                    if ("NUMBER".equals(dataType)) {
                        row.setAttribute("DataType","NUMBER");
                    } else if ("DATE".equals(dataType) ||
                               "TIMESTAMP".equals(dataType)) {
                        row.setAttribute("DataType", "DATE");
                    }else{
                        row.setAttribute("DataType","TEXT");
                    }
                    row.setAttribute("TemplateId", template.getAttribute("Id"));
                    columnVo.insertRow(row);
                }
                rs.close();
                stmt.close();           
                columnVo.getApplicationModule().getTransaction().commit();
            }
        }
    }
}
