package dms.dynamicShell;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;


public class Tabs implements Serializable {
    private final List<Tab> _tabs = new ArrayList();
    private int _selectedTabIndex = -1;
    private int _nextRenderedLoc = 0;
    private int _numRendered = 0;
    private boolean _renderTabs = true;

    List<Tab> getTabs() {
        return this._tabs;
    }

    int getSelectedTabIndex() {
        return this._selectedTabIndex;
    }

    void setSelectedTabIndex(int index) {
        this._selectedTabIndex = index;
    }

    void setNextRenderedLoc(int nextRenderedLoc) {
        this._nextRenderedLoc = nextRenderedLoc;
    }

    int getNextRenderedLoc() {
        return this._nextRenderedLoc;
    }

    void setNumRendered(int numRendered) {
        this._numRendered = numRendered;
    }

    int getNumRendered() {
        return this._numRendered;
    }

    void setTabsRendered(boolean rendered) {
        this._renderTabs = rendered;
    }

    boolean isTabsRendered() {
        return this._renderTabs;
    }

    Tabs() {
        for (int i = 0; i < 15; i++) {
            this._tabs.add(new Tab(i, TabContext.__BLANK));
        }
    }
}
