package com.safewristband.tracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.model.AlertType

@Entity(tableName = "alert_events")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String,
    val message: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?
) {
    fun toDomain(): AlertEvent = AlertEvent(
        id = id,
        type = AlertType.valueOf(type),
        message = message,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromDomain(alert: AlertEvent): AlertEntity = AlertEntity(
            id = alert.id,
            type = alert.type.name,
            message = alert.message,
            timestamp = alert.timestamp,
            latitude = alert.latitude,
            longitude = alert.longitude
        )
    }
}
