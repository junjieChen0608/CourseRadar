# CourseRadar (Version v0.3.0)
This is a senior design software project from team Tit4Tat. 
Basically, it is an android application that 
UB students can view and share course informations, professor evaluation, syllabus abstractions. 

## Limitation
Our project only gathered course scheduling data of lecture and seminar of 2017-2018 academic year. 

## Usage

### For Developers

#### Environment:
JDK version: 1.8 (Please follow the instruction on Internet to install and setup environment)

Android Studio version: [2.3.3](https://developer.android.com/studio/index.html). 

Android SDK version: 21 (Android 5.0.0) to 26 (Android 8.0). 

A Google account that linked to Firebase.

#### Become a collaborator
To properly become a collaborator of our project and access to our Firebase backend, please send us your email that is correctly
linked to Firebase to [czhang43@buffalo.edu](mailto:czhang43@buffalo.edu), we will evaluate and get back to you ASAP.

### Get our project
After you become a collaborator, you can load our project.
1. Clone our project from Github 
```
git clone https://github.com/junjieChen0608/CourseRadar.git
```
2. Load our project to Android Studio, update all plugin if necessary

#### Mac OS X users please read
For some extreme cases, you need to set up Google maven to correctly resolve Firebase dependency

Add maven.google.com to the Maven repositories in your module-level build.gradle(i.e., Project: CourseRadar)
```
    allprojects {
        repositories {
            jcenter()
            maven { url 'https://maven.google.com' }
        }
    }
```

### For all platforms
Add the following line to the end of your app-level build.gradle(i.e., Module: app)
```
    apply plugin: 'com.google.gms.google-services'
```

#### Setup Firebase dependency and link Firebase to project in Android Studio
After you config Maven(if necessary) and gms google service, you have to setup Firebase for the project in Android Studio. It needs Authentication, Real-time Database,and Storage.
1. From the up-right corner of Android Studio, log in your email account that we granted access to our Firebase 
2. From top navigation tab of Android Studio, click Tools -> 
Firebase-> 
Authentication-> 
click the drop down item-> 
click "Connect to Firebase"-> 
log in with your Google account which is correctly linked to Firebase-> 
click "Choose an existing Firebase or Google Project"-> 
choose "CourseRadar"-> 
click "Connect to Firebase"-> 
click "sync"

#### Other SDKs or OS images
You might need to update the latest SDK platforms, SDK tools, or Android system images to properly run our app, often times Android Studio will popup suggestions
for you.

### For Users
1, Make sure your have an Android smartphone, and it is running Android 5.0.0 or above.

2, Please download the APK file from [here](https://firebasestorage.googleapis.com/v0/b/courseradar.appspot.com/o/CourseRadar-MVP.apk?alt=media&token=bfcb020f-54ca-4f50-8256-2bca5bbdab9e) TO YOUR PHONE.

3, During download, your browser may warn you about potential risk of downloading APK file from unknown source, just ignore it and continue downloading! We mean you no harm :)

4, After APK is downloaded, navigate to the directory where you save the APK file and install it, again, your smartphone's OS might request permission to install APK from unknown source, please grant permission to proceed

5, You are all set! Our app is now installed! Enjoy!

## Team Member

Binyuan Deng

Cheng Zhang

Dixin Chen

Junjie Chen

Zhenkang Yang

## Project Contributor

Xuanyu Dong

Liangying Chen
