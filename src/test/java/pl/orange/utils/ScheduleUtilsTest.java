package pl.orange.utils;

import org.junit.Assert;
import org.junit.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import pl.orange.model.Calendar;
import pl.orange.model.PlannedMeeting;
import pl.orange.model.WorkingHours;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleUtilsTest {

    private String getSimpleCalendarJsonString() throws IOException {
        String calendar1Path = "src/test/resources/data/Calendar1.json";
        return Files.readString(Path.of(calendar1Path));
    }

    @Test
    public void shouldCorrectlyParseSimpleJsonToCalendarObject() throws IOException {

        // Before
        String simpleJsonString = getSimpleCalendarJsonString();

        // When
        Calendar calendar = ScheduleUtils.parseJsonToCalendar(simpleJsonString);

        // Then
        Assert.assertEquals("09:00", calendar.getWorkingHours().getStart());
        Assert.assertEquals("19:55", calendar.getWorkingHours().getEnd());
        Assert.assertEquals("09:00", calendar.getPlannedMeetings().get(0).getStart());
        Assert.assertEquals("10:30", calendar.getPlannedMeetings().get(0).getEnd());
        Assert.assertEquals(3, calendar.getPlannedMeetings().size());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowException_Parse_NullString() throws IOException {

        //Then
        ScheduleUtils.parseJsonToCalendar(null);
    }

    @Test(expected = JsonProcessingException.class)
    public void shouldCorrectlyHandle_Parse_NonJsonString() throws IOException {

        // Before
        String nonJsonString = "I am not JSON";

        // Then
        ScheduleUtils.parseJsonToCalendar(nonJsonString);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowException_Valid_NullInput() {

        // Then
        ScheduleUtils.validCalendarData(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowException_Valid_NullWorkingHours() {

        // Before
        Calendar calendar = new Calendar();
        calendar.setWorkingHours(null);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar.setPlannedMeetings(plannedMeetings);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowException_Valid_NullPlannedMeeting() {

        // Before
        Calendar calendar = new Calendar();
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStart(String.valueOf(LocalTime.of(9, 0)));
        workingHours.setEnd(String.valueOf(LocalTime.of(17, 0)));
        calendar.setWorkingHours(workingHours);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowException_Valid_NullPlannedMeetingElement() {

        // Before
        Calendar calendar = new Calendar();
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStart(String.valueOf(LocalTime.of(9, 0)));
        workingHours.setEnd(String.valueOf(LocalTime.of(17, 0)));
        calendar.setWorkingHours(workingHours);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        plannedMeetings.add(null);
        calendar.setPlannedMeetings(plannedMeetings);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_Valid_NullWorkingHour() {

        // Before
        Calendar calendar = new Calendar();
        WorkingHours workingHours = new WorkingHours();

        workingHours.setEnd(String.valueOf(LocalTime.of(17, 0)));
        calendar.setWorkingHours(workingHours);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar.setPlannedMeetings(plannedMeetings);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_Valid_InvalidWorkingHour() {

        // Before
        Calendar calendar = new Calendar();
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStart(String.valueOf(LocalTime.of(15, 0)));
        workingHours.setEnd(String.valueOf(LocalTime.of(15, 0)));
        calendar.setWorkingHours(workingHours);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar.setPlannedMeetings(plannedMeetings);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_Valid_InvalidPlannedMeetingHours() {

        // Before
        Calendar calendar = new Calendar();
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours.setEnd(String.valueOf(LocalTime.of(18, 0)));
        calendar.setWorkingHours(workingHours);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("09:45");
        plannedMeetings.add(plannedMeeting1);
        calendar.setPlannedMeetings(plannedMeetings);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_Valid_PlannedMeetingHoursNotInWorkingHours() {

        // Before
        Calendar calendar = new Calendar();
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours.setEnd(String.valueOf(LocalTime.of(10, 0)));
        calendar.setWorkingHours(workingHours);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar.setPlannedMeetings(plannedMeetings);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

    @Test
    public void shouldCorrectly_Valid_ValidDataNoException() {

        // Before
        Calendar calendar = new Calendar();
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStart(String.valueOf(LocalTime.of(9, 0)));
        workingHours.setEnd(String.valueOf(LocalTime.of(17, 0)));
        calendar.setWorkingHours(workingHours);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar.setPlannedMeetings(plannedMeetings);

        // Then
        ScheduleUtils.validCalendarData(calendar);
    }

}