package com.example.quackapp
import com.google.firebase.firestore.FirebaseFirestore

class WaitlistRepository {
    private val db = FirebaseFirestore.getInstance()

    // Save or update a waitlist entry in Firebase
    fun addEntry(entry: WaitlistEntry) {
        db.collection("waitlists").document(entry.entryId).set(entry)
    }

    // Get all waitlist entries for a specific lottery from Firebase
    fun getEntriesForLottery(lotteryId: String, callback: (List<WaitlistEntry>) -> Unit) {
        db.collection("waitlists")
            .whereEqualTo("lotteryId", lotteryId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(WaitlistEntry::class.java)
                callback(list)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}

