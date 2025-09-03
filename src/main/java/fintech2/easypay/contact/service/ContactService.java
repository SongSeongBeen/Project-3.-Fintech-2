package fintech2.easypay.contact.service;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.auth.repository.UserRepository;
import fintech2.easypay.common.BusinessException;
import fintech2.easypay.common.ErrorCode;
import fintech2.easypay.contact.dto.ContactRequest;
import fintech2.easypay.contact.dto.ContactResponse;
import fintech2.easypay.contact.entity.Contact;
import fintech2.easypay.contact.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContactService {
    
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ContactResponse addContact(String phoneNumber, ContactRequest request) {
        User owner = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        // 중복 연락처 확인
        if (contactRepository.existsByOwnerIdAndContactPhoneNumber(owner.getId(), request.getContactPhoneNumber())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 등록된 연락처입니다.");
        }
        
        // 자기 자신 등록 방지
        if (owner.getPhoneNumber().equals(request.getContactPhoneNumber())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "본인의 전화번호는 연락처에 등록할 수 없습니다.");
        }
        
        // 등록된 사용자인지 확인
        User registeredUser = userRepository.findByPhoneNumber(request.getContactPhoneNumber())
                .orElse(null);
        
        Contact contact = Contact.builder()
                .owner(owner)
                .contactName(request.getContactName())
                .contactPhoneNumber(request.getContactPhoneNumber())
                .registeredUser(registeredUser)
                .isRegistered(registeredUser != null)
                .memo(request.getMemo())
                .build();
        
        Contact savedContact = contactRepository.save(contact);
        
        log.info("연락처 추가 완료: {} -> {}", phoneNumber, request.getContactPhoneNumber());
        
        return ContactResponse.from(savedContact);
    }
    
    public Page<ContactResponse> getContacts(String phoneNumber, Pageable pageable) {
        User owner = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Page<Contact> contacts = contactRepository.findByOwnerIdOrderByIsFavoriteDescCreatedAtDesc(owner.getId(), pageable);
        return contacts.map(ContactResponse::from);
    }
    
    public ContactResponse getContact(String phoneNumber, Long contactId) {
        User owner = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Contact contact = contactRepository.findByIdAndOwnerId(contactId, owner.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "연락처를 찾을 수 없습니다."));
        
        return ContactResponse.from(contact);
    }
    
    @Transactional
    public ContactResponse updateContact(String phoneNumber, Long contactId, ContactRequest request) {
        User owner = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Contact contact = contactRepository.findByIdAndOwnerId(contactId, owner.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "연락처를 찾을 수 없습니다."));
        
        // 전화번호 변경 시 중복 확인
        if (!contact.getContactPhoneNumber().equals(request.getContactPhoneNumber())) {
            if (contactRepository.existsByOwnerIdAndContactPhoneNumber(owner.getId(), request.getContactPhoneNumber())) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "이미 등록된 연락처입니다.");
            }
            
            // 등록된 사용자인지 다시 확인
            User registeredUser = userRepository.findByPhoneNumber(request.getContactPhoneNumber())
                    .orElse(null);
            
            contact.setContactPhoneNumber(request.getContactPhoneNumber());
            contact.setRegisteredUser(registeredUser);
            contact.setRegistered(registeredUser != null);
        }
        
        contact.setContactName(request.getContactName());
        contact.setMemo(request.getMemo());
        
        Contact savedContact = contactRepository.save(contact);
        
        log.info("연락처 수정 완료: {} -> {}", phoneNumber, contactId);
        
        return ContactResponse.from(savedContact);
    }
    
    @Transactional
    public void deleteContact(String phoneNumber, Long contactId) {
        User owner = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        Contact contact = contactRepository.findByIdAndOwnerId(contactId, owner.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "연락처를 찾을 수 없습니다."));
        
        contactRepository.delete(contact);
        
        log.info("연락처 삭제 완료: {} -> {}", phoneNumber, contactId);
    }
}