package me.proxer.app.helper

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.view.GmailStyleCrossFadeSlidingPaneLayout
import com.mikepenz.materialdrawer.*
import com.mikepenz.materialdrawer.interfaces.ICrossfader
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import me.proxer.app.R
import me.proxer.app.util.DeviceUtils
import me.proxer.library.util.ProxerUrls
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import java.util.*

/**
 * @author Ruben Gees
 */
class MaterialDrawerHelper(context: Activity, toolbar: Toolbar, savedInstanceState: Bundle?,
                           private val itemClickCallback: (id: DrawerItem) -> Boolean,
                           private val accountClickCallback: (view: View, id: AccountItem) -> Boolean) {

    companion object {
        private const val CURRENT_ID_STATE = "current_id"
    }

    private val header: AccountHeader
    private val drawer: Drawer

    private var miniDrawer: MiniDrawer? = null
    private var crossfader: Crossfader<*>? = null

    private var currentItem: DrawerItem? = null

    init {
        header = buildAccountHeader(context, savedInstanceState)
        drawer = buildDrawer(context, toolbar, header, savedInstanceState)

        buildTabletDrawer(context, drawer, savedInstanceState).let {
            miniDrawer = it.first
            crossfader = it.second
        }

        currentItem = DrawerItem.fromOrNull(savedInstanceState?.getLong(CURRENT_ID_STATE))
    }

    fun onBackPressed(): Boolean {
        when {
            crossfader?.isCrossFaded() == true -> {
                crossfader?.crossFade()

                return true
            }
            isDrawerOpen() -> {
                drawer.closeDrawer()

                return true
            }
            else -> {
                val context = when {
                    drawer.drawerLayout != null -> drawer.drawerLayout.context
                    crossfader != null -> crossfader?.getCrossFadeSlidingPaneLayout()?.context
                    else -> null
                } ?: return false

                val startPage = PreferenceHelper.getStartPage(context)

                return if (currentItem != startPage) {
                    select(startPage)

                    true
                } else {
                    false
                }
            }
        }
    }

    fun saveInstanceState(outState: Bundle) {
        outState.putLong(CURRENT_ID_STATE, currentItem?.id ?: DrawerItem.NEWS.id)

        header.saveInstanceState(outState)
        drawer.saveInstanceState(outState)
        crossfader?.saveInstanceState(outState)
    }

    fun isDrawerOpen(): Boolean {
        return drawer.isDrawerOpen
    }

    fun select(item: DrawerItem) {
        drawer.setSelection(item.id)
        miniDrawer?.setSelection(item.id)
    }

    fun refreshHeader() {
        header.profiles = generateAccountItems()
        drawer.recyclerView.adapter.notifyDataSetChanged()
        miniDrawer?.createItems()
    }

    private fun buildAccountHeader(context: Activity, savedInstanceState: Bundle?): AccountHeader {
        return AccountHeaderBuilder()
                .withActivity(context)
                .withCompactStyle(true)
                .withHeaderBackground(R.color.colorPrimary)
                .withOnAccountHeaderListener { view, profile, current ->
                    onAccountItemClick(view, profile, current)
                }
                .withSavedInstance(savedInstanceState)
                .withProfiles(generateAccountItems())
                .build()
    }

    private fun generateAccountItems(): List<IProfile<*>> {
        val user = StorageHelper.user

        when (user) {
            null -> return arrayListOf(
                    ProfileDrawerItem()
                            .withName(R.string.section_guest)
                            .withIcon(R.mipmap.ic_launcher)
                            .withSelectedTextColorRes(R.color.colorAccent)
                            .withIdentifier(AccountItem.GUEST.id),
                    ProfileSettingDrawerItem()
                            .withName(R.string.section_login)
                            .withIcon(CommunityMaterial.Icon.cmd_account_key)
                            .withIdentifier(AccountItem.LOGIN.id)
            )
            else -> return arrayListOf(
                    ProfileDrawerItem()
                            .withName(user.name)
                            .withEmail(R.string.section_user_subtitle)
                            .withIcon(ProxerUrls.userImage(user.image).toString())
                            .withSelectedTextColorRes(R.color.colorAccent)
                            .withIdentifier(AccountItem.USER.id),
                    ProfileSettingDrawerItem()
                            .withName(R.string.section_notifications)
                            .withIcon(CommunityMaterial.Icon.cmd_bell_outline)
                            .withIdentifier(AccountItem.NOTIFICATIONS.id),
                    ProfileSettingDrawerItem()
                            .withName(R.string.section_ucp)
                            .withIcon(CommunityMaterial.Icon.cmd_account_key)
                            .withIdentifier(AccountItem.UCP.id),
                    ProfileSettingDrawerItem()
                            .withName(R.string.section_logout)
                            .withIcon(CommunityMaterial.Icon.cmd_account_remove)
                            .withIdentifier(AccountItem.LOGOUT.id)
            )
        }
    }

    private fun buildDrawer(context: Activity, toolbar: Toolbar, accountHeader: AccountHeader,
                            savedInstanceState: Bundle?): Drawer {
        val builder = DrawerBuilder(context)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withDrawerItems(generateDrawerItems())
                .withStickyDrawerItems(generateStickyDrawerItems())
                .withOnDrawerItemClickListener { view, id, item ->
                    onDrawerItemClick(view, id, item)
                }
                .withPositionBasedStateManagement(false)
                .withShowDrawerOnFirstLaunch(true)
                .withTranslucentStatusBar(true)
                .withGenerateMiniDrawer(DeviceUtils.isTablet(context))
                .withSavedInstance(savedInstanceState)

        return if (DeviceUtils.isTablet(context)) builder.buildView() else builder.build()
    }

    private fun buildTabletDrawer(context: Activity, drawer: Drawer, savedInstanceState: Bundle?):
            Pair<MiniDrawer?, Crossfader<*>?> {

        if (DeviceUtils.isTablet(context)) {
            val miniDrawer = drawer.miniDrawer.withIncludeSecondaryDrawerItems(true)
            val crossfader = buildCrossfader(context, drawer, miniDrawer, savedInstanceState)

            miniDrawer.withCrossFader(CrossfadeWrapper(crossfader))
            crossfader.getCrossFadeSlidingPaneLayout()?.setShadowResourceLeft(R.drawable.material_drawer_shadow_left)

            return miniDrawer to crossfader
        }

        return null to null
    }

    private fun buildCrossfader(context: Activity, drawer: Drawer, miniDrawer: MiniDrawer,
                                savedInstanceState: Bundle?): Crossfader<*> {
        return Crossfader<GmailStyleCrossFadeSlidingPaneLayout>()
                .withContent(context.find<ViewGroup>(R.id.root))
                .withFirst(drawer.slider, context.dip(300))
                .withSecond(miniDrawer.build(context), context.dip(72))
                .withSavedInstance(savedInstanceState)
                .build()
    }

    private fun generateDrawerItems(): List<IDrawerItem<*, *>> {
        return arrayListOf(
                PrimaryDrawerItem()
                        .withName(R.string.section_news)
                        .withIcon(CommunityMaterial.Icon.cmd_newspaper)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.NEWS.id),
                PrimaryDrawerItem()
                        .withName(R.string.section_chat)
                        .withIcon(CommunityMaterial.Icon.cmd_message_text)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.CHAT.id),
                PrimaryDrawerItem()
                        .withName(R.string.section_bookmarks)
                        .withIcon(CommunityMaterial.Icon.cmd_bookmark)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.BOOKMARKS.id),
                PrimaryDrawerItem()
                        .withName(R.string.section_anime)
                        .withIcon(CommunityMaterial.Icon.cmd_television)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.ANIME.id),
                PrimaryDrawerItem()
                        .withName(R.string.section_manga)
                        .withIcon(CommunityMaterial.Icon.cmd_book_open_page_variant)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.MANGA.id),
                PrimaryDrawerItem()
                        .withName(R.string.section_local_manga)
                        .withIcon(CommunityMaterial.Icon.cmd_cloud_download)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.LOCAL_MANGA.id)
        )
    }

    private fun generateStickyDrawerItems(): ArrayList<IDrawerItem<*, *>> {
        return arrayListOf(
                PrimaryDrawerItem()
                        .withName(R.string.section_info)
                        .withIcon(CommunityMaterial.Icon.cmd_information_outline)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.INFO.id),
                PrimaryDrawerItem()
                        .withName(R.string.section_donate)
                        .withIcon(CommunityMaterial.Icon.cmd_gift)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withSelectable(false)
                        .withIdentifier(DrawerItem.DONATE.id),
                PrimaryDrawerItem()
                        .withName(R.string.section_settings)
                        .withIcon(CommunityMaterial.Icon.cmd_settings)
                        .withSelectedTextColorRes(R.color.colorAccent)
                        .withSelectedIconColorRes(R.color.colorAccent)
                        .withIdentifier(DrawerItem.SETTINGS.id)
        )
    }

    private fun getStickyItemIds(): Array<DrawerItem> {
        return generateStickyDrawerItems()
                .mapNotNull { DrawerItem.fromOrNull(it.identifier) }
                .toTypedArray()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onDrawerItemClick(view: View?, id: Int, item: IDrawerItem<*, *>): Boolean {
        if (item.identifier != currentItem?.id) {
            val newItem = DrawerItem.fromOrNull(item.identifier) ?: return false

            if (item.isSelectable) {
                currentItem = newItem

                if (miniDrawer != null && newItem in getStickyItemIds()) {
                    miniDrawer?.adapter?.deselect()
                }
            }

            return itemClickCallback.invoke(newItem)
        }

        return true
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onAccountItemClick(view: View, profile: IProfile<*>, current: Boolean): Boolean {
        return accountClickCallback.invoke(view, AccountItem.fromOrDefault(profile.identifier))
    }

    enum class DrawerItem(val id: Long) {
        NEWS(0L),
        CHAT(1L),
        BOOKMARKS(2L),
        ANIME(3L),
        MANGA(4L),
        LOCAL_MANGA(5L),
        INFO(10L),
        DONATE(11L),
        SETTINGS(12L);

        companion object {
            fun fromOrNull(id: Long?) = values().firstOrNull { it.id == id }
            fun fromOrDefault(id: Long?) = fromOrNull(id) ?: NEWS
        }
    }

    enum class AccountItem(val id: Long) {
        GUEST(100L),
        LOGIN(101L),
        USER(102L),
        LOGOUT(103L),
        NOTIFICATIONS(104L),
        UCP(105L);

        companion object {
            fun fromOrNull(id: Long?) = values().firstOrNull { it.id == id }
            fun fromOrDefault(id: Long?) = fromOrNull(id) ?: USER
        }
    }

    private class CrossfadeWrapper(private val crossfader: Crossfader<*>) : ICrossfader {
        override fun crossfade() = crossfader.crossFade()
        override fun isCrossfaded() = crossfader.isCrossFaded()
    }
}
