package dms.user;

import com.bea.security.utils.DigestUtils;

import common.ADFUtils;

import common.DmsUtils;

import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.validator.ValidatorException;

import oracle.adf.model.binding.DCDataControl;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adfinternal.view.faces.model.binding.FacesCtrlActionBinding;

import oracle.jbo.Row;

public class EditUserMBean {
    private RichInputText newPwd;
    private RichInputText pwd;
    private RichTable userTree;
    private static ADFLogger logger=ADFLogger.createADFLogger(EditUserMBean.class);

    public EditUserMBean() {
    }

    public void validatePassword(FacesContext facesContext,
                                 UIComponent uIComponent, Object object) throws ValidatorException {
        String n_pwd=(object+"").trim();
        String o_pwd=(this.pwd.getValue()+"").trim();
        if(n_pwd.equals(o_pwd)){
            DCIteratorBinding binding = ADFUtils.findIterator("DmsUserViewIterator");
            Row row=binding.getViewObject().getCurrentRow();
            String encyptPwd;
            try {
                encyptPwd =
                        DigestUtils.digestSHA1((row.getAttribute("Acc") + "").trim() +n_pwd + "");
                row.setAttribute("Pwd", encyptPwd);
                binding.getDataControl().setTransactionModified();
            } catch (Exception e) {
                logger.severe(e);
            }
        }else{
            //String msgTitle=DmsUtils.getMsg("dms.common.error");
            String msg=DmsUtils.getMsg("dms_user.password_inconsitent");
            FacesMessage fm =new FacesMessage("",msg);
            throw new ValidatorException(fm);
        }
        
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

    public void setUserTree(RichTable userTree) {
        this.userTree = userTree;
    }

    public RichTable getUserTree() {
        return userTree;
    }
}
