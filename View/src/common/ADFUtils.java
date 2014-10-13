package common;


import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import javax.servlet.ServletContext;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.model.binding.DCParameter;
import oracle.adf.share.logging.ADFLogger;


import oracle.adf.view.rich.component.rich.RichDialog;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.AttributeBinding;
import oracle.binding.BindingContainer;
import oracle.binding.ControlBinding;
import oracle.binding.OperationBinding;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.domain.Date;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
import oracle.jbo.uicli.binding.JUCtrlValueBinding;

import org.apache.myfaces.trinidad.component.UIXComponentBase;
import org.apache.myfaces.trinidad.component.UIXEditableValue;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

/**
 * A series of convenience functions for dealing with ADF Bindings.
 * Note: Updated for JDeveloper 11
 *
 * @author Duncan Mills
 * @author Steve Muench
 *
 * $Id: ADFUtils.java 2513 2007-09-20 20:39:13Z ralsmith $.
 */
public class ADFUtils {

    public static final ADFLogger LOGGER =
        ADFLogger.createADFLogger(ADFUtils.class);

    /**
     * Get application module for an application module data control by name.
     * @param name application module data control name
     * @return ApplicationModule
     */
    public static ApplicationModule getApplicationModuleForDataControl(String name) {
        return (ApplicationModule)JSFUtils.resolveExpression("#{data." + name +
                                                             ".dataProvider}");
    }

    /**
     * A convenience method for getting the value of a bound attribute in the
     * current page context programatically.
     * @param attributeName of the bound value in the pageDef
     * @return value of the attribute
     */
    public static Object getBoundAttributeValue(String attributeName) {
        return findControlBinding(attributeName).getInputValue();
    }

    /**
     * A convenience method for setting the value of a bound attribute in the
     * context of the current page.
     * @param attributeName of the bound value in the pageDef
     * @param value to set
     */
    public static void setBoundAttributeValue(String attributeName,
                                              Object value) {
        findControlBinding(attributeName).setInputValue(value);
    }

    /**
     * Returns the evaluated value of a pageDef parameter.
     * @param pageDefName reference to the page definition file of the page with the parameter
     * @param parameterName name of the pagedef parameter
     * @return evaluated value of the parameter as a String
     */
    public static Object getPageDefParameterValue(String pageDefName,
                                                  String parameterName) {
        BindingContainer bindings = findBindingContainer(pageDefName);
        DCParameter param =
            ((DCBindingContainer)bindings).findParameter(parameterName);
        return param.getValue();
    }

    /**
     * Convenience method to find a DCControlBinding as an AttributeBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @param bindingContainer binding container
     * @param attributeName name of the attribute binding.
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(BindingContainer bindingContainer,
                                                      String attributeName) {
        if (attributeName != null) {
            if (bindingContainer != null) {
                ControlBinding ctrlBinding =
                    bindingContainer.getControlBinding(attributeName);
                if (ctrlBinding instanceof AttributeBinding) {
                    return (AttributeBinding)ctrlBinding;
                }
            }
        }
        return null;
    }

    /**
     * Convenience method to find a DCControlBinding as a JUCtrlValueBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @param attributeName name of the attribute binding.
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(String attributeName) {
        return findControlBinding(getBindingContainer(), attributeName);
    }

    /**
     * Return the current page's binding container.
     * @return the current page's binding container
     */
    public static BindingContainer getBindingContainer() {
        return (BindingContainer)JSFUtils.resolveExpression("#{bindings}");
    }

