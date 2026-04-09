package ru.msu.cmc.webprak.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.msu.cmc.webprak.dao.ClientRepository;
import ru.msu.cmc.webprak.dao.ServiceRepository;
import ru.msu.cmc.webprak.dao.SubscriptionRepository;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.account.AccountState;
import ru.msu.cmc.webprak.models.client.*;
import ru.msu.cmc.webprak.models.client.json.*;
import ru.msu.cmc.webprak.models.subscription.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClientsController {
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ClientsController(
        ClientRepository clientRepository,
        ServiceRepository serviceRepository,
        SubscriptionRepository subscriptionRepository
    ) {
        this.clientRepository = clientRepository;
        this.serviceRepository = serviceRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping("/clients")
    public String getClients(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "clientType", required = false) ClientType clientType,
            @RequestParam(name = "accountState", required = false, defaultValue = "ALL") AccountState accountState,
            @RequestParam(name = "serviceId", required = false) Long serviceId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        Collection<Client> clients = loadClients(name, serviceId, from, to);
        clients = clients.stream()
                .filter(client -> clientType == null || clientType == client.getClientType())
                .filter(client -> accountState == AccountState.ALL || accountState.matches(client.getAccount()))
                .collect(Collectors.toList());

        model.addAttribute("clients", clients);
        model.addAttribute("services", serviceRepository.getAll());
        model.addAttribute("clientTypes", ClientType.values());
        model.addAttribute("accountStates", AccountState.values());
        model.addAttribute("name", name);
        model.addAttribute("clientType", clientType);
        model.addAttribute("accountState", accountState);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "clients";
    }

    @GetMapping("/clients/new")
    public String getRegisterClient(Model model) {
        Client client = new Client();
        ensureClientDefaults(client);
        model.addAttribute("client", client);
        return "register_client";
    }

    @PostMapping("/clients/new")
    public String postRegisterClient(@ModelAttribute Client client) {
        normalizeClient(client);
        client.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Account account = new Account();
        account.setClient(client);
        account.setBalance(BigDecimal.ZERO);
        account.setCreditLimit(BigDecimal.ZERO);
        account.setDebtDueDate(null);
        client.setAccount(account);
        clientRepository.save(client);
        return "redirect:/clients/" + client.getId();
    }

    @GetMapping("/clients/{id}")
    public String getClientCard(@PathVariable("id") Long id, Model model) {
        Client client = clientRepository.findDetailedById(id);
        if (client == null) {
            return "redirect:/clients";
        }
        Collection<Subscription> subscriptions = subscriptionRepository.findByClientId(id);
        model.addAttribute("client", client);
        model.addAttribute("subscriptions", subscriptions);
        return "client_card";
    }

    @GetMapping("/clients/{id}/edit")
    public String getEditClient(@PathVariable("id") Long id, Model model) {
        Client client = clientRepository.findDetailedById(id);
        if (client == null) {
            return "redirect:/clients";
        }
        ensureClientDefaults(client);

        model.addAttribute("client", client);
        return "edit_client";
    }

    @PostMapping("/clients/{id}/edit")
    public String postEditClient(@PathVariable("id") Long id, @ModelAttribute Client client) {
        client.setId(id);
        Client existing = clientRepository.findDetailedById(id);
        if (existing != null) {
            client.setAccount(existing.getAccount());
            client.setCreatedAt(existing.getCreatedAt());
        }
        normalizeClient(client);
        clientRepository.update(client);
        return "redirect:/clients/" + id;
    }

    private Collection<Client> loadClients(String name, Long serviceId, LocalDate from, LocalDate to) {
        if (serviceId != null && from != null && to != null) {
            Timestamp fromTs = Timestamp.valueOf(LocalDateTime.of(from, java.time.LocalTime.MIN));
            Timestamp toTs = Timestamp.valueOf(LocalDateTime.of(to, java.time.LocalTime.MAX));
            return clientRepository.findByServiceAndPeriod(serviceId, fromTs, toTs);
        }
        if (name != null && !name.isBlank()) {
            return clientRepository.findByNameContaining(name.trim());
        }
        return clientRepository.getAll();
    }

    private void normalizeClient(Client client) {
        if (client.getDetails() == null) {
            client.setDetails(new ClientDetails());
        }
        if (client.getDetails().getDocument() == null) {
            client.getDetails().setDocument(new Document());
        }
        if (client.getContacts() == null) {
            client.setContacts(new ArrayList<>());
            return;
        }
        List<Contact> normalized = client.getContacts().stream()
            .filter(contact -> contact.getType() != null && contact.getValue() != null && !contact.getValue().isBlank())
            .collect(Collectors.toList());
        client.setContacts(normalized);
    }

    private void ensureClientDefaults(Client client) {
        if (client.getDetails() == null) {
            client.setDetails(new ClientDetails());
        }
        if (client.getDetails().getDocument() == null) {
            client.getDetails().setDocument(new Document());
        }
        if (client.getContacts() == null) {
            client.setContacts(new ArrayList<>());
        }
        while (client.getContacts().size() < 2) {
            client.getContacts().add(new Contact());
        }
    }
}
