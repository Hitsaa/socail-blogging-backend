package com.hitsa.bloggingsite.repository;

import com.hitsa.bloggingsite.model.Post;
import com.hitsa.bloggingsite.model.User;
import com.hitsa.bloggingsite.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findTopByPostAndUserOrderByVoteIdDesc(Post post, User currentUser);
}
