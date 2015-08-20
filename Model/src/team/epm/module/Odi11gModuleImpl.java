package team.epm.module;

import java.util.Map;

import oracle.adf.share.ADFContext;

import oracle.jbo.Session;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewLinkImpl;
import oracle.jbo.server.ViewObjectImpl;

import team.epm.dms.view.DmsEnabledRoleImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Fri Jan 23 20:04:50 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class Odi11gModuleImpl extends ApplicationModuleImpl {
    /**
     * This is the default constructor (do not remove).
     */
    public Odi11gModuleImpl() {
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
     * Container's getter for Odi11AgentView.
     * @return Odi11AgentView
     */
    public ViewObjectImpl getOdi11AgentView() {
        return (ViewObjectImpl)findViewObject("Odi11AgentView");
    }

    /**
     * Container's getter for Odi11ParameterView.
     * @return Odi11ParameterView
     */
    public ViewObjectImpl getOdi11ParameterView() {
        return (ViewObjectImpl)findViewObject("Odi11ParameterView");
    }

    /**
     * Container's getter for Odi11SceneCatView.
     * @return Odi11SceneCatView
     */
    public ViewObjectImpl getOdi11SceneCatView() {
        return (ViewObjectImpl)findViewObject("Odi11SceneCatView");
    }

    /**
     * Container's getter for Odi11SceneExecView.
     * @return Odi11SceneExecView
     */
    public ViewObjectImpl getOdi11SceneExecView() {
        return (ViewObjectImpl)findViewObject("Odi11SceneExecView");
    }

    /**
     * Container's getter for Odi11SceneParamView.
     * @return Odi11SceneParamView
     */
    public ViewObjectImpl getOdi11SceneParamView() {
        return (ViewObjectImpl)findViewObject("Odi11SceneParamView");
    }

    /**
     * Container's getter for Odi11SceneView.
     * @return Odi11SceneView
     */
    public ViewObjectImpl getOdi11SceneView() {
        return (ViewObjectImpl)findViewObject("Odi11SceneView");
    }

    /**
     * Container's getter for Odi11WorkrepView.
     * @return Odi11WorkrepView
     */
    public ViewObjectImpl getOdi11WorkrepView() {
        return (ViewObjectImpl)findViewObject("Odi11WorkrepView");
    }

    /**
     * Container's getter for Odi11RoleSceneView.
     * @return Odi11RoleSceneView
     */
    public ViewObjectImpl getOdi11RoleSceneView() {
        return (ViewObjectImpl)findViewObject("Odi11RoleSceneView");
    }

    /**
     * Container's getter for DmsEnabledRole.
     * @return DmsEnabledRole
     */
    public DmsEnabledRoleImpl getDmsEnabledRole() {
        return (DmsEnabledRoleImpl)findViewObject("DmsEnabledRole");
    }

    /**
     * Container's getter for Odi11gRoleSceneLnk.
     * @return Odi11gRoleSceneLnk
     */
    public ViewLinkImpl getOdi11gRoleSceneLnk() {
        return (ViewLinkImpl)findViewLink("Odi11gRoleSceneLnk");
    }

    /**
     * Container's getter for Odi11UnauthedSceneView.
     * @return Odi11UnauthedSceneView
     */
    public ViewObjectImpl getOdi11UnauthedSceneView() {
        return (ViewObjectImpl)findViewObject("Odi11UnauthedSceneView");
    }

    /**
     * Container's getter for Odi11gCatSceneLnk.
     * @return Odi11gCatSceneLnk
     */
    public ViewLinkImpl getOdi11gCatSceneLnk() {
        return (ViewLinkImpl)findViewLink("Odi11gCatSceneLnk");
    }

    /**
     * Container's getter for Odi11AuthedSceneView.
     * @return Odi11AuthedSceneView
     */
    public ViewObjectImpl getOdi11AuthedSceneView() {
        return (ViewObjectImpl)findViewObject("Odi11AuthedSceneView");
    }

    /**
     * Container's getter for Odi11SceneParamExView.
     * @return Odi11SceneParamExView
     */
    public ViewObjectImpl getOdi11SceneParamExView() {
        return (ViewObjectImpl)findViewObject("Odi11SceneParamExView");
    }

    /**
     * Container's getter for Odi11SceneLogView.
     * @return Odi11SceneLogView
     */
    public ViewObjectImpl getOdi11SceneLogView() {
        return (ViewObjectImpl)findViewObject("Odi11SceneLogView");
    }

    /**
     * Container's getter for Odi11gStatusLogLnk.
     * @return Odi11gStatusLogLnk
     */
    public ViewLinkImpl getOdi11gStatusLogLnk() {
        return (ViewLinkImpl)findViewLink("Odi11gStatusLogLnk");
    }

    /**
     * Container's getter for Odi11ScaneVO1.
     * @return Odi11ScaneVO1
     */
    public ViewObjectImpl getOdi11ScaneVO() {
        return (ViewObjectImpl)findViewObject("Odi11ScaneVO");
    }
}
