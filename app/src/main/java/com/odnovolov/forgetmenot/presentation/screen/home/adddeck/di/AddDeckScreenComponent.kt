package com.odnovolov.forgetmenot.presentation.screen.home.adddeck.di

import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckFragment
import dagger.BindsInstance
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [AddDeckScreenModule::class])
interface AddDeckScreenComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun with(timeCapsule: AndroidTimeCapsule): Builder

        fun build(): AddDeckScreenComponent
    }

    fun inject(addDeckFragment: AddDeckFragment)

    companion object {
        fun createWith(timeCapsule: AndroidTimeCapsule): AddDeckScreenComponent {
            return AppComponent.get()
                .addDeckScreenComponentBuilder()
                .with(timeCapsule)
                .build()
        }
    }
}