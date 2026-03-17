# Complete Guide: How to Reset Room Database

## ✅ Implementation Complete!

I've added **2 new programmatic methods** to reset your Room database. You now have **5 different ways** to reset the database.

---

## 🎯 5 Ways to Reset Room Database

### Method 1: Clear App Data (Fastest - No Code) ⚡
**Best for:** Quick testing during development

**Steps:**
1. On device/emulator: **Settings → Apps → Android Training**
2. Tap **Storage & cache**
3. Tap **Clear storage** or **Clear data**
4. Confirm
5. Relaunch app

**Result:** ✅ All data deleted, fresh database

**Pros:** ✅ Instant, no rebuild needed
**Cons:** ❌ Need device access

---

### Method 2: Uninstall/Reinstall (Simple) 🔄
**Best for:** Complete fresh start

**Steps:**
1. Long press app icon → **Uninstall**
2. Or: `adb uninstall com.learn.androidtraining`
3. Run app from Android Studio

**Result:** ✅ Complete reset with fresh install

**Pros:** ✅ Guaranteed clean state
**Cons:** ❌ Reinstall takes time

---

### Method 3: Change Database Version (Automatic) 🔢
**Best for:** Schema changes during development

**How it works:**
Your `AppDatabase.kt` already has:
```kotlin
@Database(entities = [PhotoItem::class], version = 2, exportSchema = false)
//                                              ↑ Current version
```

**Steps:**
1. Change version number: `version = 2` → `version = 3`
2. Rebuild app
3. Run app

**Code change:**
```kotlin
@Database(entities = [PhotoItem::class], version = 3, exportSchema = false)
```

**Result:** ✅ Database automatically deleted and recreated on next launch

**Pros:** ✅ Automatic, good for schema changes
**Cons:** ❌ Requires rebuild, version number increases

---

### Method 4: Clear All Data (NEW - Programmatic) 🆕
**Best for:** Logout, testing, user-initiated reset

**Added method:**
```kotlin
// In PhotoRepository
suspend fun clearAllData(): Result<Unit>
```

**Usage Example:**
```kotlin
// In HomeViewModel or SettingsViewModel
fun clearAllPhotos() {
    viewModelScope.launch {
        photoRepository.clearAllData().fold(
            onSuccess = {
                Log.d("Reset", "All data cleared successfully")
                // Show success message to user
            },
            onFailure = { e ->
                Log.e("Reset", "Failed to clear data", e)
            }
        )
    }
}
```

**What it does:**
- ✅ Deletes all photo files from `filesDir/photos/`
- ✅ Clears all tables in Room database
- ✅ Keeps database structure intact
- ✅ Fast and safe

**Result:** ✅ Empty database, ready for new data

**Pros:** ✅ Programmatic, can be triggered by user button, fast
**Cons:** ❌ Need to call from code

---

### Method 5: Full Database Reset (NEW - Nuclear Option) ☢️
**Best for:** Major corruption, testing edge cases

**Added method:**
```kotlin
// In PhotoRepository
fun resetDatabase()
```

**Usage Example:**
```kotlin
// In ViewModel or during testing
fun hardResetDatabase() {
    photoRepository.resetDatabase()
    Log.d("Reset", "Database completely reset")
    // May need to restart app or refresh UI
}
```

**What it does:**
- ✅ Closes database connection
- ✅ **DELETES** the entire database file
- ✅ Deletes all photo files
- ✅ Resets INSTANCE to null
- ✅ Next access will create fresh database

**Result:** ✅ Complete database destruction and recreation

**Pros:** ✅ Nuclear option, fixes any corruption
**Cons:** ⚠️ Destructive, may need app restart

---

## 📋 Quick Reference Table

| Method | Speed | Requires Rebuild | Requires Device Access | Programmatic | Destructive Level |
|--------|-------|------------------|----------------------|--------------|-------------------|
| Clear App Data | ⚡ Instant | ❌ No | ✅ Yes | ❌ No | 🔥🔥🔥 High |
| Uninstall/Reinstall | 🐌 Slow | ❌ No | ✅ Yes | ❌ No | 🔥🔥🔥 High |
| Change Version | 🏃 Fast | ✅ Yes | ❌ No | ❌ No | 🔥🔥🔥 High |
| Clear All Data | ⚡ Instant | ❌ No | ❌ No | ✅ Yes | 🔥🔥 Medium |
| Reset Database | ⚡ Instant | ❌ No | ❌ No | ✅ Yes | 🔥🔥🔥 High |

---

## 🎯 When to Use Each Method

### Development/Testing Scenarios

**"I changed PhotoItem fields and getting errors"**
→ Use **Method 3: Change Version** (version 2 → 3)

**"I want to test the app with no data"**
→ Use **Method 1: Clear App Data** (fastest)

**"I'm getting weird database corruption"**
→ Use **Method 5: Reset Database** (nuclear option)

**"I want to add a 'Clear All Photos' button"**
→ Use **Method 4: Clear All Data** (user-friendly)

**"Fresh install test"**
→ Use **Method 2: Uninstall/Reinstall**

---

## 💻 Code Examples

### Example 1: Add "Clear All" Button in UI

