package dms.quartz.utils;

import com.mchange.v2.lang.ObjectUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.sql.Statement;

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
    private Connection conn;
    private int n = 0;
    private static final int batchSize = 5000;
    private String templateName;
    private StringBuffer sql;
    private static ADFLogger logger=ADFLogger.createADFLogger(RowReader.class);
    public RowReader(Connection conn, int startLine, String templateId,
                     String combinationRecord, String temptable,
                     int columnSize, String operator,String templateName) {
        this.startLine = startLine;
        this.templateId = templateId;
        this.combinationRecord = combinationRecord;
        this.temptable = temptable;
        this.columnSize = columnSize;
        this.operator = operator;
        this.conn = conn;
        this.templateName=templateName;
        this.prepareSqlStatement();
    }

    private void prepareSqlStatement() {
        this.sql = new StringBuffer();
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
        try {
            
            this.stmt = this.conn.prepareStatement(sql.toString());
            System.out.println("prepare:"+stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void getRows(int sheetIndex, String sheetName, int curRow,
                        TreeMap<Integer, String> rowlist) {
        System.out.println(startLine + "use get row............................................."+curRow);
        if (curRow >= this.startLine - 1) {
            boolean isEpty = true;
            try {
                String rowSql = this.sql.toString();
                System.out.println("stmttttttt:"+stmt);
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
                if (this.n >= 2) {
                    //this.stmt.executeBatch();
                }
                this.n += 1;
            } catch (Exception e) {
                this.logger.severe(e);
            }
        }
    }

    public void close() {
        try {
            System.out.append("executeeeeeeee:"+this.stmt);
            this.stmt.executeBatch();
            this.stmt.getConnection().commit();
            this.stmt.close();
        } catch (SQLException e) {
            this.logger.severe(e);
        }
    }
}
