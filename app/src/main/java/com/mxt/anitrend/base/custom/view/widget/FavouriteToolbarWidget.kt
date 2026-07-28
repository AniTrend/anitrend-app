package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetToolbarFavouriteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaUtil
import timber.log.Timber

/**
 * Created by max on 2018/01/31.
 * Widget for handling favourite toggles
 */
class FavouriteToolbarWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener {

    interface Listener {
        fun onToggleFavourite(
            animeId: Int?,
            mangaId: Int?,
            characterId: Int?,
            staffId: Int?,
            studioId: Int?,
            onResult: (Result<Unit>) -> Unit,
        )
    }

    private lateinit var binding: WidgetToolbarFavouriteBinding

    private var staffBase: StaffBase? = null
    private var mediaBase: MediaBase? = null
    private var studioBase: StudioBase? = null
    private var characterBase: CharacterBase? = null

    private var animeId: Int? = null
    private var mangaId: Int? = null
    private var staffId: Int? = null
    private var studioId: Int? = null
    private var characterId: Int? = null

    private val tagName = FavouriteToolbarWidget::class.java.simpleName
    private var listener: Listener? = null
    private var recycled = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    init {
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : this(context, attrs, defStyleAttr)

    override fun onInit() {
        binding = WidgetToolbarFavouriteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        recycled = true
        listener = null
        resetFlipperState()
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    fun setModel(staffBase: StaffBase) {
        recycled = false
        this.staffBase = staffBase
        setIconType()
        clearIds()
        staffId = staffBase.id.toInt()
        binding.widgetFlipper.visibility = VISIBLE
    }

    fun setModel(characterBase: CharacterBase) {
        recycled = false
        this.characterBase = characterBase
        setIconType()
        clearIds()
        characterId = characterBase.id.toInt()
        binding.widgetFlipper.visibility = VISIBLE
    }

    fun setModel(studioBase: StudioBase) {
        recycled = false
        this.studioBase = studioBase
        setIconType()
        clearIds()
        studioId = studioBase.id.toInt()
        binding.widgetFlipper.visibility = VISIBLE
    }

    fun setModel(mediaBase: MediaBase) {
        recycled = false
        this.mediaBase = mediaBase
        setIconType()
        clearIds()
        if (MediaUtil.isAnimeType(mediaBase)) {
            animeId = mediaBase.id.toInt()
        } else {
            mangaId = mediaBase.id.toInt()
        }
        binding.widgetFlipper.visibility = VISIBLE
    }

    private fun clearIds() {
        animeId = null
        mangaId = null
        characterId = null
        staffId = null
        studioId = null
    }

    private fun isModelSet(): Boolean = staffBase != null || characterBase != null || studioBase != null || mediaBase != null

    override fun onClick(view: View) {
        if (view.id == R.id.widget_flipper) {
            if (isModelSet()) {
                if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                    listener?.onToggleFavourite(animeId, mangaId, characterId, staffId, studioId) { result ->
                        if (recycled || !isAttachedToWindow) return@onToggleFavourite
                        result.onSuccess {
                            mediaBase?.toggleFavourite()
                            studioBase?.toggleFavourite()
                            staffBase?.toggleFavourite()
                            characterBase?.toggleFavourite()
                            setIconType()
                        }.onFailure { throwable ->
                            Timber.e(throwable)
                            resetFlipperState()
                        }
                    }
                } else {
                    NotifyUtil
                        .makeText(
                            context,
                            R.string.busy_please_wait,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            } else {
                NotifyUtil
                    .makeText(
                        context,
                        R.string.text_activity_loading,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    private fun setIconType() {
        var isFavourite = false
        var requiresTint = true

        val localMedia = mediaBase
        val localStudio = studioBase
        val localStaff = staffBase
        val localCharacter = characterBase

        if (localMedia != null) {
            isFavourite = localMedia.isFavourite
            requiresTint = false
        } else if (localStudio != null) {
            isFavourite = localStudio.isFavourite
        } else if (localStaff != null) {
            isFavourite = localStaff.isFavourite
        } else if (localCharacter != null) {
            isFavourite = localCharacter.isFavourite
        }

        val drawable =
            when {
                isFavourite && requiresTint ->
                    context.getCompatTintedDrawable(R.drawable.ic_favorite_white_24dp)
                isFavourite ->
                    context.getCompatDrawable(R.drawable.ic_favorite_white_24dp)
                requiresTint ->
                    context.getCompatTintedDrawable(R.drawable.ic_favorite_border_white_24dp)
                else ->
                    context.getCompatDrawable(R.drawable.ic_favorite_border_white_24dp)
            }

        binding.widgetLike.setImageDrawable(drawable)
        resetFlipperState()
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
