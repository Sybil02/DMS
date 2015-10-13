package dcm;

import common.DmsUtils;

import common.ReplaceSpecialChar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.text.DecimalFormat;

import java.util.List;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.jbotester.load.SimpleDateFormatter;
import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;

import org.omg.Security.ReplaceSecurityServices;

public class Excel2007WriterImpl extends AbstractExcel2007Writer {
    private static ADFLogger logger=ADFLogger.createADFLogger(Excel2007WriterImpl.class);
    private String sql;
    private int dataStartLine;
    private List<ColumnDef> colsdef;
    public Excel2007WriterImpl(String sql,int dataStartLine,List<ColumnDef> colsdef) {
        this.sql=sql;
        this.dataStartLine=dataStartLine;
        this.colsdef=colsdef;
    }

    public void generate() {
        DBTransaction dbTransaction = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            dbTransaction =(DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
            stat =dbTransaction.createPreparedStatement(sql, -1);
            rs = stat.executeQuery();
            //电子表格开始
            beginSheet();
            insertRow(dataStartLine - 2);
            for (int i = 0; i < this.colsdef.size(); i++) {
                createCell(i, this.colsdef.get(i).getColumnLabel());
            }
            endRow();
            int n =dataStartLine  - 1;
            DecimalFormat dfm = new DecimalFormat();
            dfm.setMaximumFractionDigits(4);
            dfm.setGroupingUsed(false);
            ReplaceSpecialChar rsc = new ReplaceSpecialChar();
            while (rs.next()) {
                int colInx = 0;
                insertRow(n);
                for (ColumnDef col : this.colsdef) {
                    Object obj=rs.getObject(col.getDbTableCol());
                    if(obj instanceof java.sql.Date){
                        SimpleDateFormatter format=new SimpleDateFormatter("yyyy-MM-dd hh:mm:ss");
                        obj=format.format((java.sql.Date)obj);
                        createCell(colInx,(String)obj);
                    }else if(col.getDataType().equals("NUMBER")){
                        if(obj != null){
                            obj = dfm.format(Double.valueOf(obj.toString()));
                            createCell(colInx,Double.parseDouble(obj.toString()),-1);        
                        }else{
                            //数字列且为空的单元格 自定义为404
                            createCell(colInx,Double.valueOf(0),404);
                        }
                    }else{
                        obj=ObjectUtils.toString(obj);
                        obj = rsc.encodeString(obj.toString());
                        createCell(colInx,(String)obj);
                    }
                    ++colInx;
                }
                ++n;
                //结束行
                endRow();
            }
            //电子表格结束
            endSheet();
        } catch (Exception e) {
            this.logger.severe(e);
        }finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (stat != null)
                        stat.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
