package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupInvite
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import com.queatz.on.On
import io.objectbox.query.QueryBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import java.util.*

class NameHandler constructor(private val on: On) {

    fun getName(phoneId: String): String {
        val phoneList = on<StoreHandler>().store.box(Phone::class).query().equal(Phone_.id, phoneId, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find()
        return if (phoneList.isEmpty())
            on<ResourcesHandler>().resources.getString(R.string.unknown)
        else getName(phoneList[0])
    }

    fun getNameAsync(phoneId: String): Single<String> = on<DataHandler>().getPhone(phoneId)
                .observeOn(AndroidSchedulers.mainThread())
                .map { getName(it) }

    fun getName(phone: Phone?): String = when {
        phone == null -> noName()
        phone.name.isNullOrBlank() -> fallbackName(phone.id)
        else -> phone.name!!
    }

    fun getName(groupContact: GroupContact?): String = when {
        groupContact == null -> noName()
        groupContact.contactName.isNullOrBlank() -> fallbackName(groupContact.contactId)
        else -> groupContact.contactName!!
    }

    fun getName(groupInvite: GroupInvite?): String = if (groupInvite?.name.isNullOrBlank()) noName() else groupInvite!!.name ?: on<ResourcesHandler>().resources.getString(R.string.unknown)

    fun getFallbackName(phoneId: String? = null, name: String? = null): String = if (name.isNullOrBlank()) fallbackName(phoneId) else name

    private fun fallbackName(phoneId: String?): String {
        if (phoneId == null) {
            return noName()
        }

        val index = Random(phoneId.hashCode().toLong()).nextInt(fallbackNames.size)
        return fallbackNames[index]
    }

    private fun noName() = on<ResourcesHandler>().resources.getString(R.string.no_name)

    companion object {

        private val fallbackNames = arrayOf(
                "Random Armadillo",
                "Random Aardvark",
                "Random Kangaroo",
                "Random Gorilla",
                "Random Chimpanzee",
                "Random Anaconda",
                "Random Parakeet",
                "Random Rhino",
                "Random Muskrat",
                "Random Bumblebee",
                "Random Tiger",
                "Random Ocelot",
                "Random Capybara",
                "Random Sloth",
                "Random Lemur",
                "Random Baboon"
        )
    }
}
