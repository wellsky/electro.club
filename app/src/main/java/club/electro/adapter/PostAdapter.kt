package club.electro.adapter

import ImageGetter
import QuoteSpanClass
import android.content.res.Resources
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
import club.electro.databinding.ItemPostBinding
import club.electro.dto.Post
import com.bumptech.glide.Glide
import android.text.SpannableStringBuilder
import android.widget.TextView
import android.text.style.ClickableSpan
import androidx.lifecycle.LifecycleCoroutineScope
import android.graphics.Paint
import club.electro.databinding.PartPostStatsBinding


interface PostInteractionListener {
    fun onAnswerClicked(post: Post) {}
    fun onEditClicked(post: Post) {}
    fun onRemoveClicked(post: Post) {}
    fun onAvatarClicked(post: Post) {}
    fun onUrlClicked(url: String?) {}
    fun onAttachmentsClicked(post: Post) {}
    fun onOpenClicked(post: Post) {}
}

class PostAdapter(
    private val onInteractionListener: PostInteractionListener,
    private val lifecycleScope: LifecycleCoroutineScope
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, lifecycleScope)
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
    private val binding: ItemPostBinding,
    private val onInteractionListener: PostInteractionListener,
    private val lifecycleScope: LifecycleCoroutineScope
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
    }

    fun bind(post: Post) {
        binding.apply {
            val date = java.util.Date(post.published * 1000)

            authorName.text = post.authorName


            authorAvatar.setOnClickListener {
                onInteractionListener.onAvatarClicked(post)
            }

            attachments.setOnClickListener {
                onInteractionListener.onAttachmentsClicked(post)
            }

            header.isVisible = (post.authorId != null)

            menu.isVisible = false
            attachments.isVisible = false
            content.setTextColor(getColor(this.root.context, R.color.postInactiveTextColor))

            when (post.status) {
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
                    //published.text = sdf.format(date).toString() + " (id: " + post.id + ")"
                    published.text = sdf.format(date).toString()
                    content.setTextColor(getColor(this.root.context, R.color.postTextColor))
                    menu.isVisible = header.isVisible
                }
            }

            post.attachmentLinks?.let {
                attachments.isVisible = true
                attachments.text = attachments.context.getString(
                    R.string.attachments,
                    post.attachmentLinks.size.toString()
                )
                attachments.setPaintFlags(attachments.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
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

            if ((post.comments != null) || (post.views != null)) {
                partPostStats.apply {
                    root.isVisible = true

                    post.comments?.let {
                        comments.text = it.toString()

                        comments.setOnClickListener {
                            onInteractionListener.onOpenClicked(post)
                        }
                    }

                    post.views?.let {
                        views.text = it.toString()
                    }
                }
            } else {
                partPostStats.root.isVisible = false
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

            val imageGetter = ImageGetter(resources, content, lifecycleScope)

            //Initial span from HtmlCompat will link anchor tags
            val htmlSpan = HtmlCompat.fromHtml(
                preparedContent,
                HtmlCompat.FROM_HTML_MODE_COMPACT + HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE,
                imageGetter,
                null
            ) as Spannable

            val trimmedPostText: CharSequence = htmlSpan.trim()
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
        }
    }


    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan?) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Do something with span.getURL() to handle the link click...
                onInteractionListener.onUrlClicked(span?.getURL())
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun setTextViewHTML(text: TextView, html: CharSequence) {
        val sequence = html
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