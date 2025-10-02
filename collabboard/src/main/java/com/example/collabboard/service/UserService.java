    package com.example.collabboard.service;

    import com.example.collabboard.model.User;
    import com.example.collabboard.repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    @Service
    public class UserService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        public User registerUser(String username, String email, String password) throws Exception {
            // Check if user already exists
            if (userRepository.findByUsername(username).isPresent()) {
                throw new Exception("Username already exists");
            }
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            // Hash the password before saving
            user.setPassword(passwordEncoder.encode(password));
            return userRepository.save(user);
        }

        public User loginUser(String username, String password) throws Exception {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new Exception("Invalid username or password"));

            // Check if the provided password matches the stored hash
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user; // Login successful
            } else {
                throw new Exception("Invalid username or password");
            }
        }
    }
    
