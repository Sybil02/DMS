package team.epm.module;

import java.util.Map;

import oracle.adf.share.ADFContext;

import oracle.jbo.Row;
import oracle.jbo.Session;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewLinkImpl;
import oracle.jbo.server.ViewObjectImpl;

import team.epm.dms.view.DmsEnabledGroupViewImpl;
import team.epm.dms.view.DmsEnabledRoleImpl;
import team.epm.dms.view.DmsFunctionViewImpl;
import team.epm.dms.view.DmsGroupRoleViewImpl;
import team.epm.dms.view.DmsGroupViewImpl;
import team.epm.dms.view.DmsGroupedRoleViewImpl;
import team.epm.dms.view.DmsGroupedUserViewImpl;
import team.epm.dms.view.DmsMenuTreeViewImpl;
import team.epm.dms.view.DmsRoleViewImpl;
import team.epm.dms.view.DmsUnGroupedRoleViewImpl;
import team.epm.dms.view.DmsUnGroupedUserViewImpl;
import team.epm.dms.view.DmsUserGroupViewImpl;
import team.epm.dms.view.DmsUserViewImpl;
import team.epm.dms.view.DmsValueSetViewImpl;
import team.epm.module.common.DmsModule;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Thu Dec 18 16:42:00 CST 2014
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DmsModuleImpl extends ApplicationModuleImpl implements DmsModule {
    /**
     * This is the default constructor (do not remove).
     */
    public DmsModuleImpl() {
    }
    
    @Override
    protected void prepareSession(Session session) {
        super.prepareSession(session);
        Map sessionScope = ADFContext.getCurrent().getSessionScope();
        Object userId = sessionScope.get("userId");
        if (userId != null) {
            this.getSession().getUserData().put("userId", userId);
        }
    }


    /**
     * Container's getter for DmsAuditMsgView.
     * @return DmsAuditMsgView
     */
    public ViewObjectImpl getDmsAuditMsgView() {
        return (ViewObjectImpl)findViewObject("DmsAuditMsgView");
    }

    /**
     * Container's getter for DmsFunctionView.
     * @return DmsFunctionView
     */
    public DmsFunctionViewImpl getDmsFunctionView() {
        return (DmsFunctionViewImpl)findViewObject("DmsFunctionView");
    }

    /**
     * Container's getter for DmsGroupRoleView.
     * @return DmsGroupRoleView
     */
    public DmsGroupRoleViewImpl getDmsGroupRoleView() {
        return (DmsGroupRoleViewImpl)findViewObject("DmsGroupRoleView");
    }

    /**
     * Container's getter for DmsGroupView.
     * @return DmsGroupView
     */
    public DmsGroupViewImpl getDmsGroupView() {
        return (DmsGroupViewImpl)findViewObject("DmsGroupView");
    }

    /**
     * Container's getter for DmsMenuView.
     * @return DmsMenuView
     */
    public ViewObjectImpl getDmsMenuView() {
        return (ViewObjectImpl)findViewObject("DmsMenuView");
    }

    /**
     * Container's getter for DmsPropertyView.
     * @return DmsPropertyView
     */
    public ViewObjectImpl getDmsPropertyView() {
        return (ViewObjectImpl)findViewObject("DmsPropertyView");
    }

    /**
     * Container's getter for DmsRoleFunctionView.
     * @return DmsRoleFunctionView
     */
    public ViewObjectImpl getDmsRoleFunctionView() {
        return (ViewObjectImpl)findViewObject("DmsRoleFunctionView");
    }

    /**
     * Container's getter for DmsRoleMenuView.
     * @return DmsRoleMenuView
     */
    public ViewObjectImpl getDmsRoleMenuView() {
        return (ViewObjectImpl)findViewObject("DmsRoleMenuView");
    }

    /**
     * Container's getter for DmsRoleView.
     * @return DmsRoleView
     */
    public DmsRoleViewImpl getDmsRoleView() {
        return (DmsRoleViewImpl)findViewObject("DmsRoleView");
    }

    /**
     * Container's getter for DmsUserGroupView.
     * @return DmsUserGroupView
     */
    public DmsUserGroupViewImpl getDmsUserGroupView() {
        return (DmsUserGroupViewImpl)findViewObject("DmsUserGroupView");
    }

    /**
     * Container's getter for DmsUserKeyView.
     * @return DmsUserKeyView
     */
    public ViewObjectImpl getDmsUserKeyView() {
        return (ViewObjectImpl)findViewObject("DmsUserKeyView");
    }

    /**
     * Container's getter for DmsUserView.
     * @return DmsUserView
     */
    public DmsUserViewImpl getDmsUserView() {
        return (DmsUserViewImpl)findViewObject("DmsUserView");
    }

    /**
     * Container's getter for DmsValueSetView.
     * @return DmsValueSetView
     */
    public DmsValueSetViewImpl getDmsValueSetView() {
        return (DmsValueSetViewImpl)findViewObject("DmsValueSetView");
    }

    /**
     * Container's getter for DmsMenuTreeView.
     * @return DmsMenuTreeView
     */
    public DmsMenuTreeViewImpl getDmsMenuTreeView() {
        return (DmsMenuTreeViewImpl)findViewObject("DmsMenuTreeView");
    }

    /**
     * Container's getter for DmsLookupView.
     * @return DmsLookupView
     */
    public ViewObjectImpl getDmsLookupView() {
        return (ViewObjectImpl)findViewObject("DmsLookupView");
    }

    /**
     * Container's getter for DmsUserFunctionView.
     * @return DmsUserFunctionView
     */
    public ViewObjectImpl getDmsUserFunctionView() {
        return (ViewObjectImpl)findViewObject("DmsUserFunctionView");
    }

    /**
     * Container's getter for DmsSubMenuQueryView.
     * @return DmsSubMenuQueryView
     */
    public ViewObjectImpl getDmsSubMenuQueryView() {
        return (ViewObjectImpl)findViewObject("DmsSubMenuQueryView");
    }

    /**
     * Container's getter for DmsUnGroupedUserView.
     * @return DmsUnGroupedUserView
     */
    public DmsUnGroupedUserViewImpl getDmsUnGroupedUserView() {
        return (DmsUnGroupedUserViewImpl)findViewObject("DmsUnGroupedUserView");
    }

    /**
     * Container's getter for DmsEnabledGroupView.
     * @return DmsEnabledGroupView
     */
    public DmsEnabledGroupViewImpl getDmsEnabledGroupView() {
        return (DmsEnabledGroupViewImpl)findViewObject("DmsEnabledGroupView");
    }

    /**
     * Container's getter for DmsEnabledRole.
     * @return DmsEnabledRole
     */
    public DmsEnabledRoleImpl getDmsEnabledRole() {
        return (DmsEnabledRoleImpl)findViewObject("DmsEnabledRole");
    }

    /**
     * Container's getter for DmsGroupedRoleView.
     * @return DmsGroupedRoleView
     */
    public DmsGroupedRoleViewImpl getDmsGroupedRoleView() {
        return (DmsGroupedRoleViewImpl)findViewObject("DmsGroupedRoleView");
    }

    /**
     * Container's getter for DmsGroupedUserView.
     * @return DmsGroupedUserView
     */
    public DmsGroupedUserViewImpl getDmsGroupedUserView() {
        return (DmsGroupedUserViewImpl)findViewObject("DmsGroupedUserView");
    }

    /**
     * Container's getter for DmsUnGroupedRoleView.
     * @return DmsUnGroupedRoleView
     */
    public DmsUnGroupedRoleViewImpl getDmsUnGroupedRoleView() {
        return (DmsUnGroupedRoleViewImpl)findViewObject("DmsUnGroupedRoleView");
    }

    /**
     * Container's getter for DmsGroupedRoleLnk.
     * @return DmsGroupedRoleLnk
     */
    public ViewLinkImpl getDmsGroupedRoleLnk() {
        return (ViewLinkImpl)findViewLink("DmsGroupedRoleLnk");
    }

    /**
     * Container's getter for DmsGroupedUserLnk.
     * @return DmsGroupedUserLnk
     */
    public ViewLinkImpl getDmsGroupedUserLnk() {
        return (ViewLinkImpl)findViewLink("DmsGroupedUserLnk");
    }

    
    public ViewObject getDmsValueView(String valueSetSrc,String valueSetId,String groupId){
        String voName="DmsVs"+valueSetSrc;
        ViewObject vo=this.getApplicationModule().findViewObject(voName);
        if(vo==null&&valueSetSrc!=null){
            String sql="select t.code,t.meaning from \""+valueSetSrc.toUpperCase()+"\" t where t.locale='"
                +ADFContext.getCurrent().getLocale()+"' and not exists(select 1 from dms_group_value v where v.group_id='"+groupId+"' and v.value_id=t.code and v.value_set_id='"+valueSetId+"') order by t.idx";
            vo=this.getApplicationModule().createViewObjectFromQueryStmt(voName, sql);
        }
        return vo;
    }

    /**
     * Container's getter for DmsRoleFunctionLnk.
     * @return DmsRoleFunctionLnk
     */
    public ViewLinkImpl getDmsRoleFunctionLnk() {
        return (ViewLinkImpl)findViewLink("DmsRoleFunctionLnk");
    }

    /**
     * Container's getter for DmsUnassignedFunctionView.
     * @return DmsUnassignedFunctionView
     */
    public ViewObjectImpl getDmsUnassignedFunctionView() {
        return (ViewObjectImpl)findViewObject("DmsUnassignedFunctionView");
    }

    /**
     * Container's getter for DmsGroupValueView.
     * @return DmsGroupValueView
     */
    public ViewObjectImpl getDmsGroupValueView() {
        return (ViewObjectImpl)findViewObject("DmsGroupValueView");
    }

    /**
     * Container's getter for DmsGroupValueLnk.
     * @return DmsGroupValueLnk
     */
    public ViewLinkImpl getDmsGroupValueLnk() {
        return (ViewLinkImpl)findViewLink("DmsGroupValueLnk");
    }

    /**
     * Container's getter for DmsValuesetGValueLnk.
     * @return DmsValuesetGValueLnk
     */
    public ViewLinkImpl getDmsValuesetGValueLnk() {
        return (ViewLinkImpl)findViewLink("DmsValuesetGValueLnk");
    }

    /**
     * Container's getter for DmsGroupGValueLnk.
     * @return DmsGroupGValueLnk
     */
    public ViewLinkImpl getDmsGroupGValueLnk() {
        return (ViewLinkImpl)findViewLink("DmsGroupGValueLnk");
    }

    /**
     * Container's getter for DmsJobDetailsVO.
     * @return DmsJobDetailsVO
     */
    public ViewObjectImpl getDmsJobDetailsVO() {
        return (ViewObjectImpl)findViewObject("DmsJobDetailsVO");
    }
}
