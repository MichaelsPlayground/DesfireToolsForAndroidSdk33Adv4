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

After a successful connection to the card this fragment will list all applications on the card. The first application is always the 
"Master File Application" that has the fixed application identifier ("AID") "0x000000". As long as the app has a connection to the card there are 
green borders; if the connection gets lost the color changes to red.

Below each AID the type of access keys (3DES, 3K3DES or AES) is displayed. The total number of access keys is shown as well.

Clicking on an entry will open the File List Fragment. Using the options menu there are 3 more options: "add application", "free memory" and "format PICC".

### files list fragment

The are two main parts in this fragment:

**Files**: All files are listed in this section. These 4 file types are available here:
- a) Standard files
- b) Value files
- c) Linear Record files
- d) Cyclic Record files
For each file the communication setting (Open communication ("Plain"), Open communication secured with a MAC ("MACed") and 
Enciphered communication ("Encrypted")) is displayed.

The total number of access keys is show at last.

A click on an entry opens a **key selector** to choose which key ("R&W", "R" or "W" should be used for authentication). The second part 
is the selection of the stored key to run this action. After a successful authentication the File fragment is shown.

**Access keys**: The access key below are taken from the key store an can be used to test an authentication.

Using the option menu we can add a file or get the free memory on the card.

At the top of the fragment there are 2 buttons: change application key settings and change application key.

### add an application fragment

There are 6 number pickers that allow to select an application id that has 6 hexadecimal digits. It is not allowed to select "000000" as 
this is the already available Master File Application. If an application is existing we cannot overwrite the application and the 
creation will fail.

The second option is the type of keys used for this application (DES, TDES or AES). The third option is the number of key(1 to 13) available 
for this application.

Note: the specification allows to set "key number" 14 (free access without authentication) and "key number" 15 (no access allowed) but this 
fragment does not support those keys.

Second note: To create an application there is an additional parameter neccessary: **Key Settings**. The parameter is fixed to the value "0x0F" means:

The application master key can be changed, Get FID list, Get File Settings and Get Key settings are without application master key, Create and 
Delete file are permitted without application master key and this settings can be changed.

### file fragment

This fragment reads the content of the file (after a successful authentication). Depending on the files type different data is displayed:
- Standard file: The content is shown in hex string encoding and additionally converted to an UTF-8 encoded string. Additionally 
the size of the file is displayed.
- Value file: the actual value, lower and upper limit and the limited credit option together with the limited credit value is shown.
- Linear Record file: beneath the content of each record in hex string encoding the fragment informs about the record size in bytes, 
the maximum number of records and the current records
- Cyclic Records file: beneath the content of each record in hex string encoding the fragment informs about the record size in bytes, 
the maximum number of records and the current records. Note that the Cyclic record file does need a spare record for writing, means 
that there are only "Max records - 1" individual records.

In the second part the Access key number are shown for "R&W", "R", "W" and "CAR" keys.

On the top of the fragment a button allows to write to this file. 

### file write fragment

At the moment the support the writing to these file types only: Standard, Linear Record and Cyclic Record files. The writing to a value file
("credit" and "debit") will be available in a future version, sorry.

As a simple and open source hex editor is not available for Java I'm providing the writing of a string as only option. If the data is shorter  
than the file or record size the string is filled up with blanks. If the data is longer than the file/record size the data is truncated.

### change application settings

...

### change application key

...


### free memory option

A toast will be shown that displays the free memory on the card.

### format PICC option

The format PICC option is not directly executed - a confirmation dialog asks is the format command is executed. The format command will delete all 
applications (except for the Master File Application) and files from the card and releases the memory to factory settings.

Note: this command will fail if the card has an activated card configuration option "Disable card formatting". If this bit is set there is 
no way back. The second card option is a randomized UID, if this option is set there is no way back to the programmed UID.
Note, you can only set these bits! Once set they are set, tough!


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
