package com.ecclesiaflow.communication.business.domain.email;

/**
 * Email priority enumeration for queue processing.
 * <p>
 * Determines the order in which emails are processed from the queue.
 * </p>
 *
 * <p><strong>Processing Order:</strong></p>
 * <ol>
 *   <li><strong>HIGH</strong> - Transactional emails (confirmations, password resets)</li>
 *   <li><strong>NORMAL</strong> - Regular emails (notifications, updates)</li>
 *   <li><strong>LOW</strong> - Bulk/marketing emails (newsletters, announcements)</li>
 * </ol>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public enum EmailPriority {
    /**
     * High priority - Transactional emails requiring immediate delivery.
     * <p>
     * Examples: Email confirmations, password resets, account alerts.
     * </p>
     */
    HIGH,

    /**
     * Normal priority - Standard emails with regular processing.
     * <p>
     * Examples: Notifications, updates, reminders.
     * </p>
     */
    NORMAL,

    /**
     * Low priority - Bulk emails that can be delayed.
     * <p>
     * Examples: Newsletters, marketing campaigns, announcements.
     * </p>
     */
    LOW
}
