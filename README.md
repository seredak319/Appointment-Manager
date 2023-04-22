# Appointment-Manager

The Appointment-Manager is a Java implementation of algorithm that provides possible meeting times based on two persons' calendars and the expected meeting duration.

### Input
The algorithm takes two persons' calendars with defined working hours and planned meetings and expected meeting duration, example of calendar:
```json
{
    "working_hours": {
        "start": "09:00",
        "end": "16:30"
    },
    "planned_meeting": [
        {
            "start": "09:00",
            "end": "10:30"
        },
        {
            "start": "12:00",
            "end": "13:00"
        },
        {
            "start": "16:00",
            "end": "16:30"
        }
    ]
}
```

String format of meeting duration is "[HH:MM]"

Example: "[00:30]"



### Output
The program returns the possible meeting times in the form of time ranges in list of string arrays.

Example:  
[["11:30","12:00"], ["15:00", "16:00"]]

### Tests
JUnit 4
