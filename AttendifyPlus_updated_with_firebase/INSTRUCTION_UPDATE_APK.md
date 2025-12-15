# How to Update the App (Step-by-Step)

This guide explains how to release a new version of AttendifyPlus to your users. 

There are two ways to do this:
1.  **The Manual Way (Easiest to start)**: You build the APK on your laptop and upload it yourself.
2.  **The Automated Way (Advanced)**: GitHub builds it for you (requires setup).

---

## PART 0: Setting up GitHub (First Time Only)

Before you can upload updates, you need a "Repository" (project folder) on GitHub.

1.  **Create an Account:**
    *   Go to [github.com](https://github.com/) and click **Sign up**.
    *   Follow the steps to verify your email.

2.  **Create a Repository:**
    *   Log in to GitHub.
    *   Click the **+** icon in the top-right corner (next to your profile picture).
    *   Select **New repository**.
    *   **Repository name:** Enter `AttendifyPlus-Releases` (or any name you like).
    *   **Public/Private:** Select **Public** (Important! If it's private, your users can't download the update without logging in).
    *   Check the box **Add a README file** (this helps initialize the project).
    *   Click **Create repository**.

3.  **Find the Releases Page:**
    *   On your new repository page, look at the right sidebar. You should see "Releases".
    *   **If you don't see "Releases"**: Look above your list of files for a link that says **"Tags"**. Click it, then click **"Create a new release"**.
    *   *Shortcut:* You can also go directly to `https://github.com/YOUR_USERNAME/YOUR_REPO_NAME/releases/new`.

---

## OPTION 1: The Manual Way (Recommended for now)

### Step 1: Update the Version Number
Every update needs a higher number than the last one, or the phone won't install it.

1.  Open Android Studio.
2.  In the Project view (left side), find `Gradle Scripts`.
3.  Double-click `build.gradle.kts (Module: :app)`.
4.  Scroll down to `defaultConfig`.
5.  Change `versionCode`: e.g., if it is `2`, change it to **`3`**.
6.  Change `versionName`: e.g., change `"1.0.0"` to **`"1.0.1"`**.
7.  Click the **"Sync Now"** elephant icon at the top right.

### Step 2: Build the APK File
1.  In the top menu bar, click **Build**.
2.  Select **Generate Signed Bundle / APK**.
3.  Choose **APK** and click **Next**.
4.  **Key Store Path:** Choose your `.jks` file (ask me if you lost it or need a new one).
5.  **Key Store Password:** Enter your password.
6.  **Key Alias:** Enter your alias (usually "key0").
7.  **Key Password:** Enter your password.
8.  Click **Next**.
9.  Select **release** (NOT debug).
10. Click **Create**.
11. Wait for the notification at the bottom right saying "Build Successful". Click **"locate"** to find the `app-release.apk` file.

### Step 3: Upload to GitHub
1.  Go to your new repository page on GitHub (created in Part 0).
2.  **CAN'T FIND RELEASES?**
    *   Look above the list of files (Code tab) for a small tag icon or the word **"Tags"**. Click it.
    *   Click the button **"Create a new release"**.
    *   *Or manually type this URL:* `https://github.com/[YOUR_USERNAME]/[REPO_NAME]/releases/new`
3.  **Draft a new release:**
    *   **Choose a tag:** Click the dropdown, type `v` followed by your version name (e.g., `v1.0.1`), and click **"Create new tag"**.
    *   **Release title:** E.g., "v1.0.1 - Fixed Bugs".
    *   **Description:** Type what changed (e.g., "Fixed login issue").
    *   **Attach binaries:** Drag and drop your `app-release.apk` file here. Wait for it to upload fully.
4.  Click the green **Publish release** button.

### Step 4: Copy the Link
1.  After publishing, you will see your release page.
2.  Under "Assets", find `app-release.apk`.
3.  **Right-click** on it and choose **Copy link address**.
    *   *Check:* Paste it in a new browser tab. It should immediately start downloading the file. If it opens a webpage, you copied the wrong link.

### Step 5: Tell the App to Update (Firebase)
1.  Go to [console.firebase.google.com](https://console.firebase.google.com/).
2.  Open your project **AttendifyPlus**.
3.  On the left menu, click **Build** > **Realtime Database**.
4.  In the data tree, find the **`config`** folder. Click `+` to open it.
5.  Find the **`update`** folder. If it doesn't exist, create it.
6.  Change the values:
    *   **`versionCode`**: Change this to **`3`** (or whatever number you used in Step 1).
    *   **`versionName`**: Change to `"1.0.1"`.
    *   **`downloadUrl`**: Paste the link you copied from GitHub in Step 4.
    *   **`releaseNotes`**: Type a message for your users (e.g., "New update available!").

**Done!** As soon as you change that `versionCode` in Firebase, every app connected to the internet will pop up the "Update Available" dialog.

---

## OPTION 2: The Automated Way (GitHub Actions)

*This allows GitHub to automatically build the APK when you push a tag.*

### 1. Set up Secrets
1.  Go to your GitHub Repo > **Settings** > **Secrets and variables** > **Actions**.
2.  Add a New Repository Secret named `SIGNING_KEY_BASE64`.
    *   *How to get this:* Open PowerShell on your PC and run: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("path/to/your/keystore.jks"))`. Copy the giant text block.
3.  Add `KEY_STORE_PASSWORD`.
4.  Add `KEY_ALIAS`.
5.  Add `KEY_PASSWORD`.

### 2. Create the Workflow File
1.  In your project code, create a file at: `.github/workflows/android-release.yml`.
2.  Paste this code:

```yaml
name: Build & Release APK

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Decode Keystore
        run: echo ${{ secrets.SIGNING_KEY_BASE64 }} | base64 -d > app/upload-keystore.jks

      - name: Build Release APK
        run: ./gradlew assembleRelease
        env:
          SIGNING_KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          SIGNING_KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          SIGNING_STORE_PASSWORD: ${{ secrets.KEY_STORE_PASSWORD }}

      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: app/build/outputs/apk/release/app-release.apk
```

### 3. How to use it
1.  Update `versionCode` in `build.gradle.kts`.
2.  Commit and push your code.
3.  Create a tag in Git: `git tag v1.0.2` then `git push origin v1.0.2`.
4.  GitHub will automatically build the APK and create the release.
5.  You just need to copy the link and put it in Firebase (Step 5 above).
