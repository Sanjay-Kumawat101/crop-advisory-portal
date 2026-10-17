package com.cropadvisory.controller;

import com.cropadvisory.dto.RecordForm;
import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        boolean farmer = isFarmer(authentication);
        model.addAttribute("records", recordService.listFor(authentication.getName(), farmer));
        model.addAttribute("isFarmer", farmer);
        return "records/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new RecordForm());
        return "records/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") RecordForm form,
                         BindingResult result,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "records/form";
        }
        AdvisoryRecord saved = recordService.create(authentication.getName(), form);
        redirectAttributes.addFlashAttribute("message",
                "Query submitted. Your reference number is " + saved.getId() + ".");
        return "redirect:/records/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        AdvisoryRecord record = recordService.getFor(id, authentication.getName(), isFarmer(authentication));
        model.addAttribute("record", record);
        return "records/detail";
    }

    private boolean isFarmer(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FARMER"));
    }
}
