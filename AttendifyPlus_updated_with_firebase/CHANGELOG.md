# Changelog

## Unreleased (Ready for next update)

### Fixes
- **Login Credentials:** Changed the default username for new students to be their **Student ID** (instead of First Name) to prevent duplicate username issues.
- **Multi-Device Login Loop:** Fixed a bug where logging in on a new device would repeatedly ask for credential updates even if they were already changed on another device. This now forces a fresh check from the server upon login.
- **Data Safety:** Removed destructive database migration fallback to prevent accidental data loss during app updates.

### Improvements
- **Advisory Classes:** Added support for **combined strands** (e.g., "STEM & ABM") in a single advisory class.
- **UI Adjustments:** Fixed status bar padding in the Debug Settings screen to match other admin screens.
- **Teacher Instructions:** Updated the "Add Student" dialog to clearly inform teachers that the default username/password is the Student ID.

---

## v1.0.0 (Initial Automated Release)
- Initial release with automated GitHub Actions workflow.
