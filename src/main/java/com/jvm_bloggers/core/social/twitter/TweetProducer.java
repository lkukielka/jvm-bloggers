package com.jvm_bloggers.core.social.twitter;

import java.time.LocalDateTime;
import java.util.Random;
import com.jvm_bloggers.core.newsletter_issues.NewIssuePublished;
import com.jvm_bloggers.entities.blog.Blog;
import com.jvm_bloggers.entities.newsletter_issue.NewsletterIssue;
import com.jvm_bloggers.entities.twitter.Tweet;
import com.jvm_bloggers.entities.twitter.TweetRepository;

import com.jvm_bloggers.utils.NowProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PACKAGE;

@Component
@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
class TweetProducer {

    private final TweetContentGenerator contentGenerator;
    private final TweetRepository tweetRepository;
    private final NowProvider nowProvider;

    @EventListener
    public void handleNewIssueEvent(NewIssuePublished newIssuePublished) {
        final NewsletterIssue issue = newIssuePublished.getNewsletterIssue();
        final String content = contentGenerator.generateTweetContent(issue);
        tweetRepository.save(new Tweet(content, nowProvider.now()));
    }

    @EventListener
    public void handleNewBloggerEvent(Blog blog) {
        final String content = contentGenerator.generateNewBloggerTweetContent(blog);
        final LocalDateTime postingDate = generatePostingDate(nowProvider.now());
        tweetRepository.save(new Tweet(content, postingDate));
    }

    private LocalDateTime generatePostingDate(LocalDateTime now) {
        final int minPostingMinute = 481;
        final int maxPostingMinute = 1439;
        final int postingMinute =
                now.getMinute() < minPostingMinute ? generateRandomMinute(minPostingMinute, maxPostingMinute) : generateRandomMinute(now.getMinute(), maxPostingMinute);
        return now.withMinute(postingMinute);
    }

    private int generateRandomMinute(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }
}
