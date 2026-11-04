package com.example.quackapp.Model


class Campsite (
    /**-name, owner, location, price, capacity,
    features, max occupancy, rating,
    optional promotional image, rules,
    cancellation policy, availability,
    tags,
    class holds all attributes mentioned in entrant and campsite user stories*/
    val id: String = "",
    val name: String ="",
    val location: String = "",
    val price: Int = 0,
    val capacity: Int = 0,
    val optionalPromoImage: String = "", //https://developer.android.com/reference/android/net/Uri - referenced to the uploaded image and its null since its optional so it can be empty

    var features: List<String> = emptyList(),
    var rules: List<String> = emptyList(),
    var cancellationPolicy: String = "", //only mentioned in the entrant's user story - never set on the campsite owner's side
    var tags: List<String> = emptyList()
//    val owner: String = "",
){
    var rating: Double = 0.00
    var qrCode: String = ""
    var lottery: Lottery? = null //holds lottery property - none if by default campsite not in lottery
//    var availability: List<String> = [] //I'm assuming it's a list of all da dates
}