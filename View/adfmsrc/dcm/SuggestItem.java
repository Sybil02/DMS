package dcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import oracle.adf.view.rich.model.AutoSuggestUIHints;

class SuggestItem{
        String valueItem;
        Map valueSet;
        
        SuggestItem(Map valueSet, String value)
        {
            this.valueItem = value;
            this.valueSet = valueSet;
        }
        
        public List suggestMethod(FacesContext facesContext,
                                     AutoSuggestUIHints autoSuggestUIHints) {
           // System.out.println(autoSuggestUIHints.getSubmittedValue());
           // System.out.println(autoSuggestUIHints.getMaxSuggestedItems());
            
            SelectItem item = new SelectItem("1","一号","一号描述");
            List<SelectItem> items = new ArrayList<SelectItem>();
            items.add(item);
            return (List)valueSet.get(valueItem);
        }
        public void aaa()
        {}
    }
