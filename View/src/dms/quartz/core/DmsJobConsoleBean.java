package dms.quartz.core;

import common.ADFUtils;
import common.DmsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.RichDialog;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.event.DialogEvent;

import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;

public class DmsJobConsoleBean {
    private RichDialog downLoadDlg;
    private RichPopup downLoadPop;
    private RichInputDate startTime;
    private RichInputDate endTime;
    private RichSelectOneChoice jobTypeSoc;
    private RichInputText userName;
    private RichInputText jobName;
    private RichSelectOneChoice statusSoc;
    private String downFileName;
    private String downFilePath;

    public DmsJobConsoleBean() {

    }

    public void showDownLoadPop(ActionEvent actionEvent) {
        Object obj = ADFUtils.findIterator("DmsJobDetailsVOIterator").getCurrentRow().getAttribute("FileName");
        if(obj != null){
            this.downFilePath = obj.toString();
            this.downFileName = downFilePath.substring(downFilePath.lastIndexOf("\\")+1);
        }
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.downLoadPop.show(hints);
    }

    public void setDownLoadDlg(RichDialog downLoadDlg) {
        this.downLoadDlg = downLoadDlg;
    }

    public RichDialog getDownLoadDlg() {
        return downLoadDlg;
    }

    public void setDownLoadPop(RichPopup downLoadPop) {
        this.downLoadPop = downLoadPop;
    }

    public RichPopup getDownLoadPop() {
        return downLoadPop;
    }

    public void queryJob(ActionEvent actionEvent) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        System.out.println(sdf.format((Date)this.startTime.getLocalValue()));  
        ViewObject jobView = DmsUtils.getDmsApplicationModule().getDmsJobDetailsVO();
        ViewCriteria vc = jobView.createViewCriteria();
        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
        
        if(this.startTime.getLocalValue() != null){
            String sTime = sdf.format((Date)this.startTime.getLocalValue());
            vcr.setAttribute("CreatedAt", ">=to_date('"+sTime+"','yyyy-MM-dd')");
        }
        
        if(this.endTime.getLocalValue() != null){
            String eTime = sdf.format((Date)this.endTime.getLocalValue());
            vcr.setAttribute("EndTime", "<=to_date('"+eTime+"','yyyy-MM-dd')");
        }
        
        if(this.jobTypeSoc.getValue() != null){
            String type = this.jobTypeSoc.getValue().toString();
            System.out.println("type:"+type);    
            vcr.setAttribute("JobType", "like '%"+type+"%'");
        }
        
        if(this.userName.getValue() != null){
            String name = this.userName.getValue().toString();
            System.out.println("name:"+name);    
            vcr.setAttribute("CreatedBy", "like '%"+name+"%'");
        }
        
        if(this.jobName.getValue() != null){
            String jobId = this.jobName.getValue().toString();
            System.out.println("JobId:"+jobId);    
            vcr.setAttribute("JobId", "like '%"+jobId+"%'");
        }
        
        if(this.statusSoc.getValue() != null){
            String status = this.statusSoc.getValue().toString();
            System.out.println("status:"+status);    
            vcr.setAttribute("JobStatus", "like '%"+status+"%'");
        }
        
        vc.add(vcr);
        jobView.applyViewCriteria(vc);
        System.out.println("query:"+jobView.getQuery());
        jobView.executeQuery();
        jobView.getViewCriteriaManager().setApplyViewCriteriaName(null);
        
    }

    public void setStartTime(RichInputDate startTime) {
        this.startTime = startTime;
    }

    public RichInputDate getStartTime() {
        return startTime;
    }

    public void setEndTime(RichInputDate endTime) {
        this.endTime = endTime;
    }

    public RichInputDate getEndTime() {
        return endTime;
    }

    public void setJobTypeSoc(RichSelectOneChoice jobTypeSoc) {
        this.jobTypeSoc = jobTypeSoc;
    }

    public RichSelectOneChoice getJobTypeSoc() {
        return jobTypeSoc;
    }

    public void setUserName(RichInputText userName) {
        this.userName = userName;
    }

    public RichInputText getUserName() {
        return userName;
    }

    public void setJobName(RichInputText jobName) {
        this.jobName = jobName;
    }

    public RichInputText getJobName() {
        return jobName;
    }

    public void setStatusSoc(RichSelectOneChoice statusSoc) {
        this.statusSoc = statusSoc;
    }

    public RichSelectOneChoice getStatusSoc() {
        return statusSoc;
    }

    public void setDownFileName(String downFileName) {
        this.downFileName = downFileName;
    }

    public String getDownFileName() {
        return downFileName;
    }

    public void downLoadFile(FacesContext facesContext,
                             OutputStream outputStream) {
        System.out.println("File DownLoad Start..."+this.downFileName);
        FileInputStream fis;
        try {
            fis = new FileInputStream(this.downFilePath);
            byte[] bytes = new byte[1024];
            int count;
            while((count = fis.read(bytes)) >= 0){
                outputStream.write(bytes,0,count);    
            }
            outputStream.flush();
        } catch (FileNotFoundException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("找不到指定文件！"));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.downLoadPop.cancel();
    }

    public void canclDownLoadPop(ActionEvent actionEvent) {
        this.downLoadPop.cancel();
    }
}
