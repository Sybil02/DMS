package dcm.template;

import common.ADFUtils;

import javax.faces.event.ActionEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;

import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.RowImpl;
import oracle.jbo.server.ViewRowImpl;

public class TemplateBackingBean {
    private RichCommandToolbarButton ctb;
    
    
    public boolean is_Disabled(){
        DCIteratorBinding iter=ADFUtils.findIterator("DcmTemplateColumnView1Iterator");
        ViewObject vo= iter.getViewObject();
        vo.getCurrentRow();
        ViewRowImpl row = (ViewRowImpl)vo.getCurrentRow();
        byte  b=row.getEntity(0).getEntityState();
        if(b==0) {
           
            return true;
        }
        else  return false;
    }
    public void setCtb(RichCommandToolbarButton ctb) {
        this.ctb = ctb;
    }

    public RichCommandToolbarButton getCtb() {
       return ctb;
    }
}
