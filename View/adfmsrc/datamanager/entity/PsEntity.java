package datamanager.entity;

import java.io.Serializable;

import java.util.List;

public class PsEntity implements Serializable{
    private static final long serialVersionUID = 0L;

    public PsEntity() {
        super();
    }
    
    private List<PsJob> psJob;
    private List<PsMaster> psMaster;
    private List<PsMilepost> psMilepost;
    private List<PsNetwork> psNetwork;
    private List<PsOrg> psOrg;
    private List<PsWbs> psWbs;
    private List<PsWbsMaster> psWbsMaster;


    public void setPsJob(List<PsJob> psJob) {
        this.psJob = psJob;
    }

    public List<PsJob> getPsJob() {
        return psJob;
    }

    public void setPsMaster(List<PsMaster> psMaster) {
        this.psMaster = psMaster;
    }

    public List<PsMaster> getPsMaster() {
        return psMaster;
    }

    public void setPsMilepost(List<PsMilepost> psMilepost) {
        this.psMilepost = psMilepost;
    }

    public List<PsMilepost> getPsMilepost() {
        return psMilepost;
    }

    public void setPsNetwork(List<PsNetwork> psNetwork) {
        this.psNetwork = psNetwork;
    }

    public List<PsNetwork> getPsNetwork() {
        return psNetwork;
    }

    public void setPsOrg(List<PsOrg> psOrg) {
        this.psOrg = psOrg;
    }

    public List<PsOrg> getPsOrg() {
        return psOrg;
    }

    public void setPsWbs(List<PsWbs> psWbs) {
        this.psWbs = psWbs;
    }

    public List<PsWbs> getPsWbs() {
        return psWbs;
    }

    public void setPsWbsMaster(List<PsWbsMaster> psWbsMaster) {
        this.psWbsMaster = psWbsMaster;
    }

    public List<PsWbsMaster> getPsWbsMaster() {
        return psWbsMaster;
    }
}
