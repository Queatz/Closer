package closer.vlllage.com.closer.handler.feed

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

class PeopleRecyclerViewHandler constructor(private val on: On) {

    private lateinit var peopleRecyclerView: RecyclerView
    private lateinit var peopleAdapter: PeopleAdapter

    fun attach(peopleRecyclerView: RecyclerView, small: Boolean = false) {
        this.peopleAdapter = PeopleAdapter(on, small)
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
