package com.ufony.trackersdk


import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class UserPreferenceManager private constructor(private val prefName: String, private val userId: Long, private val context: Context) {
    companion object {
        private const val DEFAULT_PREF_NAME = "shared_prefs"
        private const val DEFAULT_USER_ID = "default_user_id"
        private const val AUTHO = "shared_prefs"
        private const val DEFAULT_RECIPIENT = "default_recipient"
        private const val USER_SUMMARY_CALL_TIME = "user_summary_call_time"



        fun forUser(userId: Long, context: Context): UserPreferenceManager {
            Log.d("VIREN-USER", userId.toString())
            return UserPreferenceManager(DEFAULT_PREF_NAME + "_" + userId, userId, context)
        }
    }

    /**
     *  Shared preferences for loggedIn-selected user
     */
    val userSharedPreferences: SharedPreferences
        get() {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        }

    private inline fun <reified T> getParsedJsonFromPref(key: String): T? {
        val value = userSharedPreferences.getString(key, null);
        return try {
            value?.fromJson<T>()
        } catch (t: Throwable){
             null
        }
    }



    private fun parseAndSaveJsonToPref(key: String, value: Any?) {
        saveToPref(key, value?.toJson())
    }

    private inline fun <reified T> getValueFromPref(key: String, defaultValue: T): T {
        return userSharedPreferences.run {
            when (defaultValue) {
                is Long -> {
                    getLong(key, defaultValue as Long) as T
                }
                is Boolean -> {
                    getBoolean(key, defaultValue) as T
                }
                is Int -> {
                    getInt(key, defaultValue) as T
                }
                is Float -> {
                    getFloat(key, defaultValue) as T
                }
                else -> {
                    getString(key, defaultValue.toString()) as T
                }
            }
        }
    }

    public inline fun <reified T> getValueFromPrefNullable(key: String, defaultValue: T? = null): T? {
        return userSharedPreferences.run {
            val typeName = T::class.java.name
            when (typeName) {
                "java.lang.String" -> {
                    if (defaultValue != null) getString(key, defaultValue.toString()) as T else getString(key, defaultValue?.toString()) as T?
                }
                "java.lang.Long" -> {
                    when {
                        defaultValue != null -> getLong(key, defaultValue as Long) as T
                        contains(key) -> getLong(key, 0) as T
                        else -> defaultValue
                    }
                }
                "java.lang.Boolean" -> {
                    when {
                        defaultValue != null -> getBoolean(key, defaultValue as Boolean) as T
                        contains(key) -> getBoolean(key, false) as T
                        else -> defaultValue
                    }
                }
                "java.lang.Integer" -> {
                    when {
                        defaultValue != null -> getInt(key, defaultValue as Int) as T
                        contains(key) -> getInt(key, 0) as T
                        else -> defaultValue
                    }
                }
                "java.lang.Float" -> {
                    when {
                        defaultValue != null -> getFloat(key, defaultValue as Float) as T
                        contains(key) -> getFloat(key, 0F) as T
                        else -> defaultValue
                    }
                }
                else -> {
                    getString(key, defaultValue?.toString()) as T?
                }
            }
        }
    }

    var currentUser: UserResponse?
        get() {
            return getParsedJsonFromPref(UserPreferenceMangerKeys.USER)
        }
        set(value) {
            saveToPref(UserPreferenceMangerKeys.USER, value)
        }

    private fun saveToPref(key: String, value: Any?) {
        userSharedPreferences.edit(commit = true) {
            if (value != null) {
                when (value) {
                    is Long -> {
                        putLong(key, value)
                    }
                    is Int -> {
                        putInt(key, value)
                    }
                    is Boolean -> {
                        putBoolean(key, value)
                    }
                    is String -> {
                        putString(key, value)
                    }
                    else -> {
                        putString(key, value.toJson())
                    }
                }
            } else {
                remove(key)
            }
        }
    }
    var authorisation:String?
        get() {
            return getValueFromPrefNullable<String>(UserPreferenceMangerKeys.AUTHORISATION,null)
        }
        set(value) {
            saveToPref(UserPreferenceMangerKeys.AUTHORISATION,value)
        }
    var trips: Trip?
        get() {
            return getParsedJsonFromPref(UserPreferenceMangerKeys.TRIPS)
        }
        set(value) {
            saveToPref(UserPreferenceMangerKeys.TRIPS, value)
        }
    var singleTrip: String?
        get() {
            return getValueFromPrefNullable<String>(UserPreferenceMangerKeys.singleTrip,null)
        }
        set(value) {
            saveToPref(UserPreferenceMangerKeys.singleTrip, value)
        }

    var connectionId: String?
        get() {
            return getValueFromPrefNullable(UserPreferenceMangerKeys.LAST_CONNECTION_ID)
        }
        set(value) {
            saveToPref(UserPreferenceMangerKeys.LAST_CONNECTION_ID, value)
        }

    fun setTripSubScription(trips: String) {
        saveToPref(UserPreferenceMangerKeys.SUBSCRIBED_USER, trips)
    }

    fun getTripSubScription(): ArrayList<TripSubscription> {
        val subscription = getParsedJsonFromPref<Subscription>(UserPreferenceMangerKeys.SUBSCRIBED_USER)

        return subscription?.trips ?: ArrayList()
    }


//    fun getDefaultRecipient(): Authorization.DefaultRecipient? {
//        return getValueFromPrefNullable<String>(DEFAULT_RECIPIENT, null)?.fromJson()
//    }

//    fun setDefaultRecipient(value: Authorization.DefaultRecipient?) {
//        saveToPref(DEFAULT_RECIPIENT, value?.toJson())
//    }

}


