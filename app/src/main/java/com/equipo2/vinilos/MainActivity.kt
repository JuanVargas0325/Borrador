package com.equipo2.vinilos

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.databinding.ActivityMainBinding
import com.equipo2.vinilos.ui.common.AppSnackbar
import com.equipo2.vinilos.ui.common.Motion
import com.equipo2.vinilos.ui.common.ThemeModes
import com.equipo2.vinilos.ui.common.useTabTransitions
import com.equipo2.vinilos.ui.role.RoleMenu
import com.equipo2.vinilos.ui.session.SessionEvent
import com.equipo2.vinilos.ui.session.SessionViewModel
import kotlinx.coroutines.launch
import kotlin.math.max

/** Actividad única: barra superior con el chip de rol, contenedor de navegación y barra inferior. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var roleMenu: RoleMenu
    private val session: SessionViewModel by viewModels { SessionViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        navController = binding.navHost.getFragment<NavHostFragment>().navController
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        setUpRoleMenu()
        setUpBottomNavigation()
        navController.addOnDestinationChangedListener { _, destination, _ -> onDestinationChanged(destination) }
        observeSession()

        // La actividad se recrea al cambiar el tema; si el menú estaba abierto, se vuelve a abrir sin animación.
        if (savedInstanceState?.getBoolean(KEY_ROLE_MENU_OPEN) == true) {
            binding.roleChip.root.post { showRoleMenu(animate = false) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_ROLE_MENU_OPEN, roleMenu.isShowing)
    }

    override fun onDestroy() {
        roleMenu.dismiss()
        super.onDestroy()
    }

    /** Muestra un aviso sobre el elemento inferior visible: el indicado en [anchor] o la barra inferior. */
    fun showSnackbar(
        message: CharSequence,
        isError: Boolean = false,
        anchor: View? = null,
        actionText: CharSequence? = null,
        action: (() -> Unit)? = null,
    ) {
        val target = anchor?.takeIf { it.isShown } ?: binding.bottomNav.takeIf { it.isShown }
        AppSnackbar.show(binding.root, message, isError, target, actionText, action)
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, max(bars.bottom, keyboard.bottom))
            WindowInsetsCompat.CONSUMED
        }
    }

    /** Cada pestaña conserva su estado y, desde Artistas o Coleccionistas, Atrás vuelve a Álbumes. */
    private fun setUpBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id != item.itemId) {
                // La pestaña actual sale con fade through aunque haya abierto antes una pantalla completa.
                currentFragment()?.useTabTransitions()
                navController.navigate(item.itemId, null, navOptions {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(R.id.albumListFragment) { saveState = true }
                })
            }
            true
        }
        binding.bottomNav.setOnItemReselectedListener { }
    }

    private fun setUpRoleMenu() {
        roleMenu = RoleMenu(
            anchor = binding.roleChip.root,
            onRoleSelected = session::changeRole,
            onDarkModeChanged = ::changeDarkMode,
            onGoHome = ::goHome,
        )
        binding.roleChip.root.setOnClickListener { showRoleMenu() }
    }

    private fun goHome() {
        currentFragment()?.exitTransition = Motion.fade()
        navController.navigate(R.id.action_global_roleSelect)
    }

    private fun currentFragment(): Fragment? =
        binding.navHost.getFragment<NavHostFragment>().childFragmentManager.primaryNavigationFragment

    private fun showRoleMenu(animate: Boolean = true) {
        roleMenu.show(session.role.value, ThemeModes.isDark(this), animate)
    }

    /** Deja terminar la animación del interruptor antes de aplicar el tema, que recrea la actividad. */
    private fun changeDarkMode(enabled: Boolean) {
        session.setDarkMode(enabled)
        binding.root.postDelayed({ ThemeModes.apply(session.themeMode.value) }, SWITCH_ANIMATION_MS)
    }

    private fun observeSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { session.role.collect(::bindRoleChip) }
                launch {
                    session.events.collect { event ->
                        if (event is SessionEvent.RoleChanged) {
                            val message = if (event.role == Role.COLLECTOR) R.string.snackbar_role_collector else R.string.snackbar_role_visitor
                            showSnackbar(getString(message))
                        }
                    }
                }
            }
        }
    }

    private fun bindRoleChip(role: Role?) {
        val roleName = getString(RoleMenu.roleNameRes(role))
        binding.roleChip.roleChipIcon.setImageResource(if (role == Role.COLLECTOR) R.drawable.ic_album else R.drawable.ic_person)
        binding.roleChip.roleChipText.text = roleName
        binding.roleChip.root.contentDescription = getString(R.string.cd_change_role, roleName)
    }

    /** Barras según la pantalla: ninguna en la selección de rol; sin barra inferior ni chip en Crear álbum. */
    private fun onDestinationChanged(destination: NavDestination) {
        roleMenu.dismiss()
        val isRoleSelect = destination.id == R.id.roleSelectFragment
        val isTab = destination.id in TAB_DESTINATIONS
        binding.toolbar.isVisible = !isRoleSelect
        binding.toolbarDivider.isVisible = !isRoleSelect
        binding.bottomNav.isVisible = isTab
        binding.bottomNavDivider.isVisible = isTab
        binding.roleChip.root.isVisible = isTab
        binding.toolbar.title = destination.label
        if (isTab) {
            binding.toolbar.navigationIcon = null
            binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
        } else if (destination.id == R.id.albumCreateFragment) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_close)
            binding.toolbar.navigationContentDescription = getString(R.string.cd_close_without_saving)
        }
    }

    private companion object {
        val TAB_DESTINATIONS = setOf(R.id.albumListFragment, R.id.artistsFragment, R.id.collectorsFragment)
        const val KEY_ROLE_MENU_OPEN = "role_menu_open"
        const val SWITCH_ANIMATION_MS = 250L
    }
}
