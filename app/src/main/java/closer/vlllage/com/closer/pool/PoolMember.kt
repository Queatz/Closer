package closer.vlllage.com.closer.pool

open class PoolMember {

    internal lateinit var pool: Pool

    internal fun setPool(pool: Pool): PoolMember {
        this.pool = pool
        onPoolInit()
        return this
    }

    protected open fun onPoolInit() {}
    open fun onPoolEnd() {}

    protected fun <T : PoolMember> `$`(member: Class<T>): T {
        return pool.`$`(member)
    }
}
