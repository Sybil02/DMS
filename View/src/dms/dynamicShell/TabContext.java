package dms.dynamicShell;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import oracle.adf.model.BindingContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.servlet.http.HttpSession;

import oracle.adf.controller.ControllerContext;
import oracle.adf.controller.TaskFlowId;
import oracle.adf.controller.ViewPortContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.layout.RichPanelGroupLayout;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;
import oracle.adf.view.rich.component.rich.nav.RichCommandNavigationItem;
import oracle.adf.view.rich.component.rich.nav.RichNavigationPane;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.context.DirtyPageHandler;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.DialogEvent.Outcome;

import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.model.ChildPropertyMenuModel;
import org.apache.myfaces.trinidad.model.MenuModel;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

public final class TabContext implements Serializable {
    private final Tabs _tabTracker;
    private static final String _USE_PAGEFLOW_TAB_TRACKING =
        "USE_PAGEFLOW_TAB_TRACKING";
    private static final String _TRUE = "true";
    private transient RichPanelStretchLayout _contentArea;
    private transient RichPanelStretchLayout _toolbarArea;
    private transient RichPanelGroupLayout _innerToolbar;
    private transient RichNavigationPane _tabsNavigationPane;
    private transient RichPopup _tooManyTabsPopup;
    private transient RichPopup _tabDirtyPopup;
    private static final String _KEY = "dmsTabContext";
    static final TaskFlowId __BLANK =
        TaskFlowId.parse("/WEB-INF/blank.xml#blank");
    static final int __MAX_TASKFLOWS = 15;
    private static final long serialVersionUID = 11112L;

    public static TabContext getCurrentInstance() {
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();

        TabContext tabContext =
            (TabContext)adfFacesContext.getViewScope().get("dmsTabContext");

        if (tabContext == null) {
            tabContext =
                    (TabContext)adfFacesContext.getPageFlowScope().get("dmsTabContext");
        }
        return tabContext;
    }
    

    
    public void setMainContent(String taskflowId) throws TabContext.TabContentAreaDirtyException {
        setMainContent(taskflowId, null);
    }

    public void setMainContent(String taskflowId,
                               Map<String, Object> parameters) throws TabContext.TabContentAreaDirtyException {
        if (this._tabTracker.getNumRendered() > 1) {
            throw new TabContentAreaNotReadyException();
        }
        int index = getSelectedTabIndex();
        if (index == -1) {
            try {
                addTab("", taskflowId, parameters);
            } catch (TabOverflowException toe) {
            }
        } else {
            Tab tab = (Tab)getTabs().get(getSelectedTabIndex());

            if (tab.isDirty()) {
                throw new TabContentAreaDirtyException();
            }
            tab.setTitle("");
            tab.setTaskflowId(TaskFlowId.parse(taskflowId));
        }

        setTabsRendered(false);
        _refreshTabContent();
    }

    public void addOrSelectTab(String localizedName,
                               String taskflowId) throws TabContext.TabOverflowException {
        addOrSelectTab(localizedName, taskflowId, null);
    }

    public void addOrSelectTab(String localizedName, String taskflowId,
                               Map<String, Object> parameters) throws TabContext.TabOverflowException {
        int index = getFirstTabIndex(taskflowId);
   
        if (index != -1) { //如果已经页面是那个页面，而且是处于活跃状态
            setSelectedTabIndex(index);
        } else { //如果没有这个页面，则新增页面
 
            addTab(localizedName, taskflowId, parameters);
        }
    }
    
    private Tab _saveTab = new Tab();  //用来缓存页面，方便没有确定呢关闭页面前不一定会展示这个页面
    
    public void saveTab(String localizedName, String taskflowId,
                        Map<String, Object> parameters) {
          
        _saveTab.setTaskflowId(TaskFlowId.parse(taskflowId));
        _saveTab.setTitle(localizedName);
        _saveTab.setParameters(parameters);
        _saveTab.setActive(true);
    } 
    
    public void  addSaveTab( ) throws TabContext.TabOverflowException {
        if (this._tabTracker.getNumRendered() == 15) {
            throw new TabOverflowException();
        }
        
        int index = _findNextAvailable();
        Tab tab = getTabs().get(index);
        tab.setTitle(_saveTab.getTitle());
        tab.setActive(true);
        tab.setTaskflowId(_saveTab.getTaskflowId());
        tab.setParameters(_saveTab.getParameters());
        
        this._tabTracker.setNumRendered(this._tabTracker.getNumRendered() + 1);
        this._tabTracker.setNextRenderedLoc(this._tabTracker.getNextRenderedLoc() +
                                            1);
        setSelectedTabIndex(index);
        _saveTab.setActive(false);
    }
    
    
    public void addTab(String localizedName,
                       String taskflowId) throws TabContext.TabOverflowException {
        addTab(localizedName, taskflowId, null);
    }

