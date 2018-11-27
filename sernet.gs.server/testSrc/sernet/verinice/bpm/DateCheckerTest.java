package sernet.verinice.bpm;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
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
    public void testCheckIfDateIsPastToday() {
        Date today = new Date();
        Assert.assertEquals(today, dateChecker.checkIfDateIsPast(today, "7"));
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
    public void testCheckIfDateIsPastOneWeekAndHalfAnHourAgo() {
        Instant now = Instant.now();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(Date.from(now));
        gregorianCalendar.add(Calendar.DAY_OF_YEAR, 7);
        gregorianCalendar.add(Calendar.MINUTE, -30);
        Date time = gregorianCalendar.getTime();

        Date oneWeekAndHalfAnHourAgo = Date
                .from(now.minus(Duration.ofMinutes(30l)).minus(Duration.ofDays(7l)));
        Assert.assertEquals(time, dateChecker.checkIfDateIsPast(oneWeekAndHalfAnHourAgo, "7"));
    }

    @Test
    public void testCheckIfDateIsPastOneWeekMinusHalfAnHourAgo() {
        Instant now = Instant.now();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(Date.from(now));
        gregorianCalendar.add(Calendar.MINUTE, 30);
        Date time = gregorianCalendar.getTime();

        Date oneWeekMinusHalfAnHourAgo = Date
                .from(now.plus(Duration.ofMinutes(30l)).minus(Duration.ofDays(7l)));
        Assert.assertEquals(time, dateChecker.checkIfDateIsPast(oneWeekMinusHalfAnHourAgo, "7"));
    }

    @Test
    public void testCheckIfDateIsPastHalfAYearAgo() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime halfAYearAgo = now.minus(Period.ofMonths(6));
        DayOfWeek weekday = halfAYearAgo.getDayOfWeek();
        ZonedDateTime nextDayWithSameWeekDay = now.with(TemporalAdjusters.nextOrSame(weekday));

        Assert.assertEquals(Date.from(nextDayWithSameWeekDay.toInstant()),
                dateChecker.checkIfDateIsPast(Date.from(halfAYearAgo.toInstant()), "7"));
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
