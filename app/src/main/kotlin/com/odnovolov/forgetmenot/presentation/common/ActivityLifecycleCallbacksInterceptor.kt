package com.odnovolov.forgetmenot.presentation.common

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.presentation.common.ActivityLifecycleCallbacksInterceptor.ActivityLifecycleEvent.*
import kotlinx.coroutines.flow.Flow

class ActivityLifecycleCallbacksInterceptor : ActivityLifecycleCallbacks {
    private val eventFlow = EventFlow<ActivityLifecycleEvent>()
    val activityLifecycleEventFlow: Flow<ActivityLifecycleEvent> get() = eventFlow.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        eventFlow.send(ActivityCreated(activity, savedInstanceState))
    }

    override fun onActivityStarted(activity: Activity) {
        eventFlow.send(ActivityStarted(activity))
    }

    override fun onActivityResumed(activity: Activity) {
        eventFlow.send(ActivityResumed(activity))
    }

    override fun onActivityPaused(activity: Activity) {
        eventFlow.send(ActivityPaused(activity))
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        eventFlow.send(ActivitySaveInstanceState(activity, outState))
    }

    override fun onActivityStopped(activity: Activity) {
        eventFlow.send(ActivityStopped(activity))
    }

    override fun onActivityDestroyed(activity: Activity) {
        eventFlow.send(ActivityDestroyed(activity))
    }

    sealed class ActivityLifecycleEvent {
        class ActivityCreated(val activity: Activity, val savedInstanceState: Bundle?) :
            ActivityLifecycleEvent()
        class ActivityStarted(val activity: Activity) : ActivityLifecycleEvent()
        class ActivityResumed(val activity: Activity) : ActivityLifecycleEvent()
        class ActivityPaused(val activity: Activity) : ActivityLifecycleEvent()
        class ActivitySaveInstanceState(val activity: Activity, val outState: Bundle) :
            ActivityLifecycleEvent()
        class ActivityStopped(val activity: Activity) : ActivityLifecycleEvent()
        class ActivityDestroyed(val activity: Activity) : ActivityLifecycleEvent()
    }
}