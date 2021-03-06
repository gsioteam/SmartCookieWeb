// Copyright 2020 CookieJarApps MPL
package com.cookiegames.smartcookie.popup

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.cookiegames.smartcookie.AppTheme
import com.cookiegames.smartcookie.IncognitoActivity
import com.cookiegames.smartcookie.R
import com.cookiegames.smartcookie.browser.TabsManager
import com.cookiegames.smartcookie.browser.activity.BrowserActivity
import com.cookiegames.smartcookie.controller.UIController
import com.cookiegames.smartcookie.database.HistoryEntry
import com.cookiegames.smartcookie.di.injector
import com.cookiegames.smartcookie.dialog.BrowserDialog
import com.cookiegames.smartcookie.extensions.copyToClipboard
import com.cookiegames.smartcookie.extensions.snackbar
import com.cookiegames.smartcookie.preference.UserPreferences
import com.cookiegames.smartcookie.reading.activity.ReadingActivity
import com.cookiegames.smartcookie.settings.activity.SettingsActivity
import com.cookiegames.smartcookie.utils.IntentUtils
import com.cookiegames.smartcookie.utils.Utils
import com.cookiegames.smartcookie.utils.isSpecialUrl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_interface.*
import java.util.*
import javax.inject.Inject

class PopUpClass {
    private var list: ListView? = null
    private var uiController: UIController? = null

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var tabsManager: TabsManager

    //PopupWindow display method
    fun showPopupWindow(view: View, activity: BrowserActivity) {
        view.context.injector.inject(this)
        //Create a View object yourself through inflater
        val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.toolbar_menu, null)
        val r = view.context.resources

