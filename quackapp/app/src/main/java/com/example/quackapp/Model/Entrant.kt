package com.example.quackapp.Model

/**
 * Entrant actor class in the campsite lottery and reservation system.
 * Entrants declare interest in lotteries, receive win/loss notifications,
 * review available dates, accept/decline offers, and manage active reservations.
 *
 * @param deviceId Unique hardware or application identifier for passwordless authentication.
 * @param name Full name of the entrant.
 * @param email Contact email address.
 * @param phoneNumber Contact telephone number.
 * @param notificationsEnabled Whether the entrant opts into push/in-app notifications.
 */
class Entrant(
    deviceId: String,
    name: String = "",
    email: String = "",
    phoneNumber: String = "",
    notificationsEnabled: Boolean = true
) : User(deviceId, name, email, phoneNumber, notificationsEnabled) {

    /**
     * Retrieves all active, confirmed reservations for this entrant.
     * US 01.06.01: As an entrant, I want to view a list of my active reservations.
     *
     * @param repository [ReservationRepository] instance to query from.
     * @return List of active [Reservation] objects held by this entrant.
     */
    fun getActiveReservations(repository: ReservationRepository = ReservationRepository.getInstance()): List<Reservation> {
        return repository.getActiveReservations(this.deviceId)
    }

    /**
     * Retrieves pending lottery offers awaiting a booking decision.
     * US 01.04.01: As an entrant, if I win the lottery, I want to see available dates that I can reserve.
     *
     * @param repository [ReservationRepository] instance to query from.
     * @return List of pending [LotteryOffer] objects.
     */
    fun getPendingOffers(repository: ReservationRepository = ReservationRepository.getInstance()): List<LotteryOffer> {
        return repository.getPendingOffers(this.deviceId)
    }

    /**
     * Allows users to opt in or out of receiving notifications (on by default).
     * US 01.10.01: As an entrant, I want to opt out of receiving notifications.
     */

    fun toggleNotificationPreference() {
        notificationsEnabled = !notificationsEnabled
    }
    
}
