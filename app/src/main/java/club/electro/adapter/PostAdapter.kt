package club.electro.adapter

import ImageGetter
import QuoteSpanClass
import android.content.res.Resources
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.QuoteSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.R
import club.electro.databinding.PostItemBinding
import club.electro.dto.Post
import com.bumptech.glide.Glide
import club.electro.utils.trimWhiteSpaces


interface PostInteractionListener {
    fun onClick(post: Post) {}
}

class PostAdapter(
    private val onInteractionListener: PostInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: PostItemBinding,
    private val onInteractionListener: PostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            //val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
            val date = java.util.Date(post.published * 1000)

            authorName.text = post.authorName
            published.text = sdf.format(date).toString()

            // TODO Вставить изображения в текст поста
            //val imageGetter = HtmlImageGetter(scope, resources, glide, content)
            //val imageGetter = ImageGetter(, content)
            val resources: Resources = this.root.resources
            val imageGetter = ImageGetter(resources, content)

            val source = post.content

            //val quotes = source.replace(""""#\[quote#""".toRegex(), "QUOTE")

            //val regex = """(red|green|blue)""".toRegex()
            //val regex = """"quote""".toRegex()


            val pattern = """\[quote message=(\d+?)\](.*?)\[\/quote\]"""
            val quotes = Regex(pattern).replace(source) {
                val (quoyteMessageId, quoteText) = it.destructured
                "<blockquote>" + quoteText + "</blockquote>"
            }

            val pattern1 = """<img class="emojione".*?alt="(.*?)"[^\>]+>"""
            val emojies = Regex(pattern1).replace(quotes) {
                val (emojieChar) = it.destructured
                emojieChar
            }

            val fullUrlToSrcAdded = emojies.replace("src=\"/data/", "src=\"https://electro.club/data/")

            val brStripped = fullUrlToSrcAdded.replace("<br /></p>", "</p>")
            val pStartStripped = brStripped.replace("<p>", "")
            val pEndStripped = pStartStripped.replace("</p>", "<br>")

            val htmlPostText = HtmlCompat.fromHtml(pEndStripped, HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter, null)
            val trimmedPostText: CharSequence = trimWhiteSpaces(htmlPostText)

            val result = trimmedPostText

            replaceQuoteSpans(result as Spannable)

            content.setText(result);
            content.setClickable(true);
            content.setMovementMethod(LinkMovementMethod.getInstance());


            if (!post.authorAvatar.isEmpty()) {
                Glide.with(authorAvatar.context)
                    .load(post.authorAvatar)
                    .circleCrop()
                    .timeout(5_000)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .into(authorAvatar)
            }
        }
    }

    private fun replaceQuoteSpans(spannable: Spannable)
    {
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
}