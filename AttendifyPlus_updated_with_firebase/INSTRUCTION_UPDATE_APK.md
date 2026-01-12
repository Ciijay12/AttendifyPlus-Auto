# AttendifyPlus: Easy Release and Update Guide 🚀

This guide will show you exactly how to get your app onto your classmates' phones and how to send them updates when you fix bugs. Follow these steps carefully.

---

## PART 1: The "First Time" Release (Getting the App to Students)

Follow these steps to generate your first download link.

### Step 1: Start the Build on GitHub
1.  Open your web browser and go to your **GitHub Repository** ([https://github.com/ciiiidung/AttendifyPlus-Auto](https://github.com/ciiiidung/AttendifyPlus-Auto)).
2.  Click the **"Actions"** tab at the top of the page.
3.  On the left-side menu, click **"Build & Release APK"**.
4.  Look for a gray button that says **"Run workflow"** on the right side. Click it.
5.  Click the green **"Run workflow"** button that appears.
6.  Wait about 5 minutes. You will see a yellow circle turning. When it turns into a **Green Checkmark (✅)**, your app is ready!

### Step 2: Get your Download Link
1.  Go back to the main page of your repository (click the **"Code"** tab at the top left).
2.  On the right side of the screen, look for the **"Releases"** section and click on the latest version (e.g., `v1.0.0`).
3.  Scroll down to the bottom where it says **"Assets"**.
4.  Find the file named `app-release.apk`.
5.  **Right-click** on that name and select **"Copy link address"**. This is your download link!

### Step 3: Give the Link to Students
*   **Option 1:** Paste that link into your Messenger group chat. Students just have to click it to download.
*   **Option 2:** Go to a website like `qr-code-generator.com`, paste the link, and print the QR code for students to scan.

---

## PART 2: How to Push a "New Update" (Fixing Bugs)

Whenever you change code in Android Studio and want everyone to have the new version, follow these exact steps.

### Step 1: Change the Version (Crucial!)
Android phones will not install an update if the version is the same as before.
1.  In Android Studio, open the file: `app` > `build.gradle.kts (Module :app)`.
2.  Find the lines for `versionCode` and `versionName`.
    *   **Increase `versionCode` by 1** (Example: If it was `1`, change it to `2`).
    *   **Change `versionName`** (Example: Change `"1.0.0"` to `"1.0.1"`).
3.  Click the **"Sync Now"** text that appears in the top right corner.

### Step 2: Upload your Code to GitHub
1.  At the bottom of Android Studio, click the **"Terminal"** tab.
2.  Type these three commands one by one (press Enter after each):
```powershell
git add .
git commit -m "I fixed the login bug"
git push origin master
```

### Step 3: Trigger the New Build
1.  Go back to your **GitHub Actions** tab in your browser.
2.  Click **"Build & Release APK"** and click **"Run workflow"** again.
3.  Wait for the **Green Checkmark (✅)**.

### Step 4: Notify the Phones (Firebase)
This step makes the "Update Available" pop-up appear on everyone's phones.
1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Click on your project, then click **Realtime Database** on the left.
3.  Find the folder named **`config`**, then **`update`**.
4.  Change these 4 values to match your new version:
    *   **`versionCode`**: The new number you typed in Step 1 (e.g., `2`).
    *   **`versionName`**: The new name (e.g., `1.0.1`).
    *   **`downloadUrl`**: Copy the NEW link from the GitHub Releases page.
    *   **`releaseNotes`**: Type a short message like "Fixed bugs and improved speed."

---

## 💡 Pro-Tip for Installation
When students install the APK for the first time, their phone might say "Blocked by Play Protect." Tell them to click **"Install Anyway"**. This happens because your app is a private project and not yet on the official Google Play Store.
