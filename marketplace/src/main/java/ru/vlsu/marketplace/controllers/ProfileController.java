package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.services.FavoriteService;
import ru.vlsu.marketplace.services.ReviewService;
import ru.vlsu.marketplace.services.UserService;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("reviews", reviewService.getByAuthor(user.getId()));
        model.addAttribute("favorites", favoriteService.getFavorites(user.getId()));
        return "profile";
    }

    @GetMapping("/{id}/avatar")
    @ResponseBody
    public ResponseEntity<byte[]> avatar(@PathVariable Integer id) {
        User user = userService.findById(id).orElseThrow();
        if (user.getProfilePic() != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(user.getProfilePic());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/edit")
    @ResponseBody
    public ResponseEntity<String> editProfile(@PathVariable Integer id,
                                               @RequestParam(required = false) String username,
                                               @RequestParam(required = false) String bio,
                                               @RequestParam(required = false) MultipartFile avatar,
                                               @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = userService.findById(id).orElseThrow();
        if (!user.getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).body("Нет доступа");
        }
        if (username != null && !username.isBlank()) user.setUsername(username);
        if (bio != null) user.setBio(bio);
        if (avatar != null && !avatar.isEmpty()) user.setProfilePic(avatar.getBytes());
        userService.save(user);
        return ResponseEntity.ok("OK");
    }
}
