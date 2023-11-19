package co.edu.uniandes.misw4203.equipo11.vinilos.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Album
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.toAlbum
import co.edu.uniandes.misw4203.equipo11.vinilos.data.network.models.AlbumJson
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDAO {
    @Query("SELECT * FROM album ORDER BY name COLLATE UNICODE")
    fun getAlbums(): Flow<List<Album>>

    @Query("SELECT a.* FROM PerformerAlbum pa JOIN Album a on pa.albumId = a.id WHERE pa.performerId == :performerId ORDER BY name COLLATE UNICODE")
    fun getAlbumsByPerformerId(performerId: Int): Flow<List<Album>>

    @Insert
    suspend fun insertAlbums(albums: List<Album>)

    @Query("DELETE FROM album")
    suspend fun deleteAlbums()

    @Transaction
    suspend fun deleteAndInsertAlbums(albums: List<AlbumJson>) {
        val mappedAlbums = albums.map { it.toAlbum() }

        deleteAlbums()
        insertAlbums(mappedAlbums)
    }
}
