# Cloudinary Setup Guide

## Step 1: Create Cloudinary Account
1. Go to https://cloudinary.com/
2. Click "Sign Up for Free"
3. Create an account (free tier includes 25GB storage and 25GB bandwidth/month)

## Step 2: Get Your Credentials
1. Login to your Cloudinary dashboard
2. You'll see your **Cloud Name** on the dashboard
3. Note down your **Cloud Name** (e.g., `dxxxxxxxxxxxxxx`)

## Step 3: Create Upload Preset
1. In Cloudinary dashboard, go to **Settings** (gear icon)
2. Click on **Upload** tab
3. Scroll down to **Upload presets**
4. Click **Add upload preset**
5. Configure:
   - **Preset name**: `android_unsigned` (or any name you like)
   - **Signing mode**: **Unsigned** (important!)
   - **Folder**: `android_photos` (optional, for organization)
   - **Use filename**: Yes (optional)
   - **Unique filename**: Yes (recommended)
6. Click **Save**
7. Note down the **preset name**

## Step 4: Add to local.properties
1. Open `/local.properties` in your Android project
2. Add these lines at the end:
```properties
CLOUDINARY_CLOUD_NAME=your_cloud_name_here
CLOUDINARY_UPLOAD_PRESET=your_preset_name_here
```

Example:
```properties
sdk.dir=/Users/yourname/Library/Android/sdk
CLOUDINARY_CLOUD_NAME=dab1234567890
CLOUDINARY_UPLOAD_PRESET=android_unsigned
```

## Step 5: Sync and Build
1. Sync Gradle in Android Studio
2. Build the project
3. The app will now use your Cloudinary credentials

## Important Notes

⚠️ **Security:**
- `local.properties` is NOT checked into version control (ignored by git)
- This keeps your credentials private
- Never commit `local.properties` to GitHub

⚠️ **Unsigned Uploads:**
- This app uses **unsigned uploads** for simplicity
- Unsigned uploads are public (anyone with the URL can view)
- For production apps, consider using **signed uploads** with a backend server

⚠️ **Free Tier Limits:**
- 25 GB storage
- 25 GB bandwidth/month
- 25,000 transformations/month
- Good enough for development and small apps

## Verify Setup

After setup, your Cloudinary config should look like:
```kotlin
// In BuildConfig (auto-generated)
BuildConfig.CLOUDINARY_CLOUD_NAME  // Your cloud name
BuildConfig.CLOUDINARY_UPLOAD_PRESET  // Your preset name
```

## Troubleshooting

**Error: "Cloud name is required"**
- Check that `CLOUDINARY_CLOUD_NAME` is correctly set in `local.properties`
- Make sure there are no extra spaces
- Sync Gradle and rebuild

**Error: "Upload preset not found"**
- Check that `CLOUDINARY_UPLOAD_PRESET` matches your preset name in Cloudinary dashboard
- Make sure the preset is set to **Unsigned** mode
- Sync Gradle and rebuild

**Error: "Upload failed"**
- Check your internet connection
- Verify credentials in Cloudinary dashboard
- Check logs for detailed error message

## Optional: Cloudinary Dashboard Features

### View Uploaded Images
1. Go to **Media Library** in Cloudinary dashboard
2. See all uploaded images
3. Click on any image to see details and URL

### Manage Storage
1. Go to **Settings** → **Upload**
2. Set up automatic backup
3. Configure image transformations
4. Set upload limits

### Get Image URLs
- All uploaded images get a unique URL
- Format: `https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.jpg`
- Use these URLs to display images in your app

## Next Steps

After setup:
1. Run the app
2. Take a photo
3. Watch it upload to Cloudinary (yellow → green indicator)
4. Check Cloudinary dashboard to see the uploaded image
5. Verify the image URL is saved to Firebase Firestore

