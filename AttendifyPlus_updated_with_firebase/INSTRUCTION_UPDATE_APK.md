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
Your current version is `8`. For the next update, you will change it to `9`.
1.  In Android Studio, open the file: `app` > `build.gradle.kts (Module :app)`.
2.  Find the lines for `versionCode` and `versionName`.
    *   **Increase `versionCode` by 1** (Example: Change `8` to `9`).
    *   **Change `versionName`** (Example: Change `"1.0.7"` to `"1.0.8"`).
3.  Click the **"Sync Now"** text that appears in the top right corner.

### Step 2: Upload your Code to GitHub (Using the Terminal)
1.  At the bottom of Android Studio, click the **"Terminal"** tab to open the command line.
2.  Type the following command and press Enter. This prepares all your changed files to be saved.
    ```powershell
    git add .
    ```
3.  Next, type this command and press Enter. This saves a snapshot of your changes to your computer. **Replace the message in quotes with a real description of your fix.**
    ```powershell
    git commit -m "Your fix description here"
    ```
4.  Finally, type this command and press Enter. This uploads your saved changes to GitHub.
    ```powershell
    git push
    ```

> **What if I only `commit` but don't `push`?**
> No problem. Your work is saved locally. Just type `git push` in the terminal to upload it. You can also go to the **Git > Push...** menu at the top of Android Studio.

### Step 3: Trigger the New Release
This is what actually builds the new APK file on GitHub. The action is triggered by pushing a new version "tag".

1.  In the same terminal window, type the following command, making sure the version matches what you put in `build.gradle.kts` (e.g., `v1.0.8`). Press Enter.
    ```powershell
    git tag v1.0.8
    ```
2.  Now, push that tag to GitHub. This will automatically start the "Build & Release APK" action.
    ```powershell
    git push origin v1.0.8
    ```
3.  You can go to the **"Actions"** tab on GitHub to watch for the green checkmark (✅).

> **Error: "tag already exists"?**
> If you get this error, it means you used that tag name before. Run these two commands to delete the old tag from your computer and from GitHub, then try Step 3 again.
> ```powershell
> git tag -d v1.0.8
> git push --delete origin v1.0.8
> ```
> *(Remember to replace `v1.0.8` with the correct version!)*

### Step 4: Notify the Phones (The Firebase Part)
This step makes the "Update Required" pop-up appear on everyone's phones. **You must do this after the new APK is built and released on GitHub.**

1.  **Get the New Download Link:**
    *   Go to your GitHub repository's main page.
    *   Click on the new version in the **"Releases"** section (e.g., `v1.0.8`).
    *   Scroll down to **Assets**, right-click `app-release.apk`, and **"Copy link address"**.

2.  **Create or Update the Firebase Node:**
    *   Go to the [Firebase Console](https://console.firebase.google.com/) and open your **Realtime Database**.
    *   Find your **`config`** node. If you see an `update` sub-folder, click it. If not, create it by hovering over `config` and clicking the **plus icon (+)**.
    *   Set the following key-value pairs inside the `update` object:
        *   **`versionCode`**: The new number from Step 1 (e.g., `9`). **This MUST be higher than or equal to the app's current version!**
        *   **`versionName`**: The new version name (e.g., `"1.0.8"`).
        *   **`downloadUrl`**: Paste the new APK link you just copied from GitHub.
        *   **`releaseNotes`**: Type a short message describing your latest fix.

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
