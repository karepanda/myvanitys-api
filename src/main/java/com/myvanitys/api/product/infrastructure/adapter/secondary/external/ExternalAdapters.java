package com.myvanitys.api.product.infrastructure.adapter.secondary.external;

/**
 * This is a placeholder interface for the external adapters package.
 *
 * In hexagonal architecture, secondary adapters (also called "driven adapters" or "output adapters") implement the secondary ports defined
 * in the domain to connect with external systems.
 *
 * <p>Examples of adapters that belong in this package:</p>
 * <ul>
 *   <li>External API clients</li>
 *   <li>Messaging service adapters (Kafka, RabbitMQ, etc.)</li>
 *   <li>Cloud service clients</li>
 *   <li>Notification service adapters (Email, SMS, etc.)</li>
 *   <li>External storage service clients</li>
 * </ul>
 *
 * <p>Each adapter should implement a secondary port defined in the domain.
 * The application will communicate with these external services through the secondary ports,
 * without knowing the specific implementation details of each adapter.</p>
 *
 * @see com.myvanitys.api.product.domain.port.secondary
 */
public interface ExternalAdapters {
  // This is just a marker interface to document the purpose of this package
  // Actual adapter implementations will be added as separate classes in this package
}