    public void addTab(String localizedName, String taskflowId,
                       Map<String, Object> parameters) throws TabContext.TabOverflowException {
        if (this._tabTracker.getNumRendered() == 15) {
            throw new TabOverflowException();
        }

        int index = _findNextAvailable();
        
        Tab tab = (Tab)getTabs().get(index);
        tab.setTitle(localizedName);
        tab.setActive(true);
        tab.setTaskflowId(TaskFlowId.parse(taskflowId));
        tab.setParameters(parameters);

        this._tabTracker.setNumRendered(this._tabTracker.getNumRendered() + 1);
        this._tabTracker.setNextRenderedLoc(this._tabTracker.getNextRenderedLoc() +
                                            1);
        setSelectedTabIndex(index);
    }

    public void markCurrentTabDirty(boolean isDirty) {
        markTabDirty(getSelectedTabIndex(), isDirty);
    }

    public void markTabDirty(int index, boolean isDirty) {
        Tab tab = (Tab)getTabs().get(index);
        tab.setDirty(isDirty);
        _refreshTabContent();
    }

    public void removeCurrentTab() {
        removeTab(getSelectedTabIndex()); 
    }

    public void removeTab(int index) {
        _removeTab(index, false);
    }

    public boolean isTagSetDirty() {
        for (Tab t : getTabs()) {
            if ((t.isActive()) && (t.isDirty())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCurrentTabDirty() {
        int index = getSelectedTabIndex();
        if (index == -1) {
            return false;
        }
        return ((Tab)getTabs().get(index)).isDirty();
    }

    public int getSelectedTabIndex() {
        return this._tabTracker.getSelectedTabIndex();
    }

    public void setTabsRendered(boolean render) {
        this._tabTracker.setTabsRendered(render);
    }

    public boolean isTabsRendered() {
        return this._tabTracker.isTabsRendered();
    }

    public int getFirstTabIndex(String taskflowId) {
        List tabs = this._tabTracker.getTabs();

        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = (Tab)tabs.get(i);

            if ((tab != null) && (tab.isActive())) {
                if (tab.getTaskflowId().getFullyQualifiedName().equals(taskflowId))
                    return i;
            }
        }
        return -1;
    }

    public void setSelectedTabIndex(int index) {
        this._tabTracker.setSelectedTabIndex(index);
        _refreshTabContent();
    }

    public List<Tab> getTabs() {
        return this._tabTracker.getTabs();
    }

    public MenuModel getTabMenuModel() {
        ChildPropertyMenuModel menuModel =
            new ChildPropertyMenuModel(getTabs(), "children",
                                       Collections.singletonList(Integer.valueOf(getSelectedTabIndex())));

        return menuModel;
    }

    public void setTabsNavigationPane(RichNavigationPane tabsNavigationPane) {
        this._tabsNavigationPane = tabsNavigationPane;
    }

    public RichNavigationPane getTabsNavigationPane() {
        return this._tabsNavigationPane;
    }

    public void setToolbarArea(RichPanelStretchLayout toolbarArea) {
        this._toolbarArea = toolbarArea;
    }

    public RichPanelStretchLayout getToolbarArea() {
        return this._toolbarArea;
    }

    public void setTooManyTabsPopup(RichPopup tooManyTabsPopup) {
        this._tooManyTabsPopup = tooManyTabsPopup;
    }

    public RichPopup getTooManyTabsPopup() {
        return this._tooManyTabsPopup;
    }

    public void setTabDirtyPopup(RichPopup tabDirtyPopup) {
        this._tabDirtyPopup = tabDirtyPopup;
    }

    public RichPopup getTabDirtyPopup() {
        return this._tabDirtyPopup;
    }

    public void setInnerToolbarArea(RichPanelGroupLayout innerToolbar) {
        this._innerToolbar = innerToolbar;
    }

    public RichPanelGroupLayout getInnerToolbarArea() {
        return this._innerToolbar;
    }

    public void setContentArea(RichPanelStretchLayout contentArea) {
        this._contentArea = contentArea;
    }

    public RichPanelStretchLayout getContentArea() {
        return this._contentArea;
    }

    public void tabActivatedEvent(ActionEvent action) {
        RichCommandNavigationItem tab =
            (RichCommandNavigationItem)action.getComponent();

        Object tabIndex = tab.getAttributes().get("tabIndex");
        setSelectedTabIndex(((Integer)tabIndex).intValue());
    }

    public void tabRemovedEvent(ActionEvent action) {
        removeCurrentTab();
    }

    public void handleDirtyTabDialog(DialogEvent ev) {
   
        if (ev.getOutcome().equals(DialogEvent.Outcome.yes)) {
            _removeTab(getSelectedTabIndex(), true);
            
            try{
                addSaveTab();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }else
            _saveTab.setActive(false);
    }

    public TabContext() {
       
        Tabs tabTracker = null;
        String viewId =
            ControllerContext.getInstance().getCurrentRootViewPort().getViewId();
        String currentViewKey =
            String.format("__tc_%s", new Object[] { viewId });

        if (_isPageFlowTabTrackingStrategy()) {
            RequestContext reqContext = RequestContext.getCurrentInstance();
            tabTracker =
                    (Tabs)reqContext.getPageFlowScope().get(currentViewKey);

            if (tabTracker == null) {
                tabTracker = new Tabs();
                reqContext.getPageFlowScope().put(currentViewKey, tabTracker);
            }
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session =
                (HttpSession)context.getExternalContext().getSession(true);
            tabTracker = (Tabs)session.getAttribute(currentViewKey);

            if (tabTracker == null) {
                tabTracker = new Tabs();
                session.setAttribute(currentViewKey, tabTracker);
            }
        }
        this._tabTracker = tabTracker;
    }

    private void _removeTab(int index, boolean force) {
        List tabs = getTabs();
        
        Tab tab = (Tab)tabs.get(index);

        if ((tab.isDirty()) && (!force)) {
            _showDialog(getTabDirtyPopup());
            return;
        }
        //把这个tab里面的东西清空
        tab.setTaskflowId(__BLANK);
        tab.setParameters(null);
        tab.setTitle("");
        tab.setActive(false);
        this._tabTracker.setNumRendered(this._tabTracker.getNumRendered() - 1);
        //如果删除的是当前行，那么就要选择另一个页面作为显示页面了，如果删除的话那么页面就不会跳转到其他页面当前页面就空了
        if (this._tabTracker.getSelectedTabIndex() == index) {
            this._tabTracker.setSelectedTabIndex(-1);
            if (this._tabTracker.getNumRendered() > 0) {
                int start = index == 14 ? 0 : index + 1;
                do {
                    if (start == 15)
                        start = 0;
                    Tab itorTab = (Tab)tabs.get(start);
                    if (itorTab.isActive()) {
                        this._tabTracker.setSelectedTabIndex(start);
                        break;
                    }
                    start++;
                } while (start != index);
            } else {
                this._tabTracker.setSelectedTabIndex(-1);
            }
        }
        _refreshTabContent();
    }

    private void _refreshTabContent() {
        AdfFacesContext.getCurrentInstance().addPartialTarget(getTabsNavigationPane());
        AdfFacesContext.getCurrentInstance().addPartialTarget(getContentArea());
        AdfFacesContext.getCurrentInstance().addPartialTarget(getToolbarArea());
        AdfFacesContext.getCurrentInstance().addPartialTarget(getInnerToolbarArea());
    }

    private int _findNextAvailable() {
        List tabs = getTabs();

        if (this._tabTracker.getNextRenderedLoc() == 15) {
            for (int i = 0; i < 15; i++) {
                Tab tab = (Tab)tabs.get(i);

                if (!tab.isActive()) {
                    int j = i + 1;
                    Tab toSwap = null;
                    while (j < 15) {
                        Tab testTab = (Tab)tabs.get(j++);
                        if (testTab.isActive()) {
                            toSwap = testTab;
                            break;
                        }

                    }

                    if (toSwap == null) {
                        break;
                    }
                    tab.setActive(true);
                    toSwap.setActive(false);
                    tab.setTitle(toSwap.getTitle());
                    toSwap.setTitle("");
                    tab.setTaskflowId(toSwap.getTaskflowId());
                    tab.setParameters(toSwap.getParameters());
                    toSwap.setTaskflowId(__BLANK);
                    toSwap.setParameters(null);
                }
            }

            this._tabTracker.setNextRenderedLoc(this._tabTracker.getNumRendered());
        }

        return this._tabTracker.getNextRenderedLoc();
    }

    private void _showDialog(RichPopup popup) {
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder toSend = new StringBuilder();
        toSend.append("var popup = AdfPage.PAGE.findComponent('").append(popup.getClientId(context)).append("'); ").append("if (!popup.isPopupVisible()) { ").append("var hints = {}; ").append("popup.show(hints);}");

        ExtendedRenderKitService erks =
            (ExtendedRenderKitService)Service.getService(context.getRenderKit(),
                                                         ExtendedRenderKitService.class);

        erks.addScript(context, toSend.toString());
    }

    private boolean _isPageFlowTabTrackingStrategy() {
        FacesContext context = FacesContext.getCurrentInstance();
        String tabTrackingParam =
            context.getExternalContext().getInitParameter("USE_PAGEFLOW_TAB_TRACKING");
        if ((tabTrackingParam != null) &&
            ("true".equalsIgnoreCase(tabTrackingParam))) {
            return true;
        }

        return false;
    }

    public static final class TabContentAreaDirtyException extends Exception {
    }

    public static final class TabContentAreaNotReadyException extends RuntimeException {
    }

    public final class TabOverflowException extends Exception {
        public TabOverflowException() {
        }

        public void handleDefault() {
            TabContext.this._showDialog(TabContext.this.getTooManyTabsPopup());
        }

    }
}
