package dcm.template;

import common.ADFUtils;

import javax.faces.event.ActionEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;

import oracle.jbo.RowSetIterator;

public class TemplateBackingBean {
    private RichCommandToolbarButton ctb;
    
    
    public boolean is_Disabled(){
        DCIteratorBinding iter=ADFUtils.findIterator("DcmTemplateColumnView1Iterator");
        RowSetIterator rowiter= iter.getRowSetIterator();
        if(rowiter.hasNext()){
            return false;
        }
        else  return true;
    }
    public void setCtb(RichCommandToolbarButton ctb) {
        this.ctb = ctb;
    }

    public RichCommandToolbarButton getCtb() {
          if (ctb==null)
            return ctb;
        DCIteratorBinding iter=ADFUtils.findIterator("DcmTemplateColumnView1Iterator");
        RowSetIterator rowiter= iter.getRowSetIterator();
        if(rowiter.hasNext()){
            ctb.setDisabled(false);
        }
        else ctb.setDisabled(true);
       return ctb;
    }
}
