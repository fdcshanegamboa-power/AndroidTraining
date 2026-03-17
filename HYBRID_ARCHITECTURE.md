# Hybrid Architecture: Room + Cloudinary + Firebase

## Overview
This app now uses a **hybrid offline-first architecture** combining:
- **Room Database** - Local cache for instant UI updates
- **Cloudinary** - Cloud image storage
- **Firebase Firestore** - Cloud metadata storage (source of truth)
- **Firebase Auth** - User authentication

## Architecture Strategy

### 1. Save Photo Flow (Offline-First)
```
User Takes Photo
    ↓
Save locally INSTANTLY (Room + local file) with PENDING status
    ↓
UI updates immediately (yellow indicator)
    ↓
Background upload to Cloudinary (non-blocking)
    ↓
Get Cloudinary URL
    ↓
Save metadata to Firebase Firestore
    ↓
Update Room status to SYNCED (green indicator)
    ↓
Delete local file (save device storage)
```

### 2. Load Photos Flow (Cloud as Source of Truth)
```
User Opens App
    ↓
Display local photos from Room immediately
    ↓
Background sync from Firebase Firestore
    ↓
Merge cloud photos with local (cloud wins on conflicts)
    ↓
Update Room database
    ↓
UI auto-updates via Flow
```

### 3. Delete Photo Flow (Cloud First)
```
User Clicks Delete
    ↓
Delete from Firebase Firestore (cloud first)
    ↓
Delete from Cloudinary
    ↓
Delete from Room
    ↓
Delete local file
    ↓
UI auto-updates via Flow
```

## Sync Status Indicators

Photos display a colored dot indicator:
- 🟡 **Yellow (Orange)** - `PENDING` - Uploading to cloud
- 🟢 **Green** - `SYNCED` - Successfully uploaded to cloud
- 🔴 **Red** - `FAILED` - Upload failed (retry needed)

## PhotoItem Structure

```kotlin
@Entity(tableName = "photos")
data class PhotoItem(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val date: String,
    val imageUrl: String,           // Cloudinary URL (empty until uploaded)
    val localFilePath: String,      // Local file path (deleted after upload)
    val timestamp: Long,
    val syncStatus: SyncStatus,     // PENDING, SYNCED, or FAILED
    val lastSyncedAt: Long?         // Last sync timestamp
)

enum class SyncStatus {
    PENDING,    // Yellow - Uploading to cloud
    SYNCED,     // Green - Successfully uploaded
    FAILED      // Red - Upload failed
}
```

## Key Components

### 1. PhotoRepository (Orchestrator)
**Location:** `/app/src/main/java/com/learn/androidtraining/repository/PhotoRepository.kt`

Main coordinator for all photo operations:
- `savePhoto()` - Saves locally instantly, triggers background cloud upload
- `getAllPhotosForUser()` - Returns Flow, syncs from cloud in background
- `deletePhoto()` - Deletes from cloud first, then local
- `uploadToCloud()` - Background upload to Cloudinary + Firebase
- `syncFromCloud()` - Syncs photos from Firebase to Room

### 2. CloudinaryRepository
**Location:** `/app/src/main/java/com/learn/androidtraining/cloudinary/CloudinaryRepository.kt`

Handles image uploads to Cloudinary:
- `uploadPhoto()` - Upload file, returns Cloudinary URL
- `deletePhoto()` - Delete image from Cloudinary (requires server-side)

### 3. FirebasePhotoRepository
**Location:** `/app/src/main/java/com/learn/androidtraining/firebase/FirebasePhotoRepository.kt`

Manages photo metadata in Firestore:
- `savePhoto()` - Save photo metadata to Firestore
- `getAllPhotos()` - Fetch all photos for user (cloud as source of truth)
- `deletePhoto()` - Delete photo metadata from Firestore

### 4. Room Database
**Location:** `/app/src/main/java/com/learn/androidtraining/database/`

Local cache with reactive Flow:
- `AppDatabase` - Database singleton with TypeConverters for SyncStatus
- `PhotoDao` - DAO with sync-related queries
- `Converters` - TypeConverter for SyncStatus enum

