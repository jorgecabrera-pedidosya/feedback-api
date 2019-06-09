package com.feedback.api.service;

import com.feedback.api.builder.FeedbackBuilder;
import com.feedback.api.dto.FeedbackDTO;
import com.feedback.api.enums.FeedbackStatus;
import com.feedback.api.exception.BadRequestException;
import com.feedback.api.exception.EntityNotFoundException;
import com.feedback.api.model.Feedback;
import com.feedback.api.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

  private static String USER = "User";
  private static String STORE = "Store";
  @Autowired FeedbackRepository feedbackRepository;

  public Feedback create(Feedback body) {
    Optional<Feedback> opFeedback = feedbackRepository.findById(body.getOrderId());
    if (opFeedback.isPresent())
      throw new BadRequestException(
          String.format("Feedback for order %s is already registered.", body.getOrderId()));

    Feedback newFeedback =
        new FeedbackBuilder()
            .withItemId(body.getItemId())
            .withOrderId(body.getOrderId())
            .withBuyerId(body.getBuyerId())
            .withSellerId(body.getSellerId())
            .withStoreId(body.getStoreId())
            .withScore(body.getScore())
            .withComment(body.getComment())
            .build();
    return feedbackRepository.save(newFeedback);
  }

  public Feedback retrieve(Long id) {
    return feedbackRepository
        .findById(id)
        .orElseThrow(
            () -> new EntityNotFoundException(Feedback.class.getSimpleName(), id.toString()));
  }

  public Feedback update(Long id, FeedbackDTO body) {
    Feedback feedback =
        feedbackRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException(Feedback.class.getSimpleName(), id.toString()));
    String comment = body.getComment() != null ? body.getComment().trim() : null;
    feedback.setComment(comment);
    return feedback;
  }

  public void delete(Long id) {
    Feedback feedback = retrieve(id);
    feedback.setStatus(FeedbackStatus.DELETE);
  }

  public List<Feedback> getAllFeedbacksByBuyerIdBetween(
      Long userId, Date from, Date to, Pageable pageable) {
    List<Feedback> feedbacks =
        feedbackRepository.findByBuyerIdAndCreatedDateBetween(userId, from, to, pageable).stream()
            .filter(feedback -> feedback.getStatus() != FeedbackStatus.DELETE)
            .collect(Collectors.toList());
    if (feedbacks.isEmpty()) throw new EntityNotFoundException(USER, userId.toString());
    return feedbacks;
  }

  public List<Feedback> getAllFeedbacksByStoreIdBetween(
      String storeId, Date from, Date to, Pageable pageable) {
    List<Feedback> feedbacks =
        feedbackRepository.findByStoreIdAndCreatedDateBetween(storeId, from, to, pageable).stream()
            .filter(feedback -> feedback.getStatus() != FeedbackStatus.DELETE)
            .collect(Collectors.toList());
    if (feedbacks.isEmpty()) throw new EntityNotFoundException(STORE, storeId);
    return feedbacks;
  }
}