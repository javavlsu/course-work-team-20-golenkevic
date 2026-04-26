package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vlsu.marketplace.entities.Brand;
import ru.vlsu.marketplace.repositories.BrandRepository;

@Controller
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;

    @GetMapping("/brands")
    public String brandsList(Model model) {
        model.addAttribute("brands", brandRepository.findAll());
        return "brands";
    }

    @GetMapping("/brand/{id}/logo")
    @ResponseBody
    public ResponseEntity<byte[]> brandLogo(@PathVariable Integer id) {
        Brand brand = brandRepository.findById(id).orElseThrow();
        if (brand.getLogo() != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(brand.getLogo());
        }
        return ResponseEntity.notFound().build();
    }
}
