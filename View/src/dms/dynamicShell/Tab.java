package dms.dynamicShell;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import oracle.adf.controller.TaskFlowId;

import oracle.adf.model.BindingContext;

import oracle.adf.model.binding.DCBindingContainer;

import oracle.binding.BindingContainer;

public class Tab implements Serializable {
    private final int _index;
    private boolean _isActive = false;
    private boolean _isDirty = false;
    private String _localizedName;
    private TaskFlowId _taskflowId;
    private Map<String, Object> _parameters;

    public void setTitle(String title) {
        this._localizedName = title;
    }

    public String getTitle() {
        return this._localizedName;
    }

    public int getIndex() {
        return this._index;
    }

    public DCBindingContainer getBinding() {
        return (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry().get("r" +this._index);
    }

    public void setActive(boolean rendered) {
        this._isActive = rendered;

        if (!this._isActive)
            setDirty(false);
    }

    public boolean isActive() {
        return this._isActive;
    }

    public void setDirty(boolean isDirty) {
        this._isDirty = isDirty;
    }

    public boolean isDirty() {
        return this._isDirty;
    }

    public void setTaskflowId(TaskFlowId id) {
        this._taskflowId = id;
    }

    public TaskFlowId getTaskflowId() {
        return this._taskflowId;
    }

    public void setParameters(Map<String, Object> parameters) {
        this._parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return this._parameters;
    }

    public List<Tab> getChildren() {
        return Collections.emptyList();
    }

    Tab(int index, TaskFlowId id) {
        this._index = index;
        this._taskflowId = id;
    }
}
