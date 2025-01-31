package com.example.progettolam.struct;

public class Record {
    private String nameActivity;
    private Integer duration;
    private Integer step;
    private Long startDay;

    private Long startTime;
    private Long endTime;
    private Long endDay;



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

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getStartDay() {
        return startDay;
    }

    public void setStartDay(Long startDay) {
        this.startDay = startDay;
    }

    public Long getEndDay() {
        return endDay;
    }

    public void setEndDay(Long endDay) {
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
