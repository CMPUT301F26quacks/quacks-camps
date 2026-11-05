package com.example.quackapp.Model


class Campsite (
    /**-name, owner, location, price, capacity,
    features, max occupancy, rating,
    optional promotional image, rules,
    cancellation policy, availability,
    tags,
    class holds all attributes mentioned in entrant and campsite user stories*/
    val campId: String = "",
    val name: String ="",
    val location: String = "",
    val price: Int = -1,
    val capacity: Int = -1,
    val optionalPromoImage: String = "",
    var features: List<String> = listOf(""),
    var rules: List<String> = listOf(""),
    var cancellationPolicy: String = "",
    var tags: List<String> = listOf(""),
    val ownerId: String = ""
){
    var rating: Double = 0.00
    var qrCode: String = ""
    var lottery: Lottery? = null //holds lottery property - none if by default campsite not in lottery
//    var availability: List<String> = [] //I'm assuming it's a list of all da dates
}