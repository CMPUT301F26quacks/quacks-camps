package com.example.quackapp.Model

/**
 * Reviews that entrants can leave for a campsite
 * @property rating the rating out of 5 given to the campsite
 * @property feedback the content of the review. Whatever the entrant has to say
 * @property reviewID unique ID used to distinguish reviews
 * @property campsiteID the ID of the campsite the review is for
 * @property entrantID the ID of the entrant who made the review
 */
class Review (
    var rating: Float = 0F,
    var feedback: String="",
    var reviewID: String="",
    //associating review w campsite and an entrant based on crc cards
    var campsiteID: String="",
    var entrantID: String=""

)