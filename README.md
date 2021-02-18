# UnkillableSavedState

[![](https://jitpack.io/v/alphicc/UnkillableSavedState.svg)](https://jitpack.io/#alphicc/UnkillableSavedState)

## Description (en)

UnkillableSavedState - This library is an add-on over [SavedStateHandle] (https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle) in the ViewModel.
Used to make it easier for the developer to work with SavedStateHandle.


When developing the project, the goal was to create a place where you can store variables and at the same time, the developer did not worry that they would be destroyed due to the release of memory by the operating system. You can also be sure that when you rotate the screen, they will not disappear anywhere.

### Main feature
UnkillableSavedState is convenient as it saves you the hassle of writing boilerplate code.

### How to add?
```kotlin
//Add it in your root build.gradle at the end of repositories:
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

//app gradle
dependencies {
    //UnkillableSavedState
    implementation 'com.github.alphicc:UnkillableSavedState:v0.5.0'
    kapt ("myLink2")
}
```
### How to use?

#### Step 1
After you add the library to Gradle, you can use it as follows.
Let's assume you already have a Fragment and a ViewModel. You need a place where we will save our "unkillable" values.

<img src="https://github.com/alphicc/UnkillableSavedState/blob/main/media/package%20example.png" alt="PackageExample.png" width="250"/>


#### Step 2
We declare data that will be "unkillable" in the data class. We build a project. (It is mandatory to build the project since the library uses code generation. Work on simplifying this step is planned.)
```kotlin
@Unkillable
data class SampleFragmentState(
    val testValue: Double,
    val testLiveData: MutableLiveData<Double>
) : EmptyState()
```

#### Step 3
The ViewModel needs to be initialized as follows. [More information] (https://developer.android.com/reference/androidx/lifecycle/SavedStateViewModelFactory)
```kotlin
activity?.application?.let { application -> 
    viewModel = ViewModelProvider(this, SavedStateViewModelFactory(application, this))
        .get(SampleViewModel::class.java) 
}
```

#### Step 4
Your ViewModel should look like this:
```kotlin
class SampleViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidStateViewModel<UnkillableSampleFragmentState>(application, savedStateHandle) {

    override fun provideState() = createState<UnkillableSampleFragmentState>()
}
```
ViewModel must inherit from AndroidStateViewModel / StateViewModel, which inherited from AndroidViewModel and ViewModel respectively.
UnkillableSampleFragmentState is the generated object obtained after starting kapt (See step 2).

It's all.
Now we can safely use the objects declared in SampleFragmentState. (Save something there or get it out)

### Usage example

```kotlin
...

init {
    // get values example
    Log.d("StateLog", "0 value ${state.testValue}") 
    Log.d("StateLog", "1 value ${state.testLiveData?.value}")
}

fun onSetDataClicked() {
    // set values example
    state.testValue = 2.2
    state.updateTestLiveDataValue(3.3) // yourLiveData.value = 3.3
    state.postUpdateTestLiveDataValue(3.3) // yourLiveData.postValue(3.3)
}
...
```
### Any ideas on how to improve the project?

I am open to suggestions for the development of the project and will always be happy to consider ways to improve it.

## Описание (ru)

UnkillableSavedState - это библиотека является надстройкой над [SavedStateHandle](https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle) в ViewModel.
Используется для того, чтобы облегчить работу разработчика с SavedStateHandle.


При разработке проекта ставилась цель создать место, где можно хранить переменные и при этом, разработчик не переживал о том, что они будут уничтожены по причине освобождения памяти операционной системой. Также вы можете быть уверены, что при повороте экрана они никуда не исчезнут.

### Главная особенность
UnkillableSavedState это удобно, так как избавляет вас от написания шаблонного кода.

### Как добавить?
```kotlin
//Add it in your root build.gradle at the end of repositories:
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

//app gradle
dependencies {
    //UnkillableSavedState
    implementation 'com.github.alphicc:UnkillableSavedState:v0.5.0'
    kapt ("myLink2")
}
```
### Как использовать?

#### Шаг 1
После того как Вы добавите библитеку в gradle, вы можете ее использовать следующим образом.
Предположим, что у Вас уже есть Fragment и ViewModel. Вам нужно место куда будем сохранять наши "неубиваемые" значения.

<img src="https://github.com/alphicc/UnkillableSavedState/blob/main/media/package%20example.png" alt="PackageExample.png" width="250"/>


#### Шаг 2
Декларируем данные, которые будут "неубиваемыми" в data class. Билдим проект. (Билдить проект обязательно, так как библиотека использует кодогенерацию. Работа над упрощением этого шага планируется.)
```kotlin
@Unkillable
data class SampleFragmentState(
    val testValue: Double,
    val testLiveData: MutableLiveData<Double>
) : EmptyState()
```

#### Шаг 3
ViewModel необходимо инициализировать следующим образом. [Больше информации](https://developer.android.com/reference/androidx/lifecycle/SavedStateViewModelFactory)
```kotlin
activity?.application?.let { application -> 
    viewModel = ViewModelProvider(this, SavedStateViewModelFactory(application, this))
        .get(SampleViewModel::class.java) 
}
```

#### Шаг 4
Ваш ViewModel должен выглядеть следующем образом: 
```kotlin
class SampleViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidStateViewModel<UnkillableSampleFragmentState>(application, savedStateHandle) {

    override fun provideState() = createState<UnkillableSampleFragmentState>()
}
```
ViewModel должен быть унаследован от AndroidStateViewModel/StateViewModel, которые унаследованы от AndroidViewModel и ViewModel соответственно.
UnkillableSampleFragmentState это сгенерированный объект, полученный после запуска kapt (Смотрите шаг 2).

Это все.
Теперь мы можем смело использовать объекты объявленные в SampleFragmentState. (Сохранять туда что-то или доставать)

### Пример использования

```kotlin
...

init {
    // get values example
    Log.d("StateLog", "0 value ${state.testValue}") 
    Log.d("StateLog", "1 value ${state.testLiveData?.value}")
}

fun onSetDataClicked() {
    // set values example
    state.testValue = 2.2
    state.updateTestLiveDataValue(3.3) // yourLiveData.value = 3.3
    state.postUpdateTestLiveDataValue(3.3) // yourLiveData.postValue(3.3)
}
...
```

### Есть идеи как можно улучшить проект?

Я открыт для предложений по развитию проекта и всегда буду рад рассмотреть способы его улучшения.

## License
```
MIT License

Copyright (c) 2021 Petr Shubin (@alphicc)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
