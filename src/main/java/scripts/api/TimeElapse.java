package scripts.api;

import org.tribot.api.Timing;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Purpose of class: Create a Time object for each Task instance (Composition methodology).
 * input fields can be left blank, which will be taken as 0 by default.
 */

public class TimeElapse {
    private long startTime = System.currentTimeMillis();

    private String condition;

    private Duration duration;

    private long timeElapsed;

    private long day;
    private long hour;
    private long minute;
    private long second;

    public TimeElapse(String condition) {
        this.condition = condition;

        if (this.condition != null && this.condition.matches("\\d\\d:\\d\\d:\\d\\d:\\d\\d")) {
            final String[] split = this.condition.split(":");

            if (split.length > 0) {
                this.day = Integer.parseInt(split[0], 10);
                this.hour = Integer.parseInt(split[1], 10);
                this.minute = Integer.parseInt(split[2], 10);
                this.second = Integer.parseInt(split[3], 10);
                completeDuration(
                        this.day,
                        this.hour,
                        this.minute,
                        this.second
                );
            }
        } else {
            this.duration = Duration.ZERO;
            System.out.println("Incorrect time elapsed format. DAYS:HOURS:MINUTES:SECONDS - 00:00:00:00");
        }
    }

    /**
     * If the duration is surpassed, task is complete.
     *
     * @return True if duration of time is surpassed; false otherwise.
     */
    public boolean isValidated() {
        return getDuration() != null && getDuration() != Duration.ZERO && getTimeElapsed() >= getDuration().toMillis();
    }

    public long getTimeElapsed() {
        calculateTimeElapsed();
        return timeElapsed;
    }

    private void calculateTimeElapsed() {
        setTimeElapsed(Timing.timeFromMark(getStartTime()));
    }

    // convert all fields (day, hour, minute, second) to milliseconds.
    // create a duration of the amount in ChronoUnit milliseconds.
    // for time elapsed, Task.
    private void completeDuration(long day, long hour, long minute, long second) {
        final long dayToMillisecond = Duration.of(day, ChronoUnit.DAYS)
                .toMillis();

        final long hourToMillisecond = Duration.of(hour, ChronoUnit.HOURS)
                .toMillis();

        final long minuteToMillisecond = Duration.of(minute, ChronoUnit.MINUTES)
                .toMillis();

        final long secondToMillisecond = Duration.of(second, ChronoUnit.SECONDS)
                .toMillis();

        final long totalTimeMillisecond = dayToMillisecond
                + hourToMillisecond
                + minuteToMillisecond
                + secondToMillisecond;

        this.duration = Duration.of(totalTimeMillisecond, ChronoUnit.MILLIS).abs();
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public long getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public long getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public long getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    @Override
    public String toString() {
        return this.condition;
    }
}
