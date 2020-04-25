package closer.vlllage.com.closer.handler.feed

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.helpers.TimerHandler
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

class PeopleRecyclerViewHandler constructor(private val on: On) {

    private lateinit var peopleRecyclerView: RecyclerView
    private val peopleAdapter = PeopleAdapter(on)

    fun attach(peopleRecyclerView: RecyclerView) {
        this.peopleRecyclerView = peopleRecyclerView
        peopleRecyclerView.adapter = peopleAdapter
        peopleRecyclerView.layoutManager = LinearLayoutManager(
                peopleRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )
    }

    fun setPeople(people: MutableList<Phone>) {
        peopleAdapter.people = people
    }
}
