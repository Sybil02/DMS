package dgpt;

import dms.dynamicShell.TabLauncher;

import javax.faces.event.ActionEvent;

public class Launcher extends TabLauncher{
    public Launcher() {
        super();
    }

    public void launcherBh(ActionEvent actionEvent) {
        _launchActivity("预计新签项目预算编制表-北和",
                        "/WEB-INF/dgpt/dms_xqys_bh_tsk.xml#dms_xqys_bh_tsk",
                        false);
    }

    public void launcherHh(ActionEvent actionEvent) {
        _launchActivity("预计新签项目预算编制表-杭和",
                        "/WEB-INF/dgpt/dms_xqys_hh.xml#dms_xqys_hh",
                        false);
    }
}
