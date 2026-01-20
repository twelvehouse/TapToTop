# TapToTop

TapToTop brings the "Tap to Scroll" feature from iOS to Android. It allows you to scroll to the top of any application by tapping the area near the status bar.

## Overview

Android does not natively support a "tap to scroll up" gesture. While third-party apps like TapScroll previously offered this functionality, many have become incompatible with recent Android versions. TapToTop was created to provide a reliable solution for modern Android devices.

## How It Works

The app places an invisible overlay at the top of the screen, just below the status bar. When this area is tapped, the app uses Android's Accessibility Service to perform rapid upward swipe gestures. 

To ensure the list reaches the top, the app can execute multiple swipes in quick succession. This simulates the momentum needed for long lists.

## Features

You can customize the scrolling behavior through the following settings:

*   **Scroll Power (Repeat Count)**: Set the number of consecutive swipes triggered by a single tap.
*   **Scroll Speed (Duration)**: Adjust the duration of each swipe. A shorter duration creates faster and more powerful momentum.

## Setup Requirements

To function correctly, the app requires the following configurations:

1.  **Accessibility Service**: You must enable the TapToTop Accessibility Service in your device settings. This allows the app to detect taps on the status bar area and perform the scroll gestures on your behalf.
2.  **Battery Optimization**: For the service to run reliably in the background, please set the app's battery usage to "Unrestricted" in the system settings.
