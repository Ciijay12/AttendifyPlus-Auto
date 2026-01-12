# AttendifyPlus: Release and Update Guide

This guide covers how to distribute the app for the first time and how to send updates later.

---

## PART 1: Initial Rollout (First Time Installation)
Follow these steps to give the app to your students and teachers for the first time.

### Step 1: Build the First APK
1.  Open your **GitHub Repository** in your web browser.
2.  Click the **Actions** tab.
3.  On the left sidebar, click **"Build & Release APK"**.
4.  Click **Run workflow ▼** > **Run workflow**.
5.  Wait for the **Green Checkmark ✅** (about 5 minutes).

### Step 2: Get the Download Link
1.  Click the **Code** tab, then click **Releases** on the right side.
2.  Click the latest version (e.g., `v1.0.0`).
3.  Under **Assets**, **Right-click** on `app-release.apk` and select **Copy link address**.

### Step 3: Distribute to Students
Since the app is not on the Play Store, you can share it in two ways:
*   **Option A (Direct Link):** Paste the link you copied into your class group chat (Messenger/GCash/etc.).
*   **Option B (QR Code):** Go to a "URL to QR Code" website, paste the link, and print the QR code. Students can scan it to download the app directly.

### Step 4: Student Installation Instructions
Tell your students to follow these steps to install:
1.  Download the APK file.
2.  Open the file. If a security warning appears ("Blocked by Play Protect" or "Unknown Sources"), click **Settings**.
3.  Turn **ON** "Allow from this source" or "Install unknown apps."
4.  Go back and click **Install**.

---

## PART 2: How to Release a New Update (Automated)
Follow this guide whenever you fix a bug or add a feature and want to send an update to your users.

### Step 1: Update the Version Number
Android phones will **refuse** to install an update if the `versionCode` is not higher than the currently installed version.

1.  Open Android Studio.
2.  Open `build.gradle.kts (Module: :app)`.
3.  In the `defaultConfig` block:
    *   **Increment `versionCode`**: If it is `3`, change it to **`4`**.
    *   **Update `versionName`**: Change `"1.0.0"` to `"1.0.1"`.
4.  Click **"Sync Now"** at the top right.

### Step 2: Save Your Changes to GitHub
1.  Open the **Terminal** in Android Studio.
2.  Run these commands:
```powershell
git add .
git commit -m "Description of your changes"
git push origin master
```

### Step 3: Build the APK
1.  Go to your GitHub **Actions** tab.
2.  Run the **"Build & Release APK"** workflow again.
3.  Wait for the **Green Checkmark ✅**.

### Step 4: Get the New Download Link
1.  Go to **Releases** on GitHub.
2.  Copy the link address of the new `app-release.apk`.

### Step 5: Notify Users (Firebase)
1.  Go to your **Firebase Console** > **Realtime Database**.
2.  Go to `config` > `update`.
3.  Update the **`versionCode`**, **`versionName`**, **`downloadUrl`**, and **`releaseNotes`**.

**Success!** The next time students open their app, they will see a pop-up asking them to update.
