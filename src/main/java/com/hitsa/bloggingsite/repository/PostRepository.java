package com.hitsa.bloggingsite.repository;

import com.hitsa.bloggingsite.model.Post;
import com.hitsa.bloggingsite.model.Subreddit;
import com.hitsa.bloggingsite.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllBySubreddit(Subreddit subreddit);

    List<Post> findByUser(User user);
}
