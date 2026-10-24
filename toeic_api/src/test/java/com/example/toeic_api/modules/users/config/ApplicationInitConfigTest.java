package com.example.toeic_api.modules.users.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.toeic_api.modules.users.model.User;
import com.example.toeic_api.modules.users.model.UserRole;
import com.example.toeic_api.modules.users.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ApplicationInitConfigTest {
    @Test
    void localBootstrapCreatesAdminWithHashedConfiguredPassword() {
        var users = mock(UserRepository.class);
        var encoder = new BCryptPasswordEncoder();
        var config = new ApplicationInitConfig(users, encoder);
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "bootstrapEnabled", true);
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "adminUsername", "admin");
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "adminPassword", "admin123");
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "adminEmail", "admin.local@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "adminFullName", "Local Admin");
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "minimumPasswordLength", 8);
        config.run(null);
        var saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals("admin", saved.getValue().getUsername());
        assertEquals(UserRole.ADMIN, saved.getValue().getRole());
        assertTrue(encoder.matches("admin123", saved.getValue().getPassword()));
        assertNotEquals("admin123", saved.getValue().getPassword());
        // Restarting must not overwrite an existing account's password.
        when(users.existsByUsername("admin")).thenReturn(true);
        config.run(null);
        verify(users, times(1)).save(any());
    }

    @Test
    void defaultPolicyStillRequiresTwelveCharacters() {
        var users = mock(UserRepository.class);
        var config = new ApplicationInitConfig(users, new BCryptPasswordEncoder());
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "bootstrapEnabled", true);
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "adminUsername", "admin");
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "adminPassword", "admin123");
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "adminEmail", "admin.local@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "minimumPasswordLength", 12);
        assertThrows(IllegalStateException.class, () -> config.run(null));
        verifyNoInteractions(users);
    }
}
