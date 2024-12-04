package com.opsc7311poe.gourmetguru_opscpoe

import androidx.room.Dao
import androidx.room.Delete
import java.time.Duration
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Method

@Entity(tableName = "recipes")
data class RecipeData (
    @PrimaryKey(autoGenerate = true) val id: Int? = 0, //RoomDb autogenerates an id for saving offline
    var name: String?,
    var durationHrs: Double?,
    var durationMins: Double?,
    var ingredients: List<Ingredient>?,
    var method: List<String>?,
    var isLocked: Boolean?

){
    // No-argument constructor (required by Firebase)
    constructor() : this(null, null, null, null, null, null, null)
}

data class Ingredient(
    var name: String?,
    var amount: String?
){
    // No-argument constructor (required by Firebase)
    constructor() : this(null, null)
}

//making a Data Access Object for RoomDB
@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeData)

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeData>

    @Delete
    suspend fun deleteRecipe(recipe: RecipeData)
}

//converters for RoomDB
class Converters {
    @TypeConverter
    fun fromIngredientList(ingredients: List<Ingredient>?): String {
        return Gson().toJson(ingredients)
    }
    @TypeConverter
    fun toIngredientList(ingredientsString: String): List<Ingredient> {
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return  Gson().fromJson(ingredientsString, type)
    }
    @TypeConverter
    fun fromMethodList(method: List<String>?): String {
        return Gson().toJson(method)
    }
    @TypeConverter
    fun toMethodList(methodString: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return  Gson().fromJson(methodString, type)
    }
}
