package dcm;

import java.sql.PreparedStatement;

import java.util.List;
import java.util.TreeMap;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.hexj.excelhandler.reader.IRowReader;

public class BatchExcelReader implements IRowReader{
    private List<String> batchTempList;
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
    private List<ColumnDef> colsdef;
    private static ADFLogger logger=ADFLogger.createADFLogger(RowReader.class);
    
    public BatchExcelReader(DBTransaction trans,List<String> batchTempList,String combinationRecord,String operator) {
        this.trans = trans;
        this.batchTempList = batchTempList;
        this.combinationRecord = combinationRecord;
        this.operator = operator;
    }

    public void getRows(int i, String string, int i1,
                        TreeMap<Integer, String> treeMap) {
    }
    
    private int initStartLine(){
        return 0;
    }
}
