package com.example.progettolam.struct;

import org.w3c.dom.Text;

public class Record {
    private String nameActivity;
    private Integer duration;
    private Integer step;
    private Long Start_day;
    private Long Start_time;
    private Long End_day;
    private Long End_time;
//    private String Start;
//    private String End;



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

    public Long getStart_day() {
        return Start_day;
    }

    public void setStart_day(Long start_day) {
        Start_day = start_day;
    }

    public Long getEnd_time() {
        return End_time;
    }

    public void setEnd_time(Long end_time) {
        End_time = end_time;
    }

    public Long getEnd_day() {
        return End_day;
    }

    public void setEnd_day(Long end_day) {
        End_day = end_day;
    }

    public Long getStart_time() {
        return Start_time;
    }

    public void setStart_time(Long start_time) {
        Start_time = start_time;
    }

    @Override
    public String toString() {
        return "Record{" +
                "nameActivity='" + nameActivity + '\'' +
                ", duration=" + duration +
                ", step=" + step +
                ", Start_day=" + Start_day +
                ", Start_time=" + Start_time +
                ", End_day=" + End_day +
                ", End_time=" + End_time +
                '}';
    }
}

