import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryiewModel(application : Application) : AndroidViewModel(application){

    private val categoryDao = AppDatabase.getInstance(application).categoryDao()

    val categories: LiveData<List<(Category)>> = categoryDao.getAllCategories()
}