package co.edu.uniandes.misw4203.equipo11.vinilos.repositories

import android.util.Log
import co.edu.uniandes.misw4203.equipo11.vinilos.models.Collector
import co.edu.uniandes.misw4203.equipo11.vinilos.models.CollectorWithPerformers
import co.edu.uniandes.misw4203.equipo11.vinilos.models.Performer
import co.edu.uniandes.misw4203.equipo11.vinilos.models.VinilosDB
import co.edu.uniandes.misw4203.equipo11.vinilos.network.NetworkServiceAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface ICollectorRepository {
    fun getCollectors(): Flow<List<Collector>?>
    fun getCollectorsWithPerformers(): Flow<List<CollectorWithPerformers>?>
    suspend fun refresh(): Boolean
}

class CollectorRepository : ICollectorRepository {
    private val adapter = NetworkServiceAdapter()
    private val db = VinilosDB.getInstance()

    override fun getCollectors(): Flow<List<Collector>?> = flow {
        db.collectorDao().getCollectors().collect { collectors ->
            if (collectors.isEmpty()) {
                if(!refresh()) {
                    emit(null)
                }
            } else {
                emit(collectors)
            }
        }
    }

    override fun getCollectorsWithPerformers(): Flow<List<CollectorWithPerformers>?> = flow {
        db.collectorDao().getCollectorsWithPerformers().collect { collectors ->
            if (collectors.isEmpty()) {
                if(!refresh()) {
                    emit(null)
                }
            } else {
                emit(collectors)
            }
        }
    }

    override suspend fun refresh(): Boolean {
        val collectors: List<Collector>?

        try {
            collectors = adapter.getCollectors().first()
        } catch (ex: Exception) {
            Log.e(TAG, "Error loading Collectors: $ex")
            return false
        }

        db.collectorDao().deleteAndInsertCollectors(collectors)
        return true
    }
    companion object {
        private val TAG = CollectorRepository::class.simpleName!!
    }
}