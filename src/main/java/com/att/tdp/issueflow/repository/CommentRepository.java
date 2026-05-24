package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicketId(Long ticketId);

    @Query("SELECT c FROM Comment c JOIN c.mentionedUsers u WHERE u.id = :userId ORDER BY c.createdAt DESC")
    Page<Comment> findByMentionedUsersId(@Param("userId") Long userId, Pageable pageable);
}
