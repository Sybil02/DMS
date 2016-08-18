package dcm;

import common.ReplaceSpecialChar;

import dcm.template.TemplateEntity;

import java.sql.PreparedStatement;

import java.sql.SQLException;
import java.sql.Statement;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.hexj.excelhandler.reader.IRowReader;

public class BatchExcelReader implements IRowReader{
    private String combinationRecord;
    private List<PreparedStatement> stmtList;
    private String operator;
    private DBTransaction trans;
    private List<TemplateEntity> tempList;
    private int n = 0;
    private static final int batchSize = 5000;
    private DecimalFormat dfm;
    private static ADFLogger logger=ADFLogger.createADFLogger(RowReader.class);
    
    public BatchExcelReader(DBTransaction trans,String combinationRecord,String operator,List<TemplateEntity> tempList) {
        this.trans = trans;
        this.combinationRecord = combinationRecord;
        this.operator = operator;
        this.tempList = tempList;
        dfm = new DecimalFormat();
        dfm.setMaximumFractionDigits(4);
        dfm.setGroupingUsed(false);
        this.stmtList = initStmtList();
    }

    public void getRows(int sheetIndex, String sheetName, int curRow,
                        TreeMap<Integer, String> rowlist) {
        ReplaceSpecialChar rsc = new ReplaceSpecialChar();
        
        for(int idx = 0 ; idx < this.tempList.size() ; idx++){
            if(this.tempList.get(idx).getTemplateName().equals(sheetName)){
                
                if (curRow >= tempList.get(idx).getStartLine() - 1&&sheetName.startsWith(tempList.get(idx).getTemplateName())) {
                  boolean isEpty = true;
                  try {
                      this.stmtList.get(idx).setString(1, sheetName);
                      this.stmtList.get(idx).setInt(2, curRow + 1);
                      
                      List types = this.tempList.get(idx).getColType();
                      for (int i = 0; i < this.tempList.get(idx).getColumnSize(); i++) {
                          
                          String tmpstr = rowlist.get(i);
                          
                          if(types.get(i).equals("NUMBER")){
                              if (null == tmpstr || "".equals(tmpstr.trim())) {
                                  this.stmtList.get(idx).setString(i + 3, "");
                              } else {
                                  isEpty = false;
                                  this.stmtList.get(idx).setString(i + 3, dfm.format(Double.parseDouble(tmpstr.trim())));
                              }
                          }else{
                              if (null == tmpstr || "".equals(tmpstr.trim())) {
                                  this.stmtList.get(idx).setString(i + 3, "");
                              } else {
                                  isEpty = false;
                                  this.stmtList.get(idx).setString(i + 3, rsc.decodeString(tmpstr.trim()));        
                              }
                          }

                      }
                      if (!isEpty) {
                          this.stmtList.get(idx).addBatch();
                      }
                      if ((this.n + 1) % this.batchSize == 0) {
                          this.stmtList.get(idx).executeBatch();
                          this.trans.commit();
                      }
                      this.n += 1;
                  } catch (Exception e) {
                      this.logger.severe(e);
                  }
                }
            }    
        }
        
    }
    
    private List<PreparedStatement> initStmtList(){
        List<PreparedStatement> stmtList = new ArrayList<PreparedStatement>();
        
        for(TemplateEntity entity : tempList){
            stmtList.add(this.prepareSqlStatement(entity));        
        }
        
        return stmtList;
    }
    
    private PreparedStatement prepareSqlStatement(TemplateEntity entity) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO \"").append(entity.getTemptable().toUpperCase()).append("\"(");
        sql.append("TEMPLATE_ID,COM_RECORD_ID,SHEET_NAME,ROW_NO,CREATED_BY,UPDATED_BY,UPDATED_AT,CREATED_AT");
        for (int i = 1; i <= entity.getColumnSize(); i++) {
            sql.append(",COLUMN").append(i);
        }
        sql.append(") VALUES('").append(entity.getTemplateId()).append("'");
        sql.append(",'").append(this.combinationRecord).append("'");
        sql.append(",?,?").append(",'").append(this.operator).append("'").append(",'").append(this.operator).append("'");
        sql.append(",sysdate,sysdate");
        for (int i = 1; i <= entity.getColumnSize(); i++) {
            sql.append(",?");
        }
        sql.append(")");
        PreparedStatement stmt = this.trans.createPreparedStatement(sql.toString(), 0);
        return stmt;
    }
    
    public void close() {
        try {
            for(PreparedStatement stmt : this.stmtList){
                stmt.executeBatch();
                stmt.close();
            }
            this.trans.commit();
        } catch (SQLException e) {
            this.logger.severe(e);
        }
    }
    
}
