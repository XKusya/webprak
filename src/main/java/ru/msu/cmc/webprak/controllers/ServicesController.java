package ru.msu.cmc.webprak.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.msu.cmc.webprak.dao.ServiceRepository;
import ru.msu.cmc.webprak.models.service.Service;
import ru.msu.cmc.webprak.models.service.ServiceStatus;
import ru.msu.cmc.webprak.models.service.json.ServiceBilling;
import ru.msu.cmc.webprak.models.servicetype.ServiceType;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class ServicesController {
    private final ServiceRepository serviceRepository;

    public ServicesController(
            ServiceRepository serviceRepository
    ) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/services")
    public String getServices(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "serviceTypeId", required = false) Long serviceTypeId,
            @RequestParam(name = "status", required = false, defaultValue = "ALL") ServiceStatus status,
            Model model
    ) {
        Collection<Service> services = serviceRepository.getAll();
        services = services.stream()
                .filter(s -> name == null || name.isBlank() || s.getName().toLowerCase().contains(name.trim().toLowerCase()))
                .filter(s -> serviceTypeId == null || (s.getServiceType() != null
                        && Objects.equals(s.getServiceType().getId(), serviceTypeId)))
                .filter(s -> status == ServiceStatus.ALL || status.matches(s))
                .collect(Collectors.toList());

        model.addAttribute("services", services);
        model.addAttribute("serviceTypes", serviceRepository.getServiceTypes());
        model.addAttribute("name", name);
        model.addAttribute("serviceTypeId", serviceTypeId);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ServiceStatus.values());
        return "services";
    }

    @GetMapping("/services/new")
    public String getRegisterService(Model model) {
        Service service = new Service();
        service.setBilling(new ServiceBilling());
        model.addAttribute("service", service);
        model.addAttribute("serviceTypes", serviceRepository.getServiceTypes());
        return "register_service";
    }

    @PostMapping("/services/new")
    public String postRegisterService(@ModelAttribute Service service) {
        if (service.getBilling() == null) {
            service.setBilling(new ServiceBilling());
        }
        if (service.getIsActive() == null) {
            service.setIsActive(false);
        }
        if (service.getServiceType() == null || service.getServiceType().getId() == null) {
            return "redirect:/services/new";
        }
        ServiceType serviceType = serviceRepository.getServiceTypeById(service.getServiceType().getId());
        service.setServiceType(serviceType);
        serviceRepository.save(service);
        return "redirect:/services";
    }

    @GetMapping("/services/{id}")
    public String getService(@PathVariable("id") Long id, Model model) {
        Service service = serviceRepository.getById(id);
        if (service == null) {
            return "redirect:/services";
        }
        if (service.getBilling() == null) {
            service.setBilling(new ServiceBilling());
        }
        model.addAttribute("service", service);
        return "service_card";
    }

    @GetMapping("/services/{id}/edit")
    public String getEditService(@PathVariable("id") Long id, Model model) {
        Service service = serviceRepository.getById(id);
        if (service == null) {
            return "redirect:/services";
        }
        if (service.getBilling() == null) {
            service.setBilling(new ServiceBilling());
        }
        model.addAttribute("service", service);
        model.addAttribute("serviceTypes", serviceRepository.getServiceTypes());
        return "edit_service";
    }

    @PostMapping("/services/{id}/edit")
    public String postEditService(@PathVariable("id") Long id, @ModelAttribute Service service) {
        service.setId(id);
        if (service.getBilling() == null) {
            service.setBilling(new ServiceBilling());
        }
        if (service.getIsActive() == null) {
            service.setIsActive(false);
        }
        if (service.getServiceType() == null || service.getServiceType().getId() == null) {
            return "redirect:/services/" + id + "/edit";
        }
        ServiceType serviceType = serviceRepository.getServiceTypeById(service.getServiceType().getId());
        service.setServiceType(serviceType);
        serviceRepository.update(service);
        return "redirect:/services/" + id;
    }
}
