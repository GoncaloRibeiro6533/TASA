import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

sealed interface HomeScreenState {
    data object Idle : HomeScreenState

    data object Logged : HomeScreenState

    data object NotLogged : HomeScreenState
}

class HomePageScreenViewModel(
    initialState: HomeScreenState = HomeScreenState.Idle,
) : ViewModel() {
    var state: HomeScreenState by mutableStateOf<HomeScreenState>(initialState)
        private set
}
