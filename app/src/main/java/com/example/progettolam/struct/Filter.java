package com.example.progettolam.struct;

public class Filter {
    private String start;
    private String end;
    private String walking;
    private String driving;
    private String sitting;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
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

    @Override
    public String toString() {
        return "Filter{" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", walking=" + walking +
                ", driving=" + driving +
                ", sitting=" + sitting +
                '}';
    }
    public Boolean isNotFiltered(){
        return start.isEmpty() && end.isEmpty() && !walking.isEmpty() && !driving.isEmpty() && !sitting.isEmpty();
    }
}
