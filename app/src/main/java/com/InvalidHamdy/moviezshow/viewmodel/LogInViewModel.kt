import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.InvalidHamdy.moviezshow.data.repository.AuthRepository
import com.InvalidHamdy.moviezshow.data.repository.GoogleSignInState
import com.InvalidHamdy.moviezshow.data.repository.LoginState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LogInViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _googleSignInState = MutableLiveData<GoogleSignInState>()
    val googleSignInState: LiveData<GoogleSignInState> = _googleSignInState

    fun signInWithEmail(email: String, password: String) {
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState.Error("Please fill all fields")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(result.getOrNull()!!)
            } else {
                LoginState.Error("Authentication failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _googleSignInState.value = GoogleSignInState.Loading

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            _googleSignInState.value = if (result.isSuccess) {
                GoogleSignInState.Success(result.getOrNull()!!)
            } else {
                GoogleSignInState.Error("Google Sign-In failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun checkCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}