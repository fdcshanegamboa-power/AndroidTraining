# Data Flow Diagrams

## Save Photo Flow (Detailed)

```
┌──────────────────────────────────────────────────────────────────┐
│ USER INTERFACE                                                   │
│ HomeFragment - User clicks camera button                        │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 1. Launch camera
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ CAMERA APP                                                       │
│ User takes photo, returns Bitmap                                │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 2. cameraLauncher callback
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ VIEW MODEL                                                       │
│ HomeViewModel.uploadPhoto(bitmap)                               │
│ - Creates PhotoItem with PENDING status                        │
│ - Calls repository                                              │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 3. savePhoto(photo, bitmap)
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ REPOSITORY (Main Thread - Instant)                              │
│ PhotoRepository.savePhoto()                                     │
│                                                                  │
│ Step 1: Save bitmap to local file                              │
│   → filesDir/photos/{photoId}.jpg                              │
│                                                                  │
│ Step 2: Insert to Room DB                                       │
│   → PhotoItem(syncStatus = PENDING, localFilePath = "...")     │
│                                                                  │
│ Step 3: Return Success immediately ✓                            │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             ├──────────────────┬──────────────────┐
                             │                  │                  │
                    4a. Room Flow        4b. Launch         5. Return
                      emits update      background          to caller
                             │          coroutine              │
                             ▼              │                  ▼
┌─────────────────────────────────┐        │    ┌──────────────────┐
│ UI UPDATES (Instant)            │        │    │ ViewModel         │
│ - Photo appears in list         │        │    │ - Shows success   │
│ - Sync indicator: 🟡 YELLOW     │        │    └──────────────────┘
│ - User can see photo loading    │        │
└─────────────────────────────────┘        │
                                           ▼
                        ┌────────────────────────────────────────┐
                        │ BACKGROUND UPLOAD (Non-blocking)       │
                        │ PhotoRepository.uploadToCloud()        │
                        │                                        │
                        │ Step 1: Upload to Cloudinary          │
                        │   → CloudinaryRepository.uploadPhoto() │
                        │   → Returns Cloudinary URL             │
                        │                                        │
                        │ Step 2: Save metadata to Firestore    │
                        │   → FirebasePhotoRepository.savePhoto()│
                        │   → Saves with Cloudinary URL          │
                        │                                        │
                        │ Step 3: Update Room status             │
                        │   → photoDao.updateSyncStatus()        │
                        │   → syncStatus = SYNCED                │
                        │   → imageUrl = cloudinaryUrl           │
                        │                                        │
                        │ Step 4: Delete local file              │
                        │   → file.delete() (save storage)       │
                        └────────────────┬───────────────────────┘
                                        │
                                        │ 6. Room Flow emits update
                                        ▼
                        ┌────────────────────────────────────────┐
                        │ UI UPDATES (Auto)                      │
                        │ - Sync indicator: 🟢 GREEN             │
                        │ - Photo now loads from Cloudinary URL  │
                        │ - Local file deleted                   │
                        └────────────────────────────────────────┘
```

---

## Load Photos Flow (App Start)

