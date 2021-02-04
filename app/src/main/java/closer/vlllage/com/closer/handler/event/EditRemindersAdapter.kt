package closer.vlllage.com.closer.handler.event

import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.MenuHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On
import kotlinx.android.synthetic.main.item_edit_reminder.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue

class EditRemindersAdapter(private val on: On, private val removeCallback: (EventReminder) -> Unit) : RecyclerView.Adapter<EditRemindersViewHolder>() {

    var items = mutableListOf<EventReminder>()
        set(value) {
            val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = field[oldItemPosition] === value[newItemPosition]
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
            })

            field.clear()
            field.addAll(value)

            diffUtil.dispatchUpdatesTo(this)
        }

    private val weekOptions = arrayOf(
            "First week",
            "Second week",
            "Third week",
            "Fourth week",
    )

    private val weekOptionsValues = arrayOf(
            "1",
            "2",
            "3",
            "4",
    )

    private val hourOptions = arrayOf(
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

    private val hourOptionsValues = arrayOf(
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "21",
            "22",
            "23",
    )

    private val dayOptions = arrayOf(
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday",
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

     private val dayOptionsValues = arrayOf(
            "monday",
            "tuesday",
            "wednesday",
            "thursday",
            "friday",
            "saturday",
            "sunday",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "21",
            "22",
            "23",
            "24",
            "25",
            "26",
            "27",
            "28",
            "29",
            "30",
            "31",
            "last",
    )

    private val monthOptions = arrayOf(
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

    private val monthOptionsValues = arrayOf(
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditRemindersViewHolder {
        return EditRemindersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_edit_reminder, parent, false))
    }

    override fun onBindViewHolder(holder: EditRemindersViewHolder, position: Int) {
        val reminder = items[position]

        holder.itemView.actionDelete.setOnClickListener {
            removeCallback(reminder)
        }

        holder.itemView.offsetAmount.setText(reminder.offset.amount.absoluteValue.toString())

        holder.itemView.offsetAmount.doAfterTextChanged {
            it.toString().toIntOrNull()?.let {
                reminder.offset.amount = it * (if (reminder.offset.amount < 0) -1 else 1)
                update(holder, reminder)
            }
        }

        holder.itemView.comment.setText(reminder.text ?: "")

        holder.itemView.comment.doAfterTextChanged {
            reminder.text = it?.toString()?.takeIf { it.isNotBlank() }
        }

        update(holder, reminder)

        holder.itemView.offsetUnit.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(0, R.string.minute) {
                        reminder.offset.unit = EventReminderOffsetUnit.Minute
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, R.string.hour) {
                        reminder.offset.unit = EventReminderOffsetUnit.Hour
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, R.string.day) {
                        reminder.offset.unit = EventReminderOffsetUnit.Day
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, R.string.week) {
                        reminder.offset.unit = EventReminderOffsetUnit.Week
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, R.string.month) {
                        reminder.offset.unit = EventReminderOffsetUnit.Month
                        update(holder, reminder)
                    },
                    button = ""
            )
        }

        holder.itemView.selectPosition.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(0, title = on<ResourcesHandler>().resources.getString(R.string.before_event_starts)) {
                        reminder.offset.amount = -reminder.offset.amount.absoluteValue
                        reminder.position = EventReminderPosition.Start
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, title = on<ResourcesHandler>().resources.getString(R.string.before_event_ends)) {
                        reminder.offset.amount = -reminder.offset.amount.absoluteValue
                        reminder.position = EventReminderPosition.End
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, title = on<ResourcesHandler>().resources.getString(R.string.after_event_starts)) {
                        reminder.offset.amount = reminder.offset.amount.absoluteValue
                        reminder.position = EventReminderPosition.Start
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, title = on<ResourcesHandler>().resources.getString(R.string.after_event_ends)) {
                        reminder.offset.amount = reminder.offset.amount.absoluteValue
                        reminder.position = EventReminderPosition.End
                        update(holder, reminder)
                    },
                    button = ""
            )
        }

        holder.itemView.selectTime.setOnClickListener {
            TimePickerDialog(it.context, { timePicker, hour, minute ->
                reminder.time.hour = hour
                reminder.time.minute = minute
                update(holder, reminder)
            }, reminder.time.hour, reminder.time.minute, false).show()
        }

        holder.itemView.selectRepeat.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(0, title = "None") {
                        reminder.repeat = null
                        update(holder, reminder)
                    },
                    MenuHandler.MenuOption(0, title = on<ResourcesHandler>().resources.getString(R.string.until_event_starts)) {
                        if (reminder.repeat == null) {
                            reminder.repeat = EventReminderRepeat()
                        }
                        reminder.repeat!!.until = EventReminderPosition.Start
                        update(holder, reminder)
                    }.visible(true),
                    MenuHandler.MenuOption(0, title = on<ResourcesHandler>().resources.getString(R.string.until_event_ends)) {
                        if (reminder.repeat == null) {
                            reminder.repeat = EventReminderRepeat()
                        }
                        reminder.repeat!!.until = EventReminderPosition.End
                        update(holder, reminder)
                    }.visible(true),
                    button = ""
            )
        }

        holder.itemView.selectRepeatHours.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            hourOptions,
                            hourOptionsValues.map {
                                reminder.repeat?.hours?.contains(it) == true
                            }.toBooleanArray()
                    ) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.hours == null) {
                                reminder.repeat?.hours = listOf(hourOptionsValues[item])
                            } else {
                                reminder.repeat?.hours = reminder.repeat?.hours?.toMutableList()?.let {
                                    it.removeIf { hourOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.add(hourOptionsValues[item])
                                    it.sortedBy { hourOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.hours?.size == 1) {
                                reminder.repeat?.hours = null
                            } else {
                                reminder.repeat?.hours = reminder.repeat?.hours?.toMutableList()?.let {
                                    it.removeIf { hourOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.remove(hourOptionsValues[item])
                                    it.sortedBy { hourOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        }

                        update(holder, reminder)
                    }
                    .setPositiveButton(R.string.apply) { _, _ -> }
                .show()
        }

        holder.itemView.selectRepeatDays.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            dayOptions,
                            dayOptionsValues.map {
                                reminder.repeat?.days?.contains(it) == true
                            }.toBooleanArray()) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.days == null) {
                                reminder.repeat?.days = listOf(dayOptionsValues[item])
                            } else {
                                reminder.repeat?.days = reminder.repeat?.days?.toMutableList()?.let {
                                    it.removeIf { dayOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.add(dayOptionsValues[item])
                                    it.sortedBy { dayOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.days?.size == 1) {
                                reminder.repeat?.days = null
                            } else {
                                reminder.repeat?.days = reminder.repeat?.days?.toMutableList()?.let {
                                    it.removeIf { dayOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.remove(dayOptionsValues[item])
                                    it.sortedBy { dayOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        }

                        update(holder, reminder)
                    }
                    .setPositiveButton(R.string.apply) { _, _ -> }
                .show()
        }

        holder.itemView.selectRepeatWeeks.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            weekOptions,
                            weekOptionsValues.map {
                                reminder.repeat?.weeks?.contains(it) == true
                            }.toBooleanArray()) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.weeks == null) {
                                reminder.repeat?.weeks = listOf(weekOptionsValues[item])
                            } else {
                                reminder.repeat?.weeks = reminder.repeat?.weeks?.toMutableList()?.let {
                                    it.removeIf { weekOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.add(weekOptionsValues[item])
                                    it.sortedBy { weekOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.weeks?.size == 1) {
                                reminder.repeat?.weeks = null
                            } else {
                                reminder.repeat?.weeks = reminder.repeat?.weeks?.toMutableList()?.let {
                                    it.removeIf { weekOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.remove(weekOptionsValues[item])
                                    it.sortedBy { weekOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        }

                        update(holder, reminder)
                    }
                    .setPositiveButton(R.string.apply) { _, _ -> }
                .show()
        }

        holder.itemView.selectRepeatMonths.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                    .setMultiChoiceItems(
                            monthOptions,
                            monthOptionsValues.map {
                                reminder.repeat?.months?.contains(it) == true
                            }.toBooleanArray()) { dialog, item, checked ->
                        if (checked) {
                            if (reminder.repeat?.months == null) {
                                reminder.repeat?.months = listOf(monthOptionsValues[item])
                            } else {
                                reminder.repeat?.months = reminder.repeat?.months?.toMutableList()?.let {
                                    it.removeIf { monthOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.add(monthOptionsValues[item])
                                    it.sortedBy { monthOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        } else {
                            if (reminder.repeat?.months?.size == 1) {
                                reminder.repeat?.months = null
                            } else {
                                reminder.repeat?.months = reminder.repeat?.months?.toMutableList()?.let {
                                    it.removeIf { monthOptionsValues.indexOf(it) == -1 } // Sanitize
                                    it.remove(monthOptionsValues[item])
                                    it.sortedBy { monthOptionsValues.indexOf(it) }.toList()
                                }
                            }
                        }

                        update(holder, reminder)
                    }
                    .setPositiveButton(R.string.apply) { _, _ -> }
                .show()
        }

    }

    private fun update(holder: EditRemindersViewHolder, reminder: EventReminder) {

        // Offset

        when (reminder.offset.unit) {
            EventReminderOffsetUnit.Minute, EventReminderOffsetUnit.Hour -> false
            else -> true
        }.let { visible ->
            holder.itemView.atText.visible = visible
            holder.itemView.selectTime.visible = visible
        }

        holder.itemView.offsetUnit.text = when (reminder.offset.unit) {
            EventReminderOffsetUnit.Minute -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.plural_minute, abs(reminder.offset.amount))
            EventReminderOffsetUnit.Hour -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.plural_hour, abs(reminder.offset.amount))
            EventReminderOffsetUnit.Day -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.plural_day, abs(reminder.offset.amount))
            EventReminderOffsetUnit.Week -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.plural_week, abs(reminder.offset.amount))
            EventReminderOffsetUnit.Month -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.plural_month, abs(reminder.offset.amount))
        }

        // Position

        holder.itemView.selectPosition.text = if (reminder.offset.amount == 0) {
            on<ResourcesHandler>().resources.getString(R.string.at_time_of_event)
        } else {
            when (reminder.position) {
                EventReminderPosition.Start -> on<ResourcesHandler>().resources.getString(if (reminder.offset.amount < 0) R.string.before_event_starts else R.string.after_event_starts)
                EventReminderPosition.End -> on<ResourcesHandler>().resources.getString(if (reminder.offset.amount < 0) R.string.before_event_ends else R.string.after_event_ends)
            }
        }

        // Time

        val timeFormatter = SimpleDateFormat("h:mma", Locale.US)
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, reminder.time.hour)
            set(Calendar.MINUTE, reminder.time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        holder.itemView.selectTime.text = timeFormatter.format(cal.time)

        // Repeat

        holder.itemView.selectRepeat.text = on<ResourcesHandler>().resources.getString(R.string.none)
        holder.itemView.selectRepeat.setTextColor(on<ResourcesHandler>().resources.getColor(when (reminder.repeat) {
            null -> R.color.textHintInverse
            else -> R.color.colorPrimary
        }))

        when (reminder.repeat) {
            null -> false
            else -> true
        }.let { visible ->
            holder.itemView.selectRepeatHours.visible = visible
            holder.itemView.selectRepeatDays.visible = visible
            holder.itemView.selectRepeatWeeks.visible = visible
            holder.itemView.selectRepeatMonths.visible = visible
        }

        holder.itemView.selectRepeat.text = when(reminder.repeat?.until) {
            EventReminderPosition.Start -> on<ResourcesHandler>().resources.getString(R.string.until_event_starts)
            EventReminderPosition.End -> on<ResourcesHandler>().resources.getString(R.string.until_event_ends)
            else -> on<ResourcesHandler>().resources.getString(R.string.none)
        }

        // Hours

        reminder.repeat?.hours?.let { hours ->
            if (hours.isNotEmpty()) {
                holder.itemView.selectRepeatHours.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                holder.itemView.selectRepeatHours.text = hours.asSequence()
                        .map { hourOptionsValues.indexOf(it) }
                        .filter { it != -1 }
                        .map { hourOptions[it] }
                        .joinToString(if (hours.size == 2) " ${on<ResourcesHandler>().resources.getString(R.string.and)} " else ", ")
            } else null
        } ?: run {
            holder.itemView.selectRepeatHours.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
            holder.itemView.selectRepeatHours.text = on<ResourcesHandler>().resources.getString(R.string.choose_hours)
        }

        // Days

        reminder.repeat?.days?.let { days ->
            if (days.isNotEmpty()) {
                holder.itemView.selectRepeatDays.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                holder.itemView.selectRepeatDays.text = days.asSequence()
                        .map { dayOptionsValues.indexOf(it) }
                        .filter { it != -1 }
                        .map { dayOptions[it] }
                        .joinToString(if (days.size == 2) " ${on<ResourcesHandler>().resources.getString(R.string.and)} " else ", ")
            } else null
        } ?: run {
            holder.itemView.selectRepeatDays.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
            holder.itemView.selectRepeatDays.text = on<ResourcesHandler>().resources.getString(R.string.choose_days)
        }

        // Weeks

        reminder.repeat?.weeks?.let { weeks ->
            if (weeks.isNotEmpty()) {
                holder.itemView.selectRepeatWeeks.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                holder.itemView.selectRepeatWeeks.text = weeks.asSequence()
                        .map { weekOptionsValues.indexOf(it) }
                        .filter { it != -1 }
                        .map { weekOptions[it] }
                        .joinToString(if (weeks.size == 2) " ${on<ResourcesHandler>().resources.getString(R.string.and)} " else ", ")
            } else null
        } ?: run {
            holder.itemView.selectRepeatWeeks.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
            holder.itemView.selectRepeatWeeks.text = on<ResourcesHandler>().resources.getString(R.string.choose_weeks)
        }

        // Months

        reminder.repeat?.months?.let { months ->
            if (months.isNotEmpty()) {
                holder.itemView.selectRepeatMonths.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary))
                holder.itemView.selectRepeatMonths.text = months.asSequence()
                        .map { monthOptionsValues.indexOf(it) }
                        .filter { it != -1 }
                        .map { monthOptions[it] }
                        .joinToString(if (months.size == 2) " ${on<ResourcesHandler>().resources.getString(R.string.and)} " else ", ")
            } else null
        } ?: run {
            holder.itemView.selectRepeatMonths.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
            holder.itemView.selectRepeatMonths.text = on<ResourcesHandler>().resources.getString(R.string.choose_months)
        }
    }

    override fun getItemCount() = items.size
}

class EditRemindersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

}
