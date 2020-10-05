package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.phone.GoalHandler
import closer.vlllage.com.closer.handler.phone.LifestyleHandler
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

class ProfileHelper(private val on: On) {
    fun joinLifestyle(phoneId: String) {
        on<DefaultInput>().show(R.string.add_a_lifestyle, hintRes = R.string.lifestyle_hint, buttonRes = R.string.add_this_lifestyle, themeRes = R.style.AppTheme_AlertDialog_Pink) {
            on<LifestyleHandler>().addLifestyle(it, phoneId)
        }
    }

    fun addGoal(phoneId: String) {
        on<DefaultInput>().show(R.string.add_a_goal, hintRes = R.string.goal_hint, buttonRes = R.string.add_this_goal, themeRes = R.style.AppTheme_AlertDialog_Pink) {
            on<GoalHandler>().addGoal(it, phoneId)
        }
    }

    fun editOfftime(phone: Phone) {
        on<DefaultInput>().show(R.string.offtime_hint, buttonRes = R.string.save, hintRes = R.string.write_here, prefill = phone.offtime, multiline = true) {
            on<AccountHandler>().updateAbout(offtime = it) {
                on<RefreshHandler>().refreshPhone(phone.id!!)
            }
        }
    }

    fun editOccupation(phone: Phone) {
        on<DefaultInput>().show(R.string.occupation_hint, buttonRes = R.string.save, hintRes = R.string.write_here, prefill = phone.occupation, multiline = true) {
            on<AccountHandler>().updateAbout(occupation = it) {
                on<RefreshHandler>().refreshPhone(phone.id!!)
            }
        }
    }

    fun editHistory(phone: Phone) {
        on<DefaultInput>().show(R.string.history_hint, buttonRes = R.string.save, hintRes = R.string.write_here, prefill = phone.history, multiline = true) {
            on<AccountHandler>().updateAbout(history = it) {
                on<RefreshHandler>().refreshPhone(phone.id!!)
            }
        }
    }

    fun editIntroduction(phone: Phone) {
        on<DefaultInput>().show(R.string.introduction_hint, buttonRes = R.string.save, hintRes = R.string.write_here, prefill = phone.introduction, multiline = true) {
            on<AccountHandler>().updateAbout(introduction = it) {
                on<RefreshHandler>().refreshPhone(phone.id!!)
            }
        }
    }
}