        val px = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 228f, r.displayMetrics))

        //Specify the length and width through constants
        val height = LinearLayout.LayoutParams.MATCH_PARENT

        //Make Inactive Items Outside Of PopupWindow
        val focusable = true
        uiController = view.context as UIController


        //Create a window with our parameters
        val popupWindow = PopupWindow(popupView, px, height, focusable)
        val relView = popupView.findViewById<RelativeLayout>(R.id.toolbar_menu)

        if(activity.isIncognito()){
            val incognitoHeight = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 628f, r.displayMetrics))
            popupView.findViewById<ConstraintLayout>(R.id.transparent_container).maxHeight = incognitoHeight
            popupView.findViewById<ConstraintLayout>(R.id.transparent_container).setBackgroundColor(view.context.resources.getColor(R.color.black))
        }
        else if(userPreferences.useTheme == AppTheme.DARK || userPreferences.useTheme == AppTheme.BLACK){
            popupView.findViewById<ConstraintLayout>(R.id.transparent_container).setBackgroundColor(view.context.resources.getColor(R.color.black))
        }

        //Set the location of the window on the screen
        if (userPreferences!!.bottomBar) {
            popupWindow.animationStyle = R.style.ToolbarAnimReverse
            popupWindow.showAtLocation(view, Gravity.BOTTOM or Gravity.END, 0, 0)
            relView.gravity = Gravity.BOTTOM
        } else {
            popupWindow.animationStyle = R.style.ToolbarAnim
            popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 0, 0)
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val resources = view.context.resources
        var textString = arrayOf(resources.getString(R.string.action_new_tab), resources.getString(R.string.action_incognito), resources.getString(R.string.action_share), resources.getString(R.string.action_print), resources.getString(R.string.action_history), resources.getString(R.string.action_downloads), resources.getString(R.string.action_find), resources.getString(R.string.action_copy), resources.getString(R.string.action_add_to_homescreen), resources.getString(R.string.action_bookmarks), resources.getString(R.string.reading_mode), resources.getString(R.string.settings))
        var drawableIds = intArrayOf(R.drawable.ic_round_add, R.drawable.incognito_mode, R.drawable.ic_share_black_24dp, R.drawable.ic_round_print_24, R.drawable.ic_history, R.drawable.ic_file_download_black_24dp, R.drawable.ic_search, R.drawable.ic_content_copy_black_24dp, R.drawable.ic_round_smartphone, R.drawable.state_ic_bookmark, R.drawable.ic_action_reading, R.drawable.ic_round_settings)

        if(activity.isIncognito()){
            textString = arrayOf(resources.getString(R.string.action_new_tab), resources.getString(R.string.action_print), resources.getString(R.string.action_find), resources.getString(R.string.action_copy), resources.getString(R.string.action_add_to_homescreen), resources.getString(R.string.action_bookmarks), resources.getString(R.string.reading_mode), resources.getString(R.string.settings), resources.getString(R.string.quit_private))
            drawableIds = intArrayOf(R.drawable.ic_round_add, R.drawable.ic_round_print_24, R.drawable.ic_search, R.drawable.ic_content_copy_black_24dp, R.drawable.ic_round_smartphone, R.drawable.state_ic_bookmark, R.drawable.ic_action_reading, R.drawable.ic_round_settings, R.drawable.ic_action_back)
        }


        if (userPreferences!!.bottomBar) {
            textString.reverse()
            for (i in 0 until drawableIds.size / 2) {
                val temp = drawableIds[i]
                drawableIds[i] = drawableIds[drawableIds.size - i - 1]
                drawableIds[drawableIds.size - i - 1] = temp
            }
        }

        val adapter = CustomAdapter(view.context, textString, drawableIds)
        list = popupView.findViewById<ListView>(R.id.menuList)
        list?.setAdapter(adapter)
        list?.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            var positionList = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            if(activity.isIncognito() && !userPreferences.bottomBar){
                positionList = intArrayOf(0, 3, 6, 7, 8, 9, 10, 11, 12)
            }
            else if(activity.isIncognito()){
                positionList = intArrayOf(12, 11, 10, 9, 8, 7, 6, 3, 0)
            }
            else if(userPreferences.bottomBar){
                positionList = intArrayOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
            }
            var currentView = activity.tabsManager.currentTab
            var currentUrl = uiController!!.getTabModel().currentTab?.url

            if (positionList[position] == 0) {
                uiController!!.newTabButtonClicked()
            } else if (positionList[position] == 1) {
                val incognito = Intent(view.context, IncognitoActivity::class.java)
                view.context.startActivity(incognito)
            } else if (positionList[position] == 2) {
                IntentUtils(activity).shareUrl(currentUrl, currentView?.title)
            } else if (positionList[position] == 3) {
                currentView!!.webView?.let { currentView.createWebPagePrint(it) }
            } else if (positionList[position] == 4) {
                currentView?.loadHistoryPage()
            } else if (positionList[position] == 5) {
                currentView?.loadDownloadsPage()
            } else if (positionList[position] == 6) {
                activity.findInPage()
            } else if (positionList[position] == 7) {
                if (currentUrl != null && !currentUrl.isSpecialUrl()) {
                    activity.clipboardManager.copyToClipboard(currentUrl)
                    activity.snackbar(R.string.message_link_copied)
                }
            } else if (positionList[position] == 8) {
                if (currentView != null
                        && currentView.url.isNotBlank()
                        && !currentView.url.isSpecialUrl()) {
                    HistoryEntry(currentView.url, currentView.title).also {
                        Utils.createShortcut(activity, it, currentView.favicon ?: activity.webPageBitmap!!)
                    }
                }
            } else if (positionList[position] == 9) {
               activity.drawer_layout.openDrawer(activity.getBookmarkDrawer())
            } else if (positionList[position] == 10) {
                if (currentUrl != null) {
                    ReadingActivity.launch(view.context, currentUrl)
                }
            } else if (positionList[position] == 11) {
                val settings = Intent(view.context, SettingsActivity::class.java)
                view.context.startActivity(settings)
            }
            else if (positionList[position] == 12) {
                activity.onBackPressed()
                activity.finish()
            }
            popupWindow.dismiss()
        })


        //Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener { v, event -> //Close the window when clicked
            popupWindow.dismiss()
            true
        }
    }
}