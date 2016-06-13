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

    public void launcherHtysBH(ActionEvent actionEvent) {
        _launchActivity("合同预算-北和",
                        "/WEB-INF/dgpt/dms_htys_bh_tsk.xml#dms_htys_bh_tsk",
                        false);
    }
    public void launcherProjectZx(ActionEvent actionEvent) {
        _launchActivity("在执行项目",
                        "/WEB-INF/dgpt/dms_projectzx_tsk.xml#dms_projectzx_tsk",
                        false);
    }
    
    public void launcherProjectZxo(ActionEvent actionEvent) {
        _launchActivity("在执行项目、合同预算表-料、工、费",
                        "/WEB-INF/dgpt/dms.zzxout_tsk.xml#dms.zzxout_tsk",
                        false);
    }

    public void launcherBasepCost(ActionEvent actionEvent) {
        _launchActivity("基准计划成本",
                        "/WEB-INF/dgpt/dms_jzjhcb_tsk.xml#dms_jzjhcb_tsk",
                        false);
    }
    public void launcherHtChange(ActionEvent actionEvent) {
        _launchActivity("合同变更后的基准计划成本",
                        "/WEB-INF/dgpt/dms_hychange_tsk.xml#dms_hychange_tsk",
                        false);
    }
    public void launcherRollChange(ActionEvent actionEvent) {
        _launchActivity("滚动计划成本",
                        "/WEB-INF/dgpt/dms_rpcost_tsk.xml#dms_rpcost_tsk",
                        false);
    }
}
