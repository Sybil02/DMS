package dms.quartz.utils;

import common.ReplaceSpecialChar;

import dcm.ColumnDef;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.List;

import oracle.adf.share.logging.ADFLogger;

import org.apache.commons.lang.ObjectUtils;

public class QrzExcel2007WriterImpl extends QrzAbstractExcel2007Writer {
    private static ADFLogger logger=ADFLogger.createADFLogger(QrzExcel2007WriterImpl.class);
    private String sql;
    private String jndiName;
    private int dataStartLine;
    private List<ColumnDef> colsdef;
    public QrzExcel2007WriterImpl(String sql,String jndiName,int dataStartLine,List<ColumnDef> colsdef) {
        this.sql=sql;
        this.jndiName = jndiName;
        this.dataStartLine=dataStartLine;
        this.colsdef=colsdef;
    }

    public void generate() {
        DBConnUtils dbUtils = new DBConnUtils();
        Connection conn =
            dbUtils.getJNDIConnectionByContainer(jndiName);
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat =conn.prepareStatement(sql);
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
                    if(obj instanceof java.util.Date){
                        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");//"yyyy-MM-dd hh:mm:ss"
                        if(obj != null){
                            obj=format.format((java.util.Date)obj);
                        }
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