    /**
     * Return the Binding Container as a DCBindingContainer.
     * @return current binding container as a DCBindingContainer
     */
    public static DCBindingContainer getDCBindingContainer() {
        return (DCBindingContainer)getBindingContainer();
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName,
                                                          String valueAttrName,
                                                          String displayAttrName) {
        return selectItemsForIterator(findIterator(iteratorName),
                                      valueAttrName, displayAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute to use for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName,
                                                          String valueAttrName,
                                                          String displayAttrName,
                                                          String descriptionAttrName) {
        return selectItemsForIterator(findIterator(iteratorName),
                                      valueAttrName, displayAttrName,
                                      descriptionAttrName);
    }

    /**
     * Get List of attribute values for an iterator.
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName value attribute to use
     * @return List of attribute values for an iterator
     */
    public static List attributeListForIterator(String iteratorName,
                                                String valueAttrName) {
        return attributeListForIterator(findIterator(iteratorName),
                                        valueAttrName);
    }

    /**
     * Get List of Key objects for rows in an iterator.
     * @param iteratorName iterabot binding name
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(String iteratorName) {
        return keyListForIterator(findIterator(iteratorName));
    }

    /**
     * Get List of Key objects for rows in an iterator.
     * @param iter iterator binding
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(DCIteratorBinding iter) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getKey());
        }
        return attributeList;
    }

    /**
     * Get List of Key objects for rows in an iterator using key attribute.
     * @param iteratorName iterator binding name
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(String iteratorName,
                                                   String keyAttrName) {
        return keyAttrListForIterator(findIterator(iteratorName), keyAttrName);
    }

    /**
     * Get List of Key objects for rows in an iterator using key attribute.
     *
     * @param iter iterator binding
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(DCIteratorBinding iter,
                                                   String keyAttrName) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(new Key(new Object[] { r.getAttribute(keyAttrName) }));
        }
        return attributeList;
    }

    /**
     * Get a List of attribute values for an iterator.
     *
     * @param iter iterator binding
     * @param valueAttrName name of value attribute to use
     * @return List of attribute values
     */
    public static List attributeListForIterator(DCIteratorBinding iter,
                                                String valueAttrName) {
        List attributeList = new ArrayList();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getAttribute(valueAttrName));
        }
        return attributeList;
    }

    /**
     * Find an iterator binding in the current binding container by name.
     *
     * @param name iterator binding name
     * @return iterator binding
     */
    public static DCIteratorBinding findIterator(String name) {
        DCIteratorBinding iter =
            getDCBindingContainer().findIteratorBinding(name);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + name + "' not found");
        }
        return iter;
    }

    public static DCIteratorBinding findIterator(String bindingContainer,
                                                 String iterator) {
        DCBindingContainer bindings =
            (DCBindingContainer)JSFUtils.resolveExpression("#{" +
                                                           bindingContainer +
                                                           "}");
        if (bindings == null) {
            throw new RuntimeException("Binding container '" +
                                       bindingContainer + "' not found");
        }
        DCIteratorBinding iter = bindings.findIteratorBinding(iterator);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + iterator +
                                       "' not found");
        }
        return iter;
    }

    public static JUCtrlValueBinding findCtrlBinding(String name) {
        JUCtrlValueBinding rowBinding =
            (JUCtrlValueBinding)getDCBindingContainer().findCtrlBinding(name);
        if (rowBinding == null) {
            throw new RuntimeException("CtrlBinding " + name + "' not found");
        }
        return rowBinding;
    }

    /**
     * Find an operation binding in the current binding container by name.
     *
     * @param name operation binding name
     * @return operation binding
     */
    public static OperationBinding findOperation(String name) {
        OperationBinding op =
            getDCBindingContainer().getOperationBinding(name);
        if (op == null) {
            throw new RuntimeException("Operation '" + name + "' not found");
        }
        return op;
    }

    /**
     * Find an operation binding in the current binding container by name.
     *
     * @param bindingContianer binding container name
     * @param opName operation binding name
     * @return operation binding
     */
    public static OperationBinding findOperation(String bindingContianer,
                                                 String opName) {
        DCBindingContainer bindings =
            (DCBindingContainer)JSFUtils.resolveExpression("#{" +
                                                           bindingContianer +
                                                           "}");
        if (bindings == null) {
            throw new RuntimeException("Binding container '" +
                                       bindingContianer + "' not found");
        }
        OperationBinding op = bindings.getOperationBinding(opName);
        if (op == null) {
            throw new RuntimeException("Operation '" + opName + "' not found");
        }
        return op;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter,
                                                          String valueAttrName,
                                                          String displayAttrName,
                                                          String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName),
                                           (String)r.getAttribute(displayAttrName),
                                           (String)r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter,
                                                          String valueAttrName,
                                                          String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName),
                                           (String)r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each gzllcslRow as the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName,
                                                               String displayAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName),
                                           displayAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each gzllcslRow as the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName,
                                                               String displayAttrName,
                                                               String descriptionAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName),
                                           displayAttrName,
                                           descriptionAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each gzllcslRow as the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter,
                                                               String displayAttrName,
                                                               String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(),
                                           (String)r.getAttribute(displayAttrName),
                                           (String)r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each gzllcslRow as the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return List of ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter,
                                                               String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(),
                                           (String)r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * Find the BindingContainer for a page definition by name.
     *
     * Typically used to refer eagerly to page definition parameters. It is
     * not best practice to reference or set bindings in binding containers
     * that are not the one for the current page.
     *
     * @param pageDefName name of the page defintion XML file to use
     * @return BindingContainer ref for the named definition
     */
    private static BindingContainer findBindingContainer(String pageDefName) {
        BindingContext bctx = getDCBindingContainer().getBindingContext();
        BindingContainer foundContainer =
            bctx.findBindingContainer(pageDefName);
        return foundContainer;
    }

    public static void printOperationBindingExceptions(List opList) {
        if (opList != null && !opList.isEmpty()) {
            for (Object error : opList) {
                LOGGER.severe(error.toString());
            }
        }
    }

    /**
     * @author 韩吉�?
     * @function 传入ViewObjectImpl、字段名称�?查询目标、比较符号，将设置VO的查询条件并执行查询，�?用于单一查询条件�?
     * @param vo
     * @param attributeName
     * @param attributeValue
     * @param operator
     * @return null
     */
    public static ViewCriteria setViewCriteria(ViewObjectImpl vo,
                                               String attributeName,
                                               Object attributeValue,
                                               String operator) {
        ViewCriteria vc = vo.createViewCriteria();
        ViewCriteriaRow vcRow = vc.createViewCriteriaRow();
        vcRow.setAttribute(attributeName, attributeValue);
        vcRow.setOperator(attributeName, operator);
        vc.addElement(vcRow);
        return vc;
    }

    public static void queryWithViewCriteria(ViewObjectImpl vo,
                                             String attributeName,
                                             Object attributeValue,
                                             String operator) {
        ViewCriteria vc = vo.createViewCriteria();
        ViewCriteriaRow vcRow = vc.createViewCriteriaRow();
        vcRow.setAttribute(attributeName, attributeValue);
        vcRow.setOperator(attributeName, operator);
        vc.addElement(vcRow);
        vo.applyViewCriteria(vc);
        vo.executeQuery();
        vo.getViewCriteriaManager().clearViewCriterias();
    }

    /**
     * @function ManagedBean中引用此方法，传入的PopUp对象作参数，页面上将弹出与此参数对象绑定的PopUp组件�?
     * @param pop
     * @return null
     */
    public static void showPopup(RichPopup pop) {
        FacesContext context = FacesContext.getCurrentInstance();
        String popupId = pop.getClientId(context);
        StringBuilder script = new StringBuilder();
        script.append("var popup = AdfPage.PAGE.findComponent('").append(popupId).append("'); ").append("if (!popup.isPopupVisible()) { ").append("var hints = {}; ").append("popup.show(hints);}");
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    public static void closePopup(RichPopup pop) {
        FacesContext context = FacesContext.getCurrentInstance();
        String popupId = pop.getClientId(context);
        StringBuilder script = new StringBuilder();
        script.append("var popup = AdfPage.PAGE.findComponent('").append(popupId).append("'); ").append("if (popup.isPopupVisible()) { ").append("var hints = {}; ").append("popup.hide();}");
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    public static void buttonClink(UIXComponentBase button) {
        FacesContext context = FacesContext.getCurrentInstance();
        String id = button.getClientId(context);
        StringBuilder script = new StringBuilder();
        script.append("document.getElementById('" + id + "').click();");
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    public static void buttonClink(String buttonId) {
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder script = new StringBuilder();
        script.append("document.getElementById('" + buttonId + "').click();");
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    /**
     * @function 自定义弹出PopUp，传入参数为message和type�?
     *           方法将message设置到outputText，再根据type来设置dailog的icon，最后执行弹出方法�?
     * @param message
     * @param type
     */
    public static void showCustomPopUp(RichPopup popup, RichDialog dialog,
                                       RichOutputText outputText,
                                       String message, String type) {
        outputText.setValue(message);
        if ("confirm".equals(type)) {
            dialog.setTitleIconSource("/res/image/common/dialog_confirm.png");
        }

        if ("for_sure".equals(type)) {
            dialog.setTitleIconSource("/res/image/common/dialog_for_sure.png");
        }

        if ("alert".equals(type)) {
            dialog.setTitleIconSource("/res/image/common/dialog_alert.png");
        }

        if ("error".equals(type)) {
            dialog.setTitleIconSource("/res/image/common/dialog_error.png");
        }
        showPopup(popup);
    }

    /**
     * wzq
     */
    public static void showErrorCustomPopUp(RichPopup popup, RichDialog dialog,
                                            RichOutputText outputText,
                                            String message) {
        showCustomPopUp(popup, dialog, outputText, message, "error");
    }

    /**
     * wzq
     */
    public static void showAlertCustomPopUp(RichPopup popup, RichDialog dialog,
                                            RichOutputText outputText,
                                            String message) {
        showCustomPopUp(popup, dialog, outputText, message, "alert");
    }

    /**
     * @author 韩吉�?
     * @function 清空�?��可编辑控件的value
     * @param args
     */
    public static void clearValueOfComponent(UIXEditableValue... args) {
        for (UIXEditableValue uic : args) {
            uic.resetValue();
        }
    }

    /**
     * @author 韩吉�?
     * @function 判断table是否选中
     * @param table
     * @return
     */
    public static boolean isSelectedTableRow(RichTable table) {
        Object row = table.getSelectedRowData();
        if (row != null) {
            return true;
        } else {
            return false;
        }
    }

    public static void refreshTarget(UIComponent... args) {
        if (args != null)
            for (UIComponent u : args) {
                if (u != null)
                    AdfFacesContext.getCurrentInstance().addPartialTarget(u);
            }
    }

    public static String getConfPath() {
        FacesContext context = FacesContext.getCurrentInstance();
        ServletContext servletContext =
            (ServletContext)context.getExternalContext().getContext();
        String path = servletContext.getRealPath("/WEB-INF");
        return path;
    }

    public static Row getCurrentRow(String dataControlsId) {

        DCBindingContainer bc =
            (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry(); //获得Binding容器
        Row row = null;
        if (bc.findIteratorBinding(dataControlsId) != null) {
            row =
    bc.findIteratorBinding(dataControlsId).getCurrentRow(); //获得当前�?
        }
        return row;
    }

    public static List<JUCtrlHierNodeBinding> getSelectedRows(RichTable table) {
        List<JUCtrlHierNodeBinding> rows =
            new ArrayList<JUCtrlHierNodeBinding>();
        RowKeySet rks = table.getSelectedRowKeys();
        if (rks.size() > 0) {
            Iterator it = rks.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                table.setRowKey(o);
                JUCtrlHierNodeBinding rowData =
                    (JUCtrlHierNodeBinding)table.getRowData();
                rows.add(rowData);
            }
        }
        return rows;
    }

    public static List<JUCtrlHierNodeBinding> getAllRows(RichTable table) {
        List<JUCtrlHierNodeBinding> rows =
            new ArrayList<JUCtrlHierNodeBinding>();
        int i = 0;
        while (i < table.getRows()) {
            JUCtrlHierNodeBinding rowData =
                (JUCtrlHierNodeBinding)table.getRowData(i);
            rows.add(rowData);
            i++;
        }
        return rows;
    }

    /**
     * 根据id，查找行
     * @param rowId
     */
    //    public static Row findRowById(ViewObjectImpl vo, String attributeId,
    //                                  Integer rowId) {
    //        ViewCriteria vc = vo.createViewCriteria();
    //        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
    //        vcr.setAttribute(attributeId, rowId);
    //        vc.add(vcr);
    //        RowIterator ri =
    //            vo.findByViewCriteria(vc, SysCodeConstants.paging_number, 4);
    //        while (ri.hasNext()) {
    //            return ri.next();
    //        }
    //        return null;
    //    }

    public static Map<String, Object> getViewScope() {
        return AdfFacesContext.getCurrentInstance().getViewScope();
    }

    public static Map<String, Object> getRequestScope() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
    }

    public static Map<String, Object> getPageflowScope() {
        return AdfFacesContext.getCurrentInstance().getPageFlowScope();
    }

    public static void dbCommit(ApplicationModuleImpl am) {
        try {
            am.getDBTransaction().commit();
        } catch (Exception e) {
            am.getDBTransaction().rollback();
        }
    }

    public static Date getCurrentDate() {
        return new Date(new Timestamp(new java.util.Date().getTime()));
    }
    
    public static String getCurrentYearNO() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new java.util.Date());
        return date.substring(0, 4);
    }
    
    public static void excuteScript(StringBuilder script) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

}
