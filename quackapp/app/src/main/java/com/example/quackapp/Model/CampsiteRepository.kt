package com.example.quackapp.Model

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CampsiteRepository(ownerId: String) {
    private val _camps = mutableStateListOf<Campsite>()

    private val db = Firebase.firestore
    private val citiesRef = db.collection("campsites")

    val camps: List<Campsite>
        get() = _camps

    init{
        citiesRef.addSnapshotListener { snapshot, error ->
            if (error != null){
                return@addSnapshotListener
            }

            _camps.clear()

            snapshot?.documents?.forEach{ document ->
                val camp = document.toObject(Campsite::class.java)
                if (camp != null && camp.ownerID == ownerId) {
                    _camps.add(camp)
                }
            }
        }
    }
}