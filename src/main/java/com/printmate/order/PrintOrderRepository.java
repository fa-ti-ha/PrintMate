package com.printmate.order;

import com.printmate.payment.PaymentStatus;
import com.printmate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrintOrderRepository extends JpaRepository<PrintOrder, Long>, JpaSpecificationExecutor<PrintOrder> {

    @Override
    @EntityGraph(attributePaths = {"customer", "document"})
    Optional<PrintOrder> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PrintOrder o where o.id = :id")
    Optional<PrintOrder> findByIdForUpdate(@Param("id") Long id);

    boolean existsByDocumentId(Long documentId);

    @EntityGraph(attributePaths = {"customer", "document"})
    Optional<PrintOrder> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"customer", "document"})
    Optional<PrintOrder> findByIdAndCustomer(Long id, User customer);

    @EntityGraph(attributePaths = {"customer", "document"})
    Optional<PrintOrder> findByOrderNumberAndCustomer(String orderNumber, User customer);

    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findByCustomerOrderByCreatedAtDesc(User customer);

    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findTop5ByCustomerOrderByCreatedAtDesc(User customer);

    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findByCustomerAndOrderStatusInOrderByCreatedAtDesc(User customer, List<OrderStatus> statuses);

    @Query("SELECT o FROM PrintOrder o WHERE o.orderStatus IN :statuses "
            + "AND (:search = '' OR LOWER(o.orderNumber) = LOWER(:search)) ORDER BY o.createdAt ASC, o.id ASC")
    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findQueue(@Param("statuses") List<OrderStatus> statuses, @Param("search") String search);

    long countByOrderStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime before);

    @Query("SELECT COUNT(o) FROM PrintOrder o WHERE o.orderStatus = 'READY_FOR_PICKUP' "
            + "AND (o.pickupAttempts >= :maxAttempts OR o.pickupCodeExpiresAt <= :now)")
    long countPickupIssues(@Param("maxAttempts") int maxAttempts, @Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(o.selectedPageCount * o.copies), 0) FROM PrintOrder o WHERE o.orderStatus = 'COMPLETED'")
    long calculateTotalPagesPrinted();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM PrintOrder o WHERE o.orderStatus IN :statuses AND o.paymentStatus = 'UNPAID'")
    BigDecimal calculateOutstandingValue(@Param("statuses") List<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findByOrderStatusOrderByCreatedAtAsc(OrderStatus status);

    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findByOrderStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findTop10ByOrderByCreatedAtDesc();

    long countByCustomer(User customer);

    long countByCustomerAndOrderStatus(User customer, OrderStatus status);

    long countByOrderStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM PrintOrder o WHERE o.orderStatus = :status AND o.paymentStatus = :paymentStatus")
    long countByOrderStatusAndPaymentStatus(@Param("status") OrderStatus status,
                                           @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM PrintOrder o WHERE o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM PrintOrder o WHERE o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID' AND o.completedAt >= :startDate AND o.completedAt <= :endDate")
    BigDecimal calculateRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM PrintOrder o WHERE o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID' AND o.completedAt >= :startDate AND o.completedAt <= :endDate ORDER BY o.completedAt DESC")
    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findCompletedPaidOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM PrintOrder o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    @EntityGraph(attributePaths = {"customer", "document"})
    List<PrintOrder> findOrdersCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
