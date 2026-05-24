package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.request.CreateCommentRequest;
import com.att.tdp.issueflow.dto.request.UpdateCommentRequest;
import com.att.tdp.issueflow.dto.response.CommentResponse;
import com.att.tdp.issueflow.dto.response.MentionedUserDto;
import com.att.tdp.issueflow.dto.response.MentionsPageResponse;
import com.att.tdp.issueflow.entity.Comment;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.enums.Actor;
import com.att.tdp.issueflow.enums.AuditAction;
import com.att.tdp.issueflow.enums.EntityType;
import com.att.tdp.issueflow.exception.EntityNotFoundException;
import com.att.tdp.issueflow.exception.ValidationException;
import com.att.tdp.issueflow.repository.CommentRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private final CommentRepository commentRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<CommentResponse> getByTicket(Long ticketId) {
        ticketService.findOrThrow(ticketId);
        return commentRepository.findByTicketId(ticketId).stream().map(this::toResponse).toList();
    }

    public CommentResponse create(Long ticketId, CreateCommentRequest req) {
        Ticket ticket = ticketService.findOrThrow(ticketId);
        User author = userService.findOrThrow(req.getAuthorId());
        Set<User> mentions = parseMentions(req.getContent());

        Comment comment = Comment.builder()
                .ticket(ticket)
                .author(author)
                .content(req.getContent())
                .createdAt(LocalDateTime.now())
                .mentionedUsers(mentions)
                .build();
        Comment saved = commentRepository.save(comment);
        auditLogService.record(AuditAction.CREATE, EntityType.COMMENT, saved.getId(), req.getAuthorId(), Actor.USER);
        return toResponse(saved);
    }

    public void update(Long ticketId, Long commentId, UpdateCommentRequest req) {
        ticketService.findOrThrow(ticketId);
        Comment comment = findOrThrow(commentId);
        comment.setContent(req.getContent());
        comment.setMentionedUsers(parseMentions(req.getContent()));
        commentRepository.save(comment);
        auditLogService.record(AuditAction.UPDATE, EntityType.COMMENT, commentId, null, Actor.USER);
    }

    public void delete(Long ticketId, Long commentId) {
        ticketService.findOrThrow(ticketId);
        findOrThrow(commentId);
        commentRepository.deleteById(commentId);
        auditLogService.record(AuditAction.DELETE, EntityType.COMMENT, commentId, null, Actor.USER);
    }

    public MentionsPageResponse getMentions(Long userId, int page, int pageSize) {
        userService.findOrThrow(userId);
        Page<Comment> commentsPage = commentRepository.findByMentionedUsersId(
                userId, PageRequest.of(page - 1, pageSize));
        long total = commentsPage.getTotalElements();
        List<CommentResponse> data = commentsPage.getContent().stream().map(this::toResponse).toList();
        return MentionsPageResponse.builder()
                .data(data)
                .total(total)
                .page(page)
                .build();
    }

    private Set<User> parseMentions(String content) {
        Set<User> mentions = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String username = matcher.group(1);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ValidationException("Mentioned user not found: @" + username));
            mentions.add(user);
        }
        return mentions;
    }

    private Comment findOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
    }

    public CommentResponse toResponse(Comment comment) {
        List<MentionedUserDto> mentioned = comment.getMentionedUsers().stream()
                .map(u -> MentionedUserDto.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .build())
                .toList();
        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .authorId(comment.getAuthor().getId())
                .content(comment.getContent())
                .mentionedUsers(mentioned)
                .build();
    }
}
