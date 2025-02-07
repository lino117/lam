package com.example.progettolam.struct;

public class Filter {
    private long start;
    private long end;
    private String walking;
    private String driving;
    private String sitting;
    private String unknown;
    public Filter(){
        this.start = 0;
        this.end = 0;
        this.walking = "";
        this.driving = "";
        this.sitting = "";
        this.unknown="";
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getWalking() {
        return walking;
    }

    public void setWalking(String walking) {
        this.walking = walking;
    }

    public String getDriving() {
        return driving;
    }

    public void setDriving(String driving) {
        this.driving = driving;
    }

    public String getSitting() {
        return sitting;
    }

    public void setSitting(String sitting) {
        this.sitting = sitting;
    }

    public String getUnknown() {
        return unknown;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", walking='" + walking + '\'' +
                ", driving='" + driving + '\'' +
                ", sitting='" + sitting + '\'' +
                ", unknown='" + unknown + '\'' +
                '}';
    }

    public void setUnknown(String unknown) {
        this.unknown = unknown;
    }

    public Boolean isNotFiltered(){
        return start!=0 && end!=0 && !walking.isEmpty() && !driving.isEmpty() && !sitting.isEmpty();
    }
}
