package com.example.aiexpensetracker.data.repository

import com.example.aiexpensetracker.data.local.db.dao.CategoryDao
import com.example.aiexpensetracker.data.local.db.entities.CategoryEntity
import com.example.aiexpensetracker.di.DispatcherModule.IoDispatcher
import com.example.aiexpensetracker.domain.model.category.Category
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val gson: Gson,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {

    private var isInitialized = false

    override suspend fun getCategories(): List<Category> = withContext(ioDispatcher) {
        try {
            if (!isInitialized) {
                initializeDefaultCategories()
                isInitialized = true
            }

            categoryDao.getAllCategories().map { entity ->
                Category(
                    name = entity.name,
                    keywords = gson.fromJson(entity.keywords, Array<String>::class.java).toList(),
                    isDefault = entity.isDefault
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting categories")
            getDefaultCategoriesList() // Fallback to default categories
        }
    }

    override suspend fun addCategory(category: Category) = withContext(ioDispatcher) {
        try {
            val entity = CategoryEntity(
                name = category.name,
                keywords = gson.toJson(category.keywords),
                isDefault = category.isDefault
            )
            categoryDao.insertCategory(entity)
            Timber.d("Added category: ${category.name}")
        } catch (e: Exception) {
            Timber.e(e, "Error adding category: ${category.name}")
            throw e
        }
    }

    override suspend fun deleteCategory(categoryName: String) = withContext(ioDispatcher) {
        try {
            val categories = categoryDao.getAllCategories()
            val categoryToDelete = categories.find {
                it.name.equals(categoryName, ignoreCase = true) && !it.isDefault
            }

            if (categoryToDelete != null) {
                categoryDao.deleteCategory(categoryToDelete)
                Timber.d("Deleted category: $categoryName")
            } else {
                Timber.w("Cannot delete default category or category not found: $categoryName")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting category: $categoryName")
            throw e
        }
    }

    override suspend fun initializeDefaultCategories() = withContext(ioDispatcher) {
        try {
            val existingCategories = categoryDao.getAllCategories()

            if (existingCategories.isEmpty()) {
                val defaultCategories = getDefaultCategoriesList()
                defaultCategories.forEach { category ->
                    addCategory(category)
                }
                Timber.d("Initialized ${defaultCategories.size} default categories")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing default categories")
        }
    }

    // Additional utility methods
    override suspend fun updateCategoryKeywords(categoryName: String, newKeywords: List<String>) =
        withContext(ioDispatcher) {
            try {
                val categories = categoryDao.getAllCategories()
                val categoryToUpdate = categories.find {
                    it.name.equals(categoryName, ignoreCase = true)
                }

                if (categoryToUpdate != null) {
                    val updatedEntity = categoryToUpdate.copy(
                        keywords = gson.toJson(newKeywords)
                    )
                    categoryDao.insertCategory(updatedEntity) // Using REPLACE strategy
                    Timber.d("Updated keywords for category: $categoryName")
                } else {
                    throw IllegalArgumentException("Category not found: $categoryName")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating category keywords: $categoryName")
                throw e
            }
        }

    override suspend fun getCategoryByName(name: String): Category? = withContext(ioDispatcher) {
        try {
            val categories = getCategories()
            categories.find { it.name.equals(name, ignoreCase = true) }
        } catch (e: Exception) {
            Timber.e(e, "Error getting category by name: $name")
            null
        }
    }

    override suspend fun searchCategoriesByKeyword(keyword: String): List<Category> =
        withContext(ioDispatcher) {
            try {
                val categories = getCategories()
                categories.filter { category ->
                    category.keywords.any {
                        it.contains(keyword, ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching categories by keyword: $keyword")
                emptyList()
            }
        }

    private fun getDefaultCategoriesList(): List<Category> = listOf(
        Category(
            name = "Food & Dining",
            keywords = listOf(
                "food", "restaurant", "cafe", "pizza", "burger", "meal",
                "dinner", "lunch", "breakfast", "snack", "coffee", "tea",
                "zomato", "swiggy", "dominos", "mcdonald", "kfc", "subway",
                "biryani", "dosa", "samosa", "paratha", "roti", "rice"
            ),
            isDefault = true
        ),
        Category(
            name = "Transport",
            keywords = listOf(
                "uber", "ola", "taxi", "bus", "train", "metro", "auto",
                "petrol", "fuel", "diesel", "parking", "toll", "rickshaw",
                "flight", "plane", "airport", "railway", "station",
                "bike", "car", "vehicle", "transport"
            ),
            isDefault = true
        ),
        Category(
            name = "Shopping",
            keywords = listOf(
                "amazon", "flipkart", "myntra", "ajio", "clothes", "shirt",
                "shoes", "shopping", "mall", "store", "dress", "jeans",
                "electronics", "mobile", "laptop", "headphones", "book",
                "groceries", "vegetables", "fruits", "market", "supermarket"
            ),
            isDefault = true
        ),
        Category(
            name = "Entertainment",
            keywords = listOf(
                "movie", "cinema", "netflix", "amazon prime", "spotify",
                "youtube", "game", "concert", "party", "club", "bar",
                "theatre", "show", "music", "subscription", "streaming",
                "book", "magazine", "hobby", "sports", "gym"
            ),
            isDefault = true
        ),
        Category(
            name = "Bills & Utilities",
            keywords = listOf(
                "electricity", "water", "gas", "internet", "wifi", "phone",
                "mobile", "recharge", "bill", "rent", "maintenance",
                "insurance", "loan", "emi", "credit card", "bank",
                "utility", "service", "subscription"
            ),
            isDefault = true
        ),
        Category(
            name = "Health & Medical",
            keywords = listOf(
                "doctor", "medicine", "hospital", "pharmacy", "health",
                "medical", "clinic", "appointment", "checkup", "treatment",
                "surgery", "dental", "eye", "test", "lab", "report",
                "vitamin", "supplement", "first aid", "emergency"
            ),
            isDefault = true
        ),
        Category(
            name = "Education",
            keywords = listOf(
                "school", "college", "university", "course", "class",
                "tuition", "coaching", "book", "study", "exam", "fee",
                "education", "learning", "online course", "certification",
                "training", "workshop", "seminar"
            ),
            isDefault = true
        ),
        Category(
            name = "Personal Care",
            keywords = listOf(
                "salon", "haircut", "spa", "massage", "cosmetics", "makeup",
                "skincare", "shampoo", "soap", "toothbrush", "personal",
                "grooming", "beauty", "parlour", "barber"
            ),
            isDefault = true
        ),
        Category(
            name = "Gifts & Donations",
            keywords = listOf(
                "gift", "present", "birthday", "anniversary", "wedding",
                "donation", "charity", "temple", "church", "mosque",
                "festival", "celebration", "party", "occasion"
            ),
            isDefault = true
        ),
        Category(
            name = "Miscellaneous",
            keywords = listOf(
                "other", "misc", "miscellaneous", "random", "general",
                "unknown", "cash", "atm", "withdrawal", "transfer"
            ),
            isDefault = true
        )
    )
}
