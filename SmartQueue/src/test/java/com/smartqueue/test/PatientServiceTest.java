package com.smartqueue.test;


import com.smartqueue.service.PatientService;
import com.smartqueue.model.Patient;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class PatientServiceTest {
    private static final PatientService service = new PatientService();


    private static final String TEST_EMAIL = "testuser_" +
            System.currentTimeMillis() + "@clinic.com";

    @Test
    @Order(1)
    @DisplayName("Valid patient registration should succeed")
    void testRegisterSuccess() {
        boolean result = service.register(
                "Test User", "0821234567", TEST_EMAIL, "password123"
        );
        assertTrue(result, "Registration should succeed with valid inputs");
    }

    @Test
    @Order(2)
    @DisplayName("Duplicate email registration should fail")
    void testRegisterDuplicateEmail() {
        boolean result = service.register(
                "Test User", "0821234567", TEST_EMAIL, "password123"
        );
        assertFalse(result, "Registration should fail for duplicate email");
    }

    @Test
    @Order(3)
    @DisplayName("Invalid phone number should fail registration")
    void testRegisterInvalidPhone() {
        boolean result = service.register(
                "Test User", "123", "other@clinic.com", "password123"
        );
        assertFalse(result, "Registration should fail with invalid phone number");
    }

    @Test
    @Order(4)
    @DisplayName("Short password should fail registration")
    void testRegisterShortPassword() {
        boolean result = service.register(
                "Test User", "0821234567", "new@clinic.com", "abc"
        );
        assertFalse(result, "Registration should fail with password under 6 chars");
    }

    @Test
    @Order(5)
    @DisplayName("Valid login should return patient object")
    void testLoginSuccess() {
        Patient patient = service.login(TEST_EMAIL, "password123");
        assertNotNull(patient, "Login should return a patient object");
        assertEquals(TEST_EMAIL, patient.getEmail());
    }

    @Test
    @Order(6)
    @DisplayName("Wrong password should return null")
    void testLoginWrongPassword() {
        Patient patient = service.login(TEST_EMAIL, "wrongpass");
        assertNull(patient, "Login should fail with wrong password");
    }

    @Test
    @Order(7)
    @DisplayName("Non-existent email should return null")
    void testLoginUnknownEmail() {
        Patient patient = service.login("nobody@clinic.com", "password123");
        assertNull(patient, "Login should fail for unknown email");
    }
}
