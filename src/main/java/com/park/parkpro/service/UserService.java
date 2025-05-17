package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.domain.VerificationToken;
import com.park.parkpro.domain.PasswordResetToken;
import com.park.parkpro.dto.AdminUpdateUserRequestDto;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.SignupRequestDto;
import com.park.parkpro.dto.UpdateUserProfileRequestDto;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ConflictException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.repository.VerificationTokenRepository;
import com.park.parkpro.repository.PasswordResetTokenRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParkRepository parkRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "FINANCE_OFFICER", "PARK_MANAGER", "VISITOR", "GOVERNMENT_OFFICER", "AUDITOR");
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ParkRepository parkRepository,
                       VerificationTokenRepository verificationTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository,
                       JavaMailSender mailSender, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.parkRepository = parkRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
    }

    public Boolean doesUserAlreadyExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public User createUser(CreateUserRequestDto request) {
        validateUserInput(request.getEmail(), request.getPassword(), request.getRole());
        if (doesUserAlreadyExist(request.getEmail())) {
            throw new ConflictException("User with email "+request.getEmail()+" already exists");
        } else {
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setRole(request.getRole());
            user.setActive(true); // Admin-created users are active immediately
            user.setMustResetPassword(true); // Require reset on first login
            User savedUser = userRepository.save(user);

            sendTempPasswordEmail(savedUser.getEmail(), tempPassword);

            return savedUser;
        }
    }

    @Transactional
    public User signup(SignupRequestDto request) {
        validateUserInput(request.getEmail(), request.getPassword(), "VISITOR");
        if (doesUserAlreadyExist(request.getEmail())) {
            throw new ConflictException("User with email "+request.getEmail()+" already exists");
        } else {
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole("VISITOR");
            user.setActive(false); // Inactive until verified
            User savedUser = userRepository.save(user);
            String verificationCode = String.format("%06d", new Random().nextInt(999999));
            VerificationToken token = new VerificationToken(verificationCode, savedUser, LocalDateTime.now().plusHours(24));
            verificationTokenRepository.save(token);
            System.out.println(verificationCode);
            sendVerificationEmail(savedUser.getEmail(), verificationCode);
            return savedUser;
        }
    }

    @Transactional
    public ResponseEntity<String> sendNewVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email "+email+" not found"));
        // Find the previous code
        VerificationToken token = verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Previous verification token not found"));
        // Delete the previous code first
        verificationTokenRepository.delete(token);
        // Create new code
        String newVerificationCode = String.format("%06d", new Random().nextInt(999999));
        VerificationToken newToken = new VerificationToken(newVerificationCode, user, LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(newToken);
        System.out.println(newVerificationCode);
        // Send the new code via email
        sendVerificationEmail(email, newVerificationCode);
        return ResponseEntity.ok("A new verification code was sent to your email.");
    }

    @Transactional
    public void verifyAccount(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email '" + email + "' not found"));
        VerificationToken token = verificationTokenRepository.findByTokenAndUser(code, user)
                .orElseThrow(() -> new BadRequestException("Invalid verification code"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired");
        }
        user.setActive(true);
        userRepository.save(user);
        verificationTokenRepository.delete(token);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email '" + email + "' not found"));
        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(resetToken, user, LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(token);
        String userRole = user.getRole();
        System.out.println("User role: "+userRole);
        String userRolePath = switch (userRole) {
            case "ADMIN" -> "/admin";
            case "FINANCE_OFFICER" -> "/finance";
            case "PARK_MANAGER" -> "/manager";
            case "GOVERNMENT_OFFICER" -> "/government";
            case "AUDITOR" -> "/auditor";
            default -> "";
        };
        sendPasswordResetEmail(user.getEmail(), resetToken, userRolePath);
    }

    @Transactional
    public User updateUserProfile(UUID userId, UpdateUserProfileRequestDto request, String token) {
        String emailFromToken = jwtUtil.getEmailFromToken(token);
        User requestingUser = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + emailFromToken));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Authorization check: User can only update their own profile unless ADMIN
        if (!requestingUser.getId().equals(targetUser.getId()) && !"ADMIN".equals(requestingUser.getRole())) {
            throw new ForbiddenException("You can only update your own profile");
        }

        // Check if email is being changed and ensure uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(targetUser.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BadRequestException("Email " + request.getEmail() + " is already in use");
            }
            targetUser.setEmail(request.getEmail());
        }

        // Update fields if provided
        targetUser.setFirstName(request.getFirstName());
        targetUser.setLastName(request.getLastName());
        if (request.getPhone() != null) targetUser.setPhone(request.getPhone());
        if (request.getGender() != null) targetUser.setGender(request.getGender());
        if (request.getPassportNationalId() != null) targetUser.setPassportNationalId(request.getPassportNationalId());
        if (request.getNationality() != null) targetUser.setNationality(request.getNationality());
        if (request.getAge() != null) targetUser.setAge(request.getAge());

        targetUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(targetUser);
    }

    @Transactional
    public User adminUpdateUser(UUID userId, AdminUpdateUserRequestDto request, String token) {
        String emailFromToken = jwtUtil.getEmailFromToken(token);
        User requestingUser = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + emailFromToken));

        // Authorization check: Only ADMIN can update any user's account
        if (!"ADMIN".equals(requestingUser.getRole())) {
            throw new ForbiddenException("Only ADMIN can update user accounts");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Update email with uniqueness check
        if (request.getEmail() != null && !request.getEmail().equals(targetUser.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ConflictException("Email " + request.getEmail() + " is already in use");
            }
            targetUser.setEmail(request.getEmail());
        }

        // Update basic fields if provided
        if (request.getFirstName() != null) targetUser.setFirstName(request.getFirstName());
        if (request.getLastName() != null) targetUser.setLastName(request.getLastName());
        if (request.getPhone() != null) targetUser.setPhone(request.getPhone());
        if (request.getGender() != null) targetUser.setGender(request.getGender());
        if (request.getPassportNationalId() != null) targetUser.setPassportNationalId(request.getPassportNationalId());
        if (request.getNationality() != null) targetUser.setNationality(request.getNationality());
        if (request.getAge() != null) targetUser.setAge(request.getAge());
        if (request.getIsActive() != null) targetUser.setActive(request.getIsActive());

        // Update role with validation
        if (request.getRole() != null) {
            if (!VALID_ROLES.contains(request.getRole())) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
            targetUser.setRole(request.getRole());
        }

        // Update status fields
        if (request.getIsActive() != null) targetUser.setActive(request.getIsActive());
        if (request.getMustResetPassword() != null) targetUser.setMustResetPassword(request.getMustResetPassword());

        // Update park assignment
        if (request.getParkId() != null) {
            Park park = parkRepository.findById(request.getParkId())
                    .orElseThrow(() -> new NotFoundException("Park with ID: " + request.getParkId() + " not found"));
            if (!Arrays.asList("PARK_MANAGER", "FINANCE_OFFICER").contains(targetUser.getRole())) {
                throw new BadRequestException("Only PARK_MANAGER or FINANCE_OFFICER can be assigned to a park");
            }
            targetUser.setPark(park);
        } else if (request.getParkId() == null && targetUser.getPark() != null) {
            targetUser.setPark(null); // Allow clearing park assignment
        }

        targetUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(targetUser);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustResetPassword(false); // Clear reset flag
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    @Transactional
    public void assignParkToUser(UUID userId, UUID parkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID '" + userId + "' not found"));
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park with ID '" + parkId + "' not found"));
        if (!"PARK_MANAGER".equals(user.getRole()) && !"FINANCE_OFFICER".equals(user.getRole())) {
            throw new BadRequestException("Only Park staff and Finance officer users can be assigned to a park");
        }
        if (user.getPark() != null) {
            throw new ConflictException("User " + userId + " is already assigned to park " + user.getPark().getId());
        }
        user.setPark(park);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with ID '" + id + "' not found"));
    }

    public List<User> getUsersByRole(String role) {
        if (!VALID_ROLES.contains(role)) {
            throw new BadRequestException("Invalid role: " + role);
        }
        List<User> users = userRepository.findByRole(role);
        if (users.isEmpty()) {
            throw new NotFoundException("No users found with role: " + role);
        }
        return users;
    }

    public List<User> getUsersByParkId(UUID parkId) {
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park with ID '" + parkId + "' not found");
        }
        List<User> users = userRepository.findByParkId(parkId);
        if (users.isEmpty()) {
            throw new NotFoundException("No users found for park with ID: " + parkId);
        }
        return users;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email '" + email + "' not found"));
    }

    @Transactional
    public void deleteUser(UUID userId, String token) {
        String emailFromToken = jwtUtil.getEmailFromToken(token);
        User requestingUser = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + emailFromToken));

        // Authorization check: Only ADMIN can delete users
        if (!"ADMIN".equals(requestingUser.getRole())) {
            throw new ForbiddenException("Only ADMIN can delete user accounts");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Prevent admin from deleting their own account
        if (requestingUser.getId().equals(targetUser.getId())) {
            throw new BadRequestException("Admins cannot delete their own accounts");
        }

        // Delete associated tokens
        verificationTokenRepository.findByUser(targetUser).ifPresent(verificationTokenRepository::delete);
        passwordResetTokenRepository.findByUser(targetUser).ifPresent(passwordResetTokenRepository::delete);

        // Delete the user
        userRepository.delete(targetUser);
    }

    private void validateUserInput(String email, String password, String role) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
        if (!VALID_ROLES.contains(role)) {
            throw new BadRequestException("Invalid role: " + role);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Email '" + email + "' is already taken");
        }
    }

    private void sendVerificationEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify Your Account");
        message.setText("Your verification code is: " + code);
        mailSender.send(message);
    }

    private void sendTempPasswordEmail(String email, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Account Credentials");
        message.setText("Your temporary password is: " + tempPassword + "\nPlease reset it on your first login.");
        mailSender.send(message);
    }

    private void sendPasswordResetEmail(String email, String resetToken, String rolePath) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Your Password");
        message.setText("Click this link to reset your password: http://localhost:3000/auth"+rolePath+"/reset-password/?token=" + resetToken);
        System.out.println("EMAIL:");
        System.out.println("Click this link to reset your password: http://localhost:3000/auth"+rolePath+"/reset-password/?token=" + resetToken);
        mailSender.send(message);
    }
}