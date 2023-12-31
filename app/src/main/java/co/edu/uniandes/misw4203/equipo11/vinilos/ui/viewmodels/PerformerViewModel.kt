package co.edu.uniandes.misw4203.equipo11.vinilos.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Album
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Performer
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.PerformerType
import co.edu.uniandes.misw4203.equipo11.vinilos.data.repositories.IPerformerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

abstract class PerformerViewModel(
    protected val performerRepository: IPerformerRepository,
    protected val performerId: Int,
    protected val dispatcher : CoroutineDispatcher
) : ViewModel() {
    abstract val performerType: PerformerType

    abstract val performer: SharedFlow<Performer?>

    private val _albums: MutableStateFlow<List<Album>> = MutableStateFlow(emptyList())
    val albums = _albums.asStateFlow().onSubscription { getAlbums() }
    private val getAlbumsStarted: AtomicBoolean = AtomicBoolean(false)

    @Suppress("PropertyName")
    protected val _isRefreshing = MutableStateFlow(true)
    val isRefreshing = _isRefreshing.asStateFlow()

    @Suppress("PropertyName")
    protected val _error = MutableStateFlow<ErrorUiState>(ErrorUiState.NoError)
    val error = _error.asStateFlow()

    private fun getAlbums() {
        if (getAlbumsStarted.getAndSet(true))
            return // Coroutine to get albums was already started, only start once

        viewModelScope.launch(dispatcher) {
            performerRepository.getAlbums(performerId)
                .collect { albums ->
                    _albums.value = albums
                    _isRefreshing.value = false
                }
        }
    }

    fun onErrorShown() {
        _error.value = ErrorUiState.NoError
    }

    abstract fun onRefresh()

    companion object {
        val KEY_PERFORMER_REPOSITORY = object : CreationExtras.Key<IPerformerRepository> {}
        val KEY_PERFORMER_ID = object : CreationExtras.Key<Int> {}
        val KEY_DISPATCHER = object : CreationExtras.Key<CoroutineDispatcher> {}
    }
}
