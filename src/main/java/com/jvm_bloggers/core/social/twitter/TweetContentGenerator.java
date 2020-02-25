package com.jvm_bloggers.core.social.twitter;

import com.google.common.collect.ImmutableList;
import com.jvm_bloggers.core.blogpost_redirect.LinkGenerator;
import com.jvm_bloggers.entities.blog.Blog;
import com.jvm_bloggers.entities.blog_post.BlogPost;
import com.jvm_bloggers.entities.newsletter_issue.NewsletterIssue;

import io.micrometer.core.instrument.util.StringUtils;
import io.vavr.collection.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jsoup.internal.StringUtil;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.Objects;
import java.util.Random;

import static lombok.AccessLevel.PACKAGE;

@Component
@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
class TweetContentGenerator {

    private static final int TWEET_MAX_LENGTH = 250;
    private static final String MESSAGE_TEMPLATE =
        "Nowy numer #<number> już online - <link> z postami między innymi <personal1>"
            + "<if(company && personal2)>, <company> i <personal2>"
            + "<elseif(company)> i <company>"
            + "<elseif(personal2)> i <personal2><endif> #java #jvm";
    private static final String SHORT_MESSAGE_TEMPLATE =
        "Nowy numer #<number> już online - <link> z postami między innymi <personal>"
            + "<if(company)> i <company><endif> #java #jvm";

    private static final ImmutableList<String> NEW_AUTHOR_MESSAGE_TEMPLATES = ImmutableList.of(
            "<name> dodał bloga do listy, witamy na pokładzie #java #jvm",
            "Witamy nowego autora, <name>! #java #jvm",
            "Miło nam ogłosić, że <name> dołączył do listy autorów #java #jvm");


    private final LinkGenerator linkGenerator;

    public String generateTweetContent(NewsletterIssue issue) {
        final List<String> personals =
            List.ofAll(issue.getBlogPosts())
                .map(BlogPost::getBlog)
                .filter(Blog::isPersonal)
                .map(Blog::getTwitter)
                .filter(Objects::nonNull)
                .distinct()
                .shuffle()
                .take(2)
                .padTo(2, null);

        final String company =
            List.ofAll(issue.getBlogPosts())
                .map(BlogPost::getBlog)
                .filter(Blog::isCompany)
                .map(Blog::getTwitter)
                .filter(Objects::nonNull)
                .shuffle()
                .getOrElse((String) null);

        final String issueLink = linkGenerator.generateIssueLink(issue.getIssueNumber());

        final ST template = new ST(MESSAGE_TEMPLATE);
        template.add("number", issue.getIssueNumber());
        template.add("link", issueLink);
        template.add("personal1", personals.head());
        template.add("personal2", personals.last());
        template.add("company", company);
        final String tweetContent = template.render();

        if (tweetIsTooLong(tweetContent, issueLink.length())) {
            final ST shortTemplate = new ST(SHORT_MESSAGE_TEMPLATE);
            shortTemplate.add("number", issue.getIssueNumber());
            shortTemplate.add("link", issueLink);
            shortTemplate.add("personal", personals.head());
            shortTemplate.add("company", company);
            return shortTemplate.render();
        } else {
            return tweetContent;
        }
    }

    public String generateNewBloggerTweetContent(Blog blog) {
        final String name = StringUtils.isNotBlank(blog.getTwitter()) ? blog.getTwitter() : blog.getAuthor();
        final ST template = new ST(NEW_AUTHOR_MESSAGE_TEMPLATES.get(new Random().nextInt(2)));
        template.add("name", name);
        return template.render();
    }

    private boolean tweetIsTooLong(String tweetContent, int originalIssuesLinkLength) {
        return (tweetContent.length() - originalIssuesLinkLength + 23) > TWEET_MAX_LENGTH;
    }

}
