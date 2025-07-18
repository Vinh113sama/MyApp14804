package com.example.myapp.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.myapp.R
import com.example.myapp.databinding.ActivityHomeBinding
import com.example.myapp.databinding.DialogLogoutBinding
import com.example.myapp.databinding.LayoutTopInterfaceBinding
import com.example.myapp.databinding.MiniPlayerBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import com.example.myapp.repository.AppViewModelProvider
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    val musicViewModel = AppViewModelProvider.provideMusicPlayerViewModel()
    private lateinit var miniPlayerBinding: MiniPlayerBinding
    private var rotationAnimator: ObjectAnimator? = null
    private var currentRotation = 0f

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.miniPlayerContainer) { view, insets ->
            insets
        }

        initViews()

        val headerBinding = LayoutTopInterfaceBinding.bind(navView.getHeaderView(0))
        lifecycleScope.launch {
            val result = viewModel.getUserInformation()
            headerBinding.tvUserName.text = result.name
        }

        miniPlayerBinding = MiniPlayerBinding.bind(binding.miniPlayerContainer)

        setupNavigationMenu()
        setupMiniPlayer()
        navHostFragment.navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("favoriteChanged")
            ?.observe(this) { changed ->
                if (changed == true) {
                    processFavorite()
                    navHostFragment.navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("favoriteChanged", false)
                }
            }

    }

    private val viewModel: SongViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = SongRepository(RetrofitClient.apiService)
                return SongViewModel(repository) as T
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupMiniPlayer() {
        musicViewModel.currentSong.observe(this) { song ->
//            navHostFragment.navController.currentBackStackEntry
//                ?.savedStateHandle
//                ?.set("updateHistory", true)
            if (song != null) {
                miniPlayerBinding.tvMiniTitle.text = song.title
                miniPlayerBinding.tvMiniArtist.text = song.artist.name
                Glide.with(this)
                    .load(song.imageUrl)
                    .override(600, 600)
                    .placeholder(R.drawable.image_no_available)
                    .error(R.drawable.image_no_available)
                    .into(miniPlayerBinding.imgMiniSong)
            } else {
                miniPlayerBinding.root.visibility = View.GONE
            }
        }

        binding.miniPlayerContainer.setOnClickListener {
            val playlist = musicViewModel.playlist.value ?: emptyList()
            val position = playlist.indexOf(musicViewModel.currentSong.value)
            val intent = Intent(this, PlaySongActivity::class.java)
            intent.putParcelableArrayListExtra("playlist", ArrayList(playlist))
            intent.putExtra("position", position)
            intent.putExtra("fromMiniPlayer", true)
            navHostFragment.navController.currentBackStackEntry?.savedStateHandle?.set("updateHistory", true)
            startActivity(intent)
        }

        miniPlayerBinding.imgbtnNext.setOnClickListener {
            musicViewModel.playNext()
            navHostFragment.navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("updateHistory", true)
        }

        miniPlayerBinding.imgbtnPlayPause.setOnClickListener {
            musicViewModel.togglePlayPause()
        }

        musicViewModel.isPlaying.observe(this) { isPlaying ->
            processFavorite()
            updatePlayPauseIcon(isPlaying)
        }

        miniPlayerBinding.imgbtnFavorite.setOnClickListener {
            val song = musicViewModel.currentSong.value
            if (song != null) {
                viewModel.toggleFavorite(song, onComplete = {
                    processFavorite()
                    val message = if (song.isFavorite) "Added to favorites" else "Removed from favorites successfully"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    val navController = navHostFragment.navController
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "favoriteUpdated",
                        true
                    )
                })
            }
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupNavigationMenu() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_ranking -> {
                    navController.navigate(R.id.rankingFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_history -> {
                    navController.navigate(R.id.historyFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_home -> {
                    navController.navigate(R.id.songListFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_favorite -> {
                    navController.navigate(R.id.favoriteFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_playlist -> {
                    navController.navigate(R.id.playlistListFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_signout -> {
                    showLogoutDialog(
                        context = this,
                        onConfirm = {
                            viewModel.logout()
                            val intent = Intent(this, SignInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    )
                    true
                }
                else -> false
            }
        }
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        miniPlayerBinding.imgbtnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
        startOrStopRotation(isPlaying)
    }

    private fun updateMiniPlayerUI() {
        val song = musicViewModel.currentSong.value
        if (song != null) {
            miniPlayerBinding.tvMiniTitle.text = song.title
            miniPlayerBinding.tvMiniArtist.text = song.artist.name
            processFavorite()
            Glide.with(this)
                .load(song.imageUrl)
                .placeholder(R.drawable.image_no_available)
                .error(R.drawable.image_no_available)
                .into(miniPlayerBinding.imgMiniSong)
            val isPlaying = musicViewModel.isPlaying.value ?: false
            updatePlayPauseIcon(isPlaying)
        } else {
            miniPlayerBinding.root.visibility = View.GONE
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun startOrStopRotation(isPlaying: Boolean) {
        if (rotationAnimator == null) {
            rotationAnimator = ObjectAnimator.ofFloat(
                miniPlayerBinding.imgMiniSong,
                View.ROTATION,
                currentRotation,
                currentRotation + 360f
            ).apply {
                duration = 10000
                interpolator = LinearInterpolator()
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    currentRotation = miniPlayerBinding.imgMiniSong.rotation % 360f
                }
            }
        } else {
            rotationAnimator?.setFloatValues(currentRotation, currentRotation + 360f)
        }

        if (isPlaying) {
            rotationAnimator?.start()
        } else {
            rotationAnimator?.pause()
        }
    }

    private fun isFavorite(isFav: Boolean) {
        if (isFav) {
            miniPlayerBinding.imgbtnFavorite.setImageResource(R.drawable.ic_delete_favorite)
        } else {
            miniPlayerBinding.imgbtnFavorite.setImageResource(R.drawable.ic_add_favorite)
        }
    }

    private fun processFavorite() {
        val song = musicViewModel.currentSong.value ?: return
        lifecycleScope.launch {
            val isFav = viewModel.isFavorite(song.id)
            isFavorite(isFav)
        }
    }

    private fun showLogoutDialog(
        context: Context,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        val binding = DialogLogoutBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
            onCancel?.invoke()
        }

        binding.btnLogout.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        dialog.show()
    }
    override fun onResume() {
        super.onResume()
        miniPlayerBinding.root.visibility = View.VISIBLE
        updateMiniPlayerUI()
    }
}
