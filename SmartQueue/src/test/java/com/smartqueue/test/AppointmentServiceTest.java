package com.smartqueue.test;

import com.smartqueue.service.AppointmentService;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentServiceTest {
    private static final AppointmentService service = new AppointmentService();

    @Test
    @DisplayName("Past date booking should fail")
    void testPastDateBookingFails() {
        AppointmentService.BookingResult result = service.bookAppointment(
                1, 1,
                LocalDate.now().minusDays(1),
                LocalTime.of(9, 0)
        );
        assertFalse(result.isSuccess(), "Should not allow booking in the past");
    }

    @Test
    @DisplayName("Outside clinic hours booking should fail")
    void testOutsideHoursBookingFails() {
        AppointmentService.BookingResult result = service.bookAppointment(
                1, 1,
                LocalDate.now().plusDays(1),
                LocalTime.of(20, 0)
        );
        assertFalse(result.isSuccess(), "Should not allow booking outside 07:00-17:00");
    }

    @Test
    @DisplayName("Valid future appointment should succeed")
    void testValidBookingSucceeds() {
        AppointmentService.BookingResult result = service.bookAppointment(
                1, 1,
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0)
        );
        assertTrue(result.isSuccess(), "Valid future appointment should be booked successfully");
    }
}