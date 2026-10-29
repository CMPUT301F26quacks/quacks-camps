package com.example.quacksites.model

import android.net.Uri

class Campsite (
    /*-name, owner, location, price, capacity,
    features, max occupancy, rating,
    optional promotional image, rules,
    cancellation policy, availability,
   , tags,
   class holds all attributes mentioned in entrant and campsite user stories
     */

    var name: String ="",
    var owner: String = "",
    var location: String = "",
    var price: Int,
    var capacity: Int,
    var features: List<String>, // okay..?
    var maxOccupancy: Int,
    var rating: Double, //in case it averages out weird
    var optionalPromoImage: Uri? = null, //https://developer.android.com/reference/android/net/Uri - referenced to the uploaded image and its null since its optional so it can be empty
    var rules: List<String>,
    var cancellationPolicy: String, //only mentioned in the entrant's user story - never set on the campsite owner's side
    var availability: List<String>, //I'm assuming it's a list of all da dates
    var tags: List<String>
)

