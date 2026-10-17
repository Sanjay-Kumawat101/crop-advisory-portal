package com.cropadvisory.controller;

import com.cropadvisory.dto.RecordForm;
import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.model.Role;
import com.cropadvisory.model.Status;
import com.cropadvisory.model.User;
import com.cropadvisory.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/records")
public class RecordController {

    private final RecordService service;

    public RecordController(RecordService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Status status,
                       Authentication auth, Model model) {
        User u = service.currentUser(auth.getName());
        model.addAttribute("records", service.search(u, q, status));
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", Status.values());
        return "records/list";
    }

    @GetMapping("/new")
    public String newForm(Authentication auth, Model model) {
        User u = service.currentUser(auth.getName());
        if (u.getRole() != Role.FARMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return formView(model, new RecordForm(), "New crop query", "/records");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") RecordForm form, BindingResult result,
                         Authentication auth, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return formView(model, form, "New crop query", "/records");
        }
        AdvisoryRecord r = service.create(service.currentUser(auth.getName()), form);
        ra.addFlashAttribute("message", "Query submitted.");
        return "redirect:/records/" + r.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        User u = service.currentUser(auth.getName());
        AdvisoryRecord r = service.get(u, id);
        model.addAttribute("record", r);
        model.addAttribute("history", service.history(id));
        model.addAttribute("allowed", service.allowedNext(u, r));
        model.addAttribute("canEdit", service.canEdit(u, r));
        return "records/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        User u = service.currentUser(auth.getName());
        AdvisoryRecord r = service.get(u, id);
        if (!service.canEdit(u, r)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return formView(model, RecordForm.from(r), "Edit query #" + id, "/records/" + id + "/edit");
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") RecordForm form,
                         BindingResult result, Authentication auth, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return formView(model, form, "Edit query #" + id, "/records/" + id + "/edit");
        }
        service.update(service.currentUser(auth.getName()), id, form);
        ra.addFlashAttribute("message", "Query updated.");
        return "redirect:/records/" + id;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam Status target,
                               @RequestParam(required = false) String advice,
                               Authentication auth, RedirectAttributes ra) {
        try {
            service.changeStatus(service.currentUser(auth.getName()), id, target, advice);
            ra.addFlashAttribute("message", "Status changed to " + target.getLabel() + ".");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/records/" + id;
    }

    private String formView(Model model, RecordForm form, String title, String action) {
        model.addAttribute("form", form);
        model.addAttribute("title", title);
        model.addAttribute("formAction", action);
        return "records/form";
    }
}