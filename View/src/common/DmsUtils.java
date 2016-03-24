package common;

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlHierBinding;

import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;

import team.epm.module.DcmModuleImpl;
import team.epm.module.DmsModuleImpl;
import team.epm.module.InfaModuleImpl;
import team.epm.module.Odi11gModuleImpl;

public class DmsUtils {
    public DmsUtils() {
        super();
    }
    
    //Writer the message to txt file and return the filepath
    public static String writeToTxT(String message,String type){
        File dmsBaseDir = new File("DMS\\DmsLog");
        //如若文件路径不存在则创建文件目录
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        String fileName = type + "-" + date + ".txt";
        fileName = dmsBaseDir + "\\" + fileName;
        
        File logFile = new File(fileName);

        try {
            logFile.createNewFile();
            FileWriter fw = new FileWriter(logFile);
            fw.write(message);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return fileName;    
    }
    
    public static String getRandomString(int length) { //length表示生成字符串的长度  
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";     
        Random random = new Random();     
        StringBuffer sb = new StringBuffer();     
        for (int i = 0; i < length; i++) {     
            int number = random.nextInt(base.length());     
            sb.append(base.charAt(number));     
        }     
        return sb.toString();     
     }  
    
    public static String getMsg(String key) {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle =
            context.getApplication().getResourceBundle(context, "dms_v");
        return bundle.getString(key);
    }

    public static void makeCurrent(SelectionEvent selectionEvent) {

        RichTable _table = (RichTable)selectionEvent.getSource();
        //the Collection Model is the object that provides the
        //structured data
        //for the table to render
        CollectionModel _tableModel = (CollectionModel)_table.getValue();
        //the ADF object that implements the CollectionModel is
        //JUCtrlHierBinding. It is wrapped by the CollectionModel API
        JUCtrlHierBinding _adfTableBinding =
            (JUCtrlHierBinding)_tableModel.getWrappedData();
        //Acess the ADF iterator binding that is used with
        //ADF table binding
        DCIteratorBinding _tableIteratorBinding =
            _adfTableBinding.getDCIteratorBinding();

        //the role of this method is to synchronize the table component
        //selection with the selection in the ADF model
        Object _selectedRowData = _table.getSelectedRowData();
        //cast to JUCtrlHierNodeBinding, which is the ADF object
        //that represents a row
        JUCtrlHierNodeBinding _nodeBinding =
            (JUCtrlHierNodeBinding)_selectedRowData;
        //get the row key from the node binding and set it
        //as the current row in the iterator
        Key _rwKey = _nodeBinding.getRowKey();
        _tableIteratorBinding.setCurrentRowWithKey(_rwKey.toStringFormat(true));
    }
    public static DmsModuleImpl getDmsApplicationModule(){
        return (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
    }
    public static DcmModuleImpl getDcmApplicationModule(){
        return (DcmModuleImpl)ADFUtils.getApplicationModuleForDataControl("DcmModuleDataControl");
    }
    public static Odi11gModuleImpl getOdi11gApplicationModule(){
        return (Odi11gModuleImpl)ADFUtils.getApplicationModuleForDataControl("Odi11gModuleDataControl");
    }
    public static InfaModuleImpl getInfaApplicationModule(){
        return (InfaModuleImpl)ADFUtils.getApplicationModuleForDataControl("InfaModuleDataControl");    
    }
    public static Map getSystemProperty(){
        Map props=new HashMap();
        ViewObject vo=getDmsApplicationModule().getDmsPropertyView();
        vo.reset();
        vo.executeQuery();
        while(vo.hasNext()){
            Row row=vo.next();
            props.put(row.getAttribute("Ckey"), row.getAttribute("Cvalue"));
        }
        return props;
    }
}
