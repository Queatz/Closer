package closer.vlllage.com.closer.pool

import android.support.v7.widget.RecyclerView

abstract class PoolRecyclerAdapter<T : RecyclerView.ViewHolder>(poolMember: PoolMember) : RecyclerView.Adapter<T>() {

    private val pool: Pool = poolMember.pool

    fun `$pool`(): PoolMember {
        return pool.`$`(PoolMember::class.java)
    }

    protected fun <T : PoolMember> `$`(member: Class<T>): T {
        return pool.`$`(member)
    }
}
