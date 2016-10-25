package dms.group;

import common.ADFUtils;
import common.DmsUtils;
import common.JSFUtils;

import dcm.ColumnDef;

import dcm.Excel2007WriterImpl;

import dms.login.Person;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.Types;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputFile;

import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.model.UploadedFile;

import org.hexj.excelhandler.reader.ExcelReaderUtil;

public class EditGroupBean {
    private Person person = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
    //是否是2007及以上格式
    private boolean isXlsx = true;
    private RichPopup dataExportWnd;
    private List<ColumnDef> colsdef = new ArrayList<ColumnDef>();
    private RichPopup dataImportWnd;
    private RichInputFile inputFile;


    public EditGroupBean() {
        this.initColsdef();
    }

    public void groupExport(FacesContext facesContext,
                            OutputStream outputStream) {
        this.dataExportWnd.cancel();
        LinkedHashMap<String,String> labelMap = this.initColsdef();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        for(Map.Entry<String,String> map : labelMap.entrySet()){
            if(map.getKey().equals("ENABLE_FLAG")){
                sql.append("DECODE(").append(map.getKey()).append(",'Y','是','否') ENABLE_FLAG ,");
            }else {
                sql.append(map.getKey()).append(",");
            }
        }
        sql.append("ID FROM DMS_GROUP WHERE LOCALE='").append(ADFContext.getCurrent().getLocale())
            .append("' ORDER BY NAME");
        try {
            Excel2007WriterImpl writer = new Excel2007WriterImpl(
                                                                sql.toString(),
                                                                2,this.colsdef);
            writer.process(outputStream, "用户组编辑");
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public LinkedHashMap<String,String> initColsdef(){
        this.colsdef.clear();
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        labelMap.put("ID", "ID");
        labelMap.put("NAME", "组名称");
        labelMap.put("ENABLE_FLAG", "有效");
        labelMap.put("UPDATED_AT", "更新时间");
        labelMap.put("UPDATED_BY", "更新人");
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            ColumnDef col;
            if(map.getValue().equals("UPDATED_AT")){
                col = new ColumnDef(map.getValue(),map.getKey(),"DATE");
            }else{
                col = new ColumnDef(map.getValue(),map.getKey(),"");
            }
            this.colsdef.add(col);
        }
        return labelMap;
    }

    //获取导出数据时的文件名
    public String getExportDataExcelName() {
        return "用户组编辑"+".xlsx";
    }
    

    public void operation_import(ActionEvent actionEvent) {
        this.dataImportWnd.cancel();
        //上传文件为空
        if (null == this.inputFile.getValue()) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.plz_select_import_file"));
            return;
        }
        //获取文件上传路径
        String filePath = this.uploadFile();
        if (null == filePath) {
            return;
        }
        //读取excel数据到数据库临时表
        try {
            if (!this.handleExcel(filePath)) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.inputFile.resetValue();
        if(this.input_import()){
            ViewObject groupVo =
                ADFUtils.findIterator("DmsGroupViewIterator").getViewObject();
            groupVo.executeQuery();
        }
    }
    
    //文件上传
    private String uploadFile() {
        UploadedFile file = (UploadedFile)this.inputFile.getValue();
        if (!(file.getFilename().endsWith(".xls") ||
              file.getFilename().endsWith(".xlsx"))) {
            String msg = DmsUtils.getMsg("dcm.file_format_error");
            JSFUtils.addFacesErrorMessage(msg);
            return null;
        }
        String fileExtension =file.getFilename().substring(file.getFilename().lastIndexOf("."));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        File dmsBaseDir = new File("DMS/UPLOAD/USERMANAGE");
        //如若文件路径不存在则创建文件目录
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        String fileName = "DMS/UPLOAD/USERMANAGE"+ "/"+file.getFilename();
        try {
            InputStream inputStream = file.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileName);
            BufferedInputStream bufferedInputStream =new BufferedInputStream(inputStream);
            BufferedOutputStream bufferedOutputStream =
                new BufferedOutputStream(outputStream);
            byte[] buffer = new byte[10240];
            int bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(buffer, 0, 10240)) !=-1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
            bufferedOutputStream.flush();
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            file.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            String msg = DmsUtils.getMsg("dcm.file_upload_error");
            JSFUtils.addFacesErrorMessage(msg);
            return null;
        }
        return (new File(fileName)).getAbsolutePath();
    }
    
    //读取excel数据到临时表
    private boolean handleExcel(String fileName) throws SQLException {
        DBTransaction trans =(DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        //清空已有临时表数据
        String sql = "DELETE FROM DMS_GROUP_ROLE_TEMP WHERE CREATED_BY='"+this.person.getId()+"' AND DATA_TYPE = 'GROUP'";
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        stat.executeUpdate(sql);
        stat.close();
        trans.commit();
        UploadedFile file = (UploadedFile)this.inputFile.getValue();
        String fname = file.getFilename();
        String name = fname.substring(fname.indexOf("_")+1, fname.indexOf("."));
        if(!name.equals("用户组编辑")){
            JSFUtils.addFacesErrorMessage("请选择正确的文件");
            return false;
        }
        GroupRoleRowReader groupRoleReader = new GroupRoleRowReader(trans,2,this.colsdef,this.person.getId(),"GROUP","用户组编辑");
        try {
                ExcelReaderUtil.readExcel(groupRoleReader, fileName, true);
            if(!groupRoleReader.close()){
                return false;
            }
            } catch (Exception e) {
                e.printStackTrace();
                JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.excel_handle_error"));
                return false;
            }
        return true;
    }
    
    public boolean input_import(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALL DMS_SETTING.GROUP_IMPORT(?,?)}", 0);
        try {
            cs.setString(1, this.person.getId());
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            if("N".equals(cs.getString(2))){
                flag = false;
            }
        } catch (SQLException e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
    
    public void setIsXlsx(boolean isXlsx) {
        this.isXlsx = isXlsx;
    }

    public boolean isIsXlsx() {
        return isXlsx;
    }

    public void setDataExportWnd(RichPopup dataExportWnd) {
        this.dataExportWnd = dataExportWnd;
    }

    public RichPopup getDataExportWnd() {
        return dataExportWnd;
    }

    public void setColsdef(List<ColumnDef> colsdef) {
        this.colsdef = colsdef;
    }

    public List<ColumnDef> getColsdef() {
        return colsdef;
    }

    public void setDataImportWnd(RichPopup dataImportWnd) {
        this.dataImportWnd = dataImportWnd;
    }

    public RichPopup getDataImportWnd() {
        return dataImportWnd;
    }

    public void setInputFile(RichInputFile inputFile) {
        this.inputFile = inputFile;
    }

    public RichInputFile getInputFile() {
        return inputFile;
    }

}
