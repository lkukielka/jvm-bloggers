package com.jvm_bloggers.core.data_fetching.blogs;

import com.jvm_bloggers.core.utils.JvmBloggersEvent;
import com.jvm_bloggers.entities.blog.Blog;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JvmBloggersEvent
@RequiredArgsConstructor
@Data
public class NewBlogAdded {
	private final Blog blog;
}
