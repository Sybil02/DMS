package dgpt;

import common.DmsUtils;
import common.ReplaceSpecialChar;

import dcm.AbstractExcel2007Writer;
import dcm.ColumnDef;
import dcm.Excel2007WriterImpl;

import dcm.PcColumnDef;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;

public class XmdbExcel2007Writer extends AbstractExcel2007Writer{

    private static ADFLogger logger=ADFLogger.createADFLogger(XmdbExcel2007Writer.class);
    private List<PcColumnDef> colsdef;
    private List<Map> rows;
    private LinkedHashMap<String,String> labelMap;
    public XmdbExcel2007Writer(List<PcColumnDef> colsdef,List<Map> rows,LinkedHashMap<String,String> labelMap) {
        this.colsdef=colsdef;
        this.rows = rows;
        this.labelMap = labelMap;
    }

    public void generate() {
        try {
            //电子表格开始
            beginSheet();
            insertRow(0);
            for (int i = 0; i < this.colsdef.size(); i++) {
                createCell(i, this.colsdef.get(i).getColumnLable());
            }
            endRow();
            DecimalFormat dfm = new DecimalFormat();
            dfm.setMaximumFractionDigits(4);
            dfm.setGroupingUsed(false);
            
            for(int i=0 ; i < this.rows.size() ; i++){
                insertRow(i+1);
                Map row = rows.get(i);
                createCell(0,ObjectUtils.toString(row.get("PROJECT_NAME")));
                createCell(1,ObjectUtils.toString(row.get("PRODUCT_LINE")));
                createCell(2,ObjectUtils.toString(row.get("INDUSTRY_LINE")));
                createCell(3,ObjectUtils.toString(row.get("BUSINESS_LINE")));
                createCell(4,ObjectUtils.toString(row.get("TOTAL")));
                createCell(5,ObjectUtils.toString(row.get("FCST_COST")));
                createCell(6,ObjectUtils.toString(row.get("LAST_1_10ADJ")));
                createCell(7,ObjectUtils.toString(row.get("LAST_11_12FCST")));
                createCell(8,ObjectUtils.toString(row.get(labelMap.get("M3"))));
                createCell(9,ObjectUtils.toString(row.get(labelMap.get("M4"))));
                createCell(10,ObjectUtils.toString(row.get(labelMap.get("M5"))));
                createCell(11,ObjectUtils.toString(row.get(labelMap.get("M6"))));
                createCell(12,ObjectUtils.toString(row.get(labelMap.get("M7"))));
                createCell(13,ObjectUtils.toString(row.get(labelMap.get("M8"))));
                createCell(14,ObjectUtils.toString(row.get(labelMap.get("M9"))));
                createCell(15,ObjectUtils.toString(row.get(labelMap.get("M10"))));
                createCell(16,ObjectUtils.toString(row.get(labelMap.get("M11"))));
                createCell(17,ObjectUtils.toString(row.get(labelMap.get("M12"))));
                createCell(18,ObjectUtils.toString(row.get(labelMap.get("M13"))));
                createCell(19,ObjectUtils.toString(row.get(labelMap.get("M14"))));
                createCell(20,ObjectUtils.toString(row.get(labelMap.get("M15"))));
                createCell(21,ObjectUtils.toString(row.get(labelMap.get("M16"))));
                createCell(22,ObjectUtils.toString(row.get(labelMap.get("M17"))));
                createCell(23,ObjectUtils.toString(row.get(labelMap.get("M18"))));
                createCell(24,ObjectUtils.toString(row.get(labelMap.get("M19"))));
                createCell(25,ObjectUtils.toString(row.get(labelMap.get("M20"))));
                createCell(26,ObjectUtils.toString(row.get("NEXT_ORTHERS")));
                endRow();
            }
            
            //电子表格结束
            endSheet();
        } catch (Exception e) {
            this.logger.severe(e);
        }
    }
}
