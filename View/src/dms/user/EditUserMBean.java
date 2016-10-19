package dms.user;

import com.bea.security.utils.DigestUtils;

import common.ADFUtils;
import common.DmsUtils;

import common.JSFUtils;

import dcm.ColumnDef;

import dcm.Excel2007WriterImpl;

import dcm.UserRowReader;

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
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.component.rich.output.RichOutputLabel;

import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;

import org.apache.myfaces.trinidad.model.UploadedFile;

import org.hexj.excelhandler.reader.ExcelReaderUtil;

import team.epm.dms.model.DmsUserImpl;

public class EditUserMBean {
    private RichInputText newPwd;
    private RichInputText pwd;
    private Person person = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
    private static ADFLogger logger =
        ADFLogger.createADFLogger(EditUserMBean.class);
    private RichOutputLabel msg;
    private RichPopup popup;
    private List<ColumnDef> colsdef = new ArrayList<ColumnDef>();
    //是否是2007及以上格式
    private boolean isXlsx = true;
    private RichPopup dataExportWnd;
    private RichPopup dataImportWnd;
    private RichInputFile fileInput;

    public EditUserMBean() {
        this.initColsdef();
    }

    public void setNewPwd(RichInputText newPwd) {
        this.newPwd = newPwd;
    }

    public RichInputText getNewPwd() {
        return newPwd;
    }

    public void setPwd(RichInputText pwd) {
        this.pwd = pwd;
    }

    public RichInputText getPwd() {
        return pwd;
    }

    public void setMsg(RichOutputLabel msg) {
        this.msg = msg;
    }

    public RichOutputLabel getMsg() {
        return msg;
    }

