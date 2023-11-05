package co.edu.uniandes.misw4203.equipo11.vinilos.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniandes.misw4203.equipo11.vinilos.R
import co.edu.uniandes.misw4203.equipo11.vinilos.data.database.models.Performer
import co.edu.uniandes.misw4203.equipo11.vinilos.data.datastore.models.UserType
import co.edu.uniandes.misw4203.equipo11.vinilos.data.repositories.IPerformerRepository
import co.edu.uniandes.misw4203.equipo11.vinilos.data.repositories.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class PerformerListViewModel(
    private val performerRepository: IPerformerRepository,
    private val userRepository: IUserRepository
) : ViewModel() {
    private val _musicians: MutableStateFlow<List<Performer>> = MutableStateFlow(emptyList())
    val musicians = _musicians.asStateFlow().onSubscription { getMusicians() }
    private val getMusiciansStarted: AtomicBoolean = AtomicBoolean(false)

    private val _bands: MutableStateFlow<List<Performer>> = MutableStateFlow(emptyList())
    val bands = _bands.asStateFlow().onSubscription { getBands() }
    private val getBandsStarted: AtomicBoolean = AtomicBoolean(false)

    private val _favoritePerformers = MutableStateFlow<Set<Int>>(emptySet())
    val favoritePerformers = _favoritePerformers.asStateFlow().onSubscription { getFavoritePerformers() }
    private val getFavoritePerformersStarted: AtomicBoolean = AtomicBoolean(false)

    private val _isRefreshing = MutableStateFlow(true)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<ErrorUiState>(ErrorUiState.NoError)
    val error = _error.asStateFlow()

    private fun getMusicians() {
        if (getMusiciansStarted.getAndSet(true))
            return // Coroutine to get musicians was already started, only start once

        viewModelScope.launch {
            performerRepository.getMusicians()
                .collect { musicians ->
                    if (musicians == null) {
                        _error.value = ErrorUiState.Error(R.string.network_error)
                    } else {
                        _musicians.value = musicians
                        _error.value = ErrorUiState.NoError
                    }
                    _isRefreshing.value = false
                }
        }
    }

    private fun getBands() {
        if (getBandsStarted.getAndSet(true))
            return // Coroutine to get bands was already started, only start once

        viewModelScope.launch {
            performerRepository.getBands()
                .collect { bands ->
                    if (bands == null) {
                        _error.value = ErrorUiState.Error(R.string.network_error)
                    } else {
                        _bands.value = bands
                        _error.value = ErrorUiState.NoError
                    }
                    _isRefreshing.value = false
                }
        }
    }

    private fun getFavoritePerformers() {
        if (getFavoritePerformersStarted.getAndSet(true))
            return // Coroutine to get favorite performers was already started, only start once

        viewModelScope.launch {
            // No need to listen for changes of user as userId is static over the life cycle of this view model
            val user = userRepository.getUser().first()

            // Visitors do not have favorite performers
            if ((user == null) || (user.type == UserType.Visitor))
                return@launch

            performerRepository.getFavoritePerformers(user.id).collect { performers ->
                _favoritePerformers.value = performers.map { it.id }.toSet()
            }
        }
    }

    fun onRefreshMusicians() {
        _isRefreshing.value = true
        _error.value = ErrorUiState.NoError

        viewModelScope.launch {
            if (!performerRepository.refreshMusicians()) {
                _isRefreshing.value = false
                _error.value = ErrorUiState.Error(R.string.network_error)
            }
        }
    }

    fun onRefreshBands() {
        _isRefreshing.value = true
        _error.value = ErrorUiState.NoError

        viewModelScope.launch {
            if (!performerRepository.refreshBands()) {
                _isRefreshing.value = false
                _error.value = ErrorUiState.Error(R.string.network_error)
            }
        }
    }

    // ViewModel factory edit
    companion object {
        val KEY_PERFORMER_REPOSITORY = object : CreationExtras.Key<IPerformerRepository> {}
        val KEY_USER_REPOSITORY = object : CreationExtras.Key<IUserRepository> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PerformerListViewModel(
                    performerRepository = requireNotNull(this[KEY_PERFORMER_REPOSITORY]),
                    userRepository = requireNotNull(this[KEY_USER_REPOSITORY])
                )
            }
        }
    }
}