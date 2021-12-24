package club.electro.adapter

import ImageGetter
import QuoteSpanClass
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.QuoteSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import club.electro.R
import club.electro.databinding.PostItemBinding
import club.electro.dto.Post
import com.bumptech.glide.Glide
import club.electro.utils.trimWhiteSpaces
import android.text.SpannableStringBuilder
import android.widget.TextView
import android.text.style.ClickableSpan



interface PostInteractionListener {
    fun onAnswerClicked(post: Post) {}
    fun onEditClicked(post: Post) {}
    fun onRemoveClicked(post: Post) {}
    fun onAvatarClicked(post: Post) {}
    fun onUrlClicked(url: String?) {}
}

class PostAdapter(
    private val onInteractionListener: PostInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }
}

class PostViewHolder(
    private val binding: PostItemBinding,
    private val onInteractionListener: PostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
            val date = java.util.Date(post.published * 1000)

            authorName.text = post.authorName


            authorAvatar.setOnClickListener {
                onInteractionListener.onAvatarClicked(post)
            }

            menu.isVisible = false
            content.setTextColor(getColor(this.root.context, R.color.postInactiveTextColor))

            when(post.status) {
                Post.STATUS_CREATED_LOCAL -> {
                    published.setText(R.string.post_status_publishing)
                }
                Post.STATUS_SAVING_LOCAL -> {
                    published.setText(R.string.post_status_saving)
                }
                Post.STATUS_REMOVING_LOCAL -> {
                    published.setText(R.string.post_status_removing)
                }
                Post.STATUS_PUBLISHED -> {
                    published.setText(sdf.format(date).toString() + " (id: " + post.id + ")")
                    //published.setText(sdf.format(date).toString())
                    content.setTextColor(getColor(this.root.context, R.color.postTextColor))
                    menu.isVisible = true
                }
            }

            post.authorAvatar?.let {
                Glide.with(authorAvatar.context)
                    .load(post.authorAvatar)
                    .circleCrop()
                    .timeout(5_000)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .into(authorAvatar)
            }

            menu.setOnClickListener { it ->
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.post_options)
                    this.getMenu().findItem(R.id.menu_edit).setVisible(post.canEdit)
                    this.getMenu().findItem(R.id.menu_remove).setVisible(post.canRemove)

                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_answer -> {
                                onInteractionListener.onAnswerClicked(post)
                                true
                            }
                            R.id.menu_edit -> {
                                onInteractionListener.onEditClicked(post)
                                true
                            }
                            R.id.menu_remove -> {
                                onInteractionListener.onRemoveClicked(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            val resources: Resources = this.root.resources


            val preparedContent = post.preparedContent ?: post.content

            val imageGetter = ImageGetter(resources, content)

            //Initial span from HtmlCompat will link anchor tags
            val htmlSpan = HtmlCompat.fromHtml(
                preparedContent,
                HtmlCompat.FROM_HTML_MODE_LEGACY,
                imageGetter,
                null
            ) as Spannable

            val trimmedPostText: CharSequence = trimWhiteSpaces(htmlSpan)
            val result = trimmedPostText
            replaceQuoteSpans(result as Spannable)


            //save anchor links for later
            val anchorTagSpans = result.getSpans(0, result.length, URLSpan::class.java)

            //add first span to TextView
            content.text = result

            //Linkify will now make urls clickable but overwrite our anchor links
            Linkify.addLinks(content, Linkify.ALL)
            //content.movementMethod = LinkMovementMethod.getInstance()
            content.linksClickable = true

            //we will add back the anchor links here
            val restoreAnchorsSpan = SpannableString(content.text)
            for (span in anchorTagSpans) {
                restoreAnchorsSpan.setSpan(
                    span,
                    result.getSpanStart(span),
                    result.getSpanEnd(span),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            }

            setTextViewHTML(content, restoreAnchorsSpan)

            //content.setText(result)
        }
    }


    protected fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan?) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Do something with span.getURL() to handle the link click...
                onInteractionListener.onUrlClicked(span?.getURL())
                //UrlHandler(span?.getURL(), binding.root.context).run()
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    protected fun setTextViewHTML(text: TextView, html: CharSequence) {
        val sequence = html //: CharSequence = Html.fromHtml(html)
        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder, span)
        }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun replaceQuoteSpans(spannable: Spannable) {
        val quoteSpans: Array<QuoteSpan> =
            spannable.getSpans(0, spannable.length - 1, QuoteSpan::class.java)

        for (quoteSpan in quoteSpans)
        {
            val start: Int = spannable.getSpanStart(quoteSpan)
            val end: Int = spannable.getSpanEnd(quoteSpan)
            val flags: Int = spannable.getSpanFlags(quoteSpan)
            spannable.removeSpan(quoteSpan)
            spannable.setSpan(
                QuoteSpanClass(
                    // background color
                    getColor(binding.root.context, R.color.postQuoteColor),
                    // strip color
                    getColor(binding.root.context, R.color.postQuoteStripColor),
                    // strip width
                    10F, 30F
                ),
                start, end, flags
            )
        }
    }

}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Post, newItem: Post): Any? = Unit
}