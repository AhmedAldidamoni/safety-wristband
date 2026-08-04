package com.safetywristband.tracker.data.repository

import com.safetywristband.tracker.data.local.AlertDao
import com.safetywristband.tracker.data.local.AlertEntity
import com.safetywristband.tracker.domain.model.AlertEvent
import com.safetywristband.tracker.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val alertDao: AlertDao
) : AlertRepository {

    override fun observeAlerts(): Flow<List<AlertEvent>> =
        alertDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addAlert(alert: AlertEvent) {
        alertDao.insert(AlertEntity.fromDomain(alert))
    }

    override suspend fun clearHistory() {
        alertDao.clearAll()
    }
}
