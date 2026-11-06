package com.example.quackapp.Model

import android.R
import com.google.firebase.Timestamp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


//enum so easy to filter, typos get flagged etc.
enum class LotteryStatus { OPEN, CLOSED}
enum class WaitlistStatus {WAITING, SELECTED, NOT_SELECTED}

/*-registration period
-booking period
-list of entrants
-number of lottery winners
-optional capacity of waitlist

-notify winners and non-selected entrants
-proceeding with booking
-automatically draw winners?
*/

class Lottery(
    var lotteryID: String = "",
    var registrationPeriodStart: Timestamp? = null,
    var registrationPeriodEnd: Timestamp? = null,
    var bookingPeriodStart: Timestamp? = null,
    var bookingPeriodEnd: Timestamp? = null,
    var numLotteryWinners: Int = 1,
    var optionalWaitlistCapacity: Int? = null, //since optional we use null - idk if this is the best practice tho
    var waitlist: List<String> = listOf(""), //list of entrants
    var status: LotteryStatus = LotteryStatus.CLOSED,
    var campIds: List<String> = listOf("")
)

//entrants in da waitlist for optimal holding capacity+firebase db considerations
class WaitlistEntry(
    var status: WaitlistStatus = WaitlistStatus.WAITING,
    var entryID: String = "",   //for the firebase db (id)
    var lotteryID: String = "",
    var entrantID: String=""

)