object UserPreferenceMangerKeys{
    const val USER = "shared_user"
    const val GRADES = "allGrades"
    const val CHILDS = "allChilds"
    const val CHANNELS = "CHANNELS"
    const val ATTENDANCE_OF_MONTH = "attendance_of_month"
    const val ATTENDANCE_OF_GRADE = "attendance_of_grade"
    const val CREATED_BY_ROLE = "created_by_role"
    const val CREATED_BY_Id = "created_by_Id"
    const val BIRTHDAYS = "birthdays"
    const val PUSHDETAILS = "push_details"
    const val PRODUCTDETAILS = "product_details"
    const val STORE_ORDER_EXCHANGE_TEMP_STORE = "store_order_exchange_temp_store"
    const val PRODUCT_DETAILS_RETURNED = "product_details_returned"
    const val PICKUP_DETAILS_RETURNED = "pickup_details_returned"
    const val SHOW_CONTROL_NUMBER = "control_number"
    const val ATTENDANCE_TIP = "attendance_tip"
    const val USER_UNATHORISED = "user_unathourised"
    const val AUTHORISATION = "authorisation"
    const val NOTIFICATION_POPUP_PERMISSION = "notification_permission"

    const val LOCATION_LAT = "lat"
    const val LOCATION_LON = "lon"
    const val REG_ID = "reg_id"
    const val HIGHLIGHT_NOTIFICATION_ID = "high_light_notification_id"
    const val IS_VERSION_UPDATE = "is_version_update"
    const val LAST_UPGRADE_SHOWN_TIME = "lastUpgradeShownTime"
    const val LAST_CONNECTION_ID = "connectionId"
    const val SUBSCRIBED_USER = "subscribe_user"

    const val IS_THREAD_READ = "is_thread"
    const val IS_FILECOPIED = "is_file_copied"
    const val THREADID = "threadid"
    const val IS_FORCEUPDATE = "isForceUpdate"
    const val CHILD_PROFILE_PIC_UPDTAE = "CHILD_PROFILE_PIC_UPDTAE"

    const val MODULES = "UserModules"
    const val UNREAD_MESSAGES = "unreadMessages"
    const val ROLE_PERMISSION = "RolePermission"
    const val FEES_FLAG = "FEES_FLAG"
    const val BACKGROUND_TIME = "background_time"
    const val PREVIOUS_DAY_TIME = "previous_day_time"
    const val FEES_CHILDID="fees_childId"
    const val ID_NEWER_THAN = "newer_than_id"
    const val ID_OLDER_THAN = "older_than_id"


    const val SCHOOL_DETAILS = "school_details"
    const val USER_ROLE = "userrole"
    const val SUMMARY = "summary"
    const val CHAT_WALLPAPER = "ChatWallPaper"
    const val MAP_MAX_ZOOM_LEVEL = "map_max_zoom_level"

    const val AUTHORIZED_PEOPLE = "authorized_people"
    const val BIRTHDAY_REMINDER = "Birthday_Reminder"
    const val BIRTHDAY_REMINDER_TIME = "Birthday_Reminder_Time"
    const val CHILD_FEES = "child_fees"
    const val WORD_LIST = "word_list"
    const val WORD_A_DAY_COUNT = "word_count"
    const val WORD_A_DAY_STATUS = "word_a_day_status"
    const val PREVIOUS_DAY_TIME_FOR_WORD = "word_a_day_time"

