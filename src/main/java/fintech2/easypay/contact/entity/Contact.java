package fintech2.easypay.contact.entity;

import fintech2.easypay.auth.entity.User;
import fintech2.easypay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contacts", 
    indexes = {
        @Index(name = "idx_owner_phone", columnList = "owner_id, contact_phone_number"),
        @Index(name = "idx_contact_phone", columnList = "contact_phone_number")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "contact_phone_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @Column(name = "contact_name", nullable = false)
    private String contactName;
    
    @Column(name = "contact_phone_number", nullable = false)
    private String contactPhoneNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_user_id")
    private User registeredUser;
    
    @Column(name = "is_registered")
    @Builder.Default
    private boolean isRegistered = false;
    
    @Column(name = "is_verified")
    @Builder.Default
    private boolean isVerified = false;
    
    @Column(name = "is_favorite")
    @Builder.Default
    private boolean isFavorite = false;
    
    @Column(name = "memo")
    private String memo;
    
    public void markAsRegistered(User registeredUser) {
        this.registeredUser = registeredUser;
        this.isRegistered = true;
    }
    
    public void markAsVerified() {
        this.isVerified = true;
    }
    
    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }
}