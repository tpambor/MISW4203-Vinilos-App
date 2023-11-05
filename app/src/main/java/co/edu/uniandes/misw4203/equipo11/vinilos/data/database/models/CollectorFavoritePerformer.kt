package co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models

import androidx.room.Entity

@Entity(primaryKeys = ["collectorId", "performerId"])
data class CollectorFavoritePerformer(
    val collectorId: Int,
    val performerId: Int
)
