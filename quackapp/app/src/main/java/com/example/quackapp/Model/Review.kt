package com.example.quackapp.Model

class Review (
    var rating: Float = 0F,
    var feedback: String="",
    var reviewID: String="",
    //associating review w campsite and an entrant based on crc cards
    var campsiteID: String="",
    var entrantID: String=""

)