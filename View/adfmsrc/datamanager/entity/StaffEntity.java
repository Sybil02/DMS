package datamanager.entity;

import java.io.Serializable;

import java.util.List;

public class StaffEntity implements Serializable {

    private static final long serialVersionUID = 3832232725052521684L;

    public StaffEntity() {
        super();
    }
    
    private List<Staff> staffs;
    private List<StaffFp> staffFps;

    public void setStaffs(List<Staff> staffs) {
        this.staffs = staffs;
    }

    public List<Staff> getStaffs() {
        return staffs;
    }

    public void setStaffFps(List<StaffFp> staffFps) {
        this.staffFps = staffFps;
    }

    public List<StaffFp> getStaffFps() {
        return staffFps;
    }
}
