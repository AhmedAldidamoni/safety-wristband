package com.safewristband.tracker.data.repository

import com.safewristband.tracker.data.remote.FirebaseWristbandDataSource
import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.WristbandData
import com.safewristband.tracker.domain.repository.WristbandRepository
import com.safewristband.tracker.util.Constants
import com.safewristband.tracker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WristbandRepositoryImpl @Inject constructor(
    private val remoteDataSource: FirebaseWristbandDataSource
) : WristbandRepository {

    override fun observeWristbandData(wristbandId: String): Flow<Resource<WristbandData>> =
        remoteDataSource.observeWristband(wristbandId)

    override fun observeConnectionStatus(wristbandId: String): Flow<ConnectionStatus> =
        remoteDataSource.observeWristband(wristbandId).map { resource ->
            when (resource) {
                is Resource.Loading -> ConnectionStatus.CONNECTING
                is Resource.Error -> ConnectionStatus.DISCONNECTED
                is Resource.Success -> {
                    val age = System.currentTimeMillis() - resource.data.timestamp
                    if (age > Constants.STALE_CONNECTION_THRESHOLD_MS) {
                        ConnectionStatus.STALE
                    } else {
                        ConnectionStatus.CONNECTED
                    }
                }
            }
        }
}
