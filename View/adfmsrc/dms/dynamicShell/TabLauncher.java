package dms.dynamicShell;

import java.util.HashMap;
import java.util.Map;

import javax.faces.event.ActionEvent;

import oracle.adf.share.logging.ADFLogger;

public class TabLauncher {
    private static ADFLogger _logger = ADFLogger.createADFLogger(TabLauncher.class);

        public void launchNavigationActivity(String name, String url) {
            try{
                _launchActivity(name, url, false);
            }catch(Exception e){
               _logger.warning("failed to open tab", e);
            }
        }

        public void launchNavigationActivity(String name, String url, String strParameter) {
            _launchActivity(name, url, false, parseParameter(strParameter));
        }

        public void closeCurrentActivity(ActionEvent actionEvent) {
            TabContext tabContext = TabContext.getCurrentInstance();
            int tabIndex = tabContext.getSelectedTabIndex();
            if (tabIndex != -1) {
                tabContext.removeTab(tabIndex);
            }
        }

        public void currentTabDirty(ActionEvent e) {
            /**
            * When called, marks the current tab "dirty". Only at the View level
            * is it possible to mark a tab dirty since the model level does not
            * track to which tab data belongs.
            */
            TabContext tabContext = TabContext.getCurrentInstance();
            tabContext.markCurrentTabDirty(true);
        }

        public void currentTabClean(ActionEvent e) {
            TabContext tabContext = TabContext.getCurrentInstance();
            tabContext.markCurrentTabDirty(false);
        }

//        /**
//         *check if there is dirty tab in current page
//         * @return
//         */
//        public boolean isCurrentPageTagSetDirty() {
//            TabContext tabContext = TabContext.getCurrentInstance();
//            return tabContext.isCurrentPageTagSetDirty();
//        }


        //    public void launchSecondActivity(ActionEvent actionEvent) {
        //        _launchActivity("Next Activity", "/WEB-INF/flows/second.xml#second", false);
        //    }

        //    public void launchFirstReplaceNPlace(ActionEvent actionEvent) {
        //        TabContext tabContext = TabContext.getCurrentInstance();
        //        try {
        //            tabContext.setMainContent("/WEB-INF/flows/first.xml#first");
        //        } catch (TabContext.TabContentAreaDirtyException toe) {
        //            // TODO: warn user TabContext api needed for this use case.
        //        }
        //    }

        protected void _launchActivity(String title, String taskflowId, boolean newTab) {
            this._launchActivity(title, taskflowId, newTab, null);
        }

        protected void _launchActivity(String title, String taskflowId, boolean newTab, Map<String, Object> parametersMap) {
            try {
                if (newTab) {
                    TabContext.getCurrentInstance().addTab(title, taskflowId, parametersMap);
                } else {
                    TabContext.getCurrentInstance().addOrSelectTab(title, taskflowId, parametersMap);
                }
            } catch (TabContext.TabOverflowException toe) {
                toe.handleDefault();
            }
        }

        //String to HashMap, example: parameter1:001,parameter2: 002, parameter3:003
        private Map<String, Object> parseParameter(String stringParameter) {
            Map<String, Object> hm = new HashMap<String, Object>();
            if (stringParameter != null) {
                for (String keyValue : stringParameter.split(",")) {
                    String[] pairs = keyValue.split(":");
                    hm.put(pairs[0], pairs[1]);
                }
            }
            return hm;
        }
}
