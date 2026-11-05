package com.example.quackapp


class LotteryDrawer {
    private val WaitlistRepository = WaitlistRepository()

    fun runDrawAndSave(lotteryId: String, maxWinners: Int, onComplete: (List<WaitlistEntry>) -> Unit) {
        WaitlistRepository.getEntriesForLottery(lotteryId) { entries ->
            val waitlistedEntrants = entries.filter { it.status == WaitlistStatus.WAITING }
            val shuffled = waitlistedEntrants.shuffled()

            for (i in shuffled.indices) {
                if (i < maxWinners) {
                    shuffled[i].status = WaitlistStatus.SELECTED
                } else {
                    shuffled[i].status = WaitlistStatus.NOT_SELECTED
                }
                    // Push updated status back to Firebase
                WaitlistRepository.addEntry(shuffled[i])
            }
            onComplete(shuffled)
        }
    }
    // get notification when they win the lottery
    fun receiveNotifications(){}
    /* if selected = won
    else
        didnt win the lottery
     */
}