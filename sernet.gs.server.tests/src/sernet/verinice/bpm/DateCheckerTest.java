package sernet.verinice.bpm;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

public class DateCheckerTest {

    private DateChecker dateChecker = new DateChecker();

    @Test
    public void testCheckIfDateIsPastNow() {
        Instant now = Instant.now();
        Clock clock = Clock.fixed(now, ZoneId.of("Europe/Berlin"));

        Assert.assertEquals(Date.from(now),
                dateChecker.checkIfDateIsPast(Date.from(now), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastHalfAnHourFromNow() {
        Date halfAnHourFromNow = Date.from(Instant.now().plus(Duration.ofMinutes(30l)));
        Assert.assertEquals(halfAnHourFromNow,
                dateChecker.checkIfDateIsPast(halfAnHourFromNow, "7"));
    }

    @Test
    public void testCheckIfDateIsPastWithInvalidDelta() {
        Date yesterday = Date.from(Instant.now().minus(Duration.ofDays(1l)));

        try {
            dateChecker.checkIfDateIsPast(yesterday, "0");
            Assert.fail("Expected an exception to be thrown");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testCheckIfDateIsPastYesterday() {
        Instant now = Instant.now();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(Date.from(now));
        gregorianCalendar.add(Calendar.DAY_OF_YEAR, 6);
        Date time = gregorianCalendar.getTime();
        Date yesterday = Date.from(now.minus(Duration.ofDays(1l)));
        Assert.assertEquals(time, dateChecker.checkIfDateIsPast(yesterday, "7"));
    }

    @Test
    public void testCheckIfDateIsPastYesterdayWithClockChangeForward() {
        ZonedDateTime noonOnDayOfClockChange = ZonedDateTime.of(2018, 3, 25, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        ZonedDateTime noonOnDayBeforeClockChange = noonOnDayOfClockChange.minusDays(1l);
        Clock clock = Clock.fixed(noonOnDayOfClockChange.toInstant(), ZoneId.of("Europe/Berlin"));
        Date expectedResult = Date.from(noonOnDayBeforeClockChange.plusDays(7l).toInstant());
        Assert.assertEquals(expectedResult, dateChecker
                .checkIfDateIsPast(Date.from(noonOnDayBeforeClockChange.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastYesterdayWithClockChangeBackward() {
        ZonedDateTime noonOnDayOfClockChange = ZonedDateTime.of(2018, 10, 28, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        ZonedDateTime noonOnDayBeforeClockChange = noonOnDayOfClockChange.minusDays(1l);
        Clock clock = Clock.fixed(noonOnDayOfClockChange.toInstant(), ZoneId.of("Europe/Berlin"));
        Date expectedResult = Date.from(noonOnDayBeforeClockChange.plusDays(7l).toInstant());
        Assert.assertEquals(expectedResult, dateChecker
                .checkIfDateIsPast(Date.from(noonOnDayBeforeClockChange.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastHalfAnHourAgo() {
        Instant now = Instant.now();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(Date.from(now));
        gregorianCalendar.add(Calendar.DAY_OF_YEAR, 7);
        gregorianCalendar.add(Calendar.MINUTE, -30);
        Date time = gregorianCalendar.getTime();

        Date halfAnHourAgo = Date.from(now.minus(Duration.ofMinutes(30l)));
        Assert.assertEquals(time, dateChecker.checkIfDateIsPast(halfAnHourAgo, "7"));
    }

    @Test
    public void testCheckIfDateIsPastDeltaDaysAgo() {
        Instant now = Instant.now();
        Clock clock = Clock.fixed(now, ZoneId.of("Europe/Berlin"));

        Date sevenDaysAgo = Date.from(now.atZone(ZoneId.systemDefault()).minusDays(7l).toInstant());
        Assert.assertEquals(Date.from(now),
                dateChecker.checkIfDateIsPast(sevenDaysAgo, "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastOneWeekAndHalfAnHourAgo() {
        Instant now = Instant.now();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(Date.from(now));
        gregorianCalendar.add(Calendar.DAY_OF_YEAR, 7);
        gregorianCalendar.add(Calendar.MINUTE, -30);
        Date time = gregorianCalendar.getTime();

        Date oneWeekAndHalfAnHourAgo = Date.from(
                now.atZone(ZoneId.systemDefault()).minusMinutes(30l).minusDays(7l).toInstant());
        Assert.assertEquals(time, dateChecker.checkIfDateIsPast(oneWeekAndHalfAnHourAgo, "7"));
    }

    @Test
    public void testCheckIfDateIsPastOneWeekMinusHalfAnHourAgo() {
        Instant now = Instant.now();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(Date.from(now));
        gregorianCalendar.add(Calendar.MINUTE, 30);
        Date time = gregorianCalendar.getTime();

        Date oneWeekMinusHalfAnHourAgo = Date.from(
                now.atZone(ZoneId.systemDefault()).plusMinutes(30l).minusDays(7l).toInstant());
        Assert.assertEquals(time, dateChecker.checkIfDateIsPast(oneWeekMinusHalfAnHourAgo, "7"));
    }

    @Test
    public void testCheckIfDateIsPastHalfAYearAgoWhereUTCOffsetIncreases() {
        // UTC+02:00
        ZonedDateTime dateToUseAsToday = ZonedDateTime.of(2019, 5, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        Clock clock = Clock.fixed(dateToUseAsToday.toInstant(), ZoneId.of("Europe/Berlin"));
        // UTC+01:00
        ZonedDateTime halfAYearBefore = ZonedDateTime.of(2018, 11, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        DayOfWeek weekday = halfAYearBefore.getDayOfWeek();
        ZonedDateTime nextDateWithSameWeekDay = dateToUseAsToday
                .with(TemporalAdjusters.nextOrSame(weekday));

        Assert.assertEquals(Date.from(nextDateWithSameWeekDay.toInstant()),
                dateChecker.checkIfDateIsPast(Date.from(halfAYearBefore.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastHalfAYearAgoWhereUTCOffsetDecreases() {
        // UTC+01:00
        ZonedDateTime dateToUseAsToday = ZonedDateTime.of(2019, 1, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        Clock clock = Clock.fixed(dateToUseAsToday.toInstant(), ZoneId.of("Europe/Berlin"));
        // UTC+02:00
        ZonedDateTime halfAYearBefore = ZonedDateTime.of(2018, 7, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        DayOfWeek weekday = halfAYearBefore.getDayOfWeek();
        ZonedDateTime nextDateWithSameWeekDay = dateToUseAsToday
                .with(TemporalAdjusters.nextOrSame(weekday));

        Assert.assertEquals(Date.from(nextDateWithSameWeekDay.toInstant()),
                dateChecker.checkIfDateIsPast(Date.from(halfAYearBefore.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastHalfAYearAgoWithSameUTCOffset() {
        // UTC+02:00
        ZonedDateTime dateToUseAsToday = ZonedDateTime.of(2019, 4, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        Clock clock = Clock.fixed(dateToUseAsToday.toInstant(), ZoneId.of("Europe/Berlin"));
        // UTC+02:00
        ZonedDateTime halfAYearBefore = ZonedDateTime.of(2018, 10, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));

        Assert.assertEquals(Date.from(dateToUseAsToday.toInstant()),
                dateChecker.checkIfDateIsPast(Date.from(halfAYearBefore.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastHalfAYearAgoWithSameUTCOffsetAndClockChangesInBetween() {
        // UTC+02:00
        ZonedDateTime dateToUseAsToday = ZonedDateTime.of(2019, 10, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        Clock clock = Clock.fixed(dateToUseAsToday.toInstant(), ZoneId.of("Europe/Berlin"));
        // UTC+02:00
        ZonedDateTime halfAYearBefore = ZonedDateTime.of(2019, 4, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));

        DayOfWeek weekday = halfAYearBefore.getDayOfWeek();
        ZonedDateTime nextDateWithSameWeekDay = dateToUseAsToday
                .with(TemporalAdjusters.nextOrSame(weekday));
        Assert.assertEquals(Date.from(nextDateWithSameWeekDay.toInstant()),
                dateChecker.checkIfDateIsPast(Date.from(halfAYearBefore.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastThreeMonthsAgoWithSameUTCOffset() {
        // UTC+01:00
        ZonedDateTime dateToUseAsToday = ZonedDateTime.of(2019, 3, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        Clock clock = Clock.fixed(dateToUseAsToday.toInstant(), ZoneId.of("Europe/Berlin"));
        // UTC+01:00
        ZonedDateTime threeMonthsBefore = ZonedDateTime.of(2018, 12, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));

        DayOfWeek weekday = threeMonthsBefore.getDayOfWeek();
        ZonedDateTime nextDateWithSameWeekDay = dateToUseAsToday
                .with(TemporalAdjusters.nextOrSame(weekday));
        Assert.assertEquals(Date.from(nextDateWithSameWeekDay.toInstant()), dateChecker
                .checkIfDateIsPast(Date.from(threeMonthsBefore.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastDeltaAgoWhereUTCOffsetIncreases() {
        // UTC+02:00
        ZonedDateTime dateToUseAsToday = ZonedDateTime.of(2019, 4, 1, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        Clock clock = Clock.fixed(dateToUseAsToday.toInstant(), ZoneId.of("Europe/Berlin"));
        // UTC+01:00
        ZonedDateTime dateInPast = ZonedDateTime.of(2019, 3, 29, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        DayOfWeek weekday = dateInPast.getDayOfWeek();
        ZonedDateTime nextDateWithSameWeekDay = dateToUseAsToday
                .with(TemporalAdjusters.nextOrSame(weekday));

        Assert.assertEquals(Date.from(nextDateWithSameWeekDay.toInstant()),
                dateChecker.checkIfDateIsPast(Date.from(dateInPast.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastDeltaAgoWhereUTCOffsetDecreases() {
        // UTC+02:00
        ZonedDateTime dateToUseAsToday = ZonedDateTime.of(2019, 10, 30, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        Clock clock = Clock.fixed(dateToUseAsToday.toInstant(), ZoneId.of("Europe/Berlin"));
        // UTC+01:00
        ZonedDateTime dateInPast = ZonedDateTime.of(2019, 10, 25, 12, 0, 0, 0,
                ZoneId.of("Europe/Berlin"));
        DayOfWeek weekday = dateInPast.getDayOfWeek();
        ZonedDateTime nextDateWithSameWeekDay = dateToUseAsToday
                .with(TemporalAdjusters.nextOrSame(weekday));

        Assert.assertEquals(Date.from(nextDateWithSameWeekDay.toInstant()),
                dateChecker.checkIfDateIsPast(Date.from(dateInPast.toInstant()), "7", clock));
    }

    @Test
    public void testCheckIfDateIsPastYesterdayWithDelta2() {
        Instant now = Instant.now();
        Date yesterday = Date.from(now.minus(Duration.ofDays(1l)));
        Date tomorrow = Date.from(now.plus(Duration.ofDays(1l)));
        Assert.assertEquals(tomorrow, dateChecker.checkIfDateIsPast(yesterday, "2"));
    }

    @Test
    public void testCheckIfDateIsPastTomorrow() {
        Date tomorrow = Date.from(Instant.now().plus(Duration.ofDays(1l)));
        Assert.assertEquals(tomorrow, dateChecker.checkIfDateIsPast(tomorrow, "7"));
    }

    @Test
    public void testGetIntValue() {
        Assert.assertEquals(7, dateChecker.getIntValue("7"));
        Assert.assertEquals(3, dateChecker.getIntValue("3"));
        Assert.assertEquals(7, dateChecker.getIntValue("illegal"));
    }

}
