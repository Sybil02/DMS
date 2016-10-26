package dms.group;

import common.JSFUtils;
import common.ReplaceSpecialChar;

import dcm.ColumnDef;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.List;
import java.util.TreeMap;

import java.util.UUID;

import oracle.jbo.server.DBTransaction;

import org.hexj.excelhandler.reader.IRowReader;

public class GroupRoleRowReader implements IRowReader{
    private int startLine;
    private PreparedStatement stmt;
    private List<ColumnDef> colsdef;
    private DBTransaction trans;
    private int n=0;
    private static final int batchSize = 5000;
    private String operator;
    private DecimalFormat dfm;
    private String dataType;
    private String name;
    
    public GroupRoleRowReader(DBTransaction trans,int startLine,
                              List<ColumnDef> colsdef,String operator,String dataType,String name) {
        this.trans = trans;
        this.startLine = startLine;
        this.colsdef = colsdef;
        this.operator = operator;
        this.dataType = dataType;
        this.name = name;
        dfm = new DecimalFormat();
        dfm.setMaximumFractionDigits(6);
        dfm.setGroupingUsed(false);
        this.prepareSqlStatement();
    }
    
    private void prepareSqlStatement(){
        StringBuffer sql = new StringBuffer();
        StringBuffer sqlValue = new StringBuffer();
        sql.append("INSERT INTO DCM_TEMPTABLE20 (CREATED_AT,UPDATED_AT,CREATED_BY,UPDATED_BY,");
        sqlValue.append(") VALUES(SYSDATE,SYSDATE,").append(this.operator+",").append(this.operator+",");
        sql.append("COLUMN1");
        sqlValue.append("\'").append(this.dataType).append("\'");
        for(int i=0;i<this.colsdef.size();i++){
            sql.append(",COLUMN"+(i+2));
            sqlValue.append(",?");
        }
        sqlValue.append(")");
        stmt = this.trans.createPreparedStatement(sql.toString()+sqlValue.toString(), 0);
    }

    public void getRows(int sheetIndex, String sheetName, int curRow,
                        TreeMap<Integer, String> rowlist) {
        ReplaceSpecialChar rsc = new ReplaceSpecialChar();
        if(curRow >= this.startLine - 1&&sheetName.startsWith(this.name)){
            boolean isEpty = true;
                        try {
                            for (int i = 0; i < this.colsdef.size(); i++) {
                            String tmpstr = rowlist.get(i);
                            if(this.colsdef.get(i).getDataType().equals("NUMBER")){
                                if (null == tmpstr || "".equals(tmpstr.trim())) {
                                    this.stmt.setString(i + 1, "");
                                } else {
                                    isEpty = false;
                                    this.stmt.setString(i + 1, dfm.format(Double.parseDouble(tmpstr.trim())));
                                }
                            }else{
                                if (null == tmpstr || "".equals(tmpstr.trim())) {
                                    if(this.colsdef.get(i).getDbTableCol().equals("ID")){
                                        this.stmt.setString(i+1, UUID.randomUUID().toString().replace("-", ""));
                                    }else{
                                        this.stmt.setString(i + 1, "");
                                    }
                                } else {
                                    isEpty = false;
                                    this.stmt.setString(i + 1, rsc.decodeString(tmpstr.trim()));
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
                            e.printStackTrace();
                        }
        }
    }
    
    public boolean close() {
        boolean flag = true;
            try {
                this.stmt.executeBatch();
                this.stmt.close();
                this.trans.commit();
            } catch (SQLException e) {
                JSFUtils.addFacesErrorMessage(e.getMessage());
                flag = false;
            }
        return flag;
    }
}

