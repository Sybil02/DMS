package dcm;

import java.sql.PreparedStatement;

import java.sql.SQLException;

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
    private int columnSize;
    private String operator;
    private DBTransaction trans;
    private int n = 0;
    private static final int batchSize = 5000;
    private String templateName;
    private static ADFLogger logger=ADFLogger.createADFLogger(RowReader.class);
    public RowReader(DBTransaction trans, int startLine, String templateId,
                     String combinationRecord, String temptable,
                     int columnSize, String operator,String templateName) {
        this.startLine = startLine;
        this.templateId = templateId;
        this.combinationRecord = combinationRecord;
        this.temptable = temptable;
        this.columnSize = columnSize;
        this.operator = operator;
        this.trans = trans;
        this.templateName=templateName;
        this.prepareSqlStatement();
    }

    private void prepareSqlStatement() {
        StringBuffer sql = new StringBuffer(1000);
        sql.append("INSERT INTO \"").append(this.temptable.toUpperCase()).append("\"(");
        sql.append("TEMPLATE_ID,COM_RECORD_ID,SHEET_NAME,ROW_NO,CREATED_BY,UPDATED_BY,UPDATED_AT,CREATED_AT");
        for (int i = 1; i <= this.columnSize; i++) {
            sql.append(",COLUMN").append(i);
        }
        sql.append(") VALUES('").append(this.templateId).append("'");
        sql.append(",'").append(this.combinationRecord).append("'");
        sql.append(",?,?").append(",'").append(this.operator).append("'").append(",'").append(this.operator).append("'");
        sql.append(",sysdate,sysdate");
        for (int i = 1; i <= this.columnSize; i++) {
            sql.append(",?");
        }
        sql.append(")");
        stmt = this.trans.createPreparedStatement(sql.toString(), 0);
    }

    public void getRows(int sheetIndex, String sheetName, int curRow,
                        TreeMap<Integer, String> rowlist) {
        if (curRow >= this.startLine - 1&&sheetName.startsWith(this.templateName)) {
            boolean isEpty = true;
            try {
                this.stmt.setString(1, sheetName);
                this.stmt.setInt(2, curRow + 1);
                for (int i = 0; i < this.columnSize; i++) {
                    String tmpstr = rowlist.get(i);
                    if (null == tmpstr || "".equals(tmpstr.trim())) {
                        this.stmt.setString(i + 3, "");
                    } else {
                        isEpty = false;
                        this.stmt.setString(i + 3, tmpstr.trim());
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
