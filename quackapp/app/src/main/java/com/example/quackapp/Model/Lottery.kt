package com.example.quackapp.Model

import android.R
import com.google.firebase.Timestamp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


//enum so easy to filter, typos get flagged etc.
/**
 * Denotes the status of a lottery
 * Can be OPEN or CLOSED
 */
enum class LotteryStatus { OPEN, CLOSED}
/**
 * Denotes the status of an entrant in lottery's waitlist
 * Can be WAITING for the booking period, SELECTED to book the camp, or NOT_SELECTED if they lost the lottery
 */
enum class WaitlistStatus {WAITING, SELECTED, NOT_SELECTED}

/**
 * Lottery class
 *
 * Contains all information necessary for Owner and Entrant interactions in regard to booking campsites
 *
 * @property lotteryID unique ID used to distinguish lotteries
 * @property registrationPeriodStart timestamp in milliseconds for when the registration period starts
 * @property registrationPeriodEnd timestamp in milliseconds for when the registration period ends
 * @property bookingPeriodStart timestamp in milliseconds for when the booking period starts
 * @property bookingPeriodEnd timestamp in milliseconds for when the booking period ends
 * @property numLotteryWinners number of Entrants who are selected to book
 * @property optionalWaitlistCapacity max capacity of Entrants in the waitlist
 * @property waitlist list of Entrant's user IDs who are interested in booking the camps in this lottery
 * @property status whether this lottery is open (active) or closed (done/completed)
 * @property campIds unique IDs of the camps involved in this lottery
 */
class Lottery(
    var lotteryID: String = "",
    var registrationPeriodStart: Long? = null,
    var registrationPeriodEnd: Long? = null,
    var bookingPeriodStart: Long? = null,
    var bookingPeriodEnd: Long? = null,
    var numLotteryWinners: Int = 1,
    var optionalWaitlistCapacity: Int? = null, //since optional we use null - idk if this is the best practice tho
    var waitlist: List<String> = listOf(), //list of entrants
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