# AndroidBlanky
Creating a new blank Android app is a pain and takes time. Creating this blank Android project should be the last time I have to do it.

## Generate app icons

* Create a 128x128px icon and put into `fastlane/metadata/icons/app_icon.png`. Then run `fastlane app_icon` to generate them.

How to use project:  

* Copy project to a directory of your choice. (`cp -R AndroidBlanky/ newProjectName/`)
* `rm -rf .git/` to remove history for AndroidBlanky and `git init; git config user.email "you@foo.com"; git config user.name "First Last"` to create a new git history. `git add .; git commit -m "Initial commit. Create project from template."` to get new history completed.
* Open up project in Studio and all should be golden. Now continue on to below steps to get it compiling and running...
* Edit namespace of app in manifest file and build.gradle file to not be 'com.levibostian.andoidblanky'. You will now also need to rename the source code directories. To do this, open up your source code project explorer in Studio (the place where you view your source code tree). Click the drop down arrows to `app/src/main/` and there you will see `com.levibostian.androidblanky`. Click on `com.levibostian.androidblanky` once and then look in the top right corner of the source project project explorer window (the little panel you are playing around with). There is a little gear icon for settings. Click that little gear icon and then click "Compact Empty Middle Packages". What this will do is make `com.levibostian.androidblanky` split up to show each directory individually. Then you can right click on `levibostian` > refactor > rename package (when you rename, it will take a few minutes of Studio frozen. Just wait, it will work.) and also rename `androidblanky` using the same method. (This help came from [this stackoverflow](http://stackoverflow.com/a/27677033/1486374) post.)
* Go into `app/proguard-rules.pro` and towards the top, you will see 2 rules for models and vos. Change the namespace from `com.levibostian.androidblanky` to your namespace you changed above.
* Build > Clean Project. Build > Rebuild Project.
* Create a new file `app/fabric.properties` and inside of it, add your org's Fabric api key and secret:

```
#Contains API Secret used to validate your application. Commit to internal source control; avoid making secret public.
#Mon Jan 16 18:43:59 CST 2017
apiSecret=secret_here
apiKey=key_here
```

* If calling an API, then go into `src/main/java/service/` and edit the name of `GitHubApi.java` to be your API name. If not calling an API, go into the manifest file and delete the INTERNET permission request.
* In production/strings.xml it says AndroidBlanky. Change name your app name.
* Go into Android Studio preferences > Compiler > Command line args paste `-PminSdk=23` or whatever min SDK you want to compile with for dev testing.

When creating build to release to play store:  

1. (If you have not created a keystore file yet) Run command `keytool -genkey -v -keystore KEYSTORE-NAME.keystore -alias KEYSTORE-ALIAS -keyalg RSA -validity 10000 -storepass PASSWORD1 -keypass PASSWORD2` (replacing KEYSTORE-NAME to name of your app, KEYSTORE-ALIAS to name of your app, PASSWORD1 to a password used for your keypass password and PASSWORD2 to another password for your keypass password) *NOTE: do not lose the file that this command creates. You will not be able to generate another one!*
2. Edit the environment variable to point to the path you are storing the keystore file you made above: `export ANDROID_KEYSTORE=/path/to/keystore/file/created/above`. The default is: `export ANDROID_KEYSTORE=~/.android/debug.keystore` for creating debug builds.
3. Edit the environment variable for your alias name: `export ANDROID_KEYALIAS=aliasnameyousetwhencreatingalias`. Default is: `export ANDROID_KEYALIAS=androiddebugkey` for creating debug builds.
4. Go into the `build.gradle` file and uncomment the signingConfigs{} variables. 
5. `./gradlew assembleRelease` This creates the APK files. Go to  `app/build/outputs/apk` to view them all after this command is complete. 
6. Sign the APK: `jarsigner -verbose -keystore KEYSTORE-PATH APK-PATH-CREATED-ABOVE KEYSTORE-ALIAS`
7. Verify the signature: `jarsigner –verbose -keystore KEYSTORE-PATH –verify APK-PATH-CREATED-ABOVE`
8. Zip Align the APK: `zipalign -v 4 UNALIGNED-SIGNED-APK.apk RELEASE_APK-NAME.apk` 
9. [Publish](https://play.google.com/apps/publish) that bad boy.

# Tests

* Run JUnit unit tests:

`fastlane test`

* Run Android Espresso tests and take screenshots:

`fastlane android_test`

# Credits

* [MockitoExample](https://github.com/JeroenMols/MockitoExample) for helping get Mockito v2 working.
* [u2020](https://github.com/JakeWharton/u2020) for inspiration behind some of the project structure. This project helped setup my build.gradle files a ton!
* [clean-status-bar](https://github.com/emmaguy/clean-status-bar) for app to create a clean status bar intent.