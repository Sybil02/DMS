package dgpt;

import common.DmsUtils;
import common.ReplaceSpecialChar;

import dcm.AbstractExcel2007Writer;
import dcm.ColumnDef;
import dcm.Excel2007WriterImpl;

import dcm.PcColumnDef;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.List;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;

public class XmdbExcel2007Writer extends AbstractExcel2007Writer{

    private static ADFLogger logger=ADFLogger.createADFLogger(XmdbExcel2007Writer.class);
    private String sql;
    private int dataStartLine;
    private List<PcColumnDef> colsdef;
    public XmdbExcel2007Writer(String sql,int dataStartLine,List<PcColumnDef> colsdef) {
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
            System.out.println(sql);
            rs = stat.executeQuery();
            //电子表格开始
            beginSheet();
            insertRow(dataStartLine - 2);
            for (int i = 0; i < this.colsdef.size(); i++) {
                createCell(i, this.colsdef.get(i).getColumnLable());
            }
            endRow();
            int n =dataStartLine  - 1;
            DecimalFormat dfm = new DecimalFormat();
            dfm.setMaximumFractionDigits(4);
            dfm.setGroupingUsed(false);
            while (rs.next()) {
                int colInx = 0;
                insertRow(n);
                for (PcColumnDef col : this.colsdef) {            
                    Object obj=rs.getObject(colInx+1);
                    createCell(colInx,ObjectUtils.toString(obj));
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
