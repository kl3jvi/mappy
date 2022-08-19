<img src="https://raw.githubusercontent.com/kl3jvi/mappy/764dd6626852d563e21472f4cef30c640a188ff1/assets/mappy.svg">
<p align="center">
🧭 Mappy is a Kotlin Symbol Processor to auto-generate mapper functions for your architecture layer models. It creates mappers for database Entities and UI models.
</p>

## Why Mappy?

Mappy generates extension functions for each different model. You can reduce writing repeated mapper classes for every different model by auto-generating based on annotation processor.<br><br>
You can massively reduce writing repeated files such as `_Mapper` classes.

<p align="center">
<img src="https://raw.githubusercontent.com/kl3jvi/mappy/ba49ac2e1b34d09b03afec6f1760320c55a823b7/assets/usage.svg" width="760"/>
</p>

## Gradle Setup

To use Mappy library in your project, you need to follow steps below.

Step 1. Add the JitPack repository to your build file 


<details open>
  <summary>Kotlin DSL</summary>

```kotlin
allprojects {
	repositories {
		maven (url = "https://jitpack.io")
    }
}
```
</details>

<details>
  <summary>Groovy</summary>

```kotlin
allprojects {
	repositories {
		maven {url = 'https://jitpack.io' }
    }
}
```
</details>

Step 2. Add the dependency
```kotlin
  implementation("com.github.kl3jvi.mappy:annotations:1.0.0")
  kapt("com.github.kl3jvi.mappy:aprocessor:1.0.0")
```