```
┌──────────────────────────────────────────────────────────────────┐
│ APP START                                                        │
│ HomeViewModel.init() → loadPhotos()                             │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 1. getAllPhotosForUser(userId)
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ REPOSITORY                                                       │
│ PhotoRepository.getAllPhotosForUser()                           │
│                                                                  │
│ A. Return Room Flow immediately (instant display)               │
│ B. Launch background sync from Firestore                        │
└─────────────┬──────────────────────────┬─────────────────────────┘
              │                          │
              │ 2a. Instant              │ 2b. Background
              ▼                          ▼
┌──────────────────────────┐  ┌─────────────────────────────────┐
│ Room Flow (Immediate)    │  │ Background Sync                 │
│                          │  │ syncFromCloud(userId)           │
│ photoDao                 │  │                                 │
│  .getAllPhotosForUser()  │  │ 1. Fetch from Firestore         │
│                          │  │ 2. Compare with local           │
│ Returns: Flow<List>      │  │ 3. Cloud wins on conflicts      │
│ → Emits current photos   │  │ 4. Update Room                  │
└────────────┬─────────────┘  └──────────────┬──────────────────┘
             │                               │
             │ 3. Collect Flow               │ 4. Flow emits update
             ▼                               ▼
┌──────────────────────────────────────────────────────────────────┐
│ VIEW MODEL                                                       │
│ viewModelScope.launch {                                         │
│   photoRepository.getAllPhotosForUser(userId)                   │
│     .collect { photos ->                                         │
│       _uiState.update { state.copy(photos = photos) }          │
│     }                                                            │
│ }                                                                │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 5. StateFlow emits
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ UI (HomeFragment)                                                │
│                                                                  │
│ Instant display:                                                │
│ - Shows local photos immediately                                │
│ - Loads images (Cloudinary URL or local file)                  │
│                                                                  │
│ After sync:                                                     │
│ - Auto-updates if cloud has newer photos                       │
│ - No user action required                                       │
└──────────────────────────────────────────────────────────────────┘
```

---

## Delete Photo Flow (Cloud First)

```
┌──────────────────────────────────────────────────────────────────┐
│ USER ACTION                                                      │
│ User clicks delete button on photo                              │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 1. onDeleteClick(photo)
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ VIEW MODEL                                                       │
│ HomeViewModel.deletePhoto(photo)                                │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 2. deletePhoto(photo)
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ REPOSITORY (Sequential - Cloud First)                           │
│ PhotoRepository.deletePhoto()                                   │
│                                                                  │
│ Step 1: Delete from Firebase Firestore (cloud first!)          │
│   → FirebasePhotoRepository.deletePhoto(photoId, userId)       │
│   → Verifies user ownership                                     │
│   → Deletes document                                            │
│   → ✓ Cloud is now updated                                     │
│                                                                  │
│ Step 2: Delete from Cloudinary (image storage)                 │
│   → CloudinaryRepository.deletePhoto(photo.id)                 │
│   → Note: Requires API credentials for actual deletion         │
│                                                                  │
│ Step 3: Delete from Room (local database)                      │
│   → photoDao.deletePhoto(photo)                                │
│   → Removes from local cache                                    │
│                                                                  │
│ Step 4: Delete local file (device storage)                     │
│   → File(photo.localFilePath).delete()                         │
│   → Frees up device storage                                     │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ 3. Room Flow emits update
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│ UI AUTO-UPDATES                                                  │
│                                                                  │
│ - Photo removed from RecyclerView                               │
│ - Photo count updated                                           │
│ - No manual refresh needed                                      │
│ - If this was last photo, preview cleared                       │
└──────────────────────────────────────────────────────────────────┘
```

---

## Image Loading Flow (Smart Fallback)

```
┌──────────────────────────────────────────────────────────────────┐
│ PhotoAdapter.bind(item: PhotoItem)                              │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             ▼
                     ┌───────────────┐
                     │ Check Status  │
                     └───────┬───────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
            ▼                ▼                ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │   SYNCED    │  │  PENDING    │  │   FAILED    │
    │   🟢 Green  │  │  🟡 Yellow  │  │   🔴 Red    │
    └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
           │                │                │
           ▼                ▼                ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ Load from Cloud  │  │ Load from Local  │  │ Load from Local  │
│                  │  │                  │  │ (or placeholder) │
│ if (imageUrl     │  │ if (localFile    │  │                  │
│  .isNotEmpty())  │  │  .exists())      │  │ Show retry icon  │
│                  │  │                  │  │                  │
│ Glide.load(      │  │ Glide.load(      │  │ Glide.load(      │
│   imageUrl       │  │   File(path)     │  │   File(path)     │
│ )                │  │ )                │  │ )                │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

## Sync Status State Machine

```
                    ┌─────────────────┐
                    │   Take Photo    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │    PENDING      │
                    │   🟡 Yellow     │
                    │                 │
                    │  - Photo saved  │
                    │    locally      │
                    │  - Upload       │
                    │    started      │
                    └────────┬────────┘
                             │
                ┌────────────┼────────────┐
                │                         │
                ▼                         ▼
       ┌─────────────────┐      ┌─────────────────┐
       │     SYNCED      │      │     FAILED      │
       │    🟢 Green     │      │     🔴 Red      │
       │                 │      │                 │
       │  - Uploaded to  │      │  - Upload error │
       │    Cloudinary   │      │  - Check logs   │
       │  - Saved to     │      │  - Retry needed │
       │    Firestore    │      │                 │
       │  - Local file   │      │                 │
       │    deleted      │      │                 │
       └─────────────────┘      └─────────┬───────┘
                                           │
                                           │ Manual
                                           │ Retry
                                           │
                                           ▼
                                  ┌─────────────────┐
                                  │    PENDING      │
                                  │   🟡 Yellow     │
                                  └─────────────────┘
