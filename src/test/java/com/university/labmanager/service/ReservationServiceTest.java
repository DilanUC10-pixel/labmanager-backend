package com.university.labmanager.service;

import com.university.labmanager.model.Laptop;
import com.university.labmanager.model.enums.LaptopStatus;
import com.university.labmanager.repository.BlockedDateRepository;
import com.university.labmanager.repository.LaptopRepository;
import com.university.labmanager.repository.ReservationRepository;
import com.university.labmanager.repository.SoftwareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private LaptopRepository laptopRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SoftwareRepository softwareRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReservationService reservationService;

    private Laptop sampleLaptop;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;

    @BeforeEach
    void setUp() {
        sampleLaptop = new Laptop();
        sampleLaptop.setId(1L);
        sampleLaptop.setSerialNumber("SN-12345");
        sampleLaptop.setModel("Dell XPS");
        sampleLaptop.setStatus(LaptopStatus.AVAILABLE);

        // A Valid Mon-Fri time between 7AM and 9PM
        // Using a fixed future date (Assuming 2026-03-02 is a Monday)
        validStartTime = LocalDateTime.of(2026, 3, 2, 10, 0);
        validEndTime = LocalDateTime.of(2026, 3, 2, 12, 0);
    }

    @Test
    void testFindSmartOptions_Success() {
        // Arrange
        when(blockedDateRepository.existsByDate(validStartTime.toLocalDate())).thenReturn(false);
        when(laptopRepository.findAvailableLaptopsWithSoftware(any(), anyBoolean(), anyLong(), any(), any()))
                .thenReturn(Arrays.asList(sampleLaptop));

        // Act
        List<Laptop> options = reservationService.findSmartOptions("Java,Python", validStartTime, validEndTime);

        // Assert
        assertNotNull(options);
        assertEquals(1, options.size());
        assertEquals("Dell XPS", options.get(0).getModel());
        verify(laptopRepository, times(1)).findAvailableLaptopsWithSoftware(any(), anyBoolean(), anyLong(), any(), any());
    }

    @Test
    void testFindSmartOptions_InvalidTime_ThrowsException() {
        // Arrange
        LocalDateTime invalidStart = LocalDateTime.of(2026, 3, 2, 6, 0); // 6 AM is before 7 AM

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.findSmartOptions("Any", invalidStart, validEndTime);
        });

        assertTrue(exception.getMessage().contains("between 7:00 AM and 9:00 PM"));
    }

    @Test
    void testFindSmartOptions_Weekend_ThrowsException() {
        // Arrange
        // Assume 2026-03-07 is a Saturday
        LocalDateTime weekendStart = LocalDateTime.of(2026, 3, 7, 10, 0);
        LocalDateTime weekendEnd = LocalDateTime.of(2026, 3, 7, 12, 0);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.findSmartOptions("Any", weekendStart, weekendEnd);
        });

        assertTrue(exception.getMessage().contains("Monday to Friday"));
    }

    @Test
    void testDeleteLaptop() {
        // Arrange
        doNothing().when(laptopRepository).deleteById(1L);

        // Act
        reservationService.deleteLaptop(1L);

        // Assert
        verify(laptopRepository, times(1)).deleteById(1L);
    }
}