    public void changePwd(ActionEvent actionEvent) {
        String pwd = ObjectUtils.toString(this.pwd.getValue()).trim();
        String newPwd = ObjectUtils.toString(this.newPwd.getValue()).trim();
        if (pwd.equals(newPwd)) {
            if(DmsUserImpl.isPasswordValide(newPwd)){
            ViewObject usrVo =
                ADFUtils.findIterator("DmsUserViewIterator").getViewObject();
            String usrAcc = (String)usrVo.getCurrentRow().getAttribute("Acc");
            String encyptPwd;
            try {
                encyptPwd = DigestUtils.digestSHA1(usrAcc + pwd);
                usrVo.getCurrentRow().setAttribute("Pwd", encyptPwd);
                usrVo.getApplicationModule().getTransaction().commit();
                this.popup.cancel();
            } catch (Exception e) {
                this.logger.severe(e);
            }
            }else{
                this.msg.setValue(DmsUtils.getMsg("dms.user.password_limit"));
            }
        } else {
            this.msg.setValue(DmsUtils.getMsg("dms_user.password_inconsitent"));
        }
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void hidePopup(ActionEvent actionEvent) {
        this.popup.cancel();
    }

    public void showPopup(ActionEvent actionEvent) {
        this.pwd.setValue("");
        this.newPwd.setValue("");
        this.msg.setValue("");
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.popup.show(hints);
    }

    public LinkedHashMap<String,String> initColsdef(){
        this.colsdef.clear();
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        labelMap.put("ID", "ID");
        labelMap.put("ACC", "账号");
        labelMap.put("NAME", "姓名");
        labelMap.put("PWD", "密码");
        labelMap.put("SEX", "性别");
        labelMap.put("MAIL", "邮箱");
        labelMap.put("PHONE", "电话");
        labelMap.put("LOCALE", "语言");
        labelMap.put("ENABLE_FLAG", "有效");
        labelMap.put("LOCK_FLAG", "锁定");
        labelMap.put("LOCK_TIME", "锁定时间");
        labelMap.put("OTHER_INFO", "其他");
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            ColumnDef col ;
            if(map.getValue().equals("LOCK_TIME")){
                 col = new ColumnDef(map.getValue(),map.getKey(),"DATE");
            }else{
                 col = new ColumnDef(map.getValue(),map.getKey(),"");
            }
            
            this.colsdef.add(col); 
        }
        return labelMap;
    }
    public void userExport(FacesContext facesContext,
                           OutputStream outputStream) {
        this.dataExportWnd.cancel();
        LinkedHashMap<String,String> labelMap = this.initColsdef();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            if(map.getKey().equals("SEX")){
                sql.append("DECODE(").append(map.getKey()).append(",'M','男','女') SEX ,");
            }else if(map.getKey().equals("LOCALE")){
                sql.append("DECODE(").append(map.getKey()).append(",'zh_CN','中文','英文') LOCALE ,");
            }else if(map.getKey().equals("ENABLE_FLAG")){
                sql.append("DECODE(").append(map.getKey()).append(",'Y','是','否') ENABLE_FLAG ,");
            }else if(map.getKey().equals("LOCK_FLAG")){
                sql.append("DECODE(").append(map.getKey()).append(",'Y','是','否') LOCK_FLAG ,");
            }else{
                sql.append(map.getKey()).append(",");
            }
            
        }
        sql.append("ID FROM DMS_USER");
        try {
            Excel2007WriterImpl writer = new Excel2007WriterImpl(
                                                                sql.toString(),
                                                                2,this.colsdef);
            writer.process(outputStream, "用户管理");
            outputStream.flush();
        } catch (Exception e) {
            this.logger.severe(e);
        }
    }
    
    //获取导出数据时的文件名

    public String getExportDataExcelName() {
        return "用户管理"+".xlsx";
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

    public void setDataImportWnd(RichPopup dataImportWnd) {
        this.dataImportWnd = dataImportWnd;
    }

    public RichPopup getDataImportWnd() {
        return dataImportWnd;
    }

    public void setFileInput(RichInputFile fileInput) {
        this.fileInput = fileInput;
    }

    public RichInputFile getFileInput() {
        return fileInput;
    }

    public void operation_import(ActionEvent actionEvent) {
        this.dataImportWnd.cancel();
        //上传文件为空
        if (null == this.fileInput.getValue()) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.plz_select_import_file"));
            return;
        }
        //获取文件上传路径
        String filePath = this.uploadFile();
//        this.fileInput.resetValue();
        if (null == filePath) {
            return;
        }
        //读取excel数据到数据库临时表
        try {
            if (!this.handleExcel(filePath)) {
                return;
            }
        } catch (SQLException e) {
            this.logger.severe(e);
        }
        this.fileInput.resetValue();
        if(this.input_import()){
            ViewObject usrVo =
                ADFUtils.findIterator("DmsUserViewIterator").getViewObject();
            usrVo.getCurrentRow().refresh(usrVo.QUERY_MODE_SCAN_DATABASE_TABLES);
        }
    }
    
    //文件上传
    private String uploadFile() {
        UploadedFile file = (UploadedFile)this.fileInput.getValue();
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
            this.logger.severe(e);
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
//            this.deleteTempAndError();
            String sql = "DELETE FROM DMS_USER_TEMP WHERE UPDATED_BY='"+this.person.getId()+"'";
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            stat.executeUpdate(sql);
            stat.close();
            trans.commit();
            UploadedFile file = (UploadedFile)this.fileInput.getValue();
            String fname = file.getFilename();
            String name = fname.substring(fname.indexOf("_")+1, fname.indexOf("."));
            if(!name.equals("用户管理")){
                JSFUtils.addFacesErrorMessage("请选择正确的文件");
                return false;
            }
            UserRowReader userReader = new UserRowReader(trans,2,this.colsdef,this.person.getId());
            try {
                    ExcelReaderUtil.readExcel(userReader, fileName, true);
                if(!userReader.close()){
                    return false;
                }
                } catch (Exception e) {
                    this.logger.severe(e);
                    JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.excel_handle_error"));
                    return false;
                }
            return true;
        }
        
    public boolean input_import(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALL DMS_SETTING.USER_IMPORT(?,?)}", 0);
        try {
            cs.setString(1, this.person.getId());
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            if("N".equals(cs.getString(2))){
                flag = false;
            }
        } catch (SQLException e) {
            flag = false;
            this.logger.severe(e);
        }
        return flag;
    }
}
