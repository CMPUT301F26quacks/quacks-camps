package com.example.quackapp


class LotteryDrawer {
    private val WaitlistRepository = WaitlistRepository()

    fun runDrawAndSave(lotteryId: String, maxWinners: Int, onComplete: (List<waitListEntry>) -> Unit) {
        WaitlistRepository.getEntriesForLottery(lotteryId) { entries ->
            val waitlistedEntrants = entries.filter { it.status == waitListStatus.WAITING }
            val shuffled = waitlistedEntrants.shuffled()

            for (i in shuffled.indices) {
                if (i < maxWinners) {
                    shuffled[i].status = waitListStatus.SELECTED
                } else {
                    shuffled[i].status = waitListStatus.NOT_SELECTED
                }
                    // Push updated status back to Firebase
                WaitlistRepository.addEntry(shuffled[i])
            }
            onComplete(shuffled)
        }
    }
}