    const val NEWER_THAN_DATE_TIME = "requestDateTime"
    const val OLDER_THAN_DATE_TIME = "olderThanDateTime"
    const val STORE_KIT_FLOW = "storeKitFlow"
    const val STORE_HELP_FLOW = "storehelpFlow"
    const val STORE_KIT_FLOW_OPTIONAL = "storeKitFlowOptional"
    const val STORE_NORMAL_FLOW = "storeNormalFlow"
    const val STORE_SELECTED_ITEM = "storeSelectedItem"
    const val STORE_SELECTED_ITEM_URL = "storeSelectedItemUrl"
    const val STORE_SELECTED_ITEM_SIZE = "storeSelectedItemSize"
    const val STORE_SELECTED_SCREEN = "storeSelectedScreen"
    const val STORE_SELECTED_ITEM_POSITION = "storeSelectedItemPosition"
    const val STORE_VENDOR_ID = "storeVendorId"
    const val STORE_CATEGORY_ID = "storeCategoryId"
    const val ORDER_Id = "orderId"
    const val RATING_Id = "ratingId"
    const val RATING_URL = "ratingUrl"
    const val RATING_ORDER_ID = "ratingOrderId"
    const val PRODUCT_NAME = "productName"
    const val PRODUCT_URL = "productUrl"
    const val PRODUCT_SIZE = "productSize"
    const val CHILD_Id = "childId"
    //Channel
    const val CHANNEL_PEDNING = "channel_pending"
    const val CHANNEL_PEDNING_NEWERTHAN = "channel_pending_newerthan"
    const val CHANNEL_THREADID="thread_id"
    const val CHANNEL_NEWTHREAID="new_thread"
    const val CHANNEL_ISHOMEWORK="is_homework"
    const val CHANNEL_MESSAGEID="message_id"
    const val CHANNEL_MESSAGE="message"
    const val WALL_MESSAGE="wall_message"
    const val CHANNEL_MESSAGE_GRADEID="message_grade_id"
    const val CHANNEL_APPROVED = "channel_approved"
    const val CHANNEL_REJECTED = "channel_rejected"
    const val CHANNEL_APPROVED_NEWERTHAN = "channel_approved_newerthan"
    const val CHANNEL_REJECTED_NEWERTHAN = "channel_rejected_newerthan"
    const val CHILD_ENROLLMENT = "child_enrollment"
    const val PROGRESSBAR_STOPED = "progressbar_stoped"
    const val STORE_CHILD_DETAILS = "child_details"

    //Notification
    const val NOTIFICATION_RESPONSE = "notification_response"
    const val NOTIFICATION_NEWERTHAN = "notification_newerthan"
    const val NOTIFICATION_OLDERTHAN = "notification_olderthan"

    //Tracker
    const val TRIPS = "trips"
    const val singleTrip = "singleTrip"
    //intro screen
    const val IS_INTRO_SCREEN = "isIntroScreen"
    const val ACTIVE_TRIPS = "active_trips"
    //NewerThan time
    const val NEWER_THAN_CONTACT_TIME = "contact_requestDateTime"
    const val NEWER_THAN_GRADE_TIME = "grade_requestDateTime"
    const val NEWER_THAN_CHILDREN_TIME = "child_requestDateTime"
    const val NEWER_THAN_OBSERVATION_TIME = "observation_requestDateTime"


    const val FOOD_DATA_LIST = "food_data_list"
    const val LIQUID_DATA_LIST = "liquid_data_list"
    const val DAYCARE_HEADER = "daycare_header"
    const val CHECK_VERSION = "check_version"
    const val DAYCARE_ACTIVITIES = "daycare_activities"
    const val DAYCARE_DATE_TIME = "daycare_DateTime"
    const val LOGIN_DATE_TIME = "login_DateTime"
    const val MY_ORDERS = "my_orders"
    const val STORE_HELP_SCREEN = "store_help_screen"
    const val INTRO_SHOWCASE = "intro_show_case"

    const val ATTENDANCE_TYPES_LIST = "attendance_types_list"
    const val ATTENDANCE_DEFAULT_PERMISSION = "attendance_default_permission"

    const val STORE_CATEGORY = "store_category"

    const val PENDING_RATINGS = "pending_ratings"
    const val TAP_SEQUENCE="tap_sequence"
    const val TAP_CENTER_SEQUENCE="tap_center_sequence"
    //Library
    const val SEARCH_BOOK = "search_book"

    const val CAMERA_URI = "camera_uri"

    const val REPLIES_NEWER_THAN = "replies_newer_than_id"
    const val REPLIES_OLDER_THAN = "replies_older_than_id"
    const val REPLIES_NEWER_THAN_DATE_TIME = "replies_requestDateTime"


    const val QUESTION_LIST = "question_list"
    const val TIMEZONE_OFFSET = "timezone_offset"
    const val TOTAL_NOTIFICATION = "total_notification"
    const val CHILD_ID = "child_id"
    const val SKIPPED_DB_UPDATE_V39 = "skipped_db_update_v39";
    const val QUESTION_POSISTION = "question_posistion"

    //TC FLOW
    const val SCHOOL_BRANCH_ID = "school_branch_id"
    const val isTCFLOW = "is_tc_flow";
    //Parent Concern
    const val SUB_CATEGORY_ID = "sub_category_id"
    const val IS_PARENT_CONCERN = "is_parent_concern"
    const val TEMP_IMAGES = "temp_images"
    const val PARENT_CONCERN_NEWER_THAN = "newer_than"
    const val CONCERN_MESSAGE_NEWER_THAN = "message_newer_than"
    const val IS_LIST_EMPTY = "is_list_empty"
    const val CONCERNID = "concernId"
    const val isFirstTimeOpen = "isFirstTimeOpen"
    const val IS_APP_UPDATED="app_update"
    //Survey
    const val SURVEY_QUESTION_ID = "question_id"
    const val SURVEY_CHILD_ID = "child_id"
}

fun Any.toJson() = Gson().toJson(this)

inline fun <reified T> String.fromJson(): T {
    val collectionType = object : TypeToken<T>() {

    }.type
    return Gson().fromJson(this, collectionType)
}

inline fun <reified T> String.fromJson(typeOfT: Type): T {
    return Gson().fromJson(this, typeOfT)
}