package pl.orange.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pl.orange.model.Calendar;
import pl.orange.model.PlannedMeeting;
import pl.orange.model.WorkingHours;

import java.io.IOException;
import java.util.List;

public class ScheduleUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Calendar parseJsonToCalendar(String jsonString) throws IOException {

        if (jsonString == null) {
            throw new NullPointerException("Given string should not be null");
        }

        try {
            Calendar calendar = new Calendar();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            calendar.setWorkingHours(objectMapper.treeToValue(rootNode.get("working_hours"), WorkingHours.class));
            calendar.setPlannedMeetings(List.of(objectMapper.treeToValue(rootNode.get("planned_meeting"), PlannedMeeting[].class)));

            validCalendarData(calendar);
            return calendar;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Given argument is not valid Calendar JSON");
        }
    }

    public static void validCalendarData(Calendar calendar) {

        if (calendar == null) {
            throw new NullPointerException("Given calendar is null");
        }

        if (calendar.getWorkingHours() == null) {
            throw new NullPointerException("Null working_hours");
        }

        if (calendar.getPlannedMeetings() == null) {
            throw new NullPointerException("Null planned_meetings");
        }

        if (calendar.getWorkingHours().getStart() == null || calendar.getWorkingHours().getEnd() == null) {
            throw new IllegalArgumentException("Invalid working_hours, it has to contain start and end data");
        }

        if (calendar.getWorkingHours().getEndAsTime().compareTo(calendar.getWorkingHours().getStartAsTime()) <= 0) {
            throw new IllegalArgumentException("Invalid working_hours data, duration between start and end must be positive." +
                    "\n start: " + calendar.getWorkingHours().getStart() + ", end: " + calendar.getWorkingHours().getEnd());
        }

        for (PlannedMeeting p : calendar.getPlannedMeetings()) {
            if (p == null) {
                throw new NullPointerException("Empty planned_meeting data element");
            }

            if (p.getEndAsTime().compareTo(p.getStartAsTime()) <= 0) {
                throw new IllegalArgumentException("Invalid planned_meeting data element, duration between start and end must be positive." +
                        "\n start: " + p.getStart() + ", end: " + p.getEnd());
            }

            if (p.getStartAsTime().compareTo(calendar.getWorkingHours().getStartAsTime()) < 0 || p.getEndAsTime().compareTo(calendar.getWorkingHours().getEndAsTime()) > 0) {
                throw new IllegalArgumentException("Invalid planned_meeting data element, meeting should be in working hours." +
                        "\n start of work: " + calendar.getWorkingHours().getStart() + ", end of work: " + calendar.getWorkingHours().getEnd() +
                        "\n start of meeting: " + p.getStart() + ", end of meeting: " + p.getEnd());
            }
        }
    }
}
