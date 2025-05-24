package com.example.prog_poe_2025

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val context = getApplication<Application>().applicationContext

    /** Add a single category under the user */
    fun insertCategory(userId: String, category: Category) {
        db.collection("categories")
            .document(userId)
            .collection("userCategories")
            .add(category)
            .addOnSuccessListener {
                Toast.makeText(context, "Category added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add category.", Toast.LENGTH_SHORT).show()
            }
    }

    /** Insert multiple categories at once */
    fun insertAllCategories(userId: String, categories: List<Category>) {
        val batch = db.batch()
        val collectionRef = db.collection("categories")
            .document(userId)
            .collection("userCategories")

        for (category in categories) {
            val newDoc = collectionRef.document()
            batch.set(newDoc, category)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "All categories added.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add categories.", Toast.LENGTH_SHORT).show()
            }
    }

    /** Delete all categories for a user */
    suspend fun deleteAllCategories(userId: String) {
        val snapshot = db.collection("categories")
            .document(userId)
            .collection("userCategories")
            .get()
            .await()

        val batch = db.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /** Insert a new budget under the user and return its document ID */
    suspend fun insertBudget(userId: String, budget: Budget): String {
        val docRef = db.collection("budgets").document(userId)
        docRef.set(budget).await()
        return docRef.id
    }



    /** Optional: Fetch categories (return as callback or suspend function) */
    suspend fun getAllCategories(userId: String): List<Category> {
        val snapshot = db.collection("categories")
            .document(userId)
            .collection("userCategories")
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
    }
}

//Medium(2023)


/*References List
[1] Svaghasiya, 2023. Using ViewModel in Android With Kotlin, 18 September 2023. [Online]. Available at:
https://medium.com/@ssvaghasiya61/using-viewmodel-in-android-with-kotlin-16ca735c644f [Accessed 25 April 2025].
*
* */
