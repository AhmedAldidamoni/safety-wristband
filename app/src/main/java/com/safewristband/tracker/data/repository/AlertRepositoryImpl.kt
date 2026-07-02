package com.safewristband.tracker.data.repository

import com.safewristband.tracker.data.local.AlertDao
import com.safewristband.tracker.data.local.AlertEntity
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.repository.AlertRepository
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
