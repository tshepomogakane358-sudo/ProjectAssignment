package com.smartqueue.test;


import com.smartqueue.util.PasswordUtil;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {
    @Test
    @DisplayName("Hashed password should not equal plain text")
    void testHashIsNotPlainText() {
        String plain  = "mypassword";
        String hashed = PasswordUtil.hashPassword(plain);
        assertNotEquals(plain, hashed, "Hash should differ from plain text");
    }

    @Test
    @DisplayName("Correct password should verify successfully")
    void testCorrectPasswordVerifies() {
        String plain  = "mypassword";
        String hashed = PasswordUtil.hashPassword(plain);
        assertTrue(PasswordUtil.verifyPassword(plain, hashed),
                "Correct password should pass verification");
    }

    @Test
    @DisplayName("Wrong password should fail verification")
    void testWrongPasswordFails() {
        String hashed = PasswordUtil.hashPassword("mypassword");
        assertFalse(PasswordUtil.verifyPassword("wrongpassword", hashed),
                "Wrong password should fail verification");
    }

    @Test
    @DisplayName("Two hashes of same password should differ")
    void testHashesAreDifferentEachTime() {
        String hash1 = PasswordUtil.hashPassword("mypassword");
        String hash2 = PasswordUtil.hashPassword("mypassword");
        assertNotEquals(hash1, hash2, "BCrypt should produce different salts each time");
    }

}
