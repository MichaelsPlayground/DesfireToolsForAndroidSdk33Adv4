# desfire-tools-for-android
A collection of tools for interaction with [MIFARE DESFire EV1] NFC tags using Android, mostly adapted from [libfreefare] and [nfcjlib].

## Notes from MichaelsPlayground & AndroidCrypto

Note: This repository was forked from the original source available at:

https://github.com/skjolber/desfire-tools-for-android by Thomas Skjølberg (skjolber)

I changed as less as necessary to get the app run on modern Android SDK's - this app is running on SDK 33 and 
Gradle version 7.4.2. I know that there are a lot of "deprecated" notices in the source code but that could be 
the task for further enhancements. All credits are going to Thomas Skjølberg who created this fine piece of 
app in helping us to work with Mifare DESFIRE EV1 cards (b.t.w. the app is working on DESFire EV2 and EV3 as well :-).

One note regarding the file "MifareDesfireKey1.java" located in com.github.skjolber.desfire.libfreefare. The file 
equals to "MifareDesfireKey.java" in the original repository but as there is another file named "MifareDESFireKey.java"
(see the capitol letters "DES" compared to "Des") only one file can exist in a MacOS file system (I don't know about Windows, sorry). 
I renamed the file from "MifareDesfireKey.java" to "MifareDesfireKey1.java" and changed all references in the code to the 
new file name.

Second note: I included the "libfreefare" and "model" libraries direct into my package so they are no included within 
the build.gradle (app) file.

## Status of this app

### Key management module

This module stores keys needed for authentication. There are 4 key types available: DES (8 bytes long), 
3DES (Triple DES key with 16 bytes length), 3K3DES (Triple DES key with 24 bytes length) and AES (AES-128, 16 bytes long).

As a new key in an application is a key filled with 0x00's there are 4 Null/Default keys that are of 
0's only.

Using the menu you create **4 AES keys** named for their purpose:
- AES 0 RW: should be used as key for "Read & Write" access
- AES 1 CAR: should be used as key for "Change Access Rights"
- AES 2 R: should be used as key for "Read" access
- AES 3 W: should be used as key for "Write" access

### start fragment ("scan DESFIRE EV1 tag")

This is the starting point for all activites with a DESFire EV1/EV2/EV3 card. **This app does not work on DESFire light tags.**

When a DESFire EVx tag is found the app changes to the Application List fragment

### application list fragment






## The original description follows...

The original app in Google PlayStore: https://play.google.com/store/apps/details?id=com.skjolberg.mifare.desfiretool&hl=no

Features:
  * [MIFARE DESFire EV1] tag model
  * Encryption support
    * AES
    * (3)DES 
    * 3K3DES
  * [Mifare Desfire Tool] demo application

As NXP now has a freely available [TapLinx] SDK for supporting these cards, so this project is mostly for educational and/or debugging purposes.

## Licenses
For following licenses apply

  * nfcjlib - [Modified BSD License (3-clause BSD)]
  * libfreefare - [LGPL 3.0 with classpath exception]
  * everything else - [Apache 2.0]

# Obtain
The project is based on [Gradle].

# Usage
See the example application.

# History
 - 1.0.0: Initial version

[Gradle]:                               https://gradle.org/
[Apache 2.0]:          		            http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:       		            https://github.com/skjolber/desfire-tools-for-android/issues
[Modified BSD License (3-clause BSD)]:  nfcjlib/LICENSE
[LGPL 3.0 with classpath exception]:    libfreefare/LICENSE
[Mifare Desfire Tool]:          		https://play.google.com/store/apps/details?id=com.skjolberg.mifare.desfiretool&hl=no
[TapLinx]:                              https://www.mifare.net/en/products/tools/taplinx/
[MIFARE DESFire EV1]:                   https://en.wikipedia.org/wiki/MIFARE#MIFARE_DESFire_EV1_(previously_called_DESFire8)
[libfreefare]:                          https://github.com/nfc-tools/libfreefare
[nfcjlib]:                              https://github.com/Andrade/nfcjlib

NumberPicker https://github.com/ShawnLin013/NumberPicker

dependencies in build.gradle (app):
```plaintext
https://github.com/Kaopiz/android-segmented-control
 used for implementation 'info.hoang8f:android-segmented:1.0.6' 
 
https://github.com/ShawnLin013/NumberPicker for the numberPicker (included in source code), MIT license

http://www.fampennings.nl/maarten/android/09keyboard/index.htm for Custom Keyboard (accepting hex character only)
```

settings.gradle:
```plaintext
rootProject.name = "DesfireToolsForAndroidSdk33Adv2"
include ':app'
include ':keyboard'

```

## Changes by MichaelsPlayground & AndroidCrypto

1) de.androidcrypto.desfiretoolsforandroidsdk33.FileFragment.java: show content of StandardFile in hex and string representation
2) xx
