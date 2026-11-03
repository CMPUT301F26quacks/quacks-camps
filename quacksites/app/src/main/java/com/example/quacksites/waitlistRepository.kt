package com.example.quacksites

import com.google.firebase.firestore.FirebaseFirestore

class waitlistRepository {
    private val db = FirebaseFirestore.getInstance()

    // Save or update a waitlist entry in Firebase
    fun addEntry(entry: waitListEntry) {
        db.collection("waitlists").document(entry.entryId).set(entry)
    }

    // Get all waitlist entries for a specific lottery from Firebase
    fun getEntriesForLottery(lotteryId: String, callback: (List<waitListEntry>) -> Unit) {
        db.collection("waitlists")
            .whereEqualTo("lotteryId", lotteryId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(waitListEntry::class.java)
                callback(list)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}

