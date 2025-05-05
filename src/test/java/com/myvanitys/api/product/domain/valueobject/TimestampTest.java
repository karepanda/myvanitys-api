package com.myvanitys.api.product.domain.valueobject;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TimestampTest {

    @Test
    void constructor_WithNullValue_ShouldUseCurrentInstant() {
        Timestamp timestamp = new Timestamp(null);
        assertNotNull(timestamp.value());
    }

    @Test
    void constructor_WithNonNullValue_ShouldUseProvidedValue() {
        Instant instant = Instant.now();
        Timestamp timestamp = new Timestamp(instant);
        assertEquals(instant, timestamp.value());
    }

    @Test
    void now_ShouldCreateTimestampWithCurrentInstant() {
        Timestamp timestamp = Timestamp.now();
        assertNotNull(timestamp.value());
    }

    @Test
    void of_WithNullInstant_ShouldUseCurrentInstant() {
        Timestamp timestamp = Timestamp.of(null);
        assertNotNull(timestamp.value());
    }

    @Test
    void of_WithNonNullInstant_ShouldUseProvidedInstant() {
        Instant instant = Instant.now();
        Timestamp timestamp = Timestamp.of(instant);
        assertEquals(instant, timestamp.value());
    }

    @Test
    void asInstant_ShouldReturnInstant() {
        Instant instant = Instant.now();
        Timestamp timestamp = new Timestamp(instant);
        assertEquals(instant, timestamp.asInstant());
    }

    @Test
    void isBefore_WithLaterTimestamp_ShouldReturnTrue() {
        Instant anterior = Instant.now();
        Instant posterior = anterior.plusSeconds(1);
        Timestamp timestampAnterior = new Timestamp(anterior);
        Timestamp timestampPosterior = new Timestamp(posterior);
        
        assertTrue(timestampAnterior.isBefore(timestampPosterior));
        assertFalse(timestampPosterior.isBefore(timestampAnterior));
    }

    @Test
    void isAfter_WithEarlierTimestamp_ShouldReturnTrue() {
        Instant anterior = Instant.now();
        Instant posterior = anterior.plusSeconds(1);
        Timestamp timestampAnterior = new Timestamp(anterior);
        Timestamp timestampPosterior = new Timestamp(posterior);
        
        assertTrue(timestampPosterior.isAfter(timestampAnterior));
        assertFalse(timestampAnterior.isAfter(timestampPosterior));
    }

    @Test
    void toString_ShouldReturnInstantRepresentation() {
        Instant instant = Instant.now();
        Timestamp timestamp = new Timestamp(instant);
        assertEquals(instant.toString(), timestamp.toString());
    }
}