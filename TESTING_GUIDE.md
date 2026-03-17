# Testing Guide

## Before Running the App

### 1. Sync Gradle Dependencies
In Android Studio:
- Click "Sync Now" when prompted, or
- Go to File → Sync Project with Gradle Files
- Wait for sync to complete successfully

### 2. Clean and Rebuild
```bash
./gradlew clean build
```

Or in Android Studio:
- Build → Clean Project
- Build → Rebuild Project

## Testing the Refactored App

### Initial Setup
1. **Clear App Data** (to start fresh without old Cloudinary/Firestore data):
   - Go to device Settings → Apps → AndroidTraining → Storage → Clear Data
   - Or uninstall and reinstall the app

### Test Cases

#### ✅ Test 1: Firebase Auth (Should Still Work)
1. Launch app
2. If not logged in, go through login/signup flow
3. **Expected**: Firebase Auth should work exactly as before

#### ✅ Test 2: Take Photo
1. Click the camera button
2. Grant camera permission if prompted
3. Take a photo
4. **Expected**: 
   - Photo should save instantly (no upload delay)
   - Photo should appear at the top of the list
   - Photo should display in the preview area
   - Photo count should update

#### ✅ Test 3: View Photos
1. Take multiple photos
2. Scroll through the photo list
3. **Expected**:
   - All photos should display correctly
   - Photos should load from local storage (fast)
   - Photos should be ordered by timestamp (newest first)

#### ✅ Test 4: Delete Photo
1. Click the delete button on any photo
2. **Expected**:
   - Photo should disappear from list immediately
   - Photo should be removed from device storage
   - Photo count should update

#### ✅ Test 5: Data Persistence
1. Take several photos
2. Close the app completely
3. Reopen the app
4. **Expected**:
   - All photos should still be there
   - Photos should load from Room database

#### ✅ Test 6: User Isolation
1. Login as User A, take some photos
2. Logout
3. Login as User B, take different photos
4. **Expected**:
   - User B should only see their own photos
   - User B should NOT see User A's photos

5. Logout from User B
6. Login back as User A
7. **Expected**:
   - User A should see only their original photos

#### ✅ Test 7: Offline Usage
1. Turn off WiFi and mobile data
2. Take photos
3. View photos
4. Delete photos
5. **Expected**:
   - Everything should work perfectly offline
   - No network errors

## Troubleshooting

### Build Errors
If you see Gradle sync errors:
1. Check that KSP plugin version matches Kotlin version
2. Try: `./gradlew clean`
3. Try: File → Invalidate Caches → Invalidate and Restart

### Room Errors
If you see Room compilation errors:
- Make sure `@Entity` and `@PrimaryKey` annotations are properly imported
- Check that PhotoDao methods use correct return types
- Verify AppDatabase is properly annotated with `@Database`

### Runtime Errors
If app crashes on photo save:
- Check logcat for detailed error messages
- Verify file permissions (should be automatic for internal storage)
- Check that PhotoRepository is properly initializing the photos directory

### Photos Not Showing
If photos save but don't display:
- Check that Flow is being collected in ViewModel
- Verify Glide is loading from file paths correctly
- Check logcat for loading errors

## Verification Commands

### Check Room Database
```bash
# Connect to device
adb shell

# Navigate to app database
cd /data/data/com.learn.androidtraining/databases/

# List databases
ls -la

# Dump database contents (requires sqlite3)
sqlite3 photo_database
.tables
SELECT * FROM photos;
.quit
```

### Check Stored Photos
```bash
# Connect to device
adb shell

# Navigate to app files
cd /data/data/com.learn.androidtraining/files/photos/

# List photo files
ls -la
```

## Success Criteria

All tests pass, and:
- ✅ No Cloudinary references in code
- ✅ No Firebase Firestore references in code
- ✅ Firebase Auth still works
- ✅ Photos save locally and instantly
- ✅ Photos persist across app restarts
- ✅ Photos are user-specific
- ✅ App works completely offline
- ✅ UI updates automatically when photos are added/deleted
- ✅ No build or runtime errors

## Notes

- Photos are stored in: `/data/data/com.learn.androidtraining/files/photos/`
- Database is at: `/data/data/com.learn.androidtraining/databases/photo_database`
- Both locations are private to the app (other apps cannot access)
- Photos will be deleted if app is uninstalled