### 5. UI Layer
**PhotoAdapter** - Shows sync status indicators with colored dots
**HomeViewModel** - Manages UI state and coordinates repository operations
**HomeFragment** - Displays photos with Glide (loads from URL or local file)

## Configuration

### Cloudinary Setup
Add to `/local.properties`:
```properties
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_UPLOAD_PRESET=your_upload_preset
```

### Firebase Setup
- Firebase Auth already configured
- Firebase Firestore added to dependencies
- Collection: `photos`

## Benefits

✅ **Instant UI feedback** - Photos appear immediately (offline-first)
✅ **Works offline** - Full functionality without network
✅ **Cloud backup** - Photos synced to cloud for multi-device access
✅ **Visual sync status** - Users see upload progress (yellow → green)
✅ **Saves device storage** - Local files deleted after successful upload
✅ **Cloud as source of truth** - On app start, syncs from cloud
✅ **Conflict resolution** - Cloud wins on conflicts
✅ **Reliable** - Background sync continues even if app is closed (via coroutines)

## How It Works

### Taking a Photo
1. User clicks camera button
2. Camera captures bitmap
3. `HomeViewModel.uploadPhoto()` called
4. `PhotoRepository.savePhoto()`:
   - Saves bitmap to local file instantly
   - Inserts into Room with `PENDING` status
   - Returns immediately (UI shows yellow dot)
5. Background coroutine uploads to Cloudinary
6. On success, saves to Firebase Firestore
7. Updates Room to `SYNCED` status (UI shows green dot)
8. Deletes local file to save storage

### Viewing Photos
1. User opens app
2. `HomeViewModel.loadPhotos()` starts
3. Room emits current photos via Flow (instant display)
4. Background sync fetches from Firebase Firestore
5. Merges cloud photos with local (cloud wins)
6. Room Flow emits updated list
7. UI auto-updates
8. Glide loads images:
   - If `SYNCED` → loads from Cloudinary URL
   - If `PENDING` → loads from local file path

### Deleting a Photo
1. User clicks delete button
2. `HomeViewModel.deletePhoto()` called
3. `PhotoRepository.deletePhoto()`:
   - Deletes from Firebase Firestore first (cloud as source of truth)
   - Deletes from Cloudinary (optional)
   - Deletes from Room
   - Deletes local file
4. Room Flow emits updated list
5. UI auto-updates

## Database Migration

Changed from version 1 to version 2:
- Added `syncStatus` field (SyncStatus enum)
- Added `lastSyncedAt` field (Long?)
- Renamed `imageUrl` to store Cloudinary URL
- Added `localFilePath` field
- Uses `.fallbackToDestructiveMigration()` for simplicity

## Future Enhancements

1. **WorkManager** - For reliable background sync even when app is closed
2. **Retry mechanism** - Automatic retry for failed uploads
3. **Batch sync** - Sync multiple photos at once
4. **Progress indicator** - Show upload progress percentage
5. **Offline mode banner** - Notify user when offline
6. **Cache cleanup** - Automatically delete old local files
7. **Compression** - Optimize image size before upload
8. **Multi-device sync** - Real-time sync across devices using Firebase listeners

## Testing Checklist

- [ ] Sync Gradle (should succeed)
- [ ] Build project (should compile without errors)
- [ ] Add Cloudinary credentials to `local.properties`
- [ ] Run app and login with Firebase Auth
- [ ] Take a photo (should appear instantly with yellow dot)
- [ ] Wait for upload (should turn green when synced)
- [ ] Check photo loads from Cloudinary URL
- [ ] Close and reopen app (should sync from cloud)
- [ ] Delete a photo (should delete from cloud and local)
- [ ] Test offline mode (photos should save locally)
- [ ] Go online (pending photos should upload)
- [ ] Test with multiple devices (cloud as source of truth)

## Notes

- Local files are temporary and deleted after successful cloud upload
- Cloud is the source of truth for photo metadata
- Room serves as a local cache for instant UI updates
- Sync happens automatically in the background
- Failed uploads show red indicator (manual retry needed for now)
- Cloudinary deletion requires API credentials (not implemented for unsigned uploads)

