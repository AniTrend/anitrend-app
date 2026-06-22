package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetToolbarFavouriteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.graphql.apiError
import com.mxt.anitrend.util.media.MediaUtil
import co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2018/01/31.
 * Widget for handling favourite toggles
 */
class FavouriteToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    RetroCallback<ResponseBody>,
    View.OnClickListener {

    private lateinit var presenter: WidgetPresenter<ResponseBody>
    private lateinit var binding: WidgetToolbarFavouriteBinding

    private var staffBase: StaffBase? = null
    private var mediaBase: MediaBase? = null
    private var studioBase: StudioBase? = null
    private var characterBase: CharacterBase? = null

    private lateinit var queryContainer: QueryContainerBuilder

    private val tagName = FavouriteToolbarWidget::class.java.simpleName

    init {
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : this(context, attrs, defStyleAttr)

    override fun onInit() {
        presenter = WidgetPresenter(context)
        binding = WidgetToolbarFavouriteBinding.inflate(context.getLayoutInflater(), this, true)
        queryContainer = GraphUtil.getDefaultQuery(false)
            .putVariable(KeyUtil.arg_page_limit, KeyUtil.SINGLE_ITEM_LIMIT)
        binding.widgetFlipper.setOnClickListener(this)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        resetFlipperState()
        presenter.onDestroy()
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
    }

    fun setModel(staffBase: StaffBase) {
        this.staffBase = staffBase
        setIconType()
        queryContainer.putVariable(KeyUtil.arg_staffId, staffBase.id)
        presenter.params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        binding.widgetFlipper.visibility = VISIBLE
    }

    fun setModel(characterBase: CharacterBase) {
        this.characterBase = characterBase
        setIconType()
        queryContainer.putVariable(KeyUtil.arg_characterId, characterBase.id)
        presenter.params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        binding.widgetFlipper.visibility = VISIBLE
    }

    fun setModel(studioBase: StudioBase) {
        this.studioBase = studioBase
        setIconType()
        queryContainer.putVariable(KeyUtil.arg_studioId, studioBase.id)
        presenter.params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        binding.widgetFlipper.visibility = VISIBLE
    }

    fun setModel(mediaBase: MediaBase) {
        this.mediaBase = mediaBase
        setIconType()
        val argId = if (MediaUtil.isAnimeType(mediaBase)) KeyUtil.arg_animeId else KeyUtil.arg_mangaId
        queryContainer.putVariable(argId, mediaBase.id)
        presenter.params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        binding.widgetFlipper.visibility = VISIBLE
    }

    private fun isModelSet(): Boolean {
        return staffBase != null || characterBase != null || studioBase != null || mediaBase != null
    }

    override fun onClick(view: View) {
        if (presenter.settings.isAuthenticated) {
            if (view.id == R.id.widget_flipper) {
                if (isModelSet()) {
                    if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetFlipper.showNext()
                        presenter.requestData(KeyUtil.MUT_TOGGLE_FAVOURITE, context, this)
                    } else {
                        NotifyUtil.makeText(
                            context,
                            R.string.busy_please_wait,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    NotifyUtil.makeText(
                        context,
                        R.string.text_activity_loading,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            NotifyUtil.makeText(
                context,
                R.string.info_login_req,
                R.drawable.ic_group_add_grey_600_18dp,
                Toast.LENGTH_SHORT
            ).show()
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

        val drawable = when {
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

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        try {
            if (response.isSuccessful) {
                mediaBase?.toggleFavourite()
                studioBase?.toggleFavourite()
                staffBase?.toggleFavourite()
                characterBase?.toggleFavourite()
                setIconType()
            } else {
                Timber.tag(tagName).w(response.apiError())
                NotifyUtil.makeText(
                    context,
                    R.string.text_error_request,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
        try {
            Timber.w(throwable)
            resetFlipperState()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
