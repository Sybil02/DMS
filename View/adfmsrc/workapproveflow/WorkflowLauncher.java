package workapproveflow;

import dms.dynamicShell.TabLauncher;

import javax.faces.event.ActionEvent;

public class WorkflowLauncher extends TabLauncher{
    public WorkflowLauncher() {
        super();
    }

    public void workflowAction(ActionEvent actionEvent) {
        _launchActivity("工作流", "/WEB-INF/workApproveflow/workflow_display_tsk.xml#workflow_display_tsk", false);
    }
}
