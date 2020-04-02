package com.odnovolov.forgetmenot.domain.architecturecomponents

interface CopyableCollection<E : Copyable> : Collection<E>, Copyable {
    override fun copy(): CopyableCollection<E>
}