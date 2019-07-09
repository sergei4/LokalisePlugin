
# LokalisePlugin

This is a simple plugin which helps with the integration of [Lokalise](https://lokalise.co) localization service into Android App. 
This plugin has two main purposes:
* update local project strings files with latest translations from Lokalise backend
* upload local project strings to Lokalise backend

## Getting Started
### Setup

To enable this plugin, add the following lines in your project `build.gradle`

```
buildscript {  
  repositories {  
        .... 
        maven { url 'https://jitpack.io' }  // you need Jitpack repository
  }  
  
  dependencies {  
         .......
         classpath 'com.github.sergei4:lokaliseplugin:1.7.1' // latest version of plugin goes here
  }  
}
```
And in your app module `build.gradle` add this:

```
apply plugin: 'me.eremkin.lokalise-plugin' // apply plugin
......
// and configure it
lokalise {  
    api {  
        projectId = "123456789" // t
        token = "token"  // you need token with read and write permissions
    }  
  
    translationsUpdateConfig {
        resPath = "$rootDir/android/src/main/res" // by default used "main" android sourceSet

        langs {
            en {
                lokaliseLang = "en"
                updateStrategy = "merge"
            }
            ru {
                androidLang = "ru"
                lokaliseLang = "ru"
                updateStrategy = "replace"
            }
        }
    }

    //Here you can specify which files you want to upload
    uploadEntry {
        path = "$rootDir/android/src/main/res/values/strings.xml"
        lang = "en_US"
    }
    uploadEntry {
        path = "$rootDir/android/src/main/res/values-ru/strings.xml"
        lang = "ru"
    }
}
```
To get `projectId` go to your Project/Settings/General 
To get read/write `token` read this [documentation](https://docs.lokalise.co/faqs/api-tokens)

After this configuration you should be able to use those 2 gradle tasks `uploadAndroidStrings` and `downloadAndroidStrings`  you can execute them from IDE GUI or command line.
``` ./gradlew downloadTranslations ```

## Thanks

Credit to Roman Nazarevych (@lemberh) for his [project](https://github.com/lemberh/LokalisePlugin), on which this is based

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
