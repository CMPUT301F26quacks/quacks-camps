package com.example.quackapp
import com.google.firebase.firestore.FirebaseFirestore

class ListingRepository {
    private val db = FirebaseFirestore.getInstance()

    //adds a listing, checks if valid using the restrictions on lotteryListing
    fun addListing(listing: LotteryListing){
        db.collection("listings").document(listing.lotteryId).set(listing)
    }

    // gets all listings into a list currently saved in memory
    fun getAllListings(callback: (List<LotteryListing>) -> Unit) {
        db.collection("listings").get()
            .addOnSuccessListener { snapshot ->
            callback(snapshot.toObjects(LotteryListing::class.java))
        }
    }
    //searches through the stored list using the Id
    fun getListingThroughId(lotteryId: String, callback: (LotteryListing?) -> Unit) {
        db.collection("listings").document(lotteryId).get()
            .addOnSuccessListener { document ->
                val listing = document.toObject(LotteryListing::class.java)
            }
            .addOnFailureListener { callback(null) }

    }

    //searches through the stored list using a specific Owner Name
    fun getListingsByOwner(ownerId: String, callback: (List<LotteryListing>) -> Unit) {
        db.collection("listings").whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.toObjects(LotteryListing::class.java))
            }
    }

    // searches for the listing and removes it from the mutable list
    fun removeListing(lotteryId: String) {
        db.collection("listings").document(lotteryId).delete()
    }


    // update the listing

    // if winner gets to pick for the dates

}