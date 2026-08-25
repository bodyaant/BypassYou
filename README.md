
<div align="center">

  # BypassYou

  **BypassYou** is an Android application built with **Jetpack Compose** and **Material 3 Expressive** design. It allows specified **VIP contacts** to bypass the phone's silent, vibrate, or Do Not Disturb (DND) modes with an emergency audible ringtone, ensuring you never miss critical calls.

  <p align="center">
    <a href="https://github.com/bodyaant/BypassYou/releases/latest">
      <img src="https://img.shields.io/github/v/release/bodyaant/BypassYou?logo=github&labelColor=1a1a1a&color=6750A4&style=flat-square" alt="Latest Release">
    </a>
    <a href="https://github.com/bodyaant/BypassYou/releases">
      <img src="https://img.shields.io/github/downloads/bodyaant/BypassYou/total?logo=github&labelColor=1a1a1a&color=625B71&style=flat-square" alt="Total Downloads">
    </a>
    <a href="https://github.com/bodyaant/BypassYou/stargazers">
      <img src="https://img.shields.io/github/stars/bodyaant/BypassYou?logo=github&labelColor=1a1a1a&color=E0B6FF&style=flat-square" alt="Stars">
    </a>
    <a href="LICENSE">
      <img src="https://img.shields.io/github/license/bodyaant/BypassYou?logo=gnu&labelColor=1a1a1a&color=blue&style=flat-square" alt="License">
    </a>
    <img src="https://img.shields.io/badge/Android-14.0%2B_(API_34%2B)-3DDC84?logo=android&logoColor=white&labelColor=1a1a1a&style=flat-square" alt="Android 8.0+">
  </p>

  <p align="center">
    <a href="https://github.com/bodyaant/BypassYou/releases/latest">
      <img src="https://img.shields.io/github/v/release/bodyaant/BypassYou?label=Download%20BypassYou&style=for-the-badge&color=6750A4&logo=android&logoColor=white" alt="Download BypassYou">
    </a>
  </p>
  <p align="center">
    <a href="https://boosty.to/bodyaant">
      <img src="https://img.shields.io/badge/Boosty-Support_the_author-ff6f61?logo=boosty&logoColor=white" width="200">
    </a>
  </p>

</div>

---

## Screenshots

<p align="center">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="35%"> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="35%">
</p>


---

## Features

- **Non-Destructive Sound Bypass**: Plays alarms safely via `AudioAttributes.USAGE_ALARM` without altering or messing up system ringer/notification volume sliders on MIUI, OneUI, or AOSP.
- **Acoustic Loudness Curve**: Natural volume progression.
- **VIP Whitelist Management**: Add contacts directly from the system phonebook or enter numbers manually with format tolerance.
- **Material 3 Expressive UI**: Simple and clean user interface.
- **Background Reliability**: Integrated `CallScreeningService` (Android 10+) and persistent foreground service to prevent aggressive OEM background killing.

## Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/) (Coroutines, Flow)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Expressive
- **Storage**: Clean JSON-backed shared preferences repository with synchronization.

## License

BypassYou - Minimalist, bypass-the-sound-off application for Android.  
Copyright (C) 2026 bodyaant

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
