package closer.vlllage.com.closer.pool

import java.lang.reflect.InvocationTargetException
import java.util.*

open class Pool internal constructor() {

    private val members = HashMap<Class<*>, PoolMember>()

    fun <T : PoolMember> `$`(member: Class<T>): T {
        if (!members.containsKey(member)) {
            try {
                members[member] = member.getConstructor().newInstance().setPool(this)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }

        }

        return members[member] as T
    }

    fun <T : PoolMember> `$set`(member: T): T {
        if (members.containsKey(member.javaClass)) {
            throw IllegalStateException("Cannot \$set member that already exists")
        }

        members[member.javaClass] = member

        return member
    }

    open fun end() {
        for (member in members.values) {
            member.onPoolEnd()
        }
    }

    companion object {

        fun tempPool(): TempPool {
            return TempPool()
        }
    }
}
