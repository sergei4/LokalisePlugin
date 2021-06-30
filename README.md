# LokalisePlugin
[![](https://jitpack.io/v/sergei4/LokalisePlugin.svg)](https://jitpack.io/#sergei4/LokalisePlugin)

This is a simple plugin which helps with the integration of [Lokalise](https://lokalise.co) localization service into Android or IPhone App. 
This plugin has two main purposes:
* update local project strings files with latest translations from Lokalise backend
* upload local project strings to Lokalise backend (only android)

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
         classpath 'com.github.sergei4.LokalisePlugin:lokalise-plugin:$version' // latest version of plugin goes here
  }  
}
```
Then configure app module `build.gradle`
#### Android
```
apply plugin: 'me.eremkin.lokalise-plugin' // apply plugin for android
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
#### IPhone
```
apply plugin: 'me.eremkin.lokalise-plugin.ios' // apply plugin for ios
......
// and configure it
lokalise {  
    api {  
        projectId = "123456789" // t
        token = "token"  // you need token with read and write permissions
    }  
  
    lang {
        path = "ios/ru.lproj"
        lokaliseLang = "ru"
    }

    lang {
        path = "ios/zh.lproj"
        lokaliseLang = "zh_CN"
        langCode = "zh-Hans"
    }
}
```

To get `projectId` go to your Project/Settings/General in **Lokalise.io** console

To get read/write `token` read this [documentation](https://docs.lokalise.co/faqs/api-tokens)

After configuration you should be able to use two gradle tasks for android `uploadAndroidStrings`, `downloadAndroidStrings` or one gradle task for iphone `downloadIosLocalizableStrings`  

You can execute them from IDE GUI or command line``` ./gradlew downloadAndroidStrings ```


## Thanks

Credit to Roman Nazarevych (@lemberh) for his [project](https://github.com/lemberh/LokalisePlugin), on which this is based

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
