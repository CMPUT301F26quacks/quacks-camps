package com.example.quackapp.Model

/**
 * Campsite class containing all info used by Owners and Entrants
 *
 * @property campId unique ID given to distinguish camps
 * @property name name given to the campsite
 * @property location the given location, set by owner
 * @property price cost per night
 * @property capacity maximum amount of people the camp can hold
 * @property features listed amenities and feature the camp has
 * @property rules listed rules the camp has
 * @property cancellationPolicy cancellation policy for bookings of this camp
 * @property tags short description phrases of the camp
 * @property ownerId unique ID of this camp's creator
 * @property longitude longitude of the camp's map location
 * @property latitude latitude of the camp's map location
 * @property rating rating of this camp's quality, given by entrants
 * @property qrCode QR code entrants can scan to be brought to this camp's detail page
 * @property lotteryId unique ID of the lottery this camp is currently taking part of (null if not in lottery)
 * @property availability dates that entrants can book this camp for after winning a lottery
 */
class Campsite (
    val campId: String = "",
    val name: String = "",
    val location: String = "",
    val price: Int = -1,
    val capacity: Int = -1,
    val optionalPromoImage: String = "",
    var features: List<String> = listOf(""),
    var rules: List<String> = listOf(""),
    var cancellationPolicy: String = "",
    var tags: List<String> = listOf(""),
    val ownerId: String = "",
    val longitude: Double = 0.00,
    val latitude: Double = 0.00
    ){
    var rating: Double = 0.00
    var qrCode: String = ""
    var lotteryId: String? = null //holds lottery property - none if by default campsite not in lottery

    //Uses milliseconds
    //For testing: Dec 25th - Dec 26th = 1798156800000, 1798156800000+86399999
    var availability: List<Long> = listOf()
}