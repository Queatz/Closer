package closer.vlllage.com.closer.handler.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.MenuHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On
import kotlinx.android.synthetic.main.item_edit_reminder.view.*

class EditRemindersAdapter(private val on: On, private val removeCallback: (EventReminder) -> Unit) : RecyclerView.Adapter<EditRemindersViewHolder>() {

    var items = mutableListOf<EventReminder>()
        set(value) {
            val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = field[oldItemPosition] == value[newItemPosition]

                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
            })

            field.clear()
            field.addAll(value)

            diffUtil.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditRemindersViewHolder {
        return EditRemindersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_edit_reminder, parent, false))
    }

    override fun onBindViewHolder(holder: EditRemindersViewHolder, position: Int) {
        val reminder = items[position]

        holder.itemView.actionDelete.setOnClickListener {
            removeCallback(reminder)
        }

        // populate values()

        holder.itemView.offsetUnit.text = "Hour"

        holder.itemView.offsetUnit.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(0, R.string.minute) {
                        reminder.offset.unit = "minute"
                        holder.itemView.offsetUnit.text = "Minute"

                        holder.itemView.atText.visible = false
                        holder.itemView.selectTime.visible = false
                    },
                    MenuHandler.MenuOption(0, R.string.hour) {
                        reminder.offset.unit = "hour"
                        holder.itemView.offsetUnit.text = "Hour"

                        holder.itemView.atText.visible = false
                        holder.itemView.selectTime.visible = false
                    },
                    MenuHandler.MenuOption(0, R.string.day) {
                        reminder.offset.unit = "day"
                        holder.itemView.offsetUnit.text = "Day"

                        holder.itemView.atText.visible = true
                        holder.itemView.selectTime.visible = true
                    },
                    MenuHandler.MenuOption(0, R.string.week) {
                        reminder.offset.unit = "week"
                        holder.itemView.offsetUnit.text = "Week"

                        holder.itemView.atText.visible = true
                        holder.itemView.selectTime.visible = true
                    },
                    MenuHandler.MenuOption(0, R.string.month) {
                        reminder.offset.unit = "month"
                        holder.itemView.offsetUnit.text = "Month"

                        holder.itemView.atText.visible = true
                        holder.itemView.selectTime.visible = true
                    },
                    button = ""
            )
        }

        holder.itemView.selectPosition.text = "Before the event starts"

        holder.itemView.selectPosition.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(0, title = "Before the event starts") {
                        reminder.offset.unit = "beforeStart"
                        holder.itemView.selectPosition.text = "Before the event starts"
                    },
                    MenuHandler.MenuOption(0, title = "Before the event ends") {
                        reminder.offset.unit = "beforeEnd"
                        holder.itemView.selectPosition.text = "Before the event ends"
                    },
                    MenuHandler.MenuOption(0, title = "After the event starts") {
                        reminder.offset.unit = "afterStart"
                        holder.itemView.selectPosition.text = "After the event starts"
                    },
                    MenuHandler.MenuOption(0, title = "After the event ends") {
                        reminder.offset.unit = "afterEnd"
                        holder.itemView.selectPosition.text = "After the event ends"
                    },
                    button = ""
            )
        }

        holder.itemView.selectTime.text = "5:45pm (Time of event)"

        holder.itemView.selectTime.setOnClickListener {
            // timepicker
            holder.itemView.selectTime.text = "5:30pm"
        }

        holder.itemView.selectRepeat.text = "None"

        holder.itemView.selectRepeat.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(0, title = "None") {
                        reminder.repeat = null
                        holder.itemView.selectRepeat.text = "None"
                        holder.itemView.selectRepeat.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))

                        holder.itemView.selectRepeatHours.visible = false
                        holder.itemView.selectRepeatDays.visible = false
                        holder.itemView.selectRepeatWeeks.visible = false
                        holder.itemView.selectRepeatMonths.visible = false
                    },
                    MenuHandler.MenuOption(0, title = "Until the event starts") {
                        reminder.repeat = EventReminderRepeat()
                        holder.itemView.selectRepeat.text = "Until the event starts"
                        holder.itemView.selectRepeat.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))

                        holder.itemView.selectRepeatHours.visible = true
                        holder.itemView.selectRepeatDays.visible = true
                        holder.itemView.selectRepeatWeeks.visible = true
                        holder.itemView.selectRepeatMonths.visible = true
                    }.visible(true),
                    MenuHandler.MenuOption(0, title = "Until the event ends") {
                        reminder.repeat = EventReminderRepeat()
                        holder.itemView.selectRepeat.text = "Until the event ends"
                        holder.itemView.selectRepeat.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))

                        holder.itemView.selectRepeatHours.visible = true
                        holder.itemView.selectRepeatDays.visible = true
                        holder.itemView.selectRepeatWeeks.visible = true
                        holder.itemView.selectRepeatMonths.visible = true
                    }.visible(true),
                    button = ""
            )
        }

        holder.itemView.selectRepeatHours.text = "Choose hours"
        holder.itemView.selectRepeatDays.text = "Choose days"
        holder.itemView.selectRepeatWeeks.text = "Choose weeks"
        holder.itemView.selectRepeatMonths.text = "Choose months"

        holder.itemView.selectRepeatHours.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
        holder.itemView.selectRepeatDays.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
        holder.itemView.selectRepeatWeeks.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
        holder.itemView.selectRepeatMonths.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))

        holder.itemView.selectRepeatHours.setOnClickListener {
            val options = arrayOf(
                    "12am",
                    "1am",
                    "2am",
                    "3am",
                    "4am",
                    "5am",
                    "6am",
                    "7am",
                    "8am",
                    "9am",
                    "10am",
                    "11am",
                    "12pm",
                    "1pm",
                    "2pm",
                    "3pm",
                    "4pm",
                    "5pm",
                    "6pm",
                    "7pm",
                    "8pm",
                    "9pm",
                    "10pm",
                    "11pm",
            )

            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            options,
                            options.map {
                                reminder.repeat?.hours?.contains(it) == true
                            }.toBooleanArray()
                    ) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.hours == null) {
                                reminder.repeat?.hours = listOf(options[item])
                            } else {
                                reminder.repeat?.hours = reminder.repeat?.hours?.toMutableList()?.let {
                                    it.add(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.hours?.size == 1) {
                                reminder.repeat?.hours = null
                            } else {
                                reminder.repeat?.hours = reminder.repeat?.hours?.toMutableList()?.let {
                                    it.remove(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        }

                        reminder.repeat?.hours?.let { hours ->
                            if (hours.size > 0) {
                                holder.itemView.selectRepeatHours.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                                holder.itemView.selectRepeatHours.text = hours.toList().joinToString(if (hours.size == 2) " and " else ", ")
                            } else {
                                holder.itemView.selectRepeatHours.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                                holder.itemView.selectRepeatHours.text = "Choose hours"
                            }
                        } ?: run {
                            holder.itemView.selectRepeatHours.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                            holder.itemView.selectRepeatHours.text = "Choose hours"

                        }
                    }
                    .setPositiveButton(R.string.apply, { dialog, which -> })
                    .show()
        }

        holder.itemView.selectRepeatDays.setOnClickListener {
            val options = arrayOf(
                    "Sunday",
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday",
                    "1st",
                    "2nd",
                    "3rd",
                    "4th",
                    "5th",
                    "6th",
                    "7th",
                    "8th",
                    "9th",
                    "10th",
                    "11th",
                    "12th",
                    "13th",
                    "14th",
                    "15th",
                    "16th",
                    "17th",
                    "18th",
                    "19th",
                    "20th",
                    "21st",
                    "22nd",
                    "23rd",
                    "24th",
                    "25th",
                    "26th",
                    "27th",
                    "28th",
                    "29th",
                    "30th",
                    "31st",
                    "Last day of the month",
            )

            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            options,
                            options.map {
                                reminder.repeat?.days?.contains(it) == true
                            }.toBooleanArray()) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.days == null) {
                                reminder.repeat?.days = listOf(options[item])
                            } else {
                                reminder.repeat?.days = reminder.repeat?.days?.toMutableList()?.let {
                                    it.add(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.days?.size == 1) {
                                reminder.repeat?.days = null
                            } else {
                                reminder.repeat?.days = reminder.repeat?.days?.toMutableList()?.let {
                                    it.remove(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        }

                        reminder.repeat?.days?.let { days ->
                            if (days.size > 0) {
                                holder.itemView.selectRepeatDays.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                                holder.itemView.selectRepeatDays.text = days.toList().joinToString(if (days.size == 2) " and " else ", ")
                            } else {
                                holder.itemView.selectRepeatDays.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                                holder.itemView.selectRepeatDays.text = "Choose days"
                            }
                        } ?: run {
                            holder.itemView.selectRepeatDays.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                            holder.itemView.selectRepeatDays.text = "Choose days"

                        }
                    }
                    .setPositiveButton(R.string.apply, { dialog, which -> })
                    .show()
        }

        holder.itemView.selectRepeatWeeks.setOnClickListener {
            val options = arrayOf(
                    "First week",
                    "Second week",
                    "Third week",
                    "Fourth week",
            )

            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            options,
                            booleanArrayOf(
                                    reminder.repeat?.weeks?.contains("First week") == true,
                                    reminder.repeat?.weeks?.contains("Second week") == true,
                                    reminder.repeat?.weeks?.contains("Third week") == true,
                                    reminder.repeat?.weeks?.contains("Fourth week") == true,
                            )) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.weeks == null) {
                                reminder.repeat?.weeks = listOf(options[item])
                            } else {
                                reminder.repeat?.weeks = reminder.repeat?.weeks?.toMutableList()?.let {
                                    it.add(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.weeks?.size == 1) {
                                reminder.repeat?.weeks = null
                            } else {
                                reminder.repeat?.weeks = reminder.repeat?.weeks?.toMutableList()?.let {
                                    it.remove(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        }

                        reminder.repeat?.weeks?.let { weeks ->
                            if (weeks.size > 0) {
                                holder.itemView.selectRepeatWeeks.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                                holder.itemView.selectRepeatWeeks.text = weeks.toList().joinToString(if (weeks.size == 2) " and " else ", ")
                            } else {
                                holder.itemView.selectRepeatWeeks.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                                holder.itemView.selectRepeatWeeks.text = "Choose weeks"
                            }
                        } ?: run {
                            holder.itemView.selectRepeatWeeks.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                            holder.itemView.selectRepeatWeeks.text = "Choose weeks"

                        }
                    }
                    .setPositiveButton(R.string.apply, { dialog, which -> })
                    .show()
        }

        holder.itemView.selectRepeatMonths.setOnClickListener {
            val options = arrayOf(
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December",
            )

            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            options,
                            booleanArrayOf(
                                    reminder.repeat?.months?.contains("January") == true,
                                    reminder.repeat?.months?.contains("February") == true,
                                    reminder.repeat?.months?.contains("March") == true,
                                    reminder.repeat?.months?.contains("April") == true,
                                    reminder.repeat?.months?.contains("May") == true,
                                    reminder.repeat?.months?.contains("June") == true,
                                    reminder.repeat?.months?.contains("July") == true,
                                    reminder.repeat?.months?.contains("August") == true,
                                    reminder.repeat?.months?.contains("September") == true,
                                    reminder.repeat?.months?.contains("October") == true,
                                    reminder.repeat?.months?.contains("November") == true,
                                    reminder.repeat?.months?.contains("December") == true,
                            )) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.months == null) {
                                reminder.repeat?.months = listOf(options[item])
                            } else {
                                reminder.repeat?.months = reminder.repeat?.months?.toMutableList()?.let {
                                    it.add(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.months?.size == 1) {
                                reminder.repeat?.months = null
                            } else {
                                reminder.repeat?.months = reminder.repeat?.months?.toMutableList()?.let {
                                    it.remove(options[item])
                                    it.sortedBy { options.indexOf(it) }.toList()
                                }
                            }
                        }

                        reminder.repeat?.months?.let { months ->
                            if (months.size > 0) {
                                holder.itemView.selectRepeatMonths.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                                holder.itemView.selectRepeatMonths.text = months.toList().joinToString(if (months.size == 2) " and " else ", ")
                            } else {
                                holder.itemView.selectRepeatMonths.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                                holder.itemView.selectRepeatMonths.text = "Choose months"
                            }
                        } ?: run {
                            holder.itemView.selectRepeatMonths.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
                            holder.itemView.selectRepeatMonths.text = "Choose months"

                        }
                    }
                    .setPositiveButton(R.string.apply, { dialog, which -> })
                    .show()
        }

    }

    override fun getItemCount() = items.size
}

class EditRemindersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

}
