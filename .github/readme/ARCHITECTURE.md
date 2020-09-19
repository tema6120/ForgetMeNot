# ForgetMeNot Architecture

ForgetMeMot is built on a [three-tier architecture](https://en.wikipedia.org/wiki/Multitier_architecture) with some specific features.

##  Basic components & Data flow

![Architecture scheme](/.github/readme/architecture_scheme.png)

## Multithreading

[Kotlin coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) are used for asynchronous work. View works on the regular Ui thread. Some screens use asynchronous inflation layout. A separate `businessLogicThread` is allocated to execute the logic. [Dispatchers.IO](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-i-o.html) is used to write to the database. `SpeakerImpl` encapsulates its own separate thread on which it runs.

## State

The application has the following kinds of states:

kind | who can modify | term | example
-----|----------------|------|--------
GlobalState | All `Interactor`s | Long (as long as the app is installed) | The state of cards, deck settings
State of `Interactor`s | `Interactor` that owns state | Short (as long as `Interactor` is needed) | `Exercise.State` consists `ExerciseCard`s' state, current position, text selections
Display settings state | `Controller`s | Long (as long as the app is installed) | Deck sorting, deck filter in HomeScreen
Screen state | `Controller` of screen | Short (as long as screen is not finished) | DeckSetup screen keeps reference to the deck that is being setuped

State is the core of an application and how it works affects all 3 tiers of the application. I submitted the following claims to myself:
* modify the state as a regular class:
```kotlin
state.isSearching = false
```
* possibility of tracking state to update ui
* adequate state saving, maintaining data integrity and high performance.

To address these challenges I developed `FlowMaker` and `FlowMakerWithRegistry` classes.

## Tracking state

To track state changes I utilize the power of [Kotlin delegated properties](https://kotlinlang.org/docs/reference/delegated-properties.html). Entities that represent a state must either be immutable (`Integer`, `String`, `List`) or inherit from `FlowMaker` (or `FlowMakerWithRegistry`). The requirement of this class is to delegate the assignment of all properties to its `flowMaker()` function to track changes:

```kotlin
class HomeScreenState : FlowMaker<HomeScreenState>() {
    var searchText: String by flowMaker("")
    var selectedDeckIds: List<Long> by flowMaker(emptyList())
    var exportedDeck: Deck? by flowMaker(null)
}
```

I chose the wonderful [Kotlin Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html) to represent the flow of changes. This is how you can get a flow and transform it in `ViewModel`:

```kotlin
val hasSelectedDecks: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::selectedDeckIds)
               .map { it.isNotEmpty() }
```

And so you can track in `Fragment`:

```kotlin
viewModel.hasSelectedDecks.observe(
                fragmentCoroutineScope
            ) { hasSelectedDecks: Boolean ->
                if (hasSelectedDecks) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                    }
                } else {
                    actionMode?.finish()
                }
            }

inline fun <T> Flow<T>.observe(
    coroutineScope: CoroutineScope,
    crossinline onEach: (value: T) -> Unit
) {
    coroutineScope.launch {
        collect {
            if (isActive) {
                onEach(it)
            }
        }
    }
}
```

`View` immediately reacts to change of a state property, without waiting for the `Interactor` to finish its work, thereby increasing the responsiveness of the application.

## Saving state

I use [SQDelight](https://github.com/cashapp/sqldelight) for storing state. Depending on the lifetime of the state, I apply two strategy of saving:

1. *Short-term* state never survives app upgrade. So there is no need to create and maintain database schema. Also, it is usually small in size. To save such state I just serialize it to `String` and put it to database.

2. *Long-term* state is saved by patches. `FlowMakerWithRegistry` is specifically designed to save state this way. Apart from possibility of being tracked, it logs each assignment by adding all change-related information (`propertyOwnerClass`, `propertyOwnerId`, `property`, `oldValue`, `newValue`) to `PropertyChangeRegistry` singleton. The problem is that the records of this registry will be used to write to disk on a different thread. Hence it is necessary to ensure the invariability of records. So I introduced `Copyable` interface to take safe copies of mutable objects. `FlowMakerWithRegistry` implements `Copyable`.<br/><br/>Separate attitude toward container classes. For example, although `List` is immutable, the contents of `List` can be mutable. So instead of that "mutable" `List`s, I use `CopyableList` (`List` wrapper that implements `Copyable`). Also, making copies of all objects of a collection can be costly. It happens during regular assigning a new value to the state property and is done on `businessLogicThread`, that should do its work fast. Therefore, we first compute the diffs of the old and new collection, and then make safe copies of only the affected items.

Saving state is initiated by `Controller` and usually occurs after UI events have been processed.

## Dependency Injection

I don't use any dependency injection framework. I manage dependencies by myself. I define a `DiScope` for each `View`. `DiScope` constructs all objects needed for `View` and keeps them. `DiScope` may be created in two ways: when navigating, and when restoring from the database after Android process death. `DiScope` closes when `Fragment` finishes. There is also `AppDiScope`. It exists all the time and provides dependencies common to everyone.

## Navigation

I use [Jetpack Navigation library](https://developer.android.com/jetpack/androidx/releases/navigation) for navigation. I don't use [Safe Args](https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args). Instead, I apply an alternate approach that better matches my architecture.

`Controller` initiates navigation, while `Navigator` is responsible for the navigation logic. `Navigator` is designed in such a way that in order to go to another screen, it requires a lambda of creating `DiScope` of a new screen. To create a `DiScope` instance, either a constructor or a factory method with arguments is used. These arguments are data passed between destinations.

The advantages of this approach:

* no restrictions on the type or size of data passed between screens (unlike [Safe Args](https://developer.android.com/guide/navigation/navigation-pass-data#supported_argument_types)).
* `Fragment`s do not participate in the data transfer process. In my opinion, this is not what they should do.
* we parallelize the navigation process. On the UI Thread, we immediately ask `NavController` to navigate to another screen while on `businessLogicThread` we run the code for creating `DiScope` instance of the next screen. It speeds up speeds up the opening of a new screen, data and dependencies preparation.
