package dcm;

import common.DmsUtils;

import common.ReplaceSpecialChar;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.List;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;

public class PcExcel2007WriterImpl extends AbstractExcel2007Writer{
    private static ADFLogger logger=ADFLogger.createADFLogger(Excel2007WriterImpl.class);
    private String sql;
    private int dataStartLine;
    private List<PcColumnDef> colsdef;
    
    public PcExcel2007WriterImpl(String sql,int dataStartLine,List<PcColumnDef> colsdef) {
        this.sql = sql;
        this.dataStartLine = dataStartLine;
        this.colsdef = colsdef;
    }

    public void generate() {
        DBTransaction dbTransaction = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            dbTransaction = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
            stat = dbTransaction.createPreparedStatement(sql, -1);
            rs = stat.executeQuery();
            //电子表格开始
            beginSheet();
            insertRow(dataStartLine - 2);
            for(int i =0;i<this.colsdef.size();i++){
                if("WORK".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"作业活动");
                }else if("TERM".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"预算项");
                }else if("CENTER".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"工作中心");
                }else if("WORK_TYPE".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"作业类型");
                }else if("BOM_CODE".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"物料编码");
                }else if("UNIT".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"单位");
                }else if("PLAN_COST".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"计划成本");
                }else if("OCCURRED".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"已发生");
                }else if("LGF_NUM".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"料工费数量");
                }else if("LGF_TYPE".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"料工费类型");
                }else if("PLAN_QUANTITY".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"计划数量");
                }else if("PLAN_AMOUNT".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"计划金额");
                }else if("OCCURRED_QUANTITY".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"已发生数量");
                }else if("OCCURRED_AMOUNT".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"已发生金额");
                }else if("SUM_AFTER_JUL".equals(this.colsdef.get(i).getColumnLable())){
                    createCell(i,"下年7月以后");
                }else{
                    createCell(i,this.colsdef.get(i).getColumnLable());
                }
                
            }
            endRow();
            int n = dataStartLine - 1;
            DecimalFormat dfm = new DecimalFormat();
            dfm.setMaximumFractionDigits(4);
            dfm.setGroupingUsed(false);
            ReplaceSpecialChar rsc = new ReplaceSpecialChar();
            while(rs.next()){
                int colInx = 0;
                insertRow(n);
                for(PcColumnDef col : this.colsdef){
                    Object obj = rs.getString(col.getDbTableCol());
                    obj = ObjectUtils.toString(obj);
                    obj = rsc.encodeString(obj.toString());
                    createCell(colInx,(String)obj);
                    ++colInx;
                }
                ++n;
                endRow();
            }
            endSheet();
        } catch (Exception e) {
            this.logger.severe(e);
            }finally{
                try{
                    if(rs!=null){
                        rs.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
    }
}
