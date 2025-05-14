package com.metrocard;

import com.metrocard.data.Journey;
import com.metrocard.data.Rate;
import com.metrocard.data.Route;
import com.metrocard.data.TimeSlot;
import com.metrocard.exceptions.RateNotDefinedException;
import org.junit.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestTigerCard {
    static Map<Route, Rate> rateCard;
    static Map<DayOfWeek, List<TimeSlot>> peakTimeSlots;
    static Map<Route, Integer> dailyCaps;
    static Map<Route, Integer> weeklyCaps;

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    TigerCard card;

    @BeforeClass
    public static void setupData(){
        setupRateCard();
        setupPeakTimeSlots();
        setupDailyCap();
        setupWeeklyCaps();
    }

    @Before
    public void setupTest(){
        card = new TigerCard(rateCard, peakTimeSlots, dailyCaps, weeklyCaps);
    }

    @Test
    public void testFromZoneOneToZoneOneSingleJourneyOffPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:45", formatter), 1, 1);
        Assert.assertEquals(25, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneTwoSingleJourneyOffPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:45", formatter), 2, 2);
        Assert.assertEquals(20, card.getFare());
    }

    @Test
    public void testFromZoneOneToZoneOneSingleWeekdayJourneyPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:20", formatter), 1, 1);
        Assert.assertEquals(30, card.getFare());
    }

    @Test
    public void testFromZoneOneToZoneOneSingleWeekendJourneyPeak() {
        card.add(LocalDateTime.parse("03-04-2022 09:30", formatter), 1, 1);
        Assert.assertEquals(30, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneTwoSingleWeekdayJourneyPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:20", formatter), 2, 2);
        Assert.assertEquals(25, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneTwoSingleWeekendJourneyPeak() {
        card.add(LocalDateTime.parse("03-04-2022 09:30", formatter), 2, 2);
        Assert.assertEquals(25, card.getFare());
    }

    @Test
    public void testFromZoneOneToZoneTwoSingleJourneyOffPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:45", formatter), 1, 2);
        Assert.assertEquals(30, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneOneSingleJourneyOffPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:45", formatter), 2, 1);
        Assert.assertEquals(30, card.getFare());
    }

    @Test
    public void testFromZoneOneToZoneTwoSingleWeekdayJourneyPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:20", formatter), 1, 2);
        Assert.assertEquals(35, card.getFare());
    }

    @Test
    public void testFromZoneOneToZoneTwoSingleWeekendJourneyPeak() {
        card.add(LocalDateTime.parse("03-04-2022 09:30", formatter), 1, 2);
        Assert.assertEquals(35, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneOneSingleWeekdayJourneyPeak() {
        card.add(LocalDateTime.parse("04-04-2022 10:20", formatter), 2, 1);
        Assert.assertEquals(35, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneOneSingleWeekendJourneyPeak() {
        card.add(LocalDateTime.parse("03-04-2022 09:30", formatter), 2, 1);
        Assert.assertEquals(35, card.getFare());
    }

    @Test
    public void testRateNotDefinedException() {
        Exception exception = Assert.assertThrows(RateNotDefinedException.class, ()->
                card.add(LocalDateTime.parse("03-04-2022 09:30", formatter), 1, 3));

        String expectedMessage = "Rate not defined for the Journey";
        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testMultipleJourneysWithoutAnyCapping(){
        //MONDAY 1-1 OffPeak = 25
        card.add(LocalDateTime.parse("04-04-2022 11:30", formatter), 1, 1);

        //TUESDAY 1-2 Offpeak = 30
        card.add(LocalDateTime.parse("05-04-2022 11:30", formatter), 1, 2);

        //WEDNESDAY 2-1 Peak = 35
        card.add(LocalDateTime.parse("06-04-2022 10:15", formatter), 2, 1);

        //THURSDAY 2-2 Peak = 25
        card.add(LocalDateTime.parse("07-04-2022 17:30", formatter), 2, 2);

        //FRIDAY 1-1 Peak = 30
        card.add(LocalDateTime.parse("08-04-2022 19:30", formatter), 1, 1);

        //SATURDAY 1-2 OffPeak = 30
        card.add(LocalDateTime.parse("09-04-2022 12:30", formatter), 1, 2);

        //SUNDAY 2-2 Peak = 25
        card.add(LocalDateTime.parse("10-04-2022 19:00", formatter), 2, 2);

        //Total Fare = 200
        Assert.assertEquals(200, card.getFare());
    }

    @Test
    public void testPeakHourBoundariesWithMultipleJourneysWithoutAnyCapping(){
        //MONDAY 1-1 Morning Peak Hour Boundaries = 30 + 30
        card.add(LocalDateTime.parse("04-04-2022 07:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 10:30", formatter), 1, 1);

        //TUESDAY 1-2 Evening Peak Hour Boundaries = 35 + 35
        card.add(LocalDateTime.parse("05-04-2022 17:00", formatter), 1, 2);
        card.add(LocalDateTime.parse("05-04-2022 20:00", formatter), 1, 2);

        //SATURDAY 2-1 Morning Peak Hour Boundaries = 35 + 35
        card.add(LocalDateTime.parse("09-04-2022 09:00", formatter), 2, 1);
        card.add(LocalDateTime.parse("09-04-2022 11:00", formatter), 2, 1);

        //SUNDAY 2-2 Evening Peak Hour Boundaries = 25 + 25
        card.add(LocalDateTime.parse("10-04-2022 18:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("10-04-2022 22:00", formatter), 2, 2);

        //Total Fare = 250
        Assert.assertEquals(250, card.getFare());
    }

    @Test
    public void testFromZoneOneToZoneOneDailyCap(){
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 19:40", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 19:50", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 20:00", formatter), 1, 1);

        Assert.assertEquals(100, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneTwoDailyCap(){
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("04-04-2022 19:40", formatter), 2, 2);
        card.add(LocalDateTime.parse("04-04-2022 19:50", formatter), 2, 2);
        card.add(LocalDateTime.parse("04-04-2022 20:00", formatter), 2, 2);

        Assert.assertEquals(80, card.getFare());
    }

    @Test
    public void testFromZoneOneToZoneTwoDailyCap(){
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 1, 2);
        card.add(LocalDateTime.parse("04-04-2022 19:40", formatter), 1, 2);
        card.add(LocalDateTime.parse("04-04-2022 19:50", formatter), 1, 2);
        card.add(LocalDateTime.parse("04-04-2022 20:00", formatter), 1, 2);

        Assert.assertEquals(120, card.getFare());
    }

    @Test
    public void testFromZoneTwoToZoneOneDailyCap(){
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 2, 1);
        card.add(LocalDateTime.parse("04-04-2022 19:40", formatter), 2, 1);
        card.add(LocalDateTime.parse("04-04-2022 19:50", formatter), 2, 1);
        card.add(LocalDateTime.parse("04-04-2022 20:00", formatter), 2, 1);

        Assert.assertEquals(120, card.getFare());
    }

    @Test
    public void testSingleRouteMultipleDaysJourneyDailyCap(){
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 19:40", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 19:50", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 20:00", formatter), 1, 1);

        card.add(LocalDateTime.parse("05-04-2022 20:10", formatter), 2, 2);
        card.add(LocalDateTime.parse("05-04-2022 20:20", formatter), 2, 2);
        card.add(LocalDateTime.parse("05-04-2022 20:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("05-04-2022 20:40", formatter), 2, 2);

        Assert.assertEquals(180, card.getFare());
    }

    @Test
    public void testMultipleInterZoneRoutesSingleDayJourneyDailyCap(){
        //Peak hours Single fare = 35
        card.add(LocalDateTime.parse("04-04-2022 10:20", formatter), 2, 1);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("04-04-2022 10:45", formatter), 1, 1);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("04-04-2022 16:15", formatter), 1, 1);

        //Peak hours Single fare = 30
        card.add(LocalDateTime.parse("04-04-2022 18:15", formatter), 1, 1);

        //The Daily cap reached 120 for zone 1 - 2. Charged 5 instead of 35
        card.add(LocalDateTime.parse("04-04-2022 19:00", formatter), 1, 2);

        //The Daily cap already reached, so this journey should be free
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 2, 2);

        //Total Fare = 120
        Assert.assertEquals(120, card.getFare());
    }

    @Test
    public void testMultipleIntraZoneRoutesSingleDayJourneyDailyCap(){
        //Peak Hours Single Fare = 25
        card.add(LocalDateTime.parse("04-04-2022 10:30", formatter), 2, 2);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("04-04-2022 11:30", formatter), 1, 1);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("04-04-2022 12:30", formatter), 1, 1);

        //Off-peak single fare = 25. Cap reached, as all the journeys are intra zone,
        // the farthest journey route is decided based on which journey route has largest cap.
        // 1-1 in this case has 100 as compared to 2-2 which is 80. So, daily cap applied is 100.
        card.add(LocalDateTime.parse("04-04-2022 13:30", formatter), 1, 1);

        //Off-peak single fare = 0
        card.add(LocalDateTime.parse("04-04-2022 16:30", formatter), 2, 2);

        //Off-peak single fare = 0
        card.add(LocalDateTime.parse("04-04-2022 17:30", formatter), 2, 2);

        //Peak Hours single fare = 0
        card.add(LocalDateTime.parse("04-04-2022 18:30", formatter), 2, 2);

        //Peak Hours single fare = 0
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 2, 2);

        //Total Fare = 100
        Assert.assertEquals(100, card.getFare());
    }

    @Test
    public void testMultipleInterZoneRoutesMultipleDayJourneyDailyCap(){
        //DAY-1 MONDAY = Total fare = 120
        //Peak hours Single fare = 35
        card.add(LocalDateTime.parse("04-04-2022 10:20", formatter), 2, 1);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("04-04-2022 10:45", formatter), 1, 1);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("04-04-2022 16:15", formatter), 1, 1);

        //Peak hours Single fare = 30
        card.add(LocalDateTime.parse("04-04-2022 18:15", formatter), 1, 1);

        //The Daily Cap reached 120 for zone 1 - 2. Charged 5 instead of 35
        card.add(LocalDateTime.parse("04-04-2022 19:00", formatter), 1, 2);

        //The Daily cap already reached, so this journey should be free
        card.add(LocalDateTime.parse("04-04-2022 19:30", formatter), 2, 2);


        //DAY-2 TUESDAY Total Fare = 100
        //Peak Hours Single Fare = 25
        card.add(LocalDateTime.parse("05-04-2022 10:30", formatter), 2, 2);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("05-04-2022 11:30", formatter), 1, 1);

        //Off-peak single fare = 25
        card.add(LocalDateTime.parse("05-04-2022 12:30", formatter), 1, 1);

        //Off-peak single fare = 25. Daily Cap reached, as all the journeys are intra zone,
        // the farthest journey route is decided based on which journey route has largest cap.
        // 1-1 in this case has 100 as compared to 2-2 which is 80. So, daily cap applied is 100.
        card.add(LocalDateTime.parse("05-04-2022 13:30", formatter), 1, 1);

        //Off-peak single fare = 0
        card.add(LocalDateTime.parse("05-04-2022 16:30", formatter), 2, 2);

        //Off-peak single fare = 0
        card.add(LocalDateTime.parse("05-04-2022 17:30", formatter), 2, 2);

        //Peak Hours single fare = 0
        card.add(LocalDateTime.parse("05-04-2022 18:30", formatter), 2, 2);

        //Peak Hours single fare = 0
        card.add(LocalDateTime.parse("05-04-2022 19:30", formatter), 2, 2);

        //Total Fare = 220
        Assert.assertEquals(220, card.getFare());
    }

    @Test
    public void testIntraZoneWeeklyCapWithNestedDailyCaps(){
        //MONDAY
        card.add(LocalDateTime.parse("04-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("04-04-2022 18:00", formatter), 1, 1);
        //Daily Cap reached = 100
        card.add(LocalDateTime.parse("04-04-2022 18:30", formatter), 1, 1);
        //Free Journey
        card.add(LocalDateTime.parse("04-04-2022 19:00", formatter), 1, 1);

        //TUESDAY
        card.add(LocalDateTime.parse("05-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("05-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("05-04-2022 18:00", formatter), 1, 1);
        //Daily Cap reached = 100
        card.add(LocalDateTime.parse("05-04-2022 18:30", formatter), 1, 1);
        //Free Journey
        card.add(LocalDateTime.parse("05-04-2022 19:00", formatter), 1, 1);

        //WEDNESDAY
        card.add(LocalDateTime.parse("06-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("06-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("06-04-2022 18:00", formatter), 1, 1);
        //Daily Cap reached = 100
        card.add(LocalDateTime.parse("06-04-2022 18:30", formatter), 1, 1);
        //Free Journey
        card.add(LocalDateTime.parse("06-04-2022 19:00", formatter), 1, 1);

        //THURSDAY
        card.add(LocalDateTime.parse("07-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("07-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("07-04-2022 18:00", formatter), 1, 1);
        //Daily Cap reached = 100
        card.add(LocalDateTime.parse("07-04-2022 18:30", formatter), 1, 1);
        //Free Journey
        card.add(LocalDateTime.parse("07-04-2022 19:00", formatter), 1, 1);

        card.add(LocalDateTime.parse("08-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("08-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("08-04-2022 18:00", formatter), 1, 1);
        //FRIDAY Daily Cap reached = 100, Weekly Cap Reached = 500
        card.add(LocalDateTime.parse("08-04-2022 18:30", formatter), 1, 1);
        //Free journeys here onwards for the end of the week
        card.add(LocalDateTime.parse("08-04-2022 19:00", formatter), 1, 1);

        //SATURDAY All free journeys as weekly cap is already reached
        card.add(LocalDateTime.parse("09-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("09-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("09-04-2022 18:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("09-04-2022 18:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("09-04-2022 19:00", formatter), 1, 1);

        //SUNDAY All free journeys as weekly cap is already reached
        card.add(LocalDateTime.parse("10-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("10-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("10-04-2022 18:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("10-04-2022 18:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("10-04-2022 19:00", formatter), 1, 1);

        //Next Week MONDAY
        card.add(LocalDateTime.parse("11-04-2022 17:00", formatter), 1, 1);
        card.add(LocalDateTime.parse("11-04-2022 17:30", formatter), 1, 1);
        card.add(LocalDateTime.parse("11-04-2022 18:00", formatter), 1, 1);
        //Daily Cap reached = 100
        card.add(LocalDateTime.parse("11-04-2022 18:30", formatter), 1, 1);
        //Free journey
        card.add(LocalDateTime.parse("11-04-2022 19:00", formatter), 1, 1);

        Assert.assertEquals(600, card.getFare());
    }

    @Test
    public void testIntraZoneWeeklyCapWithFarthestRoute2To2(){
        //MONDAY
        card.add(LocalDateTime.parse("04-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("04-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("04-04-2022 18:00", formatter), 2, 2);
        //Daily Cap reached = 80
        card.add(LocalDateTime.parse("04-04-2022 18:30", formatter), 2, 2);
        //Free Journey
        card.add(LocalDateTime.parse("04-04-2022 19:00", formatter), 2, 2);

        //TUESDAY
        card.add(LocalDateTime.parse("05-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("05-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("05-04-2022 18:00", formatter), 2, 2);
        //Daily Cap reached = 80
        card.add(LocalDateTime.parse("05-04-2022 18:30", formatter), 2, 2);
        //Free Journey
        card.add(LocalDateTime.parse("05-04-2022 19:00", formatter), 2, 2);

        //WEDNESDAY
        card.add(LocalDateTime.parse("06-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("06-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("06-04-2022 18:00", formatter), 2, 2);
        //Daily Cap reached = 80
        card.add(LocalDateTime.parse("06-04-2022 18:30", formatter), 2, 2);
        //Free Journey
        card.add(LocalDateTime.parse("06-04-2022 19:00", formatter), 2, 2);

        //THURSDAY
        card.add(LocalDateTime.parse("07-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("07-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("07-04-2022 18:00", formatter), 2, 2);
        //Daily Cap reached = 80
        card.add(LocalDateTime.parse("07-04-2022 18:30", formatter), 2, 2);
        //Free Journey
        card.add(LocalDateTime.parse("07-04-2022 19:00", formatter), 2, 2);

        card.add(LocalDateTime.parse("08-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("08-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("08-04-2022 18:00", formatter), 2, 2);
        //FRIDAY Daily Cap reached = 80, Weekly Cap Reached = 400
        card.add(LocalDateTime.parse("08-04-2022 18:30", formatter), 2, 2);
        //Free journeys here onwards for the end of the week
        card.add(LocalDateTime.parse("08-04-2022 19:00", formatter), 2, 2);

        //SATURDAY All free journeys as weekly cap is already reached
        card.add(LocalDateTime.parse("09-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("09-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("09-04-2022 18:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("09-04-2022 18:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("09-04-2022 19:00", formatter), 2, 2);

        //SUNDAY All free journeys as weekly cap is already reached
        card.add(LocalDateTime.parse("10-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("10-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("10-04-2022 18:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("10-04-2022 18:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("10-04-2022 19:00", formatter), 2, 2);

        //Next Week MONDAY
        card.add(LocalDateTime.parse("11-04-2022 17:00", formatter), 2, 2);
        card.add(LocalDateTime.parse("11-04-2022 17:30", formatter), 2, 2);
        card.add(LocalDateTime.parse("11-04-2022 18:00", formatter), 2, 2);
        //Daily Cap reached = 80
        card.add(LocalDateTime.parse("11-04-2022 18:30", formatter), 2, 2);
        //Free journey
        card.add(LocalDateTime.parse("11-04-2022 19:00", formatter), 2, 2);

        Assert.assertEquals(480, card.getFare());
    }


    @Test
    public void testInterZoneWeeklyCapWithNestedDailyCaps(){
        //MONDAY
        card.add(LocalDateTime.parse("04-04-2022 17:00", formatter), 1, 1);//30
        card.add(LocalDateTime.parse("04-04-2022 17:30", formatter), 1, 1);//30
        card.add(LocalDateTime.parse("04-04-2022 18:00", formatter), 1, 2);//35
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("04-04-2022 18:30", formatter), 2, 1);//25
        //Free Journey
        card.add(LocalDateTime.parse("04-04-2022 19:00", formatter), 1, 1);//0

        //TUESDAY
        card.add(LocalDateTime.parse("05-04-2022 17:00", formatter), 1, 2);//35
        card.add(LocalDateTime.parse("05-04-2022 17:30", formatter), 1, 2);//35
        card.add(LocalDateTime.parse("05-04-2022 18:00", formatter), 1, 2);//35
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("05-04-2022 18:30", formatter), 2, 2);//15
        //Free Journey
        card.add(LocalDateTime.parse("05-04-2022 19:00", formatter), 2, 2);//0

        //WEDNESDAY
        card.add(LocalDateTime.parse("06-04-2022 17:00", formatter), 2, 2);//25
        card.add(LocalDateTime.parse("06-04-2022 17:30", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("06-04-2022 18:00", formatter), 1, 1);//30
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("06-04-2022 18:30", formatter), 1, 1);//30
        //Free Journey
        card.add(LocalDateTime.parse("06-04-2022 19:00", formatter), 1, 1);//0

        //THURSDAY
        card.add(LocalDateTime.parse("07-04-2022 17:00", formatter), 1, 1);//30
        card.add(LocalDateTime.parse("07-04-2022 17:30", formatter), 1, 2);//35
        card.add(LocalDateTime.parse("07-04-2022 18:00", formatter), 2, 1);//35
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("07-04-2022 18:30", formatter), 1, 2);//20
        //Free Journey
        card.add(LocalDateTime.parse("07-04-2022 19:00", formatter), 2, 1);//0

        //FRIDAY Daily Cap no reached, total fare = 80
        card.add(LocalDateTime.parse("08-04-2022 16:00", formatter), 1, 1);//25
        card.add(LocalDateTime.parse("08-04-2022 16:30", formatter), 1, 1);//25
        card.add(LocalDateTime.parse("08-04-2022 18:00", formatter), 1, 1);//30


        //SATURDAY
        card.add(LocalDateTime.parse("09-04-2022 17:00", formatter), 2, 1);//30
        //A weekly cap of 600 reached before the daily cap of 120
        card.add(LocalDateTime.parse("09-04-2022 17:30", formatter), 1, 2);//10
        //All further journeys of the week are free
        card.add(LocalDateTime.parse("09-04-2022 18:00", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("09-04-2022 18:30", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("09-04-2022 19:00", formatter), 1, 1);//0

        //SUNDAY All free journeys as weekly cap is already reached
        card.add(LocalDateTime.parse("10-04-2022 17:00", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("10-04-2022 17:30", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("10-04-2022 18:00", formatter), 1, 2);//0
        card.add(LocalDateTime.parse("10-04-2022 18:30", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("10-04-2022 19:00", formatter), 1, 1);//0

        //Next Week MONDAY, no cap reached, Total fare = 100
        card.add(LocalDateTime.parse("11-04-2022 17:00", formatter), 1, 2);//35
        card.add(LocalDateTime.parse("11-04-2022 17:30", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("11-04-2022 18:00", formatter), 1, 1);//30

        Assert.assertEquals(700, card.getFare());
    }

    @Test
    public void testInterZoneWeeklyCapWithFarthestRoute2To1(){
        //MONDAY
        card.add(LocalDateTime.parse("04-04-2022 17:00", formatter), 1, 1);//30
        card.add(LocalDateTime.parse("04-04-2022 17:30", formatter), 1, 1);//30
        card.add(LocalDateTime.parse("04-04-2022 18:00", formatter), 2, 1);//35
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("04-04-2022 18:30", formatter), 2, 1);//25
        //Free Journey
        card.add(LocalDateTime.parse("04-04-2022 19:00", formatter), 1, 1);//0

        //TUESDAY
        card.add(LocalDateTime.parse("05-04-2022 17:00", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("05-04-2022 17:30", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("05-04-2022 18:00", formatter), 2, 1);//35
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("05-04-2022 18:30", formatter), 2, 2);//15
        //Free Journey
        card.add(LocalDateTime.parse("05-04-2022 19:00", formatter), 2, 2);//0

        //WEDNESDAY
        card.add(LocalDateTime.parse("06-04-2022 17:00", formatter), 2, 2);//25
        card.add(LocalDateTime.parse("06-04-2022 17:30", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("06-04-2022 18:00", formatter), 1, 1);//30
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("06-04-2022 18:30", formatter), 1, 1);//30
        //Free Journey
        card.add(LocalDateTime.parse("06-04-2022 19:00", formatter), 1, 1);//0

        //THURSDAY
        card.add(LocalDateTime.parse("07-04-2022 17:00", formatter), 1, 1);//30
        card.add(LocalDateTime.parse("07-04-2022 17:30", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("07-04-2022 18:00", formatter), 2, 1);//35
        //Daily Cap reached = 120
        card.add(LocalDateTime.parse("07-04-2022 18:30", formatter), 2, 1);//20
        //Free Journey
        card.add(LocalDateTime.parse("07-04-2022 19:00", formatter), 2, 1);//0

        //FRIDAY Daily Cap no reached, total fare = 80
        card.add(LocalDateTime.parse("08-04-2022 16:00", formatter), 1, 1);//25
        card.add(LocalDateTime.parse("08-04-2022 16:30", formatter), 1, 1);//25
        card.add(LocalDateTime.parse("08-04-2022 18:00", formatter), 1, 1);//30

        //SATURDAY
        card.add(LocalDateTime.parse("09-04-2022 17:00", formatter), 2, 1);//30
        //A weekly cap of 600 reached before the daily cap of 120
        card.add(LocalDateTime.parse("09-04-2022 17:30", formatter), 2, 1);//10
        //All further journeys of the week are free
        card.add(LocalDateTime.parse("09-04-2022 18:00", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("09-04-2022 18:30", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("09-04-2022 19:00", formatter), 1, 1);//0

        //SUNDAY All free journeys as weekly cap is already reached
        card.add(LocalDateTime.parse("10-04-2022 17:00", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("10-04-2022 17:30", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("10-04-2022 18:00", formatter), 2, 1);//0
        card.add(LocalDateTime.parse("10-04-2022 18:30", formatter), 1, 1);//0
        card.add(LocalDateTime.parse("10-04-2022 19:00", formatter), 1, 1);//0

        //Next Week MONDAY, no cap reached, Total fare = 100
        card.add(LocalDateTime.parse("11-04-2022 17:00", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("11-04-2022 17:30", formatter), 2, 1);//35
        card.add(LocalDateTime.parse("11-04-2022 18:00", formatter), 1, 1);//30

        Assert.assertEquals(700, card.getFare());
    }

    @Test
    public void testAddJourneyFromZoneTwoToZoneOneDailyCap(){
        card.add(new Journey(LocalDateTime.parse("04-04-2022 19:30", formatter), 2, 1));
        card.add(new Journey(LocalDateTime.parse("04-04-2022 19:40", formatter), 2, 1));
        card.add(new Journey(LocalDateTime.parse("04-04-2022 19:50", formatter), 2, 1));
        card.add(new Journey(LocalDateTime.parse("04-04-2022 20:00", formatter), 2, 1));

        Assert.assertEquals(120, card.getFare());
    }

    @Test
    public void testBulkAddJourneysToCard(){
        List<Journey> journeyList = new ArrayList<>();

        journeyList.add(new Journey(LocalDateTime.parse("04-04-2022 19:30", formatter), 2, 1));
        journeyList.add(new Journey(LocalDateTime.parse("04-04-2022 19:40", formatter), 2, 1));
        journeyList.add(new Journey(LocalDateTime.parse("04-04-2022 19:50", formatter), 2, 1));
        journeyList.add(new Journey(LocalDateTime.parse("04-04-2022 20:00", formatter), 2, 1));

        card.add(journeyList);

        Assert.assertEquals(120, card.getFare());
    }

    private static void setupRateCard() {
        rateCard = new HashMap<>();
        rateCard.put(new Route(1, 1), new Rate(25,30));
        rateCard.put(new Route(2, 2), new Rate(20, 25));
        rateCard.put(new Route(1, 2), new Rate(30, 35));
        rateCard.put(new Route(2, 1), new Rate(30, 35));
    }

    private static void setupPeakTimeSlots() {
        peakTimeSlots = new HashMap<>();

        List<TimeSlot> weekDayPeakSlots = new ArrayList<>();
        weekDayPeakSlots.add(new TimeSlot(LocalTime.of(7, 0), LocalTime.of(10, 30)));
        weekDayPeakSlots.add(new TimeSlot(LocalTime.of(17, 0), LocalTime.of(20, 0)));
        peakTimeSlots.put(DayOfWeek.MONDAY, weekDayPeakSlots);
        peakTimeSlots.put(DayOfWeek.TUESDAY, weekDayPeakSlots);
        peakTimeSlots.put(DayOfWeek.WEDNESDAY, weekDayPeakSlots);
        peakTimeSlots.put(DayOfWeek.THURSDAY, weekDayPeakSlots);
        peakTimeSlots.put(DayOfWeek.FRIDAY, weekDayPeakSlots);

        List<TimeSlot> weekEndPeakSlots = new ArrayList<>();
        weekEndPeakSlots.add(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(11, 0)));
        weekEndPeakSlots.add(new TimeSlot(LocalTime.of(18, 0), LocalTime.of(22, 0)));
        peakTimeSlots.put(DayOfWeek.SATURDAY, weekEndPeakSlots);
        peakTimeSlots.put(DayOfWeek.SUNDAY, weekEndPeakSlots);
    }

    private static void setupDailyCap() {
        dailyCaps = new HashMap<>();
        dailyCaps.put(new Route(1, 1), 100);
        dailyCaps.put(new Route(2, 2), 80);
        dailyCaps.put(new Route(1, 2), 120);
        dailyCaps.put(new Route(2, 1), 120);
    }

    private static void setupWeeklyCaps() {
        weeklyCaps = new HashMap<>();
        weeklyCaps.put(new Route(1, 1), 500);
        weeklyCaps.put(new Route(1, 2), 600);
        weeklyCaps.put(new Route(2, 1), 600);
        weeklyCaps.put(new Route(2, 2), 400);
    }
}
