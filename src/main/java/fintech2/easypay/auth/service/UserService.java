package fintech2.easypay.auth.service;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 서비스 - 캐싱 최적화 포함
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
    @Cacheable(value = "userCache", key = "#phoneNumber")
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        log.debug("사용자 조회 (캐시 미스): {}", phoneNumber);
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    
    @Cacheable(value = "userCache", key = "#id")
    public Optional<User> findById(Long id) {
        log.debug("사용자 조회 (캐시 미스): {}", id);
        return userRepository.findById(id);
    }
}