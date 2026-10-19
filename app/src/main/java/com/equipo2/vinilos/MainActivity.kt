package com.equipo2.vinilos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.equipo2.vinilos.databinding.ActivityMainBinding
import kotlin.math.max

/** Actividad única: barra superior, contenedor de navegación y barra inferior. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        navController = binding.navHost.getFragment<NavHostFragment>().navController
        navController.addOnDestinationChangedListener { _, destination, _ -> onDestinationChanged(destination) }
        setUpBottomNavigation()
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

    private fun onDestinationChanged(destination: NavDestination) {
        binding.toolbar.title = destination.label
        binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
    }
}
