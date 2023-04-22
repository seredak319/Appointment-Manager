package pl.orange.model;

import lombok.Data;

import java.util.List;

@Data
public class Calendar {

    private WorkingHours workingHours;
    private List<PlannedMeeting> plannedMeetings;
}
