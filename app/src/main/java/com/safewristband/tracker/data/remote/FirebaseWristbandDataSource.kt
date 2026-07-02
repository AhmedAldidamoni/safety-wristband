package com.safewristband.tracker.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.safewristband.tracker.data.remote.dto.WristbandDto
import com.safewristband.tracker.domain.model.WristbandData
import com.safewristband.tracker.util.Constants
import com.safewristband.tracker.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseWristbandDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    fun observeWristband(wristbandId: String): Flow<Resource<WristbandData>> = callbackFlow {
        val ref = database.getReference(Constants.FIREBASE_WRISTBANDS_NODE).child(wristbandId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(Resource.Error("No data found for this wristband"))
                    return
                }
                val dto = snapshot.getValue(WristbandDto::class.java)
                if (dto != null) {
                    trySend(Resource.Success(dto.toDomain(wristbandId)))
                } else {
                    trySend(Resource.Error("Malformed wristband data"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message, error.toException()))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
