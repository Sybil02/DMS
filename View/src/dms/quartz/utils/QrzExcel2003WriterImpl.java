package dms.quartz.utils;

import common.ReplaceSpecialChar;

import dcm.ColumnDef;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;

import java.text.DecimalFormat;

import java.util.List;

import oracle.jbo.jbotester.load.SimpleDateFormatter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class QrzExcel2003WriterImpl {
    private String sql;
    private String sheetName;
    private String jndiName;
    private int startLine;
    private List<ColumnDef> colsdef;
    private OutputStream outputStream;
    public QrzExcel2003WriterImpl(String sql,String sheetName,String fileName,
                                  String jndiName,int startLine,List<ColumnDef> colsdef) {
        this.sql=sql;
        this.sheetName=sheetName;
        this.colsdef=colsdef;
        this.startLine = startLine;
        this.jndiName = jndiName;
        
        File dmsBaseDir = new File("DMS\\DOWNLOAD\\" + sheetName);
        //如若文件路径不存在则创建文件目录
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        fileName = dmsBaseDir + "\\" + fileName;
        
        File file = new File(fileName);
        if(file.exists() && file.isFile()){
            file.delete();
        }

        try {
            this.outputStream = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void writeToFile() throws SQLException, IOException {

        DBConnUtils dbUtils = new DBConnUtils();
        Connection conn = dbUtils.getJNDIConnection(jndiName);
        PreparedStatement stat = conn.prepareStatement(sql);
        
        
        ResultSet rs = stat.executeQuery();
        // 创建excel2003对象
        Workbook wb = new HSSFWorkbook();
        // 创建新的表单
        Sheet sheet = wb.createSheet(this.sheetName);
        // 创建新行
        org.apache.poi.ss.usermodel.Row headerRow =
            sheet.createRow(startLine - 2);
        for (int i = 0; i < this.colsdef.size(); i++) {
            headerRow.createCell(i).setCellValue(this.colsdef.get(i).getColumnLabel());
        }
        int n = startLine - 1;
        DecimalFormat dfm = new DecimalFormat();
        dfm.setMaximumFractionDigits(4);
        dfm.setGroupingUsed(false);
        while (rs.next()) {
            int colInx = 0;
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(n);
            for (ColumnDef col : this.colsdef) {
                Cell cell = row.createCell(colInx);
                ++colInx;
                if(rs.getObject(col.getDbTableCol()) instanceof java.sql.Date){
                    SimpleDateFormatter format=new SimpleDateFormatter("yyyy-MM-dd hh:mm:ss");
                    Object obj=format.format((java.sql.Date)rs.getObject(col.getDbTableCol()));
                    cell.setCellValue((String)obj);
                }else if(col.getDataType().equals("NUMBER")){
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    if(rs.getString(col.getDbTableCol())!=null){
                        cell.setCellValue(Double.valueOf(dfm.format(Double.valueOf(rs.getString(col.getDbTableCol()))))); 
                    }else{
                        cell.setCellType(Cell.CELL_TYPE_BLANK);        
                    }
                }else{
                    cell.setCellValue(ReplaceSpecialChar.encodeString(rs.getString(col.getDbTableCol()))); 
                }
            }
            ++n;
        }
        rs.close();
        wb.write(outputStream);
        outputStream.close();
    }
}

