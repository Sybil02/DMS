package dcm;

import common.ReplaceSpecialChar;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.hexj.excelhandler.reader.IRowReader;

public class HtkpRowReader implements IRowReader{
    private int startLine;
    private String connectId;
    private PreparedStatement stmt;
    private List<PcColumnDef> colsdef;
    private LinkedHashMap<String,String> lineMap;
    private String operator;
    private DBTransaction trans;
    private int n = 0;
    private static final int batchSize = 5000;
    private String name;
    private static ADFLogger logger=ADFLogger.createADFLogger(RowReader.class);
    private String entity;
    private String hLine;
    private String yLine;
    private String cLine;
    private String pType;
    private DecimalFormat dfm;
    
    /**
     * @param trans
     * @param startLine 开始
     * @param connectId 关联id
     * @param colsdef 列
     * @param operator 操作者
     * @param data_type 数据类型
     */
    public HtkpRowReader(DBTransaction trans, int startLine,String connectId,
                      List<PcColumnDef> colsdef, String operator,String name,
                         LinkedHashMap<String,String> lineMap) {
        this.startLine = startLine;
        this.connectId = connectId;
        this.colsdef = colsdef;
        this.operator = operator;
        this.trans = trans;
        this.name = name;
        this.lineMap = lineMap;
        dfm = new DecimalFormat();
        dfm.setMaximumFractionDigits(4);
        dfm.setGroupingUsed(false);
        this.getEveryLine();
        this.prepareSqlStatement();
    }

    private void getEveryLine(){
        for(Map.Entry<String,String> entry : lineMap.entrySet()){
            if(entry.getKey().equals("ENTITY")){
                entity = entry.getValue();
            }else if(entry.getKey().equals("INDUSTRY_LINE")){
                hLine = entry.getValue();     
            }else if(entry.getKey().equals("BUSINESS_LINE")){
                yLine = entry.getValue();                                                    
            }else if(entry.getKey().equals("PRODUCT_LINE")){
                cLine = entry.getValue();                                                    
            }else if(entry.getKey().equals("PROJECT_TYPE")){
                pType = entry.getValue();                                                   
            }
        }
    }
    private void prepareSqlStatement() {
        StringBuffer sql = new StringBuffer();
        StringBuffer sqlValue = new StringBuffer();
        sql.append("INSERT INTO CONT_INVOICE_RETURN_BUDGET_5_T (CREATED_BY,COM_RECORD_ID,ROW_NUM,OPERATION");
        sqlValue.append(") VALUES('"+this.operator+"',?,?,'").append("CREATE'");
        for(int i=0;i<this.colsdef.size();i++){
            sql.append(","+this.colsdef.get(i).getDbTableCol());
            sqlValue.append(",?");
        }
        sqlValue.append(")");
        stmt = this.trans.createPreparedStatement(sql.toString()+sqlValue.toString(), 0);
    }

    public void getRows(int sheetIndex, String sheetName, int curRow,
                        TreeMap<Integer, String> rowlist) {
        ReplaceSpecialChar rsc = new ReplaceSpecialChar();
        if (curRow >= this.startLine - 1&&this.name.startsWith(this.connectId)) {
            boolean isEpty = true;
            try {
                this.stmt.setString(1, this.connectId);
                this.stmt.setInt(2, curRow );
                for (int i = 0; i < this.colsdef.size(); i++) {
                    String tmpstr = rowlist.get(i);
                    if(this.colsdef.get(i).getDataType().equals("NUMBER")){
                        if (null == tmpstr || "".equals(tmpstr.trim())) {
                            this.stmt.setString(i + 3, "");
                        } else {
                            isEpty = false;
                            if(this.colsdef.get(i).getDbTableCol().equals("ENTITY")){
                                this.stmt.setString(i + 3, entity);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("INDUSTRY_LINE")){
                                this.stmt.setString(i + 3, hLine);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("BUSINESS_LINE")){
                                this.stmt.setString(i + 3, yLine);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("PRODUCT_LINE")){
                                this.stmt.setString(i + 3, cLine);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("PROJECT_TYPE")){
                                this.stmt.setString(i + 3, pType);
                            }else{
                                this.stmt.setString(i + 3, dfm.format(Double.parseDouble(tmpstr.trim())));
                            }       
                        }
                    }else{
                        if (null == tmpstr || "".equals(tmpstr.trim())) {
                            this.stmt.setString(i + 3, "");
                        } else {
                            isEpty = false;
                            if(this.colsdef.get(i).getDbTableCol().equals("ENTITY")){
                                this.stmt.setString(i + 3, entity);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("INDUSTRY_LINE")){
                                this.stmt.setString(i + 3, hLine);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("BUSINESS_LINE")){
                                this.stmt.setString(i + 3, yLine);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("PRODUCT_LINE")){
                                this.stmt.setString(i + 3, cLine);
                            }else if(this.colsdef.get(i).getDbTableCol().equals("PROJECT_TYPE")){
                                this.stmt.setString(i + 3, pType);
                            }else{
                                this.stmt.setString(i + 3, rsc.decodeString(tmpstr.trim()));
                            }         
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
