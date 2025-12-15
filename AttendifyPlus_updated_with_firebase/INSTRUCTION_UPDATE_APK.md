# How to Release a New Update (Automated)

Congratulations! You have set up a fully automated system. 
Follow this guide whenever you fix a bug or add a feature and want to send an update to your users.

---

## Step 1: Update the Version Number
Android phones will **refuse** to install an update if the `versionCode` is not higher than the currently installed version.

1.  Open Android Studio.
2.  In the Project view (left side), find `Gradle Scripts`.
3.  Double-click `build.gradle.kts (Module: :app)`.
4.  Scroll down to the `defaultConfig` block.
5.  **Increment `versionCode`**:
    *   Example: If it is `3`, change it to **`4`**.
6.  **Update `versionName`** (Optional but recommended):
    *   Example: Change `"1.0.0"` to `"1.0.1"`.
7.  Click the **"Sync Now"** elephant icon at the top right corner.

---

## Step 2: Save Your Changes to GitHub
You need to send your latest code (including the version change) to GitHub.

1.  Open the **Terminal** tab in Android Studio (bottom).
2.  Run these commands one by one:

```powershell
git add .
git commit -m "Fixed bugs and ready for update"
git push origin master
```

*Note: Replace the message in quotes with whatever you actually changed.*

---

## Step 3: Build the APK (The Magic Button)
You do **not** need to build the APK manually. GitHub will do it for you.

1.  Open your **GitHub Repository** in your web browser.
2.  Click the **Actions** tab at the top of the screen.
3.  On the left sidebar, click **"Build & Release APK"**.
4.  On the right side of the blue bar, click the gray **Run workflow ▼** button.
5.  Click the green **Run workflow** button inside the dropdown.

**Now Wait:**
*   You will see a **Yellow Circle** appear. This means it is building.
*   Wait about **3 to 5 minutes**.
*   When it turns into a **Green Checkmark ✅**, it is done!

---

## Step 4: Get the Download Link
1.  On the GitHub page, click the **Code** tab (top left).
2.  Look at the right sidebar and click **Releases**.
3.  Click on the latest version (e.g., `v1.0.1`).
    *   *Note: The system automatically generates the version tag for you.*
4.  Scroll down to the **Assets** section.
5.  **Right-click** on `app-release.apk` and select **Copy link address**.

---

## Step 5: Notify Users (Firebase)
This is the switch that makes the "Update Available" dialog appear on everyone's phone.

1.  Go to [console.firebase.google.com](https://console.firebase.google.com/).
2.  Open your project **AttendifyPlus**.
3.  On the left menu, click **Build** > **Realtime Database**.
4.  Navigate to `config` > `update`.
5.  Update these values:
    *   **`versionCode`**: Change this to the number you set in Step 1 (e.g., `4`).
    *   **`versionName`**: Change this to the name you set in Step 1 (e.g., `1.0.1`).
    *   **`downloadUrl`**: Paste the link you copied from GitHub.
    *   **`releaseNotes`**: Type a message (e.g., "Fixed login bug and improved speed.").

**DONE!** 🚀
As soon as you hit Enter in Firebase, all users with the app open will receive the update prompt.
