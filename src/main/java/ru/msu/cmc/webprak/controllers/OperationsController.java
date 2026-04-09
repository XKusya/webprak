package ru.msu.cmc.webprak.controllers;

import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.msu.cmc.webprak.dao.*;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.client.Client;
import ru.msu.cmc.webprak.models.operation.*;
import ru.msu.cmc.webprak.models.subscription.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class OperationsController {
    private final OperationRepository operationRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final SubscriptionRepository subscriptionRepository;
    public OperationsController(
            OperationRepository operationRepository,
            ClientRepository clientRepository,
            ServiceRepository serviceRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.operationRepository = operationRepository;
        this.clientRepository = clientRepository;
        this.serviceRepository = serviceRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping("/operations")
    public String getOperations(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "opType", required = false) OperationType opType,
            @RequestParam(name = "clientId", required = false) Long clientId,
            @RequestParam(name = "serviceId", required = false) Long serviceId,
            Model model
    ) {
        Collection<Operation> operations = operationRepository.getAll();

        Timestamp fromTs = from != null ? Timestamp.valueOf(LocalDateTime.of(from, LocalTime.MIN)) : null;
        Timestamp toTs = to != null ? Timestamp.valueOf(LocalDateTime.of(to, LocalTime.MAX)) : null;

        Long accountId = null;
        if (clientId != null) {
            Client client = clientRepository.findDetailedById(clientId);
            if (client == null || client.getAccount() == null) {
                operations = List.of();
            } else {
                accountId = client.getAccount().getId();
            }
        }

        Long finalAccountId = accountId;
        operations = operations.stream()
                .filter(o -> fromTs == null || !o.getOpTime().before(fromTs))
                .filter(o -> toTs == null || !o.getOpTime().after(toTs))
                .filter(o -> opType == null || o.getOpType() == opType)
                .filter(o -> finalAccountId == null || Objects.equals(o.getAccount().getId(), finalAccountId))
                .filter(o -> serviceId == null || (o.getSubscription() != null
                        && Objects.equals(o.getSubscription().getService().getId(), serviceId)))
                .sorted((a, b) -> {
                    int cmp = b.getOpTime().compareTo(a.getOpTime());
                    return cmp != 0 ? cmp : b.getId().compareTo(a.getId());
                })
                .collect(Collectors.toList());

        model.addAttribute("operations", operations);
        model.addAttribute("clients", clientRepository.getAll());
        model.addAttribute("services", serviceRepository.getAll());
        model.addAttribute("opTypes", OperationType.values());
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("opType", opType);
        model.addAttribute("clientId", clientId);
        model.addAttribute("serviceId", serviceId);
        return "operations";
    }

    @GetMapping("/operations/new")
    public String getRegisterOperation(Model model) {
        OperationForm form = new OperationForm();
        form.setOpTime(LocalDateTime.now());
        model.addAttribute("operationForm", form);
        model.addAttribute("clients", clientRepository.getAll());
        model.addAttribute("subscriptions", subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE));
        model.addAttribute("opTypes", OperationType.values());
        return "register_operation";
    }

    @PostMapping("/operations/new")
    @Transactional
    public String postRegisterOperation(@ModelAttribute OperationForm operationForm) {
        if (operationForm.getClientId() == null || operationForm.getOpType() == null
                || operationForm.getAmount() == null) {
            return "redirect:/operations/new";
        }

        Client client = clientRepository.findDetailedById(operationForm.getClientId());
        if (client == null || client.getAccount() == null) {
            return "redirect:/operations";
        }

        Account account = client.getAccount();
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setOpType(operationForm.getOpType());
        operation.setOpTime(Timestamp.valueOf(
                operationForm.getOpTime() != null ? operationForm.getOpTime() : LocalDateTime.now()
        ));
        operation.setAmount(operationForm.getAmount());
        operation.setDescription(operationForm.getDescription());

        if (operationForm.getOpType() == OperationType.CHARGE && operationForm.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.getById(operationForm.getSubscriptionId());
            operation.setSubscription(subscription);
        }

        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        if (operationForm.getOpType() == OperationType.PAYMENT) {
            balance = balance.add(operationForm.getAmount());
        } else if (operationForm.getOpType() == OperationType.CHARGE) {
            balance = balance.subtract(operationForm.getAmount());
        }
        account.setBalance(balance);

        operationRepository.save(operation);
        clientRepository.update(client);

        return "redirect:/operations";
    }
}
