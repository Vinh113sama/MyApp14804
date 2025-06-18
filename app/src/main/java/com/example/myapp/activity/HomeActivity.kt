package com.example.myapp.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.annotation.OptIn
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
import com.example.myapp.databinding.LayoutTopInterfaceBinding
import com.example.myapp.databinding.MiniPlayerBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import com.example.myapp.util.AppViewModelProvider
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
            val name = viewModel.getuserInformation().name
            headerBinding.tvUserName.text = name
        }

        miniPlayerBinding = MiniPlayerBinding.bind(binding.miniPlayerContainer)

        setupNavigationMenu()
        setupMiniPlayer()
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
            if (song != null) {
                miniPlayerBinding.tvMiniTitle.text = song.title
                miniPlayerBinding.tvMiniArtist.text = song.artist.name
                Glide.with(this)
                    .load(song.imageUrl)
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
            startActivity(intent)
        }

        miniPlayerBinding.imgbtnNext.setOnClickListener {
            musicViewModel.playNext()
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
                    val navController = navHostFragment.navController
                    navController.currentBackStackEntry?.savedStateHandle?.set("favoriteUpdated", true)
                })
            }
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupNavigationMenu() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
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
            rotationAnimator = ObjectAnimator.ofFloat(miniPlayerBinding.imgMiniSong, View.ROTATION, currentRotation, currentRotation + 360f).apply {
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

    override fun onResume() {
        super.onResume()
        miniPlayerBinding.root.visibility = View.VISIBLE
        updateMiniPlayerUI()
    }
}
