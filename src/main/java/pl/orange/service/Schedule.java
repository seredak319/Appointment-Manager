package pl.orange.service;

import pl.orange.model.Calendar;
import pl.orange.model.PlannedMeeting;
import pl.orange.utils.ScheduleUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Schedule {


    // meetingDuration time format: [HH:MM]
    public List<String[]> findMeeting(String calendar1String, String calendar2String, String meetingDuration) {

        if (calendar1String == null || calendar2String == null || meetingDuration == null) {
            throw new NullPointerException("Appointment can not be set up with empty data");
        }

        Calendar calendar1;
        Calendar calendar2;
        try {
            calendar1 = ScheduleUtils.parseJsonToCalendar(calendar1String);
            calendar2 = ScheduleUtils.parseJsonToCalendar(calendar2String);
        } catch (IOException e) {
            throw new RuntimeException("Parsing JSON string failed");
        }

        long meetingMinutes = parseTime(meetingDuration);

        return findGaps(calendar1, calendar2, meetingMinutes);
    }

    public List<String[]> findMeeting(Calendar calendar1, Calendar calendar2, String meetingDuration) {

        if (calendar1 == null || calendar2 == null || meetingDuration == null) {
            throw new NullPointerException("Appointment can not be set up with empty data");
        }

        ScheduleUtils.validCalendarData(calendar1);
        ScheduleUtils.validCalendarData(calendar2);

        long meetingMinutes = parseTime(meetingDuration);

        return findGaps(calendar1, calendar2, meetingMinutes);
    }

    public List<String[]> findMeeting(Calendar calendar1, Calendar calendar2, LocalTime meetingDuration) {

        if (calendar1 == null || calendar2 == null || meetingDuration == null) {
            throw new NullPointerException("Appointment can not be set up with empty data");
        }

        ScheduleUtils.validCalendarData(calendar1);
        ScheduleUtils.validCalendarData(calendar2);

        long meetingMinutes = meetingDuration.getMinute();

        return findGaps(calendar1, calendar2, meetingMinutes);
    }

    private List<String[]> findGaps(Calendar calendar1, Calendar calendar2, long meetingMinutes) {

        // Getting a later start time considering working hours of both
        LocalTime possibleStartTime = calendar1.getWorkingHours().getStartAsTime()
                .isBefore(calendar2.getWorkingHours().getStartAsTime())
                ? calendar2.getWorkingHours().getStartAsTime()
                : calendar1.getWorkingHours().getStartAsTime();

        // Getting an earlier end time considering working hours of both
        LocalTime possibleEndTime = calendar1.getWorkingHours().getEndAsTime()
                .isBefore(calendar2.getWorkingHours().getEndAsTime())
                ? calendar1.getWorkingHours().getEndAsTime()
                : calendar2.getWorkingHours().getEndAsTime();

        if (possibleStartTime.isAfter(possibleEndTime)) {
            return new ArrayList<>();
        }

        List<String[]> gaps = new ArrayList<>();

        ArrayList<PlannedMeeting> meetings = new ArrayList<>();
        meetings.addAll(calendar1.getPlannedMeetings());
        meetings.addAll(calendar2.getPlannedMeetings());

        Comparator<PlannedMeeting> byStartTime = (o1, o2) -> {
            if (o1.getStartAsTime().compareTo(o2.getStartAsTime()) < 0) {
                return -1;
            } else if (o1.getStartAsTime().compareTo(o2.getStartAsTime()) > 0) {
                return 1;
            } else {
                return Integer.compare(o1.getEndAsTime().compareTo(o2.getEndAsTime()), 0);
            }
        };

        meetings.sort(byStartTime);

        // Gap between possible start time and first scheduled meeting
        if (meetings.size() > 0 && Duration.between(possibleStartTime, meetings.get(0).getStartAsTime()).toMinutes() >= meetingMinutes) {
            gaps.add(new String[]{possibleStartTime.toString(), meetings.get(0).getStart()});
        }

        for (int i = 1; i < meetings.size(); i++) {
            PlannedMeeting firstMeeting = meetings.get(i - 1);
            PlannedMeeting secondMeeting = meetings.get(i);

            LocalTime startOfTheMeeting = firstMeeting.getEndAsTime();
            LocalTime endOfTheMeeting = secondMeeting.getStartAsTime();

            if (firstMeeting.getEndAsTime().compareTo(possibleStartTime) < 0) {
                startOfTheMeeting = possibleStartTime;
            }

            if (secondMeeting.getStartAsTime().compareTo(possibleEndTime) > 0) {
                endOfTheMeeting = possibleEndTime;
            }

            Duration duration = Duration.between(startOfTheMeeting, endOfTheMeeting);
            long gapMinutes = duration.toMinutes();

            if (gapMinutes >= meetingMinutes) {
                gaps.add(new String[]{startOfTheMeeting.toString(), endOfTheMeeting.toString()});
            }
        }

        // Checking gaps after last meeting (if there is any) and possible end
        if (meetings.size() > 0 && Duration.between(meetings.get(meetings.size() - 1).getEndAsTime(), possibleEndTime).toMinutes() >= meetingMinutes) {
            gaps.add(new String[]{meetings.get(meetings.size() - 1).getEndAsTime().toString(), possibleEndTime.toString()});
        } else if (meetings.size() == 0 && Duration.between(possibleStartTime, possibleEndTime).toMinutes() >= meetingMinutes) {
            gaps.add(new String[]{possibleStartTime.toString(), possibleEndTime.toString()});
        }

        return gaps;
    }


    private long parseTime(String timeString) {

        long meetingMinutes;
        try {
            LocalTime meetingLocalTime = LocalTime.parse(timeString.substring(1, timeString.length() - 1));
            meetingMinutes = meetingLocalTime.getHour() * 60 + meetingLocalTime.getMinute();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Bad time format, use [HH:MM]");
        }

        if (meetingMinutes == 0) {
            throw new IllegalArgumentException("Meeting has to have non zero time");
        }

        return meetingMinutes;
    }
}
