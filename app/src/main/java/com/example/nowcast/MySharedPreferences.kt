import android.content.Context

object MySharedPreferences {

    private const val PREFS_NAME = "MyPrefsFile"

    // Method to save a string value in SharedPreferences
    fun saveData(context: Context, key: String, value: String) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.apply()
    }

    // Method to retrieve a string value from SharedPreferences
    fun loadData(context: Context, key: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, "") ?: ""
    }
}
