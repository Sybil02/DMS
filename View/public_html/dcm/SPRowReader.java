package dcm;

import common.ReplaceSpecialChar;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;
import java.util.TreeMap;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.hexj.excelhandler.reader.IRowReader;

public class SPRowReader implements IRowReader{
    private int startLine;
    private String connectId;
    private PreparedStatement stmt;
    private List<PcColumnDef> colsdef;
    private String operator;
    private DBTransaction trans;
    private int n = 0;
    private static final int batchSize = 5000;
    private String data_type;
    private static ADFLogger logger=ADFLogger.createADFLogger(RowReader.class);

    /**
     * @param trans
     * @param startLine
     * @param connectId
     * @param colsdef
     * @param operator
     * @param data_type
     */
    public SPRowReader(DBTransaction trans, int startLine,String connectId,
                      List<PcColumnDef> colsdef, String operator,String data_type) {
        this.startLine = startLine;
        this.connectId = connectId;
        this.colsdef = colsdef;
        this.operator = operator;
        this.trans = trans;
        this.data_type=data_type;
        this.prepareSqlStatement();
    }

    private void prepareSqlStatement() {
        StringBuffer sql = new StringBuffer();
        StringBuffer sqlValue = new StringBuffer();
        sql.append("INSERT INTO PRO_PLAN_COST_BODY_TEMP (CREATED_BY,CONNECT_ID,DATA_TYPE,ROW_NO");
        sqlValue.append(") VALUES("+this.operator+",?,"+this.data_type+",?");
        for(int i=0;i<this.colsdef.size();i++){
            sql.append(","+this.colsdef.get(i).getDbTableCol());
            sqlValue.append(",?");
        }
        sqlValue.append(")");
        System.out.println(sql.toString()+sqlValue.toString());
    }

    public void getRows(int sheetIndex, String sheetName, int curRow,
                        TreeMap<Integer, String> rowlist) {
        ReplaceSpecialChar rsc = new ReplaceSpecialChar();
        System.out.println(sheetIndex + ":" + sheetName);
        if (curRow >= this.startLine - 1&&sheetName.startsWith(this.connectId)) {
            boolean isEpty = true;
            try {
                this.stmt.setString(1, sheetName);
                this.stmt.setInt(2, curRow + 1);
                for (int i = 0; i < this.colsdef.size(); i++) {
                    
                    String tmpstr = rowlist.get(i);
                    if (null == tmpstr || "".equals(tmpstr.trim())) {
                        this.stmt.setString(i + 3, "");
                    } else {
                        isEpty = false;
                        this.stmt.setString(i + 3, rsc.decodeString(tmpstr.trim()));        
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
