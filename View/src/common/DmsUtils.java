package common;

import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

public class DmsUtils {
    public DmsUtils() {
        super();
    }
    public static String getMsg(String key){
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "dms_v");
        return bundle.getString(key);
    }
}
