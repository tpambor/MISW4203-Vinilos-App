package co.edu.uniandes.misw4203.equipo11.vinilos.data.repositories

import android.util.Log
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.VinilosDB
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Album
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Comment
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Performer
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Track
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.toComment
import co.edu.uniandes.misw4203.equipo11.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.misw4203.equipo11.vinilos.data.network.models.AlbumJsonRequest
import co.edu.uniandes.misw4203.equipo11.vinilos.data.network.models.AlbumJsonResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
interface IAlbumRepository {
    fun getAlbums(): Flow<Result<List<Album>>>
    suspend fun refresh()
    fun getAlbum(albumId: Int): Flow<Album?>
    fun getPerformers(albumId: Int): Flow<List<Performer>>
    fun getComments(albumId: Int): Flow<List<Comment>>
    fun getTracks(albumId: Int): Flow<List<Track>>
    suspend fun refreshAlbum(albumId: Int)
    suspend fun insertAlbum(album: AlbumJsonRequest)
    suspend fun addComment(albumId: Int, collectorId: Int, rating: Int, comment: String)
}

class AlbumRepository : IAlbumRepository {
    private val adapter = NetworkServiceAdapter()
    private val db = VinilosDB.getInstance()

    override fun getAlbums(): Flow<Result<List<Album>>> = flow {
        var isFirst = true

        db.albumDao().getAlbums().collect { albums ->
            if (!isFirst)
                emit(Result.success(albums))

            // Handle first list returned differently
            //
            // If the first list is empty, there is no data in the database.
            // This is mostly likely due to never have loaded data from the API,
            // therefore call refresh() in this case to load the data from the API.
            isFirst = true
            if(albums.isNotEmpty()) {
                emit(Result.success(albums))
            }
            else {
                try {
                    refresh()
                } catch (ex: Exception) {
                    Log.e(TAG, "Error loading albums: $ex")
                    emit(Result.failure(ex))
                }
            }
        }
    }

    override fun getAlbum(albumId: Int): Flow<Album?> = flow {
        db.albumDao().getAlbumById(albumId).collect { album ->
            emit(album)
        }
    }
    override suspend fun insertAlbum(album: AlbumJsonRequest)
    {
        try {
            val remoteAlbumJson = adapter.insertAlbum(album).first()
            val remoteAlbum = convertJsonRequestToAlbum(remoteAlbumJson)

             db.albumDao().insertAlbum(remoteAlbum)
        } catch (e: Exception) {
            println("Error inserting album: ${e.message}")
            throw e
        }
    }

    private fun convertJsonRequestToAlbum(jsonRequest: AlbumJsonResponse): Album {
        val offsetDateTime = OffsetDateTime.parse(jsonRequest.releaseDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val instant = offsetDateTime.toInstant()
        return Album(
            id = jsonRequest.id,
            name = jsonRequest.name,
            cover = jsonRequest.cover,
            releaseDate = instant,
            description = jsonRequest.description,
            genre = jsonRequest.genre,
            recordLabel = jsonRequest.recordLabel
        )
    }



    override fun getPerformers(albumId: Int): Flow<List<Performer>> = flow {
        db.albumDao().getPerformersByAlbumId(albumId).collect { performers ->
            emit(performers)
        }
    }

    override fun getComments(albumId: Int): Flow<List<Comment>> = flow {
        db.albumDao().getCommentsByAlbumId(albumId).collect { comments ->
            emit(comments)
        }
    }

    override fun getTracks(albumId: Int): Flow<List<Track>> = flow {
        db.albumDao().getTracksByAlbumId(albumId).collect { tracks ->
            emit(tracks)
        }
    }

    override suspend fun refresh() {
        db.albumDao().deleteAndInsertAlbums(
            adapter.getAlbums().first()
        )
    }

    override suspend fun refreshAlbum(albumId: Int) {
        db.albumDao().deleteAndInsertAlbums(
            listOf(adapter.getAlbum(albumId).first()),
            deleteAll = false
        )
    }

    override suspend fun addComment(albumId: Int, collectorId: Int, rating: Int, comment: String) {
        val newComment = adapter.addCommentToAlbum(albumId, collectorId, rating, comment).first()
        db.albumDao().insertComments(listOf(newComment.toComment(albumId)))
    }

    companion object {
        private val TAG = AlbumRepository::class.simpleName!!
    }
}
