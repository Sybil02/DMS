package dcm;

import common.DmsUtils;

import common.ReplaceSpecialChar;

import java.io.IOException;
import java.io.OutputStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.List;

import oracle.jbo.server.DBTransaction;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class PcExcel2003WriterImpl {
    private String sql;
    private String name;
    private List<PcColumnDef> colsdef;
    private OutputStream outputStream;
    
    public PcExcel2003WriterImpl(String sql,String name,List<PcColumnDef> colsdef,OutputStream outPutStream) {
        this.sql = sql;
        this.name = name;
        this.colsdef = colsdef;
        this.outputStream = outPutStream;
    }
    
    public void writeToFile() throws SQLException, IOException{
        DBTransaction dbTransaction = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        PreparedStatement stat = dbTransaction.createPreparedStatement(sql, -1);
        ResultSet rs = stat.executeQuery();
        //创建excel2003对象
        Workbook wb = new HSSFWorkbook();
        //创建新的表单
        Sheet sheet = wb.createSheet(name);
        //创建新行
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for(int i=0;i<this.colsdef.size();i++){
            headerRow.createCell(i).setCellValue(this.colsdef.get(i).getColumnLable());
        }
        int n = 1;
        DecimalFormat dfm = new DecimalFormat();
        dfm.setMaximumFractionDigits(4);
        dfm.setGroupingUsed(false);
        while(rs.next()){
            int colInx = 0;
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(n);
            for(PcColumnDef col : this.colsdef){
                Cell cell = row.createCell(colInx);
                ++colInx;
                cell.setCellValue(ReplaceSpecialChar.encodeString(rs.getString(col.getDbTableCol())));
            }
            ++n;
        }
        rs.close();
        wb.write(outputStream);
    }
}
