package com.safewristband.tracker.domain.repository

import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.WristbandData
import com.safewristband.tracker.util.Resource
import kotlinx.coroutines.flow.Flow

interface WristbandRepository {
    fun observeWristbandData(wristbandId: String): Flow<Resource<WristbandData>>
    fun observeConnectionStatus(wristbandId: String): Flow<ConnectionStatus>
}
