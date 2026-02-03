# AttendifyPlus: Easy Release and Update Guide 🚀

This guide will show you exactly how to get your app onto your classmates' phones and how to send them updates when you fix bugs. Follow these steps carefully.

---

## PART 1: The "First Time" Release (Getting the App to Students)

Follow these steps to generate your first download link.

### Step 1: Start the Build on GitHub
1.  Open your web browser and go to your **GitHub Repository** ([https://github.com/Ciijay12/AttendifyPlus-Auto](https://github.com/Ciijay12/AttendifyPlus-Auto)).
2.  **Crucial Security Check:** Go to **Settings > General**, scroll to the bottom (**Danger Zone**), and ensure the repository is **PUBLIC**. If it is private, the download link will fail for students.
3.  Click the **"Actions"** tab at the top of the page.
4.  On the left-side menu, click **"Build & Release APK"**.
5.  Look for a gray button that says **"Run workflow"** on the right side. Click it.
6.  Click the green **"Run workflow"** button that appears.
7.  Wait about 5 minutes. You will see a yellow circle turning. When it turns into a **Green Checkmark (✅)**, your app is ready!

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
    *   **Increase `versionCode` by 1** (Example: If it was `6`, change it to `7`).
    *   **Change `versionName`** (Example: Change `"1.0.4"` to `"1.0.5"`).
3.  Click the **"Sync Now"** text that appears in the top right corner.

### Step 2: Upload your Code to GitHub (The Easy Way)
Instead of using the terminal, let's use Android Studio's simple interface.
1.  Go to the **"Commit"** tab on the left side of Android Studio. If you don't see it, go to **View > Tool Windows > Commit**.
2.  You will see a list of all the files you've changed.
3.  In the **"Commit Message"** box, type a short description of what you fixed (e.g., "Fixed teacher login bug").
4.  Click the **"Commit and Push..."** button.
5.  A new window will appear. Just click **"Push"**. This sends your code changes to GitHub.

### Step 3: Trigger the New Release
This is what actually builds the new APK file on GitHub.
1. Go to your **GitHub Repository** in your web browser.
2. Click the **"Actions"** tab.
3. Click **"Build & Release APK"** on the left.
4. Click the **"Run workflow"** button and then the green **"Run workflow"** button to confirm. 
5. Wait for the green checkmark (✅), just like in Part 1.

### Step 4: Notify the Phones (The Firebase Part)
This step makes the "Update Required" pop-up appear on everyone's phones. **You must do this after the new APK is built and released on GitHub.**

1.  **Get the New Download Link:**
    *   Go to your GitHub repository's main page.
    *   Click on the new version in the **"Releases"** section (e.g., `v1.0.5`).
    *   Scroll down to **Assets**, right-click `app-release.apk`, and **"Copy link address"**.

2.  **Create or Update the Firebase Node:**
    *   Go to the [Firebase Console](https://console.firebase.google.com/) and open your **Realtime Database**.
    *   Hover your mouse over the line that says **`config`**. A **plus icon (`+`)** will appear on the right. Click it.
    *   In the `Key` box, type **`update`**. Leave the `Value` box empty and click the larger **plus icon (`+`)** next to it to create an object.
    *   Now, add the following key-value pairs inside the `update` object:
        *   **`versionCode`**: The new number from Step 1 (e.g., `7`). **This MUST be higher than the previous version!**
        *   **`versionName`**: The new version name (e.g., `"1.0.5"`).
        *   **`downloadUrl`**: Paste the new APK link you just copied from GitHub.
        *   **`releaseNotes`**: Type a short message like "Fixed a bug where new teachers couldn't log in."
    *   Click the **"Add"** button.

Your app will now notify all users to download the update.

---

## 🛠️ Troubleshooting common issues

### "Permission Denied / 403 Error"
If Git asks for a login or denies access, ensure you are using the correct account:
1.  Open **Credential Manager** on your Windows computer.
2.  Go to **Windows Credentials**.
3.  Remove any entry starting with `git:https://github.com`.
4.  Go back to Android Studio and push again; it will ask you to sign in.

### "Download stuck at 100%"
This usually means the link is not a "Direct Link." Ensure you right-clicked the **`app-release.apk`** file specifically in GitHub Releases and selected "Copy link address." The link should end in `.apk`.

### "Blocked by Play Protect"
When installing, tell students to click **"Install Anyway"**. This is normal for private student projects.
