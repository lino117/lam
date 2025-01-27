package com.example.progettolam.struct;

public class Record {
    private String nameActivity;
    private Integer duration;
    private Integer step;
    private String startDay;

    private String startTime;
    private String endTime;
    private String endDay;



    public String getNameActivity() {
        return nameActivity;
    }

    public void setNameActivity(String nameActivity) {
        this.nameActivity = nameActivity;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }

    @Override
    public String toString() {
        return "Record{" +
                "nameActivity='" + nameActivity + '\'' +
                ", duration=" + duration +
                ", step=" + step +
                ", startDay='" + startDay + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", endDay='" + endDay + '\'' +
                '}';
    }
}
