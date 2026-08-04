package com.safetywristband.tracker.domain.repository

import com.safetywristband.tracker.domain.model.ConnectionStatus
import com.safetywristband.tracker.domain.model.WristbandData
import com.safetywristband.tracker.util.Resource
import kotlinx.coroutines.flow.Flow

interface WristbandRepository {
    fun observeWristbandData(wristbandId: String): Flow<Resource<WristbandData>>
    fun observeConnectionStatus(wristbandId: String): Flow<ConnectionStatus>
}
