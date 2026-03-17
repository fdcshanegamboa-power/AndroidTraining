# Quick Reference: New Photo Architecture

## File Structure
```
app/src/main/java/com/learn/androidtraining/
├── database/
│   ├── AppDatabase.kt          # Room database singleton
│   └── PhotoDao.kt             # Database operations
├── repository/
│   └── PhotoRepository.kt      # Business logic layer
├── photos/
│   ├── PhotoItem.kt            # Room entity (data model)
│   ├── PhotoAdapter.kt         # RecyclerView adapter
│   └── PhotoDiffCallback.kt    # RecyclerView diff util
├── fragments/home/
│   ├── HomeViewModel.kt        # UI logic + state management
│   └── HomeFragment.kt         # UI layer
├── utils/
│   └── DataStoreManager.kt     # Preferences storage
└── App.kt                      # Application class
```

## Data Flow

### Taking & Saving a Photo
```
User Clicks Camera Button
    ↓
HomeFragment.cameraLauncher
    ↓
Camera app returns Bitmap
    ↓
HomeViewModel.uploadPhoto(bitmap)
    ↓
PhotoRepository.savePhoto(photo, bitmap)
    ↓
1. Save bitmap to filesDir/photos/{id}.jpg
2. Insert metadata into Room DB
    ↓
Room emits new list via Flow
    ↓
HomeViewModel collects Flow
    ↓
UI updates automatically
```

### Loading Photos
```
User Opens App / Logs In
    ↓
HomeViewModel.init() calls loadPhotos()
    ↓
PhotoRepository.getAllPhotosForUser(userId)
    ↓
Returns Flow<List<PhotoItem>>
    ↓
HomeViewModel collects Flow
    ↓
Updates UI state continuously (reactive)
    ↓
HomeFragment observes state
    ↓
PhotoAdapter displays photos
    ↓
Glide loads images from local file paths
```

### Deleting a Photo
```
User Clicks Delete Button
    ↓
HomeFragment calls viewModel.deletePhoto(photo)
    ↓
HomeViewModel.deletePhoto(photo)
    ↓
PhotoRepository.deletePhoto(photo)
    ↓
1. Delete from Room database
2. Delete file from storage
    ↓
Room emits updated list via Flow
    ↓
UI updates automatically
```

## Key Components

### PhotoItem (Entity)
```kotlin
@Entity(tableName = "photos")
data class PhotoItem(
    @PrimaryKey val id: String,
    val userId: String,           // Firebase Auth UID
    val name: String,             // e.g., "photo_1234567890.jpg"
    val date: String,             // Formatted date string
    val imageUrl: String,         // Local file path
    val timestamp: Long           // Unix timestamp
)
```

### PhotoDao (Database Operations)
```kotlin
@Dao interface PhotoDao {
    suspend fun insertPhoto(photo: PhotoItem)
    fun getAllPhotosForUser(userId: String): Flow<List<PhotoItem>>
    suspend fun getPhotoById(photoId: String): PhotoItem?
    suspend fun deletePhoto(photo: PhotoItem)
    suspend fun deleteAllPhotosForUser(userId: String)
}
```

### PhotoRepository (Business Logic)
```kotlin
class PhotoRepository(context: Context) {
    suspend fun savePhoto(photo: PhotoItem, bitmap: Bitmap): Result<Unit>
    fun getAllPhotosForUser(userId: String): Flow<List<PhotoItem>>
    suspend fun deletePhoto(photo: PhotoItem): Result<Unit>
    suspend fun deleteAllPhotosForUser(userId: String): Result<Unit>
}
```

### HomeViewModel (State Management)
```kotlin
data class HomeUiState(
    val photos: List<PhotoItem>,
    val lastPhotoUrl: String?,
    val isLoading: Boolean,
    val errorMessage: String?
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val uiState: StateFlow<HomeUiState>
    fun uploadPhoto(bitmap: Bitmap)
    fun deletePhoto(photo: PhotoItem)
    fun clearError()
}
```

## Storage Locations

### Photos (Images)
- **Path**: `/data/data/com.learn.androidtraining/files/photos/`
- **Format**: `{photoId}.jpg`
- **Example**: `1710691234567.jpg`
- **Access**: Private to app only

### Database (Metadata)
- **Path**: `/data/data/com.learn.androidtraining/databases/photo_database`
- **Type**: SQLite database
- **Table**: `photos`
- **Access**: Via Room only

### Preferences (Last Photo)
- **Path**: `/data/data/com.learn.androidtraining/files/datastore/photo_prefs.preferences_pb`
- **Type**: Protobuf DataStore
- **Access**: Via DataStoreManager only

## Important Concepts

### Flow (Reactive Updates)
- Room DAO returns `Flow<List<PhotoItem>>`
- ViewModel collects this Flow continuously
- When database changes, Flow automatically emits new data
- UI updates without manual refresh

### Result Type (Error Handling)
- Repository methods return `Result<T>`
- Success: `Result.success(value)`
- Failure: `Result.failure(exception)`
- Usage: `.fold(onSuccess = {}, onFailure = {})`

### Coroutines (Async Operations)
- `suspend fun` for async database/file operations
- `viewModelScope.launch {}` for ViewModel coroutines
- All database operations run on background thread (Room handles this)

### Firebase Auth Integration
- Used for user identification only
- `FirebaseAuth.getInstance().currentUser?.uid` gets user ID
- Photos are filtered by userId in Room queries
- Each user sees only their own photos

## Common Operations

### Add Room Query
```kotlin
// In PhotoDao.kt
@Query("SELECT * FROM photos WHERE name LIKE :searchQuery")
fun searchPhotos(searchQuery: String): Flow<List<PhotoItem>>
```

### Add Repository Method
```kotlin
// In PhotoRepository.kt
fun searchPhotos(query: String): Flow<List<PhotoItem>> {
    return photoDao.searchPhotos("%$query%")
}
```

### Update ViewModel
```kotlin
// In HomeViewModel.kt
fun searchPhotos(query: String) {
    viewModelScope.launch {
        photoRepository.searchPhotos(query).collect { photos ->
            _uiState.update { it.copy(photos = photos) }
        }
    }
}
```

### Database Migration (Future)
```kotlin
// In AppDatabase.kt
@Database(entities = [PhotoItem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQL migration code here
            }
        }
    }
}
```

## Dependencies

### Required
- `androidx.room:room-runtime:2.6.1`
- `androidx.room:room-ktx:2.6.1`
- KSP: `androidx.room:room-compiler:2.6.1`
- Firebase Auth (for user management)
- DataStore (for preferences)
- Glide (for image loading)

### Not Needed
- ❌ Cloudinary
- ❌ Firebase Firestore
- ❌ Firebase Storage
- ❌ Network libraries (for photo upload)

## Benefits Summary

1. **Offline-First**: Works without internet
2. **Fast**: No network latency
3. **Private**: Data never leaves device
4. **Free**: No cloud storage costs
5. **Reactive**: UI auto-updates with Flow
6. **Type-Safe**: Compile-time SQL verification
7. **Simple**: Fewer dependencies, less complexity

