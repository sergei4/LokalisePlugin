
# LokalisePlugin

This is a simple plugin which helps with the integration of [Lokalise](https://lokalise.co) localization service into Android App. 
This plugin has two main purposes:
* upload local project strings to Lokalise backend
* update local project strings files with latest translations from Lokalise backend

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
         classpath 'com.github.sergei4:lokaliseplugin:1.0.0' // latest version of plugin goes here
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
}
```
To get `projectId` go to your Project/Settings/General 
To get read/write `token` read this [documentation](https://docs.lokalise.co/faqs/api-tokens)

After this configuration you should be able to use task `downloadTranslations`  you can execute them from IDE GUI or command line.
``` ./gradlew downloadTranslations ```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
