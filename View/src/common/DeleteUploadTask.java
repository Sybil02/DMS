package common;

import java.io.File;

import java.util.Calendar;
import java.util.TimerTask;

import javax.faces.model.DataModel;

import javax.servlet.ServletContext;

import oracle.jbo.ViewObject;

/*
 * 删除上传excel的临时文件
 */

public class DeleteUploadTask extends TimerTask {
    
    
    public DeleteUploadTask(ServletContext context) {  
          this.context = context;  
      }  

    private static final int C_SCHEDULE_HOUR = 3;
    
    private static boolean isRunning = false;  
    
    private ServletContext context = null;  
    
    public void run() {
        
        Calendar cal = Calendar.getInstance();  
         if (!isRunning) {  

            // if (C_SCHEDULE_HOUR == cal.get(Calendar.HOUR_OF_DAY) ) {  
                 isRunning = true;  
                 context.log("开始执行指定任务");  
                 System.out.println("执行任务");
                 File file = new File("DMS/UPLOAD/");
                 System.out.println("定时删除文件路径*************************"+file.getAbsolutePath());
                 deleteDir(file);
                 File tempfiles = new File("");
                 File files = new File(tempfiles.getAbsoluteFile()+"/");
                 deleteXlsx(files);
                 isRunning = false;  
                 context.log("指定任务执行结束");  
             }   
    }
    private long sevenDay = 7 * 24 * 60  *60 * 1000;
    private long threeHours = 24 * 60 * 60 * 1000;
    
    private void deleteDir(File file) {

        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                if(System.currentTimeMillis() - f.lastModified() >  sevenDay)
                    deleteDir(f);
            }
            //把目录也删了
            boolean b = file.delete();
        }else {
            System.out.println("delete  ");
            file.delete();
        }
    }
    
    private void deleteXlsx(File file){
        System.out.println("Check File Path............"+file.getAbsolutePath());
        if(file.isDirectory()){
            for(File xf : file.listFiles()){
                if(xf.getName().endsWith(".xlsx")){
                    System.out.println("delete xlsx"+xf.getName());
                    xf.delete();    
                }
            }    
        }
    }
}
