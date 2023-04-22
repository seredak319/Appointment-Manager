package pl.orange.service;

import org.junit.Assert;
import org.junit.Test;
import pl.orange.model.Calendar;
import pl.orange.model.PlannedMeeting;
import pl.orange.model.WorkingHours;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleTest {
    private final Schedule schedule = new Schedule();

    @Test(expected = NullPointerException.class)
    public void shouldThrowException_NullValue() {

        // Then
        schedule.findMeeting(null, "test", "test");
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowException_InvalidJSONString() {

        // Then
        schedule.findMeeting("[{\"chess_players\": \"Ding Liren\"}]", "", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WrongDataGiven_WorkingHours() {

        // Before
        String calendarBadDataWorkingHours1 = "{\"working_hours\":{\"start\":\"13:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"09:00\",\"end\":\"12:00\"}]}";
        String calendarBadDataWorkingHours2 = "{\"working_hours\":{\"start\":\"13:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"09:00\",\"end\":\"12:00\"}]}";

        // Then
        schedule.findMeeting(calendarBadDataWorkingHours1, calendarBadDataWorkingHours2, "[00:15]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WrongDataGiven_PlannedMeetingHours() {

        // Before
        String calendarBadDataMeetingHours1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"09:00\",\"end\":\"12:00\"}]}";
        String calendarBadDataMeetingHours2 = "{\"working_hours\":{\"start\":\"11:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"11:00\",\"end\":\"10:00\"}]}";

        // Then
        schedule.findMeeting(calendarBadDataMeetingHours1, calendarBadDataMeetingHours2, "[00:15]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WrongDataGiven_MeetingDuration() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"12:00\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"11:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"11:15\",\"end\":\"11:30\"}]}";

        // Then     // MeetingDuration string format: [HH:MM]
        schedule.findMeeting(calendarValidData1, calendarValidData2, "I am not time format string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WrongDataGiven_ZeroMeetingDuration() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"12:00\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"11:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"11:15\",\"end\":\"11:30\"}]}";

        // Then
        schedule.findMeeting(calendarValidData1, calendarValidData2, "[00:00]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WrongDataGiven_IncorrectMeetingDuration() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"10:30\",\"end\":\"11:10\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"11:00\",\"end\":\"12:00\"},\"planned_meeting\":[{\"start\":\"11:15\",\"end\":\"11:30\"}]}";

        // Then
        schedule.findMeeting(calendarValidData1, calendarValidData2, "[00:61]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_MeetingBeforeWorkingHours() {

        // Before
        String calendarMeetingBeforeWorkingHours = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:50\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"12:00\"}]}";
        String calendarValidData = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"12:50\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"12:00\"}]}";

        // Then
        schedule.findMeeting(calendarMeetingBeforeWorkingHours, calendarValidData, "[00:10]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_MeetingAfterWorkingHours() {

        // Before
        String calendarMeetingAfterWorkingHours = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:50\"},\"planned_meeting\":[{\"start\":\"10:30\",\"end\":\"18:51\"}]}";
        String calendarValidData = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"12:50\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"12:00\"}]}";

        // Then
        schedule.findMeeting(calendarMeetingAfterWorkingHours, calendarValidData, "[00:10]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_NullWorkingHours() {

        // Before
        String calendarEmptyWorkingHours = "{\"working_hours\":{\"start\":\"08:30\"},\"planned_meeting\":[{\"start\":\"10:30\",\"end\":\"18:51\"}]}";
        String calendarValidData = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"12:50\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"12:00\"}]}";

        // Then
        schedule.findMeeting(calendarEmptyWorkingHours, calendarValidData, "[00:10]");
    }


    @Test
    public void shouldCorrectlyFind_EmptyPlannedMeeting() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:50\"},\"planned_meeting\":[]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"12:50\"},\"planned_meeting\":[]}";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, "[00:30]");

        // Then
        Assert.assertEquals(1, response.size());
        Assert.assertArrayEquals(new String[]{"10:00", "12:50"}, response.get(0));
    }

    @Test
    public void shouldCorrectlyFind_OneAndEmptyPlannedMeeting() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:50\"},\"planned_meeting\":[]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"12:50\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"12:00\"}]}";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, "[00:30]");

        // Then
        Assert.assertEquals(1, response.size());
        Assert.assertArrayEquals(new String[]{"12:00", "12:50"}, response.get(0));
    }

    @Test
    public void shouldCorrectlyFind_OneAndOnePlannedMeeting() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:50\"},\"planned_meeting\":[{\"start\":\"10:30\",\"end\":\"12:00\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"12:50\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"09:00\"}]}";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, "[00:30]");

        // Then
        Assert.assertEquals(2, response.size());
        Assert.assertArrayEquals(new String[]{"10:00", "10:30"}, response.get(0));
        Assert.assertArrayEquals(new String[]{"12:00", "12:50"}, response.get(1));
    }

    @Test
    public void shouldCorrectlyFind_NoTimeForMeeting() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:20\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"18:20\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"16:45\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"09:00\"}]}";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, "[00:01]");

        // Then
        Assert.assertEquals(0, response.size());
    }

    @Test
    public void shouldCorrectlyFind_MoreThanHourMeeting() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:20\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"10:45\"},{\"start\":\"13:00\",\"end\":\"14:45\"},{\"start\":\"15:00\",\"end\":\"15:15\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"19:45\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"09:00\"},{\"start\":\"13:10\",\"end\":\"14:50\"}]}";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, "[02:10]");

        // Then
        Assert.assertEquals(2, response.size());
        Assert.assertArrayEquals(new String[]{"10:45", "13:00"}, response.get(0));
        Assert.assertArrayEquals(new String[]{"15:15", "18:20"}, response.get(1));
    }

    @Test
    public void shouldCorrectlyFind_TooLongMeeting() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"10:00\",\"end\":\"18:20\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"10:45\"},{\"start\":\"13:00\",\"end\":\"14:45\"},{\"start\":\"15:00\",\"end\":\"15:15\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"08:30\",\"end\":\"19:45\"},\"planned_meeting\":[{\"start\":\"08:30\",\"end\":\"09:00\"},{\"start\":\"13:10\",\"end\":\"14:50\"}]}";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, "[20:10]");

        // Then
        Assert.assertEquals(0, response.size());
    }

    @Test
    public void shouldCorrectlyFind_GivenExample() throws IOException {

        // Before
        String calendar1 = Files.readString(Path.of("src/test/resources/data/Calendar1.json"));
        String calendar2 = Files.readString(Path.of("src/test/resources/data/Calendar2.json"));
        String meetingDuration = "[00:30]";

        // When
        List<String[]> response = schedule.findMeeting(calendar1, calendar2, meetingDuration);

        // Then
        Assert.assertEquals(3, response.size());
        Assert.assertArrayEquals(new String[]{"11:30", "12:00"}, response.get(0));
        Assert.assertArrayEquals(new String[]{"15:00", "16:00"}, response.get(1));
        Assert.assertArrayEquals(new String[]{"18:00", "18:30"}, response.get(2));
    }

    @Test
    public void shouldCorrectlyFind_BiggerExamples_InRandomOrder() throws IOException {

        // Before
        String calendar1 = Files.readString(Path.of("src/test/resources/data/CalendarBig1.json"));
        String calendar2 = Files.readString(Path.of("src/test/resources/data/CalendarBig2.json"));
        String meetingDuration = "[00:05]";

        // When
        List<String[]> response = schedule.findMeeting(calendar1, calendar2, meetingDuration);

        // Then
        Assert.assertEquals(5, response.size());
        Assert.assertArrayEquals(new String[]{"10:00", "10:30"}, response.get(0));
        Assert.assertArrayEquals(new String[]{"11:50", "12:00"}, response.get(1));
        Assert.assertArrayEquals(new String[]{"13:25", "13:30"}, response.get(2));
        Assert.assertArrayEquals(new String[]{"14:45", "15:00"}, response.get(3));
        Assert.assertArrayEquals(new String[]{"16:40", "17:00"}, response.get(4));
    }

    @Test
    public void shouldCorrectlyFind_WorkingHoursDontMatch() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"07:00\",\"end\":\"14:20\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"10:45\"},{\"start\":\"13:00\",\"end\":\"14:20\"},{\"start\":\"07:00\",\"end\":\"07:15\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"15:30\",\"end\":\"19:45\"},\"planned_meeting\":[{\"start\":\"15:30\",\"end\":\"16:00\"},{\"start\":\"16:25\",\"end\":\"16:35\"}]}";
        String meetingDuration = "[00:05]";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, meetingDuration);

        // Then
        Assert.assertEquals(0, response.size());
    }

    @Test
    public void shouldCorrectlyFind_TheSameCalendar() {

        // Before
        String calendarValidData1 = "{\"working_hours\":{\"start\":\"07:00\",\"end\":\"14:20\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"10:45\"}]}";
        String calendarValidData2 = "{\"working_hours\":{\"start\":\"07:00\",\"end\":\"14:20\"},\"planned_meeting\":[{\"start\":\"10:00\",\"end\":\"11:45\"}]}";
        String meetingDuration = "[00:05]";

        // When
        List<String[]> response = schedule.findMeeting(calendarValidData1, calendarValidData2, meetingDuration);

        // Then
        Assert.assertEquals(2, response.size());
    }


    // Test for other constructor (Calendar, Calendar, String)
    @Test
    public void shouldCorrectlyFind_OOS() {

        // Before
        Calendar calendar1 = new Calendar();
        WorkingHours workingHours1 = new WorkingHours();
        workingHours1.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours1.setEnd(String.valueOf(LocalTime.of(19, 0)));
        calendar1.setWorkingHours(workingHours1);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar1.setPlannedMeetings(plannedMeetings);

        Calendar calendar2 = new Calendar();
        WorkingHours workingHours2 = new WorkingHours();
        workingHours2.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours2.setEnd(String.valueOf(LocalTime.of(19, 0)));
        calendar2.setWorkingHours(workingHours2);
        List<PlannedMeeting> plannedMeetings2 = new ArrayList<>();
        PlannedMeeting plannedMeeting21 = new PlannedMeeting();
        plannedMeeting21.setStart("10:15");
        plannedMeeting21.setEnd("10:45");
        plannedMeetings2.add(plannedMeeting21);
        calendar2.setPlannedMeetings(plannedMeetings2);

        String meetingDuration = "[02:15]";

        // When
        List<String[]> response = schedule.findMeeting(calendar1, calendar2, meetingDuration);

        // Then
        Assert.assertEquals(2, response.size());
        Assert.assertArrayEquals(new String[]{"08:00", "10:15"}, response.get(0));
        Assert.assertArrayEquals(new String[]{"10:45", "19:00"}, response.get(1));

    }

    // Test for other constructor (Calendar, Calendar, LocalTime)
    @Test
    public void shouldCorrectlyFind_OOT() {

        // Before
        Calendar calendar1 = new Calendar();
        WorkingHours workingHours1 = new WorkingHours();
        workingHours1.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours1.setEnd(String.valueOf(LocalTime.of(19, 0)));
        calendar1.setWorkingHours(workingHours1);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar1.setPlannedMeetings(plannedMeetings);

        Calendar calendar2 = new Calendar();
        WorkingHours workingHours2 = new WorkingHours();
        workingHours2.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours2.setEnd(String.valueOf(LocalTime.of(19, 0)));
        calendar2.setWorkingHours(workingHours2);
        List<PlannedMeeting> plannedMeetings2 = new ArrayList<>();
        PlannedMeeting plannedMeeting21 = new PlannedMeeting();
        plannedMeeting21.setStart("10:15");
        plannedMeeting21.setEnd("10:45");
        plannedMeetings2.add(plannedMeeting21);
        calendar2.setPlannedMeetings(plannedMeetings2);

        LocalTime meetingDuration = LocalTime.of(2, 15);

        // When
        List<String[]> response = schedule.findMeeting(calendar1, calendar2, meetingDuration);

        // Then
        Assert.assertEquals(2, response.size());
        Assert.assertArrayEquals(new String[]{"08:00", "10:15"}, response.get(0));
        Assert.assertArrayEquals(new String[]{"10:45", "19:00"}, response.get(1));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowException_NullInPlannedMeetings_OOT() {

        // Before
        Calendar calendar1 = new Calendar();
        WorkingHours workingHours1 = new WorkingHours();
        workingHours1.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours1.setEnd(String.valueOf(LocalTime.of(19, 0)));
        calendar1.setWorkingHours(workingHours1);
        List<PlannedMeeting> plannedMeetings = new ArrayList<>();
        PlannedMeeting plannedMeeting1 = new PlannedMeeting();
        plannedMeeting1.setStart("10:15");
        plannedMeeting1.setEnd("10:45");
        plannedMeetings.add(plannedMeeting1);
        calendar1.setPlannedMeetings(plannedMeetings);

        Calendar calendar2 = new Calendar();
        WorkingHours workingHours2 = new WorkingHours();
        workingHours2.setStart(String.valueOf(LocalTime.of(8, 0)));
        workingHours2.setEnd(String.valueOf(LocalTime.of(19, 0)));
        calendar2.setWorkingHours(workingHours2);
        List<PlannedMeeting> plannedMeetings2 = new ArrayList<>();
        PlannedMeeting plannedMeeting21 = new PlannedMeeting();
        plannedMeeting21.setStart("10:15");
        plannedMeeting21.setEnd("10:45");
        plannedMeetings2.add(plannedMeeting21);
        plannedMeetings2.add(null);
        calendar2.setPlannedMeetings(plannedMeetings2);

        LocalTime meetingDuration = LocalTime.of(2, 15);

        // Then
        schedule.findMeeting(calendar1, calendar2, meetingDuration);
    }
}


