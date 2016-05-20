package dgpt;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class ValuesModel {
    public ValuesModel() {
        super();
    }
    
    private String label;
    private List<String> values;
    private List<SelectItem> simList;

    public List suggestItems(String str){
        List<SelectItem> items = new ArrayList<SelectItem>();
        for(String s : values){
            if(s.startsWith(str)){
                SelectItem sim = new SelectItem();
                sim.setLabel(s);
                sim.setValue(s);
                items.add(sim);
            }    
        }
        return items;
    }
    
    public List<SelectItem> getValuesItems(){
        List<SelectItem> items = new ArrayList<SelectItem>();
        for(String s : values){
            SelectItem sim = new SelectItem();
            sim.setLabel(s);
            sim.setValue(s);
            items.add(sim);
        }
        return items;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setValues(List<String> values) {
        this.values = values;
        this.simList = this.getValuesItems();
    }

    public List<String> getValues() {
        return values;
    }

    public List<SelectItem> getSimList() {
        return simList;
    }
}
