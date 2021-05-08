package com.hitsa.bloggingsite.service;

import com.hitsa.bloggingsite.dto.CommentsDto;
import com.hitsa.bloggingsite.exceptions.PostNotFoundException;
import com.hitsa.bloggingsite.mapper.CommentMapper;
import com.hitsa.bloggingsite.model.Comment;
import com.hitsa.bloggingsite.model.NotificationEmail;
import com.hitsa.bloggingsite.model.Post;
import com.hitsa.bloggingsite.model.User;
import com.hitsa.bloggingsite.repository.CommentRepository;
import com.hitsa.bloggingsite.repository.PostRepository;
import com.hitsa.bloggingsite.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class CommentService {
    private static final String POST_URL = "";
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final MailContentBuilder mailContentBuilder;
    private final MailService mailService;

    public void save(CommentsDto commentsDto) {
        Post post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));
        Comment comment = commentMapper.map(commentsDto, post, authService.getCurrentUser());
        commentRepository.save(comment);

        String message = mailContentBuilder.build(authService.getCurrentUser() + " posted a comment on your post." + POST_URL);
        sendCommentNotification(message, comment.getUser(), post.getUser());
    }

    private void sendCommentNotification(String message, User Senderuser, User recipientUser) {
        mailService.sendMail(new NotificationEmail(Senderuser.getUsername() + " Commented on your post", recipientUser.getEmail(), message));;
        // Senderuser is a user who is commenting and recipientUser is a user on whose post is commented.
    }

    public List<CommentsDto> getAllCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId.toString()));
        return commentRepository.findByPost(post)
                .stream()
                .map(commentMapper::mapToDto).collect(toList());
    }

    public List<CommentsDto> getAllCommentsForUser(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName));
        return commentRepository.findAllByUser(user)
                .stream()
                .map(commentMapper::mapToDto)
                .collect(toList());
    }
}
