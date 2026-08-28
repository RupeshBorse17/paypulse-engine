package com.bank.settlementengine.repository;
import com.bank.settlementengine.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // JpaRepository मुळे findById, save, delete यांसारख्या बेसिक SQL क्रड ऑपरेशन्स आपोआप मिळतात.
}