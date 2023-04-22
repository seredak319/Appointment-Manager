package pl.orange.model;

import lombok.Data;

import java.time.LocalTime;

@Data
public abstract class CalendarData {

    private String start;
    private String end;

    public LocalTime getStartAsTime() {

        if (start == null) {
            throw new NullPointerException("Start time is empty");
        }

        if (start.matches("(\\d{2}):(\\d{2})")) {
            return LocalTime.parse(start);
        } else {
            throw new IllegalArgumentException("Start time [" + start + "] is not in required format [HH:MM]");
        }
    }

    public LocalTime getEndAsTime() {

        if (end == null) {
            throw new NullPointerException("End time is empty");
        }

        if (end.matches("(\\d{2}):(\\d{2})")) {
            return LocalTime.parse(end);
        } else {
            throw new IllegalArgumentException("End time [" + end + "] is not in required format [HH:MM]");
        }
    }
}