```

---

## Multi-Device Sync (Future Enhancement)

```
Device A                                                Device B
   │                                                       │
   │ Take Photo                                           │
   ▼                                                       │
┌──────────┐                                              │
│  Local   │                                              │
│  Save    │                                              │
└────┬─────┘                                              │
     │                                                     │
     │ Upload                                             │
     ▼                                                     │
┌──────────┐                                              │
│Cloudinary│                                              │
└────┬─────┘                                              │
     │                                                     │
     │ Save metadata                                      │
     ▼                                                     │
┌──────────────────────────────────────────────┐         │
│         Firebase Firestore                   │         │
│         (Cloud - Source of Truth)            │         │
└────┬──────────────────────────────────┬──────┘         │
     │                                   │                │
     │ ✓ Photo synced                   │ Real-time      │
     │                                   │ listener       │
     │                                   │ (future)       │
     ▼                                   ▼                │
┌──────────┐                        ┌────────────┐       │
│ Device A │                        │  Device B  │◄──────┘
│ Updated  │                        │  Opens App │
└──────────┘                        └─────┬──────┘
                                          │
                                          │ Sync from cloud
                                          ▼
                                    ┌────────────┐
                                    │ Downloads  │
                                    │ new photos │
                                    └────────────┘
```

---

## Error Handling Flow

```
┌──────────────────────────────────────────────────────────────────┐
│ Upload Process                                                   │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             ▼
                    ┌────────────────┐
                    │ Try Upload to  │
                    │  Cloudinary    │
                    └────┬───────────┘
                         │
            ┌────────────┴────────────┐
            │                         │
            ▼                         ▼
    ┌───────────────┐         ┌──────────────┐
    │   SUCCESS     │         │    FAILURE   │
    └───────┬───────┘         └──────┬───────┘
            │                        │
            │                        │ Set status = FAILED
            ▼                        │ Log error
    ┌───────────────┐                │ Keep local file
    │ Try Save to   │                │
    │  Firestore    │                ▼
    └───────┬───────┘         ┌──────────────┐
            │                 │ UI shows 🔴  │
┌───────────┴───────────┐     │              │
│                       │     │ User can:    │
▼                       ▼     │ - Retry      │
┌──────────┐    ┌──────────┐ │ - Delete     │
│ SUCCESS  │    │ FAILURE  │ │ - Wait for   │
└────┬─────┘    └────┬─────┘ │   retry      │
     │               │        └──────────────┘
     │               │ Set status = FAILED
     ▼               │ Keep in Firestore queue
┌──────────┐         │
│Update    │         ▼
│Room      │  ┌──────────────┐
│SYNCED    │  │ UI shows 🔴  │
│Delete    │  │ (partial)    │
│local     │  └──────────────┘
└──────────┘
     │
     ▼
┌──────────┐
│UI shows  │
│🟢 Green  │
└──────────┘
```

---

**These diagrams show the complete data flow of the hybrid architecture!**

