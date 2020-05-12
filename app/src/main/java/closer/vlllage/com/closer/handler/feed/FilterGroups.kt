package closer.vlllage.com.closer.handler.feed

import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On

class FilterGroups(private val on: On) {
    fun public(groups: List<Group>) = groups.filter { !it.hasEvent() && !it.physical }
    fun events(groups: List<Group>) = groups.filter { it.hasEvent() }
    fun physical(groups: List<Group>) = groups.filter { it.hub }
}