**In HomeViewModel:**
```kotlin
fun clearAllPhotos() {
    viewModelScope.launch {
        photoRepository.clearAllData().fold(
            onSuccess = {
                _uiState.update { it.copy(
                    photos = emptyList(),
                    lastPhotoUrl = null
                )}
                Log.d(tag, "All photos cleared")
            },
            onFailure = { e ->
                _uiState.update { it.copy(
                    errorMessage = "Failed to clear photos"
                )}
                Log.e(tag, "Clear failed", e)
            }
        )
    }
}
```

**In HomeFragment:**
```kotlin
// Add button in layout
binding.buttonClearAll.setOnClickListener {
    AlertDialog.Builder(requireContext())
        .setTitle("Clear All Photos")
        .setMessage("Delete all photos? This cannot be undone.")
        .setPositiveButton("Delete All") { _, _ ->
            viewModel.clearAllPhotos()
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

---

### Example 2: Clear on Logout

**In LoginViewModel:**
```kotlin
fun logout() {
    viewModelScope.launch {
        // Clear user photos
        photoRepository.clearAllData()
        
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()
        
        Log.d(tag, "User logged out, data cleared")
    }
}
```

---

### Example 3: Testing Database Reset

**In Test Class:**
```kotlin
@Test
fun testDatabaseReset() = runTest {
    // Add some photos
    photoRepository.savePhoto(testPhoto1, testBitmap1)
    photoRepository.savePhoto(testPhoto2, testBitmap2)
    
    // Verify photos exist
    val photos = photoRepository.getAllPhotosForUser(testUserId).first()
    assertEquals(2, photos.size)
    
    // Reset database
    photoRepository.clearAllData().getOrThrow()
    
    // Verify empty
    val photosAfter = photoRepository.getAllPhotosForUser(testUserId).first()
    assertEquals(0, photosAfter.size)
}
```

---

### Example 4: Debug/Settings Screen

**Add to SettingsFragment:**
```kotlin
class SettingsFragment : Fragment() {
    
    private val photoRepository by lazy { PhotoRepository(requireContext()) }
    
    private fun setupDebugOptions() {
        // Only show in debug builds
        if (BuildConfig.DEBUG) {
            binding.buttonClearData.visibility = View.VISIBLE
            binding.buttonResetDatabase.visibility = View.VISIBLE
            
            binding.buttonClearData.setOnClickListener {
                showClearDataDialog()
            }
            
            binding.buttonResetDatabase.setOnClickListener {
                showResetDatabaseDialog()
            }
        }
    }
    
    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("Delete all photos and clear database?")
            .setPositiveButton("Clear") { _, _ ->
                lifecycleScope.launch {
                    photoRepository.clearAllData()
                    Toast.makeText(requireContext(), 
                        "All data cleared", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showResetDatabaseDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Reset Database")
            .setMessage("COMPLETELY destroy and recreate database? App may need restart.")
            .setPositiveButton("Reset") { _, _ ->
                photoRepository.resetDatabase()
                Toast.makeText(requireContext(), 
                    "Database reset - please restart app", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
```

---

## 🧪 Testing Commands

### ADB Commands (Command Line)

**Clear app data:**
```bash
adb shell pm clear com.learn.androidtraining
```

**Delete database file directly:**
```bash
adb shell run-as com.learn.androidtraining rm /data/data/com.learn.androidtraining/databases/photo_database
```

**Uninstall app:**
```bash
adb uninstall com.learn.androidtraining
```

---

## ⚠️ Important Notes

### About `clearAllData()`
- ✅ Safe to use anytime
- ✅ Deletes data but keeps structure
- ✅ Fast operation
- ✅ Can be called multiple times
- ❌ Doesn't affect schema

### About `resetDatabase()`
- ⚠️ Destructive operation
- ⚠️ Closes database connections
- ⚠️ May cause crash if app tries to access DB immediately after
- ⚠️ Use only when necessary
- ✅ Good for fixing corruption

### General Tips
- 🔒 Both methods already delete local photo files
- 🔄 UI will auto-update via Flow after clearing (empty list)
- 💾 Doesn't affect Cloudinary (photos stay in cloud)
- ☁️ Doesn't affect Firestore (metadata stays in cloud)
- 🔑 Doesn't affect Firebase Auth (user stays logged in)

---

## 🚀 Quick Start - Choose Your Method

**Right now, for quick test:**
1. Stop your app
2. Run: `adb shell pm clear com.learn.androidtraining`
3. Restart app
4. ✅ Fresh database!

**For future use in code:**
```kotlin
// In ViewModel
viewModelScope.launch {
    photoRepository.clearAllData()
}
```

---

## 📝 Summary

**Added to your project:**
1. ✅ `AppDatabase.clearDatabase()` - Clear all tables
2. ✅ `AppDatabase.resetDatabase()` - Destroy and recreate database
3. ✅ `PhotoRepository.clearAllData()` - Clear data + delete files
4. ✅ `PhotoRepository.resetDatabase()` - Full reset + delete files

**You can now:**
- Reset database programmatically from code
- Add "Clear All" button for users
- Clear data on logout
- Test with empty database easily
- Fix database corruption issues

**Choose based on need:**
- Quick test? → Clear App Data
- In code? → `clearAllData()`
- Corruption? → `resetDatabase()`
- Schema change? → Change version number

---

**All methods are ready to use! Try them out! 🎉**

