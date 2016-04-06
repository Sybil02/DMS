package mailTimer;

import common.DmsUtils;

import javax.faces.event.ActionEvent;

public class TempTimerBean {

    public void saveAndRefresh(ActionEvent actionEvent) {
        DmsUtils.getDmsApplicationModule().getTransaction().commit();
    }
    
    public void refreshJob(){
        
    }
    
}
