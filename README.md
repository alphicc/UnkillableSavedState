# UnkillableSavedState

## Description (en)

### Описание (ru)

UnkillableSavedState - это библиотека является надстройкой над [SavedStateHandle](https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle) в ViewModel.
Используется для того, чтобы облегчить работу разработчика с SavedStateHandle.


При разработке проекта ставилась цель создать место, где можно хранить переменные и при этом, разработчик не переживал о том, что они будут уничтожены по причине освобождения памяти операционной системой. Также вы можете быть уверены, что при повороте экрана они никуда не исчезнут.

### Главная особенность
UnkillableSavedState это удобно, так как избавляет вас от написания шаблонного кода.

### Как добавить?
```kotlin
dependencies {
    //UnkillableSavedState
    implementation("myLink")
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
    Log.d("StateLog", "0 value ${state.testValue}")
    Log.d("StateLog", "1 value ${state.testLiveData?.value}")
}

fun onSetDataClicked() {
    state.testValue = 2.2
    state.updateTestLiveDataValue(3.3)
}
...
```
