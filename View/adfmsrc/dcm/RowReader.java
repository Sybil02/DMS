package dcm;

import common.ReplaceSpecialChar;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.List;
import java.util.TreeMap;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.hexj.excelhandler.reader.IRowReader;

public class RowReader implements IRowReader {
    private int startLine;
    private String templateId;
    private String combinationRecord;
    private String temptable;
    private PreparedStatement stmt;
    private List<ColumnDef> colsdef;
    private String operator;
    private DBTransaction trans;
    private int n = 0;
    private static final int batchSize = 5000;
    private String templateName;
    private DecimalFormat dfm;
    private static ADFLogger logger=ADFLogger.createADFLogger(RowReader.class);
    public RowReader(DBTransaction trans, int startLine, String templateId,
                     String combinationRecord, String temptable,
                     List<ColumnDef> colsdef, String operator,String templateName) {
        this.startLine = startLine;
        this.templateId = templateId;
        this.combinationRecord = combinationRecord;
        this.temptable = temptable;
        this.colsdef = colsdef;
        this.operator = operator;
        this.trans = trans;
        this.templateName=templateName;
        dfm = new DecimalFormat();
        dfm.setMaximumFractionDigits(4);
        dfm.setGroupingUsed(false);
        this.prepareSqlStatement();
    }

    private void prepareSqlStatement() {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO \"").append(this.temptable.toUpperCase()).append("\"(");
        sql.append("TEMPLATE_ID,COM_RECORD_ID,SHEET_NAME,ROW_NO,CREATED_BY,UPDATED_BY,UPDATED_AT,CREATED_AT");
        for (int i = 1; i <= this.colsdef.size(); i++) {
            sql.append(",COLUMN").append(i);
        }
        sql.append(") VALUES('").append(this.templateId).append("'");
        sql.append(",'").append(this.combinationRecord).append("'");
        sql.append(",?,?").append(",'").append(this.operator).append("'").append(",'").append(this.operator).append("'");
        sql.append(",sysdate,sysdate");
        for (int i = 1; i <= this.colsdef.size(); i++) {
            sql.append(",?");
        }
        sql.append(")");
        stmt = this.trans.createPreparedStatement(sql.toString(), 0);
    }

    public void getRows(int sheetIndex, String sheetName, int curRow,
                        TreeMap<Integer, String> rowlist) {
        ReplaceSpecialChar rsc = new ReplaceSpecialChar();
        if (curRow >= this.startLine - 1&&sheetName.startsWith(this.templateName)) {
            boolean isEpty = true;
            try {
                this.stmt.setString(1, sheetName);
                this.stmt.setInt(2, curRow + 1);
                for (int i = 0; i < this.colsdef.size(); i++) {
                    
                    String tmpstr = rowlist.get(i);
                    if(this.colsdef.get(i).getDataType().equals("NUMBER")){
                        if (null == tmpstr || "".equals(tmpstr.trim())) {
                            this.stmt.setString(i + 3, "");
                        } else {
                            isEpty = false;
                            try{
                                this.stmt.setString(i + 3, dfm.format(Double.parseDouble(tmpstr.trim())));  
                            }catch(NumberFormatException ex){
                                //数字列出现非数值时，不报错，直接将值导入临时表，在校验程序部分再处理。
                                this.stmt.setString(i + 3, tmpstr.trim());
                            }
                        }
                    }else{
                        if (null == tmpstr || "".equals(tmpstr.trim())) {
                            this.stmt.setString(i + 3, "");
                        } else {
                            isEpty = false;
                            this.stmt.setString(i + 3, rsc.decodeString(tmpstr.trim()));        
                        }
                    }
                    
                }
                if (!isEpty) {
                    this.stmt.addBatch();
                }
                if ((this.n + 1) % this.batchSize == 0) {
                    this.stmt.executeBatch();
                    this.trans.commit();
                }
                this.n += 1;
            } catch (Exception e) {
                this.logger.severe(e);
            }
        }
    }

    public void close() {
        try {
            this.stmt.executeBatch();
            this.stmt.close();
            this.trans.commit();
        } catch (SQLException e) {
            this.logger.severe(e);
        }
    